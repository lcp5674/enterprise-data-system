package com.enterprise.edams.aiops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.aiops.entity.Alert;
import com.enterprise.edams.aiops.entity.AlertRule;
import com.enterprise.edams.aiops.service.AlertService;
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
 * 告警控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Tag(name = "告警管理", description = "告警及规则相关接口")
@RestController
@RequestMapping("/api/aiops/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @Operation(summary = "创建告警")
    @PostMapping
    public R<Alert> createAlert(@RequestBody Alert alert) {
        return R.ok(alertService.createAlert(alert));
    }

    @Operation(summary = "更新告警")
    @PutMapping("/{id}")
    public R<Alert> updateAlert(@PathVariable Long id, @RequestBody Alert alert) {
        alert.setId(id);
        return R.ok(alertService.updateAlert(alert));
    }

    @Operation(summary = "删除告警")
    @DeleteMapping("/{id}")
    public R<Void> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return R.ok();
    }

    @Operation(summary = "获取告警详情")
    @GetMapping("/{id}")
    public R<Alert> getAlertById(@PathVariable Long id) {
        return R.ok(alertService.getAlertById(id));
    }

    @Operation(summary = "分页查询告警")
    @GetMapping("/page")
    public R<Page<Alert>> pageAlerts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) String alertStatus,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return R.ok(alertService.pageAlerts(pageNum, pageSize, alertLevel, alertStatus, targetId, startTime, endTime));
    }

    @Operation(summary = "确认告警")
    @PostMapping("/{id}/acknowledge")
    public R<Void> acknowledgeAlert(@PathVariable Long id, @RequestParam String ackBy) {
        alertService.acknowledgeAlert(id, ackBy);
        return R.ok();
    }

    @Operation(summary = "解决告警")
    @PostMapping("/{id}/resolve")
    public R<Void> resolveAlert(@PathVariable Long id, @RequestParam String resolveBy, @RequestParam(required = false) String solution) {
        alertService.resolveAlert(id, resolveBy, solution);
        return R.ok();
    }

    @Operation(summary = "关闭告警")
    @PostMapping("/{id}/close")
    public R<Void> closeAlert(@PathVariable Long id, @RequestParam String closedBy) {
        alertService.closeAlert(id, closedBy);
        return R.ok();
    }

    @Operation(summary = "获取待处理告警")
    @GetMapping("/pending")
    public R<List<Alert>> getPendingAlerts() {
        return R.ok(alertService.getPendingAlerts());
    }

    @Operation(summary = "获取活跃告警")
    @GetMapping("/active")
    public R<List<Alert>> getActiveAlerts(@RequestParam(required = false) String targetId) {
        return R.ok(alertService.getActiveAlerts(targetId));
    }

    @Operation(summary = "按级别统计告警")
    @GetMapping("/stats/level")
    public R<List<Map<String, Object>>> countByLevel() {
        return R.ok(alertService.countByLevel());
    }

    // ==================== 告警规则接口 ====================

    @Operation(summary = "创建告警规则")
    @PostMapping("/rule")
    public R<AlertRule> createAlertRule(@RequestBody AlertRule rule) {
        return R.ok(alertService.createAlertRule(rule));
    }

    @Operation(summary = "更新告警规则")
    @PutMapping("/rule/{id}")
    public R<AlertRule> updateAlertRule(@PathVariable Long id, @RequestBody AlertRule rule) {
        rule.setId(id);
        return R.ok(alertService.updateAlertRule(rule));
    }

    @Operation(summary = "删除告警规则")
    @DeleteMapping("/rule/{id}")
    public R<Void> deleteAlertRule(@PathVariable Long id) {
        alertService.deleteAlertRule(id);
        return R.ok();
    }

    @Operation(summary = "启用/禁用告警规则")
    @PostMapping("/rule/{id}/toggle")
    public R<Void> toggleRule(@PathVariable Long id, @RequestParam boolean enabled) {
        alertService.toggleRule(id, enabled);
        return R.ok();
    }

    @Operation(summary = "评估告警规则")
    @PostMapping("/rule/evaluate")
    public R<Void> evaluateRules() {
        alertService.evaluateRules();
        return R.ok();
    }

    @Operation(summary = "查询告警规则")
    @GetMapping("/rule")
    public R<List<AlertRule>> getAlertRules(
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) String metricName,
            @RequestParam(required = false) Boolean enabled) {
        return R.ok(alertService.getAlertRules(targetId, metricName, enabled));
    }
}
