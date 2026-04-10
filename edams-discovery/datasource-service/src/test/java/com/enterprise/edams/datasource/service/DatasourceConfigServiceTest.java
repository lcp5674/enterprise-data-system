package com.enterprise.edams.datasource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.datasource.constant.DatasourceStatus;
import com.enterprise.edams.datasource.constant.DatasourceType;
import com.enterprise.edams.datasource.constant.HealthStatus;
import com.enterprise.edams.datasource.connector.DatasourceConnector;
import com.enterprise.edams.datasource.connector.DatasourceConnectorFactory;
import com.enterprise.edams.datasource.dto.*;
import com.enterprise.edams.datasource.entity.DatasourceConfig;
import com.enterprise.edams.datasource.exception.DatasourceException;
import com.enterprise.edams.datasource.repository.DatasourceConfigRepository;
import com.enterprise.edams.datasource.service.impl.DatasourceConfigServiceImpl;
import com.enterprise.edams.datasource.vo.DatasourceDetailVO;
import com.enterprise.edams.datasource.vo.DatasourceStatisticsVO;
import com.enterprise.edams.datasource.vo.DatasourceVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * DatasourceConfigService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("数据源配置服务测试")
class DatasourceConfigServiceTest {

    @Mock
    private DatasourceConfigRepository datasourceConfigRepository;

    @Mock
    private DatasourceConnectorFactory connectorFactory;

    @Mock
    private DatasourceConnector datasourceConnector;

    @InjectMocks
    private DatasourceConfigServiceImpl datasourceConfigService;

    private ObjectMapper objectMapper;
    private DatasourceConfig testConfig;
    private CreateDatasourceRequest createRequest;
    private UpdateDatasourceRequest updateRequest;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // 使用反射注入objectMapper
        var field = DatasourceConfigServiceImpl.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(datasourceConfigService, objectMapper);

        testConfig = new DatasourceConfig();
        testConfig.setId(1L);
        testConfig.setCode("DS-001");
        testConfig.setName("测试数据源");
        testConfig.setDatasourceType("MYSQL");
        testConfig.setHost("localhost");
        testConfig.setPort(3306);
        testConfig.setDatabaseName("test_db");
        testConfig.setUsername("root");
        testConfig.setPasswordEnc("encrypted_password");
        testConfig.setStatus(DatasourceStatus.INACTIVE.name());
        testConfig.setHealthStatus(HealthStatus.UNKNOWN.name());
        testConfig.setSyncEnabled(0);
        testConfig.setCreatedBy("system");
        testConfig.setCreateTime(LocalDateTime.now());
        testConfig.setDeleted(0);

        createRequest = new CreateDatasourceRequest();
        createRequest.setCode("DS-001");
        createRequest.setName("测试数据源");
        createRequest.setDatasourceType("MYSQL");
        createRequest.setHost("localhost");
        createRequest.setPort(3306);
        createRequest.setDatabaseName("test_db");
        createRequest.setUsername("root");
        createRequest.setPassword("password");
        createRequest.setSyncEnabled(false);

