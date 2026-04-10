package com.enterprise.dataplatform.lineage.service;

import com.enterprise.dataplatform.lineage.domain.entity.LineageHistory;
import com.enterprise.dataplatform.lineage.domain.entity.LineageRelation;
import com.enterprise.dataplatform.lineage.domain.entity.LineageSnapshot;
import com.enterprise.dataplatform.lineage.domain.enums.LineageDirection;
import com.enterprise.dataplatform.lineage.domain.enums.LineageType;
import com.enterprise.dataplatform.lineage.domain.event.GraphNode;
import com.enterprise.dataplatform.lineage.dto.request.CreateLineageRequest;
import com.enterprise.dataplatform.lineage.dto.request.LineageParseRequest;
import com.enterprise.dataplatform.lineage.dto.request.LineageQueryRequest;
import com.enterprise.dataplatform.lineage.dto.response.ImpactAnalysisResponse;
import com.enterprise.dataplatform.lineage.dto.response.LineageGraphResponse;
import com.enterprise.dataplatform.lineage.parser.SqlLineageParser;
import com.enterprise.dataplatform.lineage.repository.LineageGraphRepository;
import com.enterprise.dataplatform.lineage.repository.LineageHistoryRepository;
import com.enterprise.dataplatform.lineage.repository.LineageRelationRepository;
import com.enterprise.dataplatform.lineage.repository.LineageSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lineage management service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineageService {

    private final LineageRelationRepository lineageRelationRepository;
    private final LineageHistoryRepository lineageHistoryRepository;
    private final LineageSnapshotRepository lineageSnapshotRepository;
    private final LineageGraphRepository lineageGraphRepository;
    private final SqlLineageParser sqlLineageParser;

    /**
     * Create lineage relationship
     */
    @Transactional
    public LineageRelation createLineage(CreateLineageRequest request, String userId) {
        // Check if relation already exists
        Optional<LineageRelation> existing = lineageRelationRepository
                .findBySourceAssetIdAndTargetAssetIdAndIsDeletedFalse(
                        request.getSourceAssetId(), request.getTargetAssetId());

        if (existing.isPresent()) {
            throw new RuntimeException("Lineage relation already exists");
        }

        LineageRelation relation = LineageRelation.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .sourceAssetId(request.getSourceAssetId())
                .sourceFieldId(request.getSourceFieldId())
                .targetAssetId(request.getTargetAssetId())
                .targetFieldId(request.getTargetFieldId())
                .lineageType(request.getLineageType() != null ? request.getLineageType() : LineageType.MANUAL)
                .transformDesc(request.getTransformDesc())
                .transformSql(request.getTransformSql())
                .taskName(request.getTaskName())
                .jobId(request.getJobId())
                .confidence(request.getConfidence() != null ? request.getConfidence() : 100.0)
                .createdBy(userId)
                .build();

        relation = lineageRelationRepository.save(relation);

        // Sync to Neo4j
        lineageGraphRepository.createLineageRelation(
                request.getSourceAssetId(),
                request.getTargetAssetId(),
                relation.getLineageType().name(),
                Map.of("transformation", relation.getTransformDesc() != null ? relation.getTransformDesc() : "")
        );

        // Record history
        recordHistory(relation.getId(), "CREATE", null, relationToMap(relation), userId);

        log.info("Created lineage relation: {} -> {}", request.getSourceAssetId(), request.getTargetAssetId());
        return relation;
    }

    /**
     * Delete lineage relationship
     */
    @Transactional
    public void deleteLineage(String lineageId, String userId) {
        LineageRelation relation = lineageRelationRepository.findById(lineageId)
                .orElseThrow(() -> new RuntimeException("Lineage relation not found"));

        if (relation.getIsDeleted()) {
            throw new RuntimeException("Lineage relation already deleted");
        }

        Map<String, Object> oldValue = relationToMap(relation);

        relation.setIsDeleted(true);
        relation.setDeletedBy(userId);
        relation.setDeletedTime(LocalDateTime.now());
        lineageRelationRepository.save(relation);

        // Delete from Neo4j
        lineageGraphRepository.deleteLineageRelation(
                relation.getSourceAssetId(),
                relation.getTargetAssetId()
        );

        // Record history
        recordHistory(lineageId, "DELETE", oldValue, null, userId);

        log.info("Deleted lineage relation: {}", lineageId);
    }

    /**
     * Query lineage graph
     */
    public LineageGraphResponse queryLineageGraph(LineageQueryRequest request) {
        LineageDirection direction = request.getDirection() != null ? request.getDirection() : LineageDirection.BOTH;
        int depth = request.getDepth() != null ? Math.min(request.getDepth(), 10) : 3;

        List<LineageGraphResponse.LineageNode> nodes = new ArrayList<>();
        List<LineageGraphResponse.LineageEdge> edges = new ArrayList<>();

        // Query based on direction
        if (direction == LineageDirection.UPSTREAM || direction == LineageDirection.BOTH) {
            List<GraphNode> upstreamNodes = lineageGraphRepository.getUpstreamLineage(request.getAssetId(), depth);
            upstreamNodes.forEach(node -> {
                nodes.add(LineageGraphResponse.LineageNode.builder()
                        .id(node.getAssetId())
                        .name(node.getName())
                        .type(node.getType())
                        .properties(node.getProperties())
                        .build());
            });
        }

        if (direction == LineageDirection.DOWNSTREAM || direction == LineageDirection.BOTH) {
            List<GraphNode> downstreamNodes = lineageGraphRepository.getDownstreamLineage(request.getAssetId(), depth);
            downstreamNodes.forEach(node -> {
                nodes.add(LineageGraphResponse.LineageNode.builder()
                        .id(node.getAssetId())
                        .name(node.getName())
                        .type(node.getType())
                        .properties(node.getProperties())
                        .build());
            });
        }

        // Add the center node
        nodes.add(LineageGraphResponse.LineageNode.builder()
                .id(request.getAssetId())
                .name("Center Node")
                .type("TABLE")
                .build());

        // Query relations
        List<LineageRelation> relations = lineageRelationRepository.findByAssetId(request.getAssetId());
        relations.forEach(rel -> {
            edges.add(LineageGraphResponse.LineageEdge.builder()
                    .source(rel.getSourceAssetId())
                    .target(rel.getTargetAssetId())
                    .transform(rel.getTransformSql())
                    .taskName(rel.getTaskName())
                    .lineageType(rel.getLineageType().name())
                    .confidence(rel.getConfidence())
                    .build());
        });

        // Statistics
        long upstreamCount = lineageRelationRepository.countByAssetId(request.getAssetId());
        boolean hasCycle = lineageGraphRepository.hasCircularDependency(request.getAssetId());

        LineageGraphResponse.LineageStatistics statistics = LineageGraphResponse.LineageStatistics.builder()
                .upstreamCount((int) upstreamCount)
                .downstreamCount(edges.size())
                .maxDepth(depth)
                .hasCycle(hasCycle)
                .build();

        return LineageGraphResponse.builder()
                .nodes(nodes)
                .edges(edges)
                .statistics(statistics)
                .build();
    }

    /**
     * Impact analysis
     */
    public ImpactAnalysisResponse analyzeImpact(String assetId, Integer depth) {
        int maxDepth = depth != null ? Math.min(depth, 10) : 5;

        // Get all affected downstream assets
        List<GraphNode> affectedNodes = lineageGraphRepository.getDownstreamLineage(assetId, maxDepth);

        List<ImpactAnalysisResponse.AffectedAsset> affectedAssets = affectedNodes.stream()
                .map(node -> ImpactAnalysisResponse.AffectedAsset.builder()
                        .id(node.getAssetId())
                        .name(node.getName())
                        .type(node.getType())
                        .dependencyType("DIRECT")
                        .criticalLevel("MEDIUM")
                        .build())
                .collect(Collectors.toList());

        // Count critical assets (those with high sensitivity level)
        int criticalCount = (int) affectedAssets.stream()
                .filter(a -> "HIGH".equals(a.getCriticalLevel()))
                .count();

        ImpactAnalysisResponse.ImpactAnalysis analysis = ImpactAnalysisResponse.ImpactAnalysis.builder()
                .directDownstreamCount(affectedAssets.size())
                .totalDownstreamCount(affectedNodes.size())
                .criticalAssetsCount(criticalCount)
                .reportsAffectedCount(0) // Would be populated from report service
                .estimatedImpactTime(LocalDateTime.now().plusHours(24).toString())
                .build();

        return ImpactAnalysisResponse.builder()
                .assetId(assetId)
                .impactAnalysis(analysis)
                .affectedAssets(affectedAssets)
                .affectedTasks(Collections.emptyList())
                .mitigationSuggestions(List.of(
                        "Notify downstream task owners",
                        "Pause affected tasks if needed",
                        "Schedule maintenance window"
                ))
                .build();
    }

    /**
     * Traceability analysis
     */
    public LineageGraphResponse traceLineage(String assetId, Integer depth) {
        int maxDepth = depth != null ? Math.min(depth, 10) : 5;
        
        LineageQueryRequest request = LineageQueryRequest.builder()
                .assetId(assetId)
                .direction(LineageDirection.UPSTREAM)
                .depth(maxDepth)
                .build();
        
        return queryLineageGraph(request);
    }

    /**
     * Parse SQL/DDL to extract lineage
     */
    public SqlLineageParser.LineageParseResult parseSql(LineageParseRequest request) {
        if ("DDL".equalsIgnoreCase(request.getParseType())) {
            return sqlLineageParser.parseDDL(request.getSqlContent()).toLineageResult();
        }
        return sqlLineageParser.parse(request.getSqlContent());
    }

    /**
     * Verify lineage relation
     */
    @Transactional
    public void verifyLineage(String lineageId, String userId, String method) {
        LineageRelation relation = lineageRelationRepository.findById(lineageId)
                .orElseThrow(() -> new RuntimeException("Lineage relation not found"));

        relation.setIsVerified(true);
        relation.setVerifiedBy(userId);
        relation.setVerifiedTime(LocalDateTime.now());
        relation.setVerificationMethod(method);
        lineageRelationRepository.save(relation);

        log.info("Verified lineage relation: {}", lineageId);
    }

    /**
     * Get lineage statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalRelations = lineageRelationRepository.count();
        long etlRelations = lineageRelationRepository.findByLineageTypeAndIsDeletedFalse(LineageType.ETL).size();
        long sqlRelations = lineageRelationRepository.findByLineageTypeAndIsDeletedFalse(LineageType.SQL).size();
        long manualRelations = lineageRelationRepository.findByLineageTypeAndIsDeletedFalse(LineageType.MANUAL).size();

        stats.put("totalRelations", totalRelations);
        stats.put("byType", Map.of(
                "ETL", etlRelations,
                "SQL", sqlRelations,
                "MANUAL", manualRelations
        ));

        return stats;
    }

    /**
     * Get lineage change history
     */
    public List<LineageHistory> getHistory(String lineageId) {
        return lineageHistoryRepository.findByLineageIdOrderByCreatedTimeDesc(lineageId);
    }

    /**
     * Create snapshot
     */
    @Transactional
    public LineageSnapshot createSnapshot(String name, String userId) {
        LineageSnapshot snapshot = LineageSnapshot.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .snapshotName(name)
                .snapshotTime(LocalDateTime.now())
                .assetCount(0) // Would be calculated from Neo4j
                .relationCount((int) lineageRelationRepository.count())
                .status("COMPLETED")
                .createdBy(userId)
                .build();

        return lineageSnapshotRepository.save(snapshot);
    }

    private void recordHistory(String lineageId, String changeType,
                                Map<String, Object> oldValue, Map<String, Object> newValue, String userId) {
        LineageHistory history = LineageHistory.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .lineageId(lineageId)
                .changeType(changeType)
                .oldValue(oldValue)
                .newValue(newValue)
                .createdBy(userId)
                .build();

        lineageHistoryRepository.save(history);
    }

    private Map<String, Object> relationToMap(LineageRelation relation) {
        Map<String, Object> map = new HashMap<>();
        map.put("sourceAssetId", relation.getSourceAssetId());
        map.put("targetAssetId", relation.getTargetAssetId());
        map.put("lineageType", relation.getLineageType().name());
        map.put("transformDesc", relation.getTransformDesc());
        return map;
    }
}
