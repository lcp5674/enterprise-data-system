package com.enterprise.edams.insight.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 趋势预测记录实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("trend_prediction")
public class TrendPrediction extends BaseEntity {

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 资产名称
     */
    private String assetName;

    /**
     * 预测类型：GROWTH/DECLINE/CYCLE/SEASONAL
     */
    private String predictionType;

    /**
     * 预测值
     */
    private String predictedValue;

    /**
     * 置信度
     */
    private Double confidence;

    /**
     * 预测开始时间
     */
    private LocalDateTime startTime;

    /**
     * 预测结束时间
     */
    private LocalDateTime endTime;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型版本
     */
    private String modelVersion;

    /**
     * 预测结果详情(JSON)
     */
    private String details;
}
