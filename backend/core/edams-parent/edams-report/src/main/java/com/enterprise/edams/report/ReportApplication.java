package com.enterprise.edams.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import springfox.documentation.oas.annotations.EnableOpenApi;

/**
 * 报表服务启动类
 * 
 * 功能：
 * - 报表模板管理
 * - 报表生成（PDF、Excel、Word）
 * - 报表分发（邮件、企业微信、钉钉）
 * - 报表订阅
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@EnableOpenApi
@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka
@SpringBootApplication
@MapperScan("com.enterprise.edams.report.mapper")
@ComponentScan(basePackages = {"com.enterprise.edams.report", "com.enterprise.edams.common"})
public class ReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportApplication.class, args);
    }
}
