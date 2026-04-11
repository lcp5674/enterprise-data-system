package com.enterprise.dataplatform.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Quality Metrics Entity - stores aggregated data quality metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataQualityMetrics {

    private Long id;
    private LocalDateTime metricTime;
    private String metricCategory;
    private String metricName;
    private Double metricValue;
    private String metricUnit;
    private Double thresholdValue;
    private String alertLevel;
    private String dimensions;
    private LocalDateTime createdAt;
}
