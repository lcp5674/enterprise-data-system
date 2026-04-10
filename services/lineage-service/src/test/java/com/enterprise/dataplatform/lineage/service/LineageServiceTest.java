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

/**
 * LineageService 单元测试
 */
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

    @Test
    @DisplayName("创建血缘关系 - 成功")
    void testCreateLineage_Success() {
        // Given
        when(lineageRelationRepository.findBySourceAssetIdAndTargetAssetIdAndIsDeletedFalse(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(lineageRelationRepository.save(any(LineageRelation.class))).thenReturn(testRelation);
        doNothing().when(lineageGraphRepository).createLineageRelation(any(), any(), any(), any());
        when(lineageHistoryRepository.save(any(LineageHistory.class))).thenReturn(new LineageHistory());

        // When
        LineageRelation result = lineageService.createLineage(testRequest, "admin");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSourceAssetId()).isEqualTo("ASSET-001");
        verify(lineageRelationRepository, times(1)).save(any(LineageRelation.class));
        verify(lineageGraphRepository, times(1)).createLineageRelation(any(), any(), any(), any());
    }

    @Test
    @DisplayName("创建血缘关系 - 关系已存在")
    void testCreateLineage_AlreadyExists() {
        // Given
        when(lineageRelationRepository.findBySourceAssetIdAndTargetAssetIdAndIsDeletedFalse(anyString(), anyString()))
                .thenReturn(Optional.of(testRelation));

        // When & Then
        assertThatThrownBy(() -> lineageService.createLineage(testRequest, "admin"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("删除血缘关系 - 成功")
    void testDeleteLineage_Success() {
        // Given
        when(lineageRelationRepository.findById("test-id-123")).thenReturn(Optional.of(testRelation));
        when(lineageRelationRepository.save(any(LineageRelation.class))).thenReturn(testRelation);
        doNothing().when(lineageGraphRepository).deleteLineageRelation(anyString(), anyString());
        when(lineageHistoryRepository.save(any(LineageHistory.class))).thenReturn(new LineageHistory());

        // When
        lineageService.deleteLineage("test-id-123", "admin");

        // Then
        verify(lineageRelationRepository, times(1)).save(any(LineageRelation.class));
        verify(lineageGraphRepository, times(1)).deleteLineageRelation(anyString(), anyString());
    }

    @Test
    @DisplayName("删除血缘关系 - 关系不存在")
    void testDeleteLineage_NotFound() {
        // Given
        when(lineageRelationRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lineageService.deleteLineage("non-existent", "admin"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("删除血缘关系 - 已被删除")
    void testDeleteLineage_AlreadyDeleted() {
        // Given
        testRelation.setIsDeleted(true);
        when(lineageRelationRepository.findById("test-id-123")).thenReturn(Optional.of(testRelation));

        // When & Then
        assertThatThrownBy(() -> lineageService.deleteLineage("test-id-123", "admin"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already deleted");
    }

    @Test
    @DisplayName("查询血缘图谱 - 上游查询")
    void testQueryLineageGraph_Upstream() {
        // Given
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

        // When
        LineageGraphResponse response = lineageService.queryLineageGraph(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getNodes()).isNotEmpty();
        assertThat(response.getStatistics()).isNotNull();
    }

    @Test
    @DisplayName("查询血缘图谱 - 下游查询")
    void testQueryLineageGraph_Downstream() {
        // Given
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

        // When
        LineageGraphResponse response = lineageService.queryLineageGraph(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getNodes()).isNotEmpty();
    }

    @Test
    @DisplayName("查询血缘图谱 - 双向查询")
    void testQueryLineageGraph_Both() {
        // Given
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

        // When
        LineageGraphResponse response = lineageService.queryLineageGraph(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getNodes()).isNotEmpty();
    }

    @Test
    @DisplayName("查询血缘图谱 - 深度限制")
    void testQueryLineageGraph_DepthLimit() {
        // Given
        LineageQueryRequest request = LineageQueryRequest.builder()
                .assetId("ASSET-CENTER")
                .direction(LineageDirection.BOTH)
                .depth(20) // 超过最大限制10
                .build();

        when(lineageGraphRepository.getUpstreamLineage(anyString(), eq(10))).thenReturn(List.of());
        when(lineageGraphRepository.getDownstreamLineage(anyString(), eq(10))).thenReturn(List.of());
        when(lineageRelationRepository.findByAssetId(anyString())).thenReturn(List.of());
        when(lineageRelationRepository.countByAssetId(anyString())).thenReturn(0L);
        when(lineageGraphRepository.hasCircularDependency(anyString())).thenReturn(false);

        // When
        LineageGraphResponse response = lineageService.queryLineageGraph(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatistics().getMaxDepth()).isEqualTo(10);
    }

    @Test
    @DisplayName("影响分析 - 正常场景")
    void testAnalyzeImpact_Success() {
        // Given
        List<GraphNode> affectedNodes = List.of(
                GraphNode.builder().assetId("ASSET-003").name("受影响资产1").type("TABLE").build(),
                GraphNode.builder().assetId("ASSET-004").name("受影响资产2").type("TABLE").build()
        );
        when(lineageGraphRepository.getDownstreamLineage("ASSET-001", 5)).thenReturn(affectedNodes);

        // When
        ImpactAnalysisResponse response = lineageService.analyzeImpact("ASSET-001", 5);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAssetId()).isEqualTo("ASSET-001");
        assertThat(response.getAffectedAssets()).hasSize(2);
        assertThat(response.getMitigationSuggestions()).isNotEmpty();
    }

    @Test
    @DisplayName("影响分析 - 无下游依赖")
    void testAnalyzeImpact_NoDownstream() {
        // Given
        when(lineageGraphRepository.getDownstreamLineage("ASSET-001", 5)).thenReturn(List.of());

        // When
        ImpactAnalysisResponse response = lineageService.analyzeImpact("ASSET-001", 5);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAffectedAssets()).isEmpty();
    }

    @Test
    @DisplayName("溯源分析")
    void testTraceLineage() {
        // Given
        LineageQueryRequest request = LineageQueryRequest.builder()
                .assetId("ASSET-TARGET")
                .direction(LineageDirection.UPSTREAM)
                .depth(5)
                .build();

        List<GraphNode> sourceNodes = List.of(
                GraphNode.builder().assetId("ASSET-SRC-1").name("源资产1").type("TABLE").build()
        );
        when(lineageGraphRepository.getUpstreamLineage(anyString(), anyInt())).thenReturn(sourceNodes);
        when(lineageRelationRepository.findByAssetId(anyString())).thenReturn(List.of(testRelation));
        when(lineageRelationRepository.countByAssetId(anyString())).thenReturn(1L);
        when(lineageGraphRepository.hasCircularDependency(anyString())).thenReturn(false);

        // When
        LineageGraphResponse response = lineageService.traceLineage("ASSET-TARGET", 5);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getNodes()).isNotEmpty();
    }

    @Test
    @DisplayName("验证血缘关系 - 成功")
    void testVerifyLineage_Success() {
        // Given
        when(lineageRelationRepository.findById("test-id-123")).thenReturn(Optional.of(testRelation));
        when(lineageRelationRepository.save(any(LineageRelation.class))).thenReturn(testRelation);

        // When
        lineageService.verifyLineage("test-id-123", "admin", "MANUAL");

        // Then
        verify(lineageRelationRepository, times(1)).save(any(LineageRelation.class));
    }

    @Test
    @DisplayName("验证血缘关系 - 关系不存在")
    void testVerifyLineage_NotFound() {
        // Given
        when(lineageRelationRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lineageService.verifyLineage("non-existent", "admin", "MANUAL"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("获取血缘统计")
    void testGetStatistics() {
        // Given
        when(lineageRelationRepository.count()).thenReturn(100L);
        when(lineageRelationRepository.findByLineageTypeAndIsDeletedFalse(LineageType.ETL)).thenReturn(List.of());
        when(lineageRelationRepository.findByLineageTypeAndIsDeletedFalse(LineageType.SQL)).thenReturn(List.of());
        when(lineageRelationRepository.findByLineageTypeAndIsDeletedFalse(LineageType.MANUAL)).thenReturn(List.of());

        // When
        Map<String, Object> stats = lineageService.getStatistics();

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.get("totalRelations")).isEqualTo(100L);
    }

    @Test
    @DisplayName("获取血缘历史")
    void testGetHistory() {
        // Given
        List<LineageHistory> histories = List.of(
                LineageHistory.builder().id("h1").lineageId("test-id").changeType("CREATE").build(),
                LineageHistory.builder().id("h2").lineageId("test-id").changeType("UPDATE").build()
        );
        when(lineageHistoryRepository.findByLineageIdOrderByCreatedTimeDesc("test-id-123")).thenReturn(histories);

        // When
        List<LineageHistory> result = lineageService.getHistory("test-id-123");

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("创建快照")
    void testCreateSnapshot() {
        // Given
        when(lineageRelationRepository.count()).thenReturn(50L);
        when(lineageSnapshotRepository.save(any(LineageSnapshot.class))).thenAnswer(invocation -> {
            LineageSnapshot snapshot = invocation.getArgument(0);
            snapshot.setId("snapshot-123");
            return snapshot;
        });

        // When
        LineageSnapshot snapshot = lineageService.createSnapshot("测试快照", "admin");

        // Then
        assertThat(snapshot).isNotNull();
        assertThat(snapshot.getSnapshotName()).isEqualTo("测试快照");
        assertThat(snapshot.getRelationCount()).isEqualTo(50);
    }

    @Test
    @DisplayName("SQL解析 - DDL类型")
    void testParseSql_DDL() {
        // Given
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

        // When
        SqlLineageParser.LineageParseResult result = lineageService.parseSql(request);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("SQL解析 - SQL类型")
    void testParseSql_SQL() {
        // Given
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

        // When
        SqlLineageParser.LineageParseResult result = lineageService.parseSql(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
    }
}
