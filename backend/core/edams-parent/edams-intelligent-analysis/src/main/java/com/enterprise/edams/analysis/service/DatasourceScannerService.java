package com.enterprise.edams.analysis.service;

import com.enterprise.edams.analysis.exception.AnalysisException;
import com.enterprise.edams.analysis.metadata.ColumnMetadata;
import com.enterprise.edams.analysis.metadata.TableMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DatasourceScannerService {

    public List<String> scanTables(Long datasourceId, String schema, List<String> targetTables, List<String> excludedTables) {
        log.info("Scanning tables for datasource: {}, schema: {}", datasourceId, schema);

        List<String> allTables = new ArrayList<>();

        try (Connection conn = getConnection(datasourceId)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            
            try (ResultSet rs = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    allTables.add(tableName);
                }
            }

            log.info("Found {} tables in datasource {}", allTables.size(), datasourceId);

        } catch (Exception e) {
            log.error("Failed to scan tables: {}", e.getMessage(), e);
            throw new AnalysisException("SCAN_FAILED", "扫描表失败: " + e.getMessage());
        }

        if (targetTables != null && !targetTables.isEmpty()) {
            allTables.retainAll(targetTables);
            log.info("Filtered to {} target tables", allTables.size());
        }

        if (excludedTables != null && !excludedTables.isEmpty()) {
            allTables.removeAll(excludedTables);
            log.info("Excluded {} tables, remaining {}", excludedTables.size(), allTables.size());
        }

        return allTables;
    }

    public TableMetadata getTableMetadata(Long datasourceId, String schema, String tableName, int sampleRowCount) {
        log.debug("Getting metadata for table: {}.{}", schema, tableName);

        TableMetadata.TableMetadataBuilder builder = TableMetadata.builder()
                .schemaName(schema)
                .tableName(tableName);

        try (Connection conn = getConnection(datasourceId)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();

            try (ResultSet rs = metaData.getTables(catalog, schema, tableName, new String[]{"TABLE"})) {
                if (rs.next()) {
                    builder.tableComment(rs.getString("REMARKS"));
                }
            }

            List<ColumnMetadata> columns = new ArrayList<>();
            try (ResultSet rs = metaData.getColumns(catalog, schema, tableName, "%")) {
                while (rs.next()) {
                    ColumnMetadata column = ColumnMetadata.builder()
                            .columnName(rs.getString("COLUMN_NAME"))
                            .dataType(rs.getString("TYPE_NAME"))
                            .columnType(rs.getString("TYPE_NAME") + "(" + rs.getInt("COLUMN_SIZE") + ")"))
                            .columnComment(rs.getString("REMARKS"))
                            .nullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable)
                            .ordinalPosition(rs.getInt("ORDINAL_POSITION"))
                            .defaultValue(rs.getString("COLUMN_DEF"))
                            .characterMaximumLength(rs.getInt("CHAR_OCTET_LENGTH") > 0 ? rs.getInt("CHAR_OCTET_LENGTH") : null)
                            .numericPrecision(rs.getInt("NUMERIC_PRECISION") > 0 ? rs.getInt("NUMERIC_PRECISION") : null)
                            .numericScale(rs.getInt("NUM_SCALE") > 0 ? rs.getInt("NUM_SCALE") : null)
                            .build();
                    columns.add(column);
                }
            }

            List<String> primaryKeys = new ArrayList<>();
            try (ResultSet rs = metaData.getPrimaryKeys(catalog, schema, tableName)) {
                while (rs.next()) {
                    primaryKeys.add(rs.getString("COLUMN_NAME"));
                }
            }
            for (ColumnMetadata column : columns) {
                if (primaryKeys.contains(column.getColumnName())) {
                    column.setPrimaryKey(true);
                }
            }

            builder.columns(columns);

            builder.estimatedRowCount(getEstimatedRowCount(conn, schema, tableName));

        } catch (Exception e) {
            log.error("Failed to get table metadata: {}", e.getMessage(), e);
            throw new AnalysisException("METADATA_FAILED", "获取表元数据失败: " + e.getMessage());
        }

        return builder.build();
    }

    public List<List<Object>> getSampleData(Long datasourceId, String schema, String tableName, int rowCount) {
        log.debug("Getting sample data for table: {}.{}, rows: {}", schema, tableName, rowCount);

        List<List<Object>> sampleData = new ArrayList<>();

        try (Connection conn = getConnection(datasourceId)) {
            String query = String.format("SELECT * FROM %s.%s LIMIT %d",
                    schema != null ? schema : "",
                    tableName,
                    rowCount);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                ResultSetMetaData rsMetaData = rs.getMetaData();
                int columnCount = rsMetaData.getColumnCount();

                while (rs.next()) {
                    List<Object> row = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.add(rs.getObject(i));
                    }
                    sampleData.add(row);
                }
            }

        } catch (Exception e) {
            log.error("Failed to get sample data: {}", e.getMessage(), e);
        }

        return sampleData;
    }

    private Connection getConnection(Long datasourceId) throws SQLException {
        throw new AnalysisException("NOT_IMPLEMENTED", "需要实现数据源连接获取逻辑，连接到实际的数据源");
    }

    private Long getEstimatedRowCount(Connection conn, String schema, String tableName) {
        String query = String.format("SELECT COUNT(*) FROM %s.%s",
                schema != null ? schema : "",
                tableName);

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            log.warn("Failed to get row count: {}", e.getMessage());
        }

        return null;
    }
}
