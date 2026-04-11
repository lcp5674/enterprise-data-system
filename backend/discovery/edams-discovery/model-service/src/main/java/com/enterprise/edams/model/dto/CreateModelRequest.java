package com.enterprise.edams.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * 创建数据模型请求DTO
 */
@Data
@SuperBuilder
public class CreateModelRequest {

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String name;

    /**
     * 模型编码
     */
    @NotBlank(message = "模型编码不能为空")
    private String code;

    /**
     * 模型类型
     */
    @NotBlank(message = "模型类型不能为空")
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
     * 父模型ID
     */
    private Long parentId;

    /**
     * 模型分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 实体定义列表
     */
    private List<EntityDefDTO> entities;

    /**
     * 关系定义列表
     */
    private List<RelationDefDTO> relations;
}
