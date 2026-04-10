package com.enterprise.edams.catalog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 数据目录服务启动类
 * 
 * 功能：
 * - 数据目录管理（分层分类）
 * - 目录结构维护
 * - 目录权限控制
 * - 目录搜索导航
 * - 目录统计
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@EnableDiscoveryClient
@EnableKafka
@EnableAsync
@SpringBootApplication
@MapperScan("com.enterprise.edams.catalog.repository")
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
