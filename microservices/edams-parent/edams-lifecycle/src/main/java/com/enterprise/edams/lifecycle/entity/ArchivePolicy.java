package com.enterprise.edams.lifecycle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 归档策略实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("lc_archive_policy")
public class ArchivePolicy {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 策略名称
     */
    private String name;

    /**
     * 策略编码
     */
    private String code;

    /**
     * 策略描述
     */
    private String description;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 数据分类
     */
    private String dataCategory;

    /**
     * 保留期限（天）
     */
    private Integer retentionDays;

    /**
     * 归档触发条件：1-时间触发，2-容量触发，3-手动触发
     */
    private Integer triggerType;

    /**
     * 归档触发条件JSON（如：{"days": 90}）
     */
    private String triggerCondition;

    /**
     * 归档目标：1-对象存储，2-文件系统，3-磁带库
     */
    private Integer archiveTarget;

    /**
     * 归档目标配置JSON
     */
    private String targetConfig;

    /**
     * 压缩方式：0-不压缩，1-GZIP，2-ZIP，3-7Z
     */
    private Integer compressionType;

    /**
     * 加密方式：0-不加密，1-AES256
     */
    private Integer encryptionType;

    /**
     * 是否删除源数据
     */
    private Boolean deleteSource;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecuteTime;

    /**
     * 执行次数
     */
    private Integer executeCount;

    /**
     * 成功次数
     */
    private Integer successCount;

    /**
     * 失败次数
     */
    private Integer failCount;

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
