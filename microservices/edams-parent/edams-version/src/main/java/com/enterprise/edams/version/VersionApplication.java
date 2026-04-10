package com.enterprise.edams.version;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 版本管理服务启动类
 * 
 * @author EDAMS Team
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.edams")
@EnableDiscoveryClient
public class VersionApplication {

    public static void main(String[] args) {
        SpringApplication.run(VersionApplication.class, args);
    }
}
