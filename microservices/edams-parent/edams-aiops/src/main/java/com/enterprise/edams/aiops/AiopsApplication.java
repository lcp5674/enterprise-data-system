package com.enterprise.edams.aiops;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AIOps智能运维服务启动类
 * 
 * 功能模块:
 * - 智能异常检测: 基于时间序列数据分析，检测CPU、内存、磁盘、响应时间等指标的异常
 * - 容量规划预测: 基于历史数据分析资源使用趋势，预测未来容量需求
 * - 根因分析: 基于血缘关系分析故障影响范围，提供问题定位建议
 * - 健康评分: 综合计算系统/服务健康评分，展示健康趋势图
 * - 告警优化: 智能告警聚合、收敛和关联，避免告警风暴
 *
 * @author Backend Team - AIOps
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableScheduling
@EnableCaching
@ComponentScan(basePackages = {"com.enterprise.edams.aiops", "com.enterprise.edams.common"})
@MapperScan("com.enterprise.edams.aiops.repository")
public class AiopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiopsApplication.class, args);
    }
}
