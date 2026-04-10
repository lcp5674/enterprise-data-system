package com.enterprise.dataplatform.governance.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.governance-tasks:governance-tasks}")
    private String governanceTasksTopic;

    @Value("${kafka.topics.governance-events:governance-events}")
    private String governanceEventsTopic;

    @Value("${kafka.topics.ai-recommendations:ai-recommendations}")
    private String aiRecommendationsTopic;

    @Bean
    public NewTopic governanceTasksTopic() {
        return TopicBuilder.name(governanceTasksTopic)
                .partitions(6)
                .replicas(1)
                .config("retention.ms", "604800000")
                .config("max.message.bytes", "10485760")
                .build();
    }

    @Bean
    public NewTopic governanceEventsTopic() {
        return TopicBuilder.name(governanceEventsTopic)
                .partitions(6)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .build();
    }

    @Bean
    public NewTopic aiRecommendationsTopic() {
        return TopicBuilder.name(aiRecommendationsTopic)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "2592000000")
                .build();
    }
}
