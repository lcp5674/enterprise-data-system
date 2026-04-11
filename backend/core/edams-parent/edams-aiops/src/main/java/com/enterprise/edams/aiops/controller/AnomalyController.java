package com.enterprise.edams.aiops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.aiops.entity.AnomalyRecord;
import com.enterprise.edams.aiops.service.AnomalyDetectionService;
import com.enterprise.edams.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 异常检测控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Tag(name = "异常检测", description = "异常检测相关接口")
@RestController
@RequestMapping("/api/aiops/anomaly")
@RequiredArgsConstructor
public class AnomalyController {

    private final AnomalyDetectionService anomalyDetectionService;

    @Operation(summary = "检测异常")
    @PostMapping("/detect/{metricId}")
    public R<AnomalyRecord> detectAnomaly(@PathVariable Long metricId) {
        return R.ok(anomalyDetectionService.detectAnomaly(metricId));
    }

    @Operation(summary = "批量检测异常")
    @PostMapping("/detect/batch")
    public R<List<AnomalyRecord>> batchDetectAnomalies(@RequestBody List<Long> metricIds) {
        return R.ok(anomalyDetectionService.batchDetectAnomalies(metricIds));
    }

    @Operation(summary = "更新异常记录")
    @PutMapping("/{id}")
    public R<AnomalyRecord> updateAnomalyRecord(@PathVariable Long id, @RequestBody AnomalyRecord record) {
        record.setId(id);
        return R.ok(anomalyDetectionService.updateAnomalyRecord(record));
    }

    @Operation(summary = "删除异常记录")
    @DeleteMapping("/{id}")
    public R<Void> deleteAnomalyRecord(@PathVariable Long id) {
        anomalyDetectionService.deleteAnomalyRecord(id);
        return R.ok();
    }

    @Operation(summary = "获取异常详情")
    @GetMapping("/{id}")
    public R<AnomalyRecord> getAnomalyById(@PathVariable Long id) {
        return R.ok(anomalyDetectionService.getAnomalyById(id));
    }

    @Operation(summary = "分页查询异常")
    @GetMapping("/page")
    public R<Page<AnomalyRecord>> pageAnomalies(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return R.ok(anomalyDetectionService.pageAnomalies(pageNum, pageSize, severity, status, targetId, startTime, endTime));
    }

    @Operation(summary = "获取活跃异常")
    @GetMapping("/active")
    public R<List<AnomalyRecord>> getActiveAnomalies() {
        return R.ok(anomalyDetectionService.getActiveAnomalies());
    }

    @Operation(summary = "获取持续中的异常")
    @GetMapping("/ongoing")
    public R<List<AnomalyRecord>> getOngoingAnomalies() {
        return R.ok(anomalyDetectionService.getOngoingAnomalies());
    }

    @Operation(summary = "分析异常原因")
    @GetMapping("/{id}/analyze")
    public R<String> analyzeAnomaly(@PathVariable Long id) {
        return R.ok(anomalyDetectionService.analyzeAnomaly(id));
    }

    @Operation(summary = "解决异常")
    @PostMapping("/{id}/resolve")
    public R<Void> resolveAnomaly(@PathVariable Long id, @RequestParam(required = false) String resolution) {
        anomalyDetectionService.resolveAnomaly(id, resolution);
        return R.ok();
    }

    @Operation(summary = "忽略异常")
    @PostMapping("/{id}/ignore")
    public R<Void> ignoreAnomaly(@PathVariable Long id, @RequestParam String reason) {
        anomalyDetectionService.ignoreAnomaly(id, reason);
        return R.ok();
    }

    @Operation(summary = "按类型统计异常")
    @GetMapping("/stats/type")
    public R<List<Map<String, Object>>> countByType() {
        return R.ok(anomalyDetectionService.countByType());
    }

    @Operation(summary = "获取高置信度异常")
    @GetMapping("/high-confidence")
    public R<List<AnomalyRecord>> getHighConfidenceAnomalies(
            @RequestParam(defaultValue = "0.8") double minConfidence) {
        return R.ok(anomalyDetectionService.getHighConfidenceAnomalies(minConfidence));
    }

    @Operation(summary = "触发定时检测")
    @PostMapping("/scheduled-detection")
    public R<Void> triggerScheduledDetection() {
        anomalyDetectionService.scheduledAnomalyDetection();
        return R.ok();
    }
}
