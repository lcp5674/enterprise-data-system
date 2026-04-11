package com.enterprise.edams.lifecycle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 生命周期阶段配置实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("lifecycle_stage")
public class LifecycleStage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 阶段名称 */
    private String stageName;

    /** 阶段编码：CREATED, DEVELOPMENT, MATURITY, DECAY, ARCHIVED, DESTROYED */
    private String stageCode;

    /** 阶段描述 */
    private String stageDescription;

    /** 是否启用：0-禁用，1-启用 */
    private Integer enabled;

    /** 下一阶段编码 */
    private String nextStageCode;

    /** 上一阶段编码 */
    private String previousStageCode;

    /** 阶段持续时间（天） */
    private Integer durationDays;

    /** 预警阈值（百分比） */
    private Integer warningThreshold;

    /** 租户ID */
    private Long tenantId;

    /** 排序序号 */
    private Integer sortOrder;
}