package com.enterprise.dataplatform.quality.service;

import com.enterprise.dataplatform.quality.domain.entity.QualityRule;
import com.enterprise.dataplatform.quality.dto.request.QualityRuleRequest;
import com.enterprise.dataplatform.quality.dto.response.QualityRuleResponse;
import com.enterprise.dataplatform.quality.repository.QualityRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 质量规则服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QualityRuleService {

    private final QualityRuleRepository ruleRepository;

    /**
     * 创建质量规则
     */
    @Transactional
    public QualityRuleResponse createRule(QualityRuleRequest request, String creator) {
        log.info("创建质量规则: {}, 创建人: {}", request.getRuleCode(), creator);

        if (ruleRepository.existsByRuleCode(request.getRuleCode())) {
            throw new IllegalArgumentException("规则编码已存在: " + request.getRuleCode());
        }

        QualityRule rule = QualityRule.builder()
                .ruleCode(request.getRuleCode())
                .ruleName(request.getRuleName())
                .description(request.getDescription())
                .ruleType(request.getRuleType())
                .ruleCategory(request.getRuleCategory())
                .ruleExpression(request.getRuleExpression())
                .assetId(request.getAssetId())
                .assetType(request.getAssetType())
                .fieldName(request.getFieldName())
                .qualityDimension(request.getQualityDimension())
                .severityLevel(request.getSeverityLevel())
                .thresholdExpression(request.getThresholdExpression())
                .expectedValue(request.getExpectedValue())
                .alertThreshold(request.getAlertThreshold())
                .errorThreshold(request.getErrorThreshold())
                .enabled(true)
                .status("DRAFT")
                .priority(request.getPriority())
                .version(1)
                .creator(creator)
                .build();

        rule = ruleRepository.save(rule);

        log.info("质量规则创建成功: {}", rule.getId());
        return toResponse(rule);
    }

    /**
     * 更新质量规则
     */
    @Transactional
    public QualityRuleResponse updateRule(Long id, QualityRuleRequest request, String updater) {
        log.info("更新质量规则: {}, 更新人: {}", id, updater);

        QualityRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + id));

        rule.setRuleName(request.getRuleName());
        rule.setDescription(request.getDescription());
        rule.setRuleType(request.getRuleType());
        rule.setRuleCategory(request.getRuleCategory());
        rule.setRuleExpression(request.getRuleExpression());
        rule.setAssetId(request.getAssetId());
        rule.setAssetType(request.getAssetType());
        rule.setFieldName(request.getFieldName());
        rule.setQualityDimension(request.getQualityDimension());
        rule.setSeverityLevel(request.getSeverityLevel());
        rule.setThresholdExpression(request.getThresholdExpression());
        rule.setExpectedValue(request.getExpectedValue());
        rule.setAlertThreshold(request.getAlertThreshold());
        rule.setErrorThreshold(request.getErrorThreshold());
        rule.setPriority(request.getPriority());
        rule.setVersion(rule.getVersion() + 1);
        rule.setUpdater(updater);

        rule = ruleRepository.save(rule);

        log.info("质量规则更新成功: {}", id);
        return toResponse(rule);
    }

    /**
     * 发布规则
     */
    @Transactional
    public QualityRuleResponse publishRule(Long id, String publisher) {
        log.info("发布质量规则: {}, 发布人: {}", id, publisher);

        QualityRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + id));

        rule.setStatus("ACTIVE");
        rule.setEnabled(true);
        rule.setUpdater(publisher);

        rule = ruleRepository.save(rule);

        log.info("质量规则发布成功: {}", id);
        return toResponse(rule);
    }

    /**
     * 启用/禁用规则
     */
    @Transactional
    public QualityRuleResponse setRuleEnabled(Long id, boolean enabled, String updater) {
        log.info("{}质量规则: {}", enabled ? "启用" : "禁用", id);

        QualityRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + id));

        rule.setEnabled(enabled);
        rule.setUpdater(updater);

        rule = ruleRepository.save(rule);

        log.info("质量规则状态更新成功: {}", id);
        return toResponse(rule);
    }

    /**
     * 查询规则
     */
    public QualityRuleResponse getRule(Long id) {
        QualityRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + id));
        return toResponse(rule);
    }

    /**
     * 根据编码查询
     */
    public QualityRuleResponse getRuleByCode(String ruleCode) {
        QualityRule rule = ruleRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + ruleCode));
        return toResponse(rule);
    }

    /**
     * 分页查询
     */
    public Page<QualityRuleResponse> searchRules(
            String ruleType, String status, String keyword, Pageable pageable) {
        return ruleRepository.searchRules(ruleType, status, keyword, pageable)
                .map(this::toResponse);
    }

    /**
     * 获取启用的规则
     */
    public List<QualityRuleResponse> getEnabledRules() {
        return ruleRepository.findByEnabled(true).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据资产获取规则
     */
    public List<QualityRuleResponse> getRulesByAssetId(String assetId) {
        return ruleRepository.findEnabledRulesByAssetId(assetId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 删除规则
     */
    @Transactional
    public void deleteRule(Long id) {
        log.info("删除质量规则: {}", id);
        ruleRepository.deleteById(id);
    }

    private QualityRuleResponse toResponse(QualityRule rule) {
        return QualityRuleResponse.builder()
                .id(rule.getId())
                .ruleCode(rule.getRuleCode())
                .ruleName(rule.getRuleName())
                .description(rule.getDescription())
                .ruleType(rule.getRuleType())
                .ruleCategory(rule.getRuleCategory())
                .ruleExpression(rule.getRuleExpression())
                .assetId(rule.getAssetId())
                .assetType(rule.getAssetType())
                .fieldName(rule.getFieldName())
                .qualityDimension(rule.getQualityDimension())
                .severityLevel(rule.getSeverityLevel())
                .thresholdExpression(rule.getThresholdExpression())
                .expectedValue(rule.getExpectedValue())
                .alertThreshold(rule.getAlertThreshold())
                .errorThreshold(rule.getErrorThreshold())
                .enabled(rule.getEnabled())
                .status(rule.getStatus())
                .priority(rule.getPriority())
                .version(rule.getVersion())
                .creator(rule.getCreator())
                .createTime(rule.getCreateTime())
                .updater(rule.getUpdater())
                .updateTime(rule.getUpdateTime())
                .build();
    }
}
