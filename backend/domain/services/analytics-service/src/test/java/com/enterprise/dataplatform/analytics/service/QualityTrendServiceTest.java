package com.enterprise.dataplatform.analytics.service;

import com.enterprise.dataplatform.analytics.config.AnalyticsProperties;
import com.enterprise.dataplatform.analytics.dto.QualityTrendResponse;
import com.enterprise.dataplatform.analytics.entity.QualityTrend;
import com.enterprise.dataplatform.analytics.repository.QualityTrendRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QualityTrendService
 */
@ExtendWith(MockitoExtension.class)
class QualityTrendServiceTest {

    @Mock
    private QualityTrendRepository repository;

    @Mock
    private AnalyticsProperties properties;

    @Mock
    private AnalyticsProperties.QualityConfig qualityConfig;

    @Mock
    private AnalyticsProperties.QualityConfig.TrendConfig trendConfig;

    @InjectMocks
    private QualityTrendService service;

    @BeforeEach
    void setUp() {
        when(properties.getQuality()).thenReturn(qualityConfig);
        when(qualityConfig.getTrend()).thenReturn(trendConfig);
        when(trendConfig.getAggregationIntervalMinutes()).thenReturn(60);
        when(trendConfig.getRetentionDays()).thenReturn(90);
    }

    @Test
    void testGetQualityTrend_EmptyData() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        when(repository.getTrendAggregation(startTime, endTime))
                .thenReturn(new ArrayList<>());

        QualityTrendResponse response = service.getQualityTrend(null, startTime, endTime);

        assertNotNull(response);
        assertEquals("ALL", response.getCheckType());
        assertEquals(0, response.getTotalAssets());
        assertEquals(0, response.getTotalChecks());
    }

    @Test
    void testGetQualityTrend_WithData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusDays(7);
        LocalDateTime endTime = now;

        List<QualityTrendRepository.TrendAggregation> mockData = List.of(
                new QualityTrendRepository.TrendAggregation(
                        "completeness", now.minusHours(1),
                        10, 100, 90, 10, 90.0f, 85.0f, 70.0f, 95.0f
                ),
                new QualityTrendRepository.TrendAggregation(
                        "completeness", now.minusHours(2),
                        12, 120, 108, 12, 90.0f, 86.0f, 72.0f, 96.0f
                )
        );

        when(repository.getTrendAggregation(startTime, endTime)).thenReturn(mockData);
        when(repository.findByTimeRange(startTime, endTime)).thenReturn(new ArrayList<>());

        QualityTrendResponse response = service.getQualityTrend("completeness", startTime, endTime);

        assertNotNull(response);
        assertEquals("completeness", response.getCheckType());
        assertEquals(22, response.getTotalAssets());
        assertEquals(220, response.getTotalChecks());
        assertEquals(198, response.getPassedChecks());
        assertEquals(22, response.getFailedChecks());
        assertEquals(2, response.getTrendPoints().size());
    }

    @Test
    void testSaveQualityTrend() {
        QualityTrend trend = QualityTrend.builder()
                .id(1L)
                .assetId("asset-001")
                .assetName("Test Asset")
                .checkType("completeness")
                .totalChecks(100)
                .passedChecks(90)
                .failedChecks(10)
                .passRate(90.0f)
                .avgScore(85.0f)
                .checkTime(LocalDateTime.now())
                .build();

        service.saveQualityTrend(trend);

        verify(repository, times(1)).save(trend);
        assertNotNull(trend.getCreatedAt());
    }

    @Test
    void testGetAssetQualityTrend() {
        String assetId = "asset-001";
        int days = 30;

        List<QualityTrend> mockTrends = List.of(
                QualityTrend.builder()
                        .assetId(assetId)
                        .checkType("completeness")
                        .passRate(90.0f)
                        .avgScore(85.0f)
                        .build()
        );

        when(repository.findByAssetAndTimeRange(eq(assetId), any(), any()))
                .thenReturn(mockTrends);

        List<QualityTrend> trends = service.getAssetQualityTrend(assetId, days);

        assertNotNull(trends);
        assertEquals(1, trends.size());
        assertEquals(assetId, trends.get(0).getAssetId());
    }

    @Test
    void testGetLatestTrends() {
        when(repository.findLatestByCheckType("completeness"))
                .thenReturn(Optional.of(QualityTrend.builder()
                        .checkType("completeness")
                        .passRate(90.0f)
                        .build()));
        when(repository.findLatestByCheckType("freshness"))
                .thenReturn(Optional.of(QualityTrend.builder()
                        .checkType("freshness")
                        .passRate(85.0f)
                        .build()));
        when(repository.findLatestByCheckType("accuracy"))
                .thenReturn(Optional.empty());
        when(repository.findLatestByCheckType("consistency"))
                .thenReturn(Optional.empty());

        List<QualityTrend> latestTrends = service.getLatestTrends();

        assertNotNull(latestTrends);
        assertEquals(2, latestTrends.size());
    }

    @Test
    void testGetQualitySummary() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        List<QualityTrend> mockData = List.of(
                QualityTrend.builder()
                        .checkType("completeness")
                        .totalChecks(100)
                        .passedChecks(90)
                        .failedChecks(10)
                        .passRate(90.0f)
                        .avgScore(85.0f)
                        .avgCompleteness(88.0f)
                        .avgFreshness(80.0f)
                        .avgAccuracy(85.0f)
                        .avgConsistency(82.0f)
                        .build(),
                QualityTrend.builder()
                        .checkType("accuracy")
                        .totalChecks(200)
                        .passedChecks(180)
                        .failedChecks(20)
                        .passRate(90.0f)
                        .avgScore(88.0f)
                        .avgCompleteness(85.0f)
                        .avgFreshness(82.0f)
                        .avgAccuracy(90.0f)
                        .avgConsistency(86.0f)
                        .build()
        );

        when(repository.findByTimeRange(startTime, endTime)).thenReturn(mockData);

        var summary = service.getQualitySummary(startTime, endTime);

        assertNotNull(summary);
        assertEquals(2, summary.get("totalRecords"));
        assertEquals(300L, summary.get("totalChecks"));
        assertEquals(270L, summary.get("passedChecks"));
        assertEquals(30L, summary.get("failedChecks"));
    }

    @Test
    void testBatchSaveQualityTrends() {
        List<QualityTrend> trends = List.of(
                QualityTrend.builder()
                        .assetId("asset-001")
                        .checkType("completeness")
                        .passRate(90.0f)
                        .build(),
                QualityTrend.builder()
                        .assetId("asset-002")
                        .checkType("accuracy")
                        .passRate(85.0f)
                        .build()
        );

        service.batchSaveQualityTrends(trends);

        verify(repository, times(1)).batchSave(trends);
    }
}
