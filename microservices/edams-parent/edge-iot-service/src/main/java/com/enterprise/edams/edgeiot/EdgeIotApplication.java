package com.enterprise.edams.edgeiot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 边缘计算与IoT数据管理服务启动类
 * 
 * @author Advanced Backend Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableScheduling
public class EdgeIotApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(EdgeIotApplication.class, args);
    }
}
