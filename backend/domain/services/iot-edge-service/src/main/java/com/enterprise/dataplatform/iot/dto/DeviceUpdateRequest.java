package com.enterprise.dataplatform.iot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceUpdateRequest {

    private String deviceName;
    private String deviceType;
    private String manufacturer;
    private String model;
    private String firmwareVersion;
    private String hardwareVersion;

    private String location;
    private Double latitude;
    private Double longitude;

    private Map<String, String> tags;
    private Map<String, Object> properties;
}
