package com.enterprise.edams.model.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 数据模型查询DTO
 */
@Data
@SuperBuilder
public class ModelQueryDTO {

    /**
     * 模型名称（模糊匹配）
     */
    private String name;

    /**
     * 模型编码（精确匹配）
     */
    private String code;

    /**
     * 模型类型
     */
    private String modelType;

    /**
     * 模型层级
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
     * 模型状态
     */
    private String status;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
