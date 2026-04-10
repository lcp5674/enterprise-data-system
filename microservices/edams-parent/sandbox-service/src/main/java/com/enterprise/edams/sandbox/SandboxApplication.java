package com.enterprise.edams.sandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 沙箱服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class SandboxApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SandboxApplication.class, args);
    }
}
