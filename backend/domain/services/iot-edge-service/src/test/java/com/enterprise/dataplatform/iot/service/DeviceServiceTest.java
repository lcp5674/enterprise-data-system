package com.enterprise.dataplatform.iot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.dataplatform.iot.config.MqttConfig;
import com.enterprise.dataplatform.iot.config.MqttProperties;
import com.enterprise.dataplatform.iot.dto.*;
import com.enterprise.dataplatform.iot.entity.EdgeDevice;
import com.enterprise.dataplatform.iot.repository.DeviceGroupRepository;
import com.enterprise.dataplatform.iot.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceService Unit Tests")
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceGroupRepository deviceGroupRepository;

    @Mock
    private MqttConfig mqttConfig;

    @Mock
    private MqttProperties mqttProperties;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private DeviceService deviceService;

    private EdgeDevice testDevice;
    private DeviceRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        testDevice = EdgeDevice.builder()
                .id(1L)
                .deviceId("SENSOR-001")
                .deviceName("Temperature Sensor 1")
                .deviceType("SENSOR")
                .manufacturer("SensorCorp")
                .model("TC-100")
                .serialNumber("SN123456")
                .firmwareVersion("1.0.0")
                .hardwareVersion("1.0")
                .status(EdgeDevice.DeviceStatus.INACTIVE)
                .online(false)
                .authToken("test-token-123")
                .authType(EdgeDevice.AuthType.TOKEN)
                .syncPriority(EdgeDevice.SyncPriority.WARM)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        registrationRequest = DeviceRegistrationRequest.builder()
                .deviceName("Temperature Sensor 1")
                .deviceType("SENSOR")
                .manufacturer("SensorCorp")
                .model("TC-100")
                .serialNumber("SN123456")
                .build();

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should register device successfully")
    void testRegisterDevice_Success() {
        when(deviceRepository.findByDeviceId(anyString())).thenReturn(null);
        when(mqttProperties.getDataTopic()).thenReturn("edams/data");
        when(mqttProperties.getCommandTopic()).thenReturn("edams/commands");
        when(mqttProperties.getQos()).thenReturn(1);
        when(deviceRepository.insert(any(EdgeDevice.class))).thenReturn(1);

        DeviceResponse response = deviceService.registerDevice(registrationRequest);

        assertNotNull(response);
        assertEquals("Temperature Sensor 1", response.getDeviceName());
        assertEquals("SENSOR", response.getDeviceType());
        verify(deviceRepository, times(1)).insert(any(EdgeDevice.class));
    }

    @Test
    @DisplayName("Should throw exception when registering duplicate device")
    void testRegisterDevice_Duplicate() {
        when(deviceRepository.findByDeviceId(anyString())).thenReturn(testDevice);

        assertThrows(IllegalArgumentException.class, () ->
                deviceService.registerDevice(registrationRequest));
    }

    @Test
    @DisplayName("Should get device by deviceId successfully")
    void testGetDeviceById_Success() {
        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);

        DeviceResponse response = deviceService.getDeviceById("SENSOR-001");

        assertNotNull(response);
        assertEquals("SENSOR-001", response.getDeviceId());
    }

    @Test
    @DisplayName("Should throw exception when device not found")
    void testGetDeviceById_NotFound() {
        when(deviceRepository.findByDeviceId("UNKNOWN")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                deviceService.getDeviceById("UNKNOWN"));
    }

    @Test
    @DisplayName("Should get all devices successfully")
    void testGetAllDevices_Success() {
        List<EdgeDevice> devices = Arrays.asList(testDevice);
        when(deviceRepository.selectList(any())).thenReturn(devices);

        List<DeviceResponse> responses = deviceService.getAllDevices();

        assertEquals(1, responses.size());
        assertEquals("SENSOR-001", responses.get(0).getDeviceId());
    }

    @Test
    @DisplayName("Should get devices page successfully")
    void testGetDevicesPage_Success() {
        Page<EdgeDevice> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(testDevice));
        page.setTotal(1);

        when(deviceRepository.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        IPage<DeviceResponse> result = deviceService.getDevicesPage(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("Should update device successfully")
    void testUpdateDevice_Success() {
        DeviceUpdateRequest updateRequest = DeviceUpdateRequest.builder()
                .deviceName("Updated Sensor")
                .manufacturer("New Manufacturer")
                .build();

        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(deviceRepository.updateById(any(EdgeDevice.class))).thenReturn(true);

        DeviceResponse response = deviceService.updateDevice("SENSOR-001", updateRequest);

        assertNotNull(response);
        verify(deviceRepository, times(1)).updateById(any(EdgeDevice.class));
    }

    @Test
    @DisplayName("Should delete device successfully")
    void testDeleteDevice_Success() {
        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(deviceRepository.updateById(any(EdgeDevice.class))).thenReturn(true);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> deviceService.deleteDevice("SENSOR-001"));

        verify(deviceRepository, times(1)).updateById(argThat(device ->
            device.getDeleted() != null && device.getDeleted()
        ));
    }

    @Test
    @DisplayName("Should authenticate device successfully with valid token")
    void testAuthenticateDevice_Success() {
        DeviceAuthRequest authRequest = DeviceAuthRequest.builder()
                .deviceId("SENSOR-001")
                .authToken("test-token-123")
                .build();

        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(deviceRepository.updateById(any(EdgeDevice.class))).thenReturn(true);

        boolean result = deviceService.authenticateDevice(authRequest);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should fail authentication with invalid token")
    void testAuthenticateDevice_InvalidToken() {
        DeviceAuthRequest authRequest = DeviceAuthRequest.builder()
                .deviceId("SENSOR-001")
                .authToken("wrong-token")
                .build();

        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);

        boolean result = deviceService.authenticateDevice(authRequest);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail authentication for unknown device")
    void testAuthenticateDevice_UnknownDevice() {
        DeviceAuthRequest authRequest = DeviceAuthRequest.builder()
                .deviceId("UNKNOWN")
                .authToken("test-token")
                .build();

        when(deviceRepository.findByDeviceId("UNKNOWN")).thenReturn(null);

        boolean result = deviceService.authenticateDevice(authRequest);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should update device online status to online")
    void testUpdateDeviceOnlineStatus_Online() {
        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(deviceRepository.updateById(any(EdgeDevice.class))).thenReturn(true);

        deviceService.updateDeviceOnlineStatus("SENSOR-001", true);

        verify(deviceRepository, times(1)).updateById(argThat(device ->
            device.isOnline() && device.getStatus() == EdgeDevice.DeviceStatus.ACTIVE
        ));
    }

    @Test
    @DisplayName("Should update device online status to offline")
    void testUpdateDeviceOnlineStatus_Offline() {
        testDevice.setOnline(true);
        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(deviceRepository.updateById(any(EdgeDevice.class))).thenReturn(true);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        deviceService.updateDeviceOnlineStatus("SENSOR-001", false);

        verify(deviceRepository, times(1)).updateById(argThat(device ->
            !device.isOnline() && device.getStatus() == EdgeDevice.DeviceStatus.INACTIVE
        ));
    }

    @Test
    @DisplayName("Should update heartbeat successfully")
    void testUpdateHeartbeat_Success() {
        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(deviceRepository.updateById(any(EdgeDevice.class))).thenReturn(true);

        assertDoesNotThrow(() -> deviceService.updateHeartbeat("SENSOR-001"));

        verify(deviceRepository, times(1)).updateById(any(EdgeDevice.class));
    }

    @Test
    @DisplayName("Should get device statistics successfully")
    void testGetDeviceStatistics_Success() {
        when(deviceRepository.selectCount(any())).thenReturn(10L);
        when(deviceRepository.countOnlineDevices()).thenReturn(5L);
        when(deviceRepository.countByStatus(EdgeDevice.DeviceStatus.ACTIVE)).thenReturn(4L);
        when(deviceRepository.countByStatus(EdgeDevice.DeviceStatus.INACTIVE)).thenReturn(5L);
        when(deviceRepository.countByStatus(EdgeDevice.DeviceStatus.MAINTENANCE)).thenReturn(1L);
        when(deviceRepository.countByStatus(EdgeDevice.DeviceStatus.FAULT)).thenReturn(0L);

        Map<String, Object> stats = deviceService.getDeviceStatistics();

        assertNotNull(stats);
        assertEquals(10L, stats.get("totalDevices"));
        assertEquals(5L, stats.get("onlineDevices"));
        assertEquals(5L, stats.get("offlineDevices"));
        assertEquals(4L, stats.get("activeDevices"));
    }

    @Test
    @DisplayName("Should update device status successfully")
    void testUpdateDeviceStatus_Success() {
        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(deviceRepository.updateById(any(EdgeDevice.class))).thenReturn(true);

        DeviceResponse response = deviceService.updateDeviceStatus(
                "SENSOR-001", EdgeDevice.DeviceStatus.MAINTENANCE);

        assertNotNull(response);
        verify(deviceRepository, times(1)).updateById(argThat(device ->
            device.getStatus() == EdgeDevice.DeviceStatus.MAINTENANCE
        ));
    }

    @Test
    @DisplayName("Should regenerate auth token successfully")
    void testRegenerateAuthToken_Success() {
        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(deviceRepository.updateById(any(EdgeDevice.class))).thenReturn(true);

        String newToken = deviceService.regenerateAuthToken("SENSOR-001");

        assertNotNull(newToken);
        assertNotEquals("test-token-123", newToken);
        verify(deviceRepository, times(1)).updateById(any(EdgeDevice.class));
    }

    @Test
    @DisplayName("Should add device to group successfully")
    void testAddDeviceToGroup_Success() {
        testDevice.setGroups(new ArrayList<>());
        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(deviceRepository.updateById(any(EdgeDevice.class))).thenReturn(true);

        DeviceResponse response = deviceService.addDeviceToGroup("SENSOR-001", "GROUP-001");

        assertNotNull(response);
        verify(deviceRepository, times(1)).updateById(argThat(device ->
            device.getGroups() != null && device.getGroups().contains("GROUP-001")
        ));
    }

    @Test
    @DisplayName("Should remove device from group successfully")
    void testRemoveDeviceFromGroup_Success() {
        testDevice.setGroups(new ArrayList<>(Arrays.asList("GROUP-001", "GROUP-002")));
        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(deviceRepository.updateById(any(EdgeDevice.class))).thenReturn(true);

        DeviceResponse response = deviceService.removeDeviceFromGroup("SENSOR-001", "GROUP-001");

        assertNotNull(response);
        verify(deviceRepository, times(1)).updateById(argThat(device ->
            device.getGroups() != null && !device.getGroups().contains("GROUP-001")
        ));
    }

    @Test
    @DisplayName("Should update device tags successfully")
    void testUpdateDeviceTags_Success() {
        Map<String, String> newTags = new HashMap<>();
        newTags.put("env", "production");
        newTags.put("region", "north");

        when(deviceRepository.findByDeviceId("SENSOR-001")).thenReturn(testDevice);
        when(deviceRepository.updateById(any(EdgeDevice.class))).thenReturn(true);

        DeviceResponse response = deviceService.updateDeviceTags("SENSOR-001", newTags);

        assertNotNull(response);
        verify(deviceRepository, times(1)).updateById(any(EdgeDevice.class));
    }

    @Test
    @DisplayName("Should get online devices successfully")
    void testGetOnlineDevices_Success() {
        testDevice.setOnline(true);
        List<EdgeDevice> onlineDevices = Collections.singletonList(testDevice);
        when(deviceRepository.findOnlineDevices()).thenReturn(onlineDevices);

        List<DeviceResponse> responses = deviceService.getOnlineDevices();

        assertEquals(1, responses.size());
        assertTrue(responses.get(0).isOnline());
    }
}
