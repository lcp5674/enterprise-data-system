package com.enterprise.edams.asset;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import springfox.documentation.oas.annotations.EnableOpenApi;

/**
 * 资产管理服务启动类
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@EnableOpenApi
@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka
@SpringBootApplication
@MapperScan("com.enterprise.edams.asset.mapper")
@ComponentScan(basePackages = {"com.enterprise.edams.asset", "com.enterprise.edams.common"})
public class AssetApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetApplication.class, args);
    }
}
