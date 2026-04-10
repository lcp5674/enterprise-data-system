package com.enterprise.dataplatform.standard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标准映射请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardMappingRequest {

    /**
     * 数据标准ID
     */
    @NotNull(message = "数据标准ID不能为空")
    private Long standardId;

    /**
     * 数据资产ID
     */
    @NotBlank(message = "数据资产ID不能为空")
    @Size(max = 64, message = "数据资产ID长度不能超过64")
    private String assetId;

    /**
     * 数据资产名称
     */
    @Size(max = 256, message = "数据资产名称长度不能超过256")
    private String assetName;

    /**
     * 资产类型
     */
    @NotBlank(message = "资产类型不能为空")
    @Size(max = 32, message = "资产类型长度不能超过32")
    private String assetType;

    /**
     * 字段名称
     */
    @Size(max = 128, message = "字段名称长度不能超过128")
    private String fieldName;

    /**
     * 字段中文名
     */
    @Size(max = 128, message = "字段中文名长度不能超过128")
    private String fieldChineseName;

    /**
     * 数据类型
     */
    @Size(max = 64, message = "数据类型长度不能超过64")
    private String fieldDataType;

    /**
     * 映射类型
     */
    @Size(max = 32, message = "映射类型长度不能超过32")
    private String mappingType;

    /**
     * 转换规则
     */
    private String transformRule;

    /**
     * 映射说明
     */
    private String mappingDescription;

    /**
     * 映射来源
     */
    @Size(max = 32, message = "映射来源长度不能超过32")
    private String mappingSource;

    /**
     * 是否关键字段
     */
    private Boolean isKeyField;

    /**
     * 敏感级别
     */
    @Size(max = 16, message = "敏感级别长度不能超过16")
    private String sensitivityLevel;

    /**
     * 有效期开始
     */
    private String validFrom;

    /**
     * 有效期结束
     */
    private String validTo;
}
