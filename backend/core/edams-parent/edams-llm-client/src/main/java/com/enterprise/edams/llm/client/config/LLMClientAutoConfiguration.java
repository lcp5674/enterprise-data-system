package com.enterprise.edams.llm.client.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LLMProperties.class)
@ComponentScan(basePackages = "com.enterprise.edams.llm.client")
public class LLMClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LLMProperties llmProperties() {
        return new LLMProperties();
    }
}
