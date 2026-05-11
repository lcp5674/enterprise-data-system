package com.enterprise.dataplatform.lineage.service;

import com.enterprise.dataplatform.lineage.domain.entity.LineageHistory;
import com.enterprise.dataplatform.lineage.domain.entity.LineageRelation;
import com.enterprise.dataplatform.lineage.domain.entity.LineageSnapshot;
import com.enterprise.dataplatform.lineage.domain.enums.LineageDirection;
import com.enterprise.dataplatform.lineage.domain.enums.LineageType;
import com.enterprise.dataplatform.lineage.domain.event.GraphNode;
import com.enterprise.dataplatform.lineage.dto.request.CreateLineageRequest;
import com.enterprise.dataplatform.lineage.dto.request.LineageParseRequest;
import com.enterprise.dataplatform.lineage.dto.request.LineageQueryRequest;
import com.enterprise.dataplatform.lineage.dto.response.ImpactAnalysisResponse;
import com.enterprise.dataplatform.lineage.dto.response.LineageGraphResponse;
import com.enterprise.dataplatform.lineage.parser.SqlLineageParser;
import com.enterprise.dataplatform.lineage.repository.LineageGraphRepository;
import com.enterprise.dataplatform.lineage.repository.LineageHistoryRepository;
import com.enterprise.dataplatform.lineage.repository.LineageRelationRepository;
import com.enterprise.dataplatform.lineage.repository.LineageSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("血缘服务测试")
class LineageServiceTest {

    @Mock
    private LineageRelationRepository lineageRelationRepository;

    @Mock
    private LineageHistoryRepository lineageHistoryRepository;

    @Mock
    private LineageSnapshotRepository lineageSnapshotRepository;

    @Mock
    private LineageGraphRepository lineageGraphRepository;

    @Mock
    private SqlLineageParser sqlLineageParser;

    @InjectMocks
    private LineageService lineageService;

    private LineageRelation testRelation;
    private CreateLineageRequest testRequest;

    @BeforeEach
    void setUp() {
        testRelation = LineageRelation.builder()
                .id("test-id-123")
                .sourceAssetId("ASSET-001")
                .targetAssetId("ASSET-002")
                .lineageType(LineageType.MANUAL)
                .transformDesc("测试转换")
                .confidence(100.0)
                .isVerified(false)
                .isDeleted(false)
                .createdBy("admin")
                .createdTime(LocalDateTime.now())
                .build();

        testRequest = CreateLineageRequest.builder()
                .sourceAssetId("ASSET-001")
                .targetAssetId("ASSET-002")
                .lineageType(LineageType.MANUAL)
                .transformDesc("测试转换")
                .confidence(100.0)
                .build();
    }

    private LineageRelation createTestRelation(String sourceId, String targetId) {
        return LineageRelation.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .sourceAssetId(sourceId)
                .targetAssetId(targetId)
                .lineageType(LineageType.MANUAL)
                .confidence(100.0)
                .isDeleted(false)
                .createdBy("admin")
                .build();
    }

