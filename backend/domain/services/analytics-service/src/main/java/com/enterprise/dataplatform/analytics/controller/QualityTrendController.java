package com.enterprise.dataplatform.analytics.controller;

import com.enterprise.dataplatform.analytics.dto.QualityTrendResponse;
import com.enterprise.dataplatform.analytics.entity.QualityTrend;
import com.enterprise.dataplatform.analytics.service.QualityTrendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Quality Trend analysis
 */
@RestController
@RequestMapping("/api/v1/analytics/quality")
@RequiredArgsConstructor
@Tag(name = "质量趋势分析", description = "数据质量趋势监控和分析")
public class QualityTrendController {

    private final QualityTrendService qualityTrendService;

    /**
     * Get quality trend data
     * GET /api/v1/analytics/quality/trend
     */
    @GetMapping("/trend")
    @Operation(summary = "获取质量趋势", description = "获取数据质量检查的趋势分析数据")
    public ResponseEntity<Map<String, Object>> getQualityTrend(
            @Parameter(description = "检查类型：completeness, freshness, accuracy, consistency")
            @RequestParam(required = false) String checkType,
            @Parameter(description = "开始时间")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        QualityTrendResponse response = qualityTrendService.getQualityTrend(checkType, startTime, endTime);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * Get default quality trend (last 7 days)
     * GET /api/v1/analytics/quality/trend/default
     */
    @GetMapping("/trend/default")
    @Operation(summary = "获取默认质量趋势", description = "获取最近7天的质量趋势数据")
    public ResponseEntity<Map<String, Object>> getDefaultQualityTrend(
            @Parameter(description = "检查类型")
            @RequestParam(required = false) String checkType) {
        
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(7);
        
        QualityTrendResponse response = qualityTrendService.getQualityTrend(checkType, startTime, endTime);
        return ResponseEntity.ok(wrapResponse(response));
    }

    /**
     * Get quality trend for specific asset
     * GET /api/v1/analytics/quality/trend/asset/{assetId}
     */
    @GetMapping("/trend/asset/{assetId}")
    @Operation(summary = "获取资产质量趋势", description = "获取特定资产的质量趋势历史数据")
    public ResponseEntity<Map<String, Object>> getAssetQualityTrend(
            @PathVariable String assetId,
            @Parameter(description = "天数")
            @RequestParam(defaultValue = "30") int days) {
        
        List<QualityTrend> trends = qualityTrendService.getAssetQualityTrend(assetId, days);
        return ResponseEntity.ok(wrapResponse(trends));
    }

    /**
     * Get latest quality trends
     * GET /api/v1/analytics/quality/latest
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新质量趋势", description = "获取各检查类型的最新质量趋势")
    public ResponseEntity<Map<String, Object>> getLatestTrends() {
        
        List<QualityTrend> trends = qualityTrendService.getLatestTrends();
        return ResponseEntity.ok(wrapResponse(trends));
    }

    /**
     * Get quality summary statistics
     * GET /api/v1/analytics/quality/summary
     */
    @GetMapping("/summary")
    @Operation(summary = "获取质量汇总", description = "获取指定时间范围内的质量汇总统计")
    public ResponseEntity<Map<String, Object>> getQualitySummary(
            @Parameter(description = "开始时间")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Map<String, Object> summary = qualityTrendService.getQualitySummary(startTime, endTime);
        return ResponseEntity.ok(wrapResponse(summary));
    }

    /**
     * Record quality trend data
     * POST /api/v1/analytics/quality/trend
     */
    @PostMapping("/trend")
    @Operation(summary = "记录质量趋势", description = "记录单个质量检查的趋势数据")
    public ResponseEntity<Map<String, Object>> recordQualityTrend(
            @RequestBody QualityTrend trend,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String executor) {
        
        qualityTrendService.saveQualityTrend(trend);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wrapResponse(Map.of("message", "Quality trend recorded successfully")));
    }

    /**
     * Batch record quality trends
     * POST /api/v1/analytics/quality/trend/batch
     */
    @PostMapping("/trend/batch")
    @Operation(summary = "批量记录质量趋势", description = "批量记录多个质量检查的趋势数据")
    public ResponseEntity<Map<String, Object>> batchRecordQualityTrends(
            @RequestBody List<QualityTrend> trends,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String executor) {
        
        qualityTrendService.batchSaveQualityTrends(trends);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(wrapResponse(Map.of("message", "Batch recording accepted", 
                        "count", trends.size())));
    }

    /**
     * Get quality comparison between periods
     * GET /api/v1/analytics/quality/compare
     */
    @GetMapping("/compare")
    @Operation(summary = "对比质量趋势", description = "对比两个时间段的的质量趋势变化")
    public ResponseEntity<Map<String, Object>> compareQualityTrends(
            @Parameter(description = "第一个时间段开始")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime period1Start,
            @Parameter(description = "第一个时间段结束")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime period1End,
            @Parameter(description = "第二个时间段开始")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime period2Start,
            @Parameter(description = "第二个时间段结束")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime period2End) {
        
        QualityTrendResponse period1 = qualityTrendService.getQualityTrend(null, period1Start, period1End);
        QualityTrendResponse period2 = qualityTrendService.getQualityTrend(null, period2Start, period2End);
        
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("period1", period1);
        comparison.put("period2", period2);
        
        // Calculate changes
        Map<String, Object> changes = new HashMap<>();
        changes.put("passRateChange", period2.getAvgPassRate() - period1.getAvgPassRate());
        changes.put("scoreChange", period2.getAvgScore() - period1.getAvgScore());
        changes.put("checkCountChange", period2.getTotalChecks() - period1.getTotalChecks());
        comparison.put("changes", changes);
        
        return ResponseEntity.ok(wrapResponse(comparison));
    }

    /**
     * Health check endpoint
     * GET /api/v1/analytics/quality/health
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查质量趋势服务健康状态")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "quality-trend-service");
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
