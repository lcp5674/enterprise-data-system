package com.enterprise.dataplatform.iot.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DTO Unit Tests")
class DtoTest {

    @Test
    @DisplayName("Should create ApiResponse successfully")
    void testApiResponse_Success() {
        ApiResponse<String> response = ApiResponse.success("Test data");

        assertEquals(200, response.getCode());
        assertEquals("Test data", response.getData());
        assertNull(response.getMessage());
        assertNull(response.getTimestamp());
    }

    @Test
    @DisplayName("Should create ApiResponse with message")
    void testApiResponse_WithMessage() {
        ApiResponse<String> response = ApiResponse.success("Test data", "Operation successful");

        assertEquals(200, response.getCode());
        assertEquals("Test data", response.getData());
        assertEquals("Operation successful", response.getMessage());
    }

    @Test
    @DisplayName("Should create error ApiResponse")
    void testApiResponse_Error() {
        ApiResponse<Object> response = ApiResponse.error(500, "Internal server error");

        assertEquals(500, response.getCode());
        assertEquals("Internal server error", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Should create error ApiResponse with data")
    void testApiResponse_ErrorWithData() {
        ApiResponse<Object> response = ApiResponse.error(400, "Bad request", "details");

        assertEquals(400, response.getCode());
        assertEquals("Bad request", response.getMessage());
        assertEquals("details", response.getData());
    }

    @Test
    @DisplayName("Should create DeviceRegistrationRequest with all fields")
    void testDeviceRegistrationRequest() {
        DeviceRegistrationRequest request = DeviceRegistrationRequest.builder()
                .deviceName("Temperature Sensor")
                .deviceType("SENSOR")
                .manufacturer("SensorCorp")
                .model("TC-100")
                .serialNumber("SN123456")
                .firmwareVersion("1.0.0")
                .hardwareVersion("1.0")
                .ipAddress("192.168.1.100")
                .macAddress("AA:BB:CC:DD:EE:FF")
                .location("Building A")
                .latitude(40.7128)
                .longitude(-74.0060)
                .build();

        assertEquals("Temperature Sensor", request.getDeviceName());
        assertEquals("SENSOR", request.getDeviceType());
        assertEquals("SensorCorp", request.getManufacturer());
    }

    @Test
    @DisplayName("Should create DeviceUpdateRequest")
    void testDeviceUpdateRequest() {
        DeviceUpdateRequest request = DeviceUpdateRequest.builder()
                .deviceName("Updated Sensor")
                .manufacturer("New Manufacturer")
                .build();

        assertEquals("Updated Sensor", request.getDeviceName());
        assertEquals("New Manufacturer", request.getManufacturer());
    }

    @Test
    @DisplayName("Should create DeviceResponse with all fields")
    void testDeviceResponse() {
        LocalDateTime now = LocalDateTime.now();

        DeviceResponse response = DeviceResponse.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .deviceName("Temperature Sensor")
                .deviceType("SENSOR")
                .status(com.enterprise.dataplatform.iot.entity.EdgeDevice.DeviceStatus.ACTIVE)
                .online(true)
                .lastHeartbeat(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals("1", response.getId());
        assertEquals("SENSOR-001", response.getDeviceId());
        assertTrue(response.isOnline());
    }

    @Test
    @DisplayName("Should create DeviceAuthRequest")
    void testDeviceAuthRequest() {
        DeviceAuthRequest request = DeviceAuthRequest.builder()
                .deviceId("SENSOR-001")
                .authToken("test-token-123")
                .build();

        assertEquals("SENSOR-001", request.getDeviceId());
        assertEquals("test-token-123", request.getAuthToken());
    }

    @Test
    @DisplayName("Should create DeviceDataRequest")
    void testDeviceDataRequest() {
        DeviceDataRequest request = DeviceDataRequest.builder()
                .deviceId("SENSOR-001")
                .dataType("temperature")
                .dataValue("25.5")
                .unit("°C")
                .quality("GOOD")
                .build();

        assertEquals("SENSOR-001", request.getDeviceId());
        assertEquals("temperature", request.getDataType());
        assertEquals("25.5", request.getDataValue());
        assertEquals("°C", request.getUnit());
    }

    @Test
    @DisplayName("Should create DataSyncRequest")
    void testDataSyncRequest() {
        DataSyncRequest request = DataSyncRequest.builder()
                .syncType(com.enterprise.dataplatform.iot.entity.SyncRecord.SyncType.INCREMENTAL)
                .priority(com.enterprise.dataplatform.iot.entity.EdgeDevice.SyncPriority.WARM)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now())
                .build();

        assertEquals(com.enterprise.dataplatform.iot.entity.SyncRecord.SyncType.INCREMENTAL,
                request.getSyncType());
        assertEquals(com.enterprise.dataplatform.iot.entity.EdgeDevice.SyncPriority.WARM,
                request.getPriority());
    }

    @Test
    @DisplayName("Should create GroupRequest")
    void testGroupRequest() {
        GroupRequest request = GroupRequest.builder()
                .groupName("Production Sensors")
                .description("Production environment sensors")
                .parentId("PARENT-001")
                .metadata(java.util.Map.of("floor", "1", "building", "A"))
                .build();

        assertEquals("Production Sensors", request.getGroupName());
        assertEquals("Production environment sensors", request.getDescription());
        assertEquals("PARENT-001", request.getParentId());
    }
}
