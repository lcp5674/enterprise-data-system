package com.enterprise.dataplatform.standard.controller;

import com.enterprise.dataplatform.standard.dto.request.ComplianceCheckRequest;
import com.enterprise.dataplatform.standard.dto.request.StandardMappingRequest;
import com.enterprise.dataplatform.standard.dto.request.StandardRequest;
import com.enterprise.dataplatform.standard.dto.response.ComplianceCheckResponse;
import com.enterprise.dataplatform.standard.dto.response.StandardMappingResponse;
import com.enterprise.dataplatform.standard.dto.response.StandardResponse;
import com.enterprise.dataplatform.standard.service.ComplianceCheckService;
import com.enterprise.dataplatform.standard.service.DataStandardService;
import com.enterprise.dataplatform.standard.service.StandardMappingService;
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
 * StandardController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("数据标准控制器测试")
class StandardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DataStandardService dataStandardService;

    @MockBean
    private StandardMappingService mappingService;

    @MockBean
    private ComplianceCheckService complianceCheckService;

    private StandardResponse testStandard;
    private StandardMappingResponse testMapping;
    private ComplianceCheckResponse testCheckResult;

    @BeforeEach
    void setUp() {
        testStandard = StandardResponse.builder()
                .id(1L)
                .standardCode("STD-001")
                .standardName("手机号格式标准")
                .description("验证中国手机号格式")
                .standardType("FORMAT")
                .standardCategory("PERSONAL_INFO")
                .ruleExpression("^1[3-9]\\d{9}$")
                .status("PUBLISHED")
                .creator("admin")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        testMapping = StandardMappingResponse.builder()
                .id(1L)
                .standardId(1L)
                .assetId("ASSET-001")
                .fieldName("phone")
                .mappingStatus("MAPPED")
                .complianceStatus("COMPLIANT")
                .build();

        testCheckResult = ComplianceCheckResponse.builder()
                .id(1L)
                .assetId("ASSET-001")
                .standardId(1L)
                .standardName("手机号格式标准")
                .checkStatus("PASSED")
                .checkedCount(100)
                .violationCount(0)
                .complianceRate(100.0)
                .build();
    }

    // ==================== 数据标准测试 ====================

    @Test
    @DisplayName("创建数据标准 - 成功")
    void testCreateStandard() throws Exception {
        when(dataStandardService.createStandard(any(), any())).thenReturn(testStandard);

        StandardRequest request = StandardRequest.builder()
                .standardCode("STD-001")
                .standardName("手机号格式标准")
                .standardType("FORMAT")
                .standardCategory("PERSONAL_INFO")
                .ruleExpression("^1[3-9]\\d{9}$")
                .build();

        mockMvc.perform(post("/api/v1/standards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.standardCode").value("STD-001"));
    }

    @Test
    @DisplayName("获取数据标准 - 成功")
    void testGetStandard() throws Exception {
        when(dataStandardService.getStandard(1L)).thenReturn(testStandard);

        mockMvc.perform(get("/api/v1/standards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.standardCode").value("STD-001"));
    }

    @Test
    @DisplayName("更新数据标准")
    void testUpdateStandard() throws Exception {
        when(dataStandardService.updateStandard(eq(1L), any(), any())).thenReturn(testStandard);

        StandardRequest request = StandardRequest.builder()
                .standardName("更新后的标准")
                .build();

        mockMvc.perform(put("/api/v1/standards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除数据标准")
    void testDeleteStandard() throws Exception {
        mockMvc.perform(delete("/api/v1/standards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("分页查询标准")
    void testSearchStandards() throws Exception {
        Page<StandardResponse> page = new PageImpl<>(List.of(testStandard));
        when(dataStandardService.searchStandards(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/standards")
                        .param("standardType", "FORMAT")
                        .param("status", "PUBLISHED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("发布标准")
    void testPublishStandard() throws Exception {
        testStandard.setStatus("PUBLISHED");
        when(dataStandardService.publishStandard(eq(1L), any())).thenReturn(testStandard);

        mockMvc.perform(post("/api/v1/standards/1/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    // ==================== 标准映射测试 ====================

    @Test
    @DisplayName("创建标准映射")
    void testCreateMapping() throws Exception {
        when(mappingService.createMapping(any(), any())).thenReturn(testMapping);

        StandardMappingRequest request = StandardMappingRequest.builder()
                .standardId(1L)
                .assetId("ASSET-001")
                .fieldName("phone")
                .build();

        mockMvc.perform(post("/api/v1/standards/mappings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取资产的映射列表")
    void testGetAssetMappings() throws Exception {
        when(mappingService.getAssetMappings("ASSET-001")).thenReturn(List.of(testMapping));

        mockMvc.perform(get("/api/v1/standards/mappings/asset/ASSET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("删除映射")
    void testDeleteMapping() throws Exception {
        mockMvc.perform(delete("/api/v1/standards/mappings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 合规检查测试 ====================

    @Test
    @DisplayName("执行合规检查 - 成功")
    void testExecuteComplianceCheck() throws Exception {
        when(complianceCheckService.executeCheck(any())).thenReturn(testCheckResult);

        ComplianceCheckRequest request = ComplianceCheckRequest.builder()
                .assetId("ASSET-001")
                .standardId(1L)
                .build();

        mockMvc.perform(post("/api/v1/standards/compliance/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.checkStatus").value("PASSED"));
    }

    @Test
    @DisplayName("批量合规检查")
    void testBatchComplianceCheck() throws Exception {
        when(complianceCheckService.batchCheck(anyString(), anyList())).thenReturn(List.of(testCheckResult));

        mockMvc.perform(post("/api/v1/standards/compliance/batch/ASSET-001")
                        .param("standardIds", "1,2,3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取合规报告")
    void testGetComplianceReport() throws Exception {
        when(complianceCheckService.getComplianceReport(anyString(), any(), any())).thenReturn(Map.of(
                "assetId", "ASSET-001",
                "totalMappings", 10,
                "compliantCount", 8,
                "violationCount", 2,
                "complianceRate", 80.0
        ));

        mockMvc.perform(get("/api/v1/standards/compliance/report/ASSET-001")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.complianceRate").value(80.0));
    }

    @Test
    @DisplayName("获取不合规详情")
    void testGetViolations() throws Exception {
        when(complianceCheckService.getViolations(anyString(), anyString(), any())).thenReturn(List.of(
                Map.of("fieldName", "phone", "violationReason", "格式不正确", "sampleValue", "12345")
        ));

        mockMvc.perform(get("/api/v1/standards/compliance/violations/ASSET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
