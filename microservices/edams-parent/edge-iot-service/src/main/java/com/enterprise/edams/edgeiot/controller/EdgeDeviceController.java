package com.enterprise.edams.edgeiot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.edgeiot.dto.*;
import com.enterprise.edams.edgeiot.entity.EdgeDevice;
import com.enterprise.edams.edgeiot.service.EdgeDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 边缘设备控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/edge/devices")
@Tag(name = "边缘设备管理", description = "边缘设备注册、更新、查询等接口")
public class EdgeDeviceController {
    
    private final EdgeDeviceService edgeDeviceService;
    
    @PostMapping
    @Operation(summary = "注册设备", description = "注册新的边缘设备")
    public EdgeDevice registerDevice(@Valid @RequestBody DeviceRegisterRequest request) {
        log.info("注册设备请求: {}", request.getDeviceName());
        return edgeDeviceService.registerDevice(request);
    }
    
    @PutMapping("/{deviceId}")
    @Operation(summary = "更新设备", description = "更新设备信息")
    public EdgeDevice updateDevice(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @Valid @RequestBody DeviceUpdateRequest request) {
        log.info("更新设备请求: deviceId={}", deviceId);
        return edgeDeviceService.updateDevice(deviceId, request);
    }
    
    @DeleteMapping("/{deviceId}")
    @Operation(summary = "删除设备", description = "删除指定设备")
    public void deleteDevice(
            @Parameter(description = "设备ID") @PathVariable String deviceId) {
        log.info("删除设备请求: deviceId={}", deviceId);
        edgeDeviceService.deleteDevice(deviceId);
    }
    
    @GetMapping("/{deviceId}")
    @Operation(summary = "获取设备详情", description = "根据设备ID获取设备详情")
    public EdgeDevice getDevice(
            @Parameter(description = "设备ID") @PathVariable String deviceId) {
        return edgeDeviceService.getDeviceById(deviceId);
    }
    
    @GetMapping
    @Operation(summary = "查询设备列表", description = "分页查询设备列表")
    public Page<EdgeDevice> searchDevices(DeviceSearchRequest request) {
        return edgeDeviceService.searchDevices(request);
    }
    
    @PostMapping("/{deviceId}/heartbeat")
    @Operation(summary = "设备心跳", description = "更新设备心跳")
    public void heartbeat(
            @Parameter(description = "设备ID") @PathVariable String deviceId) {
        edgeDeviceService.heartbeat(deviceId);
    }
    
    @PutMapping("/{deviceId}/status")
    @Operation(summary = "更新设备状态", description = "更新设备状态")
    public void updateStatus(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @Parameter(description = "状态") @RequestParam String status) {
        edgeDeviceService.updateDeviceStatus(deviceId, status);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取设备统计", description = "获取设备统计信息")
    public DeviceStatistics getStatistics() {
        return edgeDeviceService.getDeviceStatistics();
    }
    
    @GetMapping("/offline")
    @Operation(summary = "获取离线设备", description = "获取离线设备列表")
    public java.util.List<EdgeDevice> getOfflineDevices(
            @Parameter(description = "离线阈值(毫秒)") @RequestParam(defaultValue = "180000") long offlineThreshold) {
        return edgeDeviceService.getOfflineDevices(offlineThreshold);
    }
}
