package com.enterprise.dataplatform.iot.controller;

import com.enterprise.dataplatform.iot.dto.ApiResponse;
import com.enterprise.dataplatform.iot.dto.DataSyncRequest;
import com.enterprise.dataplatform.iot.entity.DeviceData;
import com.enterprise.dataplatform.iot.entity.SyncRecord;
import com.enterprise.dataplatform.iot.service.DataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
@Tag(name = "Data Synchronization", description = "Edge-to-center data synchronization APIs")
public class DataSyncController {

    private final DataSyncService dataSyncService;

    @PostMapping("/device/{deviceId}")
    @Operation(summary = "Sync device data to center")
    public ResponseEntity<ApiResponse<SyncRecord>> syncDeviceData(
            @Parameter(description = "Device ID") @PathVariable String deviceId,
            @Parameter(description = "Sync type") @RequestParam(defaultValue = "INCREMENTAL") String syncType,
            @Parameter(description = "Priority") @RequestParam(defaultValue = "WARM") String priority) {
        log.info("Syncing data for device: {}", deviceId);
        SyncRecord record = dataSyncService.syncDeviceData(deviceId,
                SyncRecord.SyncType.valueOf(syncType),
                com.enterprise.dataplatform.iot.entity.EdgeDevice.SyncPriority.valueOf(priority));
        return ResponseEntity.ok(ApiResponse.success("Data sync initiated", record));
    }

    @PostMapping("/all")
    @Operation(summary = "Sync all online devices")
    public ResponseEntity<ApiResponse<List<SyncRecord>>> syncAllDevices(
            @RequestBody(required = false) DataSyncRequest request) {
        log.info("Syncing all devices");
        if (request == null) {
            request = new DataSyncRequest();
        }
        List<SyncRecord> records = dataSyncService.syncAllDevices(request);
        return ResponseEntity.ok(ApiResponse.success("Sync completed for all devices", records));
    }

    @GetMapping("/status/{deviceId}")
    @Operation(summary = "Get sync status for a device")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSyncStatus(
            @Parameter(description = "Device ID") @PathVariable String deviceId) {
        Map<String, Object> status = dataSyncService.getSyncStatus(deviceId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @PostMapping("/{deviceId}/retry")
    @Operation(summary = "Retry failed syncs for a device")
    public ResponseEntity<ApiResponse<Void>> retryFailedSyncs(
            @Parameter(description = "Device ID") @PathVariable String deviceId,
            @Parameter(description = "Max retries") @RequestParam(defaultValue = "3") int maxRetries) {
        log.info("Retrying failed syncs for device: {}", deviceId);
        dataSyncService.retryFailedSyncs(deviceId, maxRetries);
        return ResponseEntity.ok(ApiResponse.success("Retry initiated", null));
    }
}
