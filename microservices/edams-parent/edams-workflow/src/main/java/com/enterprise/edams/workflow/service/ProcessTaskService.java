package com.enterprise.edams.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.workflow.dto.ProcessTaskDTO;
import com.enterprise.edams.workflow.dto.TaskApproveRequest;
import com.enterprise.edams.workflow.entity.ProcessTask;

import java.util.List;

/**
 * 流程任务服务接口
 * 
 * @author EDAMS Team
 */
public interface ProcessTaskService extends IService<ProcessTask> {

    /**
     * 审批任务
     */
    void approveTask(TaskApproveRequest request, String userId, String userName);

    /**
     * 拒绝任务
     */
    void rejectTask(TaskApproveRequest request, String userId, String userName);

    /**
     * 退回任务
     */
    void backTask(TaskApproveRequest request, String userId, String userName);

    /**
     * 转办任务
     */
    void transferTask(TaskApproveRequest request, String userId, String userName);

    /**
     * 委托任务
     */
    void delegateTask(TaskApproveRequest request, String userId, String userName);

    /**
     * 获取任务详情
     */
    ProcessTaskDTO getTask(String taskId);

    /**
     * 分页查询任务
     */
    Page<ProcessTaskDTO> listTasks(Page<ProcessTask> page, String processInstanceId, Integer status);

    /**
     * 查询用户的待办任务
     */
    Page<ProcessTaskDTO> listTodoTasks(Page<ProcessTask> page, String userId);

    /**
     * 查询用户的已办任务
     */
    Page<ProcessTaskDTO> listDoneTasks(Page<ProcessTask> page, String userId);

    /**
     * 查询用户的抄送任务
     */
    Page<ProcessTaskDTO> listCcTasks(Page<ProcessTask> page, String userId);

    /**
     * 发送任务提醒
     */
    void sendTaskReminder(String taskId);

    /**
     * 批量审批
     */
    void batchApprove(List<String> taskIds, Integer result, String comment, String userId, String userName);
}
