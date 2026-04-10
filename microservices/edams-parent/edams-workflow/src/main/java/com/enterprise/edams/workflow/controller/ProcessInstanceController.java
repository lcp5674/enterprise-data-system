package com.enterprise.edams.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.workflow.dto.ProcessInstanceDTO;
import com.enterprise.edams.workflow.dto.ProcessInstanceStartRequest;
import com.enterprise.edams.workflow.entity.ProcessInstance;
import com.enterprise.edams.workflow.service.ProcessInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程实例控制器
 *
 * @author EDAMS Team
 */
@RestController
@RequestMapping("/api/v1/process-instances")
@RequiredArgsConstructor
@Tag(name = "流程实例管理", description = "流程实例相关接口")
public class ProcessInstanceController {

    private final ProcessInstanceService processInstanceService;

    @PostMapping("/start")
    @Operation(summary = "启动流程实例")
    public Result<ProcessInstanceDTO> startProcessInstance(
            @Valid @RequestBody ProcessInstanceStartRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName) {
        return Result.success(processInstanceService.startProcessInstance(request, userId, userName));
    }

    @PostMapping("/{instanceId}/terminate")
    @Operation(summary = "终止流程实例")
    public Result<Void> terminateProcessInstance(
            @PathVariable String instanceId,
            @RequestParam String reason,
            @RequestHeader("X-User-Id") String userId) {
        processInstanceService.terminateProcessInstance(instanceId, reason, userId);
        return Result.success();
    }

    @PostMapping("/{instanceId}/revoke")
    @Operation(summary = "撤回流程实例")
    public Result<Void> revokeProcessInstance(
            @PathVariable String instanceId,
            @RequestParam String reason,
            @RequestHeader("X-User-Id") String userId) {
        processInstanceService.revokeProcessInstance(instanceId, reason, userId);
        return Result.success();
    }

    @PostMapping("/{instanceId}/suspend")
    @Operation(summary = "挂起流程实例")
    public Result<Void> suspendProcessInstance(
            @PathVariable String instanceId,
            @RequestHeader("X-User-Id") String userId) {
        processInstanceService.suspendProcessInstance(instanceId, userId);
        return Result.success();
    }

    @PostMapping("/{instanceId}/activate")
    @Operation(summary = "激活流程实例")
    public Result<Void> activateProcessInstance(
            @PathVariable String instanceId,
            @RequestHeader("X-User-Id") String userId) {
        processInstanceService.activateProcessInstance(instanceId, userId);
        return Result.success();
    }

    @GetMapping("/{instanceId}")
    @Operation(summary = "获取流程实例详情")
    public Result<ProcessInstanceDTO> getProcessInstance(@PathVariable String instanceId) {
        return Result.success(processInstanceService.getProcessInstance(instanceId));
    }

    @GetMapping
    @Operation(summary = "分页查询流程实例")
    public Result<Page<ProcessInstanceDTO>> listProcessInstances(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String businessType) {
        Page<ProcessInstance> page = new Page<>(current, size);
        return Result.success(processInstanceService.listProcessInstances(page, keyword, status, businessType));
    }

    @GetMapping("/my-started")
    @Operation(summary = "查询我发起的流程实例")
    public Result<Page<ProcessInstanceDTO>> listMyStartedInstances(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestHeader("X-User-Id") String userId) {
        Page<ProcessInstance> page = new Page<>(current, size);
        return Result.success(processInstanceService.listMyStartedInstances(page, userId, status));
    }

    @GetMapping("/pending-approval")
    @Operation(summary = "查询待我审批的流程实例")
    public Result<List<ProcessInstanceDTO>> listPendingApproval(
            @RequestHeader("X-User-Id") String userId) {
        return Result.success(processInstanceService.listPendingApproval(userId));
    }

    @GetMapping("/approved-by-me")
    @Operation(summary = "查询我已审批的流程实例")
    public Result<List<ProcessInstanceDTO>> listApprovedByMe(
            @RequestHeader("X-User-Id") String userId) {
        return Result.success(processInstanceService.listApprovedByMe(userId));
    }
}
