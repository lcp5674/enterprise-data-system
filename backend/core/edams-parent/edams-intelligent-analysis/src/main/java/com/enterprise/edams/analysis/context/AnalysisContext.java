package com.enterprise.edams.analysis.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisContext {

    private Long taskId;
    private Long datasourceId;
    private Long modelConfigId;
    private String schema;
    private Integer sampleRowCount;
    private Boolean enableLineageAnalysis;
    private Boolean enableIndicatorExtraction;
    private Boolean enableSubjectClassification;
    private Boolean autoRegister;
    private String executor;
    private String datasourceType;
    private String datasourceName;
    private String jdbcUrl;
    private String username;
    private String password;
}
