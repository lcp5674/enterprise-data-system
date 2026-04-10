package com.enterprise.edams.integration.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * PostgreSQL数据库集成测试
 * 测试任务: INT-DB-001
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("PostgreSQL数据库集成测试")
class PostgreSQLIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("edams_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("sql/init-postgres.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        jdbcTemplate.execute("DELETE FROM quality_check_result WHERE id > 0");
        jdbcTemplate.execute("DELETE FROM quality_rule WHERE id > 0");
        jdbcTemplate.execute("DELETE FROM catalog_asset WHERE asset_id > 0");
        jdbcTemplate.execute("DELETE FROM data_asset WHERE id > 0");
    }

    @Test
    @DisplayName("测试数据库连接")
    void testDatabaseConnection() {
        // When
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        // Then
        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("测试数据源表CRUD操作")
    void testDatasourceCrudOperations() {
        // Given - Create
        String insertSql = "INSERT INTO datasource (name, type, connection_url, username, status) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertSql, "Test DB", "MYSQL", "jdbc:mysql://test", "user", "ACTIVE");

        // When - Read
        String querySql = "SELECT name, type, status FROM datasource WHERE name = ?";
        List<String> result = jdbcTemplate.query(querySql, (rs, rowNum) -> rs.getString("name"), "Test DB");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo("Test DB");

        // When - Update
        String updateSql = "UPDATE datasource SET status = ? WHERE name = ?";
        int updatedRows = jdbcTemplate.update(updateSql, "INACTIVE", "Test DB");
        assertThat(updatedRows).isEqualTo(1);

        // When - Delete
        String deleteSql = "DELETE FROM datasource WHERE name = ?";
        int deletedRows = jdbcTemplate.update(deleteSql, "Test DB");
        assertThat(deletedRows).isEqualTo(1);
    }

    @Test
    @DisplayName("测试数据资产表CRUD操作")
    void testDataAssetCrudOperations() {
        // Given - 先创建数据源
        String insertDatasourceSql = "INSERT INTO datasource (name, type, connection_url, username, status) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertDatasourceSql, "Asset Test DB", "POSTGRESQL", "jdbc:postgresql://test", "user", "ACTIVE");

        Long datasourceId = jdbcTemplate.queryForObject(
                "SELECT id FROM datasource WHERE name = ?", Long.class, "Asset Test DB");

        // When - Create Asset
        String insertAssetSql = "INSERT INTO data_asset (name, type, datasource_id, schema_name, table_name, description, owner, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertAssetSql, "Customer Table", "TABLE", datasourceId, "public", "customers",
                "Customer information table", "admin", "DRAFT");

        // Then - Read Asset
        String queryAssetSql = "SELECT name, type, status FROM data_asset WHERE name = ?";
        List<String> assets = jdbcTemplate.query(queryAssetSql, (rs, rowNum) -> rs.getString("name"), "Customer Table");

        assertThat(assets).hasSize(1);
        assertThat(assets.get(0)).isEqualTo("Customer Table");

        // When - Update Asset
        String updateSql = "UPDATE data_asset SET status = ?, description = ? WHERE name = ?";
        jdbcTemplate.update(updateSql, "PUBLISHED", "Updated description", "Customer Table");

        // Then - Verify Update
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM data_asset WHERE name = ?", String.class, "Customer Table");
        assertThat(status).isEqualTo("PUBLISHED");

        // When - Delete Asset
        jdbcTemplate.update("DELETE FROM data_asset WHERE name = ?", "Customer Table");
        jdbcTemplate.update("DELETE FROM datasource WHERE name = ?", "Asset Test DB");

        // Then - Verify Deletion
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM data_asset WHERE name = ?", Integer.class, "Customer Table");
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("测试事务提交")
    void testTransactionCommit() {
        // Given
        String insertSql = "INSERT INTO datasource (name, type, connection_url, username, status) VALUES (?, ?, ?, ?, ?)";

        // When - 在同一事务中插入多条记录
        jdbcTemplate.update(insertSql, "TX Test 1", "MYSQL", "jdbc:mysql://test1", "user1", "ACTIVE");
        jdbcTemplate.update(insertSql, "TX Test 2", "POSTGRESQL", "jdbc:postgresql://test2", "user2", "ACTIVE");

        // Then - 验证两条记录都已插入
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM datasource WHERE name LIKE ?", Integer.class, "TX Test %");
        assertThat(count).isEqualTo(2);

        // Clean up
        jdbcTemplate.update("DELETE FROM datasource WHERE name LIKE ?", "TX Test %");
    }

    @Test
    @DisplayName("测试批量插入操作")
    void testBatchInsertOperations() {
        // Given
        String insertSql = "INSERT INTO datasource (name, type, connection_url, username, status) VALUES (?, ?, ?, ?, ?)";

        List<Object[]> batchArgs = Arrays.asList(
                new Object[]{"Batch 1", "MYSQL", "jdbc:mysql://1", "user1", "ACTIVE"},
                new Object[]{"Batch 2", "POSTGRESQL", "jdbc:postgresql://2", "user2", "ACTIVE"},
                new Object[]{"Batch 3", "ORACLE", "jdbc:oracle://3", "user3", "ACTIVE"},
                new Object[]{"Batch 4", "SQLSERVER", "jdbc:sqlserver://4", "user4", "ACTIVE"},
                new Object[]{"Batch 5", "MONGODB", "mongodb://5", "user5", "ACTIVE"}
        );

        // When
        int[] updateCounts = jdbcTemplate.batchUpdate(insertSql, batchArgs);

        // Then
        assertThat(updateCounts).hasSize(5);
        assertThat(updateCounts).allMatch(count -> count == 1);

        // Verify
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM datasource WHERE name LIKE ?", Integer.class, "Batch %");
        assertThat(count).isEqualTo(5);

        // Clean up
        jdbcTemplate.update("DELETE FROM datasource WHERE name LIKE ?", "Batch %");
    }

    @Test
    @DisplayName("测试关联查询 - 数据源与资产")
    void testJoinQuery_DatasourceAndAssets() {
        // Given - 创建数据源和关联的资产
        String insertDatasource = "INSERT INTO datasource (name, type, connection_url, username, status) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertDatasource, "Join Test DB", "MYSQL", "jdbc:mysql://join", "user", "ACTIVE");

        Long datasourceId = jdbcTemplate.queryForObject(
                "SELECT id FROM datasource WHERE name = ?", Long.class, "Join Test DB");

        String insertAsset = "INSERT INTO data_asset (name, type, datasource_id, schema_name, table_name, owner, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertAsset, "Table A", "TABLE", datasourceId, "public", "table_a", "admin", "ACTIVE");
        jdbcTemplate.update(insertAsset, "Table B", "TABLE", datasourceId, "public", "table_b", "admin", "ACTIVE");
        jdbcTemplate.update(insertAsset, "Table C", "VIEW", datasourceId, "public", "view_c", "admin", "ACTIVE");

        // When - 执行关联查询
        String joinSql = """
            SELECT d.name as datasource_name, d.type, COUNT(a.id) as asset_count
            FROM datasource d
            LEFT JOIN data_asset a ON d.id = a.datasource_id
            WHERE d.name = ?
            GROUP BY d.id, d.name, d.type
            """;

        List<AssetCountResult> results = jdbcTemplate.query(joinSql, (rs, rowNum) -> {
            AssetCountResult result = new AssetCountResult();
            result.setDatasourceName(rs.getString("datasource_name"));
            result.setType(rs.getString("type"));
            result.setAssetCount(rs.getLong("asset_count"));
            return result;
        }, "Join Test DB");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAssetCount()).isEqualTo(3);

        // Clean up
        jdbcTemplate.update("DELETE FROM data_asset WHERE datasource_id = ?", datasourceId);
        jdbcTemplate.update("DELETE FROM datasource WHERE id = ?", datasourceId);
    }

    @Test
    @DisplayName("测试质量规则与检测结果关联")
    void testQualityRuleAndResultJoin() {
        // Given - 创建数据源、资产、质量规则
        jdbcTemplate.update(
                "INSERT INTO datasource (name, type, connection_url, username, status) VALUES (?, ?, ?, ?, ?)",
                "Quality DB", "POSTGRESQL", "jdbc:postgresql://quality", "user", "ACTIVE");

        Long datasourceId = jdbcTemplate.queryForObject(
                "SELECT id FROM datasource WHERE name = ?", Long.class, "Quality DB");

        jdbcTemplate.update(
                "INSERT INTO data_asset (name, type, datasource_id, schema_name, table_name, owner, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                "Quality Table", "TABLE", datasourceId, "public", "quality_table", "admin", "ACTIVE");

        Long assetId = jdbcTemplate.queryForObject(
                "SELECT id FROM data_asset WHERE name = ?", Long.class, "Quality Table");

        jdbcTemplate.update(
                "INSERT INTO quality_rule (name, rule_type, asset_id, expression, threshold, status) VALUES (?, ?, ?, ?, ?, ?)",
                "Not Null Check", "NOT_NULL", assetId, "column IS NOT NULL", new BigDecimal("95.00"), "ACTIVE");

        Long ruleId = jdbcTemplate.queryForObject(
                "SELECT id FROM quality_rule WHERE name = ?", Long.class, "Not Null Check");

        // When - 插入检测结果
        jdbcTemplate.update(
                "INSERT INTO quality_check_result (rule_id, asset_id, total_count, error_count, error_rate, status) VALUES (?, ?, ?, ?, ?, ?)",
                ruleId, assetId, 1000, 20, new BigDecimal("2.00"), "FAILED");

        jdbcTemplate.update(
                "INSERT INTO quality_check_result (rule_id, asset_id, total_count, error_count, error_rate, status) VALUES (?, ?, ?, ?, ?, ?)",
                ruleId, assetId, 1000, 5, new BigDecimal("0.50"), "PASSED");

        // Then - 查询关联数据
        String querySql = """
            SELECT r.name as rule_name, r.rule_type, res.status, res.error_rate
            FROM quality_rule r
            JOIN quality_check_result res ON r.id = res.rule_id
            WHERE r.id = ?
            ORDER BY res.check_time DESC
            """;

        List<QualityResult> results = jdbcTemplate.query(querySql, (rs, rowNum) -> {
            QualityResult result = new QualityResult();
            result.setRuleName(rs.getString("rule_name"));
            result.setRuleType(rs.getString("rule_type"));
            result.setStatus(rs.getString("status"));
            result.setErrorRate(rs.getBigDecimal("error_rate"));
            return result;
        }, ruleId);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getStatus()).isEqualTo("PASSED");
        assertThat(results.get(1).getStatus()).isEqualTo("FAILED");

        // Clean up
        jdbcTemplate.update("DELETE FROM quality_check_result WHERE rule_id = ?", ruleId);
        jdbcTemplate.update("DELETE FROM quality_rule WHERE id = ?", ruleId);
        jdbcTemplate.update("DELETE FROM data_asset WHERE id = ?", assetId);
        jdbcTemplate.update("DELETE FROM datasource WHERE id = ?", datasourceId);
    }

    @Test
    @DisplayName("测试索引性能")
    void testIndexPerformance() {
        // Given - 插入大量数据
        String insertSql = "INSERT INTO datasource (name, type, connection_url, username, status) VALUES (?, ?, ?, ?, ?)";

        for (int i = 0; i < 100; i++) {
            jdbcTemplate.update(insertSql,
                    "Perf Test " + i,
                    i % 2 == 0 ? "MYSQL" : "POSTGRESQL",
                    "jdbc://test" + i,
                    "user" + i,
                    "ACTIVE");
        }

        // When - 测试带索引的查询性能
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM datasource WHERE name = ?",
                    Integer.class, "Perf Test " + i);
        }
        long endTime = System.currentTimeMillis();

        // Then - 验证查询在合理时间内完成（100次查询应在1秒内完成）
        assertThat(endTime - startTime).isLessThan(1000);

        // Clean up
        jdbcTemplate.update("DELETE FROM datasource WHERE name LIKE ?", "Perf Test %");
    }

    @Test
    @DisplayName("测试复杂聚合查询")
    void testComplexAggregationQuery() {
        // Given - 创建测试数据
        jdbcTemplate.update("INSERT INTO datasource (name, type, connection_url, username, status) VALUES (?, ?, ?, ?, ?)",
                "Agg DB 1", "MYSQL", "jdbc:mysql://1", "user1", "ACTIVE");
        jdbcTemplate.update("INSERT INTO datasource (name, type, connection_url, username, status) VALUES (?, ?, ?, ?, ?)",
                "Agg DB 2", "POSTGRESQL", "jdbc:postgresql://2", "user2", "ACTIVE");
        jdbcTemplate.update("INSERT INTO datasource (name, type, connection_url, username, status) VALUES (?, ?, ?, ?, ?)",
                "Agg DB 3", "MYSQL", "jdbc:mysql://3", "user3", "INACTIVE");

        // When - 执行聚合查询
        String aggSql = """
            SELECT 
                type,
                status,
                COUNT(*) as count,
                MIN(created_at) as earliest,
                MAX(created_at) as latest
            FROM datasource
            WHERE name LIKE ?
            GROUP BY type, status
            ORDER BY type, status
            """;

        List<AggregationResult> results = jdbcTemplate.query(aggSql, (rs, rowNum) -> {
            AggregationResult result = new AggregationResult();
            result.setType(rs.getString("type"));
            result.setStatus(rs.getString("status"));
            result.setCount(rs.getLong("count"));
            result.setEarliest(rs.getTimestamp("earliest").toLocalDateTime());
            result.setLatest(rs.getTimestamp("latest").toLocalDateTime());
            return result;
        }, "Agg DB %");

        // Then
        assertThat(results).hasSizeGreaterThanOrEqualTo(2);

        // Verify counts
        long totalCount = results.stream().mapToLong(AggregationResult::getCount).sum();
        assertThat(totalCount).isEqualTo(3);

        // Clean up
        jdbcTemplate.update("DELETE FROM datasource WHERE name LIKE ?", "Agg DB %");
    }

    // Helper classes for query results
    static class AssetCountResult {
        private String datasourceName;
        private String type;
        private long assetCount;

        public String getDatasourceName() { return datasourceName; }
        public void setDatasourceName(String datasourceName) { this.datasourceName = datasourceName; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public long getAssetCount() { return assetCount; }
        public void setAssetCount(long assetCount) { this.assetCount = assetCount; }
    }

    static class QualityResult {
        private String ruleName;
        private String ruleType;
        private String status;
        private BigDecimal errorRate;

        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getRuleType() { return ruleType; }
        public void setRuleType(String ruleType) { this.ruleType = ruleType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getErrorRate() { return errorRate; }
        public void setErrorRate(BigDecimal errorRate) { this.errorRate = errorRate; }
    }

    static class AggregationResult {
        private String type;
        private String status;
        private long count;
        private LocalDateTime earliest;
        private LocalDateTime latest;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
        public LocalDateTime getEarliest() { return earliest; }
        public void setEarliest(LocalDateTime earliest) { this.earliest = earliest; }
        public LocalDateTime getLatest() { return latest; }
        public void setLatest(LocalDateTime latest) { this.latest = latest; }
    }
}
