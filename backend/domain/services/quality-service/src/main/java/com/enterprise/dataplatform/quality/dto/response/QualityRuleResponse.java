package com.enterprise.dataplatform.quality.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 质量规则响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityRuleResponse {

    private Long id;
    private String ruleCode;
    private String ruleName;
    private String description;
    private String ruleType;
    private String ruleCategory;
    private String ruleExpression;
    private String assetId;
    private String assetType;
    private String fieldName;
    private String qualityDimension;
    private String severityLevel;
    private String thresholdExpression;
    private String expectedValue;
    private Double alertThreshold;
    private Double errorThreshold;
    private Boolean enabled;
    private String status;
    private String priority;
    private Integer version;
    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
}
