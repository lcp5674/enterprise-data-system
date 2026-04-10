package com.enterprise.edams.datasource.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 数据源查询条件DTO
 */
@Data
@SuperBuilder
public class DatasourceQueryDTO {

    /**
     * 数据源名称（模糊匹配）
     */
    private String name;

    /**
     * 数据源编码（精确匹配）
     */
    private String code;

    /**
     * 数据源类型
     */
    private String datasourceType;

    /**
     * 状态
     */
    private String status;

    /**
     * 健康状态
     */
    private String healthStatus;

    /**
     * 目录名称
     */
    private String catalogName;

    /**
     * 标签
     */
    private String tag;

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
