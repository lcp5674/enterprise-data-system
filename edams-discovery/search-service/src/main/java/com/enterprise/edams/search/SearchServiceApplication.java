package com.enterprise.edams.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 资产搜索服务启动类
 * 
 * 功能：
 * - 全文检索（Elasticsearch）
 * - 多维度搜索
 * - 搜索建议/自动补全
 * - 搜索结果排序
 * - 热门搜索/搜索历史
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@EnableDiscoveryClient
@EnableKafka
@EnableAsync
@SpringBootApplication
@MapperScan("com.enterprise.edams.search.repository")
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
