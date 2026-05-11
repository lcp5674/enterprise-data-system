package com.enterprise.edams.analysis.dto.response;

import com.enterprise.edams.analysis.entity.AnalysisTask;
import com.enterprise.edams.analysis.entity.ExecutionMode;
import com.enterprise.edams.analysis.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String taskCode;
    private String taskName;
    private String taskDescription;
    private Long datasourceId;
    private String datasourceName;
    private String datasourceType;
    private String schemaName;
    private Long modelConfigId;
    private List<String> targetTables;
    private List<String> excludedTables;
    private Integer batchSize;
    private Boolean enableLineageAnalysis;
    private Boolean enableIndicatorExtraction;
    private Boolean enableSubjectClassification;
    private Boolean autoRegister;
    private Integer sampleRowCount;
    private ExecutionMode executionMode;
    private String executionModeName;
    private String cronExpression;
    private LocalDateTime scheduledTime;
    private TaskStatus status;
    private String statusName;
    private Integer progress;
    private Integer totalBatches;
    private Integer completedBatches;
    private Integer totalTables;
    private Integer analyzedTables;
    private Integer successCount;
    private Integer failureCount;
    private String executor;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long executionTimeMs;
    private String lastErrorMessage;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse fromEntity(AnalysisTask task) {
        return TaskResponse.builder()
                .id(task.getId())
                .taskCode(task.getTaskCode())
                .taskName(task.getTaskName())
                .taskDescription(task.getTaskDescription())
                .datasourceId(task.getDatasourceId())
                .datasourceName(task.getDatasourceName())
                .datasourceType(task.getDatasourceType())
                .schemaName(task.getSchemaName())
                .modelConfigId(task.getModelConfigId())
                .batchSize(task.getBatchSize())
                .enableLineageAnalysis(task.getEnableLineageAnalysis())
                .enableIndicatorExtraction(task.getEnableIndicatorExtraction())
                .enableSubjectClassification(task.getEnableSubjectClassification())
                .autoRegister(task.getAutoRegister())
                .sampleRowCount(task.getSampleRowCount())
                .executionMode(task.getExecutionMode())
                .executionModeName(task.getExecutionMode().getDescription())
                .cronExpression(task.getCronExpression())
                .scheduledTime(task.getScheduledTime())
                .status(task.getStatus())
                .statusName(task.getStatus().getDescription())
                .progress(task.getProgress())
                .totalBatches(task.getTotalBatches())
                .completedBatches(task.getCompletedBatches())
                .totalTables(task.getTotalTables())
                .analyzedTables(task.getAnalyzedTables())
                .successCount(task.getSuccessCount())
                .failureCount(task.getFailureCount())
                .executor(task.getExecutor())
                .startTime(task.getStartTime())
                .endTime(task.getEndTime())
                .executionTimeMs(task.getExecutionTimeMs())
                .lastErrorMessage(task.getLastErrorMessage())
                .createdBy(task.getCreatedBy())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
