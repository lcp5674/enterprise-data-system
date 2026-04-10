package com.enterprise.edams.edgeiot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备更新请求
 */
@Data
@Schema(description = "设备更新请求")
public class DeviceUpdateRequest {
    
    @Schema(description = "设备名称")
    private String deviceName;
    
    @Schema(description = "设备位置")
    private String location;
    
    @Schema(description = "纬度")
    private Double latitude;
    
    @Schema(description = "经度")
    private Double longitude;
    
    @Schema(description = "固件版本")
    private String firmwareVersion;
    
    @Schema(description = "设备描述")
    private String description;
    
    @Schema(description = "扩展属性")
    private String properties;
}
