package com.enterprise.edams.model;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 数据模型服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.edams")
@EnableDiscoveryClient
public class ModelServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModelServiceApplication.class, args);
    }
}
