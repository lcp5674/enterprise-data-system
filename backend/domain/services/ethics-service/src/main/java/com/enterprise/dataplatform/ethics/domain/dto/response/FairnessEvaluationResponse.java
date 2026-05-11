package com.enterprise.dataplatform.ethics.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FairnessEvaluationResponse {

    private String evaluationId;
    private String assetId;
    private String assetName;
    private String evaluationType;
    private BigDecimal overallFairnessScore;
    private String riskLevel;
    private List<String> detectedBiases;
    private Map<String, BigDecimal> groupDisparityScores;
    private Map<String, String> statisticalSignificance;
    private BigDecimal disparateImpactRatio;
    private BigDecimal statisticalParityDifference;
    private List<String> protectedAttributesAnalyzed;
    private List<String> recommendations;
    private List<String> positiveIndicators;
    private String methodology;
    private Double significanceLevel;
    private Map<String, Object> detailedMetrics;
}
