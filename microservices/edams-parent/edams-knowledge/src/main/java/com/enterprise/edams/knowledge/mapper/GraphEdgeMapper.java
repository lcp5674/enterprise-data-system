package com.enterprise.edams.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.knowledge.entity.GraphEdge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 图谱边Mapper接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Mapper
public interface GraphEdgeMapper extends BaseMapper<GraphEdge> {

    /**
     * 根据边ID查询
     *
     * @param edgeId 边ID
     * @return 图谱边
     */
    GraphEdge selectByEdgeId(@Param("edgeId") String edgeId);

    /**
     * 根据图谱ID查询边列表
     *
     * @param graphId 图谱ID
     * @return 边列表
     */
    List<GraphEdge> selectByGraphId(@Param("graphId") String graphId);

    /**
     * 根据源节点ID查询
     *
     * @param sourceNodeId 源节点ID
     * @return 边列表
     */
    List<GraphEdge> selectBySourceNodeId(@Param("sourceNodeId") String sourceNodeId);

    /**
     * 根据目标节点ID查询
     *
     * @param targetNodeId 目标节点ID
     * @return 边列表
     */
    List<GraphEdge> selectByTargetNodeId(@Param("targetNodeId") String targetNodeId);

    /**
     * 根据关系类型查询
     *
     * @param graphId 图谱ID
     * @param relationType 关系类型
     * @return 边列表
     */
    List<GraphEdge> selectByRelationType(@Param("graphId") String graphId,
                                          @Param("relationType") String relationType);

    /**
     * 统计图谱边数量
     *
     * @param graphId 图谱ID
     * @return 边数量
     */
    Long countByGraphId(@Param("graphId") String graphId);
}
