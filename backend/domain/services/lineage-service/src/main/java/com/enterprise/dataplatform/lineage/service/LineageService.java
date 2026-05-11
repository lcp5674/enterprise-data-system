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
    private final ImpactAnalysisService impactAnalysisService;

    private static final int MAX_ASSET_ID_LENGTH = 128;

    private void validateLineageRelation(LineageRelation relation) {
        if (relation == null) {
            throw new IllegalArgumentException("血缘关系不能为空");
        }
        
        if (StringUtils.isBlank(relation.getSourceAssetId())) {
            throw new IllegalArgumentException("源资产ID不能为空");
        }
        if (relation.getSourceAssetId().length() > MAX_ASSET_ID_LENGTH) {
            throw new IllegalArgumentException("源资产ID长度不能超过" + MAX_ASSET_ID_LENGTH + "个字符");
        }
        
        if (StringUtils.isBlank(relation.getTargetAssetId())) {
            throw new IllegalArgumentException("目标资产ID不能为空");
        }
        if (relation.getTargetAssetId().length() > MAX_ASSET_ID_LENGTH) {
            throw new IllegalArgumentException("目标资产ID长度不能超过" + MAX_ASSET_ID_LENGTH + "个字符");
        }
        
        if (relation.getSourceAssetId().equals(relation.getTargetAssetId())) {
            throw new IllegalArgumentException("源资产ID和目标资产ID不能相同，禁止自循环血缘关系");
        }
        
        if (relation.getLineageType() == null) {
            throw new IllegalArgumentException("血缘类型不能为空");
        }
        
        checkDuplicateLineage(relation);
        checkCircularDependency(relation);
    }

    private void checkDuplicateLineage(LineageRelation relation) {
        Optional<LineageRelation> existing = lineageRelationRepository
                .findBySourceAssetIdAndTargetAssetIdAndIsDeletedFalse(
                        relation.getSourceAssetId(), 
                        relation.getTargetAssetId());
        
        if (existing.isPresent()) {
            LineageRelation existingRelation = existing.get();
            throw new IllegalStateException(String.format(
                    "血缘关系已存在: source=%s, target=%s, lineageType=%s",
                    relation.getSourceAssetId(),
                    relation.getTargetAssetId(),
                    existingRelation.getLineageType()));
        }
    }

    private void checkCircularDependency(LineageRelation relation) {
        List<GraphNode> upstreamPath = lineageGraphRepository.getUpstreamLineage(
                relation.getSourceAssetId(), 100);
        
        boolean createsCycle = upstreamPath.stream()
                .anyMatch(node -> relation.getTargetAssetId().equals(node.getAssetId()));
        
        if (createsCycle) {
            throw new IllegalStateException(String.format(
                    "创建此血缘关系会导致循环依赖: source=%s -> target=%s 会与现有路径形成闭环",
                    relation.getSourceAssetId(),
                    relation.getTargetAssetId()));
        }
    }

    /**
     * Create lineage relationship
     */
    @Transactional
    public LineageRelation createLineage(CreateLineageRequest request, String userId) {
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

        validateLineageRelation(relation);

        relation = lineageRelationRepository.save(relation);

        lineageGraphRepository.createLineageRelation(
                request.getSourceAssetId(),
                request.getTargetAssetId(),
                relation.getLineageType().name(),
                Map.of("transformation", relation.getTransformDesc() != null ? relation.getTransformDesc() : "")
        );

        recordHistory(relation.getId(), "CREATE", null, relationToMap(relation), userId);

        log.info("Created lineage relation with validation: {} -> {}", request.getSourceAssetId(), request.getTargetAssetId());
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

        List<GraphNode> affectedNodes = lineageGraphRepository.getDownstreamLineage(assetId, maxDepth);

        List<ImpactAnalysisResponse.AffectedAsset> affectedAssets = affectedNodes.stream()
                .map(node -> ImpactAnalysisResponse.AffectedAsset.builder()
                        .id(node.getAssetId())
                        .name(node.getName())
                        .type(node.getType())
                        .dependencyType("DIRECT")
                        .criticalLevel(determineCriticalLevel(node))
                        .owner(buildOwnerInfo(node))
                        .build())
                .collect(Collectors.toList());

        int criticalCount = (int) affectedAssets.stream()
                .filter(a -> "HIGH".equals(a.getCriticalLevel()))
                .count();

        int reportsCount = impactAnalysisService.countAffectedReports(assetId);
        int dashboardsCount = impactAnalysisService.countAffectedDashboards(assetId);
        int pipelinesCount = impactAnalysisService.countAffectedPipelines(assetId);
        LocalDateTime estimatedImpactTime = impactAnalysisService.calculateEstimatedImpactTime(affectedNodes);
        List<String> affectedProcesses = impactAnalysisService.getAffectedBusinessProcesses(assetId);

        ImpactAnalysisResponse.ImpactAnalysis analysis = ImpactAnalysisResponse.ImpactAnalysis.builder()
                .directDownstreamCount((int) affectedNodes.stream().filter(n -> n.getDepth() != null && n.getDepth() == 1).count())
                .totalDownstreamCount(affectedNodes.size())
                .criticalAssetsCount(criticalCount)
                .reportsAffectedCount(reportsCount)
                .dashboardsAffectedCount(dashboardsCount)
                .pipelinesAffectedCount(pipelinesCount)
                .estimatedImpactTime(estimatedImpactTime.toString())
                .affectedBusinessProcesses(affectedProcesses)
                .build();

        List<ImpactAnalysisResponse.AffectedTask> affectedTasks = buildAffectedTasks(affectedNodes);

        return ImpactAnalysisResponse.builder()
                .assetId(assetId)
                .impactAnalysis(analysis)
                .affectedAssets(affectedAssets)
                .affectedTasks(affectedTasks)
                .mitigationSuggestions(generateMitigationSuggestions(affectedNodes.size(), criticalCount))
                .build();
    }

    private String determineCriticalLevel(GraphNode node) {
        Map<String, Object> props = node.getProperties();
        if (props == null) {
            return "MEDIUM";
        }
        Object level = props.get("sensitivityLevel");
        if (level != null) {
            return level.toString().toUpperCase();
        }
        Object priority = props.get("priority");
        if (priority != null) {
            String p = priority.toString().toUpperCase();
            if (p.contains("HIGH") || p.contains("CRITICAL") || p.contains("P0") || p.contains("P1")) {
                return "HIGH";
            }
        }
        return "MEDIUM";
    }

    private ImpactAnalysisResponse.OwnerInfo buildOwnerInfo(GraphNode node) {
        Map<String, Object> props = node.getProperties();
        if (props == null) {
            return null;
        }
        String ownerId = String.valueOf(props.getOrDefault("ownerId", ""));
        String ownerName = String.valueOf(props.getOrDefault("ownerName", ""));
        String ownerEmail = String.valueOf(props.getOrDefault("ownerEmail", ""));
        
        if (ownerId.isEmpty() && ownerName.isEmpty()) {
            return null;
        }
        
        return ImpactAnalysisResponse.OwnerInfo.builder()
                .id(ownerId)
                .name(ownerName)
                .email(ownerEmail)
                .build();
    }

    private List<ImpactAnalysisResponse.AffectedTask> buildAffectedTasks(List<GraphNode> affectedNodes) {
        return affectedNodes.stream()
                .filter(node -> "TASK".equals(node.getType()) || "JOB".equals(node.getType()))
                .map(node -> ImpactAnalysisResponse.AffectedTask.builder()
                        .id(node.getAssetId())
                        .name(node.getName())
                        .type(node.getType())
                        .status("PENDING")
                        .owner(buildOwnerInfo(node))
                        .build())
                .collect(Collectors.toList());
    }

    private List<String> generateMitigationSuggestions(int affectedCount, int criticalCount) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("Notify downstream task owners about the potential impact");
        
        if (criticalCount > 0) {
            suggestions.add("URGENT: Identify and protect critical assets that may be affected");
            suggestions.add("Consider scheduling a maintenance window for critical changes");
        }
        
        if (affectedCount > 10) {
            suggestions.add("Review all affected pipelines and data assets before making changes");
            suggestions.add("Consider implementing a phased rollout approach");
        }
        
        suggestions.add("Run data quality checks after implementing changes");
        suggestions.add("Monitor downstream systems for any anomalies");
        
        return suggestions;
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

    @lombok.Data
    public static class BatchLineageResult {
        private int totalCount;
        private int successCount;
        private int failedCount;
        private double successRate;
        private List<LineageRelation> successRelations;
        private List<FailedLineageRelation> failedRelations;
        private LocalDateTime completedAt;
    }

    @lombok.Data
    public static class FailedLineageRelation {
        private int index;
        private Long id;
        private String sourceAssetId;
        private String targetAssetId;
        private String lineageType;
        private String errorMessage;
        private String errorType;
    }

    @lombok.Data
    public static class BatchCreateLineageRequest {
        private List<LineageRelation> relations;
    }

    @Transactional
    public BatchLineageResult createLineageBatch(BatchCreateLineageRequest request, String createdBy) {
        List<LineageRelation> relations = request.getRelations();
        if (relations == null || relations.isEmpty()) {
            throw new IllegalArgumentException("血缘关系列表不能为空");
        }

        log.info("开始批量创建血缘关系: total={}, createdBy={}", relations.size(), createdBy);

        BatchLineageResult result = new BatchLineageResult();
        result.setTotalCount(relations.size());

        List<LineageRelation> successList = new ArrayList<>();
        List<FailedLineageRelation> failedList = new ArrayList<>();

        for (int i = 0; i < relations.size(); i++) {
            LineageRelation relation = relations.get(i);
            int index = i + 1;

            try {
                relation.setCreatedBy(createdBy);
                relation.setCreatedTime(LocalDateTime.now());
                relation.setIsDeleted(false);
                if (relation.getConfidence() == null) {
                    relation.setConfidence(100.0);
                }
                if (relation.getId() == null) {
                    relation.setId(UUID.randomUUID().toString().replace("-", ""));
                }

                validateLineageRelation(relation);
                LineageRelation savedRelation = lineageRelationRepository.save(relation);

                lineageGraphRepository.createLineageRelation(
                        savedRelation.getSourceAssetId(),
                        savedRelation.getTargetAssetId(),
                        savedRelation.getLineageType().name(),
                        Map.of("transformation", savedRelation.getTransformDesc() != null ? savedRelation.getTransformDesc() : "")
                );

                recordHistory(savedRelation.getId(), "CREATE", null, relationToMap(savedRelation), createdBy);

                successList.add(savedRelation);
                log.debug("批量血缘创建-成功: {}/{}, source={}, target={}", 
                        index, relations.size(), savedRelation.getSourceAssetId(), savedRelation.getTargetAssetId());

            } catch (Exception e) {
                log.error("批量血缘创建-失败: {}/{}, source={}, target={}, error={}",
                        index, relations.size(), relation.getSourceAssetId(), relation.getTargetAssetId(), e.getMessage());

                failedList.add(new FailedLineageRelation());
                FailedLineageRelation failed = failedList.get(failedList.size() - 1);
                failed.setIndex(index);
                failed.setSourceAssetId(relation.getSourceAssetId());
                failed.setTargetAssetId(relation.getTargetAssetId());
                failed.setLineageType(relation.getLineageType() != null ? relation.getLineageType().name() : null);
                failed.setErrorMessage(e.getMessage());
                failed.setErrorType(e.getClass().getSimpleName());
            }
        }

        result.setSuccessCount(successList.size());
        result.setFailedCount(failedList.size());
        result.setSuccessRate(relations.size() > 0 ? (double) successList.size() / relations.size() * 100 : 0.0);
        result.setSuccessRelations(successList);
        result.setFailedRelations(failedList);
        result.setCompletedAt(LocalDateTime.now());

        log.info("批量创建血缘关系完成: total={}, success={}, failed={}, rate={}%",
                relations.size(), successList.size(), failedList.size(), String.format("%.2f", result.getSuccessRate()));

        return result;
    }

    @lombok.Data
    public static class BatchDeleteLineageRequest {
        private List<Long> ids;
    }

    @Transactional
    public BatchLineageResult deleteLineageBatch(BatchDeleteLineageRequest request, String deletedBy) {
        List<Long> ids = request.getIds();
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("血缘关系ID列表不能为空");
        }

        log.info("开始批量删除血缘关系: total={}, deletedBy={}", ids.size(), deletedBy);

        BatchLineageResult result = new BatchLineageResult();
        result.setTotalCount(ids.size());

        List<LineageRelation> successList = new ArrayList<>();
        List<FailedLineageRelation> failedList = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            int index = i + 1;

            try {
                Optional<LineageRelation> optionalRelation = lineageRelationRepository.findById(id);

                if (optionalRelation.isEmpty()) {
                    failedList.add(new FailedLineageRelation());
                    FailedLineageRelation failed = failedList.get(failedList.size() - 1);
                    failed.setIndex(index);
                    failed.setId(id);
                    failed.setErrorMessage("血缘关系不存在");
                    failed.setErrorType("NotFoundException");
                    continue;
                }

                LineageRelation relation = optionalRelation.get();

                relation.setIsDeleted(true);
                relation.setDeletedBy(deletedBy);
                relation.setDeletedTime(LocalDateTime.now());
                lineageRelationRepository.save(relation);

                lineageGraphRepository.deleteLineageRelation(
                        relation.getSourceAssetId(),
                        relation.getTargetAssetId()
                );

                recordHistory(relation.getId(), "DELETE", relationToMap(relation), null, deletedBy);

                successList.add(relation);
                log.debug("批量血缘删除-成功: {}/{}, id={}", index, ids.size(), id);

            } catch (Exception e) {
                log.error("批量血缘删除-失败: {}/{}, id={}, error={}", index, ids.size(), id, e.getMessage());

                failedList.add(new FailedLineageRelation());
                FailedLineageRelation failed = failedList.get(failedList.size() - 1);
                failed.setIndex(index);
                failed.setId(id);
                failed.setErrorMessage(e.getMessage());
                failed.setErrorType(e.getClass().getSimpleName());
            }
        }

        result.setSuccessCount(successList.size());
        result.setFailedCount(failedList.size());
        result.setSuccessRate(ids.size() > 0 ? (double) successList.size() / ids.size() * 100 : 0.0);
        result.setSuccessRelations(successList);
        result.setFailedRelations(failedList);
        result.setCompletedAt(LocalDateTime.now());

        log.info("批量删除血缘关系完成: total={}, success={}, failed={}",
                ids.size(), successList.size(), failedList.size());

        return result;
    }
}
