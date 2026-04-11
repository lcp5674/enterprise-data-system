package com.enterprise.dataplatform.standard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数据标准服务主应用类
 * 负责数据标准的定义、映射、合规检查和执行管理
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.dataplatform.standard")
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class StandardServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StandardServiceApplication.class, args);
    }
}
