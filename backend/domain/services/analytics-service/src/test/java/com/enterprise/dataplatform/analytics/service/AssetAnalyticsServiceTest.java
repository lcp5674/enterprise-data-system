package com.enterprise.dataplatform.analytics.service;

import com.enterprise.dataplatform.analytics.config.AnalyticsProperties;
import com.enterprise.dataplatform.analytics.dto.AssetHeatmapResponse;
import com.enterprise.dataplatform.analytics.entity.AssetAnalytics;
import com.enterprise.dataplatform.analytics.repository.AssetAnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AssetAnalyticsService
 */
@ExtendWith(MockitoExtension.class)
class AssetAnalyticsServiceTest {

    @Mock
    private AssetAnalyticsRepository repository;

    @Mock
    private AnalyticsProperties properties;

    @Mock
    private AnalyticsProperties.AssetConfig assetConfig;

    @Mock
    private AnalyticsProperties.AssetConfig.HeatmapConfig heatmapConfig;

    @InjectMocks
    private AssetAnalyticsService service;

    @BeforeEach
    void setUp() {
        when(properties.getAsset()).thenReturn(assetConfig);
        when(assetConfig.getHeatmap()).thenReturn(heatmapConfig);
        when(assetConfig.getTrending()).thenReturn(new AnalyticsProperties.AssetConfig.TrendingConfig());
        when(heatmapConfig.getTopN()).thenReturn(100);
        when(heatmapConfig.getTimeRangeDays()).thenReturn(30);
    }

    @Test
    void testGetAssetHeatmap_EmptyData() {
        when(repository.getHeatmapData(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        AssetHeatmapResponse response = service.getAssetHeatmap("asset-001", 30);

        assertNotNull(response);
        assertEquals("asset-001", response.getAssetId());
        assertEquals(0, response.getTotalAccessCount());
    }

    @Test
    void testGetAssetHeatmap_WithData() {
        LocalDateTime now = LocalDateTime.now();
        List<AssetAnalyticsRepository.HeatmapData> mockData = List.of(
                new AssetAnalyticsRepository.HeatmapData(
                        "asset-001", "Asset One", "TABLE", now.minusHours(1),
                        10, 5, 2, 85.0f, 90.0f
                ),
                new AssetAnalyticsRepository.HeatmapData(
                        "asset-001", "Asset One", "TABLE", now.minusHours(2),
                        15, 8, 3, 86.0f, 91.0f
                )
        );

        when(repository.getHeatmapData(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockData);

        AssetHeatmapResponse response = service.getAssetHeatmap("asset-001", 30);

        assertNotNull(response);
        assertEquals("asset-001", response.getAssetId());
        assertEquals("Asset One", response.getAssetName());
        assertEquals("TABLE", response.getAssetType());
        assertEquals(25, response.getTotalAccessCount());
        assertEquals(13, response.getTotalDownloads());
        assertEquals(5, response.getTotalShares());
        assertEquals(2, response.getHourlyData().size());
    }

    @Test
    void testSaveAssetAnalytics() {
        AssetAnalytics analytics = AssetAnalytics.builder()
                .id(1L)
                .assetId("asset-001")
                .assetName("Test Asset")
                .assetType("TABLE")
                .accessCount(100)
                .qualityScore(85.0f)
                .valueScore(90.0f)
                .build();

        service.saveAssetAnalytics(analytics);

        verify(repository, times(1)).save(analytics);
        assertNotNull(analytics.getUpdatedAt());
        assertNotNull(analytics.getDate());
    }

    @Test
    void testGetTrendingAssets() {
        List<AssetAnalytics> mockAssets = List.of(
                AssetAnalytics.builder()
                        .assetId("asset-001")
                        .assetName("Trending Asset 1")
                        .assetType("TABLE")
                        .accessCount(1000)
                        .downloadCount(500)
                        .shareCount(100)
                        .qualityScore(95.0f)
                        .valueScore(88.0f)
                        .build(),
                AssetAnalytics.builder()
                        .assetId("asset-002")
                        .assetName("Trending Asset 2")
                        .assetType("REPORT")
                        .accessCount(800)
                        .downloadCount(400)
                        .shareCount(80)
                        .qualityScore(88.0f)
                        .valueScore(85.0f)
                        .build()
        );

        when(repository.findTopAccessed(any(LocalDate.class), any(LocalDate.class), anyInt()))
                .thenReturn(mockAssets);

        List<AssetHeatmapResponse> trending = service.getTrendingAssets(10);

        assertNotNull(trending);
        assertEquals(2, trending.size());
        assertEquals("Trending Asset 1", trending.get(0).getAssetName());
        assertEquals(1000, trending.get(0).getTotalAccessCount());
    }

    @Test
    void testGetAssetSummary() {
        List<AssetAnalytics> mockData = List.of(
                AssetAnalytics.builder()
                        .assetId("asset-001")
                        .accessCount(100)
                        .downloadCount(50)
                        .shareCount(10)
                        .qualityScore(85.0f)
                        .valueScore(90.0f)
                        .build(),
                AssetAnalytics.builder()
                        .assetId("asset-002")
                        .accessCount(200)
                        .downloadCount(100)
                        .shareCount(20)
                        .qualityScore(88.0f)
                        .valueScore(92.0f)
                        .build()
        );

        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        when(repository.findByTimeRange(startDate, endDate)).thenReturn(mockData);

        var summary = service.getAssetSummary(startDate, endDate);

        assertNotNull(summary);
        assertEquals(2, summary.get("totalAssets"));
        assertEquals(300L, summary.get("totalAccesses"));
        assertEquals(150L, summary.get("totalDownloads"));
        assertEquals(30L, summary.get("totalShares"));
    }

    @Test
    void testBatchSaveAssetAnalytics() {
        List<AssetAnalytics> analyticsList = List.of(
                AssetAnalytics.builder()
                        .assetId("asset-001")
                        .accessCount(100)
                        .build(),
                AssetAnalytics.builder()
                        .assetId("asset-002")
                        .accessCount(200)
                        .build()
        );

        service.batchSaveAssetAnalytics(analyticsList);

        verify(repository, times(1)).batchSave(analyticsList);
    }
}
