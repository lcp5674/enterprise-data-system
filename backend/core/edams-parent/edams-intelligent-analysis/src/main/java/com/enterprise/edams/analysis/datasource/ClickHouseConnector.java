package com.enterprise.edams.analysis.datasource;

import com.enterprise.edams.analysis.metadata.ColumnMetadata;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class ClickHouseConnector extends AbstractDatasourceConnector {

    @Override
    protected String buildJdbcUrl(DatasourceConnectionInfo connectionInfo) {
        StringBuilder url = new StringBuilder("jdbc:clickhouse://");
        url.append(connectionInfo.getHost()).append(":");
        url.append(connectionInfo.getPort()).append("/");
        url.append(connectionInfo.getDatabaseName());
        return url.toString();
    }

    @Override
    public String getConnectorType() {
        return "CLICKHOUSE";
    }

    @Override
    protected ColumnMetadata buildColumnMetadata(ResultSet rs) throws SQLException {
        return ColumnMetadata.builder()
                .columnName(rs.getString("COLUMN_NAME"))
                .dataType(rs.getString("TYPE_NAME"))
                .columnType(rs.getString("TYPE_NAME"))
                .columnComment(rs.getString("REMARKS"))
                .nullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable)
                .ordinalPosition(rs.getInt("ORDINAL_POSITION"))
                .defaultValue(rs.getString("COLUMN_DEF"))
                .build();
    }

    @Override
    protected String getQualifiedTableName(String schema, String tableName) {
        return "`" + tableName + "`";
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

            log.debug("Retrieved {} sample rows from ClickHouse table {}.{}", sampleData.size(), schema, tableName);
        }

        return sampleData;
    }

    @Override
    public Long getRowCount(DatasourceConnectionInfo connectionInfo, String schema, String tableName) throws Exception {
        try (java.sql.Connection conn = getConnection(connectionInfo)) {
            String qualifiedTableName = getQualifiedTableName(schema, tableName);
            String query = String.format("SELECT count() FROM %s", qualifiedTableName);

            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0L;
    }
}
