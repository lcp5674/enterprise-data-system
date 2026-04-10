package com.enterprise.edams.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
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
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建人
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField("created_time")
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
    private LocalDateTime updatedTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted = 0;

    /**
     * 乐观锁版本
     */
    @Version
    @TableField("version")
    private Integer version = 0;
}
