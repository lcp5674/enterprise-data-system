package com.enterprise.edams.llm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 大模型服务启动类
 * 提供大模型接入、配额管理和成本分析功能
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.enterprise.edams.llm", "com.enterprise.edams.common"})
@MapperScan("com.enterprise.edams.llm.repository")
@EnableScheduling
public class LlmApplication {

    public static void main(String[] args) {
        SpringApplication.run(LlmApplication.class, args);
    }
}
