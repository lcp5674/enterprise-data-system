package com.enterprise.edams.edgeiot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.edgeiot.dto.*;
import com.enterprise.edams.edgeiot.entity.EdgeDevice;
import com.enterprise.edams.edgeiot.entity.enums.DeviceStatus;
import com.enterprise.edams.edgeiot.mapper.EdgeDeviceMapper;
import com.enterprise.edams.edgeiot.service.EdgeDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 边缘设备服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class EdgeDeviceServiceImpl extends ServiceImpl<EdgeDeviceMapper, EdgeDevice>
        implements EdgeDeviceService {
    
    @Override
    public EdgeDevice registerDevice(DeviceRegisterRequest request) {
        log.info("注册边缘设备: {}", request.getDeviceName());
        
        EdgeDevice device = EdgeDevice.builder()
                .deviceId(generateDeviceId())
                .deviceName(request.getDeviceName())
                .deviceType(request.getDeviceType())
                .status(DeviceStatus.OFFLINE)
                .gatewayId(request.getGatewayId())
                .location(request.getLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .ipAddress(request.getIpAddress())
                .macAddress(request.getMacAddress())
                .firmwareVersion(request.getFirmwareVersion())
                .description(request.getDescription())
                .sensorCount(0)
                .cpuUsage(0.0)
                .memoryUsage(0.0)
                .diskUsage(0.0)
                .build();
        
        this.save(device);
        log.info("设备注册成功: deviceId={}", device.getDeviceId());
        return device;
    }
    
    @Override
    public EdgeDevice updateDevice(String deviceId, DeviceUpdateRequest request) {
        EdgeDevice device = this.getOne(
                new LambdaQueryWrapper<EdgeDevice>()
                        .eq(EdgeDevice::getDeviceId, deviceId)
        );
        
        if (device == null) {
            throw new RuntimeException("设备不存在: " + deviceId);
        }
        
        if (request.getDeviceName() != null) {
            device.setDeviceName(request.getDeviceName());
        }
        if (request.getLocation() != null) {
            device.setLocation(request.getLocation());
        }
        if (request.getLatitude() != null) {
            device.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            device.setLongitude(request.getLongitude());
        }
        if (request.getFirmwareVersion() != null) {
            device.setFirmwareVersion(request.getFirmwareVersion());
        }
        if (request.getDescription() != null) {
            device.setDescription(request.getDescription());
        }
        if (request.getProperties() != null) {
            device.setProperties(request.getProperties());
        }
        
        this.updateById(device);
        log.info("设备更新成功: deviceId={}", deviceId);
        return device;
    }
    
    @Override
    public void deleteDevice(String deviceId) {
        EdgeDevice device = this.getOne(
                new LambdaQueryWrapper<EdgeDevice>()
                        .eq(EdgeDevice::getDeviceId, deviceId)
        );
        
        if (device == null) {
            throw new RuntimeException("设备不存在: " + deviceId);
        }
        
        this.removeById(device.getId());
        log.info("设备删除成功: deviceId={}", deviceId);
    }
    
    @Override
    public EdgeDevice getDeviceById(String deviceId) {
        return this.getOne(
                new LambdaQueryWrapper<EdgeDevice>()
                        .eq(EdgeDevice::getDeviceId, deviceId)
        );
    }
    
    @Override
    public Page<EdgeDevice> searchDevices(DeviceSearchRequest request) {
        Page<EdgeDevice> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<EdgeDevice> wrapper = new LambdaQueryWrapper<>();
        
        if (request.getDeviceName() != null) {
            wrapper.like(EdgeDevice::getDeviceName, request.getDeviceName());
        }
        if (request.getDeviceType() != null) {
            wrapper.eq(EdgeDevice::getDeviceType, request.getDeviceType());
        }
        if (request.getStatus() != null) {
            wrapper.eq(EdgeDevice::getStatus, request.getStatus());
        }
        if (request.getGatewayId() != null) {
            wrapper.eq(EdgeDevice::getGatewayId, request.getGatewayId());
        }
        if (request.getLocation() != null) {
            wrapper.like(EdgeDevice::getLocation, request.getLocation());
        }
        
        wrapper.orderByDesc(EdgeDevice::getCreatedTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    public void heartbeat(String deviceId) {
        EdgeDevice device = this.getOne(
                new LambdaQueryWrapper<EdgeDevice>()
                        .eq(EdgeDevice::getDeviceId, deviceId)
        );
        
        if (device != null) {
            device.setLastHeartbeat(LocalDateTime.now());
            if (device.getStatus() == DeviceStatus.OFFLINE || device.getStatus() == DeviceStatus.UNKNOWN) {
                device.setStatus(DeviceStatus.ONLINE);
            }
            this.updateById(device);
            log.debug("设备心跳更新: deviceId={}", deviceId);
        }
    }
    
    @Override
    public void updateDeviceStatus(String deviceId, String status) {
        EdgeDevice device = this.getOne(
                new LambdaQueryWrapper<EdgeDevice>()
                        .eq(EdgeDevice::getDeviceId, deviceId)
        );
        
        if (device != null) {
            device.setStatus(DeviceStatus.valueOf(status));
            this.updateById(device);
            log.info("设备状态更新: deviceId={}, status={}", deviceId, status);
        }
    }
    
    @Override
    public DeviceStatistics getDeviceStatistics() {
        long total = this.count();
        long online = this.count(new LambdaQueryWrapper<EdgeDevice>()
                .eq(EdgeDevice::getStatus, DeviceStatus.ONLINE));
        long offline = this.count(new LambdaQueryWrapper<EdgeDevice>()
                .eq(EdgeDevice::getStatus, DeviceStatus.OFFLINE));
        long fault = this.count(new LambdaQueryWrapper<EdgeDevice>()
                .eq(EdgeDevice::getStatus, DeviceStatus.FAULT));
        
        return DeviceStatistics.builder()
                .totalCount(total)
                .onlineCount(online)
                .offlineCount(offline)
                .faultCount(fault)
                .onlineRate(total > 0 ? (double) online / total * 100 : 0)
                .build();
    }
    
    @Override
    public List<EdgeDevice> getOfflineDevices(long offlineThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusNanos(offlineThreshold * 1_000_000);
        
        return this.list(new LambdaQueryWrapper<EdgeDevice>()
                .eq(EdgeDevice::getStatus, DeviceStatus.ONLINE)
                .lt(EdgeDevice::getLastHeartbeat, threshold));
    }
    
    private String generateDeviceId() {
        return "DEV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
