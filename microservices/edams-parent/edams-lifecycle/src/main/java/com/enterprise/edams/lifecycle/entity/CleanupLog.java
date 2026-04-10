package com.enterprise.edams.lifecycle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 清理日志实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("lc_cleanup_log")
public class CleanupLog {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 清理任务ID
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 执行状态：0-执行中，1-成功，2-失败，3-超时
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long duration;

    /**
     * 清理记录数
     */
    private Long cleanedCount;

    /**
     * 备份文件路径
     */
    private String backupFilePath;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行详情
     */
    private String executeDetail;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
}
