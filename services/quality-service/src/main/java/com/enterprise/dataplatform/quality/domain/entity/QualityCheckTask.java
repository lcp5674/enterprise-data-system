package com.enterprise.dataplatform.quality.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 质量检查任务实体
 * 定义质量检查的执行任务
 */
@Entity
@Table(name = "quality_check_task", indexes = {
    @Index(name = "idx_task_code", columnList = "taskCode"),
    @Index(name = "idx_task_status", columnList = "taskStatus"),
    @Index(name = "idx_task_schedule", columnList = "scheduleType")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityCheckTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务编码
     */
    @Column(name = "task_code", nullable = false, unique = true, length = 64)
    private String taskCode;

    /**
     * 任务名称
     */
    @Column(name = "task_name", nullable = false, length = 128)
    private String taskName;

    /**
     * 任务描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 关联的质量规则ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private QualityRule rule;

    /**
     * 关联的数据资产ID
     */
    @Column(name = "asset_id", nullable = false, length = 64)
    private String assetId;

    /**
     * 数据资产名称
     */
    @Column(name = "asset_name", length = 256)
    private String assetName;

    /**
     * 资产类型
     */
    @Column(name = "asset_type", length = 32)
    private String assetType;

    /**
     * 任务类型：IMMEDIATE、SCHEDULED、EVENT_TRIGGERED
     */
    @Column(name = "task_type", nullable = false, length = 32)
    private String taskType;

    /**
     * 调度类型：ONCE、CRON、INTERVAL
     */
    @Column(name = "schedule_type", length = 32)
    private String scheduleType;

    /**
     * Cron表达式
     */
    @Column(name = "cron_expression", length = 64)
    private String cronExpression;

    /**
     * 执行间隔（秒）
     */
    @Column(name = "interval_seconds")
    private Integer intervalSeconds;

    /**
     * 下次执行时间
     */
    @Column(name = "next_execution_time")
    private LocalDateTime nextExecutionTime;

    /**
     * 任务状态：PENDING、RUNNING、COMPLETED、FAILED、PAUSED
     */
    @Column(name = "task_status", nullable = false, length = 32)
    private String taskStatus;

    /**
     * 并发执行：ALLOW、FORBID
     */
    @Column(name = "concurrent_mode", length = 16)
    private String concurrentMode;

    /**
     * 超时时间（秒）
     */
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    /**
     * 重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount;

    /**
     * 已重试次数
     */
    @Column(name = "retry_attempts")
    private Integer retryAttempts;

    /**
     * 执行参数（JSON格式）
     */
    @Column(name = "execution_params", columnDefinition = "JSONB")
    private String executionParams;

    /**
     * 创建人
     */
    @Column(name = "creator", length = 64)
    private String creator;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @Column(name = "updater", length = 64)
    private String updater;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 最后执行时间
     */
    @Column(name = "last_execution_time")
    private LocalDateTime lastExecutionTime;

    /**
     * 最后执行状态
     */
    @Column(name = "last_execution_status", length = 32)
    private String lastExecutionStatus;
}
