package com.enterprise.edams.insight.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 异常检测记录实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("anomaly_detection")
public class AnomalyDetection extends BaseEntity {

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 资产名称
     */
    private String assetName;

    /**
     * 资产类型
     */
    private String assetType;

    /**
     * 检测类型：STATISTICAL/MACHINE_LEARNING/PATTERN
     */
    private String detectionType;

    /**
     * 异常类型
     */
    private String anomalyType;

    /**
     * 异常等级：LOW/MEDIUM/HIGH/CRITICAL
     */
    private String severity;

    /**
     * 异常描述
     */
    private String description;

    /**
     * 异常值
     */
    private String anomalyValue;

    /**
     * 期望值
     */
    private String expectedValue;

    /**
     * 检测时间
     */
    private LocalDateTime detectionTime;

    /**
     * 状态：DETECTED/INVESTIGATING/RESOLVED/DISMISSED
     */
    private String status;

    /**
     * 处理备注
     */
    private String remark;
}
