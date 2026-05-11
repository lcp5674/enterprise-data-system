package com.enterprise.dataplatform.iot.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Entity Unit Tests")
class EntityTest {

    @Test
    @DisplayName("Should create EdgeDevice with all fields")
    void testEdgeDevice_Builder() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "production");

        EdgeDevice device = EdgeDevice.builder()
                .id(1L)
                .deviceId("SENSOR-001")
                .deviceName("Temperature Sensor")
                .deviceType("SENSOR")
                .manufacturer("SensorCorp")
                .model("TC-100")
                .serialNumber("SN123456")
                .firmwareVersion("1.0.0")
                .hardwareVersion("1.0")
                .status(EdgeDevice.DeviceStatus.ACTIVE)
                .online(true)
                .lastHeartbeat(now)
                .ipAddress("192.168.1.100")
                .macAddress("AA:BB:CC:DD:EE:FF")
                .location("Building A")
                .latitude(40.7128)
                .longitude(-74.0060)
                .tags(tags)
                .groups(Arrays.asList("GROUP-001", "GROUP-002"))
                .authToken("test-token")
                .authType(EdgeDevice.AuthType.TOKEN)
                .syncPriority(EdgeDevice.SyncPriority.WARM)
                .createdAt(now)
                .updatedAt(now)
                .deleted(false)
                .build();

        assertEquals(1L, device.getId());
        assertEquals("SENSOR-001", device.getDeviceId());
        assertEquals(EdgeDevice.DeviceStatus.ACTIVE, device.getStatus());
        assertTrue(device.isOnline());
        assertEquals(EdgeDevice.SyncPriority.WARM, device.getSyncPriority());
        assertEquals(EdgeDevice.AuthType.TOKEN, device.getAuthType());
        assertEquals(2, device.getGroups().size());
    }

    @Test
    @DisplayName("Should test DeviceStatus enum values")
    void testDeviceStatus_Enum() {
        assertEquals(5, EdgeDevice.DeviceStatus.values().length);
        assertEquals(EdgeDevice.DeviceStatus.ACTIVE, EdgeDevice.DeviceStatus.valueOf("ACTIVE"));
        assertEquals(EdgeDevice.DeviceStatus.INACTIVE, EdgeDevice.DeviceStatus.valueOf("INACTIVE"));
        assertEquals(EdgeDevice.DeviceStatus.MAINTENANCE, EdgeDevice.DeviceStatus.valueOf("MAINTENANCE"));
        assertEquals(EdgeDevice.DeviceStatus.FAULT, EdgeDevice.DeviceStatus.valueOf("FAULT"));
        assertEquals(EdgeDevice.DeviceStatus.UNKNOWN, EdgeDevice.DeviceStatus.valueOf("UNKNOWN"));
    }

    @Test
    @DisplayName("Should test AuthType enum values")
    void testAuthType_Enum() {
        assertEquals(4, EdgeDevice.AuthType.values().length);
        assertEquals(EdgeDevice.AuthType.TOKEN, EdgeDevice.AuthType.valueOf("TOKEN"));
        assertEquals(EdgeDevice.AuthType.CERTIFICATE, EdgeDevice.AuthType.valueOf("CERTIFICATE"));
        assertEquals(EdgeDevice.AuthType.USERNAME_PASSWORD, EdgeDevice.AuthType.valueOf("USERNAME_PASSWORD"));
        assertEquals(EdgeDevice.AuthType.API_KEY, EdgeDevice.AuthType.valueOf("API_KEY"));
    }

    @Test
    @DisplayName("Should test SyncPriority enum values")
    void testSyncPriority_Enum() {
        assertEquals(3, EdgeDevice.SyncPriority.values().length);
        assertEquals(EdgeDevice.SyncPriority.HOT, EdgeDevice.SyncPriority.valueOf("HOT"));
        assertEquals(EdgeDevice.SyncPriority.WARM, EdgeDevice.SyncPriority.valueOf("WARM"));
        assertEquals(EdgeDevice.SyncPriority.COLD, EdgeDevice.SyncPriority.valueOf("COLD"));
    }

    @Test
    @DisplayName("Should create DeviceData with all fields")
    void testDeviceData_Builder() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sensor", "temp-001");

        DeviceData data = DeviceData.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .dataType("temperature")
                .dataValue("25.5")
                .unit("°C")
                .quality("GOOD")
                .timestamp(now)
                .syncStatus(DeviceData.SyncStatus.PENDING)
                .metadata(metadata)
                .createdAt(now)
                .build();

        assertEquals("1", data.getId());
        assertEquals("SENSOR-001", data.getDeviceId());
        assertEquals("temperature", data.getDataType());
        assertEquals("25.5", data.getDataValue());
        assertEquals(DeviceData.SyncStatus.PENDING, data.getSyncStatus());
    }

    @Test
    @DisplayName("Should test SyncStatus enum values")
    void testSyncStatus_Enum() {
        assertEquals(4, DeviceData.SyncStatus.values().length);
        assertEquals(DeviceData.SyncStatus.PENDING, DeviceData.SyncStatus.valueOf("PENDING"));
        assertEquals(DeviceData.SyncStatus.SYNCING, DeviceData.SyncStatus.valueOf("SYNCING"));
        assertEquals(DeviceData.SyncStatus.SYNCED, DeviceData.SyncStatus.valueOf("SYNCED"));
        assertEquals(DeviceData.SyncStatus.FAILED, DeviceData.SyncStatus.valueOf("FAILED"));
    }

    @Test
    @DisplayName("Should create DeviceGroup with all fields")
    void testDeviceGroup_Builder() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("floor", "1");

        DeviceGroup group = DeviceGroup.builder()
                .id("1")
                .groupId("GROUP-001")
                .groupName("Production Sensors")
                .description("Production environment sensors")
                .parentId("PARENT-001")
                .deviceCount(10)
                .metadata(metadata)
                .createdAt(now)
                .updatedAt(now)
                .deleted(false)
                .build();

        assertEquals("1", group.getId());
        assertEquals("GROUP-001", group.getGroupId());
        assertEquals("Production Sensors", group.getGroupName());
        assertEquals("PARENT-001", group.getParentId());
        assertEquals(10, group.getDeviceCount());
    }

    @Test
    @DisplayName("Should create SyncRecord with all fields")
    void testSyncRecord_Builder() {
        LocalDateTime now = LocalDateTime.now();

        SyncRecord record = SyncRecord.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .syncType(SyncRecord.SyncType.INCREMENTAL)
                .status(SyncRecord.Status.COMPLETED)
                .priority("WARM")
                .startTime(now.minusMinutes(5))
                .endTime(now)
                .completedAt(now)
                .recordCount(100L)
                .dataSize(1024L)
                .compressedSize(512L)
                .retryCount(0)
                .errorMessage(null)
                .createdAt(now)
                .build();

        assertEquals("1", record.getId());
        assertEquals("SENSOR-001", record.getDeviceId());
        assertEquals(SyncRecord.SyncType.INCREMENTAL, record.getSyncType());
        assertEquals(SyncRecord.Status.COMPLETED, record.getStatus());
        assertEquals(100L, record.getRecordCount());
        assertEquals(1024L, record.getDataSize());
        assertEquals(512L, record.getCompressedSize());
    }

    @Test
    @DisplayName("Should test SyncType enum values")
    void testSyncType_Enum() {
        assertEquals(3, SyncRecord.SyncType.values().length);
        assertEquals(SyncRecord.SyncType.FULL, SyncRecord.SyncType.valueOf("FULL"));
        assertEquals(SyncRecord.SyncType.INCREMENTAL, SyncRecord.SyncType.valueOf("INCREMENTAL"));
        assertEquals(SyncRecord.SyncType.REALTIME, SyncRecord.SyncType.valueOf("REALTIME"));
    }

    @Test
    @DisplayName("Should test Status enum values")
    void testStatus_Enum() {
        assertEquals(4, SyncRecord.Status.values().length);
        assertEquals(SyncRecord.Status.PENDING, SyncRecord.Status.valueOf("PENDING"));
        assertEquals(SyncRecord.Status.IN_PROGRESS, SyncRecord.Status.valueOf("IN_PROGRESS"));
        assertEquals(SyncRecord.Status.COMPLETED, SyncRecord.Status.valueOf("COMPLETED"));
        assertEquals(SyncRecord.Status.FAILED, SyncRecord.Status.valueOf("FAILED"));
    }

    @Test
    @DisplayName("Should test DataThreshold")
    void testDataThreshold() {
        EdgeDevice.DataThreshold threshold = EdgeDevice.DataThreshold.builder()
                .minValue(10.0)
                .maxValue(40.0)
                .minAlertValue(5.0)
                .maxAlertValue(45.0)
                .build();

        assertEquals(10.0, threshold.getMinValue());
        assertEquals(40.0, threshold.getMaxValue());
        assertEquals(5.0, threshold.getMinAlertValue());
        assertEquals(45.0, threshold.getMaxAlertValue());
    }

    @Test
    @DisplayName("Should test EdgeDevice setters and getters")
    void testEdgeDevice_SettersGetters() {
        EdgeDevice device = new EdgeDevice();
        device.setId(1L);
        device.setDeviceId("SENSOR-001");
        device.setDeviceName("Test Sensor");
        device.setStatus(EdgeDevice.DeviceStatus.INACTIVE);
        device.setOnline(false);

        assertEquals(1L, device.getId());
        assertEquals("SENSOR-001", device.getDeviceId());
        assertEquals("Test Sensor", device.getDeviceName());
        assertEquals(EdgeDevice.DeviceStatus.INACTIVE, device.getStatus());
        assertFalse(device.isOnline());
    }
}
