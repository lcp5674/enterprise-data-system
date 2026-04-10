package com.enterprise.edams.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 属性定义实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("model_attribute_def")
public class AttributeDef extends BaseEntity {

    /**
     * 所属实体ID
     */
    private Long entityId;

    /**
     * 属性名称
     */
    private String name;

    /**
     * 属性编码
     */
    private String code;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 中文名称
     */
    private String chineseName;

    /**
     * 描述
     */
    private String description;

    /**
     * 字段长度
     */
    private Integer length;

    /**
     * 精度
     */
    private Integer precision;

    /**
     * 是否主键
     */
    private Integer isPrimaryKey;

    /**
     * 是否外键
     */
    private Integer isForeignKey;

    /**
     * 是否必填
     */
    private Integer isRequired;

    /**
     * 是否可为空
     */
    private Integer isNullable;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 引用外键表
     */
    private String referenceTable;

    /**
     * 引用外键字段
     */
    private String referenceColumn;

    /**
     * 属性序号
     */
    private Integer ordinal;

    /**
     * 属性定义(JSON)
     */
    private String definition;
}
