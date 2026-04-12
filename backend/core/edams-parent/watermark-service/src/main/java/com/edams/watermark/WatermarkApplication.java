package com.edams.watermark;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.edams.watermark.repository")
public class WatermarkApplication {
    public static void main(String[] args) {
        SpringApplication.run(WatermarkApplication.class, args);
    }
}
