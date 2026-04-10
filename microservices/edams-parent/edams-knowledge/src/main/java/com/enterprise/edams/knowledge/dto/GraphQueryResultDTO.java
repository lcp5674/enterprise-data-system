package com.enterprise.edams.knowledge.dto;

import lombok.Data;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 图谱查询结果DTO
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@Builder
public class GraphQueryResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 查询类型
     */
    private String queryType;

    /**
     * 查询耗时(ms)
     */
    private Long queryTime;

    /**
     * 节点列表
     */
    private List<GraphNodeDTO> nodes;

    /**
     * 边列表
     */
    private List<GraphEdgeDTO> edges;

    /**
     * 路径列表
     */
    private List<PathDTO> paths;

    /**
     * 子图列表
     */
    private List<SubgraphDTO> subgraphs;

    /**
     * 统计信息
     */
    private StatisticsDTO statistics;

    /**
     * 总数
     */
    private Long totalCount;

    /**
     * 是否有更多结果
     */
    private Boolean hasMore;

    /**
     * 路径DTO
     */
    @Data
    @Builder
    public static class PathDTO implements Serializable {
        private String pathId;
        private Double totalWeight;
        private Integer length;
        private List<GraphNodeDTO> nodes;
        private List<GraphEdgeDTO> edges;
    }

    /**
     * 子图DTO
     */
    @Data
    @Builder
    public static class SubgraphDTO implements Serializable {
        private String subgraphId;
        private String subgraphName;
        private List<GraphNodeDTO> nodes;
        private List<GraphEdgeDTO> edges;
        private Long nodeCount;
        private Long edgeCount;
    }

    /**
     * 统计信息DTO
     */
    @Data
    @Builder
    public static class StatisticsDTO implements Serializable {
        private Long totalNodes;
        private Long totalEdges;
        private Long totalPaths;
        private Map<String, Long> nodeTypeCounts;
        private Map<String, Long> relationTypeCounts;
        private Double avgDegree;
        private Double density;
    }
}
