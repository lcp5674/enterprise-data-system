package com.enterprise.edams.analysis.service;

import com.enterprise.edams.analysis.datasource.DatasourceConnectionInfo;
import com.enterprise.edams.analysis.datasource.DatasourceConnector;
import com.enterprise.edams.analysis.datasource.DatasourceConnectorFactory;
import com.enterprise.edams.analysis.exception.AnalysisException;
import com.enterprise.edams.analysis.metadata.ColumnMetadata;
import com.enterprise.edams.analysis.metadata.TableMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatasourceScannerService {

    private final DatasourceConnectionService datasourceConnectionService;
    private final DatasourceConnectorFactory connectorFactory;

    public List<String> scanTables(Long datasourceId, String schema, List<String> targetTables, List<String> excludedTables) {
        log.info("Scanning tables for datasource: {}, schema: {}", datasourceId, schema);

        DatasourceConnectionInfo connectionInfo = datasourceConnectionService.getDatasourceConnectionInfo(datasourceId);
        DatasourceConnector connector = connectorFactory.getConnector(connectionInfo);

        try {
            List<String> allTables = connector.getTables(connectionInfo, schema);

            if (targetTables != null && !targetTables.isEmpty()) {
                List<String> filtered = new ArrayList<>();
                for (String target : targetTables) {
                    if (allTables.contains(target)) {
                        filtered.add(target);
                    } else {
                        log.warn("Target table not found in datasource: {}", target);
                    }
                }
                allTables = filtered;
                log.info("Filtered to {} target tables", allTables.size());
            }

            if (excludedTables != null && !excludedTables.isEmpty()) {
                allTables.removeAll(excludedTables);
                log.info("Excluded {} tables, remaining {}", excludedTables.size(), allTables.size());
            }

            return allTables;

        } catch (Exception e) {
            log.error("Failed to scan tables: {}", e.getMessage(), e);
            throw new AnalysisException("SCAN_FAILED", "扫描表失败: " + e.getMessage());
        }
    }

    public TableMetadata getTableMetadata(Long datasourceId, String schema, String tableName, int sampleRowCount) {
        log.debug("Getting metadata for table: {}.{}", schema, tableName);

        DatasourceConnectionInfo connectionInfo = datasourceConnectionService.getDatasourceConnectionInfo(datasourceId);
        DatasourceConnector connector = connectorFactory.getConnector(connectionInfo);

        try {
            TableMetadata metadata = connector.getTableMetadata(connectionInfo, schema, tableName);

            if (sampleRowCount > 0) {
                List<List<Object>> sampleData = connector.getSampleData(connectionInfo, schema, tableName, sampleRowCount);
                metadata.setSampleData(sampleData);
            }

            log.debug("Successfully retrieved metadata for table: {}.{}", schema, tableName);
            return metadata;

        } catch (Exception e) {
            log.error("Failed to get table metadata: {}", e.getMessage(), e);
            throw new AnalysisException("METADATA_FAILED", "获取表元数据失败: " + e.getMessage());
        }
    }

    public List<List<Object>> getSampleData(Long datasourceId, String schema, String tableName, int rowCount) {
        log.debug("Getting sample data for table: {}.{}, rows: {}", schema, tableName, rowCount);

        DatasourceConnectionInfo connectionInfo = datasourceConnectionService.getDatasourceConnectionInfo(datasourceId);
        DatasourceConnector connector = connectorFactory.getConnector(connectionInfo);

        try {
            List<List<Object>> sampleData = connector.getSampleData(connectionInfo, schema, tableName, rowCount);
            log.debug("Retrieved {} sample rows from {}.{}", sampleData.size(), schema, tableName);
            return sampleData;

        } catch (Exception e) {
            log.error("Failed to get sample data: {}", e.getMessage(), e);
            throw new AnalysisException("SAMPLE_DATA_FAILED", "获取样本数据失败: " + e.getMessage());
        }
    }

    public Long getRowCount(Long datasourceId, String schema, String tableName) {
        DatasourceConnectionInfo connectionInfo = datasourceConnectionService.getDatasourceConnectionInfo(datasourceId);
        DatasourceConnector connector = connectorFactory.getConnector(connectionInfo);

        try {
            return connector.getRowCount(connectionInfo, schema, tableName);
        } catch (Exception e) {
            log.error("Failed to get row count: {}", e.getMessage(), e);
            return 0L;
        }
    }

    public List<ColumnMetadata> getColumns(Long datasourceId, String schema, String tableName) {
        TableMetadata metadata = getTableMetadata(datasourceId, schema, tableName, 0);
        return metadata.getColumns();
    }

    public List<String> getSchemas(Long datasourceId) {
        log.debug("Getting schemas for datasource: {}", datasourceId);

        DatasourceConnectionInfo connectionInfo = datasourceConnectionService.getDatasourceConnectionInfo(datasourceId);

        if (!connectionInfo.supportsSchema()) {
            log.debug("Datasource {} does not support schemas", connectionInfo.getDatasourceType());
            return List.of();
        }

        try (Connection conn = datasourceConnectionService.getConnection(datasourceId)) {
            List<String> schemas = new ArrayList<>();
            java.sql.DatabaseMetaData metaData = conn.getMetaData();

            try (java.sql.ResultSet rs = metaData.getSchemas()) {
                while (rs.next()) {
                    schemas.add(rs.getString(1));
                }
            }

            log.info("Found {} schemas in datasource {}", schemas.size(), datasourceId);
            return schemas;

        } catch (Exception e) {
            log.error("Failed to get schemas: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
