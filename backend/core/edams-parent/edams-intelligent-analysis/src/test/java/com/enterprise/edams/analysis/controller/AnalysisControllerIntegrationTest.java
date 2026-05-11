package com.enterprise.edams.analysis.controller;

import com.enterprise.edams.analysis.dto.request.CreateModelConfigRequest;
import com.enterprise.edams.analysis.dto.request.CreateTaskRequest;
import com.enterprise.edams.analysis.entity.ExecutionMode;
import com.enterprise.edams.analysis.service.DatasourceConnectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalysisControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        authToken = "Bearer test-token";
    }

    @Test
    void testListDatasources() throws Exception {
        mockMvc.perform(get("/api/v1/analysis/datasources/1/tables")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateModelConfig() throws Exception {
        CreateModelConfigRequest request = CreateModelConfigRequest.builder()
                .configCode("TEST_CONFIG_" + System.currentTimeMillis())
                .configName("测试配置")
                .modelType(com.enterprise.edams.analysis.entity.ModelType.OLLAMA)
                .baseUrl("http://localhost:11434")
                .modelName("qwen2.5-7b")
                .enabled(true)
                .isDefault(false)
                .build();

        mockMvc.perform(post("/api/v1/analysis/models")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configCode").exists())
                .andExpect(jsonPath("$.modelName").value("qwen2.5-7b"));
    }

    @Test
    void testCreateModelConfig_ValidationError() throws Exception {
        CreateModelConfigRequest request = CreateModelConfigRequest.builder()
                .configName("测试配置")
                .build();

        mockMvc.perform(post("/api/v1/analysis/models")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTask() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .taskName("集成测试任务")
                .datasourceId(1L)
                .modelConfigId(1L)
                .executionMode(ExecutionMode.MANUAL)
                .batchSize(5)
                .enableLineageAnalysis(true)
                .enableIndicatorExtraction(true)
                .enableSubjectClassification(true)
                .autoRegister(false)
                .sampleRowCount(100)
                .build();

        mockMvc.perform(post("/api/v1/analysis/tasks")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskName").value("集成测试任务"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testListTasks() throws Exception {
        mockMvc.perform(get("/api/v1/analysis/tasks")
                        .header("Authorization", authToken)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetTaskProgress() throws Exception {
        mockMvc.perform(get("/api/v1/analysis/tasks/1/progress")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTaskResults() throws Exception {
        mockMvc.perform(get("/api/v1/analysis/tasks/1/results")
                        .header("Authorization", authToken)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTableColumns() throws Exception {
        mockMvc.perform(get("/api/v1/analysis/datasources/1/tables/test_table/columns")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTableSample() throws Exception {
        mockMvc.perform(get("/api/v1/analysis/datasources/1/tables/test_table/sample")
                        .header("Authorization", authToken)
                        .param("rowCount", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
