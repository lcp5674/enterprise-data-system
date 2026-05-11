package com.enterprise.edams.analysis.dto.response;

import com.enterprise.edams.analysis.entity.LocalModelConfig;
import com.enterprise.edams.analysis.entity.ModelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigResponse {

    private Long id;
    private String configCode;
    private String configName;
    private ModelType modelType;
    private String modelTypeName;
    private String baseUrl;
    private String modelName;
    private String modelVersion;
    private Integer maxTokens;
    private Double temperature;
    private Integer contextWindow;
    private Boolean enabled;
    private Boolean isDefault;
    private Long usageCount;
    private Long successCount;
    private Long failureCount;
    private Double successRate;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public static ModelConfigResponse fromEntity(LocalModelConfig config) {
        ModelConfigResponseBuilder builder = ModelConfigResponse.builder()
                .id(config.getId())
                .configCode(config.getConfigCode())
                .configName(config.getConfigName())
                .modelType(config.getModelType())
                .modelTypeName(config.getModelType().getDisplayName())
                .baseUrl(config.getBaseUrl())
                .modelName(config.getModelName())
                .modelVersion(config.getModelVersion())
                .maxTokens(config.getMaxTokens())
                .temperature(config.getTemperature())
                .contextWindow(config.getContextWindow())
                .enabled(config.getEnabled())
                .isDefault(config.getIsDefault())
                .usageCount(config.getUsageCount())
                .successCount(config.getSuccessCount())
                .failureCount(config.getFailureCount())
                .createdBy(config.getCreatedBy())
                .createdAt(config.getCreatedAt())
                .updatedBy(config.getUpdatedBy())
                .updatedAt(config.getUpdatedAt());

        if (config.getUsageCount() != null && config.getUsageCount() > 0) {
            double successRate = (double) config.getSuccessCount() / config.getUsageCount() * 100;
            builder.successRate(Math.round(successRate * 100.0) / 100.0);
        } else {
            builder.successRate(0.0);
        }

        return builder.build();
    }
}
