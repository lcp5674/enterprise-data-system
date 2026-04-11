package com.enterprise.edams.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * EDAMS认证服务启动类
 * 
 * <p>提供用户认证、JWT令牌管理、登录日志等功能</p>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.edams")
@EnableDiscoveryClient
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
