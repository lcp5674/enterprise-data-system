package com.enterprise.edams.integration.scenarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 场景4: 知识图谱构建集成测试
 */
@Testcontainers
@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
@DisplayName("场景4: 知识图谱构建集成测试")
class KnowledgeGraphScenarioTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("edams_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("sql/init-postgres.sql");

    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>(
            DockerImageName.parse("neo4j:5.18-community"))
            .withAdminPassword("password");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private org.neo4j.driver.Driver neo4jDriver;

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KnowledgeEventCollector eventCollector;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM knowledge_relation WHERE id > 0");
        jdbcTemplate.execute("DELETE FROM knowledge_entity WHERE name LIKE 'SCENARIO_%'");
        try (var session = neo4jDriver.session()) {
            session.run("MATCH (n) WHERE n.source = 'scenario' DETACH DELETE n");
        }
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
        eventCollector.clear();
    }

    @Test
    @DisplayName("完整知识图谱构建流程测试")
    void testCompleteKnowledgeGraphBuildFlow() throws Exception {
        Long entity1 = importKnowledgeEntity("SCENARIO_Customer", "ENTITY", Map.of("field1", "value1"));
        Long entity2 = importKnowledgeEntity("SCENARIO_Order", "ENTITY", Map.of("field2", "value2"));
        assertThat(entity1).isNotNull();
        assertThat(entity2).isNotNull();

        createGraphNode("Customer", "Entity", Map.of("name", "Test Customer"));
        createGraphNode("Order", "Entity", Map.of("orderId", "ORD-001"));
        createGraphRelationship("Customer", "Order", "PLACED");

        publishKnowledgeGraphEvent("GRAPH_UPDATED", 2);
        verifyKnowledgeGraphFlow(entity1, entity2);
    }

    @Test
    @DisplayName("知识图谱查询测试")
    void testKnowledgeGraphQuery() {
        buildTestKnowledgeGraph();
        List<String> entities = queryAllEntities();
        assertThat(entities).hasSizeGreaterThanOrEqualTo(3);
    }

    private Long importKnowledgeEntity(String name, String type, Map<String, String> properties) {
        String propsJson = toJson(properties);
        jdbcTemplate.update(
            "INSERT INTO knowledge_entity (name, entity_type, properties, source) VALUES (?, ?, ?::jsonb, ?)",
            name, type, propsJson, "scenario"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM knowledge_entity WHERE name = ?", Long.class, name
        );
    }

    private void createGraphNode(String name, String type, Map<String, Object> properties) {
        try (var session = neo4jDriver.session()) {
            Map<String, Object> params = new java.util.HashMap<>(properties);
            params.put("name", name);
            params.put("type", type);
            params.put("source", "scenario");
            session.run("CREATE (n:Entity {name: $name, type: $type, source: $source}) SET n += $props", params);
        }
    }

    private void createGraphRelationship(String from, String to, String type) {
        try (var session = neo4jDriver.session()) {
            session.run(
                "MATCH (a:Entity {name: $from}), (b:Entity {name: $to}) " +
                "CREATE (a)-[:" + type + " {createdAt: datetime()}]->(b)",
                Map.of("from", from, "to", to)
            );
        }
    }

    private void buildTestKnowledgeGraph() {
        createGraphNode("Alice", "Person", Map.of("role", "Manager"));
        createGraphNode("Bob", "Person", Map.of("role", "Developer"));
        createGraphNode("Project A", "Project", Map.of("status", "Active"));
        createGraphRelationship("Alice", "Project A", "MANAGES");
        createGraphRelationship("Bob", "Project A", "WORKS_ON");
    }

    private List<String> queryAllEntities() {
        try (var session = neo4jDriver.session()) {
            var result = session.run("MATCH (n:Entity) WHERE n.source = 'scenario' RETURN n.name as name");
            return result.list(r -> r.get("name").asString());
        }
    }

    private void publishKnowledgeGraphEvent(String eventType, int entityCount) throws Exception {
        Map<String, Object> event = Map.of(
            "eventType", eventType,
            "entityCount", entityCount,
            "timestamp", System.currentTimeMillis()
        );
        kafkaTemplate.send("knowledge-events", eventType, objectMapper.writeValueAsString(event));
    }

    private void verifyKnowledgeGraphFlow(Long entity1, Long entity2) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM knowledge_entity WHERE id IN (?, ?)",
            Integer.class, entity1, entity2
        );
        assertThat(count).isEqualTo(2);

        try (var session = neo4jDriver.session()) {
            var result = session.run("MATCH (n:Entity) WHERE n.source = 'scenario' RETURN count(n) as cnt");
            assertThat(result.single().get("cnt").asInt()).isGreaterThanOrEqualTo(2);
        }
    }

    private String toJson(Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Component
    static class KnowledgeEventCollector {
        private final Map<String, java.util.List<String>> events = new java.util.concurrent.ConcurrentHashMap<>();

        public void clear() { events.clear(); }

        @KafkaListener(topics = "knowledge-events", groupId = "knowledge-test-group")
        public void collect(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
            events.computeIfAbsent("knowledge-events", k -> new java.util.ArrayList<>()).add(record.value());
        }
    }
}
