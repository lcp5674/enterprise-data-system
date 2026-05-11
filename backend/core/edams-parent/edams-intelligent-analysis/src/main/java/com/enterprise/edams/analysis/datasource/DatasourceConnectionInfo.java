package com.enterprise.edams.analysis.datasource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasourceConnectionInfo {

    private Long datasourceId;
    private String datasourceCode;
    private String datasourceName;
    private String datasourceType;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    private String password;
    private String jdbcUrl;
    private String schema;
    private String additionalParams;

    public String getJdbcUrlTemplate() {
        return switch (datasourceType.toUpperCase()) {
            case "MYSQL" -> String.format("jdbc:mysql://%s:%d/%s%s",
                    host, port, databaseName,
                    additionalParams != null ? "?" + additionalParams : "");
            case "POSTGRESQL", "POSTGRES" -> String.format("jdbc:postgresql://%s:%d/%s",
                    host, port, databaseName);
            case "ORACLE" -> String.format("jdbc:oracle:thin:@%s:%d:%s",
                    host, port, databaseName);
            case "SQLSERVER" -> String.format("jdbc:sqlserver://%s:%d;databaseName=%s",
                    host, port, databaseName);
            case "CLICKHOUSE" -> String.format("jdbc:clickhouse://%s:%d/%s",
                    host, port, databaseName);
            case "DORIS" -> String.format("jdbc:mysql://%s:%d/%s",
                    host, port, databaseName);
            case "STARROCKS" -> String.format("jdbc:mysql://%s:%d/%s",
                    host, port, databaseName);
            case "PRESTO" -> String.format("jdbc:presto://%s:%d/%s",
                    host, port, databaseName);
            default -> jdbcUrl;
        };
    }

    public String getDriverClassName() {
        return switch (datasourceType.toUpperCase()) {
            case "MYSQL", "DORIS", "STARROCKS" -> "com.mysql.cj.jdbc.Driver";
            case "POSTGRESQL", "POSTGRES" -> "org.postgresql.Driver";
            case "ORACLE" -> "oracle.jdbc.OracleDriver";
            case "SQLSERVER" -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case "CLICKHOUSE" -> "com.clickhouse.jdbc.ClickHouseDriver";
            case "PRESTO" -> "com.facebook.presto.jdbc.PrestoDriver";
            default -> "com.mysql.cj.jdbc.Driver";
        };
    }

    public boolean supportsSchema() {
        return switch (datasourceType.toUpperCase()) {
            case "POSTGRESQL", "POSTGRES", "ORACLE", "PRESTO" -> true;
            default -> false;
        };
    }
}
