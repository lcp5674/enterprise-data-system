package com.enterprise.edams.chatbot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智能问答服务启动类
 *
 * @author Chatbot Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.enterprise.edams")
@MapperScan("com.enterprise.edams.chatbot.mapper")
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class ChatbotServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatbotServiceApplication.class, args);
        System.out.println("""
            
            ╔═══════════════════════════════════════════════════════════════╗
            ║                                                               ║
            ║     ███████╗██╗   ██╗███████╗███╗   ██╗████████╗███████╗     ║
            ║     ██╔════╝╚██╗ ██╔╝██╔════╝████╗  ██║╚══██╔══╝██╔════╝     ║
            ║     ███████╗ ╚████╔╝ █████╗  ██╔██╗ ██║   ██║   ███████╗     ║
            ║     ╚════██║  ╚██╔╝  ██╔══╝  ██║╚██╗██║   ██║   ╚════██║     ║
            ║     ███████║   ██║   ███████╗██║ ╚████║   ██║   ███████║     ║
            ║     ╚══════╝   ╚═╝   ╚══════╝╚═╝  ╚═══╝   ╚═╝   ╚══════╝     ║
            ║                                                               ║
            ║         智能问答服务 - Knowledge-based Chatbot Service         ║
            ║                                                               ║
            ╚═══════════════════════════════════════════════════════════════╝
            """);
    }
}
