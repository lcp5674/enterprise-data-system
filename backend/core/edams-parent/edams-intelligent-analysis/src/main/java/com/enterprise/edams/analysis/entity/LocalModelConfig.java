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
@Table(name = "local_model_config", indexes = {
        @Index(name = "idx_model_type", columnList = "model_type"),
        @Index(name = "idx_enabled", columnList = "enabled"),
        @Index(name = "idx_is_default", columnList = "is_default")
})
public class LocalModelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_code", nullable = false, unique = true, length = 64)
    private String configCode;

    @Column(name = "config_name", nullable = false, length = 128)
    private String configName;

    @Enumerated(EnumType.STRING)
    @Column(name = "model_type", nullable = false, length = 32)
    private ModelType modelType;

    @Column(name = "base_url", nullable = false, length = 512)
    private String baseUrl;

    @Column(name = "api_key", length = 256)
    private String apiKey;

    @Column(name = "model_name", nullable = false, length = 128)
    private String modelName;

    @Column(name = "model_version", length = 64)
    private String modelVersion;

    @Column(name = "max_tokens")
    @Builder.Default
    private Integer maxTokens = 4096;

    @Column(name = "temperature", precision = 3, scale = 2)
    @Builder.Default
    private Double temperature = 0.7;

    @Column(name = "context_window")
    @Builder.Default
    private Integer contextWindow = 8192;

    @Column(name = "table_analysis_template", columnDefinition = "TEXT")
    private String tableAnalysisTemplate;

    @Column(name = "lineage_analysis_template", columnDefinition = "TEXT")
    private String lineageAnalysisTemplate;

    @Column(name = "indicator_extraction_template", columnDefinition = "TEXT")
    private String indicatorExtractionTemplate;

    @Column(name = "subject_classification_template", columnDefinition = "TEXT")
    private String subjectClassificationTemplate;

    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "usage_count")
    @Builder.Default
    private Long usageCount = 0L;

    @Column(name = "success_count")
    @Builder.Default
    private Long successCount = 0L;

    @Column(name = "failure_count")
    @Builder.Default
    private Long failureCount = 0L;

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

    public void incrementUsage() {
        this.usageCount = (this.usageCount == null ? 0L : this.usageCount) + 1;
    }

    public void incrementSuccess() {
        this.successCount = (this.successCount == null ? 0L : this.successCount) + 1;
    }

    public void incrementFailure() {
        this.failureCount = (this.failureCount == null ? 0L : this.failureCount) + 1;
    }
}
