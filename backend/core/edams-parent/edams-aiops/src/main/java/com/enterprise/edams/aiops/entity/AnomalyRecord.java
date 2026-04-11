package com.enterprise.edams.aiops.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 异常记录实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("anomaly_record")
public class AnomalyRecord extends BaseEntity {

    /**
     * 异常类型：spike, dip, trend_change, pattern, outlier
     */
    @TableField("anomaly_type")
    private String anomalyType;

    /**
     * 异常级别：high, medium, low
     */
    @TableField("severity")
    private String severity;

    /**
     * 关联指标ID
     */
    @TableField("metric_id")
    private Long metricId;

    /**
     * 指标名称
     */
    @TableField("metric_name")
    private String metricName;

    /**
     * 目标ID
     */
    @TableField("target_id")
    private String targetId;

    /**
     * 目标名称
     */
    @TableField("target_name")
    private String targetName;

    /**
     * 异常描述
     */
    @TableField("description")
    private String description;

    /**
     * 检测时间
     */
    @TableField("detect_time")
    private LocalDateTime detectTime;

    /**
     * 异常开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 异常结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 持续时间（分钟）
     */
    @TableField("duration_minutes")
    private Integer durationMinutes;

    /**
     * 异常值
     */
    @TableField("anomaly_value")
    private BigDecimal anomalyValue;

    /**
     * 期望值
     */
    @TableField("expected_value")
    private BigDecimal expectedValue;

    /**
     * 偏差百分比
     */
    @TableField("deviation_percent")
    private BigDecimal deviationPercent;

    /**
     * 检测算法
     */
    @TableField("algorithm")
    private String algorithm;

    /**
     * 置信度
     */
    @TableField("confidence")
    private BigDecimal confidence;

    /**
     * 处理状态：detected, investigating, resolved, ignored
     */
    @TableField("status")
    private String status;

    /**
     * 关联告警ID
     */
    @TableField("alert_id")
    private Long alertId;

    /**
     * 分析结果
     */
    @TableField("analysis_result")
    private String analysisResult;

    /**
     * 建议措施
     */
    @TableField("suggestion")
    private String suggestion;

    /**
     * 特征数据(JSON)
     */
    @TableField("features")
    private String features;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 判断是否仍在持续
     */
    public boolean isOngoing() {
        return endTime == null && "detected".equalsIgnoreCase(status);
    }
}
