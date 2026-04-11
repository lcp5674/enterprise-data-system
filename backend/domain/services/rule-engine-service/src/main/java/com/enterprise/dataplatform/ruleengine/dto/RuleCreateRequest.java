package com.enterprise.dataplatform.ruleengine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 规则创建/更新请求DTO
 */
@Data
public class RuleCreateRequest {

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "规则编码不能为空")
    private String ruleCode;

    @NotBlank(message = "规则分类不能为空")
    private String category;

    private String description;

    private String ruleContent;

    private String ruleFilePath;

    private String status = "DRAFT";

    private Integer priority = 0;

    private String triggerCondition;

    private String parameters;
}
