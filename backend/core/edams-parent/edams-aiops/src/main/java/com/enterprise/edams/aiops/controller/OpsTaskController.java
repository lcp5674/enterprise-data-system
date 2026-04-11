package com.enterprise.edams.aiops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.aiops.entity.OpsTask;
import com.enterprise.edams.aiops.service.OpsTaskService;
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
 * 运维任务控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Tag(name = "运维任务", description = "运维任务相关接口")
@RestController
@RequestMapping("/api/aiops/task")
@RequiredArgsConstructor
public class OpsTaskController {

    private final OpsTaskService opsTaskService;

    @Operation(summary = "创建运维任务")
    @PostMapping
    public R<OpsTask> createTask(@RequestBody OpsTask task) {
        return R.ok(opsTaskService.createTask(task));
    }

    @Operation(summary = "更新运维任务")
    @PutMapping("/{id}")
    public R<OpsTask> updateTask(@PathVariable Long id, @RequestBody OpsTask task) {
        task.setId(id);
        return R.ok(opsTaskService.updateTask(task));
    }

    @Operation(summary = "删除运维任务")
    @DeleteMapping("/{id}")
    public R<Void> deleteTask(@PathVariable Long id) {
        opsTaskService.deleteTask(id);
        return R.ok();
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public R<OpsTask> getTaskById(@PathVariable Long id) {
        return R.ok(opsTaskService.getTaskById(id));
    }

    @Operation(summary = "分页查询任务")
    @GetMapping("/page")
    public R<Page<OpsTask>> pageTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String taskStatus,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return R.ok(opsTaskService.pageTasks(pageNum, pageSize, taskType, taskStatus, targetId, startTime, endTime));
    }

    @Operation(summary = "提交任务")
    @PostMapping("/{id}/submit")
    public R<Void> submitTask(@PathVariable Long id, @RequestParam String submittedBy) {
        opsTaskService.submitTask(id, submittedBy);
        return R.ok();
    }

    @Operation(summary = "开始执行任务")
    @PostMapping("/{id}/start")
    public R<Void> startTask(@PathVariable Long id) {
        opsTaskService.startTask(id);
        return R.ok();
    }

    @Operation(summary = "更新任务进度")
    @PostMapping("/{id}/progress")
    public R<Void> updateProgress(@PathVariable Long id, @RequestParam int progress) {
        opsTaskService.updateProgress(id, progress);
        return R.ok();
    }

    @Operation(summary = "完成任务")
    @PostMapping("/{id}/complete")
    public R<Void> completeTask(@PathVariable Long id,
                                @RequestParam boolean success,
                                @RequestParam(required = false) String result,
                                @RequestParam(required = false) String outputLog) {
        opsTaskService.completeTask(id, success, result, outputLog);
        return R.ok();
    }

    @Operation(summary = "取消任务")
    @PostMapping("/{id}/cancel")
    public R<Void> cancelTask(@PathVariable Long id, @RequestParam String reason) {
        opsTaskService.cancelTask(id, reason);
        return R.ok();
    }

    @Operation(summary = "重试任务")
    @PostMapping("/{id}/retry")
    public R<Void> retryTask(@PathVariable Long id) {
        opsTaskService.retryTask(id);
        return R.ok();
    }

    @Operation(summary = "获取待执行任务")
    @GetMapping("/pending")
    public R<List<OpsTask>> getPendingTasks() {
        return R.ok(opsTaskService.getPendingTasks());
    }

    @Operation(summary = "获取执行中任务")
    @GetMapping("/running")
    public R<List<OpsTask>> getRunningTasks() {
        return R.ok(opsTaskService.getRunningTasks());
    }

    @Operation(summary = "获取超时任务")
    @GetMapping("/overdue")
    public R<List<OpsTask>> getOverdueTasks() {
        return R.ok(opsTaskService.getOverdueTasks());
    }

    @Operation(summary = "按类型统计任务")
    @GetMapping("/stats/type")
    public R<List<Map<String, Object>>> countByType() {
        return R.ok(opsTaskService.countByType());
    }

    @Operation(summary = "触发任务调度")
    @PostMapping("/schedule")
    public R<Void> triggerSchedule() {
        opsTaskService.schedulePendingTasks();
        return R.ok();
    }
}
