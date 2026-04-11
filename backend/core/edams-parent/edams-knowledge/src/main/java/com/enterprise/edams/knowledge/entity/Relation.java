package com.enterprise.edams.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 关系实体
 * 表示知识图谱中实体之间的关系
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_relation")
public class Relation extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关系ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属本体论ID
     */
    private Long ontologyId;

    /**
     * 源实体ID
     */
    private Long sourceEntityId;

    /**
     * 源实体名称
     */
    private String sourceEntityName;

    /**
     * 目标实体ID
     */
    private Long targetEntityId;

    /**
     * 目标实体名称
     */
    private String targetEntityName;

    /**
     * 关系类型
     */
    private String relationType;

    /**
     * 关系名称
     */
    private String relationName;

    /**
     * 关系描述
     */
    private String description;

    /**
     * 方向: DIRECT-正向, REVERSE-反向
     */
    private String direction;

    /**
     * 属性 (JSON格式)
     */
    private String properties;

    /**
     * 权重
     */
    private Double weight;

    /**
     * 置信度 (0-100)
     */
    private Integer confidence;

    /**
     * 证据/来源
     */
    private String evidence;

    /**
     * 是否为推理关系
     */
    private Boolean isInferred;

    /**
     * 状态: ACTIVE-活跃, INACTIVE-不活跃, DELETED-已删除
     */
    private String status;

    /**
     * 创建者
     */
    private String creator;
}
