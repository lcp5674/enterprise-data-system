package com.enterprise.edams.asset.feign.fallback;

import com.enterprise.edams.asset.dto.ProcessInstanceDTO;
import com.enterprise.edams.asset.dto.ProcessInstanceStartRequest;
import com.enterprise.edams.asset.feign.WorkflowFeignClient;
import com.enterprise.edams.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 工作流服务Feign客户端降级处理
 * 当工作流服务不可用时，返回默认值并记录日志
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class WorkflowFeignClientFallback implements WorkflowFeignClient {

    @Override
    public Result<ProcessInstanceDTO> startProcessInstance(ProcessInstanceStartRequest request, String userId, String userName) {
        log.error("Feign调用工作流服务启动流程失败, processDefinitionKey: {}, businessId: {}, userId: {}",
                request != null ? request.getProcessDefinitionKey() : "null",
                request != null ? request.getBusinessId() : "null",
                userId);
        return Result.success(null);
    }

    @Override
    public Result<ProcessInstanceDTO> getProcessInstance(String instanceId) {
        log.error("Feign调用工作流服务查询流程实例失败, instanceId: {}", instanceId);
        return Result.success(null);
    }

    @Override
    public Result<List<ProcessInstanceDTO>> listPendingApproval(String userId) {
        log.error("Feign调用工作流服务查询待审批流程失败, userId: {}", userId);
        return Result.success(Collections.emptyList());
    }

    @Override
    public Result<Void> approveProcessInstance(String instanceId, String action, String comment, String userId) {
        log.error("Feign调用工作流服务审批流程失败, instanceId: {}, action: {}, userId: {}",
                instanceId, action, userId);
        return Result.success(null);
    }

    @Override
    public Result<Void> terminateProcessInstance(String instanceId, String reason, String userId) {
        log.error("Feign调用工作流服务终止流程失败, instanceId: {}, reason: {}, userId: {}",
                instanceId, reason, userId);
        return Result.success(null);
    }

    @Override
    public Result<Boolean> isProcessCompleted(String instanceId) {
        log.error("Feign调用工作流服务检查流程完成状态失败, instanceId: {}", instanceId);
        return Result.success(false);
    }
}
