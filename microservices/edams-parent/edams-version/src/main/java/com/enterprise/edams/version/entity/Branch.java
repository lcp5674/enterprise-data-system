package com.enterprise.edams.version.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 分支实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ver_branch")
public class Branch {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 分支名称
     */
    private String branchName;

    /**
     * 分支描述
     */
    private String description;

    /**
     * 基于版本号
     */
    private Integer baseVersion;

    /**
     * 当前版本号
     */
    private Integer currentVersion;

    /**
     * 分支状态：0-开发中，1-已合并，2-已废弃
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private String createdBy;

    /**
     * 创建人名称
     */
    private String createdByName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

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
