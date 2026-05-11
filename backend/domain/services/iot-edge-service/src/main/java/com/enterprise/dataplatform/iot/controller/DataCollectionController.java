package com.enterprise.dataplatform.iot.controller;

import com.enterprise.dataplatform.iot.dto.ApiResponse;
import com.enterprise.dataplatform.iot.dto.DeviceDataRequest;
import com.enterprise.dataplatform.iot.entity.DeviceData;
import com.enterprise.dataplatform.iot.service.DataCollectionService;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
@Tag(name = "Data Collection", description = "Device data collection APIs")
public class DataCollectionController {

    private final DataCollectionService dataCollectionService;
    private final DeviceService deviceService;

    @PostMapping("/collect")
    @Operation(summary = "Collect data from a device")
    public ResponseEntity<ApiResponse<Void>> collectData(
            @Valid @RequestBody DeviceDataRequest request) {
        log.info("Collecting data from device: {}", request.getDeviceId());
        dataCollectionService.collectData(
                request.getDeviceId(),
                request.getDataType(),
                request.getDataValue()
        );
        return ResponseEntity.ok(ApiResponse.success("Data collected successfully", null));
    }

    @PostMapping("/collect/batch")
    @Operation(summary = "Collect batch data from a device")
    public ResponseEntity<ApiResponse<Void>> collectBatchData(
            @RequestBody List<DeviceDataRequest> requests) {
        for (DeviceDataRequest request : requests) {
            dataCollectionService.collectData(
                    request.getDeviceId(),
                    request.getDataType(),
                    request.getDataValue()
            );
        }
        return ResponseEntity.ok(ApiResponse.success("Batch data collected", null));
    }

    @GetMapping("/device/{deviceId}/recent")
    @Operation(summary = "Get recent data for a device")
    public ResponseEntity<ApiResponse<List<DeviceData>>> getRecentData(
            @Parameter(description = "Device ID") @PathVariable String deviceId,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {
        List<DeviceData> data = dataCollectionService.getRecentData(deviceId, limit);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @DeleteMapping("/cleanup")
    @Operation(summary = "Cleanup old data")
    public ResponseEntity<ApiResponse<Void>> cleanupOldData(
            @Parameter(description = "Retention days") @RequestParam(defaultValue = "30") int retentionDays) {
        dataCollectionService.cleanupOldData(retentionDays);
        return ResponseEntity.ok(ApiResponse.success("Old data cleaned up", null));
    }
}
