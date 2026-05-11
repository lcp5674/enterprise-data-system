package com.enterprise.edams.analysis.controller;

import com.enterprise.edams.analysis.metadata.ColumnMetadata;
import com.enterprise.edams.analysis.metadata.TableMetadata;
import com.enterprise.edams.analysis.service.DatasourceScannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/analysis/datasources")
@RequiredArgsConstructor
public class AnalysisPreviewController {

    private final DatasourceScannerService scannerService;

    @GetMapping("/{datasourceId}/tables")
    public ResponseEntity<List<String>> getTables(
            @PathVariable Long datasourceId,
            @RequestParam(required = false) String schema) {
        
        log.debug("Getting tables for datasource: {}, schema: {}", datasourceId, schema);
        
        List<String> tables = scannerService.scanTables(
                datasourceId,
                schema,
                null,
                null
        );
        
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/{datasourceId}/tables/{table}/columns")
    public ResponseEntity<TableMetadata> getColumns(
            @PathVariable Long datasourceId,
            @PathVariable String table,
            @RequestParam(required = false) String schema) {
        
        log.debug("Getting columns for table: {}.{}", schema, table);
        
        TableMetadata metadata = scannerService.getTableMetadata(
                datasourceId,
                schema,
                table,
                0
        );
        
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/{datasourceId}/tables/{table}/sample")
    public ResponseEntity<Map<String, Object>> getSampleData(
            @PathVariable Long datasourceId,
            @PathVariable String table,
            @RequestParam(required = false) String schema,
            @RequestParam(defaultValue = "100") int rowCount) {
        
        log.debug("Getting sample data for table: {}.{}, rows: {}", schema, table, rowCount);
        
        TableMetadata metadata = scannerService.getTableMetadata(
                datasourceId,
                schema,
                table,
                rowCount
        );
        
        List<List<Object>> sampleData = scannerService.getSampleData(
                datasourceId,
                schema,
                table,
                rowCount
        );
        
        return ResponseEntity.ok(Map.of(
                "metadata", metadata,
                "sampleData", sampleData
        ));
    }
}
