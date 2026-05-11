package com.enterprise.edams.analysis.datasource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatasourceConnectionInfoTest {

    private DatasourceConnectionInfo mysqlConnection;
    private DatasourceConnectionInfo postgresqlConnection;
    private DatasourceConnectionInfo clickhouseConnection;

    @BeforeEach
    void setUp() {
        mysqlConnection = DatasourceConnectionInfo.builder()
                .datasourceId(1L)
                .datasourceCode("MYSQL_PROD")
                .datasourceName("MySQL生产库")
                .datasourceType("MYSQL")
                .host("localhost")
                .port(3306)
                .databaseName("testdb")
                .username("root")
                .password("password")
                .additionalParams("useSSL=false")
                .build();

        postgresqlConnection = DatasourceConnectionInfo.builder()
                .datasourceId(2L)
                .datasourceCode("POSTGRES_PROD")
                .datasourceName("PostgreSQL生产库")
                .datasourceType("POSTGRESQL")
                .host("localhost")
                .port(5432)
                .databaseName("testdb")
                .username("postgres")
                .password("password")
                .build();

        clickhouseConnection = DatasourceConnectionInfo.builder()
                .datasourceId(3L)
                .datasourceCode("CLICKHOUSE_PROD")
                .datasourceName("ClickHouse分析库")
                .datasourceType("CLICKHOUSE")
                .host("localhost")
                .port(8123)
                .databaseName("default")
                .username("default")
                .password("")
                .build();
    }

    @Test
    void testMySQLJdbcUrlTemplate() {
        String jdbcUrl = mysqlConnection.getJdbcUrlTemplate();
        
        assertTrue(jdbcUrl.startsWith("jdbc:mysql://"));
        assertTrue(jdbcUrl.contains("localhost:3306"));
        assertTrue(jdbcUrl.contains("testdb"));
        assertTrue(jdbcUrl.contains("useSSL=false"));
    }

    @Test
    void testPostgreSQLJdbcUrlTemplate() {
        String jdbcUrl = postgresqlConnection.getJdbcUrlTemplate();
        
        assertTrue(jdbcUrl.startsWith("jdbc:postgresql://"));
        assertTrue(jdbcUrl.contains("localhost:5432"));
        assertTrue(jdbcUrl.contains("testdb"));
    }

    @Test
    void testClickHouseJdbcUrlTemplate() {
        String jdbcUrl = clickhouseConnection.getJdbcUrlTemplate();
        
        assertTrue(jdbcUrl.startsWith("jdbc:clickhouse://"));
        assertTrue(jdbcUrl.contains("localhost:8123"));
        assertTrue(jdbcUrl.contains("default"));
    }

    @Test
    void testMySQLDriverClassName() {
        assertEquals("com.mysql.cj.jdbc.Driver", mysqlConnection.getDriverClassName());
    }

    @Test
    void testPostgreSQLDriverClassName() {
        assertEquals("org.postgresql.Driver", postgresqlConnection.getDriverClassName());
    }

    @Test
    void testClickHouseDriverClassName() {
        assertEquals("com.clickhouse.jdbc.ClickHouseDriver", clickhouseConnection.getDriverClassName());
    }

    @Test
    void testSupportsSchema_PostgreSQL() {
        assertTrue(postgresqlConnection.supportsSchema());
    }

    @Test
    void testSupportsSchema_MySQL() {
        assertFalse(mysqlConnection.supportsSchema());
    }

    @Test
    void testSupportsSchema_ClickHouse() {
        assertFalse(clickhouseConnection.supportsSchema());
    }

    @Test
    void testDatasourceTypeCaseInsensitive() {
        DatasourceConnectionInfo upperCase = DatasourceConnectionInfo.builder()
                .datasourceType("MYSQL")
                .build();
        assertEquals("com.mysql.cj.jdbc.Driver", upperCase.getDriverClassName());

        DatasourceConnectionInfo lowerCase = DatasourceConnectionInfo.builder()
                .datasourceType("mysql")
                .build();
        assertEquals("com.mysql.cj.jdbc.Driver", lowerCase.getDriverClassName());
    }
}
