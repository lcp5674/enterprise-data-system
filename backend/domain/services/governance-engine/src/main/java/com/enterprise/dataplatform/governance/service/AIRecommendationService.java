package com.enterprise.dataplatform.governance.service;

import com.enterprise.dataplatform.governance.domain.entity.AIRecommendation;
import com.enterprise.dataplatform.governance.dto.request.AIRecommendationRequest;
import com.enterprise.dataplatform.governance.dto.response.AIRecommendationResponse;
import com.enterprise.dataplatform.governance.repository.AIRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI智能推荐服务
 * 调用AI模型生成治理建议
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final AIRecommendationRepository recommendationRepository;

    @Value("${ai.model.endpoint:http://localhost:8086}")
    private String aiEndpoint;

    @Value("${ai.model.name:hunyuan-instruct}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 生成AI推荐
     */
    @Transactional
    public AIRecommendationResponse generateRecommendation(AIRecommendationRequest request) {
        log.info("生成AI推荐: 类型={}, 实体ID={}", 
                request.getRecommendationType(), request.getEntityId());

        String recommendationCode = "REC-" + UUID.randomUUID().toString().substring(0, 8);

        // 调用AI模型生成推荐
        AIRecommendation recommendation = callAIModel(request);

        recommendation.setRecommendationCode(recommendationCode);
        recommendation.setStatus("PENDING");
        recommendation.setRecommendationTime(LocalDateTime.now());
        recommendation.setModel(modelName);

        recommendation = recommendationRepository.save(recommendation);

        log.info("AI推荐生成成功: {}", recommendation.getId());
        return toResponse(recommendation);
    }

    /**
     * 调用AI模型
     */
    private AIRecommendation callAIModel(AIRecommendationRequest request) {
        // 构建提示词
        String prompt = buildPrompt(request);

        try {
            // 调用AI服务
            Map<String, Object> aiRequest = new HashMap<>();
            aiRequest.put("model", modelName);
            aiRequest.put("prompt", prompt);
            aiRequest.put("max_tokens", 500);
            aiRequest.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(aiRequest, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    aiEndpoint + "/v1/generate",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> aiResponse = response.getBody();
                String content = (String) aiResponse.getOrDefault("content", "");

                return buildRecommendationFromAI(request, content);
            }
        } catch (Exception e) {
            log.warn("AI模型调用失败，使用默认推荐: {}", e.getMessage());
        }

        // 默认推荐
        return buildDefaultRecommendation(request);
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(AIRecommendationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("作为数据治理专家，请根据以下信息提供治理建议：\n\n");

        switch (request.getRecommendationType()) {
            case "QUALITY_IMPROVEMENT":
                prompt.append("类型：数据质量管理改进建议\n");
                prompt.append("资产ID：").append(request.getEntityId()).append("\n");
                prompt.append("问题描述：").append(request.getContext()).append("\n");
                break;
            case "STANDARD_MAPPING":
                prompt.append("类型：数据标准映射建议\n");
                prompt.append("资产ID：").append(request.getEntityId()).append("\n");
                prompt.append("上下文：").append(request.getContext()).append("\n");
                break;
            case "RISK_ALERT":
                prompt.append("类型：风险预警建议\n");
                prompt.append("资产ID：").append(request.getEntityId()).append("\n");
                prompt.append("风险信息：").append(request.getContext()).append("\n");
                break;
            default:
                prompt.append("类型：一般治理建议\n");
                prompt.append("实体ID：").append(request.getEntityId()).append("\n");
                prompt.append("上下文：").append(request.getContext()).append("\n");
        }

        prompt.append("\n请提供具体的改进建议和实施步骤。");
        return prompt.toString();
    }

    /**
     * 根据AI响应构建推荐
     */
    private AIRecommendation buildRecommendationFromAI(AIRecommendationRequest request, String content) {
        return AIRecommendation.builder()
                .recommendationType(request.getRecommendationType())
                .title(request.getRecommendationType() + " - 建议")
                .content(content)
                .details(toJson(Map.of("generated", true, "context", request.getContext())))
                .reasoning("基于AI模型分析生成")
                .confidence(85.0)
                .assetId(request.getEntityId())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .build();
    }

    /**
     * 构建默认推荐
     */
    private AIRecommendation buildDefaultRecommendation(AIRecommendationRequest request) {
        String title;
        String content;

        switch (request.getRecommendationType()) {
            case "QUALITY_IMPROVEMENT":
                title = "数据质量改进建议";
                content = "建议对数据进行以下质量检查：\n" +
                         "1. 完整性检查 - 确保所有必填字段有值\n" +
                         "2. 准确性检查 - 验证数据值的正确性\n" +
                         "3. 一致性检查 - 确保数据格式统一";
                break;
            case "STANDARD_MAPPING":
                title = "数据标准映射建议";
                content = "建议建立以下标准映射：\n" +
                         "1. 字段命名规范映射\n" +
                         "2. 数据类型标准映射\n" +
                         "3. 业务术语标准化";
                break;
            case "RISK_ALERT":
                title = "风险预警建议";
                content = "建议采取以下风险缓解措施：\n" +
                         "1. 增强数据访问控制\n" +
                         "2. 实施数据加密\n" +
                         "3. 定期进行安全审计";
                break;
            default:
                title = "数据治理建议";
                content = "建议实施全面的数据治理措施，包括：\n" +
                         "1. 建立数据标准\n" +
                         "2. 实施质量监控\n" +
                         "3. 定期评估数据资产";
        }

        return AIRecommendation.builder()
                .recommendationType(request.getRecommendationType())
                .title(title)
                .content(content)
                .details(toJson(Map.of("generated", false, "type", "default")))
                .reasoning("基于规则引擎生成")
                .confidence(70.0)
                .assetId(request.getEntityId())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .build();
    }

    /**
     * 查询推荐
     */
    public AIRecommendationResponse getRecommendation(Long id) {
        AIRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("推荐不存在: " + id));
        return toResponse(recommendation);
    }

    /**
     * 分页查询推荐
     */
    public Page<AIRecommendationResponse> searchRecommendations(
            String recommendationType, String status,
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return recommendationRepository.searchRecommendations(
                recommendationType, status, startTime, endTime, pageable)
                .map(this::toResponse);
    }

    /**
     * 处理推荐
     */
    @Transactional
    public AIRecommendationResponse handleRecommendation(
            Long id, String status, String handler, String comment) {
        log.info("处理AI推荐: ID={}, 状态={}, 处理人={}", id, status, handler);

        AIRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("推荐不存在: " + id));

        recommendation.setStatus(status);
        recommendation.setHandler(handler);
        recommendation.setHandleTime(LocalDateTime.now());
        recommendation.setHandleComment(comment);

        recommendation = recommendationRepository.save(recommendation);

        log.info("AI推荐处理成功: {}", id);
        return toResponse(recommendation);
    }

    /**
     * 获取待处理推荐
     */
    public Page<AIRecommendationResponse> getPendingRecommendations(Pageable pageable) {
        return recommendationRepository.findPendingRecommendations(pageable)
                .map(this::toResponse);
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private AIRecommendationResponse toResponse(AIRecommendation recommendation) {
        return AIRecommendationResponse.builder()
                .id(recommendation.getId())
                .recommendationCode(recommendation.getRecommendationCode())
                .recommendationType(recommendation.getRecommendationType())
                .title(recommendation.getTitle())
                .content(recommendation.getContent())
                .confidence(recommendation.getConfidence())
                .assetId(recommendation.getAssetId())
                .assetName(recommendation.getAssetName())
                .entityType(recommendation.getEntityType())
                .entityId(recommendation.getEntityId())
                .status(recommendation.getStatus())
                .handler(recommendation.getHandler())
                .handleTime(recommendation.getHandleTime())
                .handleComment(recommendation.getHandleComment())
                .recommendationTime(recommendation.getRecommendationTime())
                .model(recommendation.getModel())
                .createTime(recommendation.getCreateTime())
                .build();
    }
}
