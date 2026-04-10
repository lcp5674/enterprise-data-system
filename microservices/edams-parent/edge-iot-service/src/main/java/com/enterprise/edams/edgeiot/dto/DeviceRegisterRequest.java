package com.enterprise.edams.edgeiot.dto;

import com.enterprise.edams.edgeiot.entity.enums.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设备注册请求
 */
@Data
@Schema(description = "设备注册请求")
public class DeviceRegisterRequest {
    
    @NotBlank(message = "设备名称不能为空")
    @Schema(description = "设备名称")
    private String deviceName;
    
    @NotNull(message = "设备类型不能为空")
    @Schema(description = "设备类型")
    private DeviceType deviceType;
    
    @Schema(description = "所属网关ID")
    private String gatewayId;
    
    @Schema(description = "设备位置")
    private String location;
    
    @Schema(description = "纬度")
    private Double latitude;
    
    @Schema(description = "经度")
    private Double longitude;
    
    @Schema(description = "IP地址")
    private String ipAddress;
    
    @Schema(description = "MAC地址")
    private String macAddress;
    
    @Schema(description = "固件版本")
    private String firmwareVersion;
    
    @Schema(description = "设备描述")
    private String description;
}
