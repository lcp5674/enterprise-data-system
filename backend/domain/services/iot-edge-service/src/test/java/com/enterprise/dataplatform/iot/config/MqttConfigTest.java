package com.enterprise.dataplatform.iot.config;

import org.eclipse.paho.mqttv5.client.MqttActionListener;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.ByteArrayPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MqttConfig Unit Tests")
class MqttConfigTest {

    @Mock
    private MqttProperties mqttProperties;

    @Mock
    private MqttAsyncClient mqttAsyncClient;

    private MqttConfig mqttConfig;

    @BeforeEach
    void setUp() throws Exception {
        when(mqttProperties.getBrokerUrl()).thenReturn("tcp://localhost:1883");
        when(mqttProperties.getClientId()).thenReturn("test-client");
        when(mqttProperties.getKeepAliveInterval()).thenReturn(60);
        when(mqttProperties.getConnectionTimeout()).thenReturn(30);
        when(mqttProperties.isCleanSession()).thenReturn(false);
        when(mqttProperties.getAutomaticReconnect()).thenReturn(true);
        when(mqttProperties.getQos()).thenReturn(1);

        mqttConfig = new MqttConfig(mqttProperties);

        Field clientField = MqttConfig.class.getDeclaredField("mqttClient");
        clientField.setAccessible(true);
        clientField.set(mqttConfig, mqttAsyncClient);

        Field listenerMapField = MqttConfig.class.getDeclaredField("messageListeners");
        listenerMapField.setAccessible(true);
        listenerMapField.set(mqttConfig, new HashMap<>());
    }

    @Test
    @DisplayName("Should publish message successfully")
    void testPublish_Success() throws Exception {
        String topic = "test/topic";
        String payload = "test message";
        int qos = 1;
        boolean retained = false;

        doNothing().when(mqttAsyncClient).publish(
                eq(topic), any(org.eclipse.paho.mqttv5.common.MqttMessage.class),
                any(MqttActionListener.class), any(MqttActionListener.class));

        assertDoesNotThrow(() -> mqttConfig.publish(topic, payload, qos, retained));

        verify(mqttAsyncClient, times(1)).publish(
                eq(topic), any(org.eclipse.paho.mqttv5.common.MqttMessage.class),
                any(MqttActionListener.class), any(MqttActionListener.class));
    }

    @Test
    @DisplayName("Should handle publish failure")
    void testPublish_Failure() throws Exception {
        String topic = "test/topic";
        String payload = "test message";

        doThrow(new MqttException(new Exception("Connection lost")))
                .when(mqttAsyncClient).publish(
                        eq(topic), any(org.eclipse.paho.mqttv5.common.MqttMessage.class),
                        any(MqttActionListener.class), any(MqttActionListener.class));

        assertThrows(Exception.class, () ->
                mqttConfig.publish(topic, payload, 1, false));
    }

    @Test
    @DisplayName("Should subscribe to topic successfully")
    void testSubscribe_Success() throws Exception {
        String topic = "test/topic/#";
        int qos = 1;

        doNothing().when(mqttAsyncClient).subscribe(
                eq(topic), eq(qos), any(MqttActionListener.class), any(MqttActionListener.class));

        @SuppressWarnings("unchecked")
        BiConsumer<String, byte[], Integer> callback = mock(BiConsumer.class);

        assertDoesNotThrow(() -> mqttConfig.subscribe(topic, qos, callback));

        verify(mqttAsyncClient, times(1)).subscribe(eq(topic), eq(qos),
                any(MqttActionListener.class), any(MqttActionListener.class));
    }

    @Test
    @DisplayName("Should handle subscribe failure")
    void testSubscribe_Failure() throws Exception {
        String topic = "test/topic";
        int qos = 1;

        doThrow(new MqttException(new Exception("Subscribe failed")))
                .when(mqttAsyncClient).subscribe(
                        eq(topic), eq(qos), any(MqttActionListener.class), any(MqttActionListener.class));

        @SuppressWarnings("unchecked")
        BiConsumer<String, byte[], Integer> callback = mock(BiConsumer.class);

        assertThrows(Exception.class, () ->
                mqttConfig.subscribe(topic, qos, callback));
    }

    @Test
    @DisplayName("Should unsubscribe from topic successfully")
    void testUnsubscribe_Success() throws Exception {
        String topic = "test/topic";

        doNothing().when(mqttAsyncClient).unsubscribe(
                eq(topic), any(MqttActionListener.class), any(MqttActionListener.class));

        assertDoesNotThrow(() -> mqttConfig.unsubscribe(topic));

        verify(mqttAsyncClient, times(1)).unsubscribe(eq(topic),
                any(MqttActionListener.class), any(MqttActionListener.class));
    }

    @Test
    @DisplayName("Should check connection status correctly")
    void testIsConnected() throws Exception {
        when(mqttAsyncClient.isConnected()).thenReturn(true);
        assertTrue(mqttConfig.isConnected());

        when(mqttAsyncClient.isConnected()).thenReturn(false);
        assertFalse(mqttConfig.isConnected());
    }

    @Test
    @DisplayName("Should calculate exponential backoff correctly")
    void testCalculateExponentialBackoff() throws Exception {
        when(mqttProperties.getInitialReconnectDelay()).thenReturn(1000L);
        when(mqttProperties.getMaxReconnectDelay()).thenReturn(30000L);

        MqttConfig freshConfig = new MqttConfig(mqttProperties);

        Field initialDelayField = MqttConfig.class.getDeclaredField("initialReconnectDelay");
        initialDelayField.setAccessible(true);
        initialDelayField.set(freshConfig, 1000L);

        Field maxDelayField = MqttConfig.class.getDeclaredField("maxReconnectDelay");
        maxDelayField.setAccessible(true);
        maxDelayField.set(freshConfig, 30000L);

        assertEquals(1000L, freshConfig.calculateExponentialBackoff(1));
        assertEquals(2000L, freshConfig.calculateExponentialBackoff(2));
        assertEquals(4000L, freshConfig.calculateExponentialBackoff(3));
        assertEquals(8000L, freshConfig.calculateExponentialBackoff(4));
        assertEquals(16000L, freshConfig.calculateExponentialBackoff(5));
        assertEquals(30000L, freshConfig.calculateExponentialBackoff(6));
        assertEquals(30000L, freshConfig.calculateExponentialBackoff(10));
    }

    @Test
    @DisplayName("Should disconnect successfully")
    void testDisconnect_Success() throws Exception {
        doNothing().when(mqttAsyncClient).disconnect(any(MqttActionListener.class));

        assertDoesNotThrow(() -> mqttConfig.disconnect());

        verify(mqttAsyncClient, times(1)).disconnect(any(MqttActionListener.class));
    }

    @Test
    @DisplayName("Should return queued message count")
    void testGetQueuedMessageCount() throws Exception {
        Field listenerMapField = MqttConfig.class.getDeclaredField("messageListeners");
        listenerMapField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, BiConsumer<String, byte[], Integer>> listeners =
                (Map<String, BiConsumer<String, byte[], Integer>>) listenerMapField.get(mqttConfig);
        listeners.put("topic1", mock(BiConsumer.class));
        listeners.put("topic2", mock(BiConsumer.class));

        assertEquals(2, mqttConfig.getQueuedMessageCount());
    }
}
