package com.enterprise.edams.model.vo;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 模型关系VO
 */
@Data
@SuperBuilder
public class RelationVO {

    /**
     * 关系ID
     */
    private Long id;

    /**
     * 源实体ID
     */
    private Long sourceEntityId;

    /**
     * 源实体名称
     */
    private String sourceEntityName;

    /**
     * 源属性ID
     */
    private Long sourceAttributeId;

    /**
     * 源属性名称
     */
    private String sourceAttributeName;

    /**
     * 目标实体ID
     */
    private Long targetEntityId;

    /**
     * 目标实体名称
     */
    private String targetEntityName;

    /**
     * 目标属性ID
     */
    private Long targetAttributeId;

    /**
     * 目标属性名称
     */
    private String targetAttributeName;

    /**
     * 关系类型
     */
    private String relationType;

    /**
     * 关系类型描述
     */
    private String relationTypeDesc;

    /**
     * 关系名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;
}
