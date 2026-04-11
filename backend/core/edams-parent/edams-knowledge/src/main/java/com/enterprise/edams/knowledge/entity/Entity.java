package com.enterprise.edams.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体实体
 * 表示知识图谱中的具体实例/实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_entity")
public class Entity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实体ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属本体论ID
     */
    private Long ontologyId;

    /**
     * 所属类ID
     */
    private Long classId;

    /**
     * 实体名称
     */
    private String name;

    /**
     * 实体唯一标识
     */
    private String uniqueId;

    /**
     * 别名/同义词
     */
    private String alias;

    /**
     * 描述
     */
    private String description;

    /**
     * 实体类型: CONCEPT-概念, OBJECT-对象, EVENT-事件
     */
    private String entityType;

    /**
     * 标签
     */
    private String tags;

    /**
     * 属性 (JSON格式)
     */
    private String properties;

    /**
     * 扩展属性 (JSON格式)
     */
    private String extraProperties;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 置信度 (0-100)
     */
    private Integer confidence;

    /**
     * 来源
     */
    private String source;

    /**
     * 状态: ACTIVE-活跃, INACTIVE-不活跃, DELETED-已删除
     */
    private String status;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 访问次数
     */
    private Long viewCount;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 创建部门
     */
    private String department;

    /**
     * 关联资产ID
     */
    private Long assetId;

    /**
     * 关联资产类型
     */
    private String assetType;
}
