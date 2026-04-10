package com.enterprise.edams.lifecycle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 清理任务实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("lc_cleanup_task")
public class CleanupTask {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 数据表名
     */
    private String tableName;

    /**
     * 清理条件SQL
     */
    private String cleanupCondition;

    /**
     * 清理方式：1-物理删除，2-逻辑删除，3-归档后删除
     */
    private Integer cleanupType;

    /**
     * 执行周期（cron表达式）
     */
    private String cronExpression;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 执行超时时间（分钟）
     */
    private Integer timeoutMinutes;

    /**
     * 批量处理大小
     */
    private Integer batchSize;

    /**
     * 清理前是否备份
     */
    private Boolean backupBeforeDelete;

    /**
     * 备份存储路径
     */
    private String backupPath;

    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecuteTime;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;

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
     * 清理记录总数
     */
    private Long totalCleanedCount;

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
