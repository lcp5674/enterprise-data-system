package com.enterprise.edams.analysis.dto.request;

import com.enterprise.edams.analysis.entity.ModelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateModelConfigRequest {

    private String configName;

    private ModelType modelType;

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private String modelVersion;

    private Integer maxTokens;

    private Double temperature;

    private Integer contextWindow;

    private String tableAnalysisTemplate;

    private String lineageAnalysisTemplate;

    private String indicatorExtractionTemplate;

    private String subjectClassificationTemplate;

    private Boolean enabled;

    private Boolean isDefault;
}
