package com.enterprise.edams.edgeiot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.edgeiot.dto.*;
import com.enterprise.edams.edgeiot.entity.EdgeDevice;

/**
 * 边缘设备服务接口
 */
public interface EdgeDeviceService extends IService<EdgeDevice> {
    
    /**
     * 注册设备
     */
    EdgeDevice registerDevice(DeviceRegisterRequest request);
    
    /**
     * 更新设备信息
     */
    EdgeDevice updateDevice(String deviceId, DeviceUpdateRequest request);
    
    /**
     * 删除设备
     */
    void deleteDevice(String deviceId);
    
    /**
     * 获取设备详情
     */
    EdgeDevice getDeviceById(String deviceId);
    
    /**
     * 分页查询设备
     */
    Page<EdgeDevice> searchDevices(DeviceSearchRequest request);
    
    /**
     * 设备心跳
     */
    void heartbeat(String deviceId);
    
    /**
     * 更新设备状态
     */
    void updateDeviceStatus(String deviceId, String status);
    
    /**
     * 获取设备统计
     */
    DeviceStatistics getDeviceStatistics();
    
    /**
     * 获取离线设备列表
     */
    java.util.List<EdgeDevice> getOfflineDevices(long offlineThreshold);
}
