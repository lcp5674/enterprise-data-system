package com.enterprise.edams.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 实体类型定义实体
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_entity_type")
public class EntityType extends BaseEntity implements Serializable {

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
     * 所属本体ID
     */
    private String ontologyId;

    /**
     * 父类型ID
     */
    private String parentTypeId;

    /**
     * 属性定义 (JSON格式)
     */
    private String attributes;

    /**
     * 验证规则 (JSON格式)
     */
    private String validationRules;

    /**
     * 图标
     */
    private String icon;

    /**
     * 颜色
     */
    private String color;

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
