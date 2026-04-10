package com.enterprise.edams.datasource.connector;

import com.enterprise.edams.datasource.dto.ConnectionTestRequest;

import java.util.Map;
import java.util.Properties;

/**
 * 数据源连接器接口
 */
public interface DatasourceConnector {

    /**
     * 测试数据库连接
     *
     * @param request 连接参数
     * @return 是否连接成功
     */
    boolean testConnection(ConnectionTestRequest request);

    /**
     * 获取连接对象
     *
     * @param request 连接参数
     * @return 连接对象
     */
    Object getConnection(ConnectionTestRequest request);

    /**
     * 关闭连接
     *
     * @param connection 连接对象
     */
    void closeConnection(Object connection);

    /**
     * 获取数据源类型
     *
     * @return 数据源类型
     */
    String getDatasourceType();

    /**
     * 构建JDBC URL
     *
     * @param request 连接参数
     * @return JDBC URL
     */
    String buildJdbcUrl(ConnectionTestRequest request);

    /**
     * 构建连接属性
     *
     * @param request 连接参数
     * @return 连接属性
     */
    Properties buildConnectionProperties(ConnectionTestRequest request);

    /**
     * 获取数据库元信息
     *
     * @param request 连接参数
     * @return 元信息
     */
    Map<String, Object> getMetadata(ConnectionTestRequest request);

    /**
     * 获取表列表
     *
     * @param request 连接参数
     * @param schema  模式名称
     * @return 表列表
     */
    default Map<String, Object> listTables(ConnectionTestRequest request, String schema) {
        return Map.of();
    }

    /**
     * 获取列信息
     *
     * @param request   连接参数
     * @param schema    模式名称
     * @param tableName 表名
     * @return 列信息
     */
    default Map<String, Object> listColumns(ConnectionTestRequest request, String schema, String tableName) {
        return Map.of();
    }
}
