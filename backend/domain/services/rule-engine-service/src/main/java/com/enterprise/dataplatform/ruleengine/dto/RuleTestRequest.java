package com.enterprise.dataplatform.ruleengine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 规则测试请求DTO
 */
@Data
public class RuleTestRequest {

    @NotBlank(message = "规则分类不能为空")
    private String category;

    /** 测试输入数据 */
    private Map<String, Object> input;
}
