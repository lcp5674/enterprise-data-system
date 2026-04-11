package com.enterprise.edams.aiops.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 运维任务实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("ops_task")
public class OpsTask extends BaseEntity {

    /**
     * 任务标题
     */
    @TableField("task_title")
    private String taskTitle;

    /**
     * 任务描述
     */
    @TableField("task_description")
    private String description;

    /**
     * 任务类型：maintenance, deployment, backup, recovery, scaling, inspection
     */
    @TableField("task_type")
    private String taskType;

    /**
     * 任务状态：pending, running, completed, failed, cancelled
     */
    @TableField("task_status")
    private String taskStatus;

    /**
     * 优先级：low, medium, high, critical
     */
    @TableField("priority")
    private String priority;

    /**
     * 目标系统ID
     */
    @TableField("target_id")
    private String targetId;

    /**
     * 目标系统名称
     */
    @TableField("target_name")
    private String targetName;

    /**
     * 执行方式：manual, automatic, scheduled
     */
    @TableField("execution_mode")
    private String executionMode;

    /**
     * 执行人
     */
    @TableField("executor")
    private String executor;

    /**
     * 计划开始时间
     */
    @TableField("planned_start_time")
    private LocalDateTime plannedStartTime;

    /**
     * 计划结束时间
     */
    @TableField("planned_end_time")
    private LocalDateTime plannedEndTime;

    /**
     * 实际开始时间
     */
    @TableField("actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    @TableField("actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * 任务参数(JSON)
     */
    @TableField("task_params")
    private String taskParams;

    /**
     * 执行脚本
     */
    @TableField("script")
    private String script;

    /**
     * 执行结果
     */
    @TableField("result")
    private String result;

    /**
     * 输出日志
     */
    @TableField("output_log")
    private String outputLog;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 进度百分比
     */
    @TableField("progress_percent")
    private Integer progressPercent;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @TableField("max_retries")
    private Integer maxRetries;

    /**
     * 关联告警ID
     */
    @TableField("alert_id")
    private Long alertId;

    /**
     * 触发来源：manual, alert, schedule, api
     */
    @TableField("trigger_source")
    private String triggerSource;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 判断任务是否超时
     */
    public boolean isOverdue() {
        if (plannedEndTime == null || "completed".equalsIgnoreCase(taskStatus) || "cancelled".equalsIgnoreCase(taskStatus)) {
            return false;
        }
        return LocalDateTime.now().isAfter(plannedEndTime);
    }
}
