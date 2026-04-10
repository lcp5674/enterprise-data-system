package com.enterprise.edams.integration.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;

/**
 * 集成测试基础类
 * 所有集成测试类应继承此类以获得完整的测试容器支持
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    @Autowired
    protected KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // 数据库配置将在TestContainerConfig中动态设置
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
        
        // Redis配置
        registry.add("spring.redis.timeout", () -> "2000ms");
        registry.add("spring.redis.lettuce.pool.max-active", () -> "8");
        
        // Kafka配置
        registry.add("spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.consumer.key-deserializer", () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.consumer.value-deserializer", () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.group-id", () -> "test-group");
    }

    @BeforeEach
    void setUp() {
        // 清理Redis缓存
        if (redisTemplate != null) {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
        }
    }

    /**
     * 清理测试数据
     */
    protected void cleanUpTestData() {
        // 子类可重写此方法进行特定的数据清理
    }

    /**
     * 执行SQL脚本
     */
    protected void executeSql(String sql) {
        jdbcTemplate.execute(sql);
    }

    /**
     * 发送Kafka消息
     */
    protected void sendKafkaMessage(String topic, String key, Object message) throws Exception {
        String jsonMessage = objectMapper.writeValueAsString(message);
        kafkaTemplate.send(topic, key, jsonMessage);
    }
}
