package com.enterprise.edams.llm.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 成本统计DTO
 */
@Data
public class LlmCostStatistics {
    private Long tenantId;
    private Long userId;
    private Long modelId;
    private String modelCode;
    private Long totalTokens;
    private Long inputTokens;
    private Long outputTokens;
    private BigDecimal totalCost;
    private BigDecimal inputCost;
    private BigDecimal outputCost;
    private Long requestCount;
    private BigDecimal avgCostPerRequest;
    private BigDecimal avgCostPerToken;
}
