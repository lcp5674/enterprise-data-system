package com.enterprise.edams.analysis.datasource;

import com.enterprise.edams.analysis.metadata.ColumnMetadata;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class PostgreSQLConnector extends AbstractDatasourceConnector {

    @Override
    protected String buildJdbcUrl(DatasourceConnectionInfo connectionInfo) {
        StringBuilder url = new StringBuilder("jdbc:postgresql://");
        url.append(connectionInfo.getHost()).append(":");
        url.append(connectionInfo.getPort()).append("/");
        url.append(connectionInfo.getDatabaseName());
        return url.toString();
    }

    @Override
    public String getConnectorType() {
        return "POSTGRESQL";
    }

    @Override
    protected ColumnMetadata buildColumnMetadata(ResultSet rs) throws SQLException {
        return ColumnMetadata.builder()
                .columnName(rs.getString("COLUMN_NAME"))
                .dataType(rs.getString("TYPE_NAME"))
                .columnType(rs.getString("TYPE_NAME") + "(" + rs.getInt("COLUMN_SIZE") + ")")
                .columnComment(rs.getString("REMARKS"))
                .nullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable)
                .ordinalPosition(rs.getInt("ORDINAL_POSITION"))
                .defaultValue(rs.getString("COLUMN_DEF"))
                .characterMaximumLength(rs.getInt("CHAR_OCTET_LENGTH") > 0 ? rs.getInt("CHAR_OCTET_LENGTH") : null)
                .numericPrecision(rs.getInt("NUMERIC_PRECISION") > 0 ? rs.getInt("NUMERIC_PRECISION") : null)
                .numericScale(rs.getInt("NUM_SCALE") > 0 ? rs.getInt("NUM_SCALE") : null)
                .build();
    }

    @Override
    protected String getQualifiedTableName(String schema, String tableName) {
        if (schema != null && !schema.isEmpty()) {
            return "\"" + schema + "\".\"" + tableName + "\"";
        }
        return "\"" + tableName + "\"";
    }

    @Override
    protected Long estimateTableSize(java.sql.Connection conn, String schema, String tableName, Long rowCount) {
        try {
            String qualifiedTableName = getQualifiedTableName(schema, tableName);
            String query = String.format("SELECT pg_total_relation_size('%s') AS size", qualifiedTableName.replace("\"", ""));

            try (java.sql.Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    return rs.getLong("size");
                }
            }
        } catch (Exception e) {
            log.debug("Cannot estimate PostgreSQL table size: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<String> getTables(DatasourceConnectionInfo connectionInfo, String schema) throws Exception {
        List<String> tables = new ArrayList<>();

        try (java.sql.Connection conn = getConnection(connectionInfo)) {
            String query;
            if (schema != null && !schema.isEmpty()) {
                query = String.format(
                        "SELECT tablename FROM pg_tables WHERE schemaname = '%s' ORDER BY tablename",
                        schema.toLowerCase()
                );
            } else {
                query = "SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename";
            }

            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    tables.add(rs.getString("tablename"));
                }
            }

            String viewQuery;
            if (schema != null && !schema.isEmpty()) {
                viewQuery = String.format(
                        "SELECT viewname FROM pg_views WHERE schemaname = '%s' ORDER BY viewname",
                        schema.toLowerCase()
                );
            } else {
                viewQuery = "SELECT viewname FROM pg_views WHERE schemaname = 'public' ORDER BY viewname";
            }

            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(viewQuery)) {
                while (rs.next()) {
                    tables.add(rs.getString("viewname"));
                }
            }

            log.info("Found {} tables/views in PostgreSQL datasource {} (schema: {})",
                    tables.size(), connectionInfo.getDatasourceName(), schema);
        }

        return tables;
    }

    @Override
    public List<List<Object>> getSampleData(DatasourceConnectionInfo connectionInfo, String schema,
                                           String tableName, int rowCount) throws Exception {
        List<List<Object>> sampleData = new ArrayList<>();

        try (java.sql.Connection conn = getConnection(connectionInfo)) {
            String qualifiedTableName = getQualifiedTableName(schema, tableName);
            String query = String.format("SELECT * FROM %s LIMIT %d", qualifiedTableName, rowCount);

            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    int columnCount = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        row.add(rs.getObject(i));
                    }
                    sampleData.add(row);
                }
            }

            log.debug("Retrieved {} sample rows from table {}.{}", sampleData.size(), schema, tableName);
        }

        return sampleData;
    }
}
