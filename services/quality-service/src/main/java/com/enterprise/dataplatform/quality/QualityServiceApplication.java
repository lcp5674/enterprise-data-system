package com.enterprise.dataplatform.quality;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 数据质量管理服务主应用类
 * 负责数据质量规则、任务调度、评估模型和报告生成
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.dataplatform.quality")
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class QualityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QualityServiceApplication.class, args);
    }
}
