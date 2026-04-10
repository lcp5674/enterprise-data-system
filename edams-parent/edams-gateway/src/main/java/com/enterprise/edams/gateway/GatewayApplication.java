package com.enterprise.edams.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.oas.annotations.EnableOpenApi;

/**
 * API网关启动类
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@EnableOpenApi
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = {"com.enterprise.edams.gateway", "com.enterprise.edams.common"})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
