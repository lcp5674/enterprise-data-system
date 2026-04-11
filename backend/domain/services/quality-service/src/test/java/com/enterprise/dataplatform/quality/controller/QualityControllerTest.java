package com.enterprise.dataplatform.quality.controller;

import com.enterprise.dataplatform.quality.dto.request.QualityCheckRequest;
import com.enterprise.dataplatform.quality.dto.request.QualityRuleRequest;
import com.enterprise.dataplatform.quality.dto.response.QualityCheckResponse;
import com.enterprise.dataplatform.quality.dto.response.QualityRuleResponse;
import com.enterprise.dataplatform.quality.service.QualityCheckService;
import com.enterprise.dataplatform.quality.service.QualityRuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * QualityController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("质量控制器测试")
class QualityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QualityRuleService qualityRuleService;

    @MockBean
    private QualityCheckService qualityCheckService;

    private QualityRuleResponse testRule;
    private QualityCheckResponse testCheckResult;

    @BeforeEach
    void setUp() {
        testRule = QualityRuleResponse.builder()
                .id(1L)
                .ruleCode("RULE-001")
                .ruleName("完整性检查")
                .description("检查字段是否为空")
                .ruleType("COMPLETENESS")
                .qualityDimension("完整性")
                .severityLevel("HIGH")
                .status("ACTIVE")
                .enabled(true)
                .createTime(LocalDateTime.now())
                .build();

        testCheckResult = QualityCheckResponse.builder()
                .id(1L)
                .assetId("ASSET-001")
                .ruleId(1L)
                .ruleName("完整性检查")
                .checkStatus("PASSED")
                .qualityScore(95.5)
                .passedCount(100)
                .failedCount(5)
                .totalCount(105)
                .executeTime(1200L)
                .executeTimeStr("1.2秒")
                .build();
    }

    // ==================== 质量规则测试 ====================

    @Test
    @DisplayName("创建质量规则 - 成功")
    void testCreateRule() throws Exception {
        when(qualityRuleService.createRule(any(), any())).thenReturn(testRule);

        QualityRuleRequest request = QualityRuleRequest.builder()
                .ruleCode("RULE-001")
                .ruleName("完整性检查")
                .ruleType("COMPLETENESS")
                .qualityDimension("完整性")
                .severityLevel("HIGH")
                .build();

        mockMvc.perform(post("/api/v1/quality/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.ruleCode").value("RULE-001"));
    }

    @Test
    @DisplayName("获取质量规则 - 成功")
    void testGetRule() throws Exception {
        when(qualityRuleService.getRule(1L)).thenReturn(testRule);

        mockMvc.perform(get("/api/v1/quality/rules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.ruleCode").value("RULE-001"));
    }

    @Test
    @DisplayName("更新质量规则")
    void testUpdateRule() throws Exception {
        when(qualityRuleService.updateRule(eq(1L), any(), any())).thenReturn(testRule);

        QualityRuleRequest request = QualityRuleRequest.builder()
                .ruleName("更新后的规则")
                .build();

        mockMvc.perform(put("/api/v1/quality/rules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除质量规则")
    void testDeleteRule() throws Exception {
        mockMvc.perform(delete("/api/v1/quality/rules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("分页查询规则")
    void testSearchRules() throws Exception {
        Page<QualityRuleResponse> page = new PageImpl<>(List.of(testRule));
        when(qualityRuleService.searchRules(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/quality/rules")
                        .param("ruleType", "COMPLETENESS")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("启用规则")
    void testEnableRule() throws Exception {
        testRule.setEnabled(true);
        when(qualityRuleService.setRuleEnabled(eq(1L), eq(true), any())).thenReturn(testRule);

        mockMvc.perform(put("/api/v1/quality/rules/1/enable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    @DisplayName("禁用规则")
    void testDisableRule() throws Exception {
        testRule.setEnabled(false);
        when(qualityRuleService.setRuleEnabled(eq(1L), eq(false), any())).thenReturn(testRule);

        mockMvc.perform(put("/api/v1/quality/rules/1/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    @DisplayName("发布规则")
    void testPublishRule() throws Exception {
        testRule.setStatus("ACTIVE");
        when(qualityRuleService.publishRule(eq(1L), any())).thenReturn(testRule);

        mockMvc.perform(post("/api/v1/quality/rules/1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    // ==================== 质量检查测试 ====================

    @Test
    @DisplayName("执行质量检查 - 成功")
    void testExecuteCheck() throws Exception {
        when(qualityCheckService.executeCheck(any())).thenReturn(testCheckResult);

        QualityCheckRequest request = QualityCheckRequest.builder()
                .assetId("ASSET-001")
                .ruleId(1L)
                .build();

        mockMvc.perform(post("/api/v1/quality/checks/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.checkStatus").value("PASSED"))
                .andExpect(jsonPath("$.data.qualityScore").value(95.5));
    }

    @Test
    @DisplayName("批量执行质量检查")
    void testBatchExecuteCheck() throws Exception {
        when(qualityCheckService.batchExecuteCheck(any())).thenReturn(List.of(testCheckResult));

        Map<String, Object> request = Map.of(
                "assetIds", List.of("ASSET-001", "ASSET-002"),
                "ruleIds", List.of(1L, 2L)
        );

        mockMvc.perform(post("/api/v1/quality/checks/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取检查结果详情")
    void testGetCheckResult() throws Exception {
        when(qualityCheckService.getCheckResult(1L)).thenReturn(testCheckResult);

        mockMvc.perform(get("/api/v1/quality/checks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("获取资产的检查历史")
    void testGetAssetCheckHistory() throws Exception {
        Page<QualityCheckResponse> page = new PageImpl<>(List.of(testCheckResult));
        when(qualityCheckService.getAssetCheckHistory(anyString(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/quality/checks/asset/ASSET-001/history")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("获取质量统计")
    void testGetQualityStats() throws Exception {
        when(qualityCheckService.getQualityStats(anyString())).thenReturn(Map.of(
                "totalChecks", 100,
                "passedChecks", 85,
                "failedChecks", 15,
                "avgScore", 92.5
        ));

        mockMvc.perform(get("/api/v1/quality/stats/ASSET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.avgScore").value(92.5));
    }

    @Test
    @DisplayName("获取质量趋势")
    void testGetQualityTrend() throws Exception {
        when(qualityCheckService.getQualityTrend(anyString(), any(), any())).thenReturn(Map.of(
                "dates", List.of("2024-01-01", "2024-01-02", "2024-01-03"),
                "scores", List.of(90.0, 92.0, 95.0)
        ));

        mockMvc.perform(get("/api/v1/quality/trend/ASSET-001")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.dates").isArray());
    }
}
