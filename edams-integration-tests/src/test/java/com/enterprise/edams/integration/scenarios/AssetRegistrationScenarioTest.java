package com.enterprise.edams.integration.scenarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.Duration;
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
 * 场景1: 资产注册完整流程集成测试
 * 测试任务: INT-E2E-001
 * 
 * 流程:
 * 1. datasource-service 注册数据源
 * 2. catalog-service 创建目录并关联数据源
 * 3. datamap-service 生成数据地图
 * 4. lineage-service 记录血缘关系
 * 5. quality-service 执行质量检测
 */
@Testcontainers
@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
@DisplayName("场景1: 资产注册完整流程集成测试")
class AssetRegistrationScenarioTest {

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
        // PostgreSQL
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        // Neo4j
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
        
        // Redis
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
        
        // Kafka
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
    private ScenarioEventCollector eventCollector;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        jdbcTemplate.execute("DELETE FROM quality_check_result WHERE id > 0");
        jdbcTemplate.execute("DELETE FROM quality_rule WHERE id > 0");
        jdbcTemplate.execute("DELETE FROM catalog_asset WHERE asset_id > 0");
        jdbcTemplate.execute("DELETE FROM data_asset WHERE id > 0");
        jdbcTemplate.execute("DELETE FROM datasource WHERE name LIKE 'SCENARIO_%'");
        jdbcTemplate.execute("DELETE FROM data_catalog WHERE name LIKE 'SCENARIO_%'");
        
        // 清理Neo4j
        try (var session = neo4jDriver.session()) {
            session.run("MATCH (n) WHERE n.id STARTS WITH 'scenario-' DETACH DELETE n");
        }
        
        // 清理Redis
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
        
