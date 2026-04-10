package com.enterprise.edams.model.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * 更新数据模型请求DTO
 */
@Data
@SuperBuilder
public class UpdateModelRequest {

    /**
     * 模型名称
     */
    private String name;

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
     * 描述
     */
    private String description;

    /**
     * 模型状态
     */
    private String status;

    /**
     * 模型分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 模型内容
     */
    private Map<String, Object> content;

    /**
     * 模型图数据
     */
    private Map<String, Object> diagram;
}
