package com.enterprise.dataplatform.classification.dto.request;

import com.enterprise.dataplatform.classification.domain.enums.ClassificationRuleType;
import com.enterprise.dataplatform.classification.domain.enums.SensitivityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationRequest {

    @NotBlank(message = "资产ID不能为空")
    private String assetId;

    private String assetName;

    private String assetType;

    private List<String> sampleValues;

    private String columnName;

    private String dataType;

    private Boolean forceClassify;
}
