package com.enterprise.edams.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.knowledge.dto.KnowledgeGraphDTO;
import com.enterprise.edams.knowledge.entity.KnowledgeGraph;
import com.enterprise.edams.knowledge.mapper.KnowledgeGraphMapper;
import com.enterprise.edams.knowledge.service.KnowledgeGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识图谱服务实现类
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeGraphServiceImpl extends ServiceImpl<KnowledgeGraphMapper, KnowledgeGraph>
        implements KnowledgeGraphService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeGraph createGraph(KnowledgeGraphDTO dto) {
        log.info("创建知识图谱: {}", dto.getName());

        KnowledgeGraph graph = new KnowledgeGraph();
        graph.setGraphId(UUID.randomUUID().toString().replace("-", ""));
        graph.setName(dto.getName());
        graph.setDescription(dto.getDescription());
        graph.setGraphType(dto.getGraphType() != null ? dto.getGraphType() : "BUSINESS");
        graph.setStatus("DRAFT");
        graph.setNodeCount(0L);
        graph.setEdgeCount(0L);
        graph.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : "PRIVATE");
        graph.setTenantId(dto.getTenantId());
        graph.setCreator(dto.getCreator());
        graph.setConfig(dto.getConfig() != null ? toJson(dto.getConfig()) : null);

        this.save(graph);
        log.info("知识图谱创建成功: {}", graph.getGraphId());

        return graph;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeGraph updateGraph(String graphId, KnowledgeGraphDTO dto) {
        log.info("更新知识图谱: {}", graphId);

        KnowledgeGraph graph = this.getByGraphId(graphId);
        if (graph == null) {
            throw new RuntimeException("图谱不存在: " + graphId);
        }

        if (dto.getName() != null) {
            graph.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            graph.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            graph.setStatus(dto.getStatus());
        }
        if (dto.getVisibility() != null) {
            graph.setVisibility(dto.getVisibility());
        }
        if (dto.getConfig() != null) {
            graph.setConfig(toJson(dto.getConfig()));
        }
        graph.setUpdater(dto.getCreator());

        this.updateById(graph);
        log.info("知识图谱更新成功: {}", graphId);

        return graph;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGraph(String graphId) {
        log.info("删除知识图谱: {}", graphId);

        KnowledgeGraph graph = this.getByGraphId(graphId);
        if (graph == null) {
            throw new RuntimeException("图谱不存在: " + graphId);
        }

        // 软删除
        this.removeById(graph.getId());
        log.info("知识图谱删除成功: {}", graphId);
    }

    @Override
    public KnowledgeGraphDTO getGraphDetail(String graphId) {
        KnowledgeGraph graph = this.getByGraphId(graphId);
        if (graph == null) {
            throw new RuntimeException("图谱不存在: " + graphId);
        }

        return toDTO(graph);
    }

    @Override
    public List<KnowledgeGraphDTO> listGraphs(String tenantId, String keyword, String graphType, String status) {
        LambdaQueryWrapper<KnowledgeGraph> wrapper = new LambdaQueryWrapper<>();

        if (tenantId != null) {
            wrapper.eq(KnowledgeGraph::getTenantId, tenantId);
        }
        if (keyword != null) {
            wrapper.and(w -> w.like(KnowledgeGraph::getName, keyword)
                    .or()
                    .like(KnowledgeGraph::getDescription, keyword));
        }
        if (graphType != null) {
            wrapper.eq(KnowledgeGraph::getGraphType, graphType);
        }
        if (status != null) {
            wrapper.eq(KnowledgeGraph::getStatus, status);
        }

        wrapper.orderByDesc(KnowledgeGraph::getCreatedTime);

        return this.list(wrapper).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateGraph(String graphId) {
        log.info("激活知识图谱: {}", graphId);

        KnowledgeGraph graph = this.getByGraphId(graphId);
        if (graph == null) {
            throw new RuntimeException("图谱不存在: " + graphId);
        }

        graph.setStatus("ACTIVE");
        this.updateById(graph);
        log.info("知识图谱激活成功: {}", graphId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveGraph(String graphId) {
        log.info("归档知识图谱: {}", graphId);

        KnowledgeGraph graph = this.getByGraphId(graphId);
        if (graph == null) {
            throw new RuntimeException("图谱不存在: " + graphId);
        }

        graph.setStatus("ARCHIVED");
        this.updateById(graph);
        log.info("知识图谱归档成功: {}", graphId);
    }

    @Override
    public KnowledgeGraphDTO.GraphStatistics getGraphStatistics(String graphId) {
        KnowledgeGraph graph = this.getByGraphId(graphId);
        if (graph == null) {
            throw new RuntimeException("图谱不存在: " + graphId);
        }

        // 实际实现中应从Neo4j查询真实统计信息
        return KnowledgeGraphDTO.GraphStatistics.builder()
                .totalNodes(graph.getNodeCount())
                .totalEdges(graph.getEdgeCount())
                .activeNodes(graph.getNodeCount())
                .activeEdges(graph.getEdgeCount())
                .avgDegree(graph.getNodeCount() > 0 ? 
                        (double) graph.getEdgeCount() * 2 / graph.getNodeCount() : 0.0)
                .connectedComponents(1L)
                .density(graph.getNodeCount() > 1 ? 
                        (double) graph.getEdgeCount() / (graph.getNodeCount() * (graph.getNodeCount() - 1) / 2) : 0.0)
                .build();
    }

    @Override
    public void updateGraphCounts(String graphId) {
        // 从Neo4j获取实际数量并更新
        // 这里使用占位实现
        log.info("更新图谱数量: {}", graphId);
    }

    private KnowledgeGraph getByGraphId(String graphId) {
        return baseMapper.selectByGraphId(graphId);
    }

    private KnowledgeGraphDTO toDTO(KnowledgeGraph graph) {
        return KnowledgeGraphDTO.builder()
                .graphId(graph.getGraphId())
                .name(graph.getName())
                .description(graph.getDescription())
                .graphType(graph.getGraphType())
                .status(graph.getStatus())
                .nodeCount(graph.getNodeCount())
                .edgeCount(graph.getEdgeCount())
                .visibility(graph.getVisibility())
                .createdTime(graph.getCreatedTime())
                .updatedTime(graph.getUpdatedTime())
                .creator(graph.getCreator())
                .config(graph.getConfig() != null ? parseJson(graph.getConfig()) : null)
                .build();
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON转换失败", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        if (json == null) return null;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, Map.class);
        } catch (Exception e) {
            log.error("JSON解析失败", e);
            return null;
        }
    }
}
