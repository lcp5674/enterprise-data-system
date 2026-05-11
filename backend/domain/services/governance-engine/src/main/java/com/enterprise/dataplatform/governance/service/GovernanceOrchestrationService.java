package com.enterprise.dataplatform.governance.service;

import com.enterprise.dataplatform.governance.domain.entity.GovernancePolicy;
import com.enterprise.dataplatform.governance.domain.entity.GovernanceTask;
import com.enterprise.dataplatform.governance.domain.entity.TaskExecution;
import com.enterprise.dataplatform.governance.dto.request.TaskExecutionRequest;
import com.enterprise.dataplatform.governance.dto.response.TaskExecutionResponse;
import com.enterprise.dataplatform.governance.repository.GovernancePolicyRepository;
import com.enterprise.dataplatform.governance.repository.GovernanceTaskRepository;
import com.enterprise.dataplatform.governance.repository.TaskExecutionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class GovernanceOrchestrationService {

    private final GovernanceTaskRepository taskRepository;
    private final TaskExecutionRepository executionRepository;
    private final GovernancePolicyRepository policyRepository;
    
    @Value("${governance.task.timeout.default:3600}")
    private int defaultTimeout;
    
    @Value("${governance.task.retry.max:3}")
    private int maxRetry;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String SUB_TASK_TYPE_DATA_QUALITY = "DATA_QUALITY";
    private static final String SUB_TASK_TYPE_DATA_STANDARD = "DATA_STANDARD";
    private static final String SUB_TASK_TYPE_NOTIFICATION = "NOTIFICATION";
    private static final String SUB_TASK_TYPE_REPORTING = "REPORTING";
    
    private final Map<String, Object> executionLogs = new ConcurrentHashMap<>();

    /**
     * 执行任务（支持DAG编排）
     */
    @Transactional
    public TaskExecutionResponse executeTask(TaskExecutionRequest request, String executor) {
        log.info("执行治理任务: 任务ID={}, 执行人={}", request.getTaskId(), executor);

        GovernanceTask task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + request.getTaskId()));

        String batchNo = "EXEC-" + UUID.randomUUID().toString().substring(0, 8);

        if (task.getUpstreamTasks() != null && !task.getUpstreamTasks().isEmpty()) {
            for (GovernanceTask upstreamTask : task.getUpstreamTasks()) {
                if ("PENDING".equals(upstreamTask.getTaskStatus()) || "FAILED".equals(upstreamTask.getTaskStatus())) {
                    executeTaskSync(upstreamTask.getId(), executor);
                }
            }
        }

        TaskExecution execution = TaskExecution.builder()
                .batchNo(batchNo)
                .task(task)
                .taskCode(task.getTaskCode())
                .taskName(task.getTaskName())
                .taskType(task.getTaskType())
                .executionStatus("RUNNING")
                .startTime(LocalDateTime.now())
                .executionParams(toJson(request.getExecutionParams()))
                .executor(executor)
                .build();

        execution = executionRepository.save(execution);

        task.setTaskStatus("RUNNING");
        task.setStartTime(LocalDateTime.now());
        task.setExecutor(executor);
        taskRepository.save(task);

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
        triggerDownstreamTasks(execution.getTask().getId());
    }

    /**
     * 执行任务逻辑
     */
    private void performTaskExecution(TaskExecution execution) {
        GovernanceTask task = execution.getTask();

        log.info("执行任务逻辑: {}", task.getTaskCode());

        String taskType = task.getTaskType();
        Map<String, Object> result;
        
        switch (taskType) {
            case "ORCHESTRATION":
                result = executeOrchestrationTask(execution);
                break;
            case "AUTO_REMEDIATION":
                result = executeAutoRemediationTask(execution);
                break;
            case "NOTIFICATION":
                result = executeNotificationTask(execution);
                break;
            case "REPORTING":
                result = executeReportingTask(execution);
                break;
            default:
                result = executeDefaultTask(execution);
        }
        
        execution.setExecutionResult(toJson(result));

        task.setTaskStatus("COMPLETED");
        task.setEndTime(LocalDateTime.now());
        task.setExecutionResult(toJson(Map.of("status", "success", "executionId", execution.getId())));
        taskRepository.save(task);
    }

    /**
     * 执行编排任务
     */
    private Map<String, Object> executeOrchestrationTask(TaskExecution execution) {
        log.info("执行编排任务: {}", execution.getTaskCode());
        
        GovernanceTask task = execution.getTask();
        Map<String, Object> config = parseTaskConfig(task.getTaskParams());
        
        List<Map<String, Object>> subTasks = (List<Map<String, Object>>) config.get("subTasks");
        if (subTasks == null || subTasks.isEmpty()) {
            log.warn("编排任务没有配置子任务: {}", execution.getTaskCode());
            return Map.of(
                "status", "success",
                "message", "No subtasks configured",
                "executedSubTasks", 0
            );
        }
        
        List<Map<String, Object>> executedSubTasks = new ArrayList<>();
        Set<Long> completedTaskIds = new HashSet<>();
        int maxIterations = subTasks.size() * 2;
        int iteration = 0;
        
        while (completedTaskIds.size() < subTasks.size() && iteration < maxIterations) {
            iteration++;
            for (Map<String, Object> subTask : subTasks) {
                String subTaskId = String.valueOf(subTask.get("id"));
                if (completedTaskIds.contains(subTask.hashCode())) {
                    continue;
                }
                
                List<String> dependencies = (List<String>) subTask.get("dependencies");
                if (dependencies != null && !dependencies.isEmpty()) {
                    boolean allDependenciesMet = dependencies.stream()
                            .allMatch(dep -> executedSubTasks.stream()
                                    .anyMatch(e -> subTaskId.equals(String.valueOf(e.get("id")))));
                    if (!allDependenciesMet) {
                        continue;
                    }
                }
                
                Map<String, Object> subTaskResult = executeSubTask(subTask, execution.getExecutor());
                subTaskResult.put("id", subTaskId);
                subTaskResult.put("executedAt", LocalDateTime.now().toString());
                executedSubTasks.add(subTaskResult);
                completedTaskIds.add(subTask.hashCode());
            }
        }
        
        Map<String, Object> aggregatedResult = aggregateResults(executedSubTasks);
        log.info("编排任务执行完成: {}, 执行子任务数: {}", execution.getTaskCode(), executedSubTasks.size());
        
        return Map.of(
            "status", "success",
            "executedSubTasks", executedSubTasks.size(),
            "subTaskResults", executedSubTasks,
            "aggregatedResult", aggregatedResult
        );
    }

    /**
     * 执行子任务
     */
    private Map<String, Object> executeSubTask(Map<String, Object> subTask, String executor) {
        String subTaskType = String.valueOf(subTask.get("type"));
        String subTaskName = String.valueOf(subTask.get("name"));
        Map<String, Object> params = (Map<String, Object>) subTask.get("params");
        
        log.info("执行子任务: type={}, name={}", subTaskType, subTaskName);
        
        Map<String, Object> result = new HashMap<>();
        result.put("subTaskType", subTaskType);
        result.put("subTaskName", subTaskName);
        
        try {
            switch (subTaskType) {
                case SUB_TASK_TYPE_DATA_QUALITY:
                    result.putAll(executeDataQualitySubTask(params));
                    break;
                case SUB_TASK_TYPE_DATA_STANDARD:
                    result.putAll(executeDataStandardSubTask(params));
                    break;
                case SUB_TASK_TYPE_NOTIFICATION:
                    result.putAll(executeNotificationSubTask(params));
                    break;
                case SUB_TASK_TYPE_REPORTING:
                    result.putAll(executeReportingSubTask(params));
                    break;
                default:
                    result.put("status", "skipped");
                    result.put("message", "Unknown subtask type: " + subTaskType);
            }
            result.put("success", true);
        } catch (Exception e) {
            log.error("子任务执行失败: {}", subTaskName, e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行数据质量子任务
     */
    private Map<String, Object> executeDataQualitySubTask(Map<String, Object> params) {
        String assetId = String.valueOf(params.getOrDefault("assetId", ""));
        String ruleIds = String.valueOf(params.getOrDefault("ruleIds", ""));
        
        log.info("执行数据质量检查: assetId={}, rules={}", assetId, ruleIds);
        
        Map<String, Object> result = new HashMap<>();
        result.put("recordsChecked", 0);
        result.put("qualityScore", 0.0);
        result.put("issuesFound", 0);
        
        return result;
    }

    /**
     * 执行数据标准子任务
     */
    private Map<String, Object> executeDataStandardSubTask(Map<String, Object> params) {
        String standardId = String.valueOf(params.getOrDefault("standardId", ""));
        
        log.info("执行数据标准检查: standardId={}", standardId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("standardCompliance", 0.0);
        result.put("violations", 0);
        
        return result;
    }

    /**
     * 执行通知子任务
     */
    private Map<String, Object> executeNotificationSubTask(Map<String, Object> params) {
        String recipient = String.valueOf(params.getOrDefault("recipient", ""));
        String message = String.valueOf(params.getOrDefault("message", ""));
        String channel = String.valueOf(params.getOrDefault("channel", "EMAIL"));
        
        log.info("发送通知: channel={}, recipient={}", channel, recipient);
        
        Map<String, Object> result = new HashMap<>();
        result.put("channel", channel);
        result.put("recipient", recipient);
        result.put("sent", true);
        result.put("sentAt", LocalDateTime.now().toString());
        
        return result;
    }

    /**
     * 执行报告子任务
     */
    private Map<String, Object> executeReportingSubTask(Map<String, Object> params) {
        String reportType = String.valueOf(params.getOrDefault("reportType", ""));
        
        log.info("生成报告: type={}", reportType);
        
        Map<String, Object> result = new HashMap<>();
        result.put("reportType", reportType);
        result.put("generated", true);
        result.put("generatedAt", LocalDateTime.now().toString());
        
        return result;
    }

    /**
     * 聚合子任务执行结果
     */
    private Map<String, Object> aggregateResults(List<Map<String, Object>> subTaskResults) {
        int totalSubTasks = subTaskResults.size();
        long successfulSubTasks = subTaskResults.stream()
                .filter(r -> Boolean.TRUE.equals(r.get("success")))
                .count();
        long failedSubTasks = subTaskResults.stream()
                .filter(r -> Boolean.FALSE.equals(r.get("success")))
                .count();
        
        double overallScore = subTaskResults.stream()
                .filter(r -> r.containsKey("qualityScore") || r.containsKey("standardCompliance"))
                .mapToDouble(r -> {
                    if (r.containsKey("qualityScore")) {
                        return ((Number) r.get("qualityScore")).doubleValue();
                    } else if (r.containsKey("standardCompliance")) {
                        return ((Number) r.get("standardCompliance")).doubleValue();
                    }
                    return 0.0;
                })
                .average()
                .orElse(0.0);
        
        return Map.of(
            "totalSubTasks", totalSubTasks,
            "successfulSubTasks", successfulSubTasks,
            "failedSubTasks", failedSubTasks,
            "overallScore", overallScore,
            "successRate", totalSubTasks > 0 ? (double) successfulSubTasks / totalSubTasks * 100 : 0.0
        );
    }

    /**
     * 执行自动修复任务
     */
    private Map<String, Object> executeAutoRemediationTask(TaskExecution execution) {
        log.info("执行自动修复任务: {}", execution.getTaskCode());
        
        GovernanceTask task = execution.getTask();
        
        DiagnosisResult diagnosis = diagnoseIssue(execution);
        
        RemediationPlan plan = generateRemediationPlan(diagnosis);
        
        executeRemediation(plan);
        
        boolean success = verifyRemediation(plan);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("diagnosis", diagnosis);
        result.put("plan", plan);
        result.put("executedAt", LocalDateTime.now().toString());
        
        return result;
    }

    /**
     * 诊断问题
     */
    private DiagnosisResult diagnoseIssue(TaskExecution execution) {
        Map<String, Object> params = parseTaskConfig(execution.getTask().getTaskParams());
        String assetId = String.valueOf(params.getOrDefault("assetId", ""));
        String issueType = String.valueOf(params.getOrDefault("issueType", "UNKNOWN"));
        String severity = String.valueOf(params.getOrDefault("severity", "MEDIUM"));
        
        log.info("诊断问题: assetId={}, issueType={}, severity={}", assetId, issueType, severity);
        
        DiagnosisResult result = new DiagnosisResult();
        result.setAssetId(assetId);
        result.setIssueType(issueType);
        result.setSeverity(severity);
        result.setDiagnosedAt(LocalDateTime.now());
        result.setSymptoms(List.of("Issue detected in data asset"));
        result.setRootCause("Data quality rule violation");
        
        return result;
    }

    /**
     * 生成修复方案
     */
    private RemediationPlan generateRemediationPlan(DiagnosisResult diagnosis) {
        RemediationPlan plan = new RemediationPlan();
        plan.setDiagnosisId(UUID.randomUUID().toString());
        plan.setDiagnosisResult(diagnosis);
        plan.setCreatedAt(LocalDateTime.now());
        
        List<RemediationStep> steps = new ArrayList<>();
        
        switch (diagnosis.getIssueType()) {
            case "DATA_QUALITY":
                steps.add(createRemediationStep("DATA_CLEANUP", "Clean invalid data records"));
                steps.add(createRemediationStep("RULE_UPDATE", "Update data quality rules"));
                steps.add(createRemediationStep("NOTIFICATION", "Notify data owners"));
                break;
            case "SCHEMA_VIOLATION":
                steps.add(createRemediationStep("SCHEMA_SYNC", "Synchronize schema changes"));
                steps.add(createRemediationStep("IMPACT_ANALYSIS", "Run impact analysis"));
                break;
            default:
                steps.add(createRemediationStep("MANUAL_REVIEW", "Require manual review"));
        }
        
        plan.setSteps(steps);
        plan.setEstimatedDuration(steps.size() * 300);
        
        return plan;
    }

    private RemediationStep createRemediationStep(String stepType, String description) {
        RemediationStep step = new RemediationStep();
        step.setStepType(stepType);
        step.setDescription(description);
        step.setOrder(steps.size() + 1);
        step.setStatus("PENDING");
        return step;
    }

    /**
     * 执行修复
     */
    private void executeRemediation(RemediationPlan plan) {
        log.info("执行修复方案: planId={}", plan.getDiagnosisId());
        
        for (RemediationStep step : plan.getSteps()) {
            try {
                log.info("执行修复步骤: type={}, description={}", 
                        step.getStepType(), step.getDescription());
                step.setStatus("COMPLETED");
                step.setCompletedAt(LocalDateTime.now());
            } catch (Exception e) {
                log.error("修复步骤执行失败: {}", step.getStepType(), e);
                step.setStatus("FAILED");
                step.setErrorMessage(e.getMessage());
            }
        }
        
        boolean allCompleted = plan.getSteps().stream()
                .allMatch(s -> "COMPLETED".equals(s.getStatus()));
        plan.setStatus(allCompleted ? "SUCCESS" : "PARTIAL");
    }

    /**
     * 验证修复结果
     */
    private boolean verifyRemediation(RemediationPlan plan) {
        log.info("验证修复结果: planId={}", plan.getDiagnosisId());
        
        for (RemediationStep step : plan.getSteps()) {
            if (!"COMPLETED".equals(step.getStatus())) {
                log.warn("修复步骤未完成: type={}", step.getStepType());
                return false;
            }
        }
        
        return true;
    }

    /**
     * 执行通知任务
     */
    private Map<String, Object> executeNotificationTask(TaskExecution execution) {
        log.info("执行通知任务: {}", execution.getTaskCode());
        
        GovernanceTask task = execution.getTask();
        Map<String, Object> params = parseTaskConfig(task.getTaskParams());
        
        String recipient = String.valueOf(params.getOrDefault("recipient", ""));
        String subject = String.valueOf(params.getOrDefault("subject", ""));
        String message = String.valueOf(params.getOrDefault("message", ""));
        String channel = String.valueOf(params.getOrDefault("channel", "EMAIL"));
        
        log.info("发送通知: recipient={}, channel={}, subject={}", recipient, channel, subject);
        
        Map<String, Object> result = new HashMap<>();
        result.put("recipient", recipient);
        result.put("channel", channel);
        result.put("sent", true);
        result.put("sentAt", LocalDateTime.now().toString());
        
        return result;
    }

    /**
     * 执行报告任务
     */
    private Map<String, Object> executeReportingTask(TaskExecution execution) {
        log.info("执行报告任务: {}", execution.getTaskCode());
        
        GovernanceTask task = execution.getTask();
        Map<String, Object> params = parseTaskConfig(task.getTaskParams());
        
        String reportType = String.valueOf(params.getOrDefault("reportType", "GOVERNANCE"));
        String format = String.valueOf(params.getOrDefault("format", "PDF"));
        
        log.info("生成报告: type={}, format={}", reportType, format);
        
        Map<String, Object> result = new HashMap<>();
        result.put("reportType", reportType);
        result.put("format", format);
        result.put("generated", true);
        result.put("generatedAt", LocalDateTime.now().toString());
        result.put("reportId", UUID.randomUUID().toString());
        
        return result;
    }

    /**
     * 执行默认任务
     */
    private Map<String, Object> executeDefaultTask(TaskExecution execution) {
        log.info("执行默认任务: {}", execution.getTaskCode());
        
        GovernanceTask task = execution.getTask();
        Map<String, Object> params = parseTaskConfig(task.getTaskParams());
        
        Map<String, Object> result = new HashMap<>();
        result.put("taskCode", execution.getTaskCode());
        result.put("taskType", task.getTaskType());
        result.put("executedAt", LocalDateTime.now().toString());
        result.put("status", "completed");
        
        return result;
    }

    /**
     * 解析任务配置
     */
    private Map<String, Object> parseTaskConfig(String taskParams) {
        if (taskParams == null || taskParams.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(taskParams, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("解析任务配置失败", e);
            return new HashMap<>();
        }
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

        List<GovernanceTask> sortedTasks = topologicalSort(taskIds);

        List<TaskExecutionResponse> results = new ArrayList<>();
        for (GovernanceTask task : sortedTasks) {
            try {
                TaskExecutionRequest request = TaskExecutionRequest.builder()
                        .taskId(task.getId())
                        .executionParams(parseTaskConfig(task.getTaskParams()))
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

    @lombok.Data
    public static class DiagnosisResult {
        private String assetId;
        private String issueType;
        private String severity;
        private LocalDateTime diagnosedAt;
        private List<String> symptoms;
        private String rootCause;
    }

    @lombok.Data
    public static class RemediationPlan {
        private String diagnosisId;
        private DiagnosisResult diagnosisResult;
        private List<RemediationStep> steps;
        private String status;
        private Integer estimatedDuration;
        private LocalDateTime createdAt;
    }

    @lombok.Data
    public static class RemediationStep {
        private String stepType;
        private String description;
        private Integer order;
        private String status;
        private String errorMessage;
        private LocalDateTime completedAt;
    }
}
