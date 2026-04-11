package com.enterprise.dataplatform.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Analytics Summary Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryResponse {

    private String summaryType;
    private LocalDate summaryDate;
    private String dimensionType;
    private String dimensionValue;
    private String metricName;
    private Double metricValue;
    private Long metricCount;
    private Double minValue;
    private Double maxValue;
    private Double avgValue;
    private Double p50Value;
    private Double p90Value;
    private Double p99Value;
    private LocalDateTime createdAt;
}
