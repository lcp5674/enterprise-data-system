package com.enterprise.edams.knowledge.dto;

import lombok.Data;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 图谱边DTO
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@Builder
public class GraphEdgeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 边ID
     */
    private String edgeId;

    /**
     * 源节点ID
     */
    private String sourceNodeId;

    /**
     * 源节点名称
     */
    private String sourceNodeName;

    /**
     * 目标节点ID
     */
    private String targetNodeId;

    /**
     * 目标节点名称
     */
    private String targetNodeName;

    /**
     * 关系类型
     */
    private String relationType;

    /**
     * 所属图谱ID
     */
    private String graphId;

    /**
     * 属性
     */
    private Map<String, Object> properties;

    /**
     * 权重
     */
    private Double weight;

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
     * 起始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 备注
     */
    private String remark;
}
