package com.enterprise.dataplatform.ruleengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Drools规则引擎服务主应用类
 * 负责业务规则管理、规则执行与动态规则更新
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.dataplatform.ruleengine")
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class RuleEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuleEngineApplication.class, args);
    }
}
