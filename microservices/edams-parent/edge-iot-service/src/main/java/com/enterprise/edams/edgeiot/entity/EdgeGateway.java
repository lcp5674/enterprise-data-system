package com.enterprise.edams.edgeiot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

/**
 * 边缘网关实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("edge_gateway")
public class EdgeGateway extends BaseEntity {
    
    /**
     * 网关ID
     */
    @TableField("gateway_id")
    private String gatewayId;
    
    /**
     * 网关名称
     */
    @TableField("gateway_name")
    private String gatewayName;
    
    /**
     * 网关类型
     */
    @TableField("gateway_type")
    private GatewayType gatewayType;
    
    /**
     * 状态
     */
    @TableField("status")
    private GatewayStatus status;
    
    /**
     * 部署位置
     */
    @TableField("location")
    private String location;
    
    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;
    
    /**
     * 连接协议
     */
    @TableField("protocol")
    private String protocol;
    
    /**
     * 固件版本
     */
    @TableField("firmware_version")
    private String firmwareVersion;
    
    /**
     * 最后同步时间
     */
    @TableField("last_sync_time")
    private LocalDateTime lastSyncTime;
    
    /**
     * 连接的设备数量
     */
    @TableField("device_count")
    private Integer deviceCount;
    
    /**
     * 同步状态
     */
    @TableField("sync_status")
    private SyncStatus syncStatus;
    
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
     * 存储使用率
     */
    @TableField("storage_usage")
    private Double storageUsage;
    
    /**
     * 扩展属性(JSON)
     */
    @TableField("properties")
    private String properties;
}
