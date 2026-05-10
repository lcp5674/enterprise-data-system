package com.enterprise.edams.collaboration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 协作评论服务启动类
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@MapperScan("com.enterprise.edams.collaboration.repository")
@ComponentScan(basePackages = {"com.enterprise.edams.collaboration", "com.enterprise.edams.common"})
public class CollaborationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollaborationApplication.class, args);
    }
}
