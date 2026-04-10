package com.enterprise.edams.lifecycle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据保留审计实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("lc_retention_audit")
public class RetentionAudit {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 审计类型：1-归档审计，2-清理审计，3-恢复审计
     */
    private Integer auditType;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 操作类型：1-创建，2-归档，3-清理，4-恢复，5-删除
     */
    private Integer operationType;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 操作前状态
     */
    private String beforeStatus;

    /**
     * 操作后状态
     */
    private String afterStatus;

    /**
     * 操作详情JSON
     */
    private String operationDetail;

    /**
     * 合规性检查结果：0-不合规，1-合规
     */
    private Integer complianceResult;

    /**
     * 合规性检查详情
     */
    private String complianceDetail;

    /**
     * 保留期限（天）
     */
    private Integer retentionDays;

    /**
     * 实际保留天数
     */
    private Integer actualRetentionDays;

    /**
     * 存储位置
     */
    private String storageLocation;

    /**
     * 审计备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
}
