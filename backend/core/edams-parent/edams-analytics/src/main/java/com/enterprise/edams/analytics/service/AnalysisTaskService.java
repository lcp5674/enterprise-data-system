package com.enterprise.edams.analytics.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.analytics.entity.AnalysisTask;

import java.util.List;

/**
 * 分析任务服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface AnalysisTaskService {

    /**
     * 创建任务
     */
    AnalysisTask createTask(AnalysisTask task);

    /**
     * 更新任务
     */
    AnalysisTask updateTask(Long id, AnalysisTask task);

    /**
     * 删除任务
     */
    void deleteTask(Long id);

    /**
     * 获取任务详情
     */
    AnalysisTask getTaskById(Long id);

    /**
     * 分页查询任务
     */
    IPage<AnalysisTask> queryTasks(String taskName, String taskType, String status, int pageNum, int pageSize);

    /**
     * 获取用户的任务
     */
    List<AnalysisTask> getTasksByCreator(String creator);

    /**
     * 根据类型获取任务
     */
    List<AnalysisTask> getTasksByType(String taskType);

    /**
     * 根据状态获取任务
     */
    List<AnalysisTask> getTasksByStatus(String status);

    /**
     * 执行任务
     */
    void executeTask(Long id);

    /**
     * 取消任务
     */
    void cancelTask(Long id);

    /**
     * 批量执行任务
     */
    void batchExecuteTasks(List<Long> taskIds);

    /**
     * 获取待执行任务
     */
    List<AnalysisTask> getPendingTasks(Integer limit);

    /**
     * 统计任务数量
     */
    long countByStatus(String status);
}
