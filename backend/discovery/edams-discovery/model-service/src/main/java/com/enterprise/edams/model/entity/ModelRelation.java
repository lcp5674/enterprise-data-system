package com.enterprise.edams.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 模型关系实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("model_relation")
public class ModelRelation extends BaseEntity {

    /**
     * 源实体ID
     */
    private Long sourceEntityId;

    /**
     * 源属性ID
     */
    private Long sourceAttributeId;

    /**
     * 目标实体ID
     */
    private Long targetEntityId;

    /**
     * 目标属性ID
     */
    private Long targetAttributeId;

    /**
     * 关系类型：ONE_TO_ONE/ONE_TO_MANY/MANY_TO_ONE/MANY_TO_MANY
     */
    private String relationType;

    /**
     * 关系名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 源实体角色
     */
    private String sourceRole;

    /**
     * 目标实体角色
     */
    private String targetRole;
}
