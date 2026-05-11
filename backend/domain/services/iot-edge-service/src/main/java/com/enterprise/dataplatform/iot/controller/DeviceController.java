package com.enterprise.dataplatform.iot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.dataplatform.iot.dto.*;
import com.enterprise.dataplatform.iot.entity.DeviceGroup;
import com.enterprise.dataplatform.iot.entity.EdgeDevice;
import com.enterprise.dataplatform.iot.service.DataCollectionService;
import com.enterprise.dataplatform.iot.service.DataSyncService;
import com.enterprise.dataplatform.iot.service.DeviceGroupService;
import com.enterprise.dataplatform.iot.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Device Management", description = "Device registration and management APIs")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceGroupService deviceGroupService;
    private final DataSyncService dataSyncService;
    private final DataCollectionService dataCollectionService;

    @PostMapping("/register")
    @Operation(summary = "Register a new device")
    public ResponseEntity<ApiResponse<DeviceResponse>> registerDevice(
            @Valid @RequestBody DeviceRegistrationRequest request) {
        log.info("Registering device: {}", request.getDeviceName());
        DeviceResponse response = deviceService.registerDevice(request);
        return ResponseEntity.ok(ApiResponse.success("Device registered successfully", response));
    }

    @GetMapping("/{deviceId}")
    @Operation(summary = "Get device by ID")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDevice(
            @Parameter(description = "Device ID") @PathVariable String deviceId) {
        DeviceResponse response = deviceService.getDeviceById(deviceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all devices with pagination")
    public ResponseEntity<ApiResponse<IPage<DeviceResponse>>> getDevices(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Device status") @RequestParam(required = false) EdgeDevice.DeviceStatus status,
            @Parameter(description = "Device type") @RequestParam(required = false) String deviceType) {
        IPage<DeviceResponse> devices = deviceService.getDevicesPage(page, size, keyword, status, deviceType);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @GetMapping("/online")
    @Operation(summary = "Get all online devices")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getOnlineDevices() {
        List<DeviceResponse> devices = deviceService.getOnlineDevices();
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get device statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDeviceStatistics() {
        Map<String, Object> statistics = deviceService.getDeviceStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @PutMapping("/{deviceId}")
    @Operation(summary = "Update device information")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDevice(
            @Parameter(description = "Device ID") @PathVariable String deviceId,
            @Valid @RequestBody DeviceUpdateRequest request) {
        log.info("Updating device: {}", deviceId);
        DeviceResponse response = deviceService.updateDevice(deviceId, request);
        return ResponseEntity.ok(ApiResponse.success("Device updated successfully", response));
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "Delete a device")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(
            @Parameter(description = "Device ID") @PathVariable String deviceId) {
        log.info("Deleting device: {}", deviceId);
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Device deleted successfully", null));
    }

    @PostMapping("/auth")
    @Operation(summary = "Authenticate a device")
    public ResponseEntity<ApiResponse<Boolean>> authenticateDevice(
            @Valid @RequestBody DeviceAuthRequest request) {
        boolean result = deviceService.authenticateDevice(request);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("Authentication successful", true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("400", "Authentication failed"));
        }
    }

    @PostMapping("/{deviceId}/heartbeat")
    @Operation(summary = "Update device heartbeat")
    public ResponseEntity<ApiResponse<Void>> updateHeartbeat(
            @Parameter(description = "Device ID") @PathVariable String deviceId) {
        deviceService.updateHeartbeat(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Heartbeat updated", null));
    }

    @PutMapping("/{deviceId}/status")
    @Operation(summary = "Update device status")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDeviceStatus(
            @Parameter(description = "Device ID") @PathVariable String deviceId,
            @Parameter(description = "New status") @RequestParam EdgeDevice.DeviceStatus status) {
        DeviceResponse response = deviceService.updateDeviceStatus(deviceId, status);
        return ResponseEntity.ok(ApiResponse.success("Status updated", response));
    }

    @PostMapping("/{deviceId}/token/regenerate")
    @Operation(summary = "Regenerate device auth token")
    public ResponseEntity<ApiResponse<String>> regenerateToken(
            @Parameter(description = "Device ID") @PathVariable String deviceId) {
        String newToken = deviceService.regenerateAuthToken(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Token regenerated", newToken));
    }

    @PostMapping("/{deviceId}/groups/{groupId}/add")
    @Operation(summary = "Add device to group")
    public ResponseEntity<ApiResponse<DeviceResponse>> addDeviceToGroup(
            @PathVariable String deviceId, @PathVariable String groupId) {
        DeviceResponse response = deviceService.addDeviceToGroup(deviceId, groupId);
        return ResponseEntity.ok(ApiResponse.success("Device added to group", response));
    }

    @PostMapping("/{deviceId}/groups/{groupId}/remove")
    @Operation(summary = "Remove device from group")
    public ResponseEntity<ApiResponse<DeviceResponse>> removeDeviceFromGroup(
            @PathVariable String deviceId, @PathVariable String groupId) {
        DeviceResponse response = deviceService.removeDeviceFromGroup(deviceId, groupId);
        return ResponseEntity.ok(ApiResponse.success("Device removed from group", response));
    }

    @PutMapping("/{deviceId}/tags")
    @Operation(summary = "Update device tags")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDeviceTags(
            @PathVariable String deviceId, @RequestBody Map<String, String> tags) {
        DeviceResponse response = deviceService.updateDeviceTags(deviceId, tags);
        return ResponseEntity.ok(ApiResponse.success("Tags updated", response));
    }

    @GetMapping("/group/{groupId}")
    @Operation(summary = "Get devices by group")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getDevicesByGroup(
            @PathVariable String groupId) {
        List<DeviceResponse> devices = deviceService.getDevicesByGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @GetMapping("/tag/{key}/{value}")
    @Operation(summary = "Get devices by tag")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getDevicesByTag(
            @PathVariable String key, @PathVariable String value) {
        List<DeviceResponse> devices = deviceService.getDevicesByTag(key, value);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }
}
