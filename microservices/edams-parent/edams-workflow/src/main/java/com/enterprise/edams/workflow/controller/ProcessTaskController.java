package com.enterprise.edams.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.workflow.dto.ProcessTaskDTO;
import com.enterprise.edams.workflow.dto.TaskApproveRequest;
import com.enterprise.edams.workflow.entity.ProcessTask;
import com.enterprise.edams.workflow.service.ProcessTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程任务控制器
 *
 * @author EDAMS Team
 */
@RestController
@RequestMapping("/api/v1/process-tasks")
@RequiredArgsConstructor
@Tag(name = "流程任务管理", description = "流程任务相关接口")
public class ProcessTaskController {

    private final ProcessTaskService processTaskService;

    @PostMapping("/{taskId}/approve")
    @Operation(summary = "审批通过")
    public Result<Void> approveTask(
            @PathVariable String taskId,
            @Valid @RequestBody TaskApproveRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName) {
        request.setTaskId(taskId);
        processTaskService.approveTask(request, userId, userName);
        return Result.success();
    }

    @PostMapping("/{taskId}/reject")
    @Operation(summary = "审批拒绝")
    public Result<Void> rejectTask(
            @PathVariable String taskId,
            @Valid @RequestBody TaskApproveRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName) {
        request.setTaskId(taskId);
        processTaskService.rejectTask(request, userId, userName);
        return Result.success();
    }

    @PostMapping("/{taskId}/back")
    @Operation(summary = "退回任务")
    public Result<Void> backTask(
            @PathVariable String taskId,
            @Valid @RequestBody TaskApproveRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName) {
        request.setTaskId(taskId);
        processTaskService.backTask(request, userId, userName);
        return Result.success();
    }

    @PostMapping("/{taskId}/transfer")
    @Operation(summary = "转办任务")
    public Result<Void> transferTask(
            @PathVariable String taskId,
            @Valid @RequestBody TaskApproveRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName) {
        request.setTaskId(taskId);
        processTaskService.transferTask(request, userId, userName);
        return Result.success();
    }

    @PostMapping("/{taskId}/delegate")
    @Operation(summary = "委托任务")
    public Result<Void> delegateTask(
            @PathVariable String taskId,
            @Valid @RequestBody TaskApproveRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName) {
        request.setTaskId(taskId);
        processTaskService.delegateTask(request, userId, userName);
        return Result.success();
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "获取任务详情")
    public Result<ProcessTaskDTO> getTask(@PathVariable String taskId) {
        return Result.success(processTaskService.getTask(taskId));
    }

    @GetMapping
    @Operation(summary = "分页查询任务")
    public Result<Page<ProcessTaskDTO>> listTasks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String processInstanceId,
            @RequestParam(required = false) Integer status) {
        Page<ProcessTask> page = new Page<>(current, size);
        return Result.success(processTaskService.listTasks(page, processInstanceId, status));
    }

    @GetMapping("/todo")
    @Operation(summary = "查询我的待办任务")
    public Result<Page<ProcessTaskDTO>> listTodoTasks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader("X-User-Id") String userId) {
        Page<ProcessTask> page = new Page<>(current, size);
        return Result.success(processTaskService.listTodoTasks(page, userId));
    }

    @GetMapping("/done")
    @Operation(summary = "查询我的已办任务")
    public Result<Page<ProcessTaskDTO>> listDoneTasks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader("X-User-Id") String userId) {
        Page<ProcessTask> page = new Page<>(current, size);
        return Result.success(processTaskService.listDoneTasks(page, userId));
    }

    @GetMapping("/cc")
    @Operation(summary = "查询我的抄送任务")
    public Result<Page<ProcessTaskDTO>> listCcTasks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader("X-User-Id") String userId) {
        Page<ProcessTask> page = new Page<>(current, size);
        return Result.success(processTaskService.listCcTasks(page, userId));
    }

    @PostMapping("/{taskId}/remind")
    @Operation(summary = "发送任务提醒")
    public Result<Void> sendTaskReminder(@PathVariable String taskId) {
        processTaskService.sendTaskReminder(taskId);
        return Result.success();
    }

    @PostMapping("/batch-approve")
    @Operation(summary = "批量审批")
    public Result<Void> batchApprove(
            @RequestBody List<String> taskIds,
            @RequestParam Integer result,
            @RequestParam(required = false) String comment,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName) {
        processTaskService.batchApprove(taskIds, result, comment, userId, userName);
        return Result.success();
    }
}
