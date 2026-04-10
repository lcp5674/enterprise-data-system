package com.enterprise.edams.knowledge.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.knowledge.dto.*;
import com.enterprise.edams.knowledge.service.GraphQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 图谱查询接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Tag(name = "图谱查询", description = "图谱节点、边和路径的查询接口")
@RestController
@RequestMapping("/api/v1/knowledge/query")
@RequiredArgsConstructor
public class GraphQueryController {

    private final GraphQueryService graphQueryService;

    @Operation(summary = "执行图谱查询", description = "通用的图谱查询接口，支持节点、边、路径和子图查询")
    @PostMapping
    public Result<GraphQueryResultDTO> executeQuery(@RequestBody GraphQueryDTO query) {
        GraphQueryResultDTO result = graphQueryService.executeQuery(query);
        return Result.success(result);
    }

    @Operation(summary = "获取节点详情", description = "根据节点ID获取节点详细信息")
    @GetMapping("/nodes/{nodeId}")
    public Result<GraphNodeDTO> getNode(
            @Parameter(description = "图谱ID") @RequestParam String graphId,
            @Parameter(description = "节点ID") @PathVariable String nodeId) {
        GraphNodeDTO node = graphQueryService.getNode(graphId, nodeId);
        return Result.success(node);
    }

    @Operation(summary = "获取节点列表", description = "获取图谱中的节点列表，支持类型和关键词过滤")
    @GetMapping("/nodes")
    public Result<List<GraphNodeDTO>> listNodes(
            @Parameter(description = "图谱ID") @RequestParam String graphId,
            @Parameter(description = "节点类型") @RequestParam(required = false) String nodeType,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "100") int limit,
            @Parameter(description = "偏移量") @RequestParam(defaultValue = "0") int offset) {
        List<GraphNodeDTO> nodes = graphQueryService.listNodes(graphId, nodeType, keyword, limit, offset);
        return Result.success(nodes);
    }

    @Operation(summary = "批量获取节点", description = "根据节点ID列表批量获取节点信息")
    @PostMapping("/nodes/batch")
    public Result<List<GraphNodeDTO>> batchGetNodes(
            @Parameter(description = "图谱ID") @RequestParam String graphId,
            @RequestBody List<String> nodeIds) {
        List<GraphNodeDTO> nodes = graphQueryService.batchGetNodes(graphId, nodeIds);
        return Result.success(nodes);
    }

    @Operation(summary = "获取边详情", description = "根据边ID获取边的详细信息")
    @GetMapping("/edges/{edgeId}")
    public Result<GraphEdgeDTO> getEdge(
            @Parameter(description = "图谱ID") @RequestParam String graphId,
            @Parameter(description = "边ID") @PathVariable String edgeId) {
        GraphEdgeDTO edge = graphQueryService.getEdge(graphId, edgeId);
        return Result.success(edge);
    }

    @Operation(summary = "获取节点的边", description = "获取指定节点的所有边，支持方向过滤")
    @GetMapping("/nodes/{nodeId}/edges")
    public Result<List<GraphEdgeDTO>> listNodeEdges(
            @Parameter(description = "图谱ID") @RequestParam String graphId,
            @Parameter(description = "节点ID") @PathVariable String nodeId,
            @Parameter(description = "方向: OUT-出边, IN-入边, BOTH-全部") @RequestParam(defaultValue = "BOTH") String direction) {
        List<GraphEdgeDTO> edges = graphQueryService.listNodeEdges(graphId, nodeId, direction);
        return Result.success(edges);
    }

    @Operation(summary = "查询最短路径", description = "查询两节点间的最短路径")
    @GetMapping("/paths/shortest")
    public Result<List<GraphQueryResultDTO.PathDTO>> findShortestPath(
            @Parameter(description = "图谱ID") @RequestParam String graphId,
            @Parameter(description = "起始节点ID") @RequestParam String startNodeId,
            @Parameter(description = "目标节点ID") @RequestParam String targetNodeId,
            @Parameter(description = "最大深度") @RequestParam(defaultValue = "5") int maxDepth) {
        List<GraphQueryResultDTO.PathDTO> paths = graphQueryService.findShortestPath(graphId, startNodeId, targetNodeId, maxDepth);
        return Result.success(paths);
    }

    @Operation(summary = "查询所有路径", description = "查询从指定节点出发的所有路径")
    @GetMapping("/paths/from/{nodeId}")
    public Result<List<GraphQueryResultDTO.PathDTO>> findAllPaths(
            @Parameter(description = "图谱ID") @RequestParam String graphId,
            @Parameter(description = "起始节点ID") @PathVariable String nodeId,
            @Parameter(description = "最大深度") @RequestParam(defaultValue = "3") int depth) {
        List<GraphQueryResultDTO.PathDTO> paths = graphQueryService.findAllPaths(graphId, nodeId, depth);
        return Result.success(paths);
    }

    @Operation(summary = "获取子图", description = "获取以指定节点为中心的N度子图")
    @GetMapping("/subgraph/{centerNodeId}")
    public Result<GraphQueryResultDTO.SubgraphDTO> getSubgraph(
            @Parameter(description = "图谱ID") @RequestParam String graphId,
            @Parameter(description = "中心节点ID") @PathVariable String centerNodeId,
            @Parameter(description = "深度") @RequestParam(defaultValue = "2") int depth) {
        GraphQueryResultDTO.SubgraphDTO subgraph = graphQueryService.getSubgraph(graphId, centerNodeId, depth);
        return Result.success(subgraph);
    }

    @Operation(summary = "执行Cypher查询", description = "直接执行Cypher查询语句")
    @PostMapping("/cypher")
    public Result<GraphQueryResultDTO> executeCypher(
            @Parameter(description = "图谱ID") @RequestParam String graphId,
            @Parameter(description = "Cypher语句") @RequestBody Map<String, Object> request) {
        String cypher = (String) request.get("cypher");
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.get("params");
        GraphQueryResultDTO result = graphQueryService.executeCypher(graphId, cypher, params);
        return Result.success(result);
    }
}
