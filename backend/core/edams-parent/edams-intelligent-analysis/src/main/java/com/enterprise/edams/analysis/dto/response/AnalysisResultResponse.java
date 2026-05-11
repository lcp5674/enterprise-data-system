package com.enterprise.edams.analysis.dto.response;

import com.enterprise.edams.analysis.entity.AnalysisResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultResponse {

    private Long id;
    private Long taskId;
    private String taskCode;
    private String tableName;
    private String schemaName;
    private Boolean success;
    private String errorMessage;
    private BigDecimal confidence;
    private String tableDescription;
    private String tableAlias;
    private String tableCategory;
    private String fieldAnalysisResult;
    private String lineageRelations;
    private String indicatorDefinitions;
    private String subjectDomain;
    private String businessDomain;
    private String dataDomain;
    private String indicatorLayer;
    private String indicatorType;
    private Long estimatedRowCount;
    private String dataSensitivity;
    private String dataFreshness;
    private Integer inputTokens;
    private Integer outputTokens;
    private BigDecimal llmCost;
    private Boolean metadataRegistered;
    private Boolean lineageRegistered;
    private Boolean indicatorRegistered;
    private Integer batchNumber;
    private LocalDateTime analyzedAt;
    private Long analysisTimeMs;
    private LocalDateTime createdAt;

    public static AnalysisResultResponse fromEntity(AnalysisResult result) {
        return AnalysisResultResponse.builder()
                .id(result.getId())
                .taskId(result.getTaskId())
                .taskCode(result.getTaskCode())
                .tableName(result.getTableName())
                .schemaName(result.getSchemaName())
                .success(result.getSuccess())
                .errorMessage(result.getErrorMessage())
                .confidence(result.getConfidence())
                .tableDescription(result.getTableDescription())
                .tableAlias(result.getTableAlias())
                .tableCategory(result.getTableCategory())
                .fieldAnalysisResult(result.getFieldAnalysisResult())
                .lineageRelations(result.getLineageRelations())
                .indicatorDefinitions(result.getIndicatorDefinitions())
                .subjectDomain(result.getSubjectDomain())
                .businessDomain(result.getBusinessDomain())
                .dataDomain(result.getDataDomain())
                .indicatorLayer(result.getIndicatorLayer())
                .indicatorType(result.getIndicatorType())
                .estimatedRowCount(result.getEstimatedRowCount())
                .dataSensitivity(result.getDataSensitivity())
                .dataFreshness(result.getDataFreshness())
                .inputTokens(result.getInputTokens())
                .outputTokens(result.getOutputTokens())
                .llmCost(result.getLlmCost())
                .metadataRegistered(result.getMetadataRegistered())
                .lineageRegistered(result.getLineageRegistered())
                .indicatorRegistered(result.getIndicatorRegistered())
                .batchNumber(result.getBatchNumber())
                .analyzedAt(result.getAnalyzedAt())
                .analysisTimeMs(result.getAnalysisTimeMs())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
