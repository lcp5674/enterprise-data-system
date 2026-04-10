package com.enterprise.edams.datamap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 数据地图服务启动类
 * 
 * 功能：
 * - 数据资产元信息管理
 * - 数据血缘分析
 * - 影响分析
 * - 数据流向图
 * - 资产统计报表
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@EnableDiscoveryClient
@EnableKafka
@EnableAsync
@SpringBootApplication
@MapperScan("com.enterprise.edams.datamap.repository")
public class DatamapServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatamapServiceApplication.class, args);
    }
}
