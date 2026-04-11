package com.enterprise.edams.permission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * EDAMS权限服务启动类
 *
 * <p>提供RBAC权限模型、菜单管理、角色-权限绑定等功能</p>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.edams")
@EnableDiscoveryClient
public class PermissionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PermissionApplication.class, args);
    }
}
