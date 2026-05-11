package com.enterprise.edams.analysis.dto.request;

import com.enterprise.edams.analysis.entity.ExecutionMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateTaskRequest {

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    private String taskDescription;

    @NotNull(message = "数据源ID不能为空")
    private Long datasourceId;

    private String datasourceName;

    private String datasourceType;

    private String schemaName;

    private Long modelConfigId;

    private List<String> targetTables;

    private List<String> excludedTables;

    @Min(value = 1, message = "批处理大小必须大于0")
    private Integer batchSize;

    private Boolean enableLineageAnalysis;

    private Boolean enableIndicatorExtraction;

    private Boolean enableSubjectClassification;

    private Boolean autoRegister;

    @Min(value = 1, message = "采样行数必须大于0")
    private Integer sampleRowCount;

    @NotNull(message = "执行模式不能为空")
    private ExecutionMode executionMode;

    private String cronExpression;

    private LocalDateTime scheduledTime;

    private String executor;
}
