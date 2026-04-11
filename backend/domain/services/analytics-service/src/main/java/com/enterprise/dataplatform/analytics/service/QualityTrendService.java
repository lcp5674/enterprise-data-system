package com.enterprise.dataplatform.analytics.service;

import com.enterprise.dataplatform.analytics.config.AnalyticsProperties;
import com.enterprise.dataplatform.analytics.dto.QualityTrendResponse;
import com.enterprise.dataplatform.analytics.entity.QualityTrend;
import com.enterprise.dataplatform.analytics.repository.QualityTrendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Quality Trend analysis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QualityTrendService {

    private final QualityTrendRepository repository;
    private final AnalyticsProperties properties;

    /**
     * Get quality trend data
     */
    public QualityTrendResponse getQualityTrend(String checkType, LocalDateTime startTime, LocalDateTime endTime) {
        List<QualityTrendRepository.TrendAggregation> aggregations = 
                repository.getTrendAggregation(startTime, endTime);

        if (checkType != null && !checkType.isEmpty()) {
            aggregations = aggregations.stream()
                    .filter(a -> checkType.equals(a.checkType()))
                    .collect(Collectors.toList());
        }

        if (aggregations.isEmpty()) {
            return buildEmptyTrendResponse(checkType, startTime, endTime);
        }

        // Aggregate all check types if not specified
        String type = checkType != null ? checkType : "ALL";

        List<QualityTrendResponse.TrendPoint> trendPoints = aggregations.stream()
                .map(a -> QualityTrendResponse.TrendPoint.builder()
                        .time(a.hour())
                        .checkCount(a.totalChecks())
                        .passedCount(a.passedChecks())
                        .failedCount(a.failedChecks())
                        .passRate(a.avgPassRate())
                        .avgScore(a.avgScore())
                        .minScore(a.minScore())
                        .maxScore(a.maxScore())
                        .build())
                .sorted(Comparator.comparing(QualityTrendResponse.TrendPoint::getTime))
                .collect(Collectors.toList());

        // Calculate overall statistics
        int totalAssets = aggregations.stream().mapToInt(QualityTrendRepository.TrendAggregation::totalAssets).sum();
        int totalChecks = aggregations.stream().mapToInt(QualityTrendRepository.TrendAggregation::totalChecks).sum();
        int passedChecks = aggregations.stream().mapToInt(QualityTrendRepository.TrendAggregation::passedChecks).sum();
        int failedChecks = aggregations.stream().mapToInt(QualityTrendRepository.TrendAggregation::failedChecks).sum();
        float avgPassRate = (float) aggregations.stream()
                .mapToDouble(QualityTrendRepository.TrendAggregation::avgPassRate)
                .average().orElse(0);
        float avgScore = (float) aggregations.stream()
                .mapToDouble(QualityTrendRepository.TrendAggregation::avgScore)
                .average().orElse(0);

        // Calculate dimension averages
        List<QualityTrend> rawTrends = repository.findByTimeRange(startTime, endTime);
        Map<String, Float> dimensionAverages = calculateDimensionAverages(rawTrends);

        return QualityTrendResponse.builder()
                .checkType(type)
                .startTime(startTime)
                .endTime(endTime)
                .totalAssets(totalAssets)
                .totalChecks(totalChecks)
                .passedChecks(passedChecks)
                .failedChecks(failedChecks)
                .avgPassRate(avgPassRate)
                .avgScore(avgScore)
                .trendPoints(trendPoints)
                .dimensionAverages(dimensionAverages)
                .build();
    }

    /**
     * Get quality trend for a specific asset
     */
    public List<QualityTrend> getAssetQualityTrend(String assetId, int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        return repository.findByAssetAndTimeRange(assetId, startTime, endTime);
    }

    /**
     * Save quality trend data
     */
    public void saveQualityTrend(QualityTrend trend) {
        if (trend.getCreatedAt() == null) {
            trend.setCreatedAt(LocalDateTime.now());
        }
        repository.save(trend);
        log.info("Saved quality trend for asset: {}, check_type: {}", trend.getAssetId(), trend.getCheckType());
    }

    /**
     * Batch save quality trends
     */
    public void batchSaveQualityTrends(List<QualityTrend> trends) {
        repository.batchSave(trends);
        log.info("Batch saved {} quality trend records", trends.size());
    }

    /**
     * Get latest quality trends by check type
     */
    public List<QualityTrend> getLatestTrends() {
        List<String> checkTypes = List.of("completeness", "freshness", "accuracy", "consistency");
        List<QualityTrend> latestTrends = new ArrayList<>();

        for (String checkType : checkTypes) {
            repository.findLatestByCheckType(checkType)
                    .ifPresent(latestTrends::add);
        }

        return latestTrends;
    }

    /**
     * Scheduled aggregation task
     */
    @Scheduled(fixedRateString = "${analytics.quality.trend.aggregation-interval-minutes:60}000")
    public void runScheduledAggregation() {
        log.info("Starting scheduled quality trend aggregation...");
        try {
            // This could aggregate raw quality data into trend tables
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(1);
            QualityTrendResponse response = getQualityTrend(null, startTime, endTime);
            log.info("Aggregated quality trends: {} assets, {} checks, avg score: {}",
                    response.getTotalAssets(), response.getTotalChecks(), response.getAvgScore());
        } catch (Exception e) {
            log.error("Failed to run quality trend aggregation: {}", e.getMessage(), e);
        }
    }

    /**
     * Get quality summary statistics
     */
    public Map<String, Object> getQualitySummary(LocalDateTime startTime, LocalDateTime endTime) {
        List<QualityTrend> trends = repository.findByTimeRange(startTime, endTime);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRecords", trends.size());
        summary.put("totalChecks", trends.stream().mapToInt(QualityTrend::getTotalChecks).sum());
        summary.put("passedChecks", trends.stream().mapToInt(QualityTrend::getPassedChecks).sum());
        summary.put("failedChecks", trends.stream().mapToInt(QualityTrend::getFailedChecks).sum());
        summary.put("avgPassRate", trends.stream()
                .mapToDouble(QualityTrend::getPassRate)
                .average().orElse(0));
        summary.put("avgScore", trends.stream()
                .mapToDouble(QualityTrend::getAvgScore)
                .average().orElse(0));

        // Group by check type
        Map<String, List<QualityTrend>> byType = trends.stream()
                .collect(Collectors.groupingBy(QualityTrend::getCheckType));
        Map<String, Map<String, Object>> byTypeSummary = new HashMap<>();

        byType.forEach((type, typeTrends) -> {
            Map<String, Object> typeStats = new HashMap<>();
            typeStats.put("count", typeTrends.size());
            typeStats.put("avgPassRate", typeTrends.stream()
                    .mapToDouble(QualityTrend::getPassRate)
                    .average().orElse(0));
            typeStats.put("avgScore", typeTrends.stream()
                    .mapToDouble(QualityTrend::getAvgScore)
                    .average().orElse(0));
            byTypeSummary.put(type, typeStats);
        });

        summary.put("byCheckType", byTypeSummary);
        summary.put("timeRange", Map.of("start", startTime, "end", endTime));

        return summary;
    }

    private Map<String, Float> calculateDimensionAverages(List<QualityTrend> trends) {
        Map<String, Float> averages = new HashMap<>();

        double avgCompleteness = trends.stream()
                .filter(t -> t.getAvgCompleteness() != null)
                .mapToDouble(QualityTrend::getAvgCompleteness)
                .average().orElse(0);
        averages.put("completeness", (float) avgCompleteness);

        double avgFreshness = trends.stream()
                .filter(t -> t.getAvgFreshness() != null)
                .mapToDouble(QualityTrend::getAvgFreshness)
                .average().orElse(0);
        averages.put("freshness", (float) avgFreshness);

        double avgAccuracy = trends.stream()
                .filter(t -> t.getAvgAccuracy() != null)
                .mapToDouble(QualityTrend::getAvgAccuracy)
                .average().orElse(0);
        averages.put("accuracy", (float) avgAccuracy);

        double avgConsistency = trends.stream()
                .filter(t -> t.getAvgConsistency() != null)
                .mapToDouble(QualityTrend::getAvgConsistency)
                .average().orElse(0);
        averages.put("consistency", (float) avgConsistency);

        return averages;
    }

    private QualityTrendResponse buildEmptyTrendResponse(String checkType, LocalDateTime startTime, LocalDateTime endTime) {
        return QualityTrendResponse.builder()
                .checkType(checkType != null ? checkType : "ALL")
                .startTime(startTime)
                .endTime(endTime)
                .totalAssets(0)
                .totalChecks(0)
                .passedChecks(0)
                .failedChecks(0)
                .avgPassRate(0f)
                .avgScore(0f)
                .trendPoints(List.of())
                .dimensionAverages(Map.of())
                .build();
    }
}
