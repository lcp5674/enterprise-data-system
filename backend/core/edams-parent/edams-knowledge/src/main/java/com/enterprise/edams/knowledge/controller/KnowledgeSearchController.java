package com.enterprise.edams.knowledge.controller;

import com.enterprise.edams.knowledge.dto.EntityDetailDTO;
import com.enterprise.edams.knowledge.dto.KnowledgeSearchDTO;
import com.enterprise.edams.knowledge.service.KnowledgeGraphService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识图谱搜索控制器
 */
@Tag(name = "知识搜索", description = "知识图谱的搜索和图分析功能")
@RestController
@RequestMapping("/api/knowledge/search")
@RequiredArgsConstructor
public class KnowledgeSearchController {

    private final KnowledgeGraphService knowledgeGraphService;

    @GetMapping
    @Operation(summary = "搜索知识")
    public Result<KnowledgeSearchDTO> search(
            @Parameter(description = "本体论ID") @RequestParam(required = false) Long ontologyId,
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "20") int limit) {
        KnowledgeSearchDTO result = knowledgeGraphService.search(ontologyId, keyword, limit);
        return Result.success(result);
    }

    @GetMapping("/{entityId}/related")
    @Operation(summary = "获取关联实体 (一跳)")
    public Result<List<Long>> getRelatedEntities(@PathVariable Long entityId) {
        List<Long> related = knowledgeGraphService.getRelatedEntities(entityId);
        return Result.success(related);
    }

    @GetMapping("/{entityId}/multihop")
    @Operation(summary = "获取关联实体 (多跳)")
    public Result<List<Long>> getMultiHopEntities(
            @PathVariable @Parameter(description = "实体ID") Long entityId,
            @RequestParam @Parameter(description = "跳数") int hops) {
        List<Long> related = knowledgeGraphService.getMultiHopEntities(entityId, hops);
        return Result.success(related);
    }

    @GetMapping("/path")
    @Operation(summary = "查找两个实体间的最短路径")
    public Result<List<Long>> findPath(
            @RequestParam @Parameter(description = "源实体ID") Long sourceId,
            @RequestParam @Parameter(description = "目标实体ID") Long targetId,
            @RequestParam(defaultValue = "5") @Parameter(description = "最大跳数") int maxHops) {
        List<Long> path = knowledgeGraphService.findPath(sourceId, targetId, maxHops);
        return Result.success(path);
    }

    @GetMapping("/{entityId}/similar")
    @Operation(summary = "发现相似实体")
    public Result<List<Long>> findSimilarEntities(
            @PathVariable @Parameter(description = "实体ID") Long entityId,
            @RequestParam(defaultValue = "10") @Parameter(description = "返回数量") int limit) {
        List<Long> similar = knowledgeGraphService.findSimilarEntities(entityId, limit);
        return Result.success(similar);
    }

    @GetMapping("/{entityId}/subgraph")
    @Operation(summary = "获取实体的子图")
    public Result<EntityDetailDTO> getEntitySubgraph(
            @PathVariable @Parameter(description = "实体ID") Long entityId,
            @RequestParam(defaultValue = "2") @Parameter(description = "深度") int depth) {
        EntityDetailDTO subgraph = knowledgeGraphService.getEntitySubgraph(entityId, depth);
        return Result.success(subgraph);
    }
}
