package com.enterprise.dataplatform.iot.service;

import com.enterprise.dataplatform.iot.config.MqttConfig;
import com.enterprise.dataplatform.iot.config.MqttProperties;
import com.enterprise.dataplatform.iot.entity.DeviceData;
import com.enterprise.dataplatform.iot.entity.EdgeDevice;
import com.enterprise.dataplatform.iot.repository.DeviceDataRepository;
import com.enterprise.dataplatform.iot.repository.DeviceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MqttMessageService Unit Tests")
class MqttMessageServiceTest {

    @Mock
    private MqttConfig mqttConfig;

    @Mock
    private MqttProperties mqttProperties;

    @Mock
    private DeviceDataRepository deviceDataRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private MqttMessageService mqttMessageService;

    private ObjectMapper objectMapper;
    private EdgeDevice testDevice;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mqttMessageService = new MqttMessageService(
                mqttConfig, mqttProperties, deviceDataRepository, deviceRepository, objectMapper);

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
    @DisplayName("Should publish data successfully")
    void testPublishData_Success() {
        when(mqttProperties.getDataTopic()).thenReturn("edams/data");
        when(mqttProperties.getQos()).thenReturn(1);

        assertDoesNotThrow(() -> mqttMessageService.publishData("SENSOR-001", "test message"));

        verify(mqttConfig, times(1)).publish(
                eq("edams/data/SENSOR-001"),
                eq("test message"),
                eq(1),
                eq(false)
        );
    }

    @Test
    @DisplayName("Should publish command successfully")
    void testPublishCommand_Success() {
        when(mqttProperties.getCommandTopic()).thenReturn("edams/commands");
        when(mqttProperties.getQos()).thenReturn(1);

        assertDoesNotThrow(() ->
                mqttMessageService.publishCommand("SENSOR-001", "restart", java.util.Collections.emptyMap()));

        verify(mqttConfig, times(1)).publish(
                eq("edams/commands/SENSOR-001"),
                anyString(),
                eq(1),
                eq(false)
        );
    }

    @Test
    @DisplayName("Should check connection status correctly - connected")
    void testIsConnected_True() {
        when(mqttConfig.isConnected()).thenReturn(true);

        assertTrue(mqttMessageService.isConnected());
    }

    @Test
    @DisplayName("Should check connection status correctly - disconnected")
    void testIsConnected_False() {
        when(mqttConfig.isConnected()).thenReturn(false);

        assertFalse(mqttMessageService.isConnected());
    }

    @Test
    @DisplayName("Should get pending data successfully")
    void testGetPendingData_Success() {
        DeviceData pendingData = DeviceData.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .syncStatus(DeviceData.SyncStatus.PENDING)
                .build();

        when(deviceDataRepository.findBySyncStatus(DeviceData.SyncStatus.PENDING))
                .thenReturn(List.of(pendingData));

        List<DeviceData> result = mqttMessageService.getPendingData(10);

        assertEquals(1, result.size());
        assertEquals(DeviceData.SyncStatus.PENDING, result.get(0).getSyncStatus());
    }

    @Test
    @DisplayName("Should mark data as synced successfully")
    void testMarkDataAsSynced_Success() {
        DeviceData data = DeviceData.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .syncStatus(DeviceData.SyncStatus.PENDING)
                .build();

        when(deviceDataRepository.findById("1")).thenReturn(Optional.of(data));
        when(deviceDataRepository.save(any(DeviceData.class))).thenReturn(data);

        mqttMessageService.markDataAsSynced(List.of("1"));

        verify(deviceDataRepository, times(1)).save(argThat(d ->
                d.getSyncStatus() == DeviceData.SyncStatus.SYNCED
        ));
    }

    @Test
    @DisplayName("Should mark data as failed successfully")
    void testMarkDataAsFailed_Success() {
        DeviceData data = DeviceData.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .syncStatus(DeviceData.SyncStatus.PENDING)
                .build();

        when(deviceDataRepository.findById("1")).thenReturn(Optional.of(data));
        when(deviceDataRepository.save(any(DeviceData.class))).thenReturn(data);

        mqttMessageService.markDataAsFailed(List.of("1"));

        verify(deviceDataRepository, times(1)).save(argThat(d ->
                d.getSyncStatus() == DeviceData.SyncStatus.FAILED
        ));
    }

    @Test
    @DisplayName("Should return zero queued messages when empty")
    void testGetQueuedMessageCount_Empty() {
        int count = mqttMessageService.getQueuedMessageCount();
        assertEquals(0, count);
    }
}
