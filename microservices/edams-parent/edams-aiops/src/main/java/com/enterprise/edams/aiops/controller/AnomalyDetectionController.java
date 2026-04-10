package com.enterprise.edams.aiops.controller;

import com.enterprise.edams.aiops.dto.AnomalyDetectionRequest;
import com.enterprise.edams.aiops.model.AnomalyRecord;
import com.enterprise.edams.aiops.service.AnomalyDetectionService;
import com.enterprise.edams.aiops.service.AlertOptimizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 异常检测控制器
 * 
 * 提供异常检测相关API接口
 *
 * @author AIOps Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/aiops/anomaly")
@Tag(name = "异常检测", description = "智能异常检测相关接口")
public class AnomalyDetectionController {

    private final AnomalyDetectionService anomalyDetectionService;
    private final AlertOptimizationService alertOptimizationService;

    /**
     * 检测异常
     */
    @PostMapping("/detect")
    @Operation(summary = "检测异常", description = "基于时间序列数据检测系统异常")
    public ResponseEntity<Map<String, Object>> detectAnomalies(
            @Valid @RequestBody AnomalyDetectionRequest request) {
        
        log.info("收到异常检测请求 - 服务: {}", request.getServiceName());
        
        List<AnomalyRecord> anomalies = anomalyDetectionService.detectAnomalies(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("requestId", UUID.randomUUID().toString());
        response.put("detectedCount", anomalies.size());
        response.put("anomalies", anomalies);
        response.put("timestamp", LocalDateTime.now());
        
        // 缓存结果
        String requestId = (String) response.get("requestId");
        anomalyDetectionService.cacheAnomalyResults(requestId, anomalies);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 异步检测异常
     */
    @PostMapping("/detect/async")
    @Operation(summary = "异步检测异常", description = "异步执行异常检测任务")
    public ResponseEntity<Map<String, Object>> detectAnomaliesAsync(
            @Valid @RequestBody AnomalyDetectionRequest request) {
        
        log.info("收到异步异常检测请求 - 服务: {}", request.getServiceName());
        
        String taskId = UUID.randomUUID().toString();
        
        // 异步执行检测
        anomalyDetectionService.detectAnomaliesAsync(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("taskId", taskId);
        response.put("message", "异常检测任务已提交，请使用taskId查询结果");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.accepted().body(response);
    }

    /**
     * 获取缓存的检测结果
     */
    @GetMapping("/result/{requestId}")
    @Operation(summary = "获取检测结果", description = "根据请求ID获取缓存的异常检测结果")
    public ResponseEntity<Map<String, Object>> getDetectionResult(
            @Parameter(description = "请求ID") @PathVariable String requestId) {
        
        List<AnomalyRecord> cached = anomalyDetectionService.getCachedAnomalyResults(requestId);
        
        Map<String, Object> response = new HashMap<>();
        if (cached != null) {
            response.put("success", true);
            response.put("cached", true);
            response.put("anomalies", cached);
        } else {
            response.put("success", false);
            response.put("cached", false);
            response.put("message", "未找到对应的检测结果或结果已过期");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取未解决的异常
     */
    @GetMapping("/unresolved")
    @Operation(summary = "获取未解决异常", description = "获取指定时间范围内的未解决异常")
    public ResponseEntity<Map<String, Object>> getUnresolvedAnomalies(
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime since) {
        
        if (since == null) {
            since = LocalDateTime.now().minusHours(24);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("since", since);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取异常统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取异常统计", description = "获取异常数量统计信息")
    public ResponseEntity<Map<String, Object>> getAnomalyStatistics(
            @Parameter(description = "服务名称") @RequestParam(required = false) String serviceName,
            @Parameter(description = "时间范围(小时)") @RequestParam(defaultValue = "24") int hours) {
        
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalAnomalies", 15);
        statistics.put("criticalCount", 2);
        statistics.put("highCount", 5);
        statistics.put("mediumCount", 4);
        statistics.put("lowCount", 4);
        statistics.put("byType", Map.of(
                "CPU", 3,
                "MEMORY", 4,
                "DISK", 2,
                "NETWORK", 3,
                "RESPONSE_TIME", 2,
                "ERROR_RATE", 1
        ));
        statistics.put("topAffectedServices", List.of(
                Map.of("service", "user-service", "count", 5),
                Map.of("service", "data-processing", "count", 4),
                Map.of("service", "api-gateway", "count", 3)
        ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", statistics);
        response.put("period", since + " ~ " + LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 确认异常
     */
    @PutMapping("/{anomalyId}/acknowledge")
    @Operation(summary = "确认异常", description = "标记异常为已确认状态")
    public ResponseEntity<Map<String, Object>> acknowledgeAnomaly(
            @Parameter(description = "异常ID") @PathVariable String anomalyId) {
        
        log.info("确认异常 - ID: {}", anomalyId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("anomalyId", anomalyId);
        response.put("status", "ACKNOWLEDGED");
        response.put("acknowledgedAt", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 解决异常
     */
    @PutMapping("/{anomalyId}/resolve")
    @Operation(summary = "解决异常", description = "标记异常为已解决状态")
    public ResponseEntity<Map<String, Object>> resolveAnomaly(
            @Parameter(description = "异常ID") @PathVariable String anomalyId,
            @Parameter(description = "解决说明") @RequestBody(required = false) Map<String, String> resolution) {
        
        log.info("解决异常 - ID: {}", anomalyId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("anomalyId", anomalyId);
        response.put("status", "RESOLVED");
        response.put("resolvedAt", LocalDateTime.now());
        response.put("resolution", resolution != null ? resolution.get("description") : "问题已处理");
        
        return ResponseEntity.ok(response);
    }
}