    private List<GraphNode> createTestGraphNodes(int count) {
        List<GraphNode> nodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            nodes.add(GraphNode.builder()
                    .assetId("ASSET-" + i)
                    .name("资产" + i)
                    .type("TABLE")
                    .build());
        }
        return nodes;
    }

    private List<LineageRelation> createTestRelations(int count) {
        List<LineageRelation> relations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            relations.add(createTestRelation("SOURCE-" + i, "TARGET-" + i));
        }
        return relations;
    }

    @Nested
    @DisplayName("血缘关系创建测试")
    class LineageCreationTests {

        @Test
        @DisplayName("应成功创建血缘关系")
        void shouldCreateLineageSuccessfully() {
            when(lineageRelationRepository.findBySourceAssetIdAndTargetAssetIdAndIsDeletedFalse(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(lineageRelationRepository.save(any(LineageRelation.class))).thenReturn(testRelation);
            doNothing().when(lineageGraphRepository).createLineageRelation(any(), any(), any(), any());
            when(lineageHistoryRepository.save(any(LineageHistory.class))).thenReturn(new LineageHistory());

            LineageRelation result = lineageService.createLineage(testRequest, "admin");

            assertThat(result).isNotNull();
            assertThat(result.getSourceAssetId()).isEqualTo("ASSET-001");
            assertThat(result.getTargetAssetId()).isEqualTo("ASSET-002");
            verify(lineageRelationRepository, times(1)).save(any(LineageRelation.class));
            verify(lineageGraphRepository, times(1)).createLineageRelation(any(), any(), any(), any());
        }

        @Test
        @DisplayName("当血缘关系已存在时应抛出异常")
        void shouldThrowExceptionWhenLineageExists() {
            when(lineageRelationRepository.findBySourceAssetIdAndTargetAssetIdAndIsDeletedFalse(anyString(), anyString()))
                    .thenReturn(Optional.of(testRelation));

            assertThatThrownBy(() -> lineageService.createLineage(testRequest, "admin"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("创建血缘关系时默认类型应为MANUAL")
        void shouldSetDefaultLineageType() {
            CreateLineageRequest requestWithoutType = CreateLineageRequest.builder()
                    .sourceAssetId("SRC")
                    .targetAssetId("TGT")
                    .build();

            when(lineageRelationRepository.findBySourceAssetIdAndTargetAssetIdAndIsDeletedFalse(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(lineageRelationRepository.save(any(LineageRelation.class))).thenAnswer(inv -> {
                LineageRelation saved = inv.getArgument(0);
                return saved;
            });
            doNothing().when(lineageGraphRepository).createLineageRelation(any(), any(), any(), any());
            when(lineageHistoryRepository.save(any(LineageHistory.class))).thenReturn(new LineageHistory());

            lineageService.createLineage(requestWithoutType, "admin");

            verify(lineageRelationRepository).save(argThat(rel -> rel.getLineageType() == LineageType.MANUAL));
        }

        @Test
        @DisplayName("创建血缘关系时应设置默认置信度为100")
        void shouldSetDefaultConfidence() {
            CreateLineageRequest requestWithoutConfidence = CreateLineageRequest.builder()
                    .sourceAssetId("SRC")
                    .targetAssetId("TGT")
                    .build();

            when(lineageRelationRepository.findBySourceAssetIdAndTargetAssetIdAndIsDeletedFalse(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(lineageRelationRepository.save(any(LineageRelation.class))).thenAnswer(inv -> {
                LineageRelation saved = inv.getArgument(0);
                return saved;
            });
            doNothing().when(lineageGraphRepository).createLineageRelation(any(), any(), any(), any());
            when(lineageHistoryRepository.save(any(LineageHistory.class))).thenReturn(new LineageHistory());

            lineageService.createLineage(requestWithoutConfidence, "admin");

            verify(lineageRelationRepository).save(argThat(rel -> rel.getConfidence() == 100.0));
        }
    }

    @Nested
    @DisplayName("血缘关系删除测试")
    class LineageDeletionTests {

        @Test
        @DisplayName("应成功删除血缘关系")
        void shouldDeleteLineageSuccessfully() {
            when(lineageRelationRepository.findById("test-id-123")).thenReturn(Optional.of(testRelation));
            when(lineageRelationRepository.save(any(LineageRelation.class))).thenReturn(testRelation);
            doNothing().when(lineageGraphRepository).deleteLineageRelation(anyString(), anyString());
            when(lineageHistoryRepository.save(any(LineageHistory.class))).thenReturn(new LineageHistory());

            lineageService.deleteLineage("test-id-123", "admin");

            verify(lineageRelationRepository, times(1)).save(argThat(rel -> rel.getIsDeleted()));
            verify(lineageGraphRepository, times(1)).deleteLineageRelation("ASSET-001", "ASSET-002");
        }

        @Test
        @DisplayName("删除不存在的血缘关系应抛出异常")
        void shouldThrowExceptionWhenLineageNotFound() {
            when(lineageRelationRepository.findById("non-existent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lineageService.deleteLineage("non-existent", "admin"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("删除已删除的血缘关系应抛出异常")
        void shouldThrowExceptionWhenLineageAlreadyDeleted() {
            testRelation.setIsDeleted(true);
            when(lineageRelationRepository.findById("test-id-123")).thenReturn(Optional.of(testRelation));

            assertThatThrownBy(() -> lineageService.deleteLineage("test-id-123", "admin"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("already deleted");
        }
    }

    @Nested
    @DisplayName("血缘查询测试")
    class LineageQueryTests {

        @Test
        @DisplayName("应返回正确的血缘图 - 上游查询")
        void shouldReturnCorrectLineageGraph_Upstream() {
            LineageQueryRequest request = LineageQueryRequest.builder()
                    .assetId("ASSET-CENTER")
                    .direction(LineageDirection.UPSTREAM)
                    .depth(3)
                    .build();

            List<GraphNode> upstreamNodes = List.of(
                    GraphNode.builder().assetId("ASSET-001").name("上游资产1").type("TABLE").build(),
                    GraphNode.builder().assetId("ASSET-002").name("上游资产2").type("TABLE").build()
            );
            when(lineageGraphRepository.getUpstreamLineage(anyString(), anyInt())).thenReturn(upstreamNodes);
            when(lineageRelationRepository.findByAssetId(anyString())).thenReturn(List.of(testRelation));
            when(lineageRelationRepository.countByAssetId(anyString())).thenReturn(2L);
            when(lineageGraphRepository.hasCircularDependency(anyString())).thenReturn(false);

            LineageGraphResponse response = lineageService.queryLineageGraph(request);

            assertThat(response).isNotNull();
            assertThat(response.getNodes()).isNotEmpty();
            assertThat(response.getStatistics()).isNotNull();
            assertThat(response.getStatistics().getUpstreamCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("应返回正确的血缘图 - 下游查询")
        void shouldReturnCorrectLineageGraph_Downstream() {
            LineageQueryRequest request = LineageQueryRequest.builder()
                    .assetId("ASSET-CENTER")
                    .direction(LineageDirection.DOWNSTREAM)
                    .depth(3)
                    .build();

            List<GraphNode> downstreamNodes = List.of(
                    GraphNode.builder().assetId("ASSET-003").name("下游资产1").type("TABLE").build()
            );
            when(lineageGraphRepository.getDownstreamLineage(anyString(), anyInt())).thenReturn(downstreamNodes);
            when(lineageRelationRepository.findByAssetId(anyString())).thenReturn(List.of(testRelation));
            when(lineageRelationRepository.countByAssetId(anyString())).thenReturn(1L);
            when(lineageGraphRepository.hasCircularDependency(anyString())).thenReturn(false);

            LineageGraphResponse response = lineageService.queryLineageGraph(request);

            assertThat(response).isNotNull();
            assertThat(response.getNodes()).isNotEmpty();
        }

        @Test
        @DisplayName("应返回正确的血缘图 - 双向查询")
        void shouldReturnCorrectLineageGraph_Both() {
            LineageQueryRequest request = LineageQueryRequest.builder()
                    .assetId("ASSET-CENTER")
                    .direction(LineageDirection.BOTH)
                    .depth(5)
                    .build();

            List<GraphNode> nodes = List.of(
                    GraphNode.builder().assetId("ASSET-001").name("资产1").type("TABLE").build(),
                    GraphNode.builder().assetId("ASSET-002").name("资产2").type("TABLE").build()
            );
            when(lineageGraphRepository.getUpstreamLineage(anyString(), anyInt())).thenReturn(nodes);
            when(lineageGraphRepository.getDownstreamLineage(anyString(), anyInt())).thenReturn(nodes);
            when(lineageRelationRepository.findByAssetId(anyString())).thenReturn(List.of(testRelation));
            when(lineageRelationRepository.countByAssetId(anyString())).thenReturn(2L);
            when(lineageGraphRepository.hasCircularDependency(anyString())).thenReturn(false);

            LineageGraphResponse response = lineageService.queryLineageGraph(request);

            assertThat(response).isNotNull();
            assertThat(response.getNodes()).isNotEmpty();
            assertThat(response.getEdges()).isNotEmpty();
        }

        @Test
        @DisplayName("应正确限制查询深度不超过10")
        void shouldLimitDepthToMaximum() {
            LineageQueryRequest request = LineageQueryRequest.builder()
                    .assetId("ASSET-CENTER")
                    .direction(LineageDirection.BOTH)
                    .depth(20)
                    .build();

            when(lineageGraphRepository.getUpstreamLineage(anyString(), eq(10))).thenReturn(List.of());
            when(lineageGraphRepository.getDownstreamLineage(anyString(), eq(10))).thenReturn(List.of());
            when(lineageRelationRepository.findByAssetId(anyString())).thenReturn(List.of());
            when(lineageRelationRepository.countByAssetId(anyString())).thenReturn(0L);
            when(lineageGraphRepository.hasCircularDependency(anyString())).thenReturn(false);

            LineageGraphResponse response = lineageService.queryLineageGraph(request);

            assertThat(response).isNotNull();
            assertThat(response.getStatistics().getMaxDepth()).isEqualTo(10);
        }

        @Test
        @DisplayName("应检测循环依赖")
        void shouldDetectCircularDependency() {
            LineageQueryRequest request = LineageQueryRequest.builder()
                    .assetId("ASSET-001")
                    .direction(LineageDirection.BOTH)
                    .depth(3)
                    .build();

            when(lineageGraphRepository.getUpstreamLineage(anyString(), anyInt())).thenReturn(List.of());
            when(lineageGraphRepository.getDownstreamLineage(anyString(), anyInt())).thenReturn(List.of());
            when(lineageRelationRepository.findByAssetId(anyString())).thenReturn(List.of());
            when(lineageRelationRepository.countByAssetId(anyString())).thenReturn(0L);
            when(lineageGraphRepository.hasCircularDependency("ASSET-001")).thenReturn(true);

            LineageGraphResponse response = lineageService.queryLineageGraph(request);

            assertThat(response.getStatistics().isHasCycle()).isTrue();
        }

        @Test
        @DisplayName("默认查询方向应为BOTH")
        void shouldDefaultDirectionToBoth() {
            LineageQueryRequest requestWithoutDirection = LineageQueryRequest.builder()
                    .assetId("ASSET-CENTER")
                    .depth(3)
                    .build();

            when(lineageGraphRepository.getUpstreamLineage(anyString(), anyInt())).thenReturn(List.of());
            when(lineageGraphRepository.getDownstreamLineage(anyString(), anyInt())).thenReturn(List.of());
            when(lineageRelationRepository.findByAssetId(anyString())).thenReturn(List.of());
            when(lineageRelationRepository.countByAssetId(anyString())).thenReturn(0L);
            when(lineageGraphRepository.hasCircularDependency(anyString())).thenReturn(false);

            lineageService.queryLineageGraph(requestWithoutDirection);

            verify(lineageGraphRepository).getUpstreamLineage(anyString(), anyInt());
            verify(lineageGraphRepository).getDownstreamLineage(anyString(), anyInt());
        }
    }

    @Nested
    @DisplayName("影响分析测试")
    class ImpactAnalysisTests {

        @Test
        @DisplayName("应正确计算影响范围")
        void shouldCalculateImpactScopeCorrectly() {
            List<GraphNode> affectedNodes = createTestGraphNodes(10);
            when(lineageGraphRepository.getDownstreamLineage("ASSET-001", 5)).thenReturn(affectedNodes);

            ImpactAnalysisResponse response = lineageService.analyzeImpact("ASSET-001", 5);

            assertThat(response.getAssetId()).isEqualTo("ASSET-001");
            assertThat(response.getImpactAnalysis().getTotalDownstreamCount()).isEqualTo(10);
            assertThat(response.getAffectedAssets()).hasSize(10);
            assertThat(response.getMitigationSuggestions()).isNotEmpty();
        }

        @Test
        @DisplayName("无下游依赖时影响分析应返回空结果")
        void shouldReturnEmptyImpactWhenNoDownstream() {
            when(lineageGraphRepository.getDownstreamLineage("ASSET-001", 5)).thenReturn(List.of());

            ImpactAnalysisResponse response = lineageService.analyzeImpact("ASSET-001", 5);

            assertThat(response).isNotNull();
            assertThat(response.getAffectedAssets()).isEmpty();
            assertThat(response.getImpactAnalysis().getTotalDownstreamCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("影响分析应限制深度不超过10")
        void shouldLimitImpactDepthToMaximum() {
            when(lineageGraphRepository.getDownstreamLineage("ASSET-001", 10)).thenReturn(List.of());

            lineageService.analyzeImpact("ASSET-001", 20);

            verify(lineageGraphRepository).getDownstreamLineage("ASSET-001", 10);
        }

        @Test
        @DisplayName("应提供缓解建议")
        void shouldProvideMitigationSuggestions() {
            List<GraphNode> affectedNodes = createTestGraphNodes(5);
            when(lineageGraphRepository.getDownstreamLineage("ASSET-001", 5)).thenReturn(affectedNodes);

            ImpactAnalysisResponse response = lineageService.analyzeImpact("ASSET-001", 5);

            assertThat(response.getMitigationSuggestions()).isNotEmpty();
            assertThat(response.getMitigationSuggestions()).hasSizeGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("溯源分析测试")
    class TraceabilityTests {

        @Test
        @DisplayName("应正确执行溯源分析")
        void shouldTraceLineageCorrectly() {
            List<GraphNode> sourceNodes = List.of(
                    GraphNode.builder().assetId("ASSET-SRC-1").name("源资产1").type("TABLE").build()
            );
            when(lineageGraphRepository.getUpstreamLineage("ASSET-TARGET", 5)).thenReturn(sourceNodes);
            when(lineageRelationRepository.findByAssetId(anyString())).thenReturn(List.of(testRelation));
            when(lineageRelationRepository.countByAssetId(anyString())).thenReturn(1L);
            when(lineageGraphRepository.hasCircularDependency(anyString())).thenReturn(false);

            LineageGraphResponse response = lineageService.traceLineage("ASSET-TARGET", 5);

            assertThat(response).isNotNull();
            assertThat(response.getNodes()).isNotEmpty();
        }

        @Test
        @DisplayName("溯源分析默认深度应为5")
        void shouldDefaultTraceDepthTo5() {
            when(lineageGraphRepository.getUpstreamLineage("ASSET-TARGET", 5)).thenReturn(List.of());
            when(lineageRelationRepository.findByAssetId(anyString())).thenReturn(List.of());
            when(lineageRelationRepository.countByAssetId(anyString())).thenReturn(0L);
            when(lineageGraphRepository.hasCircularDependency(anyString())).thenReturn(false);

            lineageService.traceLineage("ASSET-TARGET", null);

            verify(lineageGraphRepository).getUpstreamLineage("ASSET-TARGET", 5);
        }
    }

    @Nested
    @DisplayName("血缘关系验证测试")
    class LineageVerificationTests {

        @Test
        @DisplayName("应成功验证血缘关系")
        void shouldVerifyLineageSuccessfully() {
            when(lineageRelationRepository.findById("test-id-123")).thenReturn(Optional.of(testRelation));
            when(lineageRelationRepository.save(any(LineageRelation.class))).thenReturn(testRelation);

            lineageService.verifyLineage("test-id-123", "admin", "MANUAL");

            verify(lineageRelationRepository, times(1)).save(argThat(rel ->
                    rel.getIsVerified() && "admin".equals(rel.getVerifiedBy()) && "MANUAL".equals(rel.getVerificationMethod())
            ));
        }

        @Test
        @DisplayName("验证不存在的血缘关系应抛出异常")
        void shouldThrowExceptionWhenVerifyingNonExistentLineage() {
            when(lineageRelationRepository.findById("non-existent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lineageService.verifyLineage("non-existent", "admin", "MANUAL"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("血缘统计测试")
    class StatisticsTests {

        @Test
        @DisplayName("应返回正确的血缘统计信息")
        void shouldReturnCorrectStatistics() {
            when(lineageRelationRepository.count()).thenReturn(100L);
            when(lineageRelationRepository.findByLineageTypeAndIsDeletedFalse(LineageType.ETL)).thenReturn(List.of());
            when(lineageRelationRepository.findByLineageTypeAndIsDeletedFalse(LineageType.SQL)).thenReturn(List.of());
            when(lineageRelationRepository.findByLineageTypeAndIsDeletedFalse(LineageType.MANUAL)).thenReturn(List.of());

            Map<String, Object> stats = lineageService.getStatistics();

            assertThat(stats).isNotNull();
            assertThat(stats.get("totalRelations")).isEqualTo(100L);
            assertThat(stats.get("byType")).isNotNull();
        }
    }

    @Nested
    @DisplayName("血缘历史测试")
    class HistoryTests {

        @Test
        @DisplayName("应返回血缘变更历史")
        void shouldReturnLineageHistory() {
            List<LineageHistory> histories = List.of(
                    LineageHistory.builder().id("h1").lineageId("test-id").changeType("CREATE").build(),
                    LineageHistory.builder().id("h2").lineageId("test-id").changeType("UPDATE").build()
            );
            when(lineageHistoryRepository.findByLineageIdOrderByCreatedTimeDesc("test-id-123")).thenReturn(histories);

            List<LineageHistory> result = lineageService.getHistory("test-id-123");

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("快照管理测试")
    class SnapshotTests {

        @Test
        @DisplayName("应成功创建快照")
        void shouldCreateSnapshotSuccessfully() {
            when(lineageRelationRepository.count()).thenReturn(50L);
            when(lineageSnapshotRepository.save(any(LineageSnapshot.class))).thenAnswer(invocation -> {
                LineageSnapshot snapshot = invocation.getArgument(0);
                snapshot.setId("snapshot-123");
                return snapshot;
            });

            LineageSnapshot snapshot = lineageService.createSnapshot("测试快照", "admin");

            assertThat(snapshot).isNotNull();
            assertThat(snapshot.getSnapshotName()).isEqualTo("测试快照");
            assertThat(snapshot.getRelationCount()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("SQL解析测试")
    class SqlParsingTests {

        @Test
        @DisplayName("应正确解析DDL语句")
        void shouldParseDDLCorrectly() {
            LineageParseRequest request = LineageParseRequest.builder()
                    .sqlContent("CREATE TABLE test_table (id INT)")
                    .parseType("DDL")
                    .build();

            SqlLineageParser.DDLParseResult ddlResult = SqlLineageParser.DDLParseResult.builder()
                    .success(true)
                    .ddlType("CREATE_TABLE")
                    .tableName("test_table")
                    .build();
            when(sqlLineageParser.parseDDL(anyString())).thenReturn(ddlResult);

            SqlLineageParser.LineageParseResult result = lineageService.parseSql(request);

            assertThat(result).isNotNull();
            verify(sqlLineageParser).parseDDL("CREATE TABLE test_table (id INT)");
        }

        @Test
        @DisplayName("应正确解析SQL语句")
        void shouldParseSqlCorrectly() {
            LineageParseRequest request = LineageParseRequest.builder()
                    .sqlContent("INSERT INTO target SELECT * FROM source")
                    .parseType("SQL")
                    .build();

            SqlLineageParser.LineageParseResult parseResult = SqlLineageParser.LineageParseResult.builder()
                    .success(true)
                    .sqlType("INSERT")
                    .sourceTables(List.of("source"))
                    .targetTable("target")
                    .build();
            when(sqlLineageParser.parse(anyString())).thenReturn(parseResult);

            SqlLineageParser.LineageParseResult result = lineageService.parseSql(request);

            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            verify(sqlLineageParser).parse("INSERT INTO target SELECT * FROM source");
        }

        @Test
        @DisplayName("未知解析类型应默认使用SQL解析")
        void shouldDefaultToSqlParsingForUnknownType() {
            LineageParseRequest request = LineageParseRequest.builder()
                    .sqlContent("SELECT * FROM test")
                    .parseType("UNKNOWN")
                    .build();

            SqlLineageParser.LineageParseResult parseResult = SqlLineageParser.LineageParseResult.builder()
                    .success(true)
                    .build();
            when(sqlLineageParser.parse(anyString())).thenReturn(parseResult);

            lineageService.parseSql(request);

            verify(sqlLineageParser).parse("SELECT * FROM test");
        }
    }
}
