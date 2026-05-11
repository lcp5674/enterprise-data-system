package com.enterprise.dataplatform.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Data
@Configuration
@ConfigurationProperties(prefix = "iot.mqtt")
@Validated
public class MqttProperties {

    @NotBlank(message = "MQTT broker URL is required")
    private String brokerUrl = "tcp://localhost:1883";

    private String username;

    private String password;

    @Positive
    private int keepAliveInterval = 60;

    private int connectionTimeout = 30;

    private boolean cleanSession = false;

    private int qos = 1;

    private boolean autoReconnect = true;

    private int maxReconnectAttempts = 10;

    private int initialReconnectDelay = 1000;

    private int maxReconnectDelay = 30000;

    private String clientIdPrefix = "edams-iot-edge";

    private String deviceTopicPrefix = "edams/devices";

    private String commandTopic = "edams/commands";

    private String dataTopic = "edams/data";
}
