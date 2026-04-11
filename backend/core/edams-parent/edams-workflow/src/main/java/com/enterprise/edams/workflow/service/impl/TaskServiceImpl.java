package com.enterprise.edams.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.workflow.entity.ApprovalTask;
import com.enterprise.edams.workflow.repository.ApprovalTaskMapper;
import com.enterprise.edams.workflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final ApprovalTaskMapper taskMapper;

    @Override
    public IPage<ApprovalTask> queryTasks(Long assigneeId, Long instanceId,
                                           Integer status, int pageNum, int pageSize) {
        Page<ApprovalTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ApprovalTask> wrapper = new LambdaQueryWrapper<>();

        if (assigneeId != null) wrapper.eq(ApprovalTask::getAssigneeId, assigneeId);
        if (instanceId != null) wrapper.eq(ApprovalTask::getInstanceId, instanceId);
        if (status != null) wrapper.eq(ApprovalTask::getStatus, status);

        wrapper.orderByDesc(ApprovalTask::getCreatedTime);
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public ApprovalTask getTaskDetail(Long taskId) {
        ApprovalTask task = taskMapper.selectById(taskId);
        if (task == null || task.getDeleted() == 1) {
            throw new RuntimeException("任务不存在");
        }
        return task;
    }

    @Override
    public List<ApprovalTask> getTaskHistory(Long instanceId) {
        LambdaQueryWrapper<ApprovalTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalTask::getInstanceId, instanceId)
               .ne(ApprovalTask::getStatus, 0) // 排除待处理状态
               .orderByAsc(ApprovalTask::getCreatedTime);
        return taskMapper.selectList(wrapper);
    }

    @Override
    public long getMyPendingCount(Long userId) {
        return taskMapper.countPendingByAssignee(userId);
    }
}
