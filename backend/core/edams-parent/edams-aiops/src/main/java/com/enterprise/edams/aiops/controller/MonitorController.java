package com.enterprise.edams.aiops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.aiops.entity.MonitorMetric;
import com.enterprise.edams.aiops.service.MonitorService;
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
 * 监控控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Tag(name = "监控管理", description = "监控指标相关接口")
@RestController
@RequestMapping("/api/aiops/metric")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    @Operation(summary = "创建监控指标")
    @PostMapping
    public R<MonitorMetric> createMetric(@RequestBody MonitorMetric metric) {
        return R.ok(monitorService.createMetric(metric));
    }

    @Operation(summary = "更新监控指标")
    @PutMapping("/{id}")
    public R<MonitorMetric> updateMetric(@PathVariable Long id, @RequestBody MonitorMetric metric) {
        metric.setId(id);
        return R.ok(monitorService.updateMetric(metric));
    }

    @Operation(summary = "删除监控指标")
    @DeleteMapping("/{id}")
    public R<Void> deleteMetric(@PathVariable Long id) {
        monitorService.deleteMetric(id);
        return R.ok();
    }

    @Operation(summary = "获取监控指标详情")
    @GetMapping("/{id}")
    public R<MonitorMetric> getMetricById(@PathVariable Long id) {
        return R.ok(monitorService.getMetricById(id));
    }

    @Operation(summary = "分页查询监控指标")
    @GetMapping("/page")
    public R<Page<MonitorMetric>> pageMetrics(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) String metricType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return R.ok(monitorService.pageMetrics(pageNum, pageSize, targetId, metricType, startTime, endTime));
    }

    @Operation(summary = "获取最新指标")
    @GetMapping("/latest")
    public R<MonitorMetric> getLatestMetric(
            @RequestParam String targetId,
            @RequestParam(required = false) String metricName) {
        return R.ok(monitorService.getLatestMetric(targetId, metricName));
    }

    @Operation(summary = "获取时间范围内指标")
    @GetMapping("/range")
    public R<List<MonitorMetric>> getMetricsByTimeRange(
            @RequestParam String targetId,
            @RequestParam(required = false) String metricName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return R.ok(monitorService.getMetricsByTimeRange(targetId, metricName, startTime, endTime));
    }

    @Operation(summary = "按类型统计指标")
    @GetMapping("/stats/type")
    public R<List<Map<String, Object>>> countByMetricType() {
        return R.ok(monitorService.countByMetricType());
    }

    @Operation(summary = "批量创建指标")
    @PostMapping("/batch")
    public R<Void> batchCreateMetrics(@RequestBody List<MonitorMetric> metrics) {
        monitorService.batchCreateMetrics(metrics);
        return R.ok();
    }

    @Operation(summary = "清理过期指标")
    @DeleteMapping("/cleanup")
    public R<Integer> cleanExpiredMetrics(@RequestParam(defaultValue = "30") int retentionDays) {
        return R.ok(monitorService.cleanExpiredMetrics(retentionDays));
    }
}
