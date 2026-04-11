package com.enterprise.dataplatform.governance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendationResponse {

    private Long id;

    private String recommendationType;

    private String recommendationCode;

    private String title;

    private String description;

    private String context;

    private Double confidenceScore;

    private String priority;

    private String targetAssetId;

    private String targetAssetType;

    private Map<String, Object> recommendedAction;

    private Map<String, Object> reasoning;

    private List<String> considerationFactors;

    private Map<String, Object> expectedOutcome;

    private String potentialRisks;

    private String domain;

    private List<String> relatedPolicies;

    private String status;

    private Boolean accepted;

    private Boolean applied;

    private String appliedBy;

    private LocalDateTime appliedAt;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;
}
