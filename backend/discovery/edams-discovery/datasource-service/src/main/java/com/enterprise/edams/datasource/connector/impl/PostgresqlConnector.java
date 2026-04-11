package com.enterprise.edams.datasource.connector.impl;

import com.enterprise.edams.datasource.constant.DatasourceType;
import com.enterprise.edams.datasource.connector.DatasourceConnector;
import com.enterprise.edams.datasource.dto.ConnectionTestRequest;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * PostgreSQL数据库连接器
 */
@Slf4j
public class PostgresqlConnector implements DatasourceConnector {

    @Override
    public boolean testConnection(ConnectionTestRequest request) {
        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            
            String url = buildJdbcUrl(request);
            Properties props = buildConnectionProperties(request);
            
            connection = DriverManager.getConnection(url, props);
            
            log.info("PostgreSQL连接测试成功: {}", request.getHost());
            return connection.isValid(5);
        } catch (Exception e) {
            log.error("PostgreSQL连接测试失败: {}", e.getMessage(), e);
            return false;
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public Object getConnection(ConnectionTestRequest request) {
        try {
            Class.forName("org.postgresql.Driver");
            
            String url = buildJdbcUrl(request);
            Properties props = buildConnectionProperties(request);
            
            return DriverManager.getConnection(url, props);
        } catch (Exception e) {
            log.error("获取PostgreSQL连接失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取PostgreSQL连接失败", e);
        }
    }

    @Override
    public void closeConnection(Object connection) {
        if (connection != null) {
            try {
                if (connection instanceof Connection) {
                    ((Connection) connection).close();
                }
            } catch (Exception e) {
                log.error("关闭PostgreSQL连接失败: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public String getDatasourceType() {
        return DatasourceType.POSTGRESQL.name();
    }

    @Override
    public String buildJdbcUrl(ConnectionTestRequest request) {
        if (request.getConnectionUrl() != null && !request.getConnectionUrl().isEmpty()) {
            return request.getConnectionUrl();
        }
        
        StringBuilder url = new StringBuilder("jdbc:postgresql://");
        url.append(request.getHost());
        url.append(":");
        url.append(request.getPort() != null ? request.getPort() : 5432);
        url.append("/");
        if (request.getDatabaseName() != null) {
            url.append(request.getDatabaseName());
        }
        
        return url.toString();
    }

    @Override
    public Properties buildConnectionProperties(ConnectionTestRequest request) {
        Properties props = new Properties();
        props.setProperty("user", request.getUsername());
        props.setProperty("password", request.getPassword());
        props.setProperty("loginTimeout", "5");
        props.setProperty("socketTimeout", "30");
        return props;
    }

    @Override
    public Map<String, Object> getMetadata(ConnectionTestRequest request) {
        Map<String, Object> metadata = new HashMap<>();
        
        Connection connection = null;
        try {
            connection = (Connection) getConnection(request);
            
            metadata.put("databaseProductName", connection.getMetaData().getDatabaseProductName());
            metadata.put("databaseProductVersion", connection.getMetaData().getDatabaseProductVersion());
            metadata.put("driverName", connection.getMetaData().getDriverName());
            metadata.put("driverVersion", connection.getMetaData().getDriverVersion());
            
        } catch (Exception e) {
            log.error("获取PostgreSQL元信息失败: {}", e.getMessage(), e);
            metadata.put("error", e.getMessage());
        } finally {
            closeConnection(connection);
        }
        
        return metadata;
    }

    @Override
    public Map<String, Object> listTables(ConnectionTestRequest request, String schema) {
        Map<String, Object> result = new HashMap<>();
        
        Connection connection = null;
        try {
            connection = (Connection) getConnection(request);
            
            String schemaName = schema != null ? schema : "public";
            
            String sql = "SELECT table_schema, table_name, table_type, obj_description((table_schema || '.' || table_name)::regclass, 'pg_class') as comment " +
                        "FROM information_schema.tables WHERE table_schema = '" + schemaName + "' ORDER BY table_name";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Map<String, Object> table = new HashMap<>();
                    table.put("schema", rs.getString("table_schema"));
                    table.put("name", rs.getString("table_name"));
                    table.put("type", rs.getString("table_type"));
                    table.put("comment", rs.getString("comment"));
                    result.put(rs.getString("table_name"), table);
                }
            }
        } catch (Exception e) {
            log.error("获取PostgreSQL表列表失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        } finally {
            closeConnection(connection);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> listColumns(ConnectionTestRequest request, String schema, String tableName) {
        Map<String, Object> result = new HashMap<>();
        
        Connection connection = null;
        try {
            connection = (Connection) getConnection(request);
            
            String schemaName = schema != null ? schema : "public";
            String sql = "SELECT column_name, data_type, udt_name, is_nullable, column_default, col_description((table_schema || '.' || table_name)::regclass, ordinal_position) as comment " +
                        "FROM information_schema.columns WHERE table_schema = '" + schemaName + "' AND table_name = '" + tableName + "' ORDER BY ordinal_position";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Map<String, Object> column = new HashMap<>();
                    column.put("name", rs.getString("column_name"));
                    column.put("dataType", rs.getString("data_type"));
                    column.put("udtName", rs.getString("udt_name"));
                    column.put("nullable", "YES".equals(rs.getString("is_nullable")));
                    column.put("defaultValue", rs.getString("column_default"));
                    column.put("comment", rs.getString("comment"));
                    result.put(rs.getString("column_name"), column);
                }
            }
        } catch (Exception e) {
            log.error("获取PostgreSQL列信息失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        } finally {
            closeConnection(connection);
        }
        
        return result;
    }
}
