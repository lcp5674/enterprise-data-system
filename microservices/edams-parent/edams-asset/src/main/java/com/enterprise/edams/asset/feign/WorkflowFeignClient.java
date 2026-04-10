package com.enterprise.edams.asset.feign;

import com.enterprise.edams.asset.feign.fallback.WorkflowFeignClientFallback;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.asset.dto.ProcessInstanceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 工作流服务Feign客户端
 * 用于资产服务等调用工作流服务启动流程或查询流程状态
 *
 * @author Backend Team
 * @version 1.0.0
 */
@FeignClient(
    name = "edams-workflow",
    url = "${feign.workflow.url:}",
    fallback = WorkflowFeignClientFallback.class
)
public interface WorkflowFeignClient {

    /**
     * 启动流程实例
     *
     * @param request 流程启动请求
     * @param userId  用户ID
     * @param userName 用户名称
     * @return 流程实例信息
     */
    @PostMapping("/api/v1/process-instances/start")
    Result<ProcessInstanceDTO> startProcessInstance(
            @RequestBody ProcessInstanceStartRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName);

    /**
     * 查询流程实例详情
     *
     * @param instanceId 流程实例ID
     * @return 流程实例信息
     */
    @GetMapping("/api/v1/process-instances/{instanceId}")
    Result<ProcessInstanceDTO> getProcessInstance(@PathVariable String instanceId);

    /**
     * 查询待审批的流程实例
     *
     * @param userId 用户ID
     * @return 待审批流程实例列表
     */
    @GetMapping("/api/v1/process-instances/pending-approval")
    Result<List<ProcessInstanceDTO>> listPendingApproval(@RequestParam String userId);

    /**
     * 审批流程任务
     *
     * @param instanceId 流程实例ID
     * @param action     审批动作(approve/reject)
     * @param comment    审批意见
     * @param userId     审批人ID
     * @return 操作结果
     */
    @PostMapping("/api/v1/process-instances/{instanceId}/approve")
    Result<Void> approveProcessInstance(
            @PathVariable String instanceId,
            @RequestParam String action,
            @RequestParam(required = false) String comment,
            @RequestHeader("X-User-Id") String userId);

    /**
     * 终止流程实例
     *
     * @param instanceId 流程实例ID
     * @param reason     终止原因
     * @param userId     操作人ID
     * @return 操作结果
     */
    @PostMapping("/api/v1/process-instances/{instanceId}/terminate")
    Result<Void> terminateProcessInstance(
            @PathVariable String instanceId,
            @RequestParam String reason,
            @RequestHeader("X-User-Id") String userId);

    /**
     * 检查流程是否完成
     *
     * @param instanceId 流程实例ID
     * @return 是否完成
     */
    @GetMapping("/api/v1/process-instances/{instanceId}/completed")
    Result<Boolean> isProcessCompleted(@PathVariable String instanceId);
}
