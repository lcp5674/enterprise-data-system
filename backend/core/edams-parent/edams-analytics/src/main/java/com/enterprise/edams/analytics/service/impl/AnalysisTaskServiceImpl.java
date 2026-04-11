package com.enterprise.edams.analytics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.analytics.entity.AnalysisTask;
import com.enterprise.edams.analytics.repository.AnalysisTaskMapper;
import com.enterprise.edams.analytics.service.AnalysisTaskService;
import com.enterprise.edams.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 分析任务服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisTaskServiceImpl implements AnalysisTaskService {

    private final AnalysisTaskMapper taskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisTask createTask(AnalysisTask task) {
        // 校验任务名称
        if (taskMapper.selectByTaskName(task.getTaskName()) != null) {
            throw new BusinessException("任务名称已存在: " + task.getTaskName());
        }

        task.setStatus("PENDING");
        task.setExecutionTime(0L);
        task.setResultRows(0L);
        
        taskMapper.insert(task);
        log.info("分析任务创建成功: id={}, name={}", task.getId(), task.getTaskName());
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisTask updateTask(Long id, AnalysisTask task) {
        AnalysisTask existTask = taskMapper.selectById(id);
        if (existTask == null) {
            throw new BusinessException("任务不存在: " + id);
        }

        // 只有待执行状态的任务可以更新
        if (!"PENDING".equals(existTask.getStatus())) {
            throw new BusinessException("当前状态不允许更新: " + existTask.getStatus());
        }

        existTask.setTaskName(task.getTaskName());
        existTask.setTaskType(task.getTaskType());
        existTask.setQuerySql(task.getQuerySql());
        existTask.setDatasourceId(task.getDatasourceId());
        existTask.setDescription(task.getDescription());
        existTask.setParameters(task.getParameters());

        taskMapper.updateById(existTask);
        log.info("分析任务更新成功: id={}", id);
        return existTask;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        AnalysisTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在: " + id);
        }

        // 正在执行的任务不能删除
        if ("RUNNING".equals(task.getStatus())) {
            throw new BusinessException("任务正在执行中,不能删除");
        }

        taskMapper.deleteById(id);
        log.info("分析任务删除成功: id={}", id);
    }

    @Override
    public AnalysisTask getTaskById(Long id) {
        AnalysisTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在: " + id);
        }
        return task;
    }

    @Override
    public IPage<AnalysisTask> queryTasks(String taskName, String taskType, String status, int pageNum, int pageSize) {
        Page<AnalysisTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AnalysisTask> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(taskName)) {
            wrapper.like(AnalysisTask::getTaskName, taskName);
        }
        if (StringUtils.isNotBlank(taskType)) {
            wrapper.eq(AnalysisTask::getTaskType, taskType);
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(AnalysisTask::getStatus, status);
        }

        wrapper.orderByDesc(AnalysisTask::getCreatedTime);
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    public List<AnalysisTask> getTasksByCreator(String creator) {
        return taskMapper.selectByCreator(creator);
    }

    @Override
    public List<AnalysisTask> getTasksByType(String taskType) {
        return taskMapper.selectByTaskType(taskType);
    }

    @Override
    public List<AnalysisTask> getTasksByStatus(String status) {
        return taskMapper.selectByStatus(status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeTask(Long id) {
        AnalysisTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在: " + id);
        }

        if ("RUNNING".equals(task.getStatus())) {
            throw new BusinessException("任务正在执行中");
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        try {
            // 更新为执行中状态
            taskMapper.updateStatus(id, "RUNNING");
            
            // TODO: 实际执行SQL查询逻辑
            log.info("执行任务: id={}, sql={}", id, task.getQuerySql());
            
            // 模拟执行耗时
            Thread.sleep(100);
            
            // 更新执行成功结果
            taskMapper.updateExecuteResult(id, "SUCCESS", 100L, 1000L, null);
            
            log.info("任务执行成功: id={}", id);
        } catch (Exception e) {
            // 更新执行失败结果
            taskMapper.updateExecuteResult(id, "FAILED", 0L, 0L, e.getMessage());
            throw new BusinessException("任务执行失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long id) {
        AnalysisTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在: " + id);
        }

        if (!"RUNNING".equals(task.getStatus())) {
            throw new BusinessException("只有执行中的任务可以取消");
        }

        // TODO: 实际的取消逻辑
        taskMapper.updateStatus(id, "FAILED");
        log.info("任务已取消: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchExecuteTasks(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return;
        }

        for (Long taskId : taskIds) {
            try {
                executeTask(taskId);
            } catch (Exception e) {
                log.error("批量执行任务失败: taskId={}, error={}", taskId, e.getMessage());
            }
        }
    }

    @Override
    public List<AnalysisTask> getPendingTasks(Integer limit) {
        return taskMapper.selectPendingTasks(limit);
    }

    @Override
    public long countByStatus(String status) {
        return taskMapper.countByStatus(status);
    }
}
