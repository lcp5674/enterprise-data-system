package com.enterprise.edams.analysis.datasource;

import com.enterprise.edams.analysis.metadata.ColumnMetadata;
import com.enterprise.edams.analysis.metadata.TableMetadata;

import java.sql.Connection;
import java.util.List;

public interface DatasourceConnector {

    Connection getConnection(DatasourceConnectionInfo connectionInfo) throws Exception;

    List<String> getTables(DatasourceConnectionInfo connectionInfo, String schema) throws Exception;

    TableMetadata getTableMetadata(DatasourceConnectionInfo connectionInfo, String schema, String tableName) throws Exception;

    List<List<Object>> getSampleData(DatasourceConnectionInfo connectionInfo, String schema, String tableName, int rowCount) throws Exception;

    Long getRowCount(DatasourceConnectionInfo connectionInfo, String schema, String tableName) throws Exception;

    String getConnectorType();

    default void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                // Ignore close errors
            }
        }
    }
}
