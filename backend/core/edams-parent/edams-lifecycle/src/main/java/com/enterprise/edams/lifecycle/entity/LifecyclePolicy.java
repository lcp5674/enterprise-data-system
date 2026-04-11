package com.enterprise.edams.lifecycle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 生命周期策略实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("lifecycle_policy")
public class LifecyclePolicy extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 策略名称 */
    private String policyName;

    /** 策略编码 */
    private String policyCode;

    /** 策略描述 */
    private String policyDescription;

    /** 数据资产类型 */
    private String dataAssetType;

    /** 归档策略：0-不归档，1-自动归档，2-手动归档 */
    private Integer archiveStrategy;

    /** 归档天数阈值 */
    private Integer archiveThresholdDays;

    /** 销毁策略：0-不销毁，1-自动销毁，2-手动销毁 */
    private Integer destroyStrategy;

    /** 销毁天数阈值 */
    private Integer destroyThresholdDays;

    /** 清理策略：0-不清理，1-自动清理，2-手动清理 */
    private Integer cleanupStrategy;

    /** 清理天数阈值 */
    private Integer cleanupThresholdDays;

    /** 是否启用：0-禁用，1-启用 */
    private Integer enabled;

    /** 租户ID */
    private Long tenantId;

    /** 备注 */
    private String remark;
}