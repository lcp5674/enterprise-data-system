package com.enterprise.edams.edgeiot.schedule;

import com.enterprise.edams.edgeiot.entity.EdgeDevice;
import com.enterprise.edams.edgeiot.entity.enums.DeviceStatus;
import com.enterprise.edams.edgeiot.service.EdgeDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 设备健康检查定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceHealthCheckTask {
    
    private final EdgeDeviceService edgeDeviceService;
    
    @Value("${edge.iot.offline-threshold:180000}")
    private long offlineThreshold;
    
    /**
     * 每分钟检查设备离线状态
     */
    @Scheduled(fixedRate = 60000)
    public void checkOfflineDevices() {
        log.debug("开始设备离线检查...");
        
        try {
            // 获取离线设备
            List<EdgeDevice> offlineDevices = edgeDeviceService.getOfflineDevices(offlineThreshold);
            
            for (EdgeDevice device : offlineDevices) {
                device.setStatus(DeviceStatus.OFFLINE);
                edgeDeviceService.updateById(device);
                log.warn("设备已离线: deviceId={}, lastHeartbeat={}", 
                        device.getDeviceId(), device.getLastHeartbeat());
            }
            
            if (!offlineDevices.isEmpty()) {
                log.info("离线设备检查完成，发现{}个离线设备", offlineDevices.size());
            }
        } catch (Exception e) {
            log.error("设备离线检查异常", e);
        }
    }
}
