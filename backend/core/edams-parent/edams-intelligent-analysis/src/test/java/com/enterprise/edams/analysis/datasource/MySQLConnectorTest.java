package com.enterprise.edams.analysis.datasource;

import com.enterprise.edams.analysis.metadata.ColumnMetadata;
import com.enterprise.edams.analysis.metadata.TableMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MySQLConnectorTest {

    private MySQLConnector connector;
    private DatasourceConnectionInfo connectionInfo;

    @BeforeEach
    void setUp() {
        connector = new MySQLConnector();
        
        String host = System.getenv("MYSQL_HOST");
        String port = System.getenv("MYSQL_PORT");
        String db = System.getenv("MYSQL_DATABASE");
        String user = System.getenv("MYSQL_USER");
        String pass = System.getenv("MYSQL_PASSWORD");
        
        if (host == null) host = "localhost";
        if (port == null) port = "3306";
        if (db == null) db = "test";
        if (user == null) user = "root";
        if (pass == null) pass = "root";

        connectionInfo = DatasourceConnectionInfo.builder()
                .datasourceId(1L)
                .datasourceCode("MYSQL_TEST")
                .datasourceName("MySQL测试库")
                .datasourceType("MYSQL")
                .host(host)
                .port(Integer.parseInt(port))
                .databaseName(db)
                .username(user)
                .password(pass)
                .additionalParams("useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true")
                .build();
    }

    @Test
    void testConnectorType() {
        assertEquals("MYSQL", connector.getConnectorType());
    }

    @Test
    void testBuildJdbcUrl() {
        String jdbcUrl = connector.buildJdbcUrl(connectionInfo);
        
        assertNotNull(jdbcUrl);
        assertTrue(jdbcUrl.startsWith("jdbc:mysql://"));
        assertTrue(jdbcUrl.contains(host()));
        assertTrue(jdbcUrl.contains(connectionInfo.getDatabaseName()));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "MYSQL_HOST", matches = ".*")
    void testGetConnection() throws Exception {
        try (Connection conn = connector.getConnection(connectionInfo)) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "MYSQL_HOST", matches = ".*")
    void testGetTables() throws Exception {
        List<String> tables = connector.getTables(connectionInfo, null);
        
        assertNotNull(tables);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "MYSQL_HOST", matches = ".*")
    void testGetTableMetadata() throws Exception {
        List<String> tables = connector.getTables(connectionInfo, null);
        
        if (!tables.isEmpty()) {
            String firstTable = tables.get(0);
            TableMetadata metadata = connector.getTableMetadata(connectionInfo, null, firstTable);
            
            assertNotNull(metadata);
            assertEquals(firstTable, metadata.getTableName());
            assertNotNull(metadata.getColumns());
        }
    }

    private String host() {
        String host = System.getenv("MYSQL_HOST");
        return host != null ? host : "localhost";
    }
}
