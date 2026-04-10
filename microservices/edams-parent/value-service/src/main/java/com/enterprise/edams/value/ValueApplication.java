package com.enterprise.edams.value;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数据价值服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class ValueApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ValueApplication.class, args);
    }
}
