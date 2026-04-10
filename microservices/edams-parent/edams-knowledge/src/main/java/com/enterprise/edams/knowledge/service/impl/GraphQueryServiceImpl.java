package com.enterprise.edams.knowledge.service.impl;

import com.enterprise.edams.knowledge.config.KnowledgeConfig;
import com.enterprise.edams.knowledge.dto.GraphEdgeDTO;
import com.enterprise.edams.knowledge.dto.GraphNodeDTO;
import com.enterprise.edams.knowledge.dto.GraphQueryDTO;
import com.enterprise.edams.knowledge.dto.GraphQueryResultDTO;
import com.enterprise.edams.knowledge.entity.GraphEdge;
import com.enterprise.edams.knowledge.entity.GraphNode;
import com.enterprise.edams.knowledge.mapper.GraphEdgeMapper;
import com.enterprise.edams.knowledge.mapper.GraphNodeMapper;
import com.enterprise.edams.knowledge.service.GraphQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 图谱查询服务实现类
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphQueryServiceImpl implements GraphQueryService {

    private final Driver neo4jDriver;
    private final GraphNodeMapper graphNodeMapper;
    private final GraphEdgeMapper graphEdgeMapper;
    private final KnowledgeConfig knowledgeConfig;
    private final ObjectMapper objectMapper;

    @Override
    @CircuitBreaker(name = "neo4j", fallbackMethod = "executeQueryFallback")
    public GraphQueryResultDTO executeQuery(GraphQueryDTO query) {
        log.info("执行图谱查询: type={}, graphId={}", query.getQueryType(), query.getGraphId());

        long startTime = System.currentTimeMillis();
        GraphQueryResultDTO result;

        try (Session session = neo4jDriver.session()) {
            result = switch (query.getQueryType().toUpperCase()) {
                case "NODE" -> queryNodes(session, query);
                case "EDGE" -> queryEdges(session, query);
                case "PATH" -> queryPaths(session, query);
                case "SUBGRAPH" -> querySubgraph(session, query);
                default -> throw new IllegalArgumentException("不支持的查询类型: " + query.getQueryType());
            };
        }

        result.setQueryTime(System.currentTimeMillis() - startTime);
        log.info("图谱查询完成, 耗时: {}ms", result.getQueryTime());

        return result;
    }

    @Override
    public GraphNodeDTO getNode(String graphId, String nodeId) {
        log.info("查询节点: graphId={}, nodeId={}", graphId, nodeId);

        GraphNode node = graphNodeMapper.selectByNodeId(nodeId);
        if (node == null) {
            return getNodeFromNeo4j(graphId, nodeId);
        }

        return toNodeDTO(node);
    }

    @Override
    public List<GraphNodeDTO> listNodes(String graphId, String nodeType, String keyword, int limit, int offset) {
        log.info("查询节点列表: graphId={}, nodeType={}, keyword={}", graphId, nodeType, keyword);

        List<GraphNode> nodes;
        if (keyword != null && !keyword.isEmpty()) {
            nodes = graphNodeMapper.selectByNameLike(graphId, keyword);
        } else if (nodeType != null) {
            nodes = graphNodeMapper.selectByNodeType(graphId, nodeType);
        } else {
            nodes = graphNodeMapper.selectByGraphId(graphId);
        }

        int fromIndex = Math.min(offset, nodes.size());
        int toIndex = Math.min(offset + limit, nodes.size());

        return nodes.subList(fromIndex, toIndex).stream()
                .map(this::toNodeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GraphEdgeDTO getEdge(String graphId, String edgeId) {
        GraphEdge edge = graphEdgeMapper.selectByEdgeId(edgeId);
        return edge != null ? toEdgeDTO(edge) : getEdgeFromNeo4j(graphId, edgeId);
    }

    @Override
    public List<GraphEdgeDTO> listNodeEdges(String graphId, String nodeId, String direction) {
        List<GraphEdge> edges = switch (direction.toUpperCase()) {
            case "OUT" -> graphEdgeMapper.selectBySourceNodeId(nodeId);
            case "IN" -> graphEdgeMapper.selectByTargetNodeId(nodeId);
            default -> {
                List<GraphEdge> outEdges = graphEdgeMapper.selectBySourceNodeId(nodeId);
                List<GraphEdge> inEdges = graphEdgeMapper.selectByTargetNodeId(nodeId);
                List<GraphEdge> allEdges = new ArrayList<>(outEdges);
                allEdges.addAll(inEdges);
                yield allEdges;
            }
        };

        return edges.stream()
                .map(this::toEdgeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GraphQueryResultDTO.PathDTO> findShortestPath(String graphId, String startNodeId,
                                                               String targetNodeId, int maxDepth) {
        log.info("查询最短路径: graphId={}, start={}, target={}, depth={}",
                graphId, startNodeId, targetNodeId, maxDepth);

        String cypher = """
            MATCH path = shortestPath((start)-[*1..%d]-(target))
            WHERE start.nodeId = $startNodeId AND target.nodeId = $targetNodeId
            RETURN path
            """.formatted(Math.min(maxDepth, knowledgeConfig.getGraph().getMaxQueryDepth()));

        try (Session session = neo4jDriver.session()) {
            List<Record> records = session.run(cypher,
                    Map.of("startNodeId", startNodeId, "targetNodeId", targetNodeId)).list();

            return records.stream()
                    .map(record -> parsePath(record.get("path")))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public GraphQueryResultDTO.SubgraphDTO getSubgraph(String graphId, String centerNodeId, int depth) {
        log.info("查询子图: graphId={}, center={}, depth={}", graphId, centerNodeId, depth);

        int actualDepth = Math.min(depth, knowledgeConfig.getGraph().getMaxQueryDepth());
        String cypher = """
            MATCH path = (center)-[*1..%d]-(neighbor)
            WHERE center.nodeId = $centerNodeId
            WITH collect(DISTINCT center) + collect(DISTINCT neighbor) AS nodes,
                 collect(DISTINCT relationships(path)) AS rels
            UNWIND nodes AS node
            WITH collect(DISTINCT node) AS allNodes, rels
            UNWIND rels AS rel
            UNWIND rel AS r
            RETURN allNodes, collect(DISTINCT r) AS edges
            """.formatted(actualDepth);

        try (Session session = neo4jDriver.session()) {
            Record record = session.run(cypher,
                    Map.of("centerNodeId", centerNodeId)).single();

            return parseSubgraph(record);
        }
    }

    @Override
    @CircuitBreaker(name = "neo4j", fallbackMethod = "executeCypherFallback")
    public GraphQueryResultDTO executeCypher(String graphId, String cypher, Map<String, Object> params) {
        log.info("执行Cypher查询: graphId={}", graphId);

        long startTime = System.currentTimeMillis();

        try (Session session = neo4jDriver.session()) {
            org.neo4j.driver.Result result = session.run(cypher, params);

            List<GraphNodeDTO> nodes = new ArrayList<>();
            List<GraphEdgeDTO> edges = new ArrayList<>();

            for (Record record : result.list()) {
                for (Value value : record.values()) {
                    if (value.type().name().equals("NODE")) {
                        nodes.add(parseNeo4jNode(value.asNode()));
                    } else if (value.type().name().equals("RELATIONSHIP")) {
                        edges.add(parseNeo4jRelationship(value.asRelationship()));
                    }
                }
            }

            return GraphQueryResultDTO.builder()
                    .queryType("CYPHER")
                    .nodes(nodes)
                    .edges(edges)
                    .totalCount((long) (nodes.size() + edges.size()))
                    .hasMore(false)
                    .build();
        }
    }

    @Override
    public List<GraphQueryResultDTO.PathDTO> findAllPaths(String graphId, String nodeId, int depth) {
        int actualDepth = Math.min(depth, knowledgeConfig.getGraph().getMaxQueryDepth());

        String cypher = """
            MATCH path = (start)-[*1..%d]-(end)
            WHERE start.nodeId = $nodeId
            RETURN path
            """.formatted(actualDepth);

        try (Session session = neo4jDriver.session()) {
            List<Record> records = session.run(cypher, Map.of("nodeId", nodeId)).list();

            return records.stream()
                    .map(record -> parsePath(record.get("path")))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<GraphNodeDTO> batchGetNodes(String graphId, List<String> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return List.of();
        }

        return nodeIds.stream()
                .map(nodeId -> getNode(graphId, nodeId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    private GraphQueryResultDTO queryNodes(Session session, GraphQueryDTO query) {
        String cypher;
        Map<String, Object> params = new HashMap<>();
        params.put("graphId", query.getGraphId());

        if (query.getNodeIds() != null && !query.getNodeIds().isEmpty()) {
            cypher = "MATCH (n) WHERE n.graphId = $graphId AND n.nodeId IN $nodeIds RETURN n";
            params.put("nodeIds", query.getNodeIds());
        } else if (query.getNodeName() != null) {
            cypher = "MATCH (n) WHERE n.graphId = $graphId AND n.name CONTAINS $nodeName RETURN n";
            params.put("nodeName", query.getNodeName());
        } else if (query.getNodeType() != null) {
            cypher = "MATCH (n:%s) WHERE n.graphId = $graphId RETURN n".formatted(query.getNodeType());
        } else {
            cypher = "MATCH (n) WHERE n.graphId = $graphId RETURN n";
        }

        cypher += " SKIP $offset LIMIT $limit";
        params.put("offset", query.getOffset());
        params.put("limit", query.getLimit());

        List<Record> records = session.run(cypher, params).list();

        List<GraphNodeDTO> nodes = records.stream()
                .map(r -> parseNeo4jNode(r.get("n").asNode()))
                .collect(Collectors.toList());

        return GraphQueryResultDTO.builder()
                .queryType(query.getQueryType())
                .nodes(nodes)
                .totalCount((long) nodes.size())
                .hasMore(nodes.size() == query.getLimit())
                .build();
    }

    private GraphQueryResultDTO queryEdges(Session session, GraphQueryDTO query) {
        String cypher;
        Map<String, Object> params = new HashMap<>();
        params.put("graphId", query.getGraphId());

        if (query.getRelationType() != null) {
            cypher = "MATCH (s)-[r:%s]->(t) WHERE s.graphId = $graphId RETURN r, s, t".formatted(query.getRelationType());
        } else {
            cypher = "MATCH (s)-[r]->(t) WHERE s.graphId = $graphId RETURN r, s, t";
        }

        cypher += " SKIP $offset LIMIT $limit";
        params.put("offset", query.getOffset());
        params.put("limit", query.getLimit());

        List<Record> records = session.run(cypher, params).list();

        List<GraphEdgeDTO> edges = records.stream()
                .map(r -> parseNeo4jEdge(r.get("r").asRelationship(), r.get("s").asNode(), r.get("t").asNode()))
                .collect(Collectors.toList());

        return GraphQueryResultDTO.builder()
                .queryType(query.getQueryType())
                .edges(edges)
                .totalCount((long) edges.size())
                .hasMore(edges.size() == query.getLimit())
                .build();
    }

    private GraphQueryResultDTO queryPaths(Session session, GraphQueryDTO query) {
        if (query.getStartNodeId() == null || query.getTargetNodeId() == null) {
            return GraphQueryResultDTO.builder()
                    .queryType(query.getQueryType())
                    .paths(List.of())
                    .totalCount(0L)
                    .build();
        }

        int depth = Math.min(query.getDepth(), knowledgeConfig.getGraph().getMaxQueryDepth());
        String cypher = """
            MATCH path = (start)-[*1..%d]-(target)
            WHERE start.nodeId = $startNodeId AND target.nodeId = $targetNodeId
            RETURN path
            """.formatted(depth);

        List<Record> records = session.run(cypher,
                Map.of("startNodeId", query.getStartNodeId(), "targetNodeId", query.getTargetNodeId())).list();

        List<GraphQueryResultDTO.PathDTO> paths = records.stream()
                .map(r -> parsePath(r.get("path")))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return GraphQueryResultDTO.builder()
                .queryType(query.getQueryType())
                .paths(paths)
                .totalCount((long) paths.size())
                .hasMore(false)
                .build();
    }

    private GraphQueryResultDTO querySubgraph(Session session, GraphQueryDTO query) {
        if (query.getStartNodeId() == null) {
            return GraphQueryResultDTO.builder()
                    .queryType(query.getQueryType())
                    .subgraphs(List.of())
                    .totalCount(0L)
                    .build();
        }

        int depth = Math.min(query.getDepth(), knowledgeConfig.getGraph().getMaxQueryDepth());
        String cypher = """
            MATCH path = (center)-[*1..%d]-(neighbor)
            WHERE center.nodeId = $centerNodeId
            RETURN center, collect(DISTINCT neighbor) AS neighbors, 
                   [rel IN relationships(path) | rel] AS rels
            """.formatted(depth);

        Record record = session.run(cypher, Map.of("centerNodeId", query.getStartNodeId())).single();

        return GraphQueryResultDTO.builder()
                .queryType(query.getQueryType())
                .subgraphs(List.of(parseSubgraph(record)))
                .totalCount(1L)
                .hasMore(false)
                .build();
    }

    private GraphQueryResultDTO parseSubgraph(Record record) {
        List<GraphNodeDTO> nodes = new ArrayList<>();
        List<GraphEdgeDTO> edges = new ArrayList<>();

        if (record.containsKey("center")) {
            nodes.add(parseNeo4jNode(record.get("center").asNode()));
        }
        if (record.containsKey("allNodes")) {
            for (Value node : record.get("allNodes").asList()) {
                nodes.add(parseNeo4jNode(node.asNode()));
            }
        }
        if (record.containsKey("neighbors")) {
            for (Value neighbor : record.get("neighbors").asList()) {
                nodes.add(parseNeo4jNode(neighbor.asNode()));
            }
        }
        if (record.containsKey("rels")) {
            for (Value rel : record.get("rels").asList()) {
                edges.add(parseNeo4jEdge(rel.asRelationship(), null, null));
            }
        }

        return GraphQueryResultDTO.SubgraphDTO.builder()
                .subgraphId(UUID.randomUUID().toString())
                .nodes(nodes.stream().distinct().collect(Collectors.toList()))
                .edges(edges.stream().distinct().collect(Collectors.toList()))
                .nodeCount((long) nodes.size())
                .edgeCount((long) edges.size())
                .build();
    }

    private GraphQueryResultDTO.PathDTO parsePath(org.neo4j.driver.types.Path path) {
        if (path == null) return null;

        List<GraphNodeDTO> nodes = new ArrayList<>();
        List<GraphEdgeDTO> edges = new ArrayList<>();
        double totalWeight = 0.0;

        for (org.neo4j.driver.types.Path.Node node : path.nodes()) {
            nodes.add(parseNeo4jNode(node));
        }

        for (org.neo4j.driver.types.Path.Relationship rel : path.relationships()) {
            GraphEdgeDTO edge = parseNeo4jEdge(rel, null, null);
            edges.add(edge);
            if (edge.getWeight() != null) {
                totalWeight += edge.getWeight();
            }
        }

        return GraphQueryResultDTO.PathDTO.builder()
                .pathId(UUID.randomUUID().toString())
                .nodes(nodes)
                .edges(edges)
                .length(nodes.size() - 1)
                .totalWeight(totalWeight)
                .build();
    }

    private GraphNodeDTO parseNeo4jNode(org.neo4j.driver.types.Node node) {
        Map<String, Object> properties = new HashMap<>();
        node.keys().forEach(key -> {
            Object value = node.get(key).asObject();
            if (value instanceof org.neo4j.driver.types.Point) {
                properties.put(key, node.get(key).asPoint().toString());
            } else {
                properties.put(key, value);
            }
        });

        List<String> labels = node.labels().stream().toList();

        return GraphNodeDTO.builder()
                .nodeId(properties.get("nodeId") != null ? properties.get("nodeId").toString() : null)
                .name(properties.get("name") != null ? properties.get("name").toString() : null)
                .nodeType(properties.get("nodeType") != null ? properties.get("nodeType").toString() : null)
                .graphId(properties.get("graphId") != null ? properties.get("graphId").toString() : null)
                .labels(labels)
                .properties(properties)
                .status(properties.get("status") != null ? properties.get("status").toString() : "ACTIVE")
                .build();
    }

    private GraphEdgeDTO parseNeo4jEdge(org.neo4j.driver.types.Relationship rel,
                                         org.neo4j.driver.types.Node source,
                                         org.neo4j.driver.types.Node target) {
        Map<String, Object> properties = new HashMap<>();
        rel.keys().forEach(key -> properties.put(key, rel.get(key).asObject()));

        return GraphEdgeDTO.builder()
                .edgeId(properties.get("edgeId") != null ? properties.get("edgeId").toString() : null)
                .sourceNodeId(properties.get("sourceNodeId") != null ? properties.get("sourceNodeId").toString() : null)
                .targetNodeId(properties.get("targetNodeId") != null ? properties.get("targetNodeId").toString() : null)
                .relationType(rel.type())
                .graphId(properties.get("graphId") != null ? properties.get("graphId").toString() : null)
                .properties(properties)
                .weight(properties.get("weight") != null ? Double.parseDouble(properties.get("weight").toString()) : 1.0)
                .status(properties.get("status") != null ? properties.get("status").toString() : "ACTIVE")
                .build();
    }

    private GraphNodeDTO getNodeFromNeo4j(String graphId, String nodeId) {
        String cypher = "MATCH (n) WHERE n.graphId = $graphId AND n.nodeId = $nodeId RETURN n";

        try (Session session = neo4jDriver.session()) {
            Record record = session.run(cypher,
                    Map.of("graphId", graphId, "nodeId", nodeId)).single();
            return parseNeo4jNode(record.get("n").asNode());
        } catch (Exception e) {
            log.error("从Neo4j查询节点失败: {}", nodeId, e);
            return null;
        }
    }

    private GraphEdgeDTO getEdgeFromNeo4j(String graphId, String edgeId) {
        String cypher = "MATCH (s)-[r]->(t) WHERE r.graphId = $graphId AND r.edgeId = $edgeId RETURN r, s, t";

        try (Session session = neo4jDriver.session()) {
            Record record = session.run(cypher,
                    Map.of("graphId", graphId, "edgeId", edgeId)).single();
            return parseNeo4jEdge(record.get("r").asRelationship(),
                    record.get("s").asNode(), record.get("t").asNode());
        } catch (Exception e) {
            log.error("从Neo4j查询边失败: {}", edgeId, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private GraphNodeDTO toNodeDTO(GraphNode node) {
        Map<String, Object> properties = node.getProperties() != null ?
                parseJson(node.getProperties()) : new HashMap<>();

        return GraphNodeDTO.builder()
                .nodeId(node.getNodeId())
                .name(node.getName())
                .nodeType(node.getNodeType())
                .graphId(node.getGraphId())
                .labels(node.getLabels() != null ? List.of(node.getLabels().split(",")) : List.of())
                .properties(properties)
                .dataSource(node.getDataSource())
                .qualityScore(node.getQualityScore())
                .status(node.getStatus())
                .createdTime(node.getCreatedTime())
                .lastUpdatedAt(node.getLastUpdatedAt())
                .creator(node.getCreator())
                .remark(node.getRemark())
                .build();
    }

    private GraphEdgeDTO toEdgeDTO(GraphEdge edge) {
        return GraphEdgeDTO.builder()
                .edgeId(edge.getEdgeId())
                .sourceNodeId(edge.getSourceNodeId())
                .targetNodeId(edge.getTargetNodeId())
                .relationType(edge.getRelationType())
                .graphId(edge.getGraphId())
                .properties(edge.getProperties() != null ? parseJson(edge.getProperties()) : new HashMap<>())
                .weight(edge.getWeight())
                .dataSource(edge.getDataSource())
                .qualityScore(edge.getQualityScore())
                .status(edge.getStatus())
                .createdTime(edge.getCreatedTime())
                .startTime(edge.getStartTime())
                .endTime(edge.getEndTime())
                .creator(edge.getCreator())
                .remark(edge.getRemark())
                .build();
    }

    private Map<String, Object> parseJson(String json) {
        if (json == null) return new HashMap<>();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("JSON解析失败", e);
            return new HashMap<>();
        }
    }

    // Fallback methods
    public GraphQueryResultDTO executeQueryFallback(GraphQueryDTO query, Exception e) {
        log.error("图谱查询熔断降级: {}", e.getMessage());
        return GraphQueryResultDTO.builder()
                .queryType(query.getQueryType())
                .nodes(List.of())
                .edges(List.of())
                .totalCount(0L)
                .hasMore(false)
                .build();
    }

    public GraphQueryResultDTO executeCypherFallback(String graphId, String cypher, 
                                                      Map<String, Object> params, Exception e) {
        log.error("Cypher查询熔断降级: {}", e.getMessage());
        return GraphQueryResultDTO.builder()
                .queryType("CYPHER")
                .nodes(List.of())
                .edges(List.of())
                .totalCount(0L)
                .hasMore(false)
                .build();
    }
}
