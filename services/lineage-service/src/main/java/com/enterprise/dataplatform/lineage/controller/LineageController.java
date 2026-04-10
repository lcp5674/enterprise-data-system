package com.enterprise.dataplatform.lineage.controller;

import com.enterprise.dataplatform.lineage.domain.entity.LineageHistory;
import com.enterprise.dataplatform.lineage.domain.entity.LineageRelation;
import com.enterprise.dataplatform.lineage.domain.entity.LineageSnapshot;
import com.enterprise.dataplatform.lineage.dto.request.CreateLineageRequest;
import com.enterprise.dataplatform.lineage.dto.request.LineageParseRequest;
import com.enterprise.dataplatform.lineage.dto.request.LineageQueryRequest;
import com.enterprise.dataplatform.lineage.dto.response.ImpactAnalysisResponse;
import com.enterprise.dataplatform.lineage.dto.response.LineageGraphResponse;
import com.enterprise.dataplatform.lineage.parser.SqlLineageParser;
import com.enterprise.dataplatform.lineage.service.LineageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Lineage API Controller
 */
@RestController
@RequestMapping("/api/v1/lineage")
@RequiredArgsConstructor
@Tag(name = "Lineage Management", description = "Data lineage management APIs")
public class LineageController {

    private final LineageService lineageService;

    @PostMapping
    @Operation(summary = "Create lineage relationship", description = "Create a new lineage relationship")
    public ResponseEntity<LineageRelation> createLineage(
            @Valid @RequestBody CreateLineageRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.ok(lineageService.createLineage(request, userId));
    }

    @DeleteMapping("/{lineageId}")
    @Operation(summary = "Delete lineage relationship", description = "Delete an existing lineage relationship")
    public ResponseEntity<Void> deleteLineage(
            @PathVariable String lineageId,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        lineageService.deleteLineage(lineageId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/graph")
    @Operation(summary = "Query lineage graph", description = "Get lineage graph for an asset")
    public ResponseEntity<LineageGraphResponse> getLineageGraph(
            @RequestParam String assetId,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false, defaultValue = "3") Integer depth,
            @RequestParam(required = false) Boolean includeTasks,
            @RequestParam(required = false) String layout) {
        
        LineageQueryRequest request = LineageQueryRequest.builder()
                .assetId(assetId)
                .direction(direction != null ? 
                        com.enterprise.dataplatform.lineage.domain.enums.LineageDirection.valueOf(direction) : null)
                .depth(depth)
                .includeTasks(includeTasks)
                .layout(layout)
                .build();
        
        return ResponseEntity.ok(lineageService.queryLineageGraph(request));
    }

    @GetMapping("/impact/{assetId}")
    @Operation(summary = "Impact analysis", description = "Analyze the impact of changes to an asset")
    public ResponseEntity<ImpactAnalysisResponse> analyzeImpact(
            @PathVariable String assetId,
            @RequestParam(required = false, defaultValue = "5") Integer depth) {
        return ResponseEntity.ok(lineageService.analyzeImpact(assetId, depth));
    }

    @GetMapping("/trace/{assetId}")
    @Operation(summary = "Traceability analysis", description = "Trace data sources for an asset")
    public ResponseEntity<LineageGraphResponse> traceLineage(
            @PathVariable String assetId,
            @RequestParam(required = false, defaultValue = "5") Integer depth) {
        return ResponseEntity.ok(lineageService.traceLineage(assetId, depth));
    }

    @PostMapping("/parse")
    @Operation(summary = "Parse SQL/DDL", description = "Parse SQL or DDL to extract lineage")
    public ResponseEntity<SqlLineageParser.LineageParseResult> parseLineage(
            @Valid @RequestBody LineageParseRequest request) {
        return ResponseEntity.ok(lineageService.parseSql(request));
    }

    @PostMapping("/{lineageId}/verify")
    @Operation(summary = "Verify lineage", description = "Mark a lineage relation as verified")
    public ResponseEntity<Void> verifyLineage(
            @PathVariable String lineageId,
            @RequestParam String method,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        lineageService.verifyLineage(lineageId, userId, method);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get lineage statistics", description = "Get lineage coverage statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(lineageService.getStatistics());
    }

    @GetMapping("/{lineageId}/history")
    @Operation(summary = "Get lineage history", description = "Get change history for a lineage relation")
    public ResponseEntity<List<LineageHistory>> getHistory(@PathVariable String lineageId) {
        return ResponseEntity.ok(lineageService.getHistory(lineageId));
    }

    @PostMapping("/snapshot")
    @Operation(summary = "Create snapshot", description = "Create a point-in-time snapshot of lineage")
    public ResponseEntity<LineageSnapshot> createSnapshot(
            @RequestParam String name,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.ok(lineageService.createSnapshot(name, userId));
    }

    @GetMapping("/table/{assetId}")
    @Operation(summary = "Get table lineage", description = "Get table-level lineage for an asset")
    public ResponseEntity<LineageGraphResponse> getTableLineage(
            @PathVariable String assetId,
            @RequestParam(required = false, defaultValue = "UPSTREAM") String direction,
            @RequestParam(required = false, defaultValue = "3") Integer depth) {
        
        LineageQueryRequest request = LineageQueryRequest.builder()
                .assetId(assetId)
                .direction(com.enterprise.dataplatform.lineage.domain.enums.LineageDirection.valueOf(direction))
                .depth(depth)
                .includeTasks(true)
                .build();
        
        return ResponseEntity.ok(lineageService.queryLineageGraph(request));
    }

    @GetMapping("/field/{assetId}")
    @Operation(summary = "Get field lineage", description = "Get field-level lineage for an asset")
    public ResponseEntity<LineageGraphResponse> getFieldLineage(
            @PathVariable String assetId,
            @RequestParam(required = false, defaultValue = "UPSTREAM") String direction,
            @RequestParam(required = false, defaultValue = "2") Integer depth) {
        
        LineageQueryRequest request = LineageQueryRequest.builder()
                .assetId(assetId)
                .direction(com.enterprise.dataplatform.lineage.domain.enums.LineageDirection.valueOf(direction))
                .depth(depth)
                .build();
        
        return ResponseEntity.ok(lineageService.queryLineageGraph(request));
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare lineage versions", description = "Compare lineage at different time points")
    public ResponseEntity<Map<String, Object>> compareLineage(
            @RequestParam String assetId,
            @RequestParam String version1,
            @RequestParam String version2) {
        // Implementation for version comparison
        return ResponseEntity.ok(Map.of(
                "assetId", assetId,
                "version1", version1,
                "version2", version2,
                "addedRelations", List.of(),
                "removedRelations", List.of()
        ));
    }
}
