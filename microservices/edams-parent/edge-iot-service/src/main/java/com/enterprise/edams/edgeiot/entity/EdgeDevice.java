package com.enterprise.edams.edgeiot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

/**
 * 边缘设备实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("edge_device")
public class EdgeDevice extends BaseEntity {
    
    /**
     * 设备ID
     */
    @TableField("device_id")
    private String deviceId;
    
    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;
    
    /**
     * 设备类型
     */
    @TableField("device_type")
    private DeviceType deviceType;
    
    /**
     * 设备状态
     */
    @TableField("status")
    private DeviceStatus status;
    
    /**
     * 网关ID
     */
    @TableField("gateway_id")
    private String gatewayId;
    
    /**
     * 设备位置
     */
    @TableField("location")
    private String location;
    
    /**
     * 纬度
     */
    @TableField("latitude")
    private Double latitude;
    
    /**
     * 经度
     */
    @TableField("longitude")
    private Double longitude;
    
    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;
    
    /**
     * MAC地址
     */
    @TableField("mac_address")
    private String macAddress;
    
    /**
     * 固件版本
     */
    @TableField("firmware_version")
    private String firmwareVersion;
    
    /**
     * 最后心跳时间
     */
    @TableField("last_heartbeat")
    private LocalDateTime lastHeartbeat;
    
    /**
     * 最后同步时间
     */
    @TableField("last_sync_time")
    private LocalDateTime lastSyncTime;
    
    /**
     * 传感器数量
     */
    @TableField("sensor_count")
    private Integer sensorCount;
    
    /**
     * CPU使用率
     */
    @TableField("cpu_usage")
    private Double cpuUsage;
    
    /**
     * 内存使用率
     */
    @TableField("memory_usage")
    private Double memoryUsage;
    
    /**
     * 磁盘使用率
     */
    @TableField("disk_usage")
    private Double diskUsage;
    
    /**
     * 扩展属性(JSON)
     */
    @TableField("properties")
    private String properties;
    
    /**
     * 设备描述
     */
    @TableField("description")
    private String description;
}
