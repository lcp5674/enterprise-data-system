package com.enterprise.dataplatform.standard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据标准请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataStandardRequest {

    /**
     * 标准编码
     */
    @NotBlank(message = "标准编码不能为空")
    @Size(max = 64, message = "标准编码长度不能超过64")
    private String standardCode;

    /**
     * 标准名称
     */
    @NotBlank(message = "标准名称不能为空")
    @Size(max = 128, message = "标准名称长度不能超过128")
    private String standardName;

    /**
     * 标准描述
     */
    private String description;

    /**
     * 标准类别
     */
    @NotBlank(message = "标准类别不能为空")
    @Size(max = 32, message = "标准类别长度不能超过32")
    private String category;

    /**
     * 标准类型
     */
    @NotBlank(message = "标准类型不能为空")
    @Size(max = 32, message = "标准类型长度不能超过32")
    private String standardType;

    /**
     * 标准规则内容（JSON格式）
     */
    private String ruleContent;

    /**
     * 数据类型
     */
    @Size(max = 32, message = "数据类型长度不能超过32")
    private String dataType;

    /**
     * 取值范围（JSON格式）
     */
    private String valueRange;

    /**
     * 精度要求
     */
    private Integer precisionRequired;

    /**
     * 最大长度
     */
    private Integer maxLength;

    /**
     * 是否必填
     */
    private Boolean required = false;

    /**
     * 默认值
     */
    @Size(max = 256, message = "默认值长度不能超过256")
    private String defaultValue;

    /**
     * 优先级
     */
    @Size(max = 16, message = "优先级长度不能超过16")
    private String priority;

    /**
     * 标准来源
     */
    @Size(max = 32, message = "标准来源长度不能超过32")
    private String source;

    /**
     * 外部参考编号
     */
    @Size(max = 128, message = "外部参考编号长度不能超过128")
    private String externalRef;

    /**
     * 适用范围（JSON格式）
     */
    private String applicableScope;

    /**
     * 违规处理建议
     */
    private String violationHandling;
}