        updateRequest = new UpdateDatasourceRequest();
        updateRequest.setName("更新后的数据源");
        updateRequest.setDescription("更新描述");
    }

    @Test
    @DisplayName("创建数据源 - 成功")
    void testCreateDatasource_Success() {
        // Given
        when(datasourceConfigRepository.selectByCode("DS-001")).thenReturn(null);
        when(datasourceConfigRepository.insert(any(DatasourceConfig.class))).thenReturn(1);

        // When
        Long id = datasourceConfigService.createDatasource(createRequest);

        // Then
        assertThat(id).isEqualTo(1L);
        verify(datasourceConfigRepository, times(1)).insert(any(DatasourceConfig.class));
    }

    @Test
    @DisplayName("创建数据源 - 编码已存在")
    void testCreateDatasource_DuplicateCode() {
        // Given
        when(datasourceConfigRepository.selectByCode("DS-001")).thenReturn(testConfig);

        // When & Then
        assertThatThrownBy(() -> datasourceConfigService.createDatasource(createRequest))
                .isInstanceOf(DatasourceException.class)
                .hasMessageContaining("数据源编码已存在");
    }

    @Test
    @DisplayName("更新数据源 - 成功")
    void testUpdateDatasource_Success() {
        // Given
        when(datasourceConfigRepository.selectById(1L)).thenReturn(testConfig);
        when(datasourceConfigRepository.updateById(any(DatasourceConfig.class))).thenReturn(true);

        // When
        boolean result = datasourceConfigService.updateDatasource(1L, updateRequest);

        // Then
        assertThat(result).isTrue();
        verify(datasourceConfigRepository, times(1)).updateById(any(DatasourceConfig.class));
    }

    @Test
    @DisplayName("更新数据源 - 数据源不存在")
    void testUpdateDatasource_NotFound() {
        // Given
        when(datasourceConfigRepository.selectById(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> datasourceConfigService.updateDatasource(999L, updateRequest))
                .isInstanceOf(DatasourceException.class)
                .hasMessageContaining("数据源不存在");
    }

    @Test
    @DisplayName("删除数据源 - 成功（逻辑删除）")
    void testDeleteDatasource_Success() {
        // Given
        when(datasourceConfigRepository.selectById(1L)).thenReturn(testConfig);
        when(datasourceConfigRepository.updateById(any(DatasourceConfig.class))).thenReturn(true);

        // When
        boolean result = datasourceConfigService.deleteDatasource(1L);

        // Then
        assertThat(result).isTrue();
        verify(datasourceConfigRepository, times(1)).updateById(any(DatasourceConfig.class));
    }

    @Test
    @DisplayName("删除数据源 - 数据源不存在")
    void testDeleteDatasource_NotFound() {
        // Given
        when(datasourceConfigRepository.selectById(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> datasourceConfigService.deleteDatasource(999L))
                .isInstanceOf(DatasourceException.class)
                .hasMessageContaining("数据源不存在");
    }

    @Test
    @DisplayName("获取数据源详情 - 成功")
    void testGetDatasourceDetail_Success() {
        // Given
        when(datasourceConfigRepository.selectById(1L)).thenReturn(testConfig);

        // When
        DatasourceDetailVO result = datasourceConfigService.getDatasourceDetail(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("DS-001");
    }

    @Test
    @DisplayName("获取数据源详情 - 数据源不存在")
    void testGetDatasourceDetail_NotFound() {
        // Given
        when(datasourceConfigRepository.selectById(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> datasourceConfigService.getDatasourceDetail(999L))
                .isInstanceOf(DatasourceException.class)
                .hasMessageContaining("数据源不存在");
    }

    @Test
    @DisplayName("分页查询数据源列表")
    void testListDatasources() {
        // Given
        DatasourceQueryDTO query = new DatasourceQueryDTO();
        query.setPageNum(1);
        query.setPageSize(10);

        Page<DatasourceConfig> page = new Page<>(1, 10);
        Page<DatasourceConfig> resultPage = new Page<>(1, 10);
        resultPage.setRecords(List.of(testConfig));
        resultPage.setTotal(1);

        when(datasourceConfigRepository.selectPageList(any(Page.class), any(DatasourceQueryDTO.class)))
                .thenReturn(resultPage);

        // When
        IPage<DatasourceVO> result = datasourceConfigService.listDatasources(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("启用数据源 - 成功")
    void testEnableDatasource_Success() {
        // Given
        when(datasourceConfigRepository.selectById(1L)).thenReturn(testConfig);
        when(datasourceConfigRepository.updateById(any(DatasourceConfig.class))).thenReturn(true);

        // When
        boolean result = datasourceConfigService.enableDatasource(1L);

        // Then
        assertThat(result).isTrue();
        verify(datasourceConfigRepository, times(1)).updateById(any(DatasourceConfig.class));
    }

    @Test
    @DisplayName("启用数据源 - 数据源不存在")
    void testEnableDatasource_NotFound() {
        // Given
        when(datasourceConfigRepository.selectById(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> datasourceConfigService.enableDatasource(999L))
                .isInstanceOf(DatasourceException.class)
                .hasMessageContaining("数据源不存在");
    }

    @Test
    @DisplayName("禁用数据源 - 成功")
    void testDisableDatasource_Success() {
        // Given
        when(datasourceConfigRepository.selectById(1L)).thenReturn(testConfig);
        when(datasourceConfigRepository.updateById(any(DatasourceConfig.class))).thenReturn(true);

        // When
        boolean result = datasourceConfigService.disableDatasource(1L);

        // Then
        assertThat(result).isTrue();
        verify(datasourceConfigRepository, times(1)).updateById(any(DatasourceConfig.class));
    }

    @Test
    @DisplayName("测试数据源连接 - 成功")
    void testTestConnection_Success() {
        // Given
        ConnectionTestRequest request = new ConnectionTestRequest();
        request.setDatasourceType("MYSQL");
        request.setHost("localhost");
        request.setPort(3306);

        when(connectorFactory.getConnector("MYSQL")).thenReturn(datasourceConnector);
        when(datasourceConnector.testConnection(any(ConnectionTestRequest.class))).thenReturn(true);

        // When
        ConnectionTestResponse result = datasourceConfigService.testConnection(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("连接成功");
    }

    @Test
    @DisplayName("测试数据源连接 - 失败")
    void testTestConnection_Failure() {
        // Given
        ConnectionTestRequest request = new ConnectionTestRequest();
        request.setDatasourceType("MYSQL");
        request.setHost("invalid-host");
        request.setPort(3306);

        when(connectorFactory.getConnector("MYSQL")).thenReturn(datasourceConnector);
        when(datasourceConnector.testConnection(any(ConnectionTestRequest.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        // When
        ConnectionTestResponse result = datasourceConfigService.testConnection(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getMessage()).contains("连接失败");
    }

    @Test
    @DisplayName("测试已有数据源连接 - 成功")
    void testTestDatasourceConnection_Success() {
        // Given
        when(datasourceConfigRepository.selectById(1L)).thenReturn(testConfig);
        when(connectorFactory.getConnector("MYSQL")).thenReturn(datasourceConnector);
        when(datasourceConnector.testConnection(any(ConnectionTestRequest.class))).thenReturn(true);
        when(datasourceConfigRepository.updateById(any(DatasourceConfig.class))).thenReturn(true);

        // When
        ConnectionTestResponse result = datasourceConfigService.testDatasourceConnection(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSuccess()).isTrue();
        verify(datasourceConfigRepository, times(1)).updateById(any(DatasourceConfig.class));
    }

    @Test
    @DisplayName("测试已有数据源连接 - 数据源不存在")
    void testTestDatasourceConnection_NotFound() {
        // Given
        when(datasourceConfigRepository.selectById(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> datasourceConfigService.testDatasourceConnection(999L))
                .isInstanceOf(DatasourceException.class)
                .hasMessageContaining("数据源不存在");
    }

    @Test
    @DisplayName("获取数据源统计信息")
    void testGetStatistics() {
        // Given
        when(datasourceConfigRepository.countByType(anyString())).thenReturn(5L);
        when(datasourceConfigRepository.countByStatus(anyString())).thenReturn(3L);
        when(datasourceConfigRepository.countByHealthStatus(anyString())).thenReturn(2L);
        when(datasourceConfigRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);
        when(datasourceConfigRepository.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // When
        DatasourceStatisticsVO result = datasourceConfigService.getStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("根据编码获取数据源 - 成功")
    void testGetByCode_Success() {
        // Given
        when(datasourceConfigRepository.selectByCode("DS-001")).thenReturn(testConfig);

        // When
        DatasourceConfig result = datasourceConfigService.getByCode("DS-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("DS-001");
    }

    @Test
    @DisplayName("根据编码获取数据源 - 不存在")
    void testGetByCode_NotFound() {
        // Given
        when(datasourceConfigRepository.selectByCode("NON-EXISTENT")).thenReturn(null);

        // When
        DatasourceConfig result = datasourceConfigService.getByCode("NON-EXISTENT");

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("批量获取数据源")
    void testListByCodes() {
        // Given
        List<String> codes = List.of("DS-001", "DS-002");
        when(datasourceConfigRepository.selectByCodes(codes)).thenReturn(List.of(testConfig));

        // When
        List<DatasourceConfig> result = datasourceConfigService.listByCodes(codes);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("验证编码唯一性 - 唯一")
    void testIsCodeUnique_True() {
        // Given
        when(datasourceConfigRepository.selectByCode("UNIQUE-CODE")).thenReturn(null);

        // When
        boolean result = datasourceConfigService.isCodeUnique("UNIQUE-CODE");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("验证编码唯一性 - 不唯一")
    void testIsCodeUnique_False() {
        // Given
        when(datasourceConfigRepository.selectByCode("DS-001")).thenReturn(testConfig);

        // When
        boolean result = datasourceConfigService.isCodeUnique("DS-001");

        // Then
        assertThat(result).isFalse();
    }
}
