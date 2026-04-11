package com.enterprise.dataplatform.index;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Index Service Application - 全文搜索服务主类
 *
 * @author Team-D
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.dataplatform")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.enterprise.dataplatform.index.feign")
@EnableAsync
public class IndexServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndexServiceApplication.class, args);
    }
}
