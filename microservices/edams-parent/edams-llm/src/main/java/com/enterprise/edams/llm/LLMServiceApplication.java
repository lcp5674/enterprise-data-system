package com.enterprise.edams.llm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * LLM服务启动类
 *
 * @author LLM Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.edams")
@MapperScan("com.enterprise.edams.llm.mapper")
@EnableDiscoveryClient
@EnableScheduling
public class LLMServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LLMServiceApplication.class, args);
        System.out.println("""
            
            ╔═══════════════════════════════════════════════════════════════╗
            ║                                                               ║
            ║     ██╗  ██╗███████╗███╗   ██╗ ██████╗                       ║
            ║     ╚██╗██╔╝██╔════╝████╗  ██║██╔═══██╗                      ║
            ║      ╚███╔╝ █████╗  ██╔██╗ ██║██║   ██║                      ║
            ║      ██╔██╗ ██╔══╝  ██║╚██╗██║██║   ██║                      ║
            ║     ██╔╝ ██╗███████╗██║ ╚████║╚██████╔╝                      ║
            ║     ╚═╝  ╚═╝╚══════╝╚═╝  ╚═══╝ ╚═════╝                       ║
            ║                                                               ║
            ║            LLM服务 - Large Language Model Service            ║
            ║                                                               ║
            ╚═══════════════════════════════════════════════════════════════╝
            """);
    }
}
