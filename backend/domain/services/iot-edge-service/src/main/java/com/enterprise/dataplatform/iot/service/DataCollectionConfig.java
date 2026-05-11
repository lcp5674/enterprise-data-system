package com.enterprise.dataplatform.iot.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "iot.data-collection")
public class DataCollectionConfig {

    private int batchSize = 100;
    private long flushIntervalMs = 5000;
    private int maxRetries = 3;
    private int retentionDays = 30;
}
