package com.enterprise.dataplatform.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.enterprise.dataplatform")
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@EnableAsync
public class IotEdgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IotEdgeServiceApplication.class, args);
    }
}
