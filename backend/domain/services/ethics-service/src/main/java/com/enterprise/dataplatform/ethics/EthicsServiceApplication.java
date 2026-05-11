package com.enterprise.dataplatform.ethics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.enterprise.dataplatform.ethics")
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class EthicsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EthicsServiceApplication.class, args);
    }
}
