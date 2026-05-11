package com.enterprise.edams.analysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "analysis_task", indexes = {
        @Index(name = "idx_task_status", columnList = "status"),
        @Index(name = "idx_execution_mode", columnList = "execution_mode"),
        @Index(name = "idx_task_created_at", columnList = "created_at"),
        @Index(name = "idx_task_datasource_id", columnList = "datasource_id"),
        @Index(name = "idx_task_cron", columnList = "cron_expression")
})
public class AnalysisTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_code", nullable = false, unique = true, length = 64)
    private String taskCode;

    @Column(name = "task_name", nullable = false, length = 256)
    private String taskName;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    @Column(name = "datasource_id", nullable = false)
    private Long datasourceId;

    @Column(name = "datasource_name", length = 256)
    private String datasourceName;

    @Column(name = "datasource_type", length = 64)
    private String datasourceType;

    @Column(name = "schema_name", length = 128)
    private String schemaName;

    @Column(name = "model_config_id")
    private Long modelConfigId;

    @Column(name = "target_tables", columnDefinition = "TEXT")
    private String targetTables;

    @Column(name = "excluded_tables", columnDefinition = "TEXT")
    private String excludedTables;

    @Column(name = "batch_size")
    @Builder.Default
    private Integer batchSize = 5;

    @Column(name = "enable_lineage_analysis")
    @Builder.Default
    private Boolean enableLineageAnalysis = true;

    @Column(name = "enable_indicator_extraction")
    @Builder.Default
    private Boolean enableIndicatorExtraction = true;

    @Column(name = "enable_subject_classification")
    @Builder.Default
    private Boolean enableSubjectClassification = true;

    @Column(name = "auto_register")
    @Builder.Default
    private Boolean autoRegister = false;

    @Column(name = "sample_row_count")
    @Builder.Default
    private Integer sampleRowCount = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_mode", nullable = false, length = 32)
    @Builder.Default
    private ExecutionMode executionMode = ExecutionMode.MANUAL;

    @Column(name = "cron_expression", length = 64)
    private String cronExpression;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "progress")
    @Builder.Default
    private Integer progress = 0;

    @Column(name = "total_batches")
    @Builder.Default
    private Integer totalBatches = 0;

    @Column(name = "completed_batches")
    @Builder.Default
    private Integer completedBatches = 0;

    @Column(name = "total_tables")
    @Builder.Default
    private Integer totalTables = 0;

    @Column(name = "analyzed_tables")
    @Builder.Default
    private Integer analyzedTables = 0;

    @Column(name = "success_count")
    @Builder.Default
    private Integer successCount = 0;

    @Column(name = "failure_count")
    @Builder.Default
    private Integer failureCount = 0;

    @Column(name = "executor", length = 64)
    private String executor;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;

    @Column(name = "error_stack_trace", columnDefinition = "TEXT")
    private String errorStackTrace;

    @Column(name = "task_config", columnDefinition = "TEXT")
    private String taskConfig;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void incrementProgress() {
        this.progress = Math.min(100, (this.progress == null ? 0 : this.progress) + 1);
    }

    public void incrementSuccess() {
        this.successCount = (this.successCount == null ? 0 : this.successCount) + 1;
        this.analyzedTables = (this.analyzedTables == null ? 0 : this.analyzedTables) + 1;
    }

    public void incrementFailure() {
        this.failureCount = (this.failureCount == null ? 0 : this.failureCount) + 1;
        this.analyzedTables = (this.analyzedTables == null ? 0 : this.analyzedTables) + 1;
    }

    public void incrementCompletedBatches() {
        this.completedBatches = (this.completedBatches == null ? 0 : this.completedBatches) + 1;
        if (this.totalBatches != null && this.totalBatches > 0) {
            this.progress = (this.completedBatches * 100) / this.totalBatches;
        }
    }

    public boolean canStart() {
        return this.status == TaskStatus.PENDING || this.status == TaskStatus.PAUSED;
    }

    public boolean isRunning() {
        return this.status == TaskStatus.RUNNING;
    }
}
