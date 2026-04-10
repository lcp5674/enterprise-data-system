package com.enterprise.edams.aiops.controller;

import com.enterprise.edams.aiops.dto.CapacityForecastResponse;
import com.enterprise.edams.aiops.service.CapacityPlanningService;
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

/**
 * 容量规划控制器
 * 
 * 提供容量规划预测相关API接口
 *
 * @author AIOps Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/aiops/capacity")
@Tag(name = "容量规划", description = "容量规划预测相关接口")
public class CapacityPlanningController {

    private final CapacityPlanningService capacityPlanningService;

    /**
     * 预测容量需求
     */
    @GetMapping("/forecast")
    @Operation(summary = "容量预测", description = "基于历史数据预测资源容量需求")
    public ResponseEntity<Map<String, Object>> forecastCapacity(
            @Parameter(description = "资源类型: CPU, MEMORY, DISK, NETWORK") 
            @RequestParam String resourceType,
            @Parameter(description = "资源名称") 
            @RequestParam(required = false) String resourceName,
            @Parameter(description = "服务名称") 
            @RequestParam(required = false) String serviceName,
            @Parameter(description = "预测天数") 
            @RequestParam(defaultValue = "30") int forecastDays) {
        
        log.info("收到容量预测请求 - 类型: {}, 资源: {}, 服务: {}, 天数: {}", 
                resourceType, resourceName, serviceName, forecastDays);
        
        if (resourceName == null) {
            resourceName = resourceType.toLowerCase() + "_usage";
        }
        if (serviceName == null) {
            serviceName = "default-service";
        }
        
        CapacityForecastResponse forecast = capacityPlanningService.forecastCapacity(
                resourceType, resourceName, serviceName, forecastDays);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("forecast", forecast);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 批量预测多资源容量
     */
    @GetMapping("/forecast/multi")
    @Operation(summary = "多资源容量预测", description = "同时预测多种资源的容量需求")
    public ResponseEntity<Map<String, Object>> forecastMultipleResources(
            @Parameter(description = "服务名称") 
            @RequestParam String serviceName,
            @Parameter(description = "预测天数") 
            @RequestParam(defaultValue = "30") int forecastDays) {
        
        log.info("收到多资源容量预测请求 - 服务: {}, 天数: {}", serviceName, forecastDays);
        
        List<CapacityForecastResponse> forecasts = capacityPlanningService.forecastMultipleResources(
                serviceName, forecastDays);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("serviceName", serviceName);
        response.put("forecasts", forecasts);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取容量概览
     */
    @GetMapping("/overview")
    @Operation(summary = "容量概览", description = "获取所有资源的容量使用概览")
    public ResponseEntity<Map<String, Object>> getCapacityOverview(
            @Parameter(description = "服务名称") 
            @RequestParam(required = false) String serviceName) {
        
        Map<String, Object> overview = new HashMap<>();
        
        // CPU概览
        Map<String, Object> cpuOverview = new HashMap<>();
        cpuOverview.put("totalCapacity", 100);
        cpuOverview.put("currentUsage", 42.5);
        cpuOverview.put("usagePercentage", 42.5);
        cpuOverview.put("trend", "STABLE");
        cpuOverview.put("riskLevel", "LOW");
        
        // Memory概览
        Map<String, Object> memoryOverview = new HashMap<>();
        memoryOverview.put("totalCapacity", 128);
        memoryOverview.put("currentUsage", 85.2);
        memoryOverview.put("usagePercentage", 66.5);
        memoryOverview.put("trend", "INCREASING");
        memoryOverview.put("riskLevel", "MEDIUM");
        
        // Disk概览
        Map<String, Object> diskOverview = new HashMap<>();
        diskOverview.put("totalCapacity", 1000);
        diskOverview.put("currentUsage", 450);
        diskOverview.put("usagePercentage", 45.0);
        diskOverview.put("trend", "STABLE");
        diskOverview.put("riskLevel", "LOW");
        
        overview.put("cpu", cpuOverview);
        overview.put("memory", memoryOverview);
        overview.put("disk", diskOverview);
        overview.put("timestamp", LocalDateTime.now());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("serviceName", serviceName != null ? serviceName : "all-services");
        response.put("overview", overview);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取即将耗尽的资源
     */
    @GetMapping("/exhaustion/warnings")
    @Operation(summary = "容量耗尽警告", description = "获取即将达到容量上限的资源列表")
    public ResponseEntity<Map<String, Object>> getExhaustionWarnings(
            @Parameter(description = "警告阈值(百分比)") 
            @RequestParam(defaultValue = "80") int threshold) {
        
        log.info("查询容量耗尽警告 - 阈值: {}%", threshold);
        
        List<Map<String, Object>> warnings = List.of(
                Map.of(
                        "resourceType", "MEMORY",
                        "resourceName", "heap_memory",
                        "currentUsage", 95.5,
                        "estimatedDaysRemaining", 7,
                        "riskLevel", "CRITICAL",
                        "recommendedAction", "立即扩容"
                ),
                Map.of(
                        "resourceType", "CONNECTION",
                        "resourceName", "database_connections",
                        "currentUsage", 85.0,
                        "estimatedDaysRemaining", 14,
                        "riskLevel", "HIGH",
                        "recommendedAction", "优化连接池配置"
                )
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("threshold", threshold);
        response.put("warningCount", warnings.size());
        response.put("warnings", warnings);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取扩容建议
     */
    @GetMapping("/recommendations")
    @Operation(summary = "扩容建议", description = "获取资源扩容建议列表")
    public ResponseEntity<Map<String, Object>> getScaleUpRecommendations(
            @Parameter(description = "服务名称") 
            @RequestParam(required = false) String serviceName) {
        
        List<Map<String, Object>> recommendations = List.of(
                Map.of(
                        "resourceType", "MEMORY",
                        "currentCapacity", 128,
                        "recommendedCapacity", 192,
                        "scaleUpPercentage", 50,
                        "priority", "HIGH",
                        "reason", "预计30天后将达到容量上限",
                        "estimatedCost", "+¥2000/月"
                ),
                Map.of(
                        "resourceType", "CPU",
                        "currentCapacity", 16,
                        "recommendedCapacity", 24,
                        "scaleUpPercentage", 50,
                        "priority", "MEDIUM",
                        "reason", "负载高峰时CPU使用率达到85%",
                        "estimatedCost", "+¥1500/月"
                )
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("serviceName", serviceName != null ? serviceName : "all-services");
        response.put("recommendations", recommendations);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}
