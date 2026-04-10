package com.enterprise.edams.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据模型VO
 */
@Data
@SuperBuilder
public class ModelVO {

    /**
     * 模型ID
     */
    private Long id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型编码
     */
    private String code;

    /**
     * 模型类型
     */
    private String modelType;

    /**
     * 模型类型描述
     */
    private String modelTypeDesc;

    /**
     * 模型层级
     */
    private String level;

    /**
     * 模型层级描述
     */
    private String levelDesc;

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
     * 模型状态描述
     */
    private String statusDesc;

    /**
     * 模型版本号
     */
    private String version;

    /**
     * 模型分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 实体数量
     */
    private Integer entityCount;

    /**
     * 引用次数
     */
    private Integer referenceCount;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
