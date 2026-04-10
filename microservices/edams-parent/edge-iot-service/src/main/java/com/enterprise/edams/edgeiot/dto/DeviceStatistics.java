package com.enterprise.edams.edgeiot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 设备统计
 */
@Data
@Builder
@Schema(description = "设备统计")
public class DeviceStatistics {
    
    @Schema(description = "设备总数")
    private long totalCount;
    
    @Schema(description = "在线设备数")
    private long onlineCount;
    
    @Schema(description = "离线设备数")
    private long offlineCount;
    
    @Schema(description = "故障设备数")
    private long faultCount;
    
    @Schema(description = "在线率(%)")
    private double onlineRate;
}
