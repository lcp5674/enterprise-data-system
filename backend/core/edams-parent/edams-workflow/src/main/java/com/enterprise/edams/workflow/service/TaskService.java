package com.enterprise.edams.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.workflow.entity.ApprovalTask;

/**
 * 任务服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface TaskService {

    /** 分页查询任务列表 */
    IPage<ApprovalTask> queryTasks(Long assigneeId, Long instanceId, Integer status,
                                   int pageNum, int pageSize);

    /** 获取任务详情 */
    ApprovalTask getTaskDetail(Long taskId);

    /** 获取流程实例的所有任务历史（审批记录） */
    List<ApprovalTask> getTaskHistory(Long instanceId);

    /** 获取我的待办数量 */
    long getMyPendingCount(Long userId);
}
