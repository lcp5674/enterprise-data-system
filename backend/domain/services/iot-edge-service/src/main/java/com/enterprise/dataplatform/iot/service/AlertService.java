package com.enterprise.dataplatform.iot.service;

import com.enterprise.dataplatform.iot.config.MqttProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MqttProperties mqttProperties;

    private static final String ALERT_TOPIC = "iot.alerts";
    private static final Map<String, AlertRule> alertRules = new ConcurrentHashMap<>();

    public enum AlertLevel {
        INFO, WARNING, ERROR, CRITICAL
    }

    public enum AlertType {
        DEVICE_OFFLINE,
        DATA_THRESHOLD_VIOLATION,
        CONNECTION_FAILURE,
        SYNC_FAILURE,
        AUTH_FAILURE,
        MAINTENANCE_REQUIRED,
        UNEXPECTED_BEHAVIOR
    }

    @Async
    public void sendAlert(String deviceId, AlertLevel level, String message) {
        Alert alert = Alert.builder()
                .id(generateAlertId())
                .deviceId(deviceId)
                .level(level)
                .type(AlertType.UNEXPECTED_BEHAVIOR)
                .message(message)
                .timestamp(LocalDateTime.now())
                .acknowledged(false)
                .build();

        processAlert(alert);
    }

    @Async
    public void sendAlert(String deviceId, AlertType type, AlertLevel level, String message) {
        Alert alert = Alert.builder()
                .id(generateAlertId())
                .deviceId(deviceId)
                .level(level)
                .type(type)
                .message(message)
                .timestamp(LocalDateTime.now())
                .acknowledged(false)
                .build();

        processAlert(alert);
    }

    public void registerAlertRule(String ruleId, AlertRule rule) {
        alertRules.put(ruleId, rule);
        log.info("Alert rule registered: {}", ruleId);
    }

    public void unregisterAlertRule(String ruleId) {
        alertRules.remove(ruleId);
        log.info("Alert rule unregistered: {}", ruleId);
    }

    private void processAlert(Alert alert) {
        if (shouldSuppressAlert(alert)) {
            log.debug("Alert suppressed by rule: {}", alert.getId());
            return;
        }

        try {
            String message = buildAlertMessage(alert);

            kafkaTemplate.send(ALERT_TOPIC, alert.getDeviceId(), message);

            log.warn("Alert sent - Device: {}, Level: {}, Type: {}, Message: {}",
                    alert.getDeviceId(), alert.getLevel(), alert.getType(), alert.getMessage());

            if (alert.getLevel() == AlertLevel.CRITICAL) {
                handleCriticalAlert(alert);
            }

        } catch (Exception e) {
            log.error("Failed to send alert: {}", alert.getId(), e);
        }
    }

    private boolean shouldSuppressAlert(Alert alert) {
        for (AlertRule rule : alertRules.values()) {
            if (rule.matches(alert)) {
                return true;
            }
        }
        return false;
    }

    private String buildAlertMessage(Alert alert) {
        return String.format(
                "{\"alertId\":\"%s\",\"deviceId\":\"%s\",\"level\":\"%s\",\"type\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                alert.getId(),
                alert.getDeviceId(),
                alert.getLevel(),
                alert.getType(),
                alert.getMessage(),
                alert.getTimestamp()
        );
    }

    private void handleCriticalAlert(Alert alert) {
        log.error("CRITICAL ALERT for device {}: {}", alert.getDeviceId(), alert.getMessage());
    }

    private String generateAlertId() {
        return "ALERT-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }

    @Getter
    @lombok.Builder
    @lombok.AllArgsConstructor
    public static class Alert {
        private final String id;
        private final String deviceId;
        private final AlertLevel level;
        private final AlertType type;
        private final String message;
        private final LocalDateTime timestamp;
        private boolean acknowledged;
        private String acknowledgedBy;
        private LocalDateTime acknowledgedAt;
    }

    @Getter
    @lombok.AllArgsConstructor
    public static class AlertRule {
        private final String ruleId;
        private final String deviceIdPattern;
        private final List<AlertLevel> levels;
        private final List<AlertType> types;
        private final int suppressMinutes;

        public boolean matches(Alert alert) {
            if (deviceIdPattern != null && !alert.getDeviceId().matches(deviceIdPattern)) {
                return false;
            }
            if (levels != null && !levels.contains(alert.getLevel())) {
                return false;
            }
            if (types != null && !types.contains(alert.getType())) {
                return false;
            }
            return true;
        }
    }
}
