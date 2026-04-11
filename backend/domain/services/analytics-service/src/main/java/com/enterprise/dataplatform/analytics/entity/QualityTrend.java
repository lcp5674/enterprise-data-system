package com.enterprise.dataplatform.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Quality Trend Entity - stores quality check trend data over time
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityTrend {

    private Long id;
    private LocalDateTime checkTime;
    private String assetId;
    private String assetName;
    private String checkType;
    private Integer totalChecks;
    private Integer passedChecks;
    private Integer failedChecks;
    private Float passRate;
    private Float avgScore;
    private Float minScore;
    private Float maxScore;
    private Float avgCompleteness;
    private Float avgFreshness;
    private Float avgAccuracy;
    private Float avgConsistency;
    private List<String> dimensionScores;
    private LocalDateTime createdAt;
}
