package com.enterprise.dataplatform.iot.dto;

import com.enterprise.dataplatform.iot.entity.EdgeDevice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {

    private String id;
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private String firmwareVersion;
    private String hardwareVersion;
    private EdgeDevice.DeviceStatus status;
    private boolean online;
    private LocalDateTime lastHeartbeat;
    private String ipAddress;
    private String macAddress;
    private String location;
    private Double latitude;
    private Double longitude;
    private Map<String, String> tags;
    private List<String> groups;
    private Map<String, Object> properties;
    private EdgeDevice.AuthType authType;
    private List<String> capabilities;
    private EdgeDevice.DataThreshold dataThreshold;
    private EdgeDevice.SyncPriority syncPriority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
