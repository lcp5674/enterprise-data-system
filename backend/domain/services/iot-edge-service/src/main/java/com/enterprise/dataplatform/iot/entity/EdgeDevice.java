package com.enterprise.dataplatform.iot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("edge_device")
@Document(collection = "edge_devices")
public class EdgeDevice {

    @Id
    private String id;

    @TableField("device_id")
    private String deviceId;

    @TableField("device_name")
    private String deviceName;

    @TableField("device_type")
    private String deviceType;

    @TableField("manufacturer")
    private String manufacturer;

    @TableField("model")
    private String model;

    @TableField("serial_number")
    private String serialNumber;

    @TableField("firmware_version")
    private String firmwareVersion;

    @TableField("hardware_version")
    private String hardwareVersion;

    @TableField("status")
    private DeviceStatus status;

    @TableField("online")
    private boolean online;

    @TableField("last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("mac_address")
    private String macAddress;

    @TableField("location")
    private String location;

    @TableField("latitude")
    private Double latitude;

    @TableField("longitude")
    private Double longitude;

    @TableField("tags")
    private Map<String, String> tags;

    @TableField("groups")
    private List<String> groups;

    @TableField("properties")
    private Map<String, Object> properties;

    @TableField("auth_token")
    private String authToken;

    @TableField("auth_type")
    private AuthType authType;

    @TableField("connection_info")
    private String connectionInfo;

    @TableField("capabilities")
    private List<String> capabilities;

    @TableField("data_threshold")
    private DataThreshold dataThreshold;

    @TableField("sync_priority")
    private SyncPriority syncPriority;

    @TableField("created_at")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField("created_by")
    private String createdBy;

    @TableField("updated_by")
    private String updatedBy;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    public enum DeviceStatus {
        ACTIVE,
        INACTIVE,
        MAINTENANCE,
        FAULT,
        UNKNOWN
    }

    public enum AuthType {
        TOKEN,
        CERTIFICATE,
        USERNAME_PASSWORD,
        API_KEY
    }

    public enum SyncPriority {
        HOT,
        WARM,
        COLD
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataThreshold {
        private Double minValue;
        private Double maxValue;
        private Integer maxDataSize;
        private Integer syncIntervalSeconds;
    }
}
