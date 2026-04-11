package com.enterprise.edams.llm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 使用日志DTO
 */
@Data
public class LlmUsageLogDTO {
    private Long id;
    private String requestId;
    private String traceId;
    private Long tenantId;
    private Long userId;
    private String userName;
    private Long modelId;
    private String modelCode;
    private String provider;
    private String requestType;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;
    private BigDecimal inputCost;
    private BigDecimal outputCost;
    private BigDecimal totalCost;
    private Long latencyMs;
    private String status;
    private String errorCode;
    private String errorMessage;
    private String module;
    private String appName;
    private String ipAddress;
    private LocalDateTime requestTime;
}
