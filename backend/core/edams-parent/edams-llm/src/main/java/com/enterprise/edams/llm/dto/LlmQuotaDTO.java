package com.enterprise.edams.llm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配额DTO
 */
@Data
public class LlmQuotaDTO {
    private Long id;
    private Long tenantId;
    private Long userId;
    private String quotaType;
    private Long modelId;
    private String modelCode;
    private Long quotaLimit;
    private Long quotaUsed;
    private Double usagePercent;
    private BigDecimal costLimit;
    private BigDecimal costUsed;
    private Double costPercent;
    private Integer requestLimit;
    private Integer requestUsed;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private Boolean enabled;
    private Integer alertThreshold;
    private String status;
    private String remark;
}
