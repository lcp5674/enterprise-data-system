package com.enterprise.edams.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 数据模型实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("data_model")
public class DataModel extends BaseEntity {

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型编码
     */
    private String code;

    /**
     * 模型类型：CONCEPTUAL/LOGICAL/PHYSICAL
     */
    private String modelType;

    /**
     * 模型层级：DOMAIN/SUBJECT/TEMPLATE/DATAMART
     */
    private String level;

    /**
     * 所属主题
     */
    private String subject;

    /**
     * 所属域
     */
    private String domain;

    /**
     * 描述
     */
    private String description;

    /**
     * 模型状态：DRAFT/PUBLISHED/DEPRECATED/ARCHIVED
     */
    private String status;

    /**
     * 模型版本号
     */
    private String version;

    /**
     * 模型内容(JSON)
     */
    private String content;

    /**
     * 模型图数据(JSON)
     */
    private String diagram;

    /**
     * 父模型ID
     */
    private Long parentId;

    /**
     * 模型分类
     */
    private String category;

    /**
     * 标签(JSON)
     */
    private String tags;

    /**
     * 引用次数
     */
    private Integer referenceCount;
}
