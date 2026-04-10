package com.enterprise.edams.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 本体实体 - 表示知识图谱的本体/概念模型
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_ontology")
public class Ontology extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 本体唯一标识
     */
    private String ontologyId;

    /**
     * 本体名称
     */
    private String name;

    /**
     * 本体描述
     */
    private String description;

    /**
     * 版本号
     */
    private String version;

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 状态: DRAFT-草稿, ACTIVE-激活, DEPRECATED-废弃
     */
    private String status;

    /**
     * 实体类型定义 (JSON格式)
     */
    private String entityTypes;

    /**
     * 关系类型定义 (JSON格式)
     */
    private String relationTypes;

    /**
     * 属性定义 (JSON格式)
     */
    private String properties;

    /**
     * 约束规则 (JSON格式)
     */
    private String constraints;

    /**
     * 所属图谱ID
     */
    private String graphId;

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
}
