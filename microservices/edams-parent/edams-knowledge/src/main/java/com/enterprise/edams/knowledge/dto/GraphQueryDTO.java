package com.enterprise.edams.knowledge.dto;

import lombok.Data;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 图谱查询请求DTO
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@Builder
public class GraphQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图谱ID
     */
    @NotBlank(message = "图谱ID不能为空")
    private String graphId;

    /**
     * 查询类型: NODE-节点查询, EDGE-边查询, PATH-路径查询, SUBGRAPH-子图查询
     */
    @NotBlank(message = "查询类型不能为空")
    private String queryType;

    /**
     * 起始节点ID (路径/子图查询时使用)
     */
    private String startNodeId;

    /**
     * 目标节点ID (路径查询时使用)
     */
    private String targetNodeId;

    /**
     * 节点ID列表 (批量查询时使用)
     */
    private List<String> nodeIds;

    /**
     * 节点名称 (按名称查询)
     */
    private String nodeName;

    /**
     * 节点类型
     */
    private String nodeType;

    /**
     * 关系类型
     */
    private String relationType;

    /**
     * 查询深度 (路径/子图查询)
     */
    @Builder.Default
    private Integer depth = 1;

    /**
     * 最大返回结果数
     */
    @Builder.Default
    private Integer limit = 100;

    /**
     * 偏移量
     */
    @Builder.Default
    private Integer offset = 0;

    /**
     * 属性过滤条件
     */
    private Map<String, Object> propertyFilters;

    /**
     * 是否包含属性
     */
    @Builder.Default
    private Boolean includeProperties = true;

    /**
     * 是否包含统计信息
     */
    @Builder.Default
    private Boolean includeStatistics = false;

    /**
     * Cypher查询语句 (高级查询时使用)
     */
    private String cypher;

    /**
     * 查询参数
     */
    private Map<String, Object> queryParams;
}
