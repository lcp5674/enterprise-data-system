package com.enterprise.dataplatform.iot.service;

import com.enterprise.dataplatform.iot.config.MqttConfig;
import com.enterprise.dataplatform.iot.config.MqttProperties;
import com.enterprise.dataplatform.iot.entity.DeviceData;
import com.enterprise.dataplatform.iot.entity.EdgeDevice;
import com.enterprise.dataplatform.iot.repository.DeviceDataRepository;
import com.enterprise.dataplatform.iot.repository.DeviceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttMessageService {

    private final MqttConfig mqttConfig;
    private final MqttProperties mqttProperties;
    private final DeviceDataRepository deviceDataRepository;
    private final DeviceRepository deviceRepository;
    private final ObjectMapper objectMapper;

    private final Map<String, MessageHandler> topicHandlers = new ConcurrentHashMap<>();
    private final Queue<MqttMessage> messageQueue = new LinkedList<>();
    private final Object queueLock = new Object();

    public void initializeSubscriptions() {
        String wildcardTopic = mqttProperties.getDataTopic() + "/#";
        mqttConfig.subscribe(wildcardTopic, mqttProperties.getQos(), this::processIncomingMessage);
        log.info("Initialized MQTT subscriptions for topic pattern: {}", wildcardTopic);
    }

    public void publishData(String deviceId, Object data) {
        String topic = mqttProperties.getDataTopic() + "/" + deviceId;
        publishData(deviceId, topic, data, mqttProperties.getQos(), false);
    }

    public void publishData(String deviceId, String topic, Object data, int qos, boolean retained) {
        try {
            String payload;
            if (data instanceof String) {
                payload = (String) data;
            } else {
                payload = objectMapper.writeValueAsString(data);
            }
            mqttConfig.publish(topic, payload, qos, retained);
            log.debug("Published data to topic: {} with QoS: {}", topic, qos);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for device: {}", deviceId, e);
            throw new RuntimeException("Failed to serialize message", e);
        }
    }

    public void publishCommand(String deviceId, String command, Map<String, Object> params) {
        String topic = mqttProperties.getCommandTopic() + "/" + deviceId;
        Map<String, Object> message = new HashMap<>();
        message.put("command", command);
        message.put("params", params);
        message.put("timestamp", LocalDateTime.now().toString());
        message.put("messageId", UUID.randomUUID().toString());

        publishData(deviceId, topic, message, mqttProperties.getQos(), false);
        log.info("Published command to device: {}, command: {}", deviceId, command);
    }

    public void subscribeToTopic(String topic, int qos, MessageHandler handler) {
        topicHandlers.put(topic, handler);
        mqttConfig.subscribe(topic, qos, (t, payload, q) -> {
            if (handler != null) {
                handler.handle(topic, payload, q);
            }
        });
        log.info("Subscribed to topic: {} with QoS: {}", topic, qos);
    }

    public void unsubscribeFromTopic(String topic) {
        MessageHandler handler = topicHandlers.remove(topic);
        if (handler != null) {
            mqttConfig.unsubscribe(topic, handler);
        }
        log.info("Unsubscribed from topic: {}", topic);
    }

    private void processIncomingMessage(String topic, byte[] payload, int qos) {
        try {
            String messageBody = new String(payload, StandardCharsets.UTF_8);
            log.debug("Processing message from topic: {} with QoS: {}", topic, qos);

            String[] topicParts = topic.split("/");
            if (topicParts.length < 2) {
                log.warn("Invalid topic format: {}", topic);
                return;
            }

            String deviceId = topicParts[2];
            Optional<EdgeDevice> deviceOpt = Optional.ofNullable(deviceRepository.findByDeviceId(deviceId));

            if (deviceOpt.isPresent()) {
                storeDeviceData(deviceId, topic, messageBody, qos);
            } else {
                log.warn("Received message from unknown device: {}", deviceId);
            }

            notifyHandlers(topic, payload, qos);

        } catch (Exception e) {
            log.error("Error processing incoming MQTT message from topic: {}", topic, e);
        }
    }

    @Async
    protected void storeDeviceData(String deviceId, String topic, String payload, int qos) {
        try {
            DeviceData data = DeviceData.builder()
                    .deviceId(deviceId)
                    .dataKey(extractDataKey(topic))
                    .dataValue(payload)
                    .timestamp(LocalDateTime.now())
                    .quality(DeviceData.DataQuality.GOOD)
                    .syncStatus(DeviceData.SyncStatus.PENDING)
                    .compressed(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            deviceDataRepository.save(data);
            log.debug("Stored device data for device: {}", deviceId);
        } catch (Exception e) {
            log.error("Failed to store device data for device: {}", deviceId, e);
        }
    }

    private void notifyHandlers(String topic, byte[] payload, int qos) {
        topicHandlers.entrySet().stream()
                .filter(entry -> topicMatches(entry.getKey(), topic))
                .forEach(entry -> {
                    try {
                        entry.getValue().handle(topic, payload, qos);
                    } catch (Exception e) {
                        log.error("Error in message handler for topic: {}", topic, e);
                    }
                });
    }

    private boolean topicMatches(String pattern, String topic) {
        String[] patternParts = pattern.split("/");
        String[] topicParts = topic.split("/");

        if (patternParts.length != topicParts.length) {
            return false;
        }

        for (int i = 0; i < patternParts.length; i++) {
            if (!patternParts[i].equals("#") && !patternParts[i].equals("+") &&
                    !patternParts[i].equals(topicParts[i])) {
                return false;
            }
        }
        return true;
    }

    private String extractDataKey(String topic) {
        String[] parts = topic.split("/");
        if (parts.length > 3) {
            return parts[3];
        }
        return "default";
    }

    public List<DeviceData> getPendingData(int limit) {
        return deviceDataRepository.findBySyncStatus(DeviceData.SyncStatus.PENDING)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void markDataAsSynced(List<String> dataIds) {
        for (String dataId : dataIds) {
            Optional<DeviceData> dataOpt = deviceDataRepository.findById(dataId);
            if (dataOpt.isPresent()) {
                DeviceData data = dataOpt.get();
                data.setSyncStatus(DeviceData.SyncStatus.SYNCED);
                data.setSyncTime(LocalDateTime.now());
                deviceDataRepository.save(data);
            }
        }
    }

    public void markDataAsFailed(List<String> dataIds) {
        for (String dataId : dataIds) {
            Optional<DeviceData> dataOpt = deviceDataRepository.findById(dataId);
            if (dataOpt.isPresent()) {
                DeviceData data = dataOpt.get();
                data.setSyncStatus(DeviceData.SyncStatus.FAILED);
                deviceDataRepository.save(data);
            }
        }
    }

    public boolean isConnected() {
        return mqttConfig.isConnected();
    }

    public int getQueuedMessageCount() {
        synchronized (queueLock) {
            return messageQueue.size();
        }
    }

    public interface MessageHandler {
        void handle(String topic, byte[] payload, int qos);
    }
}
