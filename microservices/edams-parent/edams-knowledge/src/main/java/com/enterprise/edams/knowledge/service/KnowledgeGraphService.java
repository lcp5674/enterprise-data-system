package com.enterprise.edams.knowledge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.knowledge.dto.KnowledgeGraphDTO;
import com.enterprise.edams.knowledge.entity.KnowledgeGraph;

import java.util.List;

/**
 * 知识图谱服务接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
public interface KnowledgeGraphService extends IService<KnowledgeGraph> {

    /**
     * 创建知识图谱
     *
     * @param dto 图谱信息
     * @return 创建的图谱
     */
    KnowledgeGraph createGraph(KnowledgeGraphDTO dto);

    /**
     * 更新知识图谱
     *
     * @param graphId 图谱ID
     * @param dto 图谱信息
     * @return 更新后的图谱
     */
    KnowledgeGraph updateGraph(String graphId, KnowledgeGraphDTO dto);

    /**
     * 删除知识图谱
     *
     * @param graphId 图谱ID
     */
    void deleteGraph(String graphId);

    /**
     * 获取图谱详情
     *
     * @param graphId 图谱ID
     * @return 图谱详情
     */
    KnowledgeGraphDTO getGraphDetail(String graphId);

    /**
     * 获取图谱列表
     *
     * @param tenantId 租户ID
     * @param keyword 关键词
     * @param graphType 图谱类型
     * @param status 状态
     * @return 图谱列表
     */
    List<KnowledgeGraphDTO> listGraphs(String tenantId, String keyword, String graphType, String status);

    /**
     * 激活图谱
     *
     * @param graphId 图谱ID
     */
    void activateGraph(String graphId);

    /**
     * 归档图谱
     *
     * @param graphId 图谱ID
     */
    void archiveGraph(String graphId);

    /**
     * 获取图谱统计信息
     *
     * @param graphId 图谱ID
     * @return 统计信息
     */
    KnowledgeGraphDTO.GraphStatistics getGraphStatistics(String graphId);

    /**
     * 更新图谱节点和边数量
     *
     * @param graphId 图谱ID
     */
    void updateGraphCounts(String graphId);
}
