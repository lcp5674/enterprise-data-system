package com.enterprise.edams.lifecycle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 数据生命周期管理服务启动类
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@MapperScan("com.enterprise.edams.lifecycle.repository")
@ComponentScan(basePackages = {"com.enterprise.edams.lifecycle", "com.enterprise.edams.common"})
public class LifecycleApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifecycleApplication.class, args);
    }
}
