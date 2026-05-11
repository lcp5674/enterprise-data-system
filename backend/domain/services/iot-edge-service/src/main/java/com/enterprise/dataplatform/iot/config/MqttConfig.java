package com.enterprise.dataplatform.iot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MqttConfig implements MqttCallback {

    private final MqttProperties mqttProperties;
    private final Map<String, CopyOnWriteArrayList<MqttMessageListener>> topicListeners = new ConcurrentHashMap<>();
    private MqttAsyncClient mqttClient;
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private volatile boolean connected = false;

    @Bean
    public MqttAsyncClient mqttClient() {
        try {
            String clientId = mqttProperties.getClientIdPrefix() + "-" + System.currentTimeMillis();
            mqttClient = new MqttAsyncClient(
                    mqttProperties.getBrokerUrl(),
                    clientId,
                    new MemoryPersistence()
            );
            mqttClient.setCallback(this);
            connect();
            return mqttClient;
        } catch (MqttException e) {
            log.error("Failed to create MQTT client", e);
            throw new RuntimeException("Failed to create MQTT client", e);
        }
    }

    public void connect() {
        try {
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setServerURIs(new String[]{mqttProperties.getBrokerUrl()});
            options.setKeepAliveInterval(mqttProperties.getKeepAliveInterval());
            options.setConnectionTimeout(mqttProperties.getConnectionTimeout());
            options.setCleanSession(mqttProperties.isCleanSession());
            options.setAutomaticReconnect(mqttProperties.isAutoReconnect());

            if (mqttProperties.getUsername() != null && !mqttProperties.getUsername().isEmpty()) {
                options.setUserName(mqttProperties.getUsername());
                options.setPassword(mqttProperties.getPassword() != null ?
                        mqttProperties.getPassword().getBytes() : null);
            }

            mqttClient.connect(options);
            connected = true;
            reconnectAttempts.set(0);
            log.info("MQTT client connected to {}", mqttProperties.getBrokerUrl());
        } catch (MqttException e) {
            log.error("Failed to connect to MQTT broker", e);
            scheduleReconnect();
        }
    }

    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                connected = false;
                log.info("MQTT client disconnected");
            }
        } catch (MqttException e) {
            log.error("Failed to disconnect MQTT client", e);
        }
    }

    public void subscribe(String topic, int qos, MqttMessageListener listener) {
        try {
            topicListeners.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(listener);

            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.subscribe(topic, qos);
                log.info("Subscribed to topic: {} with QoS: {}", topic, qos);
            }
        } catch (MqttException e) {
            log.error("Failed to subscribe to topic: {}", topic, e);
            throw new RuntimeException("Failed to subscribe to topic: " + topic, e);
        }
    }

    public void unsubscribe(String topic, MqttMessageListener listener) {
        try {
            CopyOnWriteArrayList<MqttMessageListener> listeners = topicListeners.get(topic);
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    topicListeners.remove(topic);
                    if (mqttClient != null && mqttClient.isConnected()) {
                        mqttClient.unsubscribe(topic);
                        log.info("Unsubscribed from topic: {}", topic);
                    }
                }
            }
        } catch (MqttException e) {
            log.error("Failed to unsubscribe from topic: {}", topic, e);
        }
    }

    public void publish(String topic, String payload, int qos, boolean retained) {
        try {
            if (mqttClient == null || !mqttClient.isConnected()) {
                log.warn("MQTT client not connected, message will not be published");
                return;
            }

            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            message.setRetained(retained);

            mqttClient.publish(topic, message);
            log.debug("Published message to topic: {} with QoS: {}", topic, qos);
        } catch (MqttException e) {
            log.error("Failed to publish message to topic: {}", topic, e);
            throw new RuntimeException("Failed to publish message to topic: " + topic, e);
        }
    }

    public void publish(String topic, byte[] payload, int qos, boolean retained) {
        try {
            if (mqttClient == null || !mqttClient.isConnected()) {
                log.warn("MQTT client not connected, message will not be published");
                return;
            }

            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);
            message.setRetained(retained);

            mqttClient.publish(topic, message);
            log.debug("Published binary message to topic: {} with QoS: {}", topic, qos);
        } catch (MqttException e) {
            log.error("Failed to publish binary message to topic: {}", topic, e);
            throw new RuntimeException("Failed to publish binary message to topic: " + topic, e);
        }
    }

    private void scheduleReconnect() {
        int attempts = reconnectAttempts.incrementAndGet();
        if (attempts > mqttProperties.getMaxReconnectAttempts()) {
            log.error("Max reconnect attempts reached, giving up");
            return;
        }

        long delay = calculateExponentialBackoff(attempts);
        log.info("Scheduling MQTT reconnect attempt {} in {} ms", attempts, delay);

        new Thread(() -> {
            try {
                Thread.sleep(delay);
                connect();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Reconnect interrupted", e);
            }
        }).start();
    }

    private long calculateExponentialBackoff(int attempt) {
        long delay = (long) (mqttProperties.getInitialReconnectDelay() * Math.pow(2, attempt - 1));
        return Math.min(delay, mqttProperties.getMaxReconnectDelay());
    }

    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        connected = false;
        log.warn("MQTT disconnected: {}", disconnectResponse.getReasonString());
        if (mqttProperties.isAutoReconnect()) {
            scheduleReconnect();
        }
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {
        log.error("MQTT error occurred: {}", exception.getMessage(), exception);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        log.debug("Message arrived on topic: {}", topic);
        CopyOnWriteArrayList<MqttMessageListener> listeners = topicListeners.get(topic);
        if (listeners != null) {
            for (MqttMessageListener listener : listeners) {
                try {
                    listener.onMessage(topic, message.getPayload(), message.getQos());
                } catch (Exception e) {
                    log.error("Error processing message from topic: {}", topic, e);
                }
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttToken deliveryToken) {
        log.debug("Message delivery complete for token: {}", deliveryToken.getMessageId());
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        connected = true;
        log.info("MQTT connection complete, reconnect: {}, server: {}", reconnect, serverURI);

        for (Map.Entry<String, CopyOnWriteArrayList<MqttMessageListener>> entry : topicListeners.entrySet()) {
            try {
                mqttClient.subscribe(entry.getKey(), mqttProperties.getQos());
                log.info("Resubscribed to topic: {}", entry.getKey());
            } catch (MqttException e) {
                log.error("Failed to resubscribe to topic: {}", entry.getKey(), e);
            }
        }
    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {
        log.debug("Auth packet arrived with reason code: {}", reasonCode);
    }

    public boolean isConnected() {
        return connected && mqttClient != null && mqttClient.isConnected();
    }

    @PreDestroy
    public void cleanup() {
        disconnect();
        try {
            if (mqttClient != null) {
                mqttClient.close();
            }
        } catch (MqttException e) {
            log.error("Failed to close MQTT client", e);
        }
    }

    public interface MqttMessageListener {
        void onMessage(String topic, byte[] payload, int qos);
    }
}
