package com.enterprise.edams.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 关系类型定义实体
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_relation_type")
public class RelationType extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 类型唯一标识
     */
    private String typeId;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 类型描述
     */
    private String description;

    /**
     * 关系方向: UNDIRECTED-无向, DIRECTED-有向
     */
    private String direction;

    /**
     * 源实体类型约束
     */
    private String sourceTypeConstraint;

    /**
     * 目标实体类型约束
     */
    private String targetTypeConstraint;

    /**
     * 所属本体ID
     */
    private String ontologyId;

    /**
     * 属性定义 (JSON格式)
     */
    private String attributes;

    /**
     * 是否可逆
     */
    private Boolean reversible;

    /**
     * 逆关系类型ID
     */
    private String inverseRelationTypeId;

    /**
     * 基数约束 (如 1:N, N:M)
     */
    private String cardinality;

    /**
     * 状态: ACTIVE-激活, INACTIVE-未激活
     */
    private String status;

    /**
     * 所属租户ID
     */
    private String tenantId;

    /**
     * 显示顺序
     */
    private Integer sortOrder;
}
