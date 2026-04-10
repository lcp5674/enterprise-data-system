package com.enterprise.edams.sla.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SLA指标记录实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sla_metric")
public class SlaMetric extends BaseEntity {
    
    /**
     * SLA定义ID
     */
    @TableField("sla_definition_id")
    private Long slaDefinitionId;
    
    /**
     * 资产ID
     */
    @TableField("asset_id")
    private Long assetId;
    
    /**
     * 指标类型
     */
    @TableField("metric_type")
    private SlaType metricType;
    
    /**
     * 指标值
     */
    @TableField("metric_value")
    private BigDecimal metricValue;
    
    /**
     * 单位
     */
    @TableField("unit")
    private String unit;
    
    /**
     * 采集时间
     */
    @TableField("timestamp")
    private LocalDateTime timestamp;
    
    /**
     * 是否达标
     */
    @TableField("meets_target")
    private Boolean meetsTarget;
    
    /**
     * 偏差
     */
    @TableField("deviation")
    private BigDecimal deviation;
    
    /**
     * 数据来源
     */
    @TableField("data_source")
    private String dataSource;
}
