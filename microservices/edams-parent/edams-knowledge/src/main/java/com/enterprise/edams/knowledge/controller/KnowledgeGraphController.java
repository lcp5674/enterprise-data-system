package com.enterprise.edams.knowledge.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.knowledge.dto.KnowledgeGraphDTO;
import com.enterprise.edams.knowledge.entity.KnowledgeGraph;
import com.enterprise.edams.knowledge.service.KnowledgeGraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识图谱管理接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Tag(name = "知识图谱管理", description = "知识图谱的创建、更新、删除和查询接口")
@RestController
@RequestMapping("/api/v1/knowledge/graphs")
@RequiredArgsConstructor
public class KnowledgeGraphController {

    private final KnowledgeGraphService knowledgeGraphService;

    @Operation(summary = "创建知识图谱", description = "创建一个新的知识图谱")
    @PostMapping
    public Result<KnowledgeGraph> createGraph(@RequestBody KnowledgeGraphDTO dto) {
        KnowledgeGraph graph = knowledgeGraphService.createGraph(dto);
        return Result.success("图谱创建成功", graph);
    }

    @Operation(summary = "更新知识图谱", description = "更新指定知识图谱的信息")
    @PutMapping("/{graphId}")
    public Result<KnowledgeGraph> updateGraph(
            @Parameter(description = "图谱ID") @PathVariable String graphId,
            @RequestBody KnowledgeGraphDTO dto) {
        KnowledgeGraph graph = knowledgeGraphService.updateGraph(graphId, dto);
        return Result.success("图谱更新成功", graph);
    }

    @Operation(summary = "删除知识图谱", description = "删除指定的知识图谱")
    @DeleteMapping("/{graphId}")
    public Result<Void> deleteGraph(
            @Parameter(description = "图谱ID") @PathVariable String graphId) {
        knowledgeGraphService.deleteGraph(graphId);
        return Result.success("图谱删除成功", null);
    }

    @Operation(summary = "获取图谱详情", description = "获取指定知识图谱的详细信息")
    @GetMapping("/{graphId}")
    public Result<KnowledgeGraphDTO> getGraphDetail(
            @Parameter(description = "图谱ID") @PathVariable String graphId) {
        KnowledgeGraphDTO graph = knowledgeGraphService.getGraphDetail(graphId);
        return Result.success(graph);
    }

    @Operation(summary = "获取图谱列表", description = "获取知识图谱列表，支持条件筛选")
    @GetMapping
    public Result<List<KnowledgeGraphDTO>> listGraphs(
            @Parameter(description = "租户ID") @RequestParam(required = false) String tenantId,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "图谱类型") @RequestParam(required = false) String graphType,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {
        List<KnowledgeGraphDTO> graphs = knowledgeGraphService.listGraphs(tenantId, keyword, graphType, status);
        return Result.success(graphs);
    }

    @Operation(summary = "激活图谱", description = "将草稿状态的图谱激活")
    @PostMapping("/{graphId}/activate")
    public Result<Void> activateGraph(
            @Parameter(description = "图谱ID") @PathVariable String graphId) {
        knowledgeGraphService.activateGraph(graphId);
        return Result.success("图谱激活成功", null);
    }

    @Operation(summary = "归档图谱", description = "将活跃状态的图谱归档")
    @PostMapping("/{graphId}/archive")
    public Result<Void> archiveGraph(
            @Parameter(description = "图谱ID") @PathVariable String graphId) {
        knowledgeGraphService.archiveGraph(graphId);
        return Result.success("图谱归档成功", null);
    }

    @Operation(summary = "获取图谱统计信息", description = "获取指定图谱的统计信息")
    @GetMapping("/{graphId}/statistics")
    public Result<KnowledgeGraphDTO.GraphStatistics> getGraphStatistics(
            @Parameter(description = "图谱ID") @PathVariable String graphId) {
        KnowledgeGraphDTO.GraphStatistics statistics = knowledgeGraphService.getGraphStatistics(graphId);
        return Result.success(statistics);
    }

    @Operation(summary = "更新图谱数量", description = "更新图谱的节点和边数量统计")
    @PostMapping("/{graphId}/counts")
    public Result<Void> updateGraphCounts(
            @Parameter(description = "图谱ID") @PathVariable String graphId) {
        knowledgeGraphService.updateGraphCounts(graphId);
        return Result.success("图谱数量更新成功", null);
    }
}
