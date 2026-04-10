package com.enterprise.edams.datasource.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.datasource.constant.DatasourceStatus;
import com.enterprise.edams.datasource.constant.DatasourceType;
import com.enterprise.edams.datasource.constant.HealthStatus;
import com.enterprise.edams.datasource.dto.*;
import com.enterprise.edams.datasource.entity.DatasourceConfig;
import com.enterprise.edams.datasource.exception.DatasourceException;
import com.enterprise.edams.datasource.service.impl.DatasourceConfigServiceImpl;
import com.enterprise.edams.datasource.vo.DatasourceDetailVO;
import com.enterprise.edams.datasource.vo.DatasourceVO;
import com.enterprise.edams.datasource.repository.DatasourceConfigRepository;
import com.enterprise.edams.datasource.connector.DatasourceConnector;
import com.enterprise.edams.datasource.connector.DatasourceConnectorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 数据源配置服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class DatasourceConfigServiceTest {

    @Mock
    private DatasourceConfigRepository datasourceConfigRepository;

    @Mock
    private DatasourceConnectorFactory connectorFactory;

    @Mock
    private DatasourceConnector connector;

    @InjectMocks
    private DatasourceConfigServiceImpl datasourceConfigService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private DatasourceConfig testConfig;
    private CreateDatasourceRequest createRequest;

    @BeforeEach
    void setUp() {
        // 创建测试数据源配置
        testConfig = new DatasourceConfig();
        testConfig.setId(1L);
        testConfig.setName("测试数据源");
        testConfig.setCode("TEST_DS");
        testConfig.setDatasourceType(DatasourceType.MYSQL.name());
        testConfig.setHost("localhost");
        testConfig.setPort(3306);
        testConfig.setDatabaseName("test_db");
        testConfig.setUsername("root");
        testConfig.setPasswordEnc("encrypted_password");
        testConfig.setStatus(DatasourceStatus.ACTIVE.name());
        testConfig.setHealthStatus(HealthStatus.HEALTHY.name());
        testConfig.setSyncEnabled(1);
        testConfig.setSyncInterval(60);
        testConfig.setCreatedBy("system");
        testConfig.setCreatedTime(LocalDateTime.now());
        testConfig.setUpdatedTime(LocalDateTime.now());
        testConfig.setDeleted(0);

        // 创建请求对象
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
    @DisplayName("创建数据源配置成功")
    void testCreateDatasource_Success() {
        // Given
        when(datasourceConfigRepository.selectByCode("TEST_DS")).thenReturn(null);
        when(datasourceConfigRepository.insert(any(DatasourceConfig.class))).thenReturn(1);

        // When
        Long id = datasourceConfigService.createDatasource(createRequest);

        // Then
        assertNotNull(id);
        verify(datasourceConfigRepository).insert(any(DatasourceConfig.class));
    }

    @Test
    @DisplayName("创建数据源配置失败-编码重复")
    void testCreateDatasource_DuplicateCode() {
        // Given
        when(datasourceConfigRepository.selectByCode("TEST_DS")).thenReturn(testConfig);

        // When & Then
        assertThrows(DatasourceException.class, () -> {
            datasourceConfigService.createDatasource(createRequest);
        });

        verify(datasourceConfigRepository, never()).insert(any());
    }

    @Test
    @DisplayName("更新数据源配置成功")
    void testUpdateDatasource_Success() {
        // Given
        when(datasourceConfigRepository.selectById(1L)).thenReturn(testConfig);
        when(datasourceConfigRepository.updateById(any(DatasourceConfig.class))).thenReturn(true);

        UpdateDatasourceRequest updateRequest = new UpdateDatasourceRequest();
        updateRequest.setName("更新后的数据源");
        updateRequest.setHost("192.168.1.1");

        // When
        boolean result = datasourceConfigService.updateDatasource(1L, updateRequest);

        // Then
        assertTrue(result);
        verify(datasourceConfigRepository).updateById(any(DatasourceConfig.class));
    }

    @Test
    @DisplayName("更新数据源配置失败-数据源不存在")
    void testUpdateDatasource_NotFound() {
        // Given
        when(datasourceConfigRepository.selectById(1L)).thenReturn(null);

        UpdateDatasourceRequest updateRequest = new UpdateDatasourceRequest();
        updateRequest.setName("更新后的数据源");

        // When & Then
        assertThrows(DatasourceException.class, () -> {
            datasourceConfigService.updateDatasource(1L, updateRequest);
        });

        verify(datasourceConfigRepository, never()).updateById(any());
    }

    @Test
    @DisplayName("删除数据源配置成功")
    void testDeleteDatasource_Success() {
        // Given
        when(datasourceConfigRepository.selectById(1L)).thenReturn(testConfig);
        when(datasourceConfigRepository.updateById(any(DatasourceConfig.class))).thenReturn(true);

        // When
        boolean result = datasourceConfigService.deleteDatasource(1L);

        // Then
        assertTrue(result);
        verify(datasourceConfigRepository).updateById(any(DatasourceConfig.class));
    }

    @Test
    @DisplayName("获取数据源详情成功")
    void testGetDatasourceDetail_Success() {
        // Given
        when(datasourceConfigRepository.selectById(1L)).thenReturn(testConfig);

        // When
        DatasourceDetailVO detail = datasourceConfigService.getDatasourceDetail(1L);

        // Then
        assertNotNull(detail);
        assertEquals("测试数据源", detail.getName());
        assertEquals("TEST_DS", detail.getCode());
        assertEquals(DatasourceType.MYSQL.name(), detail.getDatasourceType());
    }

    @Test
    @DisplayName("启用数据源成功")
    void testEnableDatasource_Success() {
        // Given
        testConfig.setStatus(DatasourceStatus.INACTIVE.name());
        when(datasourceConfigRepository.selectById(1L)).thenReturn(testConfig);
        when(datasourceConfigRepository.updateById(any(DatasourceConfig.class))).thenReturn(true);

        // When
        boolean result = datasourceConfigService.enableDatasource(1L);

        // Then
        assertTrue(result);
        verify(datasourceConfigRepository).updateById(any(DatasourceConfig.class));
    }

    @Test
    @DisplayName("禁用数据源成功")
    void testDisableDatasource_Success() {
        // Given
        testConfig.setStatus(DatasourceStatus.ACTIVE.name());
        when(datasourceConfigRepository.selectById(1L)).thenReturn(testConfig);
        when(datasourceConfigRepository.updateById(any(DatasourceConfig.class))).thenReturn(true);

        // When
        boolean result = datasourceConfigService.disableDatasource(1L);

        // Then
        assertTrue(result);
        verify(datasourceConfigRepository).updateById(any(DatasourceConfig.class));
    }

    @Test
    @DisplayName("测试连接成功")
    void testConnection_Success() {
        // Given
        ConnectionTestRequest request = new ConnectionTestRequest();
        request.setDatasourceType(DatasourceType.MYSQL.name());
        request.setHost("localhost");
        request.setPort(3306);

        when(connectorFactory.getConnector(DatasourceType.MYSQL.name())).thenReturn(connector);
        when(connector.testConnection(request)).thenReturn(true);

        // When
        ConnectionTestResponse response = datasourceConfigService.testConnection(request);

        // Then
        assertTrue(response.getSuccess());
        assertEquals("连接成功", response.getMessage());
    }

    @Test
    @DisplayName("测试连接失败")
    void testConnection_Failed() {
        // Given
        ConnectionTestRequest request = new ConnectionTestRequest();
        request.setDatasourceType(DatasourceType.MYSQL.name());
        request.setHost("invalid-host");
        request.setPort(3306);

        when(connectorFactory.getConnector(DatasourceType.MYSQL.name())).thenReturn(connector);
        when(connector.testConnection(request)).thenThrow(new RuntimeException("Connection refused"));

        // When
        ConnectionTestResponse response = datasourceConfigService.testConnection(request);

        // Then
        assertFalse(response.getSuccess());
        assertNotNull(response.getErrorDetail());
    }

    @Test
    @DisplayName("编码唯一性验证-唯一")
    void testIsCodeUnique_Unique() {
        // Given
        when(datasourceConfigRepository.selectByCode("NEW_CODE")).thenReturn(null);

        // When
        boolean unique = datasourceConfigService.isCodeUnique("NEW_CODE");

        // Then
        assertTrue(unique);
    }

    @Test
    @DisplayName("编码唯一性验证-不唯一")
    void testIsCodeUnique_NotUnique() {
        // Given
        when(datasourceConfigRepository.selectByCode("TEST_DS")).thenReturn(testConfig);

        // When
        boolean unique = datasourceConfigService.isCodeUnique("TEST_DS");

        // Then
        assertFalse(unique);
    }

    @Test
    @DisplayName("根据编码获取数据源")
    void testGetByCode() {
        // Given
        when(datasourceConfigRepository.selectByCode("TEST_DS")).thenReturn(testConfig);

        // When
        DatasourceConfig config = datasourceConfigService.getByCode("TEST_DS");

        // Then
        assertNotNull(config);
        assertEquals("TEST_DS", config.getCode());
    }
}
