package com.enterprise.edams.aiops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 智能运维服务启动类
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.enterprise.edams.aiops", "com.enterprise.edams.common"})
@EnableDiscoveryClient
public class AIOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIOpsApplication.class, args);
    }
}
