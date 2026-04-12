package com.enterprise.dataplatform.governance.controller;

import com.enterprise.dataplatform.governance.domain.entity.GovernanceTask;
import com.enterprise.dataplatform.governance.dto.request.GovernanceTaskRequest;
import com.enterprise.dataplatform.governance.dto.response.GovernanceTaskResponse;
import com.enterprise.dataplatform.governance.service.GovernanceOrchestrationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/governance/tasks")
@RequiredArgsConstructor
@Tag(name = "治理任务管理", description = "治理任务的创建、调度和执行管理")
public class GovernanceTaskController {

    private final GovernanceOrchestrationService orchestrationService;

    @PostMapping
    @Operation(summary = "创建治理任务", description = "创建新的治理任务")
    public ResponseEntity<GovernanceTaskResponse> createTask(
            @Validated @RequestBody GovernanceTaskRequest request) {
        log.info("创建治理任务: {}", request.getTaskName());
        GovernanceTask task = orchestrationService.createTask(request);
        return ResponseEntity.ok(toTaskResponse(task));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新治理任务", description = "更新现有治理任务")
    public ResponseEntity<GovernanceTaskResponse> updateTask(
            @PathVariable Long id,
            @Validated @RequestBody GovernanceTaskRequest request) {
        log.info("更新治理任务: {}", id);
        GovernanceTask task = orchestrationService.updateTask(id, request);
        return ResponseEntity.ok(toTaskResponse(task));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除治理任务", description = "删除治理任务")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("删除治理任务: {}", id);
        orchestrationService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取任务详情", description = "根据ID获取任务详情")
    public ResponseEntity<GovernanceTaskResponse> getTask(@PathVariable Long id) {
        log.info("获取任务详情: {}", id);
        return orchestrationService.getTaskById(id)
                .map(this::toTaskResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "分页查询任务", description = "分页查询治理任务")
    public ResponseEntity<IPage<GovernanceTaskResponse>> listTasks(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long policyId,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) String keyword) {
        log.info("分页查询任务: pageNum={}, pageSize={}, status={}", pageNum, pageSize, status);
        Page<GovernanceTask> page = orchestrationService.listTasks(
                pageNum, pageSize, taskType, status, policyId, assignedTo, keyword);
        IPage<GovernanceTaskResponse> responsePage = page.convert(this::toTaskResponse);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待执行任务", description = "获取待执行的任务列表")
    public ResponseEntity<List<GovernanceTaskResponse>> getPendingTasks(
            @RequestParam(required = false) String assignedTo,
            @RequestParam(defaultValue = "50") Integer limit) {
        log.info("获取待执行任务: assignedTo={}, limit={}", assignedTo, limit);
        List<GovernanceTask> tasks = orchestrationService.getPendingTasks(assignedTo, limit);
        List<GovernanceTaskResponse> responses = tasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/scheduled")
    @Operation(summary = "获取调度任务", description = "获取已调度的任务列表")
    public ResponseEntity<List<GovernanceTaskResponse>> getScheduledTasks(
            @RequestParam(required = false) Long policyId) {
        log.info("获取调度任务: policyId={}", policyId);
        List<GovernanceTask> tasks = orchestrationService.getScheduledTasks(policyId);
        List<GovernanceTaskResponse> responses = tasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "立即执行任务", description = "手动触发任务立即执行")
    public ResponseEntity<Map<String, Object>> executeTask(@PathVariable Long id) {
        log.info("立即执行任务: {}", id);
        String executionId = orchestrationService.executeTask(id);
        return ResponseEntity.ok(Map.of(
                "executionId", executionId,
                "message", "任务已开始执行"
        ));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消任务", description = "取消待执行的任务")
    public ResponseEntity<GovernanceTaskResponse> cancelTask(@PathVariable Long id) {
        log.info("取消任务: {}", id);
        return orchestrationService.cancelTask(id)
                .map(this::toTaskResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "重试任务", description = "重试失败的任务")
    public ResponseEntity<Map<String, Object>> retryTask(@PathVariable Long id) {
        log.info("重试任务: {}", id);
        String executionId = orchestrationService.retryTask(id);
        return ResponseEntity.ok(Map.of(
                "executionId", executionId,
                "message", "任务重试已开始"
        ));
    }

    @PostMapping("/batch/execute")
    @Operation(summary = "批量执行任务", description = "批量执行多个任务")
    public ResponseEntity<Map<String, Object>> batchExecuteTasks(
            @RequestBody List<Long> taskIds) {
        log.info("批量执行任务: {}", taskIds);
        Map<String, Object> result = orchestrationService.batchExecuteTasks(taskIds);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/trigger")
    @Operation(summary = "触发任务生成", description = "根据策略触发条件自动生成任务")
    public ResponseEntity<List<GovernanceTaskResponse>> triggerTaskGeneration(
            @RequestParam Long policyId,
            @RequestParam(required = false) String targetAssetId) {
        log.info("触发任务生成: policyId={}, targetAssetId={}", policyId, targetAssetId);
        List<GovernanceTask> tasks = orchestrationService.triggerTaskGeneration(policyId, targetAssetId);
        List<GovernanceTaskResponse> responses = tasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private GovernanceTaskResponse toTaskResponse(GovernanceTask task) {
        return GovernanceTaskResponse.builder()
                .id(task.getId())
                .taskName(task.getTaskName())
                .taskType(task.getTaskType())
                .policyId(task.getPolicyId())
                .description(task.getDescription())
                .priority(task.getPriority())
                .priorityLabel(getPriorityLabel(task.getPriority()))
                .status(task.getStatus())
                .targetAssetId(task.getTargetAssetId())
                .targetAssetType(task.getTargetAssetType())
                .taskConfig(task.getTaskConfig())
                .dependsOnTasks(task.getDependsOnTasks())
                .scheduleExpression(task.getScheduleExpression())
                .scheduledTime(task.getScheduledTime())
                .actualStartTime(task.getActualStartTime())
                .actualEndTime(task.getActualEndTime())
                .assignedTo(task.getAssignedTo())
                .retryCount(task.getRetryCount())
                .lastError(task.getLastError())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .createdBy(task.getCreatedBy())
                .build();
    }

    private String getPriorityLabel(Integer priority) {
        if (priority == null) return "未知";
        return switch (priority) {
            case 1 -> "紧急";
            case 2 -> "高";
            case 3 -> "中";
            case 4 -> "低";
            default -> "未知";
        };
    }
}
