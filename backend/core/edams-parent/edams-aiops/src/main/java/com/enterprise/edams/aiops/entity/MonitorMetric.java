package com.enterprise.edams.aiops.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 监控指标实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("monitor_metric")
public class MonitorMetric extends BaseEntity {

    /**
     * 指标名称
     */
    @TableField("metric_name")
    private String metricName;

    /**
     * 指标类型：cpu, memory, disk, network, custom
     */
    @TableField("metric_type")
    private String metricType;

    /**
     * 服务/系统标识
     */
    @TableField("target_id")
    private String targetId;

    /**
     * 目标名称
     */
    @TableField("target_name")
    private String targetName;

    /**
     * 指标值
     */
    @TableField("metric_value")
    private BigDecimal metricValue;

    /**
     * 指标单位
     */
    @TableField("metric_unit")
    private String metricUnit;

    /**
     * 采集时间
     */
    @TableField("collect_time")
    private LocalDateTime collectTime;

    /**
     * 采集间隔（秒）
     */
    @TableField("interval_seconds")
    private Integer intervalSeconds;

    /**
     * 最小值
     */
    @TableField("min_value")
    private BigDecimal minValue;

    /**
     * 最大值
     */
    @TableField("max_value")
    private BigDecimal maxValue;

    /**
     * 平均值
     */
    @TableField("avg_value")
    private BigDecimal avgValue;

    /**
     * 标签(JSON)
     */
    @TableField("tags")
    private String tags;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;
}
