package com.enterprise.dataplatform.standard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 合规检查请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceCheckRequest {

    /**
     * 检查批次号
     */
    @NotBlank(message = "检查批次号不能为空")
    @Size(max = 64, message = "批次号长度不能超过64")
    private String batchNo;

    /**
     * 数据标准ID
     */
    private Long standardId;

    /**
     * 数据资产ID
     */
    @NotBlank(message = "数据资产ID不能为空")
    @Size(max = 64, message = "数据资产ID长度不能超过64")
    private String assetId;

    /**
     * 字段名称
     */
    @Size(max = 128, message = "字段名称长度不能超过128")
    private String fieldName;

    /**
     * 检查类型
     */
    @NotBlank(message = "检查类型不能为空")
    @Size(max = 32, message = "检查类型长度不能超过32")
    private String checkType;

    /**
     * 检查方法
     */
    @Size(max = 64, message = "检查方法长度不能超过64")
    private String checkMethod;

    /**
     * 检查执行时间
     */
    private String checkTime;

    /**
     * 备注
     */
    private String remark;
}
