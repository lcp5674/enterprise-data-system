package com.enterprise.edams.knowledge.dto;

import lombok.Data;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 图谱节点DTO
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@Builder
public class GraphNodeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点类型
     */
    private String nodeType;

    /**
     * 所属图谱ID
     */
    private String graphId;

    /**
     * 标签列表
     */
    private List<String> labels;

    /**
     * 属性
     */
    private Map<String, Object> properties;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 数据质量评分
     */
    private Double qualityScore;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdatedAt;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 备注
     */
    private String remark;

    /**
     * 关联的边
     */
    private List<GraphEdgeDTO> edges;

    /**
     * 邻居节点
     */
    private List<GraphNodeDTO> neighbors;

    /**
     * 路径信息
     */
    private List<String> path;
}
