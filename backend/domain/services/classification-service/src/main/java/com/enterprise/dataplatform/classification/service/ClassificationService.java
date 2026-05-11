package com.enterprise.dataplatform.classification.service;

import com.enterprise.dataplatform.classification.domain.entity.AssetClassification;
import com.enterprise.dataplatform.classification.domain.entity.ClassificationRule;
import com.enterprise.dataplatform.classification.domain.enums.SensitivityLevel;
import com.enterprise.dataplatform.classification.dto.request.ClassificationRequest;
import com.enterprise.dataplatform.classification.dto.request.ClassificationRuleRequest;
import com.enterprise.dataplatform.classification.dto.response.ClassificationResponse;
import com.enterprise.dataplatform.classification.repository.AssetClassificationRepository;
import com.enterprise.dataplatform.classification.repository.ClassificationRuleRepository;
import com.enterprise.dataplatform.classification.strategy.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassificationService {

    private final ClassificationRuleRepository ruleRepository;
    private final AssetClassificationRepository classificationRepository;
    private final List<ClassificationStrategy> strategies;

    @Transactional
    public ClassificationResponse classifyAsset(ClassificationRequest request) {
        log.info("开始分类资产: assetId={}, columnName={}", request.getAssetId(), request.getColumnName());

        AssetClassification existing = classificationRepository
                .findByAssetId(request.getAssetId())
                .orElse(null);

        if (existing != null && !Boolean.TRUE.equals(request.getForceClassify())) {
            return toResponse(existing, "Asset already classified");
        }

        List<ClassificationRule> activeRules = ruleRepository.findByIsActiveTrue();
        List<ClassificationRule> sortedRules = activeRules.stream()
                .sorted(Comparator.comparing(ClassificationRule::getPriority).reversed())
                .collect(Collectors.toList());

        ClassificationResponse bestMatch = null;
        double highestConfidence = 0.0;

        for (ClassificationRule rule : sortedRules) {
            ClassificationResponse response = tryClassify(request, rule);
            if (response != null && "CLASSIFIED".equals(response.getStatus())) {
                if (response.getConfidenceScore() > highestConfidence) {
                    highestConfidence = response.getConfidenceScore();
                    bestMatch = response;
                }
                if (highestConfidence >= 0.95) {
                    break;
                }
            }
        }

        if (bestMatch == null) {
            bestMatch = ClassificationResponse.builder()
                    .assetId(request.getAssetId())
                    .assetName(request.getColumnName())
                    .assetType(request.getAssetType())
                    .sensitivityLevel(SensitivityLevel.INTERNAL)
                    .confidenceScore(0.5)
                    .classificationMethod("DEFAULT")
                    .status("CLASSIFIED")
                    .message("Default classification applied")
                    .build();
        }

        AssetClassification classification = saveClassification(request, bestMatch);

        boolean requiresApproval = bestMatch.getConfidenceScore() < 0.85
                || classification.getSensitivityLevel().getLevel() >= SensitivityLevel.CONFIDENTIAL.getLevel();
        bestMatch.setRequiresApproval(requiresApproval);

        if (requiresApproval) {
            bestMatch.setStatus("PENDING_APPROVAL");
            classification.setApprovalStatus("PENDING");
            classificationRepository.save(classification);
        }

        log.info("资产分类完成: assetId={}, level={}, confidence={}, requiresApproval={}",
                request.getAssetId(), bestMatch.getSensitivityLevel(),
                bestMatch.getConfidenceScore(), requiresApproval);

        return bestMatch;
    }

    private ClassificationResponse tryClassify(ClassificationRequest request, ClassificationRule rule) {
        for (ClassificationStrategy strategy : strategies) {
            if (strategy.match(rule)) {
                try {
                    return strategy.classify(
                            request.getAssetId(),
                            request.getColumnName(),
                            request.getDataType(),
                            request.getSampleValues(),
                            rule
                    );
                } catch (Exception e) {
                    log.warn("Strategy {} failed for rule {}", strategy.getClass().getSimpleName(), rule.getRuleName(), e);
                }
            }
        }
        return null;
    }

    private AssetClassification saveClassification(ClassificationRequest request, ClassificationResponse response) {
        AssetClassification classification = classificationRepository
                .findByAssetId(request.getAssetId())
                .orElse(AssetClassification.builder()
                        .assetId(request.getAssetId())
                        .createdBy("SYSTEM")
                        .build());

        if (classification.getSensitivityLevel() != null && response.getConfidenceScore() != null) {
            classification.setPreviousLevel(classification.getSensitivityLevel().name());
        }

        classification.setAssetName(request.getAssetName() != null ? request.getAssetName() : request.getColumnName());
        classification.setAssetType(request.getAssetType());
        classification.setSensitivityLevel(response.getSensitivityLevel());
        classification.setConfidenceScore(response.getConfidenceScore());
        classification.setClassificationMethod(response.getClassificationMethod());
        classification.setMatchedRuleId(null);
        classification.setMatchedRuleName(response.getMatchedRuleName());
        classification.setClassificationStatus("CLASSIFIED");
        classification.setManualOverride(false);
        classification.setUpdateTime(LocalDateTime.now());

        return classificationRepository.save(classification);
    }

    private ClassificationResponse toResponse(AssetClassification classification, String message) {
        return ClassificationResponse.builder()
                .assetId(classification.getAssetId())
                .assetName(classification.getAssetName())
                .assetType(classification.getAssetType())
                .sensitivityLevel(classification.getSensitivityLevel())
                .confidenceScore(classification.getConfidenceScore())
                .classificationMethod(classification.getClassificationMethod())
                .matchedRuleName(classification.getMatchedRuleName())
                .status(classification.getClassificationStatus())
                .message(message)
                .build();
    }

    @Transactional
    public ClassificationRule createRule(ClassificationRuleRequest request) {
        ClassificationRule rule = ClassificationRule.builder()
                .ruleName(request.getRuleName())
                .ruleDescription(request.getRuleDescription())
                .ruleType(request.getRuleType())
                .sensitivityLevel(request.getSensitivityLevel())
                .pattern(request.getPattern())
                .dataType(request.getDataType())
                .keywords(request.getKeywords() != null ? String.join(";", request.getKeywords()) : null)
                .columnPattern(request.getColumnPattern())
                .priority(request.getPriority())
                .isActive(true)
                .confidenceThreshold(request.getConfidenceThreshold() != null ? request.getConfidenceThreshold() : 0.7)
                .autoClassify(request.getAutoClassify() != null ? request.getAutoClassify() : true)
                .triggerApproval(request.getTriggerApproval() != null ? request.getTriggerApproval() : true)
                .createdBy("SYSTEM")
                .build();

        return ruleRepository.save(rule);
    }

    public List<ClassificationRule> getAllRules() {
        return ruleRepository.findAll();
    }

    public List<ClassificationRule> getActiveRules() {
        return ruleRepository.findByIsActiveTrue();
    }

    @Transactional
    public ClassificationRule updateRule(Long ruleId, ClassificationRuleRequest request) {
        ClassificationRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        if (request.getRuleName() != null) rule.setRuleName(request.getRuleName());
        if (request.getRuleDescription() != null) rule.setRuleDescription(request.getRuleDescription());
        if (request.getRuleType() != null) rule.setRuleType(request.getRuleType());
        if (request.getSensitivityLevel() != null) rule.setSensitivityLevel(request.getSensitivityLevel());
        if (request.getPattern() != null) rule.setPattern(request.getPattern());
        if (request.getDataType() != null) rule.setDataType(request.getDataType());
        if (request.getKeywords() != null) rule.setKeywords(String.join(",", request.getKeywords()));
        if (request.getColumnPattern() != null) rule.setColumnPattern(request.getColumnPattern());
        if (request.getPriority() != null) rule.setPriority(request.getPriority());
        if (request.getConfidenceThreshold() != null) rule.setConfidenceThreshold(request.getConfidenceThreshold());
        if (request.getAutoClassify() != null) rule.setAutoClassify(request.getAutoClassify());
        if (request.getTriggerApproval() != null) rule.setTriggerApproval(request.getTriggerApproval());

        return ruleRepository.save(rule);
    }

    @Transactional
    public void deleteRule(Long ruleId) {
        ruleRepository.deleteById(ruleId);
    }

    @Transactional
    public void toggleRuleStatus(Long ruleId) {
        ClassificationRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        rule.setIsActive(!rule.getIsActive());
        ruleRepository.save(rule);
    }

    public AssetClassification getClassification(String assetId) {
        return classificationRepository.findByAssetId(assetId)
                .orElse(null);
    }

    public List<AssetClassification> getClassificationsByLevel(SensitivityLevel level) {
        return classificationRepository.findBySensitivityLevel(level);
    }

    public Map<SensitivityLevel, Long> getClassificationStatistics() {
        List<AssetClassification> all = classificationRepository.findAll();
        return all.stream()
                .collect(Collectors.groupingBy(
                        AssetClassification::getSensitivityLevel,
                        Collectors.counting()
                ));
    }

    @Transactional
    public ClassificationResponse approveClassification(String assetId, String approvedBy, String comment) {
        AssetClassification classification = classificationRepository.findByAssetId(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Classification not found: " + assetId));

        classification.setApprovalStatus("APPROVED");
        classification.setApprovedBy(approvedBy);
        classification.setApprovedTime(LocalDateTime.now());
        classification.setReviewComment(comment);

        classificationRepository.save(classification);

        return toResponse(classification, "Classification approved");
    }

    @Transactional
    public ClassificationResponse rejectClassification(String assetId, String rejectedBy, String reason) {
        AssetClassification classification = classificationRepository.findByAssetId(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Classification not found: " + assetId));

        classification.setApprovalStatus("REJECTED");
        classification.setReviewedBy(rejectedBy);
        classification.setReviewTime(LocalDateTime.now());
        classification.setReviewComment(reason);

        classificationRepository.save(classification);

        ClassificationResponse response = toResponse(classification, "Classification rejected");
        response.setStatus("REJECTED");
        return response;
    }

    @Transactional
    public ClassificationResponse manualOverride(String assetId, SensitivityLevel newLevel, String reason, String operator) {
        AssetClassification classification = classificationRepository.findByAssetId(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Classification not found: " + assetId));

        classification.setPreviousLevel(classification.getSensitivityLevel().name());
        classification.setSensitivityLevel(newLevel);
        classification.setManualOverride(true);
        classification.setReason(reason);
        classification.setReviewedBy(operator);
        classification.setReviewTime(LocalDateTime.now());

        classificationRepository.save(classification);

        ClassificationResponse response = toResponse(classification, "Manual override applied");
        response.setStatus("OVERRIDDEN");
        return response;
    }

    public double calculateAutoClassificationAccuracy() {
        List<AssetClassification> classified = classificationRepository.findByClassificationMethod("AUTO");
        if (classified.isEmpty()) {
            return 0.0;
        }

        long approvedCount = classified.stream()
                .filter(c -> !"REJECTED".equals(c.getApprovalStatus()))
                .count();

        return (double) approvedCount / classified.size() * 100;
    }
}
