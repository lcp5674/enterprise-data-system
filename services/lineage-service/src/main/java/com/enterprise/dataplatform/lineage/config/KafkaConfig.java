package com.enterprise.dataplatform.lineage.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka configuration for lineage events
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.lineage-changes:lineage-changes}")
    private String lineageChangesTopic;

    @Value("${kafka.topic.lineage-parse:lineage-parse}")
    private String lineageParseTopic;

    @Bean
    public NewTopic lineageChangesTopic() {
        return TopicBuilder.name(lineageChangesTopic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic lineageParseTopic() {
        return TopicBuilder.name(lineageParseTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
