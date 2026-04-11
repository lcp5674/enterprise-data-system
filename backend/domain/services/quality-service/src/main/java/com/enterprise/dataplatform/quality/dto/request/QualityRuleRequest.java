package com.enterprise.dataplatform.quality.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 质量规则请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityRuleRequest {

    @NotBlank(message = "规则编码不能为空")
    @Size(max = 64, message = "规则编码长度不能超过64")
    private String ruleCode;

    @NotBlank(message = "规则名称不能为空")
    @Size(max = 128, message = "规则名称长度不能超过128")
    private String ruleName;

    private String description;

    @NotBlank(message = "规则类型不能为空")
    @Size(max = 32, message = "规则类型长度不能超过32")
    private String ruleType;

    @NotBlank(message = "规则分类不能为空")
    @Size(max = 32, message = "规则分类长度不能超过32")
    private String ruleCategory;

    private String ruleExpression;

    @Size(max = 64, message = "资产ID长度不能超过64")
    private String assetId;

    @Size(max = 32, message = "资产类型长度不能超过32")
    private String assetType;

    @Size(max = 128, message = "字段名称长度不能超过128")
    private String fieldName;

    @Size(max = 32, message = "质量维度长度不能超过32")
    private String qualityDimension;

    @NotBlank(message = "严重级别不能为空")
    @Size(max = 16, message = "严重级别长度不能超过16")
    private String severityLevel;

    private String thresholdExpression;

    @Size(max = 256, message = "期望值长度不能超过256")
    private String expectedValue;

    private Double alertThreshold;

    private Double errorThreshold;

    @Size(max = 16, message = "优先级长度不能超过16")
    private String priority;
}
