package com.enterprise.dataplatform.governance.controller;

import com.enterprise.dataplatform.governance.dto.request.GovernancePolicyRequest;
import com.enterprise.dataplatform.governance.dto.request.GovernanceTaskRequest;
import com.enterprise.dataplatform.governance.dto.response.GovernancePolicyResponse;
import com.enterprise.dataplatform.governance.dto.response.GovernanceTaskResponse;
import com.enterprise.dataplatform.governance.dto.response.RecommendationResponse;
import com.enterprise.dataplatform.governance.service.AIRecommendationService;
import com.enterprise.dataplatform.governance.service.GovernanceOrchestrationService;
import com.enterprise.dataplatform.governance.service.GovernancePolicyService;
import com.enterprise.dataplatform.governance.service.GovernanceTaskService;
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
 * GovernanceController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("治理控制器测试")
class GovernanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GovernancePolicyService policyService;

    @MockBean
    private GovernanceTaskService taskService;

    @MockBean
    private AIRecommendationService aiRecommendationService;

    @MockBean
    private GovernanceOrchestrationService orchestrationService;

    private GovernancePolicyResponse testPolicy;
    private GovernanceTaskResponse testTask;
    private RecommendationResponse testRecommendation;

    @BeforeEach
    void setUp() {
        testPolicy = GovernancePolicyResponse.builder()
                .id(1L)
                .policyCode("POL-001")
                .policyName("数据质量治理策略")
                .description("确保数据质量符合标准")
                .policyType("QUALITY")
                .targetAssets(List.of("ASSET-001", "ASSET-002"))
                .enabled(true)
                .status("ACTIVE")
                .createTime(LocalDateTime.now())
                .build();

        testTask = GovernanceTaskResponse.builder()
                .id(1L)
                .taskName("执行质量检查")
                .taskType("QUALITY_CHECK")
                .targetAssetId("ASSET-001")
                .priority("HIGH")
                .status("PENDING")
                .assignedTo("admin")
                .build();

        testRecommendation = RecommendationResponse.builder()
                .id(1L)
                .assetId("ASSET-001")
                .category("QUALITY_IMPROVEMENT")
                .title("建议优化字段phone的数据质量")
                .description("phone字段存在格式不规范的数据，建议添加格式校验规则")
                .confidence(0.85)
                .actionItems(List.of(
                        Map.of("action", "添加格式校验规则", "priority", "HIGH"),
                        Map.of("action", "清理现有不合规数据", "priority", "MEDIUM")
                ))
                .build();
    }

    // ==================== 治理策略测试 ====================

    @Test
    @DisplayName("创建治理策略 - 成功")
    void testCreatePolicy() throws Exception {
        when(policyService.createPolicy(any(), any())).thenReturn(testPolicy);

        GovernancePolicyRequest request = GovernancePolicyRequest.builder()
                .policyCode("POL-001")
                .policyName("数据质量治理策略")
                .policyType("QUALITY")
                .description("确保数据质量符合标准")
                .build();

        mockMvc.perform(post("/api/v1/governance/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.policyCode").value("POL-001"));
    }

    @Test
    @DisplayName("获取治理策略")
    void testGetPolicy() throws Exception {
        when(policyService.getPolicy(1L)).thenReturn(testPolicy);

        mockMvc.perform(get("/api/v1/governance/policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("更新治理策略")
    void testUpdatePolicy() throws Exception {
        when(policyService.updatePolicy(eq(1L), any(), any())).thenReturn(testPolicy);

        GovernancePolicyRequest request = GovernancePolicyRequest.builder()
                .policyName("更新后的策略")
                .build();

        mockMvc.perform(put("/api/v1/governance/policies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除治理策略")
    void testDeletePolicy() throws Exception {
        mockMvc.perform(delete("/api/v1/governance/policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("分页查询策略")
    void testSearchPolicies() throws Exception {
        Page<GovernancePolicyResponse> page = new PageImpl<>(List.of(testPolicy));
        when(policyService.searchPolicies(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/governance/policies")
                        .param("policyType", "QUALITY")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("启用策略")
    void testEnablePolicy() throws Exception {
        testPolicy.setEnabled(true);
        when(policyService.setPolicyEnabled(eq(1L), eq(true), any())).thenReturn(testPolicy);

        mockMvc.perform(put("/api/v1/governance/policies/1/enable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    // ==================== 治理任务测试 ====================

    @Test
    @DisplayName("创建治理任务")
    void testCreateTask() throws Exception {
        when(taskService.createTask(any(), any())).thenReturn(testTask);

        GovernanceTaskRequest request = GovernanceTaskRequest.builder()
                .taskName("执行质量检查")
                .taskType("QUALITY_CHECK")
                .targetAssetId("ASSET-001")
                .priority("HIGH")
                .build();

        mockMvc.perform(post("/api/v1/governance/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取任务详情")
    void testGetTask() throws Exception {
        when(taskService.getTask(1L)).thenReturn(testTask);

        mockMvc.perform(get("/api/v1/governance/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("执行任务")
    void testExecuteTask() throws Exception {
        testTask.setStatus("COMPLETED");
        when(taskService.executeTask(eq(1L), any())).thenReturn(testTask);

        mockMvc.perform(post("/api/v1/governance/tasks/1/execute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("完成任务")
    void testCompleteTask() throws Exception {
        testTask.setStatus("COMPLETED");
        when(taskService.completeTask(eq(1L), any())).thenReturn(testTask);

        mockMvc.perform(post("/api/v1/governance/tasks/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\": \"检查完成，无异常\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取待处理任务")
    void testGetPendingTasks() throws Exception {
        Page<GovernanceTaskResponse> page = new PageImpl<>(List.of(testTask));
        when(taskService.getPendingTasks(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/governance/tasks/pending")
                        .param("assignee", "admin")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== AI推荐测试 ====================

    @Test
    @DisplayName("获取资产改善建议")
    void testGetRecommendations() throws Exception {
        when(aiRecommendationService.getRecommendations(anyString())).thenReturn(List.of(testRecommendation));

        mockMvc.perform(get("/api/v1/governance/recommendations/ASSET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取质量分析")
    void testGetQualityAnalysis() throws Exception {
        when(aiRecommendationService.getQualityAnalysis(anyString())).thenReturn(Map.of(
                "overallScore", 85.5,
                "issues", List.of(
                        Map.of("type", "COMPLETENESS", "severity", "MEDIUM", "count", 10),
                        Map.of("type", "VALIDITY", "severity", "HIGH", "count", 5)
                ),
                "suggestions", List.of("建议添加非空约束", "建议优化数据格式")
        ));

        mockMvc.perform(get("/api/v1/governance/analysis/ASSET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.overallScore").value(85.5));
    }

    // ==================== 编排服务测试 ====================

    @Test
    @DisplayName("执行治理编排")
    void testOrchestrate() throws Exception {
        when(orchestrationService.orchestrate(anyString(), anyMap())).thenReturn(Map.of(
                "executionId", "exec-001",
                "status", "RUNNING",
                "progress", 0.0
        ));

        mockMvc.perform(post("/api/v1/governance/orchestrate")
                        .param("scenario", "FULL_GOVERNANCE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assets\": [\"ASSET-001\", \"ASSET-002\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.executionId").value("exec-001"));
    }

    @Test
    @DisplayName("获取编排执行状态")
    void testGetOrchestrationStatus() throws Exception {
        when(orchestrationService.getExecutionStatus("exec-001")).thenReturn(Map.of(
                "executionId", "exec-001",
                "status", "COMPLETED",
                "progress", 100.0,
                "completedSteps", 5,
                "totalSteps", 5
        ));

        mockMvc.perform(get("/api/v1/governance/orchestrate/exec-001/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("获取治理仪表盘统计")
    void testGetGovernanceStats() throws Exception {
        when(orchestrationService.getGovernanceStats()).thenReturn(Map.of(
                "totalPolicies", 10,
                "activePolicies", 8,
                "pendingTasks", 15,
                "completedTasksToday", 25,
                "avgQualityScore", 88.5
        ));

        mockMvc.perform(get("/api/v1/governance/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalPolicies").value(10));
    }
}
