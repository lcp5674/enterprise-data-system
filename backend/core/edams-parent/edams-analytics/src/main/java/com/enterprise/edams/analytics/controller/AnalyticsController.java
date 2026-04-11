package com.enterprise.edams.analytics.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.analytics.entity.AnalysisTask;
import com.enterprise.edams.analytics.service.AnalysisTaskService;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分析任务控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analytics/tasks")
@RequiredArgsConstructor
@Tag(name = "分析任务", description = "分析任务的CRUD和执行接口")
public class AnalyticsController {

    private final AnalysisTaskService taskService;

    /**
     * 创建任务
     */
    @PostMapping
    @Operation(summary = "创建分析任务")
    public Result<AnalysisTask> createTask(@RequestBody AnalysisTask task) {
        AnalysisTask created = taskService.createTask(task);
        return Result.success(created);
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新分析任务")
    public Result<AnalysisTask> updateTask(
            @PathVariable Long id,
            @RequestBody AnalysisTask task) {
        AnalysisTask updated = taskService.updateTask(id, task);
        return Result.success(updated);
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分析任务")
    public Result<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return Result.success();
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取任务详情")
    public Result<AnalysisTask> getTaskById(@PathVariable Long id) {
        AnalysisTask task = taskService.getTaskById(id);
        return Result.success(task);
    }

    /**
     * 分页查询任务
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询任务")
    public PageResult<AnalysisTask> queryTasks(
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<AnalysisTask> page = taskService.queryTasks(taskName, taskType, status, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 根据类型查询任务
     */
    @GetMapping("/type/{taskType}")
    @Operation(summary = "根据类型查询任务")
    public Result<List<AnalysisTask>> getTasksByType(@PathVariable String taskType) {
        List<AnalysisTask> tasks = taskService.getTasksByType(taskType);
        return Result.success(tasks);
    }

    /**
     * 根据状态查询任务
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询任务")
    public Result<List<AnalysisTask>> getTasksByStatus(@PathVariable String status) {
        List<AnalysisTask> tasks = taskService.getTasksByStatus(status);
        return Result.success(tasks);
    }

    /**
     * 执行任务
     */
    @PostMapping("/{id}/execute")
    @Operation(summary = "执行任务")
    public Result<Void> executeTask(@PathVariable Long id) {
        taskService.executeTask(id);
        return Result.success();
    }

    /**
     * 取消任务
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消任务")
    public Result<Void> cancelTask(@PathVariable Long id) {
        taskService.cancelTask(id);
        return Result.success();
    }

    /**
     * 批量执行任务
     */
    @PostMapping("/batch-execute")
    @Operation(summary = "批量执行任务")
    public Result<Void> batchExecuteTasks(@RequestBody List<Long> taskIds) {
        taskService.batchExecuteTasks(taskIds);
        return Result.success();
    }

    /**
     * 获取待执行任务
     */
    @GetMapping("/pending")
    @Operation(summary = "获取待执行任务")
    public Result<List<AnalysisTask>> getPendingTasks(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<AnalysisTask> tasks = taskService.getPendingTasks(limit);
        return Result.success(tasks);
    }

    /**
     * 统计任务数量
     */
    @GetMapping("/count/{status}")
    @Operation(summary = "统计任务数量")
    public Result<Long> countByStatus(@PathVariable String status) {
        long count = taskService.countByStatus(status);
        return Result.success(count);
    }
}
