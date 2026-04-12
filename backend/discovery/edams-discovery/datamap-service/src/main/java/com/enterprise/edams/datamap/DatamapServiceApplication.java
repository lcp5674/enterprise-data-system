package com.enterprise.edams.datamap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DatamapServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DatamapServiceApplication.class, args);
    }
}
