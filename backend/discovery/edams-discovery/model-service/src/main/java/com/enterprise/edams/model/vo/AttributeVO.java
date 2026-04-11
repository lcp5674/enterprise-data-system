package com.enterprise.edams.model.vo;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 属性VO
 */
@Data
@SuperBuilder
public class AttributeVO {

    /**
     * 属性ID
     */
    private Long id;

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
    private Boolean isPrimaryKey;

    /**
     * 是否外键
     */
    private Boolean isForeignKey;

    /**
     * 是否必填
     */
    private Boolean isRequired;

    /**
     * 是否可为空
     */
    private Boolean isNullable;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 序号
     */
    private Integer ordinal;
}
