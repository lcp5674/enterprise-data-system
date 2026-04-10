package com.enterprise.edams.datasource.connector;

import com.enterprise.edams.datasource.constant.DatasourceType;
import com.enterprise.edams.datasource.connector.impl.MysqlConnector;
import com.enterprise.edams.datasource.dto.ConnectionTestRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MySQL连接器单元测试
 */
class MysqlConnectorTest {

    private MysqlConnector connector;
    private ConnectionTestRequest request;

    @BeforeEach
    void setUp() {
        connector = new MysqlConnector();
        
        request = new ConnectionTestRequest();
        request.setDatasourceType(DatasourceType.MYSQL.name());
        request.setHost("localhost");
        request.setPort(3306);
        request.setDatabaseName("test_db");
        request.setUsername("root");
        request.setPassword("password");
    }

    @Test
    @DisplayName("获取数据源类型")
    void testGetDatasourceType() {
        assertEquals(DatasourceType.MYSQL.name(), connector.getDatasourceType());
    }

    @Test
    @DisplayName("构建JDBC URL")
    void testBuildJdbcUrl() {
        String url = connector.buildJdbcUrl(request);
        
        assertTrue(url.startsWith("jdbc:mysql://"));
        assertTrue(url.contains("localhost:3306"));
        assertTrue(url.contains("test_db"));
        assertTrue(url.contains("useUnicode=true"));
        assertTrue(url.contains("characterEncoding=UTF-8"));
    }

    @Test
    @DisplayName("构建JDBC URL-使用自定义URL")
    void testBuildJdbcUrl_CustomUrl() {
        request.setConnectionUrl("jdbc:mysql://custom-host:3306/custom_db?useSSL=false");
        
        String url = connector.buildJdbcUrl(request);
        
        assertEquals("jdbc:mysql://custom-host:3306/custom_db?useSSL=false", url);
    }

    @Test
    @DisplayName("构建JDBC URL-包含自定义参数")
    void testBuildJdbcUrl_WithParams() {
        request.setJdbcParams(Map.of("useSSL", "false", "rewriteBatchedStatements", "true"));
        
        String url = connector.buildJdbcUrl(request);
        
        assertTrue(url.contains("useSSL=false"));
        assertTrue(url.contains("rewriteBatchedStatements=true"));
    }

    @Test
    @DisplayName("构建连接属性")
    void testBuildConnectionProperties() {
        var props = connector.buildConnectionProperties(request);
        
        assertEquals("root", props.getProperty("user"));
        assertEquals("password", props.getProperty("password"));
        assertEquals("5000", props.getProperty("connectTimeout"));
        assertEquals("30000", props.getProperty("socketTimeout"));
    }

    @Test
    @DisplayName("测试连接-无效连接")
    void testConnection_Invalid() {
        request.setHost("invalid-host");
        request.setPort(9999);
        
        // 这个测试预期会失败，因为连接无效
        boolean result = connector.testConnection(request);
        
        assertFalse(result);
    }

    @Test
    @DisplayName("关闭连接-空连接")
    void testCloseConnection_Null() {
        // 不应该抛出异常
        assertDoesNotThrow(() -> connector.closeConnection(null));
    }
}
