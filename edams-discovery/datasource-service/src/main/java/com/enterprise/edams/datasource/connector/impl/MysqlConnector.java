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
 * MySQL数据库连接器
 */
@Slf4j
public class MysqlConnector implements DatasourceConnector {

    @Override
    public boolean testConnection(ConnectionTestRequest request) {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            String url = buildJdbcUrl(request);
            Properties props = buildConnectionProperties(request);
            
            connection = DriverManager.getConnection(url, props);
            
            log.info("MySQL连接测试成功: {}", request.getHost());
            return connection.isValid(5);
        } catch (Exception e) {
            log.error("MySQL连接测试失败: {}", e.getMessage(), e);
            return false;
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public Object getConnection(ConnectionTestRequest request) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            String url = buildJdbcUrl(request);
            Properties props = buildConnectionProperties(request);
            
            return DriverManager.getConnection(url, props);
        } catch (Exception e) {
            log.error("获取MySQL连接失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取MySQL连接失败", e);
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
                log.error("关闭MySQL连接失败: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public String getDatasourceType() {
        return DatasourceType.MYSQL.name();
    }

    @Override
    public String buildJdbcUrl(ConnectionTestRequest request) {
        if (request.getConnectionUrl() != null && !request.getConnectionUrl().isEmpty()) {
            return request.getConnectionUrl();
        }
        
        StringBuilder url = new StringBuilder("jdbc:mysql://");
        url.append(request.getHost());
        url.append(":");
        url.append(request.getPort() != null ? request.getPort() : 3306);
        url.append("/");
        if (request.getDatabaseName() != null) {
            url.append(request.getDatabaseName());
        }
        
        // 添加默认参数
        url.append("?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai");
        
        // 添加用户自定义参数
        if (request.getJdbcParams() != null) {
            request.getJdbcParams().forEach((key, value) -> {
                url.append("&").append(key).append("=").append(value);
            });
        }
        
        return url.toString();
    }

    @Override
    public Properties buildConnectionProperties(ConnectionTestRequest request) {
        Properties props = new Properties();
        props.setProperty("user", request.getUsername());
        props.setProperty("password", request.getPassword());
        props.setProperty("connectTimeout", "5000");
        props.setProperty("socketTimeout", "30000");
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
            metadata.put("catalog", connection.getCatalog());
            
        } catch (Exception e) {
            log.error("获取MySQL元信息失败: {}", e.getMessage(), e);
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
            
            String catalog = connection.getCatalog();
            String schemaPattern = schema != null ? schema : null;
            
            String sql = schema != null 
                    ? "SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_TYPE, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = ?"
                    : "SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_TYPE, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_CATALOG = ?";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Map<String, Object> table = new HashMap<>();
                    table.put("schema", rs.getString("TABLE_SCHEMA"));
                    table.put("name", rs.getString("TABLE_NAME"));
                    table.put("type", rs.getString("TABLE_TYPE"));
                    table.put("comment", rs.getString("TABLE_COMMENT"));
                    result.put(rs.getString("TABLE_NAME"), table);
                }
            }
        } catch (Exception e) {
            log.error("获取MySQL表列表失败: {}", e.getMessage(), e);
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
            
            String sql = "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_DEFAULT, COLUMN_COMMENT " +
                        "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Map<String, Object> column = new HashMap<>();
                    column.put("name", rs.getString("COLUMN_NAME"));
                    column.put("dataType", rs.getString("DATA_TYPE"));
                    column.put("columnType", rs.getString("COLUMN_TYPE"));
                    column.put("nullable", "YES".equals(rs.getString("IS_NULLABLE")));
                    column.put("key", rs.getString("COLUMN_KEY"));
                    column.put("defaultValue", rs.getString("COLUMN_DEFAULT"));
                    column.put("comment", rs.getString("COLUMN_COMMENT"));
                    result.put(rs.getString("COLUMN_NAME"), column);
                }
            }
        } catch (Exception e) {
            log.error("获取MySQL列信息失败: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        } finally {
            closeConnection(connection);
        }
        
        return result;
    }
}
