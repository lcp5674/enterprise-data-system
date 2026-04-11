package com.enterprise.dataplatform.lineage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Data Lineage Service Application
 * 
 * Provides data lineage management capabilities including:
 * - Lineage collection (DDL parsing, ETL logs)
 * - Lineage relationship storage (Neo4j)
 * - Lineage visualization queries
 * - Impact analysis / Traceability analysis
 * - Lineage change monitoring
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class LineageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LineageServiceApplication.class, args);
    }
}
