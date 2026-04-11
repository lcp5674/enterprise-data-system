package com.enterprise.edams.llm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LLM调用响应DTO
 */
@Data
public class LlmInvokeResponse {
    private String requestId;
    private String traceId;
    private String content;
    private String finishReason;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;
    private BigDecimal cost;
    private Long latencyMs;
    private String status;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime responseTime;
}
