package com.enterprise.edams.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.oas.annotations.EnableOpenApi;

/**
 * 认证服务启动类
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@EnableOpenApi
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.enterprise.edams.auth.mapper")
@ComponentScan(basePackages = {"com.enterprise.edams.auth", "com.enterprise.edams.common"})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
