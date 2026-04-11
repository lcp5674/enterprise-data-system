package com.enterprise.edams.lifecycle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 数据生命周期实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_lifecycle")
public class DataLifecycle extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 数据资产ID */
    private Long dataAssetId;

    /** 数据资产名称 */
    private String dataAssetName;

    /** 数据资产类型 */
    private String dataAssetType;

    /** 当前生命周期阶段 */
    private String currentStage;

    /** 上一个生命周期阶段 */
    private String previousStage;

    /** 阶段变更时间 */
    private LocalDateTime stageChangedAt;

    /** 阶段变更原因 */
    private String changeReason;

    /** 预计归档时间 */
    private LocalDateTime expectedArchiveTime;

    /** 预计销毁时间 */
    private LocalDateTime expectedDestroyTime;

    /** 实际归档时间 */
    private LocalDateTime actualArchiveTime;

    /** 实际销毁时间 */
    private LocalDateTime actualDestroyTime;

    /** 生命周期策略ID */
    private Long policyId;

    /** 租户ID */
    private Long tenantId;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 备注 */
    private String remark;
}