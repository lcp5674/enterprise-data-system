package com.enterprise.dataplatform.governance.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 任务执行记录实体
 * 记录任务的执行历史
 */
@Entity
@Table(name = "task_execution", indexes = {
    @Index(name = "idx_execution_task", columnList = "task_id"),
    @Index(name = "idx_execution_status", columnList = "execution_status"),
    @Index(name = "idx_execution_time", columnList = "start_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 执行批次号
     */
    @Column(name = "batch_no", nullable = false, length = 64)
    private String batchNo;

    /**
     * 关联的任务ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private GovernanceTask task;

    /**
     * 任务编码
     */
    @Column(name = "task_code", nullable = false, length = 64)
    private String taskCode;

    /**
     * 任务名称
     */
    @Column(name = "task_name", length = 128)
    private String taskName;

    /**
     * 任务类型
     */
    @Column(name = "task_type", length = 32)
    private String taskType;

    /**
     * 执行状态：PENDING、RUNNING、COMPLETED、FAILED、SKIPPED
     */
    @Column(name = "execution_status", nullable = false, length = 32)
    private String executionStatus;

    /**
     * 执行结果：SUCCESS、PARTIAL_SUCCESS、FAILED
     */
    @Column(name = "result_status", length = 32)
    private String resultStatus;

    /**
     * 开始时间
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    /**
     * 执行参数（JSON格式）
     */
    @Column(name = "execution_params", columnDefinition = "JSONB")
    private String executionParams;

    /**
     * 执行结果详情（JSON格式）
     */
    @Column(name = "result_details", columnDefinition = "TEXT")
    private String resultDetails;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 执行日志
     */
    @Column(name = "execution_logs", columnDefinition = "TEXT")
    private String executionLogs;

    /**
     * 执行人
     */
    @Column(name = "executor", length = 64)
    private String executor;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 备注
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
