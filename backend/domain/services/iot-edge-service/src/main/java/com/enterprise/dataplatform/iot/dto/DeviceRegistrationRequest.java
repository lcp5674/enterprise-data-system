package com.enterprise.dataplatform.iot.dto;

import com.enterprise.dataplatform.iot.entity.EdgeDevice;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRegistrationRequest {

    @NotBlank(message = "Device name is required")
    private String deviceName;

    @NotBlank(message = "Device type is required")
    private String deviceType;

    private String manufacturer;
    private String model;
    private String serialNumber;
    private String firmwareVersion;
    private String hardwareVersion;

    private String ipAddress;
    private String macAddress;
    private String location;
    private Double latitude;
    private Double longitude;

    private Map<String, String> tags;
    private List<String> groups;
    private Map<String, Object> properties;

    private EdgeDevice.AuthType authType;
    private EdgeDevice.SyncPriority syncPriority;
    private EdgeDevice.DataThreshold dataThreshold;
    private List<String> capabilities;
}
