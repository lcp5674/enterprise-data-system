package com.enterprise.edams.aiops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.aiops.entity.OpsTask;
import com.enterprise.edams.aiops.repository.OpsTaskMapper;
import com.enterprise.edams.aiops.service.OpsTaskService;
import com.enterprise.edams.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 运维任务服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpsTaskServiceImpl implements OpsTaskService {

    private final OpsTaskMapper opsTaskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpsTask createTask(OpsTask task) {
        if (task.getTaskStatus() == null) {
            task.setTaskStatus("pending");
        }
        if (task.getProgressPercent() == null) {
            task.setProgressPercent(0);
        }
        if (task.getRetryCount() == null) {
            task.setRetryCount(0);
        }
        if (task.getMaxRetries() == null) {
            task.setMaxRetries(3);
        }
        task.setTenantId(task.getTenantId() != null ? task.getTenantId() : 1L);
        opsTaskMapper.insert(task);
        log.info("创建运维任务: {}", task.getTaskTitle());
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpsTask updateTask(OpsTask task) {
        OpsTask existing = opsTaskMapper.selectById(task.getId());
        if (existing == null) {
            throw new BusinessException("任务不存在");
        }
        opsTaskMapper.updateById(task);
        log.info("更新运维任务: {}", task.getId());
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        opsTaskMapper.deleteById(id);
        log.info("删除运维任务: {}", id);
    }

    @Override
    public OpsTask getTaskById(Long id) {
        return opsTaskMapper.selectById(id);
    }

    @Override
    public Page<OpsTask> pageTasks(int pageNum, int pageSize, String taskType, String taskStatus, String targetId, LocalDateTime startTime, LocalDateTime endTime) {
        Page<OpsTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OpsTask> wrapper = new LambdaQueryWrapper<>();
        
        if (taskType != null && !taskType.isEmpty()) {
            wrapper.eq(OpsTask::getTaskType, taskType);
        }
        if (taskStatus != null && !taskStatus.isEmpty()) {
            wrapper.eq(OpsTask::getTaskStatus, taskStatus);
        }
        if (targetId != null && !targetId.isEmpty()) {
            wrapper.eq(OpsTask::getTargetId, targetId);
        }
        if (startTime != null) {
            wrapper.ge(OpsTask::getPlannedStartTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OpsTask::getPlannedStartTime, endTime);
        }
        
        wrapper.orderByDesc(OpsTask::getCreatedTime);
        return opsTaskMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTask(Long id, String submittedBy) {
        OpsTask task = opsTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        task.setUpdatedBy(submittedBy);
        opsTaskMapper.updateById(task);
        log.info("提交运维任务: {} by {}", id, submittedBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startTask(Long id) {
        opsTaskMapper.startTask(id, LocalDateTime.now(), LocalDateTime.now());
        log.info("开始执行运维任务: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(Long id, int progress) {
        opsTaskMapper.updateTaskStatus(id, "running", progress, LocalDateTime.now());
        log.debug("更新任务进度: {} -> {}%", id, progress);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long id, boolean success, String result, String outputLog) {
        String status = success ? "completed" : "failed";
        opsTaskMapper.completeTask(id, status, LocalDateTime.now(), result, outputLog, LocalDateTime.now());
        log.info("完成任务: {} - {}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long id, String reason) {
        OpsTask task = opsTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        task.setTaskStatus("cancelled");
        task.setErrorMessage(reason);
        opsTaskMapper.updateById(task);
        log.info("取消运维任务: {} - {}", id, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryTask(Long id) {
        OpsTask task = opsTaskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        if (task.getRetryCount() >= task.getMaxRetries()) {
            throw new BusinessException("已达到最大重试次数");
        }
        task.setTaskStatus("pending");
        task.setRetryCount(task.getRetryCount() + 1);
        task.setProgressPercent(0);
        task.setErrorMessage(null);
        opsTaskMapper.updateById(task);
        log.info("重试运维任务: {} (第{}次)", id, task.getRetryCount());
    }

    @Override
    public List<OpsTask> getPendingTasks() {
        return opsTaskMapper.findPendingTasks(LocalDateTime.now());
    }

    @Override
    public List<OpsTask> getRunningTasks() {
        return opsTaskMapper.findRunningTasks();
    }

    @Override
    public List<OpsTask> getOverdueTasks() {
        return opsTaskMapper.findOverdueTasks(LocalDateTime.now());
    }

    @Override
    public List<Map<String, Object>> countByType() {
        return opsTaskMapper.countByType();
    }

    @Override
    @Scheduled(fixedDelayString = "${aiops.ops-task.schedule-interval:60000}")
    public void schedulePendingTasks() {
        log.debug("调度待执行任务...");
        List<OpsTask> pendingTasks = getPendingTasks();
        for (OpsTask task : pendingTasks) {
            try {
                startTask(task.getId());
                log.info("自动启动任务: {}", task.getId());
            } catch (Exception e) {
                log.error("启动任务失败: {}", task.getId(), e);
            }
        }
    }
}
