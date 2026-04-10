package com.enterprise.edams.aiops.controller;

import com.enterprise.edams.aiops.service.RootCauseAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 根因分析控制器
 * 
 * 提供根因分析相关API接口
 *
 * @author AIOps Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/aiops/rca")
@Tag(name = "根因分析", description = "根因分析相关接口")
public class RootCauseAnalysisController {

    private final RootCauseAnalysisService rootCauseAnalysisService;

    /**
     * 执行根因分析
     */
    @PostMapping("/analyze")
    @Operation(summary = "执行根因分析", description = "对指定事件进行根因分析")
    public ResponseEntity<Map<String, Object>> analyzeRootCause(
            @Parameter(description = "事件ID") 
            @RequestParam String incidentId,
            @Parameter(description = "服务名称") 
            @RequestParam String serviceName,
            @Parameter(description = "分析开始时间") 
            @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "分析结束时间") 
            @RequestParam(required = false) LocalDateTime endTime) {
        
        if (startTime == null) {
            startTime = LocalDateTime.now().minusHours(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        
        log.info("收到根因分析请求 - 事件ID: {}, 服务: {}, 时间范围: {} ~ {}", 
                incidentId, serviceName, startTime, endTime);
        
        RootCauseAnalysisService.RootCauseResult result = rootCauseAnalysisService.analyzeRootCause(
                incidentId, serviceName, startTime, endTime);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("result", result);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取根因分析历史
     */
    @GetMapping("/history")
    @Operation(summary = "获取分析历史", description = "获取历史根因分析记录")
    public ResponseEntity<Map<String, Object>> getAnalysisHistory(
            @Parameter(description = "服务名称") 
            @RequestParam(required = false) String serviceName,
            @Parameter(description = "分析数量限制") 
            @RequestParam(defaultValue = "10") int limit) {
        
        // 模拟历史记录
        List<Map<String, Object>> history = List.of(
                createMockHistoryRecord("INC001", "user-service", "DATABASE", 0.92),
                createMockHistoryRecord("INC002", "api-gateway", "NETWORK", 0.85),
                createMockHistoryRecord("INC003", "data-processing", "MEMORY", 0.78),
                createMockHistoryRecord("INC004", "notification-service", "DEPENDENCY", 0.71)
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("serviceName", serviceName != null ? serviceName : "all");
        response.put("history", history);
        response.put("count", history.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取服务血缘关系图
     */
    @GetMapping("/lineage/{serviceName}")
    @Operation(summary = "获取血缘关系", description = "获取服务的上下游依赖关系")
    public ResponseEntity<Map<String, Object>> getLineageGraph(
            @Parameter(description = "服务名称") 
            @PathVariable String serviceName) {
        
        Map<String, Object> lineageGraph = rootCauseAnalysisService.getLineageGraph(serviceName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("serviceName", serviceName);
        response.put("lineageGraph", lineageGraph);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取问题定位建议
     */
    @GetMapping("/suggestions")
    @Operation(summary = "获取定位建议", description = "基于当前系统状态获取问题定位建议")
    public ResponseEntity<Map<String, Object>> getProblemSuggestions(
            @Parameter(description = "问题类型") 
            @RequestParam(required = false) String problemType) {
        
        List<Map<String, Object>> suggestions = List.of(
                Map.of(
                        "step", 1,
                        "action", "检查系统日志",
                        "command", "kubectl logs <pod-name> --tail=100",
                        "description", "查看最近的系统日志"
                ),
                Map.of(
                        "step", 2,
                        "action", "检查资源使用情况",
                        "command", "kubectl top pods",
                        "description", "查看各Pod的资源使用情况"
                ),
                Map.of(
                        "step", 3,
                        "action", "检查网络连接",
                        "command", "netstat -an | grep ESTABLISHED",
                        "description", "检查当前网络连接状态"
                ),
                Map.of(
                        "step", 4,
                        "action", "检查数据库连接",
                        "command", "SELECT * FROM pg_stat_activity",
                        "description", "查看当前数据库连接"
                )
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("problemType", problemType != null ? problemType : "general");
        response.put("suggestions", suggestions);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取影响分析
     */
    @GetMapping("/impact/{incidentId}")
    @Operation(summary = "获取影响分析", description = "获取指定事件的影响范围分析")
    public ResponseEntity<Map<String, Object>> getImpactAnalysis(
            @Parameter(description = "事件ID") 
            @PathVariable String incidentId) {
        
        List<Map<String, Object>> impacts = List.of(
                Map.of(
                        "affectedService", "api-gateway",
                        "impactType", "调用失败",
                        "severity", "HIGH",
                        "affectedUsers", 1250,
                        "duration", "5分钟"
                ),
                Map.of(
                        "affectedService", "notification-service",
                        "impactType", "队列积压",
                        "severity", "MEDIUM",
                        "affectedUsers", 4500,
                        "duration", "15分钟"
                ),
                Map.of(
                        "affectedService", "user-dashboard",
                        "impactType", "响应延迟",
                        "severity", "LOW",
                        "affectedUsers", 8200,
                        "duration", "持续"
                )
        );
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAffectedServices", impacts.size());
        summary.put("totalAffectedUsers", impacts.stream()
                .mapToInt(m -> (Integer) m.get("affectedUsers"))
                .sum());
        summary.put("estimatedDowntime", "20分钟");
        summary.put("businessImpact", "中等 - 部分用户操作受影响");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("incidentId", incidentId);
        response.put("impacts", impacts);
        response.put("summary", summary);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 创建模拟历史记录
     */
    private Map<String, Object> createMockHistoryRecord(String incidentId, String serviceName, 
                                                          String causeType, double confidence) {
        Map<String, Object> record = new HashMap<>();
        record.put("incidentId", incidentId);
        record.put("serviceName", serviceName);
        record.put("rootCause", causeType + "_ISSUE");
        record.put("confidence", confidence);
        record.put("analyzedAt", LocalDateTime.now().minusDays((long)(Math.random() * 7)));
        record.put("affectedServicesCount", (int)(Math.random() * 5) + 1);
        record.put("resolutionTime", (int)(Math.random() * 60) + 10 + "分钟");
        return record;
    }
}
