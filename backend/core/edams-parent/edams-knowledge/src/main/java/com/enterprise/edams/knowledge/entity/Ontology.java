package com.enterprise.edams.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 本体论实体
 * 表示一个完整的知识图谱本体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_ontology")
public class Ontology extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 本体论ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 本体论名称
     */
    private String name;

    /**
     * 本体论描述
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
     * 根节点数量
     */
    private Integer rootClassCount;

    /**
     * 总类数量
     */
    private Integer totalClassCount;

    /**
     * 总实体数量
     */
    private Long totalEntityCount;

    /**
     * 总关系数量
     */
    private Long totalRelationCount;

    /**
     * 状态: DRAFT-草稿, PUBLISHED-已发布, DEPRECATED-已废弃
     */
    private String status;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 最后修改者
     */
    private String modifier;
}
