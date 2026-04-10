package com.enterprise.dataplatform.standard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 合规检查响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceCheckResponse {

    /**
     * 检查ID
     */
    private Long id;

    /**
     * 检查批次号
     */
    private String batchNo;

    /**
     * 数据标准ID
     */
    private Long standardId;

    /**
     * 数据标准编码
     */
    private String standardCode;

    /**
     * 数据标准名称
     */
    private String standardName;

    /**
     * 数据资产ID
     */
    private String assetId;

    /**
     * 数据资产名称
     */
    private String assetName;

    /**
     * 资产类型
     */
    private String assetType;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 检查类型
     */
    private String checkType;

    /**
     * 检查结果
     */
    private String checkResult;

    /**
     * 合规率
     */
    private Double complianceRate;

    /**
     * 违规数量
     */
    private Integer violationCount;

    /**
     * 总记录数
     */
    private Long totalRecords;

    /**
     * 检查的记录数
     */
    private Long checkedRecords;

    /**
     * 违规样本
     */
    private String violationSamples;

    /**
     * 违规详情
     */
    private String violationDetails;

    /**
     * 检查方法
     */
    private String checkMethod;

    /**
     * 检查时间
     */
    private LocalDateTime checkTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTimeMs;

    /**
     * 检查状态
     */
    private String checkStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行人
     */
    private String executor;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String remark;
}
