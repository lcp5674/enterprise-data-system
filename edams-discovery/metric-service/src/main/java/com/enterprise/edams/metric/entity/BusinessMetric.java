package com.enterprise.edams.metric.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 业务指标实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("business_metric")
public class BusinessMetric extends BaseEntity {

    /**
     * 指标编码
     */
    private String code;

    /**
     * 指标名称
     */
    private String name;

    /**
     * 指标类型：BASIC/COMPOSITE/DERIVED
     */
    private String metricType;

    /**
     * 业务领域
     */
    private String domain;

    /**
     * 业务主题
     */
    private String subject;

    /**
     * 描述
     */
    private String description;

    /**
     * 计算公式
     */
    private String formula;

    /**
     * 数据源ID
     */
    private Long datasourceId;

    /**
     * 数据表
     */
    private String tableName;

    /**
     * 维度(JSON)
     */
    private String dimensions;

    /**
     * 指标状态：DRAFT/PUBLISHED/DEPRECATED
     */
    private String status;

    /**
     * 标签(JSON)
     */
    private String tags;

    /**
     * 版本号
     */
    private String version;

    /**
     * 创建方式：MANUAL/AUTO
     */
    private String createMode;
}
