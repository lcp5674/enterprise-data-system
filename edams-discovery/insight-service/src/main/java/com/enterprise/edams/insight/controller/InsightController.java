package com.enterprise.edams.insight.controller;

import com.enterprise.edams.insight.entity.AnomalyDetection;
import com.enterprise.edams.insight.entity.TrendPrediction;
import com.enterprise.edams.insight.service.InsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能洞察控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    // ==================== 异常检测接口 ====================

    @PostMapping("/anomalies/detect/{assetId}")
    public ResponseEntity<Map<String, Object>> detectAnomalies(
            @PathVariable Long assetId,
            @RequestParam(defaultValue = "STATISTICAL") String detectionType) {
        List<AnomalyDetection> anomalies = insightService.detectAnomalies(assetId, detectionType);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", anomalies);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/anomalies")
    public ResponseEntity<Map<String, Object>> getAnomalyList(
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<AnomalyDetection> anomalies = insightService.getAnomalyList(assetId, status, severity, startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", anomalies);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/anomalies/{id}")
    public ResponseEntity<Map<String, Object>> getAnomalyDetail(@PathVariable Long id) {
        AnomalyDetection anomaly = insightService.getAnomalyDetail(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", anomaly);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/anomalies/{id}/handle")
    public ResponseEntity<Map<String, Object>> handleAnomaly(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String remark) {
        insightService.handleAnomaly(id, status, remark);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "异常处理成功");
        return ResponseEntity.ok(result);
    }

    // ==================== 趋势预测接口 ====================

    @PostMapping("/predictions/{assetId}")
    public ResponseEntity<Map<String, Object>> generatePrediction(
            @PathVariable Long assetId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        TrendPrediction prediction = insightService.generatePrediction(assetId, startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", prediction);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/predictions")
    public ResponseEntity<Map<String, Object>> getPredictionList(@RequestParam(required = false) Long assetId) {
        List<TrendPrediction> predictions = insightService.getPredictionList(assetId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", predictions);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/predictions/{id}")
    public ResponseEntity<Map<String, Object>> getPredictionDetail(@PathVariable Long id) {
        TrendPrediction prediction = insightService.getPredictionDetail(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", prediction);
        return ResponseEntity.ok(result);
    }

    // ==================== 智能推荐接口 ====================

    @GetMapping("/recommendations/{assetId}")
    public ResponseEntity<Map<String, Object>> getRecommendations(
            @PathVariable Long assetId,
            @RequestParam(defaultValue = "RELATED") String recommendationType) {
        List<Map<String, Object>> recommendations = insightService.getRecommendations(assetId, recommendationType);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", recommendations);
        return ResponseEntity.ok(result);
    }

    // ==================== 统计接口 ====================

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = insightService.getStatistics();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", statistics);
        return ResponseEntity.ok(result);
    }
}
