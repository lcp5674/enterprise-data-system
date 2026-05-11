package com.enterprise.edams.analysis.entity;

import jakarta.persistence.*;
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
@Entity
@Table(name = "analysis_result", indexes = {
        @Index(name = "idx_result_task_id", columnList = "task_id"),
        @Index(name = "idx_result_table_name", columnList = "table_name"),
        @Index(name = "idx_result_success", columnList = "success"),
        @Index(name = "idx_result_subject_domain", columnList = "subject_domain"),
        @Index(name = "idx_result_analyzed_at", columnList = "analyzed_at")
})
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "task_code", length = 64)
    private String taskCode;

    @Column(name = "table_name", nullable = false, length = 128)
    private String tableName;

    @Column(name = "schema_name", length = 128)
    private String schemaName;

    @Column(name = "success")
    @Builder.Default
    private Boolean success = false;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "confidence", precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "table_description", columnDefinition = "TEXT")
    private String tableDescription;

    @Column(name = "table_alias", length = 128)
    private String tableAlias;

    @Column(name = "table_category", length = 32)
    private String tableCategory;

    @Column(name = "field_analysis_result", columnDefinition = "TEXT")
    private String fieldAnalysisResult;

    @Column(name = "lineage_relations", columnDefinition = "TEXT")
    private String lineageRelations;

    @Column(name = "indicator_definitions", columnDefinition = "TEXT")
    private String indicatorDefinitions;

    @Column(name = "subject_domain", length = 128)
    private String subjectDomain;

    @Column(name = "business_domain", length = 128)
    private String businessDomain;

    @Column(name = "data_domain", length = 128)
    private String dataDomain;

    @Column(name = "indicator_layer", length = 32)
    private String indicatorLayer;

    @Column(name = "indicator_type", length = 32)
    private String indicatorType;

    @Column(name = "estimated_row_count")
    private Long estimatedRowCount;

    @Column(name = "data_sensitivity", length = 32)
    private String dataSensitivity;

    @Column(name = "data_freshness", length = 32)
    private String dataFreshness;

    @Column(name = "raw_llm_response", columnDefinition = "TEXT")
    private String rawLlmResponse;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "llm_cost", precision = 10, scale = 4)
    private BigDecimal llmCost;

    @Column(name = "metadata_registered")
    @Builder.Default
    private Boolean metadataRegistered = false;

    @Column(name = "lineage_registered")
    @Builder.Default
    private Boolean lineageRegistered = false;

    @Column(name = "indicator_registered")
    @Builder.Default
    private Boolean indicatorRegistered = false;

    @Column(name = "registration_details", columnDefinition = "TEXT")
    private String registrationDetails;

    @Column(name = "batch_number")
    private Integer batchNumber;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Column(name = "analysis_time_ms")
    private Long analysisTimeMs;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (analyzedAt == null) {
            analyzedAt = LocalDateTime.now();
        }
    }

    public boolean isRegistrationComplete() {
        return Boolean.TRUE.equals(metadataRegistered) 
                && Boolean.TRUE.equals(lineageRegistered) 
                && Boolean.TRUE.equals(indicatorRegistered);
    }
}