        // 清理事件收集器
        eventCollector.clear();
    }

    @Test
    @DisplayName("完整资产注册流程测试")
    void testCompleteAssetRegistrationFlow() throws Exception {
        // ========== Step 1: 注册数据源 ==========
        Long datasourceId = registerDatasource();
        assertThat(datasourceId).isNotNull();
        
        // ========== Step 2: 创建数据目录 ==========
        Long catalogId = createDataCatalog();
        assertThat(catalogId).isNotNull();
        
        // ========== Step 3: 注册数据资产 ==========
        Long assetId = registerDataAsset(datasourceId);
        assertThat(assetId).isNotNull();
        
        // ========== Step 4: 关联资产到目录 ==========
        associateAssetWithCatalog(catalogId, assetId);
        
        // ========== Step 5: 建立血缘关系 ==========
        createLineageRelationship(assetId);
        
        // ========== Step 6: 创建质量规则 ==========
        Long ruleId = createQualityRule(assetId);
        assertThat(ruleId).isNotNull();
        
        // ========== Step 7: 执行质量检测 ==========
        executeQualityCheck(ruleId, assetId);
        
        // ========== Step 8: 发送资产变更事件 ==========
        publishAssetEvent(assetId, "ASSET_REGISTERED");
        
        // ========== 验证完整流程 ==========
        verifyCompleteFlow(datasourceId, catalogId, assetId, ruleId);
    }

    @Test
    @DisplayName("多资产批量注册流程测试")
    void testBatchAssetRegistrationFlow() throws Exception {
        // Given - 创建数据源和目录
        Long datasourceId = registerDatasource();
        Long catalogId = createDataCatalog();
        
        // When - 批量注册资产
        int assetCount = 5;
        for (int i = 0; i < assetCount; i++) {
            Long assetId = registerDataAsset(datasourceId, "Batch Asset " + i);
            associateAssetWithCatalog(catalogId, assetId);
            createLineageRelationship(assetId);
            createQualityRule(assetId);
        }
        
        // Then - 验证批量注册结果
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM data_asset WHERE datasource_id = ?", 
            Integer.class, datasourceId
        );
        assertThat(count).isEqualTo(assetCount);
        
        // 验证Neo4j中的血缘关系
        try (var session = neo4jDriver.session()) {
            var result = session.run(
                "MATCH (a:DataAsset) WHERE a.id STARTS WITH 'scenario-asset' RETURN count(a) as count"
            );
            assertThat(result.single().get("count").asInt()).isEqualTo(assetCount);
        }
    }

    @Test
    @DisplayName("资产注册失败回滚测试")
    void testAssetRegistrationRollback() {
        // Given - 创建数据源
        Long datasourceId = registerDatasource();
        
        // When - 模拟资产注册失败（违反约束）
        try {
            // 尝试插入重复ID的资产
            jdbcTemplate.update(
                "INSERT INTO data_asset (id, name, type, datasource_id, status) VALUES (?, ?, ?, ?, ?)",
                -1, "Duplicate Asset", "TABLE", datasourceId, "ACTIVE"
            );
            jdbcTemplate.update(
                "INSERT INTO data_asset (id, name, type, datasource_id, status) VALUES (?, ?, ?, ?, ?)",
                -1, "Duplicate Asset 2", "TABLE", datasourceId, "ACTIVE"
            );
        } catch (Exception e) {
            // Expected - 应该抛出唯一约束冲突异常
        }
        
        // Then - 验证第一条插入也被回滚（如果在同一事务中）
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM data_asset WHERE id = ?", 
            Integer.class, -1
        );
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("资产注册事件通知测试")
    void testAssetRegistrationEventNotification() throws Exception {
        // Given
        Long datasourceId = registerDatasource();
        Long assetId = registerDataAsset(datasourceId);
        
        // When - 发送资产创建事件
        publishAssetEvent(assetId, "ASSET_CREATED");
        
        // Then - 验证事件被消费
        await().atMost(5, TimeUnit.SECONDS).until(() -> 
            eventCollector.hasEvent("asset-events", "ASSET_CREATED")
        );
        
        // When - 发送质量检测完成事件
        publishQualityEvent(assetId, "QUALITY_CHECK_COMPLETED");
        
        // Then - 验证质量事件被消费
        await().atMost(5, TimeUnit.SECONDS).until(() -> 
            eventCollector.hasEvent("quality-events", "QUALITY_CHECK_COMPLETED")
        );
    }

    // Helper methods
    private Long registerDatasource() {
        jdbcTemplate.update(
            "INSERT INTO datasource (name, type, connection_url, username, status) VALUES (?, ?, ?, ?, ?)",
            "SCENARIO_Test_DB", "POSTGRESQL", "jdbc:postgresql://test", "test_user", "ACTIVE"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM datasource WHERE name = ?", Long.class, "SCENARIO_Test_DB"
        );
    }

    private Long createDataCatalog() {
        jdbcTemplate.update(
            "INSERT INTO data_catalog (name, description, owner, status) VALUES (?, ?, ?, ?)",
            "SCENARIO_Test_Catalog", "Test catalog for scenario", "admin", "ACTIVE"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM data_catalog WHERE name = ?", Long.class, "SCENARIO_Test_Catalog"
        );
    }

    private Long registerDataAsset(Long datasourceId) {
        return registerDataAsset(datasourceId, "Scenario Test Asset");
    }

    private Long registerDataAsset(Long datasourceId, String assetName) {
        jdbcTemplate.update(
            "INSERT INTO data_asset (name, type, datasource_id, schema_name, table_name, owner, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
            assetName, "TABLE", datasourceId, "public", "test_table", "admin", "DRAFT"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM data_asset WHERE name = ?", Long.class, assetName
        );
    }

    private void associateAssetWithCatalog(Long catalogId, Long assetId) {
        jdbcTemplate.update(
            "INSERT INTO catalog_asset (catalog_id, asset_id) VALUES (?, ?)",
            catalogId, assetId
        );
    }

    private void createLineageRelationship(Long assetId) {
        try (var session = neo4jDriver.session()) {
            session.run(
                "CREATE (a:DataAsset {id: $id, name: $name, type: 'TABLE'})",
                Map.of("id", "scenario-asset-" + assetId, "name", "Asset " + assetId)
            );
            
            // 创建上游依赖
            session.run(
                "CREATE (source:DataAsset {id: $sourceId, name: $sourceName})",
                Map.of("sourceId", "scenario-source-" + assetId, "sourceName", "Source " + assetId)
            );
            
            // 建立关系
            session.run(
                "MATCH (source:DataAsset {id: $sourceId}), (target:DataAsset {id: $targetId}) " +
                "CREATE (target)-[:DEPENDS_ON {type: 'ETL', createdAt: datetime()}]->(source)",
                Map.of("sourceId", "scenario-source-" + assetId, "targetId", "scenario-asset-" + assetId)
            );
        }
    }

    private Long createQualityRule(Long assetId) {
        return createQualityRule(assetId, "NOT_NULL_CHECK");
    }

    private Long createQualityRule(Long assetId, String ruleName) {
        jdbcTemplate.update(
            "INSERT INTO quality_rule (name, rule_type, asset_id, expression, threshold, status) VALUES (?, ?, ?, ?, ?, ?)",
            ruleName, "NOT_NULL", assetId, "column IS NOT NULL", new BigDecimal("95.00"), "ACTIVE"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM quality_rule WHERE name = ? AND asset_id = ?", 
            Long.class, ruleName, assetId
        );
    }

    private void executeQualityCheck(Long ruleId, Long assetId) {
        jdbcTemplate.update(
            "INSERT INTO quality_check_result (rule_id, asset_id, total_count, error_count, error_rate, status) VALUES (?, ?, ?, ?, ?, ?)",
            ruleId, assetId, 1000, 10, new BigDecimal("1.00"), "PASSED"
        );
    }

    private void publishAssetEvent(Long assetId, String eventType) throws Exception {
        Map<String, Object> event = Map.of(
            "assetId", assetId,
            "eventType", eventType,
            "timestamp", System.currentTimeMillis()
        );
        kafkaTemplate.send("asset-events", assetId.toString(), objectMapper.writeValueAsString(event));
    }

    private void publishQualityEvent(Long assetId, String eventType) throws Exception {
        Map<String, Object> event = Map.of(
            "assetId", assetId,
            "eventType", eventType,
            "timestamp", System.currentTimeMillis()
        );
        kafkaTemplate.send("quality-events", assetId.toString(), objectMapper.writeValueAsString(event));
    }

    private void verifyCompleteFlow(Long datasourceId, Long catalogId, Long assetId, Long ruleId) {
        // 验证数据源存在
        Integer dsCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM datasource WHERE id = ?", Integer.class, datasourceId
        );
        assertThat(dsCount).isEqualTo(1);
        
        // 验证目录存在
        Integer catCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM data_catalog WHERE id = ?", Integer.class, catalogId
        );
        assertThat(catCount).isEqualTo(1);
        
        // 验证资产存在
        Integer assetCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM data_asset WHERE id = ?", Integer.class, assetId
        );
        assertThat(assetCount).isEqualTo(1);
        
        // 验证目录关联
        Integer assocCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM catalog_asset WHERE catalog_id = ? AND asset_id = ?", 
            Integer.class, catalogId, assetId
        );
        assertThat(assocCount).isEqualTo(1);
        
        // 验证质量规则
        Integer ruleCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM quality_rule WHERE id = ?", Integer.class, ruleId
        );
        assertThat(ruleCount).isEqualTo(1);
        
        // 验证质量检测结果
        Integer resultCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM quality_check_result WHERE rule_id = ? AND asset_id = ?", 
            Integer.class, ruleId, assetId
        );
        assertThat(resultCount).isEqualTo(1);
        
        // 验证Neo4j血缘关系
        try (var session = neo4jDriver.session()) {
            var result = session.run(
                "MATCH (a:DataAsset {id: $id})-[:DEPENDS_ON]->(source) RETURN count(source) as count",
                Map.of("id", "scenario-asset-" + assetId)
            );
            assertThat(result.single().get("count").asInt()).isEqualTo(1);
        }
    }

    // Event collector component
    @Component
    static class ScenarioEventCollector {
        private final java.util.concurrent.ConcurrentHashMap<String, java.util.List<String>> events = 
            new java.util.concurrent.ConcurrentHashMap<>();

        public void clear() {
            events.clear();
        }

        @KafkaListener(topics = "asset-events", groupId = "scenario-test-group")
        public void collectAssetEvents(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
            events.computeIfAbsent("asset-events", k -> new java.util.ArrayList<>()).add(record.value());
        }

        @KafkaListener(topics = "quality-events", groupId = "scenario-test-group")
        public void collectQualityEvents(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
            events.computeIfAbsent("quality-events", k -> new java.util.ArrayList<>()).add(record.value());
        }

        public boolean hasEvent(String topic, String content) {
            java.util.List<String> topicEvents = events.get(topic);
            if (topicEvents == null) return false;
            return topicEvents.stream().anyMatch(e -> e.contains(content));
        }
    }
}
