package com.enterprise.dataplatform.governance.controller;

import com.enterprise.dataplatform.governance.domain.entity.AIRecommendation;
import com.enterprise.dataplatform.governance.dto.request.AIRecommendationRequest;
import com.enterprise.dataplatform.governance.dto.response.AIRecommendationResponse;
import com.enterprise.dataplatform.governance.service.AIRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/governance/ai")
@RequiredArgsConstructor
@Tag(name = "AI智能推荐", description = "基于AI的治理推荐和决策支持")
public class AIRecommendationController {

    private final AIRecommendationService recommendationService;

    @PostMapping("/recommend")
    @Operation(summary = "获取AI推荐", description = "获取AI生成的治理推荐")
    public ResponseEntity<List<AIRecommendationResponse>> getRecommendations(
            @Validated @RequestBody AIRecommendationRequest request) {
        log.info("获取AI推荐: type={}, domain={}", request.getRecommendationType(), request.getDomain());
        List<AIRecommendation> recommendations = recommendationService.getRecommendations(request);
        List<AIRecommendationResponse> responses = recommendations.stream()
                .map(this::toRecommendationResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/policy/suggest")
    @Operation(summary = "策略建议", description = "基于资产情况AI推荐适用的治理策略")
    public ResponseEntity<List<AIRecommendationResponse>> suggestPolicies(
            @RequestParam String assetType,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String assetId) {
        log.info("策略建议: assetType={}, domain={}", assetType, domain);
        List<AIRecommendation> suggestions = recommendationService.suggestPolicies(assetType, domain, assetId);
        List<AIRecommendationResponse> responses = suggestions.stream()
                .map(this::toRecommendationResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/quality/improve")
    @Operation(summary = "质量改进建议", description = "基于质量检测结果AI推荐改进方案")
    public ResponseEntity<List<AIRecommendationResponse>> suggestQualityImprovements(
            @RequestParam String assetId,
            @RequestParam String checkType,
            @RequestParam(required = false) Map<String, Object> currentMetrics) {
        log.info("质量改进建议: assetId={}, checkType={}", assetId, checkType);
        List<AIRecommendation> suggestions = recommendationService.suggestQualityImprovements(
                assetId, checkType, currentMetrics);
        List<AIRecommendationResponse> responses = suggestions.stream()
                .map(this::toRecommendationResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/mapping/suggest")
    @Operation(summary = "标准映射建议", description = "基于数据特征AI推荐标准映射方案")
    public ResponseEntity<List<AIRecommendationResponse>> suggestStandardMappings(
            @RequestParam String assetId,
            @RequestParam(required = false) String targetStandardType) {
        log.info("标准映射建议: assetId={}", assetId);
        List<AIRecommendation> suggestions = recommendationService.suggestStandardMappings(
                assetId, targetStandardType);
        List<AIRecommendationResponse> responses = suggestions.stream()
                .map(this::toRecommendationResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/lineage/analyze")
    @Operation(summary = "血缘影响分析建议", description = "基于血缘关系AI分析变更影响")
    public ResponseEntity<List<AIRecommendationResponse>> analyzeLineageImpact(
            @RequestParam String assetId,
            @RequestParam String changeType) {
        log.info("血缘影响分析建议: assetId={}, changeType={}", assetId, changeType);
        List<AIRecommendation> suggestions = recommendationService.analyzeLineageImpact(assetId, changeType);
        List<AIRecommendationResponse> responses = suggestions.stream()
                .map(this::toRecommendationResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/governance/summary")
    @Operation(summary = "治理态势摘要", description = "AI生成数据资产治理态势摘要")
    public ResponseEntity<Map<String, Object>> generateGovernanceSummary(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String period) {
        log.info("治理态势摘要: domain={}, period={}", domain, period);
        Map<String, Object> summary = recommendationService.generateGovernanceSummary(domain, period);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "接受推荐", description = "接受AI推荐并标记为已采纳")
    public ResponseEntity<AIRecommendationResponse> acceptRecommendation(
            @PathVariable Long id,
            @RequestParam String acceptedBy) {
        log.info("接受推荐: id={}, acceptedBy={}", id, acceptedBy);
        return recommendationService.acceptRecommendation(id, acceptedBy)
                .map(this::toRecommendationResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "拒绝推荐", description = "拒绝AI推荐")
    public ResponseEntity<Void> rejectRecommendation(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        log.info("拒绝推荐: id={}, reason={}", id, reason);
        recommendationService.rejectRecommendation(id, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/apply")
    @Operation(summary = "应用推荐", description = "将已采纳的推荐应用到实际治理流程")
    public ResponseEntity<Map<String, Object>> applyRecommendation(
            @PathVariable Long id,
            @RequestParam String appliedBy) {
        log.info("应用推荐: id={}, appliedBy={}", id, appliedBy);
        Map<String, Object> result = recommendationService.applyRecommendation(id, appliedBy);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    @Operation(summary = "获取推荐历史", description = "获取AI推荐的执行历史")
    public ResponseEntity<List<AIRecommendationResponse>> getRecommendationHistory(
            @RequestParam(required = false) String recommendationType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") Integer limit) {
        log.info("获取推荐历史: type={}, status={}", recommendationType, status);
        List<AIRecommendation> history = recommendationService.getRecommendationHistory(
                recommendationType, status, limit);
        List<AIRecommendationResponse> responses = history.stream()
                .map(this::toRecommendationResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private AIRecommendationResponse toRecommendationResponse(AIRecommendation recommendation) {
        return AIRecommendationResponse.builder()
                .id(recommendation.getId())
                .recommendationType(recommendation.getRecommendationType())
                .recommendationCode(recommendation.getRecommendationCode())
                .title(recommendation.getTitle())
                .description(recommendation.getDescription())
                .context(recommendation.getContext())
                .confidenceScore(recommendation.getConfidenceScore())
                .priority(recommendation.getPriority())
                .targetAssetId(recommendation.getTargetAssetId())
                .targetAssetType(recommendation.getTargetAssetType())
                .recommendedAction(recommendation.getRecommendedAction())
                .reasoning(recommendation.getReasoning())
                .considerationFactors(recommendation.getConsiderationFactors())
                .expectedOutcome(recommendation.getExpectedOutcome())
                .potentialRisks(recommendation.getPotentialRisks())
                .domain(recommendation.getDomain())
                .relatedPolicies(recommendation.getRelatedPolicies())
                .status(recommendation.getStatus())
                .accepted(recommendation.getAccepted())
                .applied(recommendation.getApplied())
                .appliedBy(recommendation.getAppliedBy())
                .appliedAt(recommendation.getAppliedAt())
                .expiresAt(recommendation.getExpiresAt())
                .createdAt(recommendation.getCreatedAt())
                .build();
    }
}
