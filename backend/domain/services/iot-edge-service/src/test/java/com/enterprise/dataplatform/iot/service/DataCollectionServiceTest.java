package com.enterprise.dataplatform.iot.service;

import com.enterprise.dataplatform.iot.config.MqttProperties;
import com.enterprise.dataplatform.iot.entity.DeviceData;
import com.enterprise.dataplatform.iot.entity.EdgeDevice;
import com.enterprise.dataplatform.iot.repository.DeviceDataRepository;
import com.enterprise.dataplatform.iot.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataCollectionService Unit Tests")
class DataCollectionServiceTest {

    @Mock
    private DeviceDataRepository deviceDataRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DataCollectionConfig dataCollectionConfig;

    @Mock
    private AlertService alertService;

    @InjectMocks
    private DataCollectionService dataCollectionService;

    private EdgeDevice testDevice;

    @BeforeEach
    void setUp() {
        testDevice = EdgeDevice.builder()
                .id(1L)
                .deviceId("SENSOR-001")
                .deviceName("Temperature Sensor")
                .deviceType("SENSOR")
                .status(EdgeDevice.DeviceStatus.ACTIVE)
                .online(true)
                .build();
    }

    @Test
    @DisplayName("Should collect data successfully")
    void testCollectData_Success() {
        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(dataCollectionConfig.getBatchSize()).thenReturn(100);

        assertDoesNotThrow(() ->
                dataCollectionService.collectData("SENSOR-001", "temperature", "25.5"));
    }

    @Test
    @DisplayName("Should skip collection for unknown device")
    void testCollectData_UnknownDevice() {
        when(deviceRepository.findByDeviceId("UNKNOWN")).thenReturn(null);

        assertDoesNotThrow(() ->
                dataCollectionService.collectData("UNKNOWN", "temperature", "25.5"));
    }

    @Test
    @DisplayName("Should register transformer successfully")
    void testRegisterTransformer_Success() {
        DataCollectionService.DataTransformer transformer = raw -> raw.toString().toUpperCase();

        assertDoesNotThrow(() ->
                dataCollectionService.registerTransformer("temperature", transformer));
    }

    @Test
    @DisplayName("Should collect batch data successfully")
    void testCollectBatchData_Success() {
        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(dataCollectionConfig.getBatchSize()).thenReturn(100);

        List<java.util.Map<String, Object>> batchData = List.of(
                java.util.Map.of("dataType", "temperature", "data", "25.5"),
                java.util.Map.of("dataType", "humidity", "data", "60.0")
        );

        assertDoesNotThrow(() ->
                dataCollectionService.collectBatchData("SENSOR-001", batchData));
    }

    @Test
    @DisplayName("Should get recent data successfully")
    void testGetRecentData_Success() {
        DeviceData data = DeviceData.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .dataValue("25.5")
                .timestamp(LocalDateTime.now())
                .build();

        when(deviceDataRepository.findByDeviceId("SENSOR-001"))
                .thenReturn(Collections.singletonList(data));

        List<DeviceData> result = dataCollectionService.getRecentData("SENSOR-001", 10);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should handle data with threshold validation")
    void testCollectData_WithThreshold() {
        EdgeDevice.DataThreshold threshold = EdgeDevice.DataThreshold.builder()
                .minValue(10.0)
                .maxValue(40.0)
                .build();
        testDevice.setDataThreshold(threshold);

        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(dataCollectionConfig.getBatchSize()).thenReturn(100);

        assertDoesNotThrow(() ->
                dataCollectionService.collectData("SENSOR-001", "temperature", "25.5"));
    }

    @Test
    @DisplayName("Should trigger alert for value above threshold")
    void testCollectData_ThresholdViolation() {
        EdgeDevice.DataThreshold threshold = EdgeDevice.DataThreshold.builder()
                .minValue(10.0)
                .maxValue(40.0)
                .build();
        testDevice.setDataThreshold(threshold);

        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(dataCollectionConfig.getBatchSize()).thenReturn(100);

        assertDoesNotThrow(() ->
                dataCollectionService.collectData("SENSOR-001", "temperature", "50.0"));

        verify(alertService, atLeastOnce()).sendAlert(
                eq("SENSOR-001"),
                eq(AlertService.AlertLevel.WARNING),
                anyString()
        );
    }

    @Test
    @DisplayName("Should cleanup old data successfully")
    void testCleanupOldData_Success() {
        DeviceData oldData = DeviceData.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .syncStatus(DeviceData.SyncStatus.SYNCED)
                .createdAt(LocalDateTime.now().minusDays(31))
                .build();

        when(deviceDataRepository.findAll()).thenReturn(Collections.singletonList(oldData));

        assertDoesNotThrow(() ->
                dataCollectionService.cleanupOldData(30));

        verify(deviceDataRepository, atLeastOnce()).delete(any(DeviceData.class));
    }

    @Test
    @DisplayName("Should not cleanup unsynced data")
    void testCleanupOldData_Unsynchronized() {
        DeviceData unsyncedData = DeviceData.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .syncStatus(DeviceData.SyncStatus.PENDING)
                .createdAt(LocalDateTime.now().minusDays(31))
                .build();

        when(deviceDataRepository.findAll()).thenReturn(Collections.singletonList(unsyncedData));

        assertDoesNotThrow(() ->
                dataCollectionService.cleanupOldData(30));

        verify(deviceDataRepository, never()).delete(any(DeviceData.class));
    }
}
