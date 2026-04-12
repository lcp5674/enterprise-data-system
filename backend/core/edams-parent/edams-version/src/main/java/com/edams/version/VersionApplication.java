package com.edams.version;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.edams.version.repository")
public class VersionApplication {
    public static void main(String[] args) {
        SpringApplication.run(VersionApplication.class, args);
    }
}
