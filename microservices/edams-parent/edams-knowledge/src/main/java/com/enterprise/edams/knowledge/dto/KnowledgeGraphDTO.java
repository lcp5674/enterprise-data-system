package com.enterprise.edams.knowledge.dto;

import lombok.Data;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 知识图谱DTO
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@Builder
public class KnowledgeGraphDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图谱ID
     */
    private String graphId;

    /**
     * 图谱名称
     */
    private String name;

    /**
     * 图谱描述
     */
    private String description;

    /**
     * 图谱类型
     */
    private String graphType;

    /**
     * 状态
     */
    private String status;

    /**
     * 节点数量
     */
    private Long nodeCount;

    /**
     * 边数量
     */
    private Long edgeCount;

    /**
     * 可见范围
     */
    private String visibility;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 图谱统计信息
     */
    private GraphStatistics statistics;

    /**
     * 节点类型分布
     */
    private Map<String, Long> nodeTypeDistribution;

    /**
     * 关系类型分布
     */
    private Map<String, Long> relationTypeDistribution;

    /**
     * 图谱配置
     */
    private Map<String, Object> config;

    /**
     * 图谱统计信息
     */
    @Data
    @Builder
    public static class GraphStatistics implements Serializable {
        private Long totalNodes;
        private Long totalEdges;
        private Long activeNodes;
        private Long activeEdges;
        private Double avgDegree;
        private Long connectedComponents;
        private Double density;
    }
}
