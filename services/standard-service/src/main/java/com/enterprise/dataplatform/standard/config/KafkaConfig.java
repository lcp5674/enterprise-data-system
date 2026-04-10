package com.enterprise.dataplatform.standard.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka配置类
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Kafka管理员客户端
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * 标准变更事件主题
     */
    @Bean
    public NewTopic standardChangesTopic() {
        return TopicBuilder.name("standard-changes")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * 标准映射变更事件主题
     */
    @Bean
    public NewTopic mappingChangesTopic() {
        return TopicBuilder.name("mapping-changes")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * 合规检查事件主题
     */
    @Bean
    public NewTopic complianceEventsTopic() {
        return TopicBuilder.name("standard-compliance-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
