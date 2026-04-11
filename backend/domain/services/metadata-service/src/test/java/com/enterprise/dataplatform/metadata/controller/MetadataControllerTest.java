package com.enterprise.dataplatform.metadata.controller;

import com.enterprise.dataplatform.metadata.dto.request.MetadataFieldRequest;
import com.enterprise.dataplatform.metadata.dto.request.MetadataRegisterRequest;
import com.enterprise.dataplatform.metadata.dto.response.MetadataResponse;
import com.enterprise.dataplatform.metadata.service.MetadataService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MetadataController 集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("元数据控制器测试")
class MetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MetadataService metadataService;

    private MetadataResponse testMetadata;
    private MetadataRegisterRequest testRequest;

    @BeforeEach
    void setUp() {
        testMetadata = MetadataResponse.builder()
                .objectId("META-001")
                .objectName("用户信息表")
                .objectType("TABLE")
                .domainCode("DOMAIN-USER")
                .domainName("用户域")
                .description("存储用户基本信息")
                .owner("admin")
                .sensitivity("INTERNAL")
                .status("ACTIVE")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        testRequest = MetadataRegisterRequest.builder()
                .objectName("用户信息表")
                .objectType("TABLE")
                .domainCode("DOMAIN-USER")
                .description("存储用户基本信息")
                .owner("admin")
                .sensitivity("INTERNAL")
                .build();
    }

    @Test
    @DisplayName("注册元数据 - 成功")
    void testRegisterMetadata() throws Exception {
        when(metadataService.registerMetadata(any(), any())).thenReturn(testMetadata);

        mockMvc.perform(post("/api/v1/metadata/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.objectId").value("META-001"))
                .andExpect(jsonPath("$.data.objectName").value("用户信息表"));
    }

    @Test
    @DisplayName("注册元数据 - 带字段")
    void testRegisterMetadataWithFields() throws Exception {
        when(metadataService.registerMetadata(any(), any())).thenReturn(testMetadata);

        List<MetadataFieldRequest> fields = List.of(
                MetadataFieldRequest.builder()
                        .fieldName("id")
                        .fieldType("BIGINT")
                        .description("主键ID")
                        .nullable(false)
                        .build(),
                MetadataFieldRequest.builder()
                        .fieldName("username")
                        .fieldType("VARCHAR")
                        .description("用户名")
                        .nullable(false)
                        .build()
        );

        mockMvc.perform(post("/api/v1/metadata/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest))
                        .requestAttr("fields", fields))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取元数据 - 成功")
    void testGetMetadata() throws Exception {
        when(metadataService.getMetadata("META-001")).thenReturn(testMetadata);

        mockMvc.perform(get("/api/v1/metadata/META-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.objectId").value("META-001"))
                .andExpect(jsonPath("$.data.objectName").value("用户信息表"));
    }

    @Test
    @DisplayName("获取元数据 - 不存在")
    void testGetMetadataNotFound() throws Exception {
        when(metadataService.getMetadata("NOT-EXIST")).thenThrow(
                new IllegalArgumentException("元数据不存在"));

        mockMvc.perform(get("/api/v1/metadata/NOT-EXIST"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("更新元数据 - 成功")
    void testUpdateMetadata() throws Exception {
        testMetadata.setObjectName("更新后的名称");
        when(metadataService.updateMetadata(anyString(), any(), any())).thenReturn(testMetadata);

        mockMvc.perform(put("/api/v1/metadata/META-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除元数据 - 成功")
    void testDeleteMetadata() throws Exception {
        mockMvc.perform(delete("/api/v1/metadata/META-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    @DisplayName("搜索元数据 - 分页")
    void testSearchMetadata() throws Exception {
        Page<MetadataResponse> page = new PageImpl<>(List.of(testMetadata));
        when(metadataService.searchMetadata(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/metadata/search")
                        .param("keyword", "用户")
                        .param("objectType", "TABLE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("搜索元数据 - 按域查询")
    void testSearchMetadataByDomain() throws Exception {
        Page<MetadataResponse> page = new PageImpl<>(List.of(testMetadata));
        when(metadataService.searchMetadata(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/metadata/search")
                        .param("domainCode", "DOMAIN-USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取域内元数据")
    void testGetMetadataByDomain() throws Exception {
        when(metadataService.getMetadataByDomain("DOMAIN-USER")).thenReturn(List.of(testMetadata));

        mockMvc.perform(get("/api/v1/metadata/domain/DOMAIN-USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].objectId").value("META-001"));
    }

    @Test
    @DisplayName("获取元数据统计")
    void testGetMetadataStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", 100);
        stats.put("byType", Map.of("TABLE", 50, "API", 30, "VIEW", 20));
        stats.put("bySensitivity", Map.of("INTERNAL", 60, "CONFIDENTIAL", 40));
        when(metadataService.getMetadataStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/metadata/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(100));
    }

    @Test
    @DisplayName("同步资产元数据")
    void testSyncFromAsset() throws Exception {
        Map<String, Object> assetInfo = new HashMap<>();
        assetInfo.put("assetId", "ASSET-001");
        assetInfo.put("assetName", "用户表");
        when(metadataService.syncFromAsset(anyString(), any())).thenReturn(testMetadata);

        mockMvc.perform(post("/api/v1/metadata/sync/ASSET-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
