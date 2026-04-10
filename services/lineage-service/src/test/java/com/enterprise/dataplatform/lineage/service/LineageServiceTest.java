package com.enterprise.dataplatform.lineage.service;

import com.enterprise.dataplatform.lineage.domain.entity.LineageRelation;
import com.enterprise.dataplatform.lineage.domain.entity.DataAsset;
import com.enterprise.dataplatform.lineage.dto.request.LineageCreateRequest;
import com.enterprise.dataplatform.lineage.dto.response.LineageGraphResponse;
import com.enterprise.dataplatform.lineage.repository.LineageGraphRepository;
import com.enterprise.dataplatform.lineage.repository.LineageRelationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LineageServiceTest {

    @Mock
    private LineageGraphRepository graphRepository;

    @Mock
    private LineageRelationRepository relationRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private LineageService lineageService;

    private LineageRelation testRelation;
    private DataAsset testAsset;

    @BeforeEach
    void setUp() {
        testRelation = LineageRelation.builder()
                .id(1L)
                .sourceAssetId("TABLE_SRC")
                .targetAssetId("TABLE_TGT")
                .lineageType("DIRECT")
                .transformType("EXTRACT_TRANSFORM")
                .transformSql("SELECT id, name FROM source")
                .confidence(1.0)
                .createdAt(LocalDateTime.now())
                .build();

        testAsset = DataAsset.builder()
                .assetId("TABLE_SRC")
                .assetName("source_table")
                .assetType("TABLE")
                .build();
    }

    @Test
    void createLineage_shouldCreateSuccessfully() {
        LineageCreateRequest request = LineageCreateRequest.builder()
                .sourceAssetId("TABLE_SRC")
                .targetAssetId("TABLE_TGT")
                .lineageType("DIRECT")
                .transformSql("SELECT * FROM source")
                .build();

        when(graphRepository.createRelation(any())).thenReturn(testRelation);
        doNothing().when(kafkaTemplate).send(anyString(), anyString(), any());

        LineageRelation result = lineageService.createLineage(request);

        assertNotNull(result);
        verify(graphRepository, times(1)).createRelation(any());
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void createLineageFromSql_shouldParseAndCreateRelations() {
        String sql = "INSERT INTO target_table SELECT id, name FROM source_table";

        when(graphRepository.createRelation(any())).thenReturn(testRelation);
        doNothing().when(kafkaTemplate).send(anyString(), anyString(), any());

        List<LineageRelation> results = lineageService.createLineageFromSql(
                "TABLE_TGT", sql, "DDL");

        assertNotNull(results);
    }

    @Test
    void queryLineageGraph_shouldReturnGraph() {
        List<Map<String, Object>> nodes = List.of(
                Map.of("assetId", "TABLE_A", "assetName", "Table A"),
                Map.of("assetId", "TABLE_B", "assetName", "Table B")
        );
        List<Map<String, Object>> edges = List.of(
                Map.of("source", "TABLE_A", "target", "TABLE_B")
        );

        when(graphRepository.getAssetNode("TABLE_A")).thenReturn(nodes.get(0));
        when(graphRepository.getDownstreamLineage("TABLE_A", 10)).thenReturn(nodes);
        when(graphRepository.getDownstreamEdges("TABLE_A", 10)).thenReturn(edges);

        LineageGraphResponse result = lineageService.queryLineageGraph("TABLE_A", "DOWNSTREAM", 10);

        assertNotNull(result);
        assertNotNull(result.getNodes());
        assertNotNull(result.getEdges());
    }

    @Test
    void analyzeImpact_shouldReturnAffectedAssets() {
        List<String> affectedTables = List.of("TABLE_B", "TABLE_C", "TABLE_D");

        when(graphRepository.getDownstreamLineage("TABLE_A", 10)).thenReturn(
                affectedTables.stream()
                        .map(id -> Map.<String, Object>of("assetId", id, "assetName", id))
                        .toList()
        );

        List<Map<String, Object>> result = lineageService.analyzeImpact("TABLE_A");

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void traceLineage_shouldReturnSourceAssets() {
        List<String> sourceTables = List.of("TABLE_X", "TABLE_Y");

        when(graphRepository.getUpstreamLineage("TABLE_Z", 10)).thenReturn(
                sourceTables.stream()
                        .map(id -> Map.<String, Object>of("assetId", id, "assetName", id))
                        .toList()
        );

        List<Map<String, Object>> result = lineageService.traceLineage("TABLE_Z");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void hasCircularDependency_shouldDetectCircularDependency() {
        when(graphRepository.hasCircularDependency("TABLE_A")).thenReturn(true);

        boolean result = lineageService.hasCircularDependency("TABLE_A");

        assertTrue(result);
    }

    @Test
    void hasCircularDependency_shouldReturnFalseWhenNoCircular() {
        when(graphRepository.hasCircularDependency("TABLE_B")).thenReturn(false);

        boolean result = lineageService.hasCircularDependency("TABLE_B");

        assertFalse(result);
    }

    @Test
    void deleteLineage_shouldDeleteSuccessfully() {
        when(relationRepository.findById(1L)).thenReturn(Optional.of(testRelation));
        doNothing().when(relationRepository).delete(any(LineageRelation.class));

        assertDoesNotThrow(() -> lineageService.deleteLineage(1L));

        verify(relationRepository, times(1)).delete(any(LineageRelation.class));
    }

    @Test
    void getLineageStatistics_shouldReturnStatistics() {
        when(relationRepository.countBySourceAssetId("TABLE_SRC")).thenReturn(5L);
        when(relationRepository.countByTargetAssetId("TABLE_TGT")).thenReturn(3L);
        when(relationRepository.count()).thenReturn(100L);
        when(graphRepository.countNodes()).thenReturn(50L);
        when(graphRepository.countEdges()).thenReturn(80L);

        Map<String, Object> stats = lineageService.getLineageStatistics();

        assertNotNull(stats);
        assertEquals(100L, stats.get("totalRelations"));
        assertEquals(50L, stats.get("totalNodes"));
        assertEquals(80L, stats.get("totalEdges"));
    }

    @Test
    void refreshLineage_shouldRefreshSuccessfully() {
        when(graphRepository.getAssetNode("TABLE_A")).thenReturn(
                Map.of("assetId", "TABLE_A", "assetName", "Table A")
        );
        when(relationRepository.findBySourceAssetId("TABLE_A")).thenReturn(List.of(testRelation));

        assertDoesNotThrow(() -> lineageService.refreshLineage("TABLE_A"));

        verify(graphRepository, times(1)).getAssetNode("TABLE_A");
    }
}
