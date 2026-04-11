package com.enterprise.edams.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 本体类实体
 * 表示知识图谱中的类/概念
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_ontology_class")
public class OntologyClass extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 类ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属本体论ID
     */
    private Long ontologyId;

    /**
     * 父类ID (NULL表示根节点)
     */
    private Long parentClassId;

    /**
     * 类名称
     */
    private String className;

    /**
     * 类中文名称
     */
    private String classNameZh;

    /**
     * 类描述
     */
    private String description;

    /**
     * 图标
     */
    private String icon;

    /**
     * 颜色
     */
    private String color;

    /**
     * 层级深度
     */
    private Integer level;

    /**
     * 是否为叶子节点
     */
    private Boolean isLeaf;

    /**
     * 子类数量
     */
    private Integer subclassCount;

    /**
     * 实例数量
     */
    private Integer instanceCount;

    /**
     * 属性定义 (JSON格式)
     */
    private String properties;

    /**
     * 显示顺序
     */
    private Integer sortOrder;

    /**
     * 创建者
     */
    private String creator;
}
