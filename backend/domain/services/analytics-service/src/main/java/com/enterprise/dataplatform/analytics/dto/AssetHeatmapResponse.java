package com.enterprise.dataplatform.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Asset Heatmap Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetHeatmapResponse {

    private String assetId;
    private String assetName;
    private String assetType;
    private Integer totalAccessCount;
    private Integer totalDownloads;
    private Integer totalShares;
    private Float avgQualityScore;
    private Float avgValueScore;
    private List<HeatmapEntry> hourlyData;
    private LocalDateTime lastUpdated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeatmapEntry {
        private LocalDateTime hour;
        private Integer accessCount;
        private Integer downloads;
        private Integer shares;
    }
}
