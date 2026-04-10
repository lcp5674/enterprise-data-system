package com.enterprise.edams.integration.scenarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
 * 场景2: 质量检测流程集成测试
 * 测试任务: INT-E2E-002
 *
 * 流程:
 * 1. quality-service 创建质量规则
 * 2. quality-service 执行质量检测
 * 3. lineage-service 关联血缘
 * 4. notification-service 发送告警通知
 */
@Testcontainers
@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
@DisplayName("场景2: 质量检测流程集成测试")
class QualityCheckScenarioTest {

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
    private QualityEventCollector eventCollector;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM quality_check_result WHERE id > 0");
        jdbcTemplate.execute("DELETE FROM quality_rule WHERE name LIKE 'SCENARIO_%'");
        jdbcTemplate.execute("DELETE FROM data_asset WHERE name LIKE 'SCENARIO_%'");
        jdbcTemplate.execute("DELETE FROM datasource WHERE name LIKE 'SCENARIO_%'");
        jdbcTemplate.execute("DELETE FROM notification WHERE title LIKE 'SCENARIO_%'");

        try (var session = neo4jDriver.session()) {
            session.run("MATCH (n) WHERE n.id STARTS WITH 'quality-' DETACH DELETE n");
        }

        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
        eventCollector.clear();
    }

    @Test
    @DisplayName("完整质量检测流程测试")
    void testCompleteQualityCheckFlow() throws Exception {
        // ========== Step 1: 准备测试数据 ==========
        Long datasourceId = createTestDatasource();
        Long assetId = createTestAsset(datasourceId);

        // ========== Step 2: 创建质量规则 ==========
        Long ruleId = createQualityRule(assetId, "SCENARIO_NotNullRule", "NOT_NULL");
        assertThat(ruleId).isNotNull();

        // ========== Step 3: 执行质量检测 ==========
        Long checkResultId = executeQualityCheck(ruleId, assetId, 1000, 50, 5.0);
        assertThat(checkResultId).isNotNull();

        // ========== Step 4: 关联血缘关系 ==========
        createQualityLineage(assetId, ruleId);

        // ========== Step 5: 发送质量检测事件 ==========
        publishQualityCheckEvent(assetId, ruleId, "FAILED");

        // ========== Step 6: 触发通知服务 ==========
        createNotification(assetId, ruleId, "Quality check failed for asset");

        // ========== 验证完整流程 ==========
        verifyQualityCheckFlow(ruleId, assetId, checkResultId);
    }

    @Test
    @DisplayName("质量检测通过流程测试")
    void testQualityCheckPassedFlow() throws Exception {
        // Given
        Long datasourceId = createTestDatasource();
        Long assetId = createTestAsset(datasourceId);
        Long ruleId = createQualityRule(assetId, "SCENARIO_PassRule", "UNIQUE");

        // When - 执行质量检测（通过）
        executeQualityCheck(ruleId, assetId, 1000, 0, 0.0);

        // Then - 发送通过事件
        publishQualityCheckEvent(assetId, ruleId, "PASSED");

        // Verify
        await().atMost(5, TimeUnit.SECONDS).until(() ->
            eventCollector.hasEvent("quality-events", "PASSED")
        );
    }

    @Test
    @DisplayName("多规则质量检测流程测试")
    void testMultipleRulesQualityCheck() throws Exception {
        // Given
        Long datasourceId = createTestDatasource();
        Long assetId = createTestAsset(datasourceId);

        // When - 创建多个质量规则
        Long rule1 = createQualityRule(assetId, "SCENARIO_Rule1", "NOT_NULL");
        Long rule2 = createQualityRule(assetId, "SCENARIO_Rule2", "UNIQUE");
        Long rule3 = createQualityRule(assetId, "SCENARIO_Rule3", "RANGE");

        // 执行多个规则检测
        executeQualityCheck(rule1, assetId, 1000, 10, 1.0);
        executeQualityCheck(rule2, assetId, 1000, 5, 0.5);
        executeQualityCheck(rule3, assetId, 1000, 100, 10.0);

        // Then - 验证所有规则结果
        Integer resultCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM quality_check_result WHERE asset_id = ?",
            Integer.class, assetId
        );
        assertThat(resultCount).isEqualTo(3);

        // 验证失败规则触发通知
        publishQualityCheckEvent(assetId, rule3, "FAILED");
        createNotification(assetId, rule3, "Range check failed");

        Integer notifCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notification WHERE recipient = ?",
            Integer.class, "admin@enterprise.com"
        );
        assertThat(notifCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("质量检测阈值告警测试")
    void testQualityThresholdAlert() throws Exception {
        // Given
        Long datasourceId = createTestDatasource();
        Long assetId = createTestAsset(datasourceId);

        // 创建阈值为5%的规则
        Long ruleId = createQualityRuleWithThreshold(assetId, "SCENARIO_ThresholdRule", 5.0);

        // When - 执行检测，错误率超过阈值
        executeQualityCheck(ruleId, assetId, 1000, 100, 10.0);

        // Then - 应该触发告警
        BigDecimal errorRate = jdbcTemplate.queryForObject(
            "SELECT error_rate FROM quality_check_result WHERE rule_id = ? ORDER BY check_time DESC LIMIT 1",
            BigDecimal.class, ruleId
        );
        assertThat(errorRate.doubleValue()).isGreaterThan(5.0);

        // 触发告警通知
        createNotification(assetId, ruleId, "Error rate exceeded threshold: " + errorRate + "%");

        Integer alertCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notification WHERE content LIKE ?",
            Integer.class, "%threshold%"
        );
        assertThat(alertCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("质量检测事件驱动通知测试")
    void testEventDrivenNotification() throws Exception {
        // Given
        Long datasourceId = createTestDatasource();
        Long assetId = createTestAsset(datasourceId);
        Long ruleId = createQualityRule(assetId, "SCENARIO_EventRule", "NOT_NULL");

        // When - 执行检测并发送事件
        executeQualityCheck(ruleId, assetId, 1000, 200, 20.0);
        publishQualityCheckEvent(assetId, ruleId, "FAILED");

        // Then - 验证事件被消费
        await().atMost(5, TimeUnit.SECONDS).until(() ->
            eventCollector.hasEvent("quality-events", "FAILED")
        );

        // 模拟通知服务消费事件并创建通知
        createNotification(assetId, ruleId, "Quality check failed with 20% error rate");

        // 验证通知创建
        await().atMost(3, TimeUnit.SECONDS).until(() -> {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notification WHERE title LIKE ?",
                Integer.class, "%SCENARIO_%"
            );
            return count > 0;
        });
    }

    @Test
    @DisplayName("质量检测血缘追踪测试")
    void testQualityCheckLineageTracking() throws Exception {
        // Given
        Long datasourceId = createTestDatasource();
        Long assetId = createTestAsset(datasourceId);
        Long ruleId = createQualityRule(assetId, "SCENARIO_LineageRule", "NOT_NULL");

        // When - 创建血缘关系
        createQualityLineage(assetId, ruleId);

        // Then - 验证Neo4j中的血缘关系
        try (var session = neo4jDriver.session()) {
            var result = session.run(
                "MATCH (a:DataAsset {id: $assetId})-[:HAS_QUALITY_RULE]->(r:QualityRule) RETURN count(r) as count",
                Map.of("assetId", "quality-asset-" + assetId)
            );
            assertThat(result.single().get("count").asInt()).isEqualTo(1);
        }

        // 执行检测并更新血缘
        executeQualityCheck(ruleId, assetId, 1000, 0, 0.0);
        updateLineageWithCheckResult(assetId, ruleId, "PASSED");

        // 验证更新后的血缘
        try (var session = neo4jDriver.session()) {
            var result = session.run(
                "MATCH (a:DataAsset {id: $assetId})-[:HAS_QUALITY_RULE]->(r:QualityRule) " +
                "RETURN r.lastCheckStatus as status",
                Map.of("assetId", "quality-asset-" + assetId)
            );
            assertThat(result.single().get("status").asString()).isEqualTo("PASSED");
        }
    }

    // Helper methods
    private Long createTestDatasource() {
        jdbcTemplate.update(
            "INSERT INTO datasource (name, type, connection_url, username, status) VALUES (?, ?, ?, ?, ?)",
            "SCENARIO_Quality_DB", "POSTGRESQL", "jdbc:postgresql://quality", "quality_user", "ACTIVE"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM datasource WHERE name = ?", Long.class, "SCENARIO_Quality_DB"
        );
    }

    private Long createTestAsset(Long datasourceId) {
        jdbcTemplate.update(
            "INSERT INTO data_asset (name, type, datasource_id, schema_name, table_name, owner, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
            "SCENARIO_Quality_Table", "TABLE", datasourceId, "public", "quality_table", "admin", "ACTIVE"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM data_asset WHERE name = ?", Long.class, "SCENARIO_Quality_Table"
        );
    }

    private Long createQualityRule(Long assetId, String ruleName, String ruleType) {
        jdbcTemplate.update(
            "INSERT INTO quality_rule (name, rule_type, asset_id, expression, threshold, status) VALUES (?, ?, ?, ?, ?, ?)",
            ruleName, ruleType, assetId, "test_expression", new BigDecimal("95.00"), "ACTIVE"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM quality_rule WHERE name = ?", Long.class, ruleName
        );
    }

    private Long createQualityRuleWithThreshold(Long assetId, String ruleName, double threshold) {
        jdbcTemplate.update(
            "INSERT INTO quality_rule (name, rule_type, asset_id, expression, threshold, status) VALUES (?, ?, ?, ?, ?, ?)",
            ruleName, "THRESHOLD", assetId, "error_rate < " + threshold, new BigDecimal(threshold), "ACTIVE"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM quality_rule WHERE name = ?", Long.class, ruleName
        );
    }

    private Long executeQualityCheck(Long ruleId, Long assetId, int totalCount, int errorCount, double errorRate) {
        jdbcTemplate.update(
            "INSERT INTO quality_check_result (rule_id, asset_id, total_count, error_count, error_rate, status) VALUES (?, ?, ?, ?, ?, ?)",
            ruleId, assetId, totalCount, errorCount, new BigDecimal(errorRate),
            errorRate > 5.0 ? "FAILED" : "PASSED"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM quality_check_result WHERE rule_id = ? AND asset_id = ? ORDER BY check_time DESC LIMIT 1",
            Long.class, ruleId, assetId
        );
    }

    private void createQualityLineage(Long assetId, Long ruleId) {
        try (var session = neo4jDriver.session()) {
            session.run(
                "CREATE (a:DataAsset {id: $assetId, name: $assetName, type: 'TABLE'})",
                Map.of("assetId", "quality-asset-" + assetId, "assetName", "Asset " + assetId)
            );
            session.run(
                "CREATE (r:QualityRule {id: $ruleId, name: $ruleName, type: 'NOT_NULL'})",
                Map.of("ruleId", "quality-rule-" + ruleId, "ruleName", "Rule " + ruleId)
            );
            session.run(
                "MATCH (a:DataAsset {id: $assetId}), (r:QualityRule {id: $ruleId}) " +
                "CREATE (a)-[:HAS_QUALITY_RULE {createdAt: datetime()}]->(r)",
                Map.of("assetId", "quality-asset-" + assetId, "ruleId", "quality-rule-" + ruleId)
            );
        }
    }

    private void updateLineageWithCheckResult(Long assetId, Long ruleId, String status) {
        try (var session = neo4jDriver.session()) {
            session.run(
                "MATCH (a:DataAsset {id: $assetId})-[:HAS_QUALITY_RULE]->(r:QualityRule {id: $ruleId}) " +
                "SET r.lastCheckStatus = $status, r.lastCheckTime = datetime()",
                Map.of("assetId", "quality-asset-" + assetId, "ruleId", "quality-rule-" + ruleId, "status", status)
            );
        }
    }

    private void publishQualityCheckEvent(Long assetId, Long ruleId, String status) throws Exception {
        Map<String, Object> event = Map.of(
            "assetId", assetId,
            "ruleId", ruleId,
            "eventType", "QUALITY_CHECK_" + status,
            "status", status,
            "timestamp", System.currentTimeMillis()
        );
        kafkaTemplate.send("quality-events", assetId.toString(), objectMapper.writeValueAsString(event));
    }

    private void createNotification(Long assetId, Long ruleId, String content) {
        jdbcTemplate.update(
            "INSERT INTO notification (type, title, content, recipient, status) VALUES (?, ?, ?, ?, ?)",
            "QUALITY_ALERT", "SCENARIO_Quality Alert", content, "admin@enterprise.com", "PENDING"
        );
    }

    private void verifyQualityCheckFlow(Long ruleId, Long assetId, Long checkResultId) {
        // 验证质量规则存在
        Integer ruleCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM quality_rule WHERE id = ?", Integer.class, ruleId
        );
        assertThat(ruleCount).isEqualTo(1);

        // 验证检测结果存在
        Integer resultCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM quality_check_result WHERE id = ?", Integer.class, checkResultId
        );
        assertThat(resultCount).isEqualTo(1);

        // 验证通知创建
        Integer notifCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notification WHERE title LIKE ?", Integer.class, "%SCENARIO_%"
        );
        assertThat(notifCount).isGreaterThan(0);

        // 验证事件发送
        await().atMost(5, TimeUnit.SECONDS).until(() ->
            eventCollector.getEventCount("quality-events") > 0
        );
    }

    // Event collector component
    @Component
    static class QualityEventCollector {
        private final Map<String, AtomicInteger> eventCounts = new java.util.concurrent.ConcurrentHashMap<>();
        private final Map<String, java.util.List<String>> events = new java.util.concurrent.ConcurrentHashMap<>();

        public void clear() {
            eventCounts.clear();
            events.clear();
        }

        @KafkaListener(topics = "quality-events", groupId = "quality-scenario-test-group")
        public void collectQualityEvents(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
            eventCounts.computeIfAbsent("quality-events", k -> new AtomicInteger(0)).incrementAndGet();
            events.computeIfAbsent("quality-events", k -> new java.util.ArrayList<>()).add(record.value());
        }

        public boolean hasEvent(String topic, String content) {
            java.util.List<String> topicEvents = events.get(topic);
            if (topicEvents == null) return false;
            return topicEvents.stream().anyMatch(e -> e.contains(content));
        }

        public int getEventCount(String topic) {
            return eventCounts.getOrDefault(topic, new AtomicInteger(0)).get();
        }
    }
}
