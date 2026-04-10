package com.enterprise.edams.datasource.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.datasource.constant.DatasourceStatus;
import com.enterprise.edams.datasource.constant.DatasourceType;
import com.enterprise.edams.datasource.constant.HealthStatus;
import com.enterprise.edams.datasource.dto.ConnectionTestRequest;
import com.enterprise.edams.datasource.dto.ConnectionTestResponse;
import com.enterprise.edams.datasource.dto.CreateDatasourceRequest;
import com.enterprise.edams.datasource.dto.UpdateDatasourceRequest;
import com.enterprise.edams.datasource.entity.DatasourceConfig;
import com.enterprise.edams.datasource.service.DatasourceConfigService;
import com.enterprise.edams.datasource.vo.DatasourceDetailVO;
import com.enterprise.edams.datasource.vo.DatasourceStatisticsVO;
import com.enterprise.edams.datasource.vo.DatasourceVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 数据源配置控制器单元测试
 */
@WebMvcTest(DatasourceConfigController.class)
class DatasourceConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DatasourceConfigService datasourceConfigService;

    private DatasourceVO datasourceVO;
    private DatasourceDetailVO datasourceDetailVO;
    private CreateDatasourceRequest createRequest;

    @BeforeEach
    void setUp() {
        datasourceVO = new DatasourceVO();
        datasourceVO.setId(1L);
        datasourceVO.setName("测试数据源");
        datasourceVO.setCode("TEST_DS");
        datasourceVO.setDatasourceType(DatasourceType.MYSQL.name());
        datasourceVO.setDatasourceTypeDesc("MySQL数据库");
        datasourceVO.setHost("localhost");
        datasourceVO.setPort(3306);
        datasourceVO.setStatus(DatasourceStatus.ACTIVE.name());
        datasourceVO.setStatusDesc("已启用");
        datasourceVO.setHealthStatus(HealthStatus.HEALTHY.name());
        datasourceVO.setHealthStatusDesc("健康");
        datasourceVO.setSyncEnabled(true);
        datasourceVO.setSyncInterval(60);
        datasourceVO.setCreatedBy("system");
        datasourceVO.setCreatedTime(LocalDateTime.now());
        datasourceVO.setUpdatedTime(LocalDateTime.now());

        datasourceDetailVO = new DatasourceDetailVO();
        datasourceDetailVO.setId(1L);
        datasourceDetailVO.setName("测试数据源");
        datasourceDetailVO.setCode("TEST_DS");
        datasourceDetailVO.setDatasourceType(DatasourceType.MYSQL.name());
        datasourceDetailVO.setHost("localhost");
        datasourceDetailVO.setPort(3306);
        datasourceDetailVO.setUsername("root");
        datasourceDetailVO.setDatabaseName("test_db");
        datasourceDetailVO.setStatus(DatasourceStatus.ACTIVE.name());
        datasourceDetailVO.setHealthStatus(HealthStatus.HEALTHY.name());

        createRequest = new CreateDatasourceRequest();
        createRequest.setName("测试数据源");
        createRequest.setCode("TEST_DS");
        createRequest.setDatasourceType(DatasourceType.MYSQL.name());
        createRequest.setHost("localhost");
        createRequest.setPort(3306);
        createRequest.setDatabaseName("test_db");
        createRequest.setUsername("root");
        createRequest.setPassword("password");
    }

    @Test
    @DisplayName("创建数据源配置")
    void testCreateDatasource() throws Exception {
        when(datasourceConfigService.createDatasource(any(CreateDatasourceRequest.class)))
                .thenReturn(1L);

        mockMvc.perform(post("/api/v1/datasources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("创建数据源配置-参数校验失败")
    void testCreateDatasource_ValidationFailed() throws Exception {
        createRequest.setCode(""); // 空的编码

        mockMvc.perform(post("/api/v1/datasources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("更新数据源配置")
    void testUpdateDatasource() throws Exception {
        UpdateDatasourceRequest updateRequest = new UpdateDatasourceRequest();
        updateRequest.setName("更新后的数据源");

        when(datasourceConfigService.updateDatasource(eq(1L), any(UpdateDatasourceRequest.class)))
                .thenReturn(true);

        mockMvc.perform(put("/api/v1/datasources/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("数据源配置更新成功"));
    }

    @Test
    @DisplayName("删除数据源配置")
    void testDeleteDatasource() throws Exception {
        when(datasourceConfigService.deleteDatasource(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/datasources/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("数据源配置删除成功"));
    }

    @Test
    @DisplayName("获取数据源详情")
    void testGetDatasourceDetail() throws Exception {
        when(datasourceConfigService.getDatasourceDetail(1L)).thenReturn(datasourceDetailVO);

        mockMvc.perform(get("/api/v1/datasources/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("TEST_DS"))
                .andExpect(jsonPath("$.data.name").value("测试数据源"));
    }

    @Test
    @DisplayName("分页查询数据源列表")
    void testListDatasources() throws Exception {
        Page<DatasourceVO> page = new Page<>(1, 10);
        page.setRecords(List.of(datasourceVO));
        page.setTotal(1);
        page.setPages(1);

        when(datasourceConfigService.listDatasources(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/datasources")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].code").value("TEST_DS"));
    }

    @Test
    @DisplayName("启用数据源")
    void testEnableDatasource() throws Exception {
        when(datasourceConfigService.enableDatasource(1L)).thenReturn(true);

        mockMvc.perform(post("/api/v1/datasources/1/enable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("数据源启用成功"));
    }

    @Test
    @DisplayName("禁用数据源")
    void testDisableDatasource() throws Exception {
        when(datasourceConfigService.disableDatasource(1L)).thenReturn(true);

        mockMvc.perform(post("/api/v1/datasources/1/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("数据源禁用成功"));
    }

    @Test
    @DisplayName("测试数据源连接")
    void testConnection() throws Exception {
        ConnectionTestRequest request = new ConnectionTestRequest();
        request.setDatasourceType(DatasourceType.MYSQL.name());
        request.setHost("localhost");
        request.setPort(3306);

        ConnectionTestResponse response = ConnectionTestResponse.builder()
                .success(true)
                .message("连接成功")
                .responseTime(100L)
                .build();

        when(datasourceConfigService.testConnection(any(ConnectionTestRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/datasources/test-connection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("连接成功"));
    }

    @Test
    @DisplayName("获取数据源统计信息")
    void testGetStatistics() throws Exception {
        Map<String, Long> byType = new HashMap<>();
        byType.put("MYSQL", 10L);
        byType.put("POSTGRESQL", 5L);

        DatasourceStatisticsVO statistics = DatasourceStatisticsVO.builder()
                .totalCount(15L)
                .byType(byType)
                .syncedCount(10L)
                .unsyncedCount(5L)
                .build();

        when(datasourceConfigService.getStatistics()).thenReturn(statistics);

        mockMvc.perform(get("/api/v1/datasources/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(15));
    }

    @Test
    @DisplayName("验证编码唯一性")
    void testCheckCodeUnique() throws Exception {
        when(datasourceConfigService.isCodeUnique("NEW_CODE")).thenReturn(true);

        mockMvc.perform(get("/api/v1/datasources/check-code/NEW_CODE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unique").value(true));
    }
}
