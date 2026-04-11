package com.enterprise.edams.knowledge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 知识图谱服务启动类
 * 提供本体论、实体、关系的管理和检索功能
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.enterprise.edams.knowledge", "com.enterprise.edams.common"})
@MapperScan("com.enterprise.edams.knowledge.repository")
public class KnowledgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeApplication.class, args);
    }
}
