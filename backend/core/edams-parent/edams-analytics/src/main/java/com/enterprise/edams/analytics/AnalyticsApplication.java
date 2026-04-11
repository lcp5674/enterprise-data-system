package com.enterprise.edams.analytics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import springfox.documentation.oas.annotations.EnableOpenApi;

/**
 * 数据分析服务启动类
 * 
 * 功能：
 * - Ad-hoc查询：支持自定义SQL的数据分析
 * - 自助分析：提供可视化的分析工具
 * - 资产价值分析：评估数据资产的商业价值
 * - 分析报表：生成分析报告
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@EnableOpenApi
@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka
@EnableCaching
@SpringBootApplication
@MapperScan("com.enterprise.edams.analytics.mapper")
@ComponentScan(basePackages = {"com.enterprise.edams.analytics", "com.enterprise.edams.common"})
public class AnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsApplication.class, args);
    }
}
