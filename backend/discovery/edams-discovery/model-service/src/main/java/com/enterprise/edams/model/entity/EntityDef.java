package com.enterprise.edams.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 实体定义实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("model_entity_def")
public class EntityDef extends BaseEntity {

    /**
     * 所属模型ID
     */
    private Long modelId;

    /**
     * 实体名称
     */
    private String name;

    /**
     * 实体编码
     */
    private String code;

    /**
     * 实体类型：TABLE/VIEW
     */
    private String entityType;

    /**
     * 中文名称
     */
    private String chineseName;

    /**
     * 描述
     */
    private String description;

    /**
     * 所属数据库
     */
    private String databaseName;

    /**
     * 所属模式
     */
    private String schemaName;

    /**
     * 实体定义(JSON)
     */
    private String definition;

    /**
     * 实体属性(JSON)
     */
    private String properties;
}
