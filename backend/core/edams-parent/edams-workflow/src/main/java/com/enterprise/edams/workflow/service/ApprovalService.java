package com.enterprise.edams.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.workflow.entity.WorkflowInstance;
import java.util.List;
import java.util.Map;

/**
 * 审批服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface ApprovalService {

    /** 发起流程实例 */
    WorkflowInstance startProcess(Long definitionId, Long initiatorId,
                                   String businessTitle, String formData);

    /** 分页查询我的发起 */
    IPage<WorkflowInstance> queryMyInitiated(Long initiatorId, Integer status,
                                             int pageNum, int pageSize);

    /** 分页查询待我审批的列表 */
    IPage<WorkflowInstance> queryMyPending(Long assigneeId, int pageNum, int pageSize);

    /** 分页查询所有运行中的流程实例 */
    IPage<WorkflowInstance> queryInstances(String keyword, Integer status,
                                           int pageNum, int pageSize);

    /** 根据ID获取实例详情（含任务历史） */
    WorkflowInstance getInstanceDetail(Long instanceId);

    /** 审批通过/同意 */
    void approve(Long taskId, Long assigneeId, String comment);

    /** 审批拒绝/不同意 */
    void reject(Long taskId, Long assigneeId, String comment);

    /** 驳回（退回到上一步或发起人） */
    void returnTask(Long taskId, Long assigneeId, String targetNodeKey, String comment);

    /** 撤销发起人的流程 */
    void cancel(Long instanceId, String operatorId, String reason);

    /** 转办任务给其他人 */
    void delegate(Long taskId, Long fromUserId, Long toUserId, String comment);

    /** 获取待办数量 */
    long getPendingCount(Long assigneeId);
}
