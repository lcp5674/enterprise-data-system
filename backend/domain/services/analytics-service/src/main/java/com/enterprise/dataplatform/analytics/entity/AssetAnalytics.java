package com.enterprise.dataplatform.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Asset Analytics Entity - stores asset access statistics for heatmap and trending analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetAnalytics {

    private Long id;
    private String assetId;
    private String assetName;
    private String assetType;
    private String ownerId;
    private String department;
    private Integer accessCount;
    private Integer downloadCount;
    private Integer shareCount;
    private Integer commentCount;
    private Float qualityScore;
    private Float valueScore;
    private Float freshnessScore;
    private Float completenessScore;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate date;
}
