package com.enterprise.dataplatform.iot.service;

import com.enterprise.dataplatform.iot.config.MqttProperties;
import com.enterprise.dataplatform.iot.dto.DataSyncRequest;
import com.enterprise.dataplatform.iot.entity.DeviceData;
import com.enterprise.dataplatform.iot.entity.EdgeDevice;
import com.enterprise.dataplatform.iot.entity.SyncRecord;
import com.enterprise.dataplatform.iot.repository.DeviceDataRepository;
import com.enterprise.dataplatform.iot.repository.DeviceRepository;
import com.enterprise.dataplatform.iot.repository.SyncRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataSyncService Unit Tests")
class DataSyncServiceTest {

    @Mock
    private DeviceDataRepository deviceDataRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private SyncRecordRepository syncRecordRepository;

    @Mock
    private MqttProperties mqttProperties;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private DataSyncService dataSyncService;

    private ObjectMapper objectMapper;
    private EdgeDevice testDevice;
    private DeviceData testData;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        dataSyncService = new DataSyncService(
                deviceDataRepository, deviceRepository, syncRecordRepository,
                mqttProperties, redisTemplate, kafkaTemplate, objectMapper);

        testDevice = EdgeDevice.builder()
                .id(1L)
                .deviceId("SENSOR-001")
                .deviceName("Temperature Sensor")
                .deviceType("SENSOR")
                .status(EdgeDevice.DeviceStatus.ACTIVE)
                .online(true)
                .syncPriority(EdgeDevice.SyncPriority.WARM)
                .build();

        testData = DeviceData.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .dataType("temperature")
                .dataValue("25.5")
                .timestamp(LocalDateTime.now())
                .syncStatus(DeviceData.SyncStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should sync device data successfully")
    void testSyncDeviceData_Success() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        when(deviceDataRepository.findByDeviceIdAndTimeRange(anyString(), any(), any()))
                .thenReturn(Collections.singletonList(testData));
        when(deviceDataRepository.save(any(DeviceData.class))).thenReturn(testData);
        when(syncRecordRepository.save(any(SyncRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(redisTemplate.delete(anyString())).thenReturn(true);

        SyncRecord result = dataSyncService.syncDeviceData(
                "SENSOR-001", SyncRecord.SyncType.INCREMENTAL, EdgeDevice.SyncPriority.WARM);

        assertNotNull(result);
        assertEquals(SyncRecord.Status.COMPLETED, result.getStatus());
        assertEquals(1L, result.getRecordCount());
    }

    @Test
    @DisplayName("Should return null when sync already in progress")
    void testSyncDeviceData_AlreadyInProgress() {
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(false);

        SyncRecord result = dataSyncService.syncDeviceData(
                "SENSOR-001", SyncRecord.SyncType.INCREMENTAL, EdgeDevice.SyncPriority.WARM);

        assertNull(result);
    }

    @Test
    @DisplayName("Should complete with zero records when no pending data")
    void testSyncDeviceData_NoPendingData() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        when(deviceDataRepository.findByDeviceIdAndTimeRange(anyString(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(syncRecordRepository.save(any(SyncRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(redisTemplate.delete(anyString())).thenReturn(true);

        SyncRecord result = dataSyncService.syncDeviceData(
                "SENSOR-001", SyncRecord.SyncType.INCREMENTAL, EdgeDevice.SyncPriority.WARM);

        assertNotNull(result);
        assertEquals(SyncRecord.Status.COMPLETED, result.getStatus());
        assertEquals(0L, result.getRecordCount());
    }

    @Test
    @DisplayName("Should sync all devices successfully")
    void testSyncAllDevices_Success() throws Exception {
        DataSyncRequest request = new DataSyncRequest();

        when(deviceRepository.findOnlineDevices()).thenReturn(Collections.singletonList(testDevice));
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        when(deviceDataRepository.findByDeviceIdAndTimeRange(anyString(), any(), any()))
                .thenReturn(Collections.singletonList(testData));
        when(deviceDataRepository.save(any(DeviceData.class))).thenReturn(testData);
        when(syncRecordRepository.save(any(SyncRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(redisTemplate.delete(anyString())).thenReturn(true);

        List<SyncRecord> results = dataSyncService.syncAllDevices(request);

        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should get sync status for device")
    void testGetSyncStatus_Success() {
        SyncRecord recentRecord = SyncRecord.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .status(SyncRecord.Status.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();

        when(syncRecordRepository.findByDeviceId("SENSOR-001"))
                .thenReturn(Collections.singletonList(recentRecord));
        when(deviceDataRepository.countByDeviceIdAndSyncStatus("SENSOR-001", DeviceData.SyncStatus.PENDING))
                .thenReturn(5L);

        java.util.Map<String, Object> status = dataSyncService.getSyncStatus("SENSOR-001");

        assertNotNull(status);
        assertEquals(5L, status.get("pendingRecords"));
    }

    @Test
    @DisplayName("Should handle failed sync with retry")
    void testRetryFailedSyncs_Success() throws Exception {
        SyncRecord failedRecord = SyncRecord.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .status(SyncRecord.Status.FAILED)
                .syncType(SyncRecord.SyncType.INCREMENTAL)
                .priority("WARM")
                .retryCount(0)
                .build();

        when(syncRecordRepository.findByDeviceIdAndStatus("SENSOR-001", SyncRecord.Status.FAILED))
                .thenReturn(Collections.singletonList(failedRecord));
        when(syncRecordRepository.save(any(SyncRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        when(deviceDataRepository.findByDeviceIdAndTimeRange(anyString(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(redisTemplate.delete(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> dataSyncService.retryFailedSyncs("SENSOR-001", 3));

        verify(syncRecordRepository, atLeast(1)).save(any(SyncRecord.class));
    }
}
