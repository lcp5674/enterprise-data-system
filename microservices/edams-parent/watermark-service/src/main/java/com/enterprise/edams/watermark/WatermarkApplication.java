package com.enterprise.edams.watermark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 水印服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class WatermarkApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(WatermarkApplication.class, args);
    }
}
