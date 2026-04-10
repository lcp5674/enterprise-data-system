package com.enterprise.edams.datamap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 数据地图服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.edams")
@EnableDiscoveryClient
public class DatamapServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatamapServiceApplication.class, args);
    }
}
