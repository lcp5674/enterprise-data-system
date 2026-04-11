package com.enterprise.dataplatform.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Quality Trend Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityTrendResponse {

    private String checkType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalAssets;
    private Integer totalChecks;
    private Integer passedChecks;
    private Integer failedChecks;
    private Float avgPassRate;
    private Float avgScore;
    private List<TrendPoint> trendPoints;
    private Map<String, Float> dimensionAverages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private LocalDateTime time;
        private Integer checkCount;
        private Integer passedCount;
        private Integer failedCount;
        private Float passRate;
        private Float avgScore;
        private Float minScore;
        private Float maxScore;
    }
}
