package com.enterprise.edams.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.knowledge.entity.KnowledgeGraph;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 知识图谱Mapper接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Mapper
public interface KnowledgeGraphMapper extends BaseMapper<KnowledgeGraph> {

    /**
     * 根据图谱ID查询
     *
     * @param graphId 图谱ID
     * @return 知识图谱
     */
    KnowledgeGraph selectByGraphId(@Param("graphId") String graphId);

    /**
     * 更新图谱节点和边数量
     *
     * @param graphId 图谱ID
     * @param nodeCount 节点数量
     * @param edgeCount 边数量
     * @return 影响行数
     */
    int updateCounts(@Param("graphId") String graphId,
                     @Param("nodeCount") Long nodeCount,
                     @Param("edgeCount") Long edgeCount);
}
