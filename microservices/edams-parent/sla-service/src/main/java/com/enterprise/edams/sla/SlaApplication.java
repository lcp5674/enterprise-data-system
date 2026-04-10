package com.enterprise.edams.sla;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SLA监控服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class SlaApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SlaApplication.class, args);
    }
}
