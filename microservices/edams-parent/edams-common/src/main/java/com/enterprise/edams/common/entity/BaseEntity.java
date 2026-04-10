package com.enterprise.edams.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 包含通用字段：主键、创建人、创建时间、更新人、更新时间、逻辑删除、版本号
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建人
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField("created_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 更新时间
     */
    @TableField("updated_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 乐观锁版本
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 删除人
     */
    @TableField("deleted_by")
    private String deletedBy;

    /**
     * 删除时间
     */
    @TableField("deleted_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime deletedTime;

    /**
     * 保存前执行
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdTime == null) {
            this.createdTime = now;
        }
        if (this.updatedTime == null) {
            this.updatedTime = now;
        }
        if (this.createdBy == null || this.createdBy.isEmpty()) {
            this.createdBy = getCurrentUsername();
        }
        if (this.updatedBy == null || this.updatedBy.isEmpty()) {
            this.updatedBy = getCurrentUsername();
        }
        if (this.isDeleted == null) {
            this.isDeleted = 0;
        }
    }

    /**
     * 更新前执行
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedTime = LocalDateTime.now();
        this.updatedBy = getCurrentUsername();
    }

    /**
     * 获取当前用户名
     */
    protected String getCurrentUsername() {
        try {
            return org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
