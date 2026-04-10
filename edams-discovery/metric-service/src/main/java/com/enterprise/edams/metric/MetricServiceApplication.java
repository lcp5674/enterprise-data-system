package com.enterprise.edams.metric;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 指标管理服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.edams")
@EnableDiscoveryClient
public class MetricServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetricServiceApplication.class, args);
    }
}
