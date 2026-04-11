package com.enterprise.edams.model.vo;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 实体详情VO
 */
@Data
@SuperBuilder
public class EntityDetailVO {

    /**
     * 实体ID
     */
    private Long id;

    /**
     * 实体名称
     */
    private String name;

    /**
     * 实体编码
     */
    private String code;

    /**
     * 实体类型
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
     * 属性列表
     */
    private List<AttributeVO> attributes;
}
