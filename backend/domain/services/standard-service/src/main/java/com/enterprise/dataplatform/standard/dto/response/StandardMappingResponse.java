package com.enterprise.dataplatform.standard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标准映射响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardMappingResponse {

    /**
     * 映射ID
     */
    private Long id;

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
     * 字段中文名
     */
    private String fieldChineseName;

    /**
     * 数据类型
     */
    private String fieldDataType;

    /**
     * 映射状态
     */
    private String mappingStatus;

    /**
     * 映射类型
     */
    private String mappingType;

    /**
     * 转换规则
     */
    private String transformRule;

    /**
     * 覆盖率
     */
    private Integer coverageRate;

    /**
     * 质量得分
     */
    private Double qualityScore;

    /**
     * 映射说明
     */
    private String mappingDescription;

    /**
     * 映射来源
     */
    private String mappingSource;

    /**
     * 是否关键字段
     */
    private Boolean isKeyField;

    /**
     * 敏感级别
     */
    private String sensitivityLevel;

    /**
     * 版本
     */
    private Integer version;

    /**
     * 有效期开始
     */
    private LocalDateTime validFrom;

    /**
     * 有效期结束
     */
    private LocalDateTime validTo;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updater;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 审批人
     */
    private String approver;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 审批意见
     */
    private String approvalComment;
}
