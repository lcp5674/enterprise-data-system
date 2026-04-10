package com.enterprise.edams.integration.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Neo4j图数据库集成测试
 * 测试任务: INT-NEO4J-001
 */
@Testcontainers
@DataNeo4jTest
@ActiveProfiles("test")
@DisplayName("Neo4j图数据库集成测试")
class Neo4jIntegrationTest {

    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>(
            DockerImageName.parse("neo4j:5.18-community"))
            .withAdminPassword("password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
    }

    @Autowired
    private Driver driver;

    @BeforeEach
    void setUp() {
        // 清理所有节点和关系
        try (Session session = driver.session()) {
            session.run("MATCH (n) DETACH DELETE n");
        }
    }

    @Test
    @DisplayName("测试Neo4j连接")
    void testNeo4jConnection() {
        try (Session session = driver.session()) {
            var result = session.run("RETURN 1 as num");
            assertThat(result.single().get("num").asInt()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("测试节点创建和查询")
    void testNodeCreationAndQuery() {
        // Given - 创建数据资产节点
        try (Session session = driver.session()) {
            session.run("""
                CREATE (a:DataAsset {
                    id: 'asset-001',
                    name: 'Customer Table',
                    type: 'TABLE',
                    schema: 'public',
                    owner: 'admin'
                })
                RETURN a
                """);
        }

        // When - 查询节点
        try (Session session = driver.session()) {
            var result = session.run("""
                MATCH (a:DataAsset {id: 'asset-001'})
                RETURN a.name as name, a.type as type, a.owner as owner
                """);

            var record = result.single();

            // Then
            assertThat(record.get("name").asString()).isEqualTo("Customer Table");
            assertThat(record.get("type").asString()).isEqualTo("TABLE");
            assertThat(record.get("owner").asString()).isEqualTo("admin");
        }
    }

    @Test
    @DisplayName("测试关系创建和查询")
    void testRelationshipCreationAndQuery() {
        // Given - 创建节点和关系
        try (Session session = driver.session()) {
            session.run("""
                CREATE (source:DataAsset {id: 'asset-001', name: 'Orders'})
                CREATE (target:DataAsset {id: 'asset-002', name: 'Customers'})
                CREATE (source)-[r:DEPENDS_ON {
                    relationType: 'FOREIGN_KEY',
                    createdAt: datetime()
                }]->(target)
                RETURN source, target, r
                """);
        }

        // When - 查询关系
        try (Session session = driver.session()) {
            var result = session.run("""
                MATCH (source:DataAsset)-[r:DEPENDS_ON]->(target:DataAsset)
                RETURN source.name as sourceName, target.name as targetName, r.relationType as type
                """);

            var record = result.single();

            // Then
            assertThat(record.get("sourceName").asString()).isEqualTo("Orders");
            assertThat(record.get("targetName").asString()).isEqualTo("Customers");
            assertThat(record.get("type").asString()).isEqualTo("FOREIGN_KEY");
        }
    }

    @Test
    @DisplayName("测试血缘关系 - 上游追溯")
    void testLineageUpstreamQuery() {
        // Given - 创建血缘关系链
        // asset-d -> asset-c -> asset-b -> asset-a
        try (Session session = driver.session()) {
            session.run("""
                CREATE (a:DataAsset {id: 'asset-a', name: 'Source Table'})
                CREATE (b:DataAsset {id: 'asset-b', name: 'Staging Table'})
                CREATE (c:DataAsset {id: 'asset-c', name: 'Warehouse Table'})
                CREATE (d:DataAsset {id: 'asset-d', name: 'Report View'})
                CREATE (b)-[:DEPENDS_ON {relationType: 'ETL'}]->(a)
                CREATE (c)-[:DEPENDS_ON {relationType: 'ETL'}]->(b)
                CREATE (d)-[:DEPENDS_ON {relationType: 'VIEW'}]->(c)
                """);
        }

        // When - 查询asset-d的上游依赖
        try (Session session = driver.session()) {
            var result = session.run("""
                MATCH path = (target:DataAsset {id: 'asset-d'})-[:DEPENDS_ON*]->(source:DataAsset)
                RETURN source.name as sourceName, length(path) as depth
                ORDER BY depth
                """);

            List<String> upstreamAssets = result.list(record -> record.get("sourceName").asString());

            // Then
            assertThat(upstreamAssets).containsExactly("Warehouse Table", "Staging Table", "Source Table");
        }
    }

    @Test
    @DisplayName("测试血缘关系 - 下游追溯")
    void testLineageDownstreamQuery() {
        // Given - 创建血缘关系链
        try (Session session = driver.session()) {
            session.run("""
                CREATE (a:DataAsset {id: 'asset-a', name: 'Source Table'})
                CREATE (b:DataAsset {id: 'asset-b', name: 'Staging Table'})
                CREATE (c:DataAsset {id: 'asset-c', name: 'Warehouse Table'})
                CREATE (d:DataAsset {id: 'asset-d', name: 'Report View'})
                CREATE (e:DataAsset {id: 'asset-e', name: 'Dashboard'})
                CREATE (b)-[:DEPENDS_ON]->(a)
                CREATE (c)-[:DEPENDS_ON]->(b)
                CREATE (d)-[:DEPENDS_ON]->(c)
                CREATE (e)-[:DEPENDS_ON]->(c)
                """);
        }

        // When - 查询asset-a的下游影响
        try (Session session = driver.session()) {
            var result = session.run("""
                MATCH path = (source:DataAsset {id: 'asset-a'})<-[:DEPENDS_ON*]-(target:DataAsset)
                RETURN target.name as targetName, length(path) as depth
                ORDER BY depth
                """);

            List<String> downstreamAssets = result.list(record -> record.get("targetName").asString());

            // Then
            assertThat(downstreamAssets).contains("Staging Table", "Warehouse Table", "Report View", "Dashboard");
        }
    }

    @Test
    @DisplayName("测试循环依赖检测")
    void testCircularDependencyDetection() {
        // Given - 创建循环依赖
        try (Session session = driver.session()) {
            session.run("""
                CREATE (a:DataAsset {id: 'asset-a', name: 'Table A'})
                CREATE (b:DataAsset {id: 'asset-b', name: 'Table B'})
                CREATE (c:DataAsset {id: 'asset-c', name: 'Table C'})
                CREATE (a)-[:DEPENDS_ON]->(b)
                CREATE (b)-[:DEPENDS_ON]->(c)
                CREATE (c)-[:DEPENDS_ON]->(a)
                """);
        }

        // When - 检测循环
        try (Session session = driver.session()) {
            var result = session.run("""
                MATCH path = (a:DataAsset)-[:DEPENDS_ON*]->(a)
                RETURN a.name as nodeName, length(path) as cycleLength
                LIMIT 1
                """);

            // Then - 应该检测到循环
            assertTrue(result.hasNext(), "应该检测到循环依赖");
            var record = result.single();
            assertThat(record.get("cycleLength").asInt()).isGreaterThanOrEqualTo(3);
        }
    }

    @Test
    @DisplayName("测试最短路径查询")
    void testShortestPathQuery() {
        // Given - 创建复杂关系网络
        try (Session session = driver.session()) {
            session.run("""
                CREATE (a:DataAsset {id: 'asset-a', name: 'Source'})
                CREATE (b:DataAsset {id: 'asset-b', name: 'Intermediate 1'})
                CREATE (c:DataAsset {id: 'asset-c', name: 'Intermediate 2'})
                CREATE (d:DataAsset {id: 'asset-d', name: 'Target'})
                CREATE (a)-[:DEPENDS_ON]->(b)
                CREATE (b)-[:DEPENDS_ON]->(c)
                CREATE (c)-[:DEPENDS_ON]->(d)
                CREATE (a)-[:DEPENDS_ON {type: 'DIRECT'}]->(d)
                """);
        }

        // When - 查询最短路径
        try (Session session = driver.session()) {
            var result = session.run("""
                MATCH (source:DataAsset {id: 'asset-a'}), (target:DataAsset {id: 'asset-d'})
                MATCH path = shortestPath((source)-[:DEPENDS_ON*]->(target))
                RETURN length(path) as pathLength, [n in nodes(path) | n.name] as nodeNames
                """);

            var record = result.single();

            // Then - 最短路径应该是直接连接
            assertThat(record.get("pathLength").asInt()).isEqualTo(1);
            List<String> nodeNames = record.get("nodeNames").asList(Values.ofString());
            assertThat(nodeNames).containsExactly("Source", "Target");
        }
    }

    @Test
    @DisplayName("测试节点属性更新")
    void testNodePropertyUpdate() {
        // Given
        try (Session session = driver.session()) {
            session.run("""
                CREATE (a:DataAsset {id: 'asset-001', name: 'Old Name', status: 'DRAFT'})
                """);
        }

        // When - 更新属性
        try (Session session = driver.session()) {
            session.run("""
                MATCH (a:DataAsset {id: 'asset-001'})
                SET a.name = 'New Name',
                    a.status = 'PUBLISHED',
                    a.updatedAt = datetime()
                RETURN a
                """);
        }

        // Then - 验证更新
        try (Session session = driver.session()) {
            var result = session.run("""
                MATCH (a:DataAsset {id: 'asset-001'})
                RETURN a.name as name, a.status as status
                """);

            var record = result.single();
            assertThat(record.get("name").asString()).isEqualTo("New Name");
            assertThat(record.get("status").asString()).isEqualTo("PUBLISHED");
        }
    }

    @Test
    @DisplayName("测试关系删除")
    void testRelationshipDeletion() {
        // Given
        try (Session session = driver.session()) {
            session.run("""
                CREATE (a:DataAsset {id: 'asset-a', name: 'A'})
                CREATE (b:DataAsset {id: 'asset-b', name: 'B'})
                CREATE (a)-[r:DEPENDS_ON]->(b)
                """);
        }

        // When - 删除关系
        try (Session session = driver.session()) {
            session.run("""
                MATCH (a:DataAsset {id: 'asset-a'})-[r:DEPENDS_ON]->(b:DataAsset {id: 'asset-b'})
                DELETE r
                """);
        }

        // Then - 验证关系已删除
        try (Session session = driver.session()) {
            var result = session.run("""
                MATCH (a:DataAsset {id: 'asset-a'})-[r:DEPENDS_ON]->(b:DataAsset {id: 'asset-b'})
                RETURN count(r) as relationCount
                """);

            assertThat(result.single().get("relationCount").asInt()).isZero();
        }
    }

    @Test
    @DisplayName("测试批量节点创建")
    void testBatchNodeCreation() {
        // When - 批量创建节点
        try (Session session = driver.session()) {
            for (int i = 1; i <= 100; i++) {
                session.run("""
                    CREATE (a:DataAsset {
                        id: $id,
                        name: $name,
                        type: 'TABLE',
                        seq: $seq
                    })
                    """,
                    Map.of(
                        "id", "asset-" + String.format("%03d", i),
                        "name", "Table " + i,
                        "seq", i
                    )
                );
            }
        }

        // Then - 验证批量创建
        try (Session session = driver.session()) {
            var result = session.run("MATCH (a:DataAsset) RETURN count(a) as count");
            assertThat(result.single().get("count").asInt()).isEqualTo(100);
        }
    }

    @Test
    @DisplayName("测试复杂图遍历")
    void testComplexGraphTraversal() {
        // Given - 创建复杂图结构
        try (Session session = driver.session()) {
            session.run("""
                // 创建数据源层
                CREATE (ds1:DataSource {id: 'ds-1', name: 'MySQL Production', type: 'MYSQL'})
                CREATE (ds2:DataSource {id: 'ds-2', name: 'PostgreSQL DW', type: 'POSTGRESQL'})
                
                // 创建数据资产层
                CREATE (a1:DataAsset {id: 'asset-1', name: 'Orders Raw', layer: 'ODS'})
                CREATE (a2:DataAsset {id: 'asset-2', name: 'Customers Raw', layer: 'ODS'})
                CREATE (a3:DataAsset {id: 'asset-3', name: 'Orders Clean', layer: 'DWD'})
                CREATE (a4:DataAsset {id: 'asset-4', name: 'Customer Orders', layer: 'DWS'})
                CREATE (a5:DataAsset {id: 'asset-5', name: 'Sales Report', layer: 'ADS'})
                
                // 创建关系
                CREATE (ds1)-[:CONTAINS]->(a1)
                CREATE (ds1)-[:CONTAINS]->(a2)
                CREATE (a1)-[:TRANSFORMS_TO]->(a3)
                CREATE (a2)-[:JOINS_WITH]->(a4)
                CREATE (a3)-[:AGGREGATES_TO]->(a4)
                CREATE (a4)-[:GENERATES]->(a5)
                CREATE (ds2)-[:CONTAINS]->(a3)
                CREATE (ds2)-[:CONTAINS]->(a4)
                CREATE (ds2)-[:CONTAINS]->(a5)
                """);
        }

        // When - 查询从数据源到报表的完整链路
        try (Session session = driver.session()) {
            var result = session.run("""
                MATCH (ds:DataSource)-[:CONTAINS|TRANSFORMS_TO|JOINS_WITH|AGGREGATES_TO|GENERATES*]->(report:DataAsset {layer: 'ADS'})
                WITH ds, report, 
                     [n in nodes(path) WHERE n:DataAsset | n.name] as assetNames
                RETURN ds.name as sourceName, report.name as reportName, assetNames
                """);

            var records = result.list();

            // Then
            assertThat(records).isNotEmpty();
            assertThat(records.get(0).get("reportName").asString()).isEqualTo("Sales Report");
        }
    }

    @Test
    @DisplayName("测试影响力分析")
    void testImpactAnalysis() {
        // Given
        try (Session session = driver.session()) {
            session.run("""
                CREATE (source:DataAsset {id: 'source', name: 'Core Table', criticality: 'HIGH'})
                CREATE (a1:DataAsset {id: 'a1', name: 'Report 1'})
                CREATE (a2:DataAsset {id: 'a2', name: 'Report 2'})
                CREATE (a3:DataAsset {id: 'a3', name: 'Dashboard 1'})
                CREATE (a4:DataAsset {id: 'a4', name: 'API Endpoint'})
                CREATE (source)-[:DEPENDS_ON {impact: 'DIRECT'}]->(a1)
                CREATE (source)-[:DEPENDS_ON {impact: 'DIRECT'}]->(a2)
                CREATE (a1)-[:DEPENDS_ON {impact: 'INDIRECT'}]->(a3)
                CREATE (a2)-[:DEPENDS_ON {impact: 'INDIRECT'}]->(a4)
                """);
        }

        // When - 分析source表的影响范围
        try (Session session = driver.session()) {
            var result = session.run("""
                MATCH (source:DataAsset {id: 'source'})-[:DEPENDS_ON*]->(affected:DataAsset)
                RETURN affected.name as name,
                       affected.criticality as criticality,
                       min(length(shortestPath((source)-[:DEPENDS_ON*]->(affected)))) as distance
                ORDER BY distance
                """);

            List<String> affectedAssets = result.list(record -> record.get("name").asString());

            // Then
            assertThat(affectedAssets).containsExactly("Report 1", "Report 2", "Dashboard 1", "API Endpoint");
        }
    }
}
