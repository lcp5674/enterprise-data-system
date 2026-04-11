package com.enterprise.edams.workflow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.workflow.entity.WorkflowInstance;
import com.enterprise.edams.workflow.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 审批流程控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@Tag(name = "审批管理", description="发起审批、审批处理、查询等接口")
public class ApprovalController {

    private final ApprovalService approvalService;

    // ==================== 发起与查询 ====================

    @PostMapping("/start")
    @Operation(summary = "发起流程实例", description="基于已发布的流程定义创建新的审批流程")
    public Result<WorkflowInstance> startProcess(@RequestBody StartProcessRequest request) {
        WorkflowInstance instance = approvalService.startProcess(
                request.getDefinitionId(),
                request.getInitiatorId(),
                request.getBusinessTitle(),
                request.getFormData()
        );
        return Result.success(instance);
    }

    @GetMapping("/my/initiated")
    @Operation(summary = "我发起的流程", description="查询当前用户发起的所有流程")
    public PageResult<WorkflowInstance> myInitiated(
            @RequestParam Long initiatorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<WorkflowInstance> page = approvalService.queryMyInitiated(initiatorId, status, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @GetMapping("/my/pending")
    @Operation(summary = "待我审批的流程", description="当前用户需要处理的审批任务列表")
    public PageResult<WorkflowInstance> myPending(
            @RequestParam Long assigneeId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<WorkflowInstance> page = approvalService.queryMyPending(assigneeId, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @GetMapping
    @Operation(summary = "分页查询所有流程实例", description="管理员查看所有运行中的流程")
    public PageResult<WorkflowInstance> queryInstances(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<WorkflowInstance> page = approvalService.queryInstances(keyword, status, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @GetMapping("/{instanceId}")
    @Operation(summary = "获取流程实例详情")
    public Result<WorkflowInstance> getInstanceDetail(@PathVariable Long instanceId) {
        return Result.success(approvalService.getInstanceDetail(instanceId));
    }

    // ==================== 审批操作 ====================

    @PostMapping("/tasks/{taskId}/approve")
    @Operation(summary = "审批通过/同意")
    public Result<Void> approve(
            @PathVariable Long taskId,
            @RequestBody ApprovalRequest request) {
        approvalService.approve(taskId, request.getAssigneeId(), request.getComment());
        return Result.success();
    }

    @PostMapping("/tasks/{taskId}/reject")
    @Operation(summary = "审批拒绝/不同意（直接结束）")
    public Result<Void> reject(
            @PathVariable Long taskId,
            @RequestBody ApprovalRequest request) {
        approvalService.reject(taskId, request.getAssigneeId(), request.getComment());
        return Result.success();
    }

    @PostMapping("/tasks/{taskId}/return")
    @Operation(summary = "驳回（退回到指定节点或发起人）")
    public Result<Void> returnTask(
            @PathVariable Long taskId,
            @RequestBody ReturnRequest request) {
        approvalService.returnTask(taskId, request.getAssigneeId(), 
                                   request.getTargetNodeKey(), request.getComment());
        return Result.success();
    }

    @PostMapping("/{instanceId}/cancel")
    @Operation(summary = "撤销流程", description="只有发起人可以撤销自己发起的流程")
    public Result<Void> cancel(
            @PathVariable Long instanceId,
            @RequestBody CancelRequest request) {
        approvalService.cancel(instanceId, request.getOperatorId(), request.getReason());
        return Result.success();
    }

    @PostMapping("/tasks/{taskId}/delegate")
    @Operation(summary = "转办任务", description="将任务转移给其他人处理")
    public Result<Void> delegate(
            @PathVariable Long taskId,
            @RequestBody DelegateRequest request) {
        approvalService.delegate(taskId, request.getFromUserId(), 
                                request.getToUserId(), request.getComment());
        return Result.success();
    }

    @GetMapping("/my/pending-count")
    @Operation(summary = "获取我的待办数量")
    public Result<Long> getPendingCount(@RequestParam Long assigneeId) {
        return Result.success(approvalService.getPendingCount(assigneeId));
    }

    // ==================== 内部DTO类 ====================

    /** 发起流程请求 */
    @lombok.Data public static class StartProcessRequest {
        private Long definitionId;
        private Long initiatorId;
        private String businessTitle;
        private String formData;
    }

    /** 审批请求 */
    @lombok.Data public static class ApprovalRequest {
        private Long assigneeId;
        private String comment;
    }

    /** 驳回请求 */
    @lombok.Data public static class ReturnRequest {
        private Long assigneeId;
        private String targetNodeKey;
        private String comment;
    }

    /** 撤销请求 */
    @lombok.Data public static class CancelRequest {
        private String operatorId;
        private String reason;
    }

    /** 转办请求 */
    @lombok.Data public static class DelegateRequest {
        private Long fromUserId;
        private Long toUserId;
        private String comment;
    }
}
