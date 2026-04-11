package com.enterprise.edams.knowledge.service;

import com.enterprise.edams.knowledge.dto.EntityDetailDTO;
import com.enterprise.edams.knowledge.dto.KnowledgeSearchDTO;

import java.util.List;

/**
 * 知识图谱服务接口
 * 提供综合知识查询和分析能力
 */
public interface KnowledgeGraphService {

    /**
     * 搜索知识
     */
    KnowledgeSearchDTO search(Long ontologyId, String keyword, int limit);

    /**
     * 获取实体的关联实体 (一跳)
     */
    List<Long> getRelatedEntities(Long entityId);

    /**
     * 获取实体的关联实体 (多跳)
     */
    List<Long> getMultiHopEntities(Long entityId, int hops);

    /**
     * 发现两个实体间的最短路径
     */
    List<Long> findPath(Long sourceId, Long targetId, int maxHops);

    /**
     * 分析实体相似度
     */
    List<Long> findSimilarEntities(Long entityId, int limit);

    /**
     * 获取子图
     */
    EntityDetailDTO getEntitySubgraph(Long entityId, int depth);
}
