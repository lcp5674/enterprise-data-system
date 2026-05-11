package com.enterprise.edams.analysis.datasource;

import com.enterprise.edams.analysis.metadata.ColumnMetadata;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class MySQLConnector extends AbstractDatasourceConnector {

    @Override
    protected String buildJdbcUrl(DatasourceConnectionInfo connectionInfo) {
        StringBuilder url = new StringBuilder("jdbc:mysql://");
        url.append(connectionInfo.getHost()).append(":");
        url.append(connectionInfo.getPort()).append("/");
        url.append(connectionInfo.getDatabaseName());

        if (connectionInfo.getAdditionalParams() != null && !connectionInfo.getAdditionalParams().isEmpty()) {
            url.append("?").append(connectionInfo.getAdditionalParams());
        } else {
            url.append("?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true");
        }

        return url.toString();
    }

    @Override
    public String getConnectorType() {
        return "MYSQL";
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
        return "`" + tableName + "`";
    }

    @Override
    protected Long estimateTableSize(java.sql.Connection conn, String schema, String tableName, Long rowCount) {
        try {
            String qualifiedTableName = getQualifiedTableName(schema, tableName);
            String query = String.format("SELECT data_length + index_length AS size FROM information_schema.TABLES WHERE table_name = '%s'", tableName);

            try (java.sql.Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    return rs.getLong("size");
                }
            }
        } catch (Exception e) {
            log.debug("Cannot estimate MySQL table size: {}", e.getMessage());
        }
        return null;
    }
}
