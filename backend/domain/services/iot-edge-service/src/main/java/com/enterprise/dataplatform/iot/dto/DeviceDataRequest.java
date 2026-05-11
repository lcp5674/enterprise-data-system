package com.enterprise.dataplatform.iot.dto;

import com.enterprise.dataplatform.iot.entity.DeviceData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDataRequest {

    private String deviceId;
    private String dataType;
    private String dataKey;
    private String dataValue;
    private String unit;
    private LocalDateTime timestamp;
    private DeviceData.DataQuality quality;
    private Map<String, String> tags;
    private Map<String, Object> metadata;
}
