package com.enterprise.dataplatform.governance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数据治理引擎主应用类
 * 负责治理策略管理、任务编排和AI智能推荐
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.dataplatform.governance")
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class GovernanceEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovernanceEngineApplication.class, args);
    }
}
