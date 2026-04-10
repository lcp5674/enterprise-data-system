package com.enterprise.edams.edgeiot.dto;

import com.enterprise.edams.edgeiot.entity.enums.DeviceStatus;
import com.enterprise.edams.edgeiot.entity.enums.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备搜索请求
 */
@Data
@Schema(description = "设备搜索请求")
public class DeviceSearchRequest {
    
    @Schema(description = "设备名称")
    private String deviceName;
    
    @Schema(description = "设备类型")
    private DeviceType deviceType;
    
    @Schema(description = "设备状态")
    private DeviceStatus status;
    
    @Schema(description = "网关ID")
    private String gatewayId;
    
    @Schema(description = "设备位置")
    private String location;
    
    @Schema(description = "页码")
    private int pageNum = 1;
    
    @Schema(description = "每页数量")
    private int pageSize = 20;
}
