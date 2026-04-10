package com.enterprise.edams.insight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 智能洞察服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.edams")
@EnableDiscoveryClient
public class InsightServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsightServiceApplication.class, args);
    }
}
