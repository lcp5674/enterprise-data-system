package com.enterprise.dataplatform.iot.service;

import com.enterprise.dataplatform.iot.config.MqttProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService Unit Tests")
class AlertServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private MqttProperties mqttProperties;

    @InjectMocks
    private AlertService alertService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Should send alert successfully")
    void testSendAlert_Success() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        alertService.sendAlert("SENSOR-001", AlertService.AlertLevel.WARNING, "Test alert message");

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());

        assertEquals("iot.alerts", topicCaptor.getValue());
        assertEquals("SENSOR-001", keyCaptor.getValue());
        assertTrue(messageCaptor.getValue().contains("SENSOR-001"));
        assertTrue(messageCaptor.getValue().contains("WARNING"));
    }

    @Test
    @DisplayName("Should send alert with type successfully")
    void testSendAlert_WithType_Success() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        alertService.sendAlert("SENSOR-001", AlertService.AlertType.DEVICE_OFFLINE,
                AlertService.AlertLevel.ERROR, "Device offline alert");

        verify(kafkaTemplate, times(1)).send(eq("iot.alerts"), eq("SENSOR-001"), anyString());
    }

    @Test
    @DisplayName("Should handle Kafka send failure gracefully")
    void testSendAlert_KafkaFailure() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka error")));

        assertDoesNotThrow(() ->
                alertService.sendAlert("SENSOR-001", AlertService.AlertLevel.WARNING, "Test alert"));
    }

    @Test
    @DisplayName("Should register alert rule successfully")
    void testRegisterAlertRule_Success() {
        AlertService.AlertRule rule = new AlertService.AlertRule(
                "RULE-001",
                "SENSOR-.*",
                List.of(AlertService.AlertLevel.WARNING, AlertService.AlertLevel.ERROR),
                List.of(AlertService.AlertType.DEVICE_OFFLINE),
                5
        );

        alertService.registerAlertRule("RULE-001", rule);

        assertDoesNotThrow(() -> alertService.registerAlertRule("RULE-001", rule));
    }

    @Test
    @DisplayName("Should unregister alert rule successfully")
    void testUnregisterAlertRule_Success() {
        AlertService.AlertRule rule = new AlertService.AlertRule(
                "RULE-001",
                "SENSOR-.*",
                null,
                null,
                5
        );
        alertService.registerAlertRule("RULE-001", rule);

        alertService.unregisterAlertRule("RULE-001");

        assertDoesNotThrow(() -> alertService.unregisterAlertRule("RULE-001"));
    }

    @Test
    @DisplayName("Should suppress alert when rule matches")
    void testAlertRule_Matches() {
        AlertService.AlertRule rule = new AlertService.AlertRule(
                "RULE-001",
                "SENSOR-.*",
                List.of(AlertService.AlertLevel.WARNING),
                List.of(AlertService.AlertType.DEVICE_OFFLINE),
                5
        );

        assertTrue(rule.matches(createTestAlert("SENSOR-001", AlertService.AlertLevel.WARNING,
                AlertService.AlertType.DEVICE_OFFLINE)));
    }

    @Test
    @DisplayName("Alert rule should not match different device pattern")
    void testAlertRule_NoMatch_DevicePattern() {
        AlertService.AlertRule rule = new AlertService.AlertRule(
                "RULE-001",
                "SENSOR-.*",
                null,
                null,
                5
        );

        assertFalse(rule.matches(createTestAlert("ACTUATOR-001", AlertService.AlertLevel.WARNING,
                AlertService.AlertType.DEVICE_OFFLINE)));
    }

    @Test
    @DisplayName("Alert rule should not match different level")
    void testAlertRule_NoMatch_Level() {
        AlertService.AlertRule rule = new AlertService.AlertRule(
                "RULE-001",
                null,
                List.of(AlertService.AlertLevel.ERROR),
                null,
                5
        );

        assertFalse(rule.matches(createTestAlert("SENSOR-001", AlertService.AlertLevel.WARNING,
                AlertService.AlertType.DEVICE_OFFLINE)));
    }

    @Test
    @DisplayName("Alert rule should not match different type")
    void testAlertRule_NoMatch_Type() {
        AlertService.AlertRule rule = new AlertService.AlertRule(
                "RULE-001",
                null,
                null,
                List.of(AlertService.AlertType.CONNECTION_FAILURE),
                5
        );

        assertFalse(rule.matches(createTestAlert("SENSOR-001", AlertService.AlertLevel.WARNING,
                AlertService.AlertType.DEVICE_OFFLINE)));
    }

    @Test
    @DisplayName("Alert rule should match when all null conditions")
    void testAlertRule_MatchAllNull() {
        AlertService.AlertRule rule = new AlertService.AlertRule(
                "RULE-001",
                null,
                null,
                null,
                5
        );

        assertTrue(rule.matches(createTestAlert("SENSOR-001", AlertService.AlertLevel.WARNING,
                AlertService.AlertType.DEVICE_OFFLINE)));
    }

    @Test
    @DisplayName("Should send critical alert and call handleCriticalAlert")
    void testSendAlert_CriticalLevel() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        alertService.sendAlert("SENSOR-001", AlertService.AlertType.CONNECTION_FAILURE,
                AlertService.AlertLevel.CRITICAL, "Critical connection failure");

        verify(kafkaTemplate, times(1)).send(eq("iot.alerts"), eq("SENSOR-001"), anyString());
    }

    private AlertService.Alert createTestAlert(String deviceId, AlertService.AlertLevel level,
                                                 AlertService.AlertType type) {
        return AlertService.Alert.builder()
                .id("TEST-ALERT-001")
                .deviceId(deviceId)
                .level(level)
                .type(type)
                .message("Test alert")
                .timestamp(java.time.LocalDateTime.now())
                .acknowledged(false)
                .build();
    }
}
