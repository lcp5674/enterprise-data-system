package com.enterprise.edams.integration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Testcontainers配置类
 * 提供PostgreSQL、Neo4j、Redis、Kafka等容器的测试配置
 */
@TestConfiguration
public class TestContainerConfig {

    private static final String POSTGRES_IMAGE = "postgres:15-alpine";
    private static final String NEO4J_IMAGE = "neo4j:5.18-community";
    private static final String REDIS_IMAGE = "redis:7-alpine";
    private static final String KAFKA_IMAGE = "confluentinc/cp-kafka:7.6.0";

    private static PostgreSQLContainer<?> postgresContainer;
    private static Neo4jContainer<?> neo4jContainer;
    private static GenericContainer<?> redisContainer;
    private static KafkaContainer kafkaContainer;

    @PostConstruct
    public void startContainers() {
        // 启动PostgreSQL容器
        if (postgresContainer == null) {
            postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
                    .withDatabaseName("edams_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withInitScript("sql/init-postgres.sql")
                    .waitingFor(Wait.forListeningPort());
            postgresContainer.start();
            System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
            System.setProperty("spring.datasource.username", postgresContainer.getUsername());
            System.setProperty("spring.datasource.password", postgresContainer.getPassword());
        }

        // 启动Neo4j容器
        if (neo4jContainer == null) {
            neo4jContainer = new Neo4jContainer<>(DockerImageName.parse(NEO4J_IMAGE))
                    .withAdminPassword("password")
                    .waitingFor(Wait.forListeningPort());
            neo4jContainer.start();
            System.setProperty("spring.neo4j.uri", neo4jContainer.getBoltUrl());
            System.setProperty("spring.neo4j.authentication.username", "neo4j");
            System.setProperty("spring.neo4j.authentication.password", "password");
        }

        // 启动Redis容器
        if (redisContainer == null) {
            redisContainer = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                    .withExposedPorts(6379)
                    .waitingFor(Wait.forListeningPort());
            redisContainer.start();
            System.setProperty("spring.redis.host", redisContainer.getHost());
            System.setProperty("spring.redis.port", redisContainer.getMappedPort(6379).toString());
        }

        // 启动Kafka容器
        if (kafkaContainer == null) {
            kafkaContainer = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE))
                    .waitingFor(Wait.forListeningPort());
            kafkaContainer.start();
            System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
        }
    }

    @PreDestroy
    public void stopContainers() {
        if (kafkaContainer != null && kafkaContainer.isRunning()) {
            kafkaContainer.stop();
        }
        if (redisContainer != null && redisContainer.isRunning()) {
            redisContainer.stop();
        }
        if (neo4jContainer != null && neo4jContainer.isRunning()) {
            neo4jContainer.stop();
        }
        if (postgresContainer != null && postgresContainer.isRunning()) {
            postgresContainer.stop();
        }
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        factory.setHostName(redisContainer.getHost());
        factory.setPort(redisContainer.getMappedPort(6379));
        return factory;
    }

    public static PostgreSQLContainer<?> getPostgresContainer() {
        return postgresContainer;
    }

    public static Neo4jContainer<?> getNeo4jContainer() {
        return neo4jContainer;
    }

    public static GenericContainer<?> getRedisContainer() {
        return redisContainer;
    }

    public static KafkaContainer getKafkaContainer() {
        return kafkaContainer;
    }
}
