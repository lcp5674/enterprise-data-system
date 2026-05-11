package com.enterprise.edams.analysis.datasource;

import com.enterprise.edams.analysis.metadata.ColumnMetadata;
import com.enterprise.edams.analysis.metadata.TableMetadata;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class AbstractDatasourceConnector implements DatasourceConnector {

    @Override
    public Connection getConnection(DatasourceConnectionInfo connectionInfo) throws Exception {
        String jdbcUrl = connectionInfo.getJdbcUrl();
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            jdbcUrl = buildJdbcUrl(connectionInfo);
        }

        String driverClassName = connectionInfo.getDriverClassName();
        if (driverClassName != null && !driverClassName.isEmpty()) {
            Class.forName(driverClassName);
        }

        Connection connection = DriverManager.getConnection(
                jdbcUrl,
                connectionInfo.getUsername(),
                connectionInfo.getPassword()
        );

        log.debug("Connection established to datasource: {}", connectionInfo.getDatasourceName());
        return connection;
    }

    protected abstract String buildJdbcUrl(DatasourceConnectionInfo connectionInfo);

    @Override
    public List<String> getTables(DatasourceConnectionInfo connectionInfo, String schema) throws Exception {
        List<String> tables = new ArrayList<>();

        try (Connection conn = getConnection(connectionInfo)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();

            String schemaPattern = connectionInfo.supportsSchema() ? schema : null;

            try (ResultSet rs = metaData.getTables(catalog, schemaPattern, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    String tableType = rs.getString("TABLE_TYPE");

                    if ("TABLE".equals(tableType) || "VIEW".equals(tableType)) {
                        tables.add(tableName);
                    }
                }
            }

            log.info("Found {} tables in datasource {} (schema: {})",
                    tables.size(), connectionInfo.getDatasourceName(), schema);
        }

        return tables;
    }

    @Override
    public TableMetadata getTableMetadata(DatasourceConnectionInfo connectionInfo, String schema, String tableName) throws Exception {
        TableMetadata.TableMetadataBuilder builder = TableMetadata.builder()
                .schemaName(schema)
                .tableName(tableName);

        try (Connection conn = getConnection(connectionInfo)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            String schemaPattern = connectionInfo.supportsSchema() ? schema : null;

            try (ResultSet rs = metaData.getTables(catalog, schemaPattern, tableName, new String[]{"TABLE", "VIEW"})) {
                if (rs.next()) {
                    builder.tableComment(rs.getString("REMARKS"));
                    builder.tableType(rs.getString("TABLE_TYPE"));
                }
            }

            List<ColumnMetadata> columns = new ArrayList<>();
            try (ResultSet rs = metaData.getColumns(catalog, schemaPattern, tableName, "%")) {
                while (rs.next()) {
                    ColumnMetadata column = buildColumnMetadata(rs);
                    columns.add(column);
                }
            }

            List<String> primaryKeys = new ArrayList<>();
            try (ResultSet rs = metaData.getPrimaryKeys(catalog, schemaPattern, tableName)) {
                while (rs.next()) {
                    primaryKeys.add(rs.getString("COLUMN_NAME"));
                }
            }

            for (ColumnMetadata column : columns) {
                if (primaryKeys.contains(column.getColumnName())) {
                    column.setPrimaryKey(true);
                }
            }

            List<String> foreignKeys = new ArrayList<>();
            try (ResultSet rs = metaData.getImportedKeys(catalog, schemaPattern, tableName)) {
                while (rs.next()) {
                    foreignKeys.add(rs.getString("FKCOLUMN_NAME"));
                }
            }

            for (ColumnMetadata column : columns) {
                if (foreignKeys.contains(column.getColumnName())) {
                    column.setForeignKey(true);
                }
            }

            builder.columns(columns);

            Long rowCount = getRowCount(connectionInfo, schema, tableName);
            builder.estimatedRowCount(rowCount);

            builder.dataSizeBytes(estimateTableSize(conn, schema, tableName, rowCount));
        }

        return builder.build();
    }

    protected abstract ColumnMetadata buildColumnMetadata(ResultSet rs) throws SQLException;

    @Override
    public List<List<Object>> getSampleData(DatasourceConnectionInfo connectionInfo, String schema,
                                            String tableName, int rowCount) throws Exception {
        List<List<Object>> sampleData = new ArrayList<>();

        try (Connection conn = getConnection(connectionInfo)) {
            String qualifiedTableName = getQualifiedTableName(schema, tableName);
            String query = String.format("SELECT * FROM %s LIMIT %d", qualifiedTableName, rowCount);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                ResultSetMetaData rsMetaData = rs.getMetaData();
                int columnCount = rsMetaData.getColumnCount();

                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = rs.getObject(i);
                        row.add(value);
                    }
                    sampleData.add(row);
                }
            }

            log.debug("Retrieved {} sample rows from table {}.{}", sampleData.size(), schema, tableName);
        }

        return sampleData;
    }

    protected abstract String getQualifiedTableName(String schema, String tableName);

    @Override
    public Long getRowCount(DatasourceConnectionInfo connectionInfo, String schema, String tableName) throws Exception {
        try (Connection conn = getConnection(connectionInfo)) {
            String qualifiedTableName = getQualifiedTableName(schema, tableName);
            String query = String.format("SELECT COUNT(*) FROM %s", qualifiedTableName);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0L;
    }

    protected Long estimateTableSize(Connection conn, String schema, String tableName, Long rowCount) {
        try {
            String qualifiedTableName = getQualifiedTableName(schema, tableName);
            String query = String.format("SELECT pg_total_relation_size('%s') AS size", qualifiedTableName);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    return rs.getLong("size");
                }
            }
        } catch (Exception e) {
            log.debug("Cannot estimate table size: {}", e.getMessage());
        }
        return null;
    }
}
