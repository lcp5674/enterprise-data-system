package com.enterprise.edams.analysis.service;

import com.enterprise.edams.analysis.datasource.DatasourceConnectionInfo;
import com.enterprise.edams.analysis.datasource.DatasourceConnectorFactory;
import com.enterprise.edams.analysis.datasource.MySQLConnector;
import com.enterprise.edams.analysis.datasource.PostgreSQLConnector;
import com.enterprise.edams.analysis.datasource.ClickHouseConnector;
import com.enterprise.edams.analysis.exception.AnalysisException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatasourceRegistryTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetDatasource_ValidId() {
        DatasourceConnectionInfo info = DatasourceConnectionService.DatasourceRegistry.getDatasource(1L);
        
        assertNotNull(info);
        assertEquals(1L, info.getDatasourceId());
        assertNotNull(info.getDatasourceType());
    }

    @Test
    void testGetDatasource_InvalidId() {
        assertThrows(AnalysisException.class, () -> {
            DatasourceConnectionService.DatasourceRegistry.getDatasource(999L);
        });
    }

    @Test
    void testGetAllDatasources() {
        List<DatasourceConnectionInfo> datasources = DatasourceConnectionService.DatasourceRegistry.getAllDatasources();
        
        assertNotNull(datasources);
        assertFalse(datasources.isEmpty());
    }

    @Test
    void testContains() {
        assertTrue(DatasourceConnectionService.DatasourceRegistry.contains(1L));
        assertFalse(DatasourceConnectionService.DatasourceRegistry.contains(999L));
    }

    @Test
    void testRegisterDatasource() {
        DatasourceConnectionInfo newDatasource = DatasourceConnectionInfo.builder()
                .datasourceId(100L)
                .datasourceCode("TEST_DS")
                .datasourceName("测试数据源")
                .datasourceType("MYSQL")
                .host("testhost")
                .port(3306)
                .databaseName("testdb")
                .username("testuser")
                .password("testpass")
                .build();

        DatasourceConnectionService.DatasourceRegistry.registerDatasource(newDatasource);

        DatasourceConnectionInfo retrieved = DatasourceConnectionService.DatasourceRegistry.getDatasource(100L);
        assertNotNull(retrieved);
        assertEquals("TEST_DS", retrieved.getDatasourceCode());
    }

    @Test
    void testRemoveDatasource() {
        DatasourceConnectionInfo newDatasource = DatasourceConnectionInfo.builder()
                .datasourceId(200L)
                .datasourceCode("TEMP_DS")
                .datasourceName("临时数据源")
                .datasourceType("MYSQL")
                .host("localhost")
                .port(3306)
                .databaseName("testdb")
                .username("root")
                .password("root")
                .build();

        DatasourceConnectionService.DatasourceRegistry.registerDatasource(newDatasource);
        assertTrue(DatasourceConnectionService.DatasourceRegistry.contains(200L));

        DatasourceConnectionService.DatasourceRegistry.removeDatasource(200L);
        assertFalse(DatasourceConnectionService.DatasourceRegistry.contains(200L));
    }

    @Test
    void testDefaultDatasourcesInitialized() {
        List<DatasourceConnectionInfo> datasources = DatasourceConnectionService.DatasourceRegistry.getAllDatasources();
        
        assertTrue(datasources.size() >= 3, "Should have at least 3 default datasources (MySQL, PostgreSQL, ClickHouse)");
        
        boolean hasMySQL = datasources.stream().anyMatch(ds -> "MYSQL".equals(ds.getDatasourceType()));
        boolean hasPostgreSQL = datasources.stream().anyMatch(ds -> "POSTGRESQL".equals(ds.getDatasourceType()));
        boolean hasClickHouse = datasources.stream().anyMatch(ds -> "CLICKHOUSE".equals(ds.getDatasourceType()));
        
        assertTrue(hasMySQL, "Should have MySQL datasource");
        assertTrue(hasPostgreSQL, "Should have PostgreSQL datasource");
        assertTrue(hasClickHouse, "Should have ClickHouse datasource");
    }

    @Test
    void testJdbcUrlTemplateGeneration() {
        DatasourceConnectionInfo mysqlDs = DatasourceConnectionService.DatasourceRegistry.getDatasource(1L);
        
        String jdbcUrl = mysqlDs.getJdbcUrlTemplate();
        
        assertNotNull(jdbcUrl);
        assertTrue(jdbcUrl.startsWith("jdbc:mysql://"));
        assertTrue(jdbcUrl.contains(mysqlDs.getHost()));
        assertTrue(jdbcUrl.contains(String.valueOf(mysqlDs.getPort())));
        assertTrue(jdbcUrl.contains(mysqlDs.getDatabaseName()));
    }
}
