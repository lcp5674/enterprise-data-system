package com.enterprise.edams.knowledge.service;

import com.enterprise.edams.knowledge.dto.GraphQueryDTO;
import com.enterprise.edams.knowledge.dto.GraphQueryResultDTO;
import com.enterprise.edams.knowledge.dto.GraphNodeDTO;
import com.enterprise.edams.knowledge.dto.GraphEdgeDTO;

import java.util.List;

/**
 * 图谱查询服务接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
public interface GraphQueryService {

    /**
     * 执行图谱查询
     *
     * @param query 查询条件
     * @return 查询结果
     */
    GraphQueryResultDTO executeQuery(GraphQueryDTO query);

    /**
     * 查询节点
     *
     * @param graphId 图谱ID
     * @param nodeId 节点ID
     * @return 节点详情
     */
    GraphNodeDTO getNode(String graphId, String nodeId);

    /**
     * 查询节点列表
     *
     * @param graphId 图谱ID
     * @param nodeType 节点类型
     * @param keyword 关键词
     * @param limit 返回数量
     * @param offset 偏移量
     * @return 节点列表
     */
    List<GraphNodeDTO> listNodes(String graphId, String nodeType, String keyword, int limit, int offset);

    /**
     * 查询边
     *
     * @param graphId 图谱ID
     * @param edgeId 边ID
     * @return 边详情
     */
    GraphEdgeDTO getEdge(String graphId, String edgeId);

    /**
     * 查询节点的所有边
     *
     * @param graphId 图谱ID
     * @param nodeId 节点ID
     * @param direction 方向 (OUT, IN, BOTH)
     * @return 边列表
     */
    List<GraphEdgeDTO> listNodeEdges(String graphId, String nodeId, String direction);

    /**
     * 查询两节点间的最短路径
     *
     * @param graphId 图谱ID
     * @param startNodeId 起始节点ID
     * @param targetNodeId 目标节点ID
     * @param maxDepth 最大深度
     * @return 路径列表
     */
    List<GraphQueryResultDTO.PathDTO> findShortestPath(String graphId, String startNodeId, 
                                                        String targetNodeId, int maxDepth);

    /**
     * 查询指定深度的子图
     *
     * @param graphId 图谱ID
     * @param centerNodeId 中心节点ID
     * @param depth 深度
     * @return 子图
     */
    GraphQueryResultDTO.SubgraphDTO getSubgraph(String graphId, String centerNodeId, int depth);

    /**
     * 执行Cypher查询
     *
     * @param graphId 图谱ID
     * @param cypher Cypher语句
     * @param params 参数
     * @return 查询结果
     */
    GraphQueryResultDTO executeCypher(String graphId, String cypher, java.util.Map<String, Object> params);

    /**
     * 获取节点的所有路径
     *
     * @param graphId 图谱ID
     * @param nodeId 节点ID
     * @param depth 最大深度
     * @return 路径列表
     */
    List<GraphQueryResultDTO.PathDTO> findAllPaths(String graphId, String nodeId, int depth);

    /**
     * 批量查询节点
     *
     * @param graphId 图谱ID
     * @param nodeIds 节点ID列表
     * @return 节点列表
     */
    List<GraphNodeDTO> batchGetNodes(String graphId, List<String> nodeIds);
}
