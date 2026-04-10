package com.enterprise.dataplatform.governance.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 治理任务实体
 * 定义具体的治理执行任务
 */
@Entity
@Table(name = "governance_task", indexes = {
    @Index(name = "idx_task_code", columnList = "taskCode"),
    @Index(name = "idx_task_status", columnList = "taskStatus"),
    @Index(name = "idx_task_type", columnList = "taskType")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceTask {

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
     * 关联的策略ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private GovernancePolicy policy;

    /**
     * 任务类型：ORCHESTRATION、AUTO_REMEDIATION、NOTIFICATION、REPORTING
     */
    @Column(name = "task_type", nullable = false, length = 32)
    private String taskType;

    /**
     * 任务分类
     */
    @Column(name = "task_category", length = 64)
    private String taskCategory;

    /**
     * 执行类型：IMMEDIATE、SCHEDULED、EVENT_TRIGGERED
     */
    @Column(name = "execution_type", length = 32)
    private String executionType;

    /**
     * 任务配置（JSON格式）
     */
    @Column(name = "task_config", columnDefinition = "JSONB")
    private String taskConfig;

    /**
     * 任务参数（JSON格式）
     */
    @Column(name = "task_params", columnDefinition = "JSONB")
    private String taskParams;

    /**
     * 任务状态：PENDING、WAITING、RUNNING、COMPLETED、FAILED、PAUSED、CANCELLED
     */
    @Column(name = "task_status", nullable = false, length = 32)
    private String taskStatus;

    /**
     * DAG依赖关系（JSON格式）
     * 定义任务的前置依赖
     */
    @Column(name = "dag_dependencies", columnDefinition = "JSONB")
    private String dagDependencies;

    /**
     * 任务执行顺序
     */
    @Column(name = "execution_order")
    private Integer executionOrder;

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
     * 下游任务列表
     */
    @ManyToMany(mappedBy = "upstreamTasks")
    @Builder.Default
    private List<GovernanceTask> downstreamTasks = new ArrayList<>();

    /**
     * 上游任务列表
     */
    @ManyToMany
    @JoinTable(
        name = "task_dependency",
        joinColumns = @JoinColumn(name = "downstream_task_id"),
        inverseJoinColumns = @JoinColumn(name = "upstream_task_id")
    )
    @Builder.Default
    private List<GovernanceTask> upstreamTasks = new ArrayList<>();

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
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 开始执行时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束执行时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    /**
     * 执行人
     */
    @Column(name = "executor", length = 64)
    private String executor;

    /**
     * 执行结果
     */
    @Column(name = "execution_result", columnDefinition = "TEXT")
    private String executionResult;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
