package com.enterprise.edams.datasource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 数据源管理服务启动类
 * 
 * 功能：
 * - 数据源配置管理
 * - 数据库连接池管理
 * - 数据源健康检查
 * - 数据源分类
 * - 数据源权限管理
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@EnableDiscoveryClient
@EnableKafka
@EnableAsync
@SpringBootApplication
@MapperScan("com.enterprise.edams.datasource.repository")
public class DatasourceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatasourceServiceApplication.class, args);
    }
}
