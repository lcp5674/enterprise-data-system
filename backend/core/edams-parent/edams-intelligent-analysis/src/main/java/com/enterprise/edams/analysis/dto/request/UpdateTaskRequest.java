package com.enterprise.edams.analysis.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    private String taskName;

    private String taskDescription;

    private Long modelConfigId;

    private List<String> targetTables;

    private List<String> excludedTables;

    private Integer batchSize;

    private Boolean enableLineageAnalysis;

    private Boolean enableIndicatorExtraction;

    private Boolean enableSubjectClassification;

    private Boolean autoRegister;

    private Integer sampleRowCount;

    private String cronExpression;

    private String executor;
}
