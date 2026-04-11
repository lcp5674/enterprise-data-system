package com.enterprise.edams.analytics.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 指标定义实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("edams_metric_definition")
public class MetricDefinition extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField("metric_name")
    private String metricName;

    @TableField("metric_code")
    private String metricCode;

    @TableField("metric_type")
    private String metricType;

    @TableField("formula")
    private String formula;

    @TableField("data_type")
    private String dataType;

    @TableField("unit")
    private String unit;

    @TableField("description")
    private String description;

    @TableField("business_definition")
    private String businessDefinition;

    @TableField("technical_definition")
    private String technicalDefinition;

    @TableField("domain")
    private String domain;

    @TableField("subject")
    private String subject;

    @TableField("dimensions")
    private String dimensions;

    @TableField("source_table")
    private String sourceTable;

    @TableField("status")
    private Integer status;

    @TableField("current_value")
    private BigDecimal currentValue;

    @TableField("target_value")
    private BigDecimal targetValue;

    @TableField("warning_threshold")
    private BigDecimal warningThreshold;

    @TableField("alert_threshold")
    private BigDecimal alertThreshold;

    @TableField("version")
    private Integer version;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("creator_name")
    private String creatorName;
}
