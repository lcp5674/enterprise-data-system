package com.enterprise.edams.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 图谱节点实体 - 存储在Neo4j中
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_graph_node")
public class GraphNode extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点唯一标识
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
     * 所属本体ID
     */
    private String ontologyId;

    /**
     * 属性 (JSON格式)
     */
    private String properties;

    /**
     * 标签列表 (逗号分隔)
     */
    private String labels;

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
     * 状态: ACTIVE-活跃, INACTIVE-不活跃, MERGED-已合并, DELETED-已删除
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
     * 首次出现时间
     */
    private LocalDateTime firstSeenAt;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdatedAt;

    /**
     * 备注
     */
    private String remark;
}
