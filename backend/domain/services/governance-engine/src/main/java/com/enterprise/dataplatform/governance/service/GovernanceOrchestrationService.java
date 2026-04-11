package com.enterprise.dataplatform.governance.service;

import com.enterprise.dataplatform.governance.domain.entity.GovernanceTask;
import com.enterprise.dataplatform.governance.domain.entity.TaskExecution;
import com.enterprise.dataplatform.governance.dto.request.TaskExecutionRequest;
import com.enterprise.dataplatform.governance.dto.response.TaskExecutionResponse;
import com.enterprise.dataplatform.governance.repository.GovernanceTaskRepository;
import com.enterprise.dataplatform.governance.repository.TaskExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 治理任务编排服务
 * 实现DAG任务调度和执行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GovernanceOrchestrationService {

    private final GovernanceTaskRepository taskRepository;
    private final TaskExecutionRepository executionRepository;

    /**
     * 正在执行的任务
     */
    private final Map<String, GovernanceTask> executingTasks = new ConcurrentHashMap<>();

    /**
     * 执行任务（支持DAG编排）
     */
    @Transactional
    public TaskExecutionResponse executeTask(TaskExecutionRequest request, String executor) {
        log.info("执行治理任务: 任务ID={}, 执行人={}", request.getTaskId(), executor);

        GovernanceTask task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + request.getTaskId()));

        String batchNo = "EXEC-" + UUID.randomUUID().toString().substring(0, 8);

        // 如果有前置依赖，先执行依赖任务
        if (task.getUpstreamTasks() != null && !task.getUpstreamTasks().isEmpty()) {
            for (GovernanceTask upstreamTask : task.getUpstreamTasks()) {
                if ("PENDING".equals(upstreamTask.getTaskStatus()) || "FAILED".equals(upstreamTask.getTaskStatus())) {
                    executeTaskSync(upstreamTask.getId(), executor);
                }
            }
        }

        // 创建执行记录
        TaskExecution execution = TaskExecution.builder()
                .batchNo(batchNo)
                .task(task)
                .taskCode(task.getTaskCode())
                .taskName(task.getTaskName())
                .taskType(task.getTaskType())
                .executionStatus("RUNNING")
                .startTime(LocalDateTime.now())
                .executionParams(request.getExecutionParams())
                .executor(executor)
                .build();

        execution = executionRepository.save(execution);

        // 更新任务状态
        task.setTaskStatus("RUNNING");
        task.setStartTime(LocalDateTime.now());
        task.setExecutor(executor);
        taskRepository.save(task);

        executingTasks.put(batchNo, task);

        // 异步执行任务
        executeTaskAsync(execution.getId());

        return toResponse(execution);
    }

    /**
     * 同步执行任务
     */
    private void executeTaskSync(Long taskId, String executor) {
        GovernanceTask task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return;

        String batchNo = "EXEC-" + UUID.randomUUID().toString().substring(0, 8);

        TaskExecution execution = TaskExecution.builder()
                .batchNo(batchNo)
                .task(task)
                .taskCode(task.getTaskCode())
                .taskName(task.getTaskName())
                .taskType(task.getTaskType())
                .executionStatus("RUNNING")
                .startTime(LocalDateTime.now())
                .executor(executor)
                .build();

        executionRepository.save(execution);

        task.setTaskStatus("RUNNING");
        task.setStartTime(LocalDateTime.now());
        taskRepository.save(task);

        performTaskExecution(execution);
    }

    /**
     * 异步执行任务
     */
    public void executeTaskAsync(Long executionId) {
        log.info("异步执行任务: {}", executionId);
        long startTime = System.currentTimeMillis();

        TaskExecution execution = executionRepository.findById(executionId).orElse(null);
        if (execution == null) {
            log.error("执行记录不存在: {}", executionId);
            return;
        }

        try {
            performTaskExecution(execution);

            execution.setExecutionStatus("COMPLETED");
            execution.setResultStatus("SUCCESS");
            execution.setEndTime(LocalDateTime.now());
            execution.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("任务执行失败: {}", executionId, e);
            execution.setExecutionStatus("FAILED");
            execution.setResultStatus("FAILED");
            execution.setErrorMessage(e.getMessage());
            execution.setEndTime(LocalDateTime.now());
            execution.setExecutionTimeMs(System.currentTimeMillis() - startTime);

            updateTaskToFailed(execution.getTask().getId(), e.getMessage());
        }

        executionRepository.save(execution);
        executingTasks.remove(execution.getBatchNo());

        // 执行下游任务
        triggerDownstreamTasks(execution.getTask().getId());
    }

    /**
     * 执行任务逻辑
     */
    private void performTaskExecution(TaskExecution execution) {
        GovernanceTask task = execution.getTask();

        log.info("执行任务逻辑: {}", task.getTaskCode());

        // 模拟任务执行
        // 根据任务类型执行不同的逻辑
        switch (task.getTaskType()) {
            case "ORCHESTRATION":
                executeOrchestrationTask(execution);
                break;
            case "AUTO_REMEDIATION":
                executeAutoRemediationTask(execution);
                break;
            case "NOTIFICATION":
                executeNotificationTask(execution);
                break;
            case "REPORTING":
                executeReportingTask(execution);
                break;
            default:
                executeDefaultTask(execution);
        }

        // 更新任务状态
        task.setTaskStatus("COMPLETED");
        task.setEndTime(LocalDateTime.now());
        task.setExecutionResult(toJson(Map.of("status", "success", "executionId", execution.getId())));
        taskRepository.save(task);
    }

    private void executeOrchestrationTask(TaskExecution execution) {
        // 编排任务逻辑
        log.info("执行编排任务: {}", execution.getTaskCode());
    }

    private void executeAutoRemediationTask(TaskExecution execution) {
        // 自动修复任务逻辑
        log.info("执行自动修复任务: {}", execution.getTaskCode());
    }

    private void executeNotificationTask(TaskExecution execution) {
        // 通知任务逻辑
        log.info("执行通知任务: {}", execution.getTaskCode());
    }

    private void executeReportingTask(TaskExecution execution) {
        // 报告任务逻辑
        log.info("执行报告任务: {}", execution.getTaskCode());
    }

    private void executeDefaultTask(TaskExecution execution) {
        log.info("执行默认任务: {}", execution.getTaskCode());
    }

    /**
     * 触发下游任务
     */
    private void triggerDownstreamTasks(Long taskId) {
        GovernanceTask task = taskRepository.findById(taskId).orElse(null);
        if (task == null || task.getDownstreamTasks() == null) return;

        for (GovernanceTask downstreamTask : task.getDownstreamTasks()) {
            // 检查是否所有前置依赖都已完成
            boolean allDependenciesMet = downstreamTask.getUpstreamTasks().stream()
                    .allMatch(t -> "COMPLETED".equals(t.getTaskStatus()));

            if (allDependenciesMet && "PENDING".equals(downstreamTask.getTaskStatus())) {
                try {
                    executeTaskSync(downstreamTask.getId(), "SYSTEM");
                } catch (Exception e) {
                    log.error("触发下游任务失败: {}", downstreamTask.getTaskCode(), e);
                }
            }
        }
    }

    /**
     * 更新任务为失败状态
     */
    @Transactional
    public void updateTaskToFailed(Long taskId, String errorMessage) {
        taskRepository.findById(taskId).ifPresent(task -> {
            task.setTaskStatus("FAILED");
            task.setErrorMessage(errorMessage);
            task.setEndTime(LocalDateTime.now());
            taskRepository.save(task);
        });
    }

    /**
     * 批量执行任务
     */
    @Transactional
    public List<TaskExecutionResponse> batchExecuteTasks(List<Long> taskIds, String executor) {
        log.info("批量执行任务: 数量={}, 执行人={}", taskIds.size(), executor);

        // 拓扑排序
        List<GovernanceTask> sortedTasks = topologicalSort(taskIds);

        List<TaskExecutionResponse> results = new ArrayList<>();
        for (GovernanceTask task : sortedTasks) {
            try {
                TaskExecutionRequest request = TaskExecutionRequest.builder()
                        .taskId(task.getId())
                        .executionParams(task.getTaskParams())
                        .build();
                TaskExecutionResponse response = executeTask(request, executor);
                results.add(response);
            } catch (Exception e) {
                log.error("批量执行失败: 任务ID={}", task.getId(), e);
            }
        }

        return results;
    }

    /**
     * 拓扑排序
     */
    private List<GovernanceTask> topologicalSort(List<Long> taskIds) {
        List<GovernanceTask> tasks = taskRepository.findAllById(taskIds);
        Map<Long, GovernanceTask> taskMap = tasks.stream()
                .collect(Collectors.toMap(GovernanceTask::getId, t -> t));

        List<GovernanceTask> sorted = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Set<Long> visiting = new HashSet<>();

        for (Long taskId : taskIds) {
            if (visited.contains(taskId)) continue;
            topologicalSortDFS(taskId, taskMap, visited, visiting, sorted);
        }

        return sorted;
    }

    private void topologicalSortDFS(Long taskId, Map<Long, GovernanceTask> taskMap,
                                    Set<Long> visited, Set<Long> visiting, List<GovernanceTask> sorted) {
        if (visited.contains(taskId)) return;
        if (visiting.contains(taskId)) {
            throw new IllegalStateException("检测到循环依赖: " + taskId);
        }

        visiting.add(taskId);
        GovernanceTask task = taskMap.get(taskId);

        if (task != null && task.getUpstreamTasks() != null) {
            for (GovernanceTask upstream : task.getUpstreamTasks()) {
                topologicalSortDFS(upstream.getId(), taskMap, visited, visiting, sorted);
            }
        }

        visiting.remove(taskId);
        visited.add(taskId);

        if (task != null) {
            sorted.add(task);
        }
    }

    /**
     * 查询执行记录
     */
    public TaskExecutionResponse getExecution(Long executionId) {
        TaskExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("执行记录不存在: " + executionId));
        return toResponse(execution);
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private TaskExecutionResponse toResponse(TaskExecution execution) {
        return TaskExecutionResponse.builder()
                .id(execution.getId())
                .batchNo(execution.getBatchNo())
                .taskId(execution.getTask() != null ? execution.getTask().getId() : null)
                .taskCode(execution.getTaskCode())
                .taskName(execution.getTaskName())
                .taskType(execution.getTaskType())
                .executionStatus(execution.getExecutionStatus())
                .resultStatus(execution.getResultStatus())
                .startTime(execution.getStartTime())
                .endTime(execution.getEndTime())
                .executionTimeMs(execution.getExecutionTimeMs())
                .executor(execution.getExecutor())
                .createTime(execution.getCreateTime())
                .build();
    }
}
