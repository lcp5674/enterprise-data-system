package com.enterprise.edams.knowledge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 知识图谱服务启动类
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.edams")
@MapperScan("com.enterprise.edams.knowledge.mapper")
@EnableDiscoveryClient
@EnableAsync
public class KnowledgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeServiceApplication.class, args);
        System.out.println("""
            
            ╔═══════════════════════════════════════════════════════════════╗
            ║                                                               ║
            ║     ██████╗  ██████╗ ██████╗ ████████╗ █████╗ ███╗   ██╗       ║
            ║    ██╔════╝ ██╔═══██╗██╔══██╗╚══██╔══╝██╔══██╗████╗  ██║       ║
            ║    ██║  ███╗██║   ██║██║  ██║   ██║   ███████║██╔██╗ ██║       ║
            ║    ██║   ██║██║   ██║██║  ██║   ██║   ██╔══██║██║╚██╗██║       ║
            ║    ╚██████╔╝╚██████╔╝██████╔╝   ██║   ██║  ██║██║ ╚████║       ║
            ║     ╚═════╝  ╚═════╝ ╚═════╝    ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═══╝       ║
            ║                                                               ║
            ║            知识图谱服务 - Knowledge Service                   ║
            ║                                                               ║
            ╚═══════════════════════════════════════════════════════════════╝
            """);
    }
}
