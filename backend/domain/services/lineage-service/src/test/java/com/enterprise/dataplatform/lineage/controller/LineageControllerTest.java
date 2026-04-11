package com.enterprise.dataplatform.lineage.controller;

import com.enterprise.dataplatform.lineage.dto.request.SqlParseRequest;
import com.enterprise.dataplatform.lineage.dto.response.LineageNode;
import com.enterprise.dataplatform.lineage.dto.response.LineageResponse;
import com.enterprise.dataplatform.lineage.service.LineageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LineageController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("数据血缘控制器测试")
class LineageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LineageService lineageService;

    private LineageResponse testLineage;
    private List<LineageNode> testNodes;

    @BeforeEach
    void setUp() {
        testNodes = List.of(
                LineageNode.builder()
                        .id("node-1")
                        .name("source_table")
                        .type("TABLE")
                        .fullName("db.schema.source_table")
                        .level(0)
                        .build(),
                LineageNode.builder()
                        .id("node-2")
                        .name("target_table")
                        .type("TABLE")
                        .fullName("db.schema.target_table")
                        .level(1)
                        .build()
        );

        testLineage = LineageResponse.builder()
                .assetId("ASSET-001")
                .assetName("用户订单表")
                .nodes(testNodes)
                .build();
    }

    @Test
    @DisplayName("获取资产血缘 - 成功")
    void testGetAssetLineage() throws Exception {
        when(lineageService.getAssetLineage(anyString())).thenReturn(testLineage);

        mockMvc.perform(get("/api/v1/lineage/asset/ASSET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.assetId").value("ASSET-001"))
                .andExpect(jsonPath("$.data.nodes").isArray());
    }

    @Test
    @DisplayName("获取完整血缘关系")
    void testGetFullLineage() throws Exception {
        when(lineageService.getFullLineage(anyString())).thenReturn(testLineage);

        mockMvc.perform(get("/api/v1/lineage/full/ASSET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.assetId").value("ASSET-001"));
    }

    @Test
    @DisplayName("获取上游血缘")
    void testGetUpstreamLineage() throws Exception {
        when(lineageService.getUpstreamLineage(anyString())).thenReturn(testLineage);

        mockMvc.perform(get("/api/v1/lineage/upstream/ASSET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取下游血缘")
    void testGetDownstreamLineage() throws Exception {
        when(lineageService.getDownstreamLineage(anyString())).thenReturn(testLineage);

        mockMvc.perform(get("/api/v1/lineage/downstream/ASSET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取影响分析")
    void testGetImpactAnalysis() throws Exception {
        when(lineageService.getImpactAnalysis(anyString())).thenReturn(testLineage);

        mockMvc.perform(get("/api/v1/lineage/impact/ASSET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("解析SQL血缘 - 成功")
    void testParseSqlLineage() throws Exception {
        when(lineageService.parseSqlLineage(any())).thenReturn(testLineage);

        SqlParseRequest request = SqlParseRequest.builder()
                .sql("SELECT a.id, b.name FROM users a JOIN orders b ON a.id = b.user_id")
                .databaseType("MYSQL")
                .build();

        mockMvc.perform(post("/api/v1/lineage/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nodes").isArray());
    }

    @Test
    @DisplayName("解析SQL血缘 - SQL语法错误")
    void testParseSqlLineageInvalid() throws Exception {
        SqlParseRequest request = SqlParseRequest.builder()
                .sql("SELECT FROM invalid syntax")
                .databaseType("MYSQL")
                .build();

        mockMvc.perform(post("/api/v1/lineage/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("创建血缘关系")
    void testCreateLineageRelation() throws Exception {
        mockMvc.perform(post("/api/v1/lineage/relation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "sourceAssetId": "ASSET-001",
                                    "targetAssetId": "ASSET-002",
                                    "relationType": "DERIVES_FROM",
                                    "transformation": "SELECT * FROM source"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除血缘关系")
    void testDeleteLineageRelation() throws Exception {
        mockMvc.perform(delete("/api/v1/lineage/relation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取血缘统计")
    void testGetLineageStats() throws Exception {
        when(lineageService.getLineageStats()).thenReturn(Map.of(
                "totalRelations", 100,
                "totalTables", 50,
                "avgDepth", 3.5
        ));

        mockMvc.perform(get("/api/v1/lineage/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalRelations").value(100));
    }

    @Test
    @DisplayName("血缘追踪 - 空资产ID")
    void testLineageEmptyAssetId() throws Exception {
        mockMvc.perform(get("/api/v1/lineage/asset/"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("血缘可视化数据")
    void testGetVisualizationData() throws Exception {
        when(lineageService.getVisualizationData(anyString())).thenReturn(Map.of(
                "nodes", List.of(Map.of("id", "n1", "label", "Table1")),
                "edges", List.of(Map.of("source", "n1", "target", "n2"))
        ));

        mockMvc.perform(get("/api/v1/lineage/visualize/ASSET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nodes").exists());
    }
}
