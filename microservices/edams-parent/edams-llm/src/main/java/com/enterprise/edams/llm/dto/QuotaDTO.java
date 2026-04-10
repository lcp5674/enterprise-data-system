package com.enterprise.edams.llm.dto;

import lombok.Data;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配额DTO
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Data
@Builder
public class QuotaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配额ID
     */
    private String quotaId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 提供商ID
     */
    private String providerId;

    /**
     * 提供商名称
     */
    private String providerName;

    /**
     * 配额类型
     */
    private String quotaType;

    /**
     * Token配额上限
     */
    private Long tokenLimit;

    /**
     * 已使用Token数
     */
    private Long tokenUsed;

    /**
     * Token剩余
     */
    private Long tokenRemaining;

    /**
     * Token使用率
     */
    private Double tokenUsageRate;

    /**
     * 请求次数配额上限
     */
    private Long requestLimit;

    /**
     * 已使用请求次数
     */
    private Long requestUsed;

    /**
     * 请求剩余
     */
    private Long requestRemaining;

    /**
     * 请求使用率
     */
    private Double requestUsageRate;

    /**
     * 配额重置时间
     */
    private LocalDateTime resetTime;

    /**
     * 状态
     */
    private String status;

    /**
     * 费用配额
     */
    private BigDecimal costLimit;

    /**
     * 已消费费用
     */
    private BigDecimal costUsed;

    /**
     * 费用剩余
     */
    private BigDecimal costRemaining;

    /**
     * 费用使用率
     */
    private Double costUsageRate;

    /**
     * 今日使用统计
     */
    private TodayUsage todayUsage;

    /**
     * 今日使用统计
     */
    @Data
    @Builder
    public static class TodayUsage implements Serializable {
        private Long inputTokens;
        private Long outputTokens;
        private Long totalTokens;
        private Long requestCount;
        private BigDecimal totalCost;
        private LocalDateTime lastUsedTime;
    }
}
