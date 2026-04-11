package com.enterprise.dataplatform.analytics.service;

import com.enterprise.dataplatform.analytics.config.AnalyticsProperties;
import com.enterprise.dataplatform.analytics.dto.AssetHeatmapResponse;
import com.enterprise.dataplatform.analytics.entity.AssetAnalytics;
import com.enterprise.dataplatform.analytics.repository.AssetAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Asset Analytics - heatmaps and trending analysis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetAnalyticsService {

    private final AssetAnalyticsRepository repository;
    private final AnalyticsProperties properties;

    /**
     * Get asset heatmap data for visualization
     */
    public AssetHeatmapResponse getAssetHeatmap(String assetId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<AssetAnalyticsRepository.HeatmapData> rawData = repository.getHeatmapData(startDate, endDate);

        if (assetId != null && !assetId.isEmpty()) {
            rawData = rawData.stream()
                    .filter(d -> assetId.equals(d.assetId()))
                    .collect(Collectors.toList());
        }

        if (rawData.isEmpty()) {
            return buildEmptyHeatmapResponse(assetId);
        }

        // Group by asset
        Map<String, List<AssetAnalyticsRepository.HeatmapData>> groupedData = rawData.stream()
                .collect(Collectors.groupingBy(AssetAnalyticsRepository.HeatmapData::assetId));

        // Take first asset for single asset request
        String targetAssetId = assetId != null ? assetId : rawData.get(0).assetId();
        List<AssetAnalyticsRepository.HeatmapData> assetData = groupedData.getOrDefault(targetAssetId, List.of());

        AssetAnalyticsRepository.HeatmapData firstEntry = assetData.isEmpty() ? rawData.get(0) : assetData.get(0);

        List<AssetHeatmapResponse.HeatmapEntry> hourlyEntries = assetData.stream()
                .map(d -> AssetHeatmapResponse.HeatmapEntry.builder()
                        .hour(d.hour())
                        .accessCount(d.accessCount())
                        .downloads(d.downloads())
                        .shares(d.shares())
                        .build())
                .sorted(Comparator.comparing(AssetHeatmapResponse.HeatmapEntry::getHour).reversed())
                .collect(Collectors.toList());

        // Calculate totals
        int totalAccess = assetData.stream().mapToInt(AssetAnalyticsRepository.HeatmapData::accessCount).sum();
        int totalDownloads = assetData.stream().mapToInt(AssetAnalyticsRepository.HeatmapData::downloads).sum();
        int totalShares = assetData.stream().mapToInt(AssetAnalyticsRepository.HeatmapData::shares).sum();
        float avgQuality = (float) assetData.stream()
                .mapToDouble(AssetAnalyticsRepository.HeatmapData::avgQuality)
                .average().orElse(0);
        float avgValue = (float) assetData.stream()
                .mapToDouble(AssetAnalyticsRepository.HeatmapData::avgValue)
                .average().orElse(0);

        return AssetHeatmapResponse.builder()
                .assetId(targetAssetId)
                .assetName(firstEntry.assetName())
                .assetType(firstEntry.assetType())
                .totalAccessCount(totalAccess)
                .totalDownloads(totalDownloads)
                .totalShares(totalShares)
                .avgQualityScore(avgQuality)
                .avgValueScore(avgValue)
                .hourlyData(hourlyEntries)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Get all assets heatmap (top N)
     */
    public List<AssetHeatmapResponse> getAllAssetsHeatmap(int topN) {
        int days = properties.getAsset().getHeatmap().getTimeRangeDays();
        int limit = topN > 0 ? topN : properties.getAsset().getHeatmap().getTopN();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<AssetAnalyticsRepository.HeatmapData> rawData = repository.getHeatmapData(startDate, endDate);

        // Group and aggregate by asset
        Map<String, List<AssetAnalyticsRepository.HeatmapData>> groupedData = rawData.stream()
                .collect(Collectors.groupingBy(AssetAnalyticsRepository.HeatmapData::assetId));

        List<AssetHeatmapResponse> results = new ArrayList<>();

        groupedData.forEach((assetId, dataList) -> {
            if (results.size() >= limit) return;

            AssetAnalyticsRepository.HeatmapData firstEntry = dataList.get(0);

            int totalAccess = dataList.stream().mapToInt(AssetAnalyticsRepository.HeatmapData::accessCount).sum();
            int totalDownloads = dataList.stream().mapToInt(AssetAnalyticsRepository.HeatmapData::downloads).sum();
            int totalShares = dataList.stream().mapToInt(AssetAnalyticsRepository.HeatmapData::shares).sum();
            float avgQuality = (float) dataList.stream()
                    .mapToDouble(AssetAnalyticsRepository.HeatmapData::avgQuality)
                    .average().orElse(0);
            float avgValue = (float) dataList.stream()
                    .mapToDouble(AssetAnalyticsRepository.HeatmapData::avgValue)
                    .average().orElse(0);

            results.add(AssetHeatmapResponse.builder()
                    .assetId(assetId)
                    .assetName(firstEntry.assetName())
                    .assetType(firstEntry.assetType())
                    .totalAccessCount(totalAccess)
                    .totalDownloads(totalDownloads)
                    .totalShares(totalShares)
                    .avgQualityScore(avgQuality)
                    .avgValueScore(avgValue)
                    .hourlyData(List.of())
                    .lastUpdated(LocalDateTime.now())
                    .build());
        });

        // Sort by total access count descending
        results.sort((a, b) -> Integer.compare(b.getTotalAccessCount(), a.getTotalAccessCount()));

        return results;
    }

    /**
     * Get top trending assets
     */
    public List<AssetHeatmapResponse> getTrendingAssets(int limit) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(properties.getAsset().getTrending().getUpdateIntervalSeconds() / 60);
        int topLimit = limit > 0 ? limit : properties.getAsset().getTrending().getMinAccessCount();

        List<AssetAnalytics> topAssets = repository.findTopAccessed(startDate, endDate, topLimit);

        return topAssets.stream()
                .map(asset -> AssetHeatmapResponse.builder()
                        .assetId(asset.getAssetId())
                        .assetName(asset.getAssetName())
                        .assetType(asset.getAssetType())
                        .totalAccessCount(asset.getAccessCount())
                        .totalDownloads(asset.getDownloadCount())
                        .totalShares(asset.getShareCount())
                        .avgQualityScore(asset.getQualityScore())
                        .avgValueScore(asset.getValueScore())
                        .lastUpdated(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Save asset analytics data
     */
    public void saveAssetAnalytics(AssetAnalytics analytics) {
        analytics.setUpdatedAt(LocalDateTime.now());
        if (analytics.getDate() == null) {
            analytics.setDate(LocalDate.now());
        }
        repository.save(analytics);
        log.info("Saved asset analytics for asset: {}", analytics.getAssetId());
    }

    /**
     * Batch save asset analytics
     */
    @Async
    public void batchSaveAssetAnalytics(List<AssetAnalytics> analyticsList) {
        repository.batchSave(analyticsList);
        log.info("Batch saved {} asset analytics records", analyticsList.size());
    }

    /**
     * Scheduled task to refresh trending data
     */
    @Scheduled(fixedRateString = "${analytics.asset.trending.update-interval-seconds:300}000")
    public void refreshTrendingAssets() {
        log.info("Starting scheduled trending assets refresh...");
        try {
            // This could trigger a material view refresh or recalculate trends
            List<AssetHeatmapResponse> trending = getTrendingAssets(properties.getAsset().getHeatmap().getTopN());
            log.info("Refreshed {} trending assets", trending.size());
        } catch (Exception e) {
            log.error("Failed to refresh trending assets: {}", e.getMessage(), e);
        }
    }

    /**
     * Get asset analytics summary
     */
    public Map<String, Object> getAssetSummary(LocalDate startDate, LocalDate endDate) {
        List<AssetAnalytics> analytics = repository.findByTimeRange(startDate, endDate);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAssets", analytics.size());
        summary.put("totalAccesses", analytics.stream().mapToInt(AssetAnalytics::getAccessCount).sum());
        summary.put("totalDownloads", analytics.stream().mapToInt(AssetAnalytics::getDownloadCount).sum());
        summary.put("totalShares", analytics.stream().mapToInt(AssetAnalytics::getShareCount).sum());
        summary.put("avgQualityScore", analytics.stream()
                .mapToDouble(AssetAnalytics::getQualityScore)
                .average().orElse(0));
        summary.put("avgValueScore", analytics.stream()
                .mapToDouble(AssetAnalytics::getValueScore)
                .average().orElse(0));
        summary.put("dateRange", Map.of("start", startDate, "end", endDate));

        return summary;
    }

    private AssetHeatmapResponse buildEmptyHeatmapResponse(String assetId) {
        return AssetHeatmapResponse.builder()
                .assetId(assetId)
                .totalAccessCount(0)
                .totalDownloads(0)
                .totalShares(0)
                .avgQualityScore(0f)
                .avgValueScore(0f)
                .hourlyData(List.of())
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}
