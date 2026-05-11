package com.enterprise.dataplatform.ethics.domain.dto.request;

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
public class FairnessEvaluationRequest {

    private String assetId;

    private String assetName;

    private String evaluationType;

    private Map<String, List<String>> dataGroups;

    private Map<String, BigDecimal> groupMetrics;

    private List<String> protectedAttributes;

    private Double significanceLevel;

    private String methodology;
}
