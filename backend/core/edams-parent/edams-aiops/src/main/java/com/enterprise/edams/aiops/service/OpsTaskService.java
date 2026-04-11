package com.enterprise.edams.aiops.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.aiops.entity.OpsTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 运维任务服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface OpsTaskService {

    /**
     * 创建运维任务
     */
    OpsTask createTask(OpsTask task);

    /**
     * 更新运维任务
     */
    OpsTask updateTask(OpsTask task);

    /**
     * 删除运维任务
     */
    void deleteTask(Long id);

    /**
     * 根据ID查询任务
     */
    OpsTask getTaskById(Long id);

    /**
     * 分页查询任务
     */
    Page<OpsTask> pageTasks(int pageNum, int pageSize, String taskType, String taskStatus, String targetId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 提交任务
     */
    void submitTask(Long id, String submittedBy);

    /**
     * 开始执行任务
     */
    void startTask(Long id);

    /**
     * 更新任务进度
     */
    void updateProgress(Long id, int progress);

    /**
     * 完成执行任务
     */
    void completeTask(Long id, boolean success, String result, String outputLog);

    /**
     * 取消任务
     */
    void cancelTask(Long id, String reason);

    /**
     * 重试任务
     */
    void retryTask(Long id);

    /**
     * 查询待执行任务
     */
    List<OpsTask> getPendingTasks();

    /**
     * 查询执行中任务
     */
    List<OpsTask> getRunningTasks();

    /**
     * 查询超时任务
     */
    List<OpsTask> getOverdueTasks();

    /**
     * 按类型统计任务
     */
    List<Map<String, Object>> countByType();

    /**
     * 调度待执行任务
     */
    void schedulePendingTasks();
}
