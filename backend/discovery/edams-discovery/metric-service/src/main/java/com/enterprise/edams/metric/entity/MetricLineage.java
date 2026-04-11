package com.enterprise.edams.metric.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 指标血缘关系实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("metric_lineage")
public class MetricLineage extends BaseEntity {

    /**
     * 源指标ID
     */
    private Long sourceMetricId;

    /**
     * 源指标编码
     */
    private String sourceMetricCode;

    /**
     * 目标指标ID
     */
    private Long targetMetricId;

    /**
     * 目标指标编码
     */
    private String targetMetricCode;

    /**
     * 血缘类型：DERIVE/CALCULATE/AGGREGATE
     */
    private String lineageType;

    /**
     * 转换规则
     */
    private String transformRule;

    /**
     * 描述
     */
    private String description;
}
