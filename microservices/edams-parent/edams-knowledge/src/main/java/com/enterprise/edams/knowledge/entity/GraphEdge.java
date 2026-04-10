package com.enterprise.edams.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图谱边/关系实体 - 存储在Neo4j中
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_graph_edge")
public class GraphEdge extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 边唯一标识
     */
    private String edgeId;

    /**
     * 源节点ID
     */
    private String sourceNodeId;

    /**
     * 目标节点ID
     */
    private String targetNodeId;

    /**
     * 关系类型
     */
    private String relationType;

    /**
     * 所属图谱ID
     */
    private String graphId;

    /**
     * 属性 (JSON格式)
     */
    private String properties;

    /**
     * 权重
     */
    private Double weight;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 来源记录ID
     */
    private String sourceRecordId;

    /**
     * 数据质量评分
     */
    private Double qualityScore;

    /**
     * 状态: ACTIVE-活跃, INACTIVE-不活跃, DELETED-已删除
     */
    private String status;

    /**
     * 所属租户ID
     */
    private String tenantId;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 起始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 备注
     */
    private String remark;
}
