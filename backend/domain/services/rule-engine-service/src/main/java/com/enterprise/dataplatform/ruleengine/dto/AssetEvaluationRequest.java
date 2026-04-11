package com.enterprise.dataplatform.ruleengine.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 资产评估请求DTO
 */
@Data
public class AssetEvaluationRequest {

    @NotBlank(message = "资产ID不能为空")
    private String assetId;

    private String assetName;

    private String assetType;

    @Min(value = 0, message = "质量问题数不能为负数")
    private Integer qualityIssues = 0;

    @DecimalMin(value = "0.0", message = "完整度不能小于0")
    @DecimalMax(value = "1.0", message = "完整度不能大于1")
    private Double completeness = 0.0;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private Double accuracy = 0.0;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private Double consistency = 0.0;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private Double timeliness = 0.0;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private Double uniqueness = 0.0;

    @Min(value = 0)
    private Double dailyAccessCount = 0.0;

    private String lastAccessTime;

    @Min(value = 0)
    private Double dataSizeMb = 0.0;

    private String standardCode;

    private String owner;

    private String businessDomain;

    private String sensitivityLevel;

    private String createTime;
}
