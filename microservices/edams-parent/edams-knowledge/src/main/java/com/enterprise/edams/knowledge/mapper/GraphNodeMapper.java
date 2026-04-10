package com.enterprise.edams.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.knowledge.entity.GraphNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 图谱节点Mapper接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Mapper
public interface GraphNodeMapper extends BaseMapper<GraphNode> {

    /**
     * 根据节点ID查询
     *
     * @param nodeId 节点ID
     * @return 图谱节点
     */
    GraphNode selectByNodeId(@Param("nodeId") String nodeId);

    /**
     * 根据图谱ID查询节点列表
     *
     * @param graphId 图谱ID
     * @return 节点列表
     */
    List<GraphNode> selectByGraphId(@Param("graphId") String graphId);

    /**
     * 根据节点类型查询
     *
     * @param graphId 图谱ID
     * @param nodeType 节点类型
     * @return 节点列表
     */
    List<GraphNode> selectByNodeType(@Param("graphId") String graphId,
                                     @Param("nodeType") String nodeType);

    /**
     * 统计图谱节点数量
     *
     * @param graphId 图谱ID
     * @return 节点数量
     */
    Long countByGraphId(@Param("graphId") String graphId);

    /**
     * 根据名称模糊查询
     *
     * @param graphId 图谱ID
     * @param name 名称
     * @return 节点列表
     */
    List<GraphNode> selectByNameLike(@Param("graphId") String graphId,
                                      @Param("name") String name);
}
