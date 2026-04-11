package com.enterprise.dataplatform.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ClickHouse OLAP Analytics Service Main Application
 * Provides data analytics capabilities for EDAMS including:
 * - Asset access heatmaps and trending
 * - Quality trend analysis
 * - User behavior analysis
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.dataplatform.analytics")
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableScheduling
@EnableKafka
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
