package com.enterprise.dataplatform.iot.controller;

import com.enterprise.dataplatform.iot.dto.*;
import com.enterprise.dataplatform.iot.entity.EdgeDevice;
import com.enterprise.dataplatform.iot.service.AlertService;
import com.enterprise.dataplatform.iot.service.DeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
@DisplayName("DeviceController Unit Tests")
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceService deviceService;

    @MockBean
    private AlertService alertService;

    @Autowired
    private ObjectMapper objectMapper;

    private DeviceResponse testDeviceResponse;
    private EdgeDevice testDevice;

    @BeforeEach
    void setUp() {
        testDeviceResponse = DeviceResponse.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .deviceName("Temperature Sensor")
                .deviceType("SENSOR")
                .status(EdgeDevice.DeviceStatus.ACTIVE)
                .online(true)
                .createdAt(LocalDateTime.now())
                .build();

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
    @DisplayName("Should register device successfully")
    void testRegisterDevice() throws Exception {
        DeviceRegistrationRequest request = DeviceRegistrationRequest.builder()
                .deviceName("Temperature Sensor")
                .deviceType("SENSOR")
                .manufacturer("SensorCorp")
                .build();

        when(deviceService.registerDevice(any(DeviceRegistrationRequest.class)))
                .thenReturn(testDeviceResponse);

        mockMvc.perform(post("/api/v1/devices/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.deviceId").value("SENSOR-001"));

        verify(deviceService, times(1)).registerDevice(any(DeviceRegistrationRequest.class));
    }

    @Test
    @DisplayName("Should get device by ID successfully")
    void testGetDeviceById() throws Exception {
        when(deviceService.getDeviceById("SENSOR-001")).thenReturn(testDeviceResponse);

        mockMvc.perform(get("/api/v1/devices/SENSOR-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.deviceId").value("SENSOR-001"));

        verify(deviceService, times(1)).getDeviceById("SENSOR-001");
    }

    @Test
    @DisplayName("Should get all devices successfully")
    void testGetAllDevices() throws Exception {
        when(deviceService.getAllDevices()).thenReturn(List.of(testDeviceResponse));

        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(deviceService, times(1)).getAllDevices();
    }

    @Test
    @DisplayName("Should update device successfully")
    void testUpdateDevice() throws Exception {
        DeviceUpdateRequest request = DeviceUpdateRequest.builder()
                .deviceName("Updated Sensor")
                .build();

        DeviceResponse updatedResponse = DeviceResponse.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .deviceName("Updated Sensor")
                .deviceType("SENSOR")
                .status(EdgeDevice.DeviceStatus.ACTIVE)
                .online(true)
                .build();

        when(deviceService.updateDevice(eq("SENSOR-001"), any(DeviceUpdateRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/devices/SENSOR-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.deviceName").value("Updated Sensor"));

        verify(deviceService, times(1)).updateDevice(eq("SENSOR-001"), any(DeviceUpdateRequest.class));
    }

    @Test
    @DisplayName("Should delete device successfully")
    void testDeleteDevice() throws Exception {
        doNothing().when(deviceService).deleteDevice("SENSOR-001");

        mockMvc.perform(delete("/api/v1/devices/SENSOR-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(deviceService, times(1)).deleteDevice("SENSOR-001");
    }

    @Test
    @DisplayName("Should authenticate device successfully")
    void testAuthenticateDevice() throws Exception {
        DeviceAuthRequest request = DeviceAuthRequest.builder()
                .deviceId("SENSOR-001")
                .authToken("test-token")
                .build();

        when(deviceService.authenticateDevice(any(DeviceAuthRequest.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/devices/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(deviceService, times(1)).authenticateDevice(any(DeviceAuthRequest.class));
    }

    @Test
    @DisplayName("Should get device statistics successfully")
    void testGetDeviceStatistics() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDevices", 100L);
        stats.put("onlineDevices", 50L);
        stats.put("offlineDevices", 50L);

        when(deviceService.getDeviceStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/devices/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalDevices").value(100));

        verify(deviceService, times(1)).getDeviceStatistics();
    }

    @Test
    @DisplayName("Should update device status successfully")
    void testUpdateDeviceStatus() throws Exception {
        DeviceResponse updatedResponse = DeviceResponse.builder()
                .id("1")
                .deviceId("SENSOR-001")
                .status(EdgeDevice.DeviceStatus.MAINTENANCE)
                .build();

        when(deviceService.updateDeviceStatus("SENSOR-001", EdgeDevice.DeviceStatus.MAINTENANCE))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/devices/SENSOR-001/status")
                        .param("status", "MAINTENANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(deviceService, times(1)).updateDeviceStatus("SENSOR-001", EdgeDevice.DeviceStatus.MAINTENANCE);
    }

    @Test
    @DisplayName("Should regenerate auth token successfully")
    void testRegenerateAuthToken() throws Exception {
        when(deviceService.regenerateAuthToken("SENSOR-001")).thenReturn("new-token-123");

        mockMvc.perform(post("/api/v1/devices/SENSOR-001/regenerate-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("new-token-123"));

        verify(deviceService, times(1)).regenerateAuthToken("SENSOR-001");
    }

    @Test
    @DisplayName("Should get online devices successfully")
    void testGetOnlineDevices() throws Exception {
        when(deviceService.getOnlineDevices()).thenReturn(List.of(testDeviceResponse));

        mockMvc.perform(get("/api/v1/devices/online"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(deviceService, times(1)).getOnlineDevices();
    }

    @Test
    @DisplayName("Should get devices by group successfully")
    void testGetDevicesByGroup() throws Exception {
        when(deviceService.getDevicesByGroup("GROUP-001")).thenReturn(List.of(testDeviceResponse));

        mockMvc.perform(get("/api/v1/devices/group/GROUP-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(deviceService, times(1)).getDevicesByGroup("GROUP-001");
    }

    @Test
    @DisplayName("Should get devices by tag successfully")
    void testGetDevicesByTag() throws Exception {
        when(deviceService.getDevicesByTag("env", "production")).thenReturn(List.of(testDeviceResponse));

        mockMvc.perform(get("/api/v1/devices/tag")
                        .param("tagKey", "env")
                        .param("tagValue", "production"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(deviceService, times(1)).getDevicesByTag("env", "production");
    }

    @Test
    @DisplayName("Should add device to group successfully")
    void testAddDeviceToGroup() throws Exception {
        when(deviceService.addDeviceToGroup("SENSOR-001", "GROUP-001")).thenReturn(testDeviceResponse);

        mockMvc.perform(post("/api/v1/devices/SENSOR-001/group/GROUP-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(deviceService, times(1)).addDeviceToGroup("SENSOR-001", "GROUP-001");
    }

    @Test
    @DisplayName("Should remove device from group successfully")
    void testRemoveDeviceFromGroup() throws Exception {
        when(deviceService.removeDeviceFromGroup("SENSOR-001", "GROUP-001")).thenReturn(testDeviceResponse);

        mockMvc.perform(delete("/api/v1/devices/SENSOR-001/group/GROUP-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(deviceService, times(1)).removeDeviceFromGroup("SENSOR-001", "GROUP-001");
    }

    @Test
    @DisplayName("Should update device tags successfully")
    void testUpdateDeviceTags() throws Exception {
        Map<String, String> tags = Map.of("env", "production", "region", "north");
        when(deviceService.updateDeviceTags(eq("SENSOR-001"), anyMap())).thenReturn(testDeviceResponse);

        mockMvc.perform(put("/api/v1/devices/SENSOR-001/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tags)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(deviceService, times(1)).updateDeviceTags(eq("SENSOR-001"), anyMap());
    }
}
