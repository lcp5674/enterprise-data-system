package com.enterprise.dataplatform.analytics.controller;

import com.enterprise.dataplatform.analytics.dto.AssetHeatmapResponse;
import com.enterprise.dataplatform.analytics.entity.AssetAnalytics;
import com.enterprise.dataplatform.analytics.service.AssetAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Asset Analytics
 */
@RestController
@RequestMapping("/api/v1/analytics/assets")
@RequiredArgsConstructor
@Tag(name = "资产分析", description = "资产访问热力图和趋势分析")
public class AssetAnalyticsController {

    private final AssetAnalyticsService assetAnalyticsService;

    /**
     * Get asset heatmap data
     * GET /api/v1/analytics/assets/heatmap
     */
    @GetMapping("/heatmap")
    @Operation(summary = "获取资产访问热力图", description = "获取资产的访问热度数据用于可视化展示")
    public ResponseEntity<Map<String, Object>> getAssetHeatmap(
            @Parameter(description = "资产ID，不指定则返回所有资产")
            @RequestParam(required = false) String assetId,
            @Parameter(description = "时间范围（天数）")
            @RequestParam(defaultValue = "30") int days) {
        
        AssetHeatmapResponse response = assetAnalyticsService.getAssetHeatmap(assetId, days);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * Get all assets heatmap (top N)
     * GET /api/v1/analytics/assets/heatmap/all
     */
    @GetMapping("/heatmap/all")
    @Operation(summary = "获取所有资产热力图", description = "获取多个资产的访问热度排行")
    public ResponseEntity<Map<String, Object>> getAllAssetsHeatmap(
            @Parameter(description = "返回数量")
            @RequestParam(defaultValue = "100") int topN) {
        
        List<AssetHeatmapResponse> responses = assetAnalyticsService.getAllAssetsHeatmap(topN);
        return ResponseEntity.ok(wrapResponse(responses));
    }

    /**
     * Get trending assets
     * GET /api/v1/analytics/assets/trending
     */
    @GetMapping("/trending")
    @Operation(summary = "获取热门资产", description = "获取当前最热门的资产排行")
    public ResponseEntity<Map<String, Object>> getTrendingAssets(
            @Parameter(description = "返回数量")
            @RequestParam(defaultValue = "20") int limit) {
        
        List<AssetHeatmapResponse> responses = assetAnalyticsService.getTrendingAssets(limit);
        return ResponseEntity.ok(wrapResponse(responses));
    }

    /**
     * Get asset analytics summary
     * GET /api/v1/analytics/assets/summary
     */
    @GetMapping("/summary")
    @Operation(summary = "获取资产分析汇总", description = "获取指定时间范围内的资产分析汇总数据")
    public ResponseEntity<Map<String, Object>> getAssetSummary(
            @Parameter(description = "开始日期")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> summary = assetAnalyticsService.getAssetSummary(startDate, endDate);
        return ResponseEntity.ok(wrapResponse(summary));
    }

    /**
     * Record asset analytics
     * POST /api/v1/analytics/assets
     */
    @PostMapping
    @Operation(summary = "记录资产分析数据", description = "记录单个资产的访问分析数据")
    public ResponseEntity<Map<String, Object>> recordAssetAnalytics(
            @RequestBody AssetAnalytics analytics,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String executor) {
        
        assetAnalyticsService.saveAssetAnalytics(analytics);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wrapResponse(Map.of("message", "Asset analytics recorded successfully")));
    }

    /**
     * Batch record asset analytics
     * POST /api/v1/analytics/assets/batch
     */
    @PostMapping("/batch")
    @Operation(summary = "批量记录资产分析数据", description = "批量记录多个资产的访问分析数据")
    public ResponseEntity<Map<String, Object>> batchRecordAssetAnalytics(
            @RequestBody List<AssetAnalytics> analyticsList,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String executor) {
        
        assetAnalyticsService.batchSaveAssetAnalytics(analyticsList);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(wrapResponse(Map.of("message", "Batch recording accepted", 
                        "count", analyticsList.size())));
    }

    /**
     * Get assets by type
     * GET /api/v1/analytics/assets/type/{assetType}
     */
    @GetMapping("/type/{assetType}")
    @Operation(summary = "按类型获取资产分析", description = "获取指定类型资产的分析数据")
    public ResponseEntity<Map<String, Object>> getAssetsByType(
            @PathVariable String assetType,
            @Parameter(description = "时间范围（天数）")
            @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "返回数量")
            @RequestParam(defaultValue = "100") int limit) {
        
        List<AssetHeatmapResponse> allAssets = assetAnalyticsService.getAllAssetsHeatmap(limit * 2);
        List<AssetHeatmapResponse> filtered = allAssets.stream()
                .filter(a -> assetType.equals(a.getAssetType()))
                .limit(limit)
                .toList();
        
        return ResponseEntity.ok(wrapResponse(filtered));
    }

    /**
     * Health check endpoint
     * GET /api/v1/analytics/assets/health
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查资产分析服务健康状态")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "analytics-service");
        health.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(wrapResponse(health));
    }

    private Map<String, Object> wrapResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
