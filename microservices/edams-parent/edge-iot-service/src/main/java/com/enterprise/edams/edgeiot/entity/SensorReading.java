package com.enterprise.edams.edgeiot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

/**
 * 传感器读数实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sensor_reading")
public class SensorReading extends BaseEntity {
    
    /**
     * 设备ID
     */
    @TableField("device_id")
    private String deviceId;
    
    /**
     * 传感器ID
     */
    @TableField("sensor_id")
    private String sensorId;
    
    /**
     * 传感器类型
     */
    @TableField("sensor_type")
    private SensorType sensorType;
    
    /**
     * 传感器名称
     */
    @TableField("sensor_name")
    private String sensorName;
    
    /**
     * 读数值
     */
    @TableField("value")
    private Double value;
    
    /**
     * 单位
     */
    @TableField("unit")
    private String unit;
    
    /**
     * 数据质量
     */
    @TableField("quality")
    private DataQuality quality;
    
    /**
     * 采集时间
     */
    @TableField("reading_time")
    private LocalDateTime readingTime;
    
    /**
     * 关联数据资产ID
     */
    @TableField("asset_id")
    private Long assetId;
    
    /**
     * 标签(JSON)
     */
    @TableField("tags")
    private String tags;
}
