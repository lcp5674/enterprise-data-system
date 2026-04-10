package com.enterprise.edams.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识图谱实体
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_knowledge_graph")
public class KnowledgeGraph extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图谱唯一标识
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
     * 图谱类型: BUSINESS-业务图谱, TECHNICAL-技术图谱, METADATA-元数据图谱
     */
    private String graphType;

    /**
     * 状态: DRAFT-草稿, ACTIVE-激活, ARCHIVED-归档
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
     * 所属租户ID
     */
    private String tenantId;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 更新人
     */
    private String updater;

    /**
     * 配置信息 (JSON格式)
     */
    private String config;

    /**
     * 可见范围: PRIVATE-私有, TEAM-团队, PUBLIC-公开
     */
    private String visibility;
}
