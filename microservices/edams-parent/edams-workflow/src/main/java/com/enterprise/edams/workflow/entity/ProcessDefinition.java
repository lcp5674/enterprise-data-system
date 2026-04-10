package com.enterprise.edams.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程定义实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("wf_process_definition")
public class ProcessDefinition {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 流程定义Key
     */
    private String processKey;

    /**
     * 流程定义名称
     */
    private String name;

    /**
     * 流程定义描述
     */
    private String description;

    /**
     * 流程分类
     */
    private String category;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * BPMN XML内容
     */
    @TableField(typeHandler = org.apache.ibatis.type.BlobTypeHandler.class)
    private String bpmnXml;

    /**
     * 流程图片
     */
    @TableField(typeHandler = org.apache.ibatis.type.BlobTypeHandler.class)
    private String diagramSvg;

    /**
     * 状态：0-草稿，1-已发布，2-已停用
     */
    private Integer status;

    /**
     * 是否最新版本
     */
    private Boolean isLatest;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
}
