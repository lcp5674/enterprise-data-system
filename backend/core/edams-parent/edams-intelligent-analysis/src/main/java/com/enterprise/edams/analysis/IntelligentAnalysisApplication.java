package com.enterprise.edams.analysis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@MapperScan("com.enterprise.edams.analysis.repository")
public class IntelligentAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelligentAnalysisApplication.class, args);
    }
}
