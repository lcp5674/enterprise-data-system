package com.enterprise.dataplatform.classification.dto.response;

import com.enterprise.dataplatform.classification.domain.enums.SensitivityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResponse {

    private String assetId;
    private String assetName;
    private String assetType;
    private SensitivityLevel sensitivityLevel;
    private Double confidenceScore;
    private String classificationMethod;
    private String matchedRuleName;
    private List<String> matchedKeywords;
    private List<String> dataCategories;
    private List<String> tags;
    private Boolean requiresApproval;
    private String status;
    private String message;
}
