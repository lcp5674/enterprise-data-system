package com.enterprise.edams.analysis.dto.request;

import com.enterprise.edams.analysis.entity.ModelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateModelConfigRequest {

    @NotBlank(message = "配置编码不能为空")
    private String configCode;

    @NotBlank(message = "配置名称不能为空")
    private String configName;

    @NotNull(message = "模型类型不能为空")
    private ModelType modelType;

    @NotBlank(message = "API地址不能为空")
    private String baseUrl;

    private String apiKey;

    @NotBlank(message = "模型名称不能为空")
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
