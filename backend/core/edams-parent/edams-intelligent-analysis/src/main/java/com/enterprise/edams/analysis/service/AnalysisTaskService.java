package com.enterprise.edams.analysis.service;

import com.enterprise.edams.analysis.context.AnalysisContext;
import com.enterprise.edams.analysis.dto.request.CreateTaskRequest;
import com.enterprise.edams.analysis.dto.request.TaskQueryDTO;
import com.enterprise.edams.analysis.dto.request.UpdateTaskRequest;
import com.enterprise.edams.analysis.dto.response.AnalysisResultResponse;
import com.enterprise.edams.analysis.dto.response.TaskProgressVO;
import com.enterprise.edams.analysis.dto.response.TaskResponse;
import com.enterprise.edams.analysis.entity.AnalysisResult;
import com.enterprise.edams.analysis.entity.AnalysisTask;
import com.enterprise.edams.analysis.entity.ExecutionMode;
import com.enterprise.edams.analysis.entity.TaskStatus;
import com.enterprise.edams.analysis.exception.AnalysisException;
import com.enterprise.edams.analysis.metadata.TableMetadata;
import com.enterprise.edams.analysis.repository.AnalysisResultRepository;
import com.enterprise.edams.analysis.repository.AnalysisTaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisTaskService {

    private final AnalysisTaskRepository taskRepository;
    private final AnalysisResultRepository resultRepository;
    private final IntelligentAnalyzerService analyzerService;
    private final DatasourceScannerService scannerService;
    private final ObjectMapper objectMapper;

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        log.info("Creating analysis task: {}", request.getTaskName());

        String taskCode = generateTaskCode();

        List<String> targetTables = request.getTargetTables();
        List<String> excludedTables = request.getExcludedTables();

        AnalysisTask task = AnalysisTask.builder()
                .taskCode(taskCode)
                .taskName(request.getTaskName())
                .taskDescription(request.getTaskDescription())
                .datasourceId(request.getDatasourceId())
                .datasourceName(request.getDatasourceName())
                .datasourceType(request.getDatasourceType())
                .schemaName(request.getSchemaName())
                .modelConfigId(request.getModelConfigId())
                .targetTables(serializeList(targetTables))
                .excludedTables(serializeList(excludedTables))
                .batchSize(request.getBatchSize() != null ? request.getBatchSize() : 5)
                .enableLineageAnalysis(request.getEnableLineageAnalysis() != null ? request.getEnableLineageAnalysis() : true)
                .enableIndicatorExtraction(request.getEnableIndicatorExtraction() != null ? request.getEnableIndicatorExtraction() : true)
                .enableSubjectClassification(request.getEnableSubjectClassification() != null ? request.getEnableSubjectClassification() : true)
                .autoRegister(request.getAutoRegister() != null ? request.getAutoRegister() : false)
                .sampleRowCount(request.getSampleRowCount() != null ? request.getSampleRowCount() : 100)
                .executionMode(request.getExecutionMode() != null ? request.getExecutionMode() : ExecutionMode.MANUAL)
                .cronExpression(request.getCronExpression())
                .scheduledTime(request.getScheduledTime())
                .executor(request.getExecutor())
                .status(TaskStatus.PENDING)
                .build();

        task = taskRepository.save(task);
        log.info("Analysis task created: id={}, code={}", task.getId(), task.getTaskCode());

        return TaskResponse.fromEntity(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        log.info("Updating analysis task: {}", id);

        AnalysisTask task = taskRepository.findById(id)
                .orElseThrow(() -> new AnalysisException("TASK_NOT_FOUND", "任务不存在: " + id));

        if (task.isRunning()) {
            throw new AnalysisException("TASK_RUNNING", "任务正在执行中，无法修改");
        }

        if (request.getTaskName() != null) {
            task.setTaskName(request.getTaskName());
        }
        if (request.getTaskDescription() != null) {
            task.setTaskDescription(request.getTaskDescription());
        }
        if (request.getModelConfigId() != null) {
            task.setModelConfigId(request.getModelConfigId());
        }
        if (request.getTargetTables() != null) {
            task.setTargetTables(serializeList(request.getTargetTables()));
        }
        if (request.getExcludedTables() != null) {
            task.setExcludedTables(serializeList(request.getExcludedTables()));
        }
        if (request.getBatchSize() != null) {
            task.setBatchSize(request.getBatchSize());
        }
        if (request.getEnableLineageAnalysis() != null) {
            task.setEnableLineageAnalysis(request.getEnableLineageAnalysis());
        }
        if (request.getEnableIndicatorExtraction() != null) {
            task.setEnableIndicatorExtraction(request.getEnableIndicatorExtraction());
        }
        if (request.getEnableSubjectClassification() != null) {
            task.setEnableSubjectClassification(request.getEnableSubjectClassification());
        }
        if (request.getAutoRegister() != null) {
            task.setAutoRegister(request.getAutoRegister());
        }
        if (request.getSampleRowCount() != null) {
            task.setSampleRowCount(request.getSampleRowCount());
        }
        if (request.getCronExpression() != null) {
            task.setCronExpression(request.getCronExpression());
        }
        if (request.getExecutor() != null) {
            task.setExecutor(request.getExecutor());
        }

        task = taskRepository.save(task);
        return TaskResponse.fromEntity(task);
    }

    @Transactional
    public void startTask(Long taskId) {
        log.info("Starting task manually: {}", taskId);

        AnalysisTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AnalysisException("TASK_NOT_FOUND", "任务不存在: " + taskId));

        if (!task.canStart()) {
            throw new AnalysisException("INVALID_STATUS", "任务当前状态不允许启动: " + task.getStatus());
        }

        executeTaskAsync(taskId);
    }

    @Async
    public void executeTaskAsync(Long taskId) {
        log.info("Executing task asynchronously: {}", taskId);

        AnalysisTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AnalysisException("TASK_NOT_FOUND", "任务不存在: " + taskId));

        long startTime = System.currentTimeMillis();

        try {
            taskRepository.markAsRunning(taskId, LocalDateTime.now(), LocalDateTime.now());

            List<String> allTables = scannerService.scanTables(
                    task.getDatasourceId(),
                    task.getSchemaName(),
                    deserializeList(task.getTargetTables()),
                    deserializeList(task.getExcludedTables())
            );

            task.setTotalTables(allTables.size());

            List<List<String>> batches = partitionList(allTables, task.getBatchSize());
            task.setTotalBatches(batches.size());

            taskRepository.save(task);

            log.info("Task {} will process {} tables in {} batches", taskId, allTables.size(), batches.size());

            AnalysisContext context = buildAnalysisContext(task);

            for (int i = 0; i < batches.size(); i++) {
                List<String> batch = batches.get(i);
                int batchNumber = i + 1;

                log.info("Executing batch {}/{} for task {}", batchNumber, batches.size(), taskId);

                executeBatch(task, batch, batchNumber, context);

                task.setCompletedBatches(batchNumber);
                if (task.getTotalBatches() > 0) {
                    task.setProgress((batchNumber * 100) / task.getTotalBatches());
                }
                taskRepository.save(task);

                if (Thread.currentThread().isInterrupted()) {
                    log.warn("Task {} interrupted", taskId);
                    task.setStatus(TaskStatus.CANCELLED);
                    taskRepository.save(task);
                    return;
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;
            taskRepository.markAsCompleted(taskId, TaskStatus.COMPLETED, LocalDateTime.now(), executionTime, LocalDateTime.now());

            log.info("Task {} completed successfully in {}ms", taskId, executionTime);

        } catch (Exception e) {
            log.error("Task {} failed: {}", taskId, e.getMessage(), e);

            task.setStatus(TaskStatus.FAILED);
            task.setLastErrorMessage(e.getMessage());
            task.setEndTime(LocalDateTime.now());
            task.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            taskRepository.save(task);

            throw e;
        }
    }

    private void executeBatch(AnalysisTask task, List<String> tables, int batchNumber, AnalysisContext context) {
        for (String tableName : tables) {
            long tableStartTime = System.currentTimeMillis();

            try {
                TableMetadata metadata = scannerService.getTableMetadata(
                        task.getDatasourceId(),
                        task.getSchemaName(),
                        tableName,
                        task.getSampleRowCount()
                );

                AnalysisResult result = analyzerService.analyzeTable(context, tableName, metadata);

                result.setTaskId(task.getId());
                result.setTaskCode(task.getTaskCode());
                result.setBatchNumber(batchNumber);
                result.setAnalyzedAt(LocalDateTime.now());
                result.setAnalysisTimeMs(System.currentTimeMillis() - tableStartTime);

                resultRepository.save(result);

                task.incrementSuccess();

                log.debug("Table {} analyzed successfully in {}ms", tableName, System.currentTimeMillis() - tableStartTime);

            } catch (Exception e) {
                log.error("Failed to analyze table {}: {}", tableName, e.getMessage(), e);

                AnalysisResult failedResult = AnalysisResult.builder()
                        .taskId(task.getId())
                        .taskCode(task.getTaskCode())
                        .tableName(tableName)
                        .schemaName(task.getSchemaName())
                        .success(false)
                        .errorMessage(e.getMessage())
                        .batchNumber(batchNumber)
                        .analyzedAt(LocalDateTime.now())
                        .analysisTimeMs(System.currentTimeMillis() - tableStartTime)
                        .build();

                resultRepository.save(failedResult);
                task.incrementFailure();
            }

            taskRepository.save(task);
        }
    }

    @Transactional
    public void pauseTask(Long taskId) {
        log.info("Pausing task: {}", taskId);

        AnalysisTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AnalysisException("TASK_NOT_FOUND", "任务不存在: " + taskId));

        if (task.getStatus() != TaskStatus.RUNNING) {
            throw new AnalysisException("INVALID_STATUS", "只有正在执行的任务才能暂停");
        }

        task.setStatus(TaskStatus.PAUSED);
        taskRepository.save(task);

        log.info("Task paused: {}", taskId);
    }

    @Transactional
    public void resumeTask(Long taskId) {
        log.info("Resuming task: {}", taskId);

        AnalysisTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AnalysisException("TASK_NOT_FOUND", "任务不存在: " + taskId));

        if (task.getStatus() != TaskStatus.PAUSED) {
            throw new AnalysisException("INVALID_STATUS", "只有暂停的任务才能恢复");
        }

        task.setStatus(TaskStatus.PENDING);
        taskRepository.save(task);

        executeTaskAsync(taskId);
    }

    @Transactional
    public void cancelTask(Long taskId) {
        log.info("Cancelling task: {}", taskId);

        AnalysisTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AnalysisException("TASK_NOT_FOUND", "任务不存在: " + taskId));

        if (task.isTerminal()) {
            throw new AnalysisException("INVALID_STATUS", "任务已结束，无法取消");
        }

        task.setStatus(TaskStatus.CANCELLED);
        taskRepository.save(task);

        log.info("Task cancelled: {}", taskId);
    }

    public TaskResponse getTask(Long id) {
        AnalysisTask task = taskRepository.findById(id)
                .orElseThrow(() -> new AnalysisException("TASK_NOT_FOUND", "任务不存在: " + id));
        return TaskResponse.fromEntity(task);
    }

    public Page<TaskResponse> listTasks(TaskQueryDTO query, Pageable pageable) {
        Specification<AnalysisTask> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
                Predicate keywordPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(root.get("taskName"), "%" + query.getKeyword() + "%"),
                        criteriaBuilder.like(root.get("taskCode"), "%" + query.getKeyword() + "%")
                );
                predicates.add(keywordPredicate);
            }

            if (query.getStatus() != null && !query.getStatus().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), TaskStatus.valueOf(query.getStatus())));
            }

            if (query.getExecutionMode() != null && !query.getExecutionMode().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("executionMode"), ExecutionMode.valueOf(query.getExecutionMode())));
            }

            if (query.getDatasourceId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("datasourceId"), query.getDatasourceId()));
            }

            if (query.getExecutor() != null && !query.getExecutor().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("executor"), query.getExecutor()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return taskRepository.findAll(spec, pageable).map(TaskResponse::fromEntity);
    }

    public TaskProgressVO getTaskProgress(Long taskId) {
        AnalysisTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AnalysisException("TASK_NOT_FOUND", "任务不存在: " + taskId));

        Double tablesPerMinute = null;
        if (task.getStartTime() != null && task.getAnalyzedTables() != null && task.getAnalyzedTables() > 0) {
            long minutes = java.time.Duration.between(task.getStartTime(), LocalDateTime.now()).toMinutes();
            if (minutes > 0) {
                tablesPerMinute = (double) task.getAnalyzedTables() / minutes;
            }
        }

        String estimatedTime = null;
        if (tablesPerMinute != null && tablesPerMinute > 0 && task.getTotalTables() != null) {
            int remainingTables = task.getTotalTables() - task.getAnalyzedTables();
            long remainingMinutes = (long) (remainingTables / tablesPerMinute);
            estimatedTime = remainingMinutes + "分钟";
        }

        return TaskProgressVO.builder()
                .taskId(task.getId())
                .taskCode(task.getTaskCode())
                .taskName(task.getTaskName())
                .progress(task.getProgress())
                .totalTables(task.getTotalTables())
                .analyzedTables(task.getAnalyzedTables())
                .totalBatches(task.getTotalBatches())
                .completedBatches(task.getCompletedBatches())
                .successCount(task.getSuccessCount())
                .failureCount(task.getFailureCount())
                .status(task.getStatus().name())
                .estimatedTimeRemaining(estimatedTime)
                .tablesPerMinute(tablesPerMinute)
                .build();
    }

    public Page<AnalysisResultResponse> getTaskResults(Long taskId, Pageable pageable) {
        if (!taskRepository.existsById(taskId)) {
            throw new AnalysisException("TASK_NOT_FOUND", "任务不存在: " + taskId);
        }

        return resultRepository.findByTaskId(taskId, pageable)
                .map(AnalysisResultResponse::fromEntity);
    }

    @Scheduled(cron = "${analysis.task.schedule-cron:0 * * * * ?}")
    public void executeScheduledTasks() {
        log.debug("Checking scheduled tasks...");

        List<AnalysisTask> tasks = taskRepository.findTasksReadyToExecute();

        for (AnalysisTask task : tasks) {
            log.info("Executing scheduled task: {}", task.getTaskCode());
            executeTaskAsync(task.getId());
        }
    }

    private AnalysisContext buildAnalysisContext(AnalysisTask task) {
        return AnalysisContext.builder()
                .taskId(task.getId())
                .datasourceId(task.getDatasourceId())
                .modelConfigId(task.getModelConfigId())
                .schema(task.getSchemaName())
                .sampleRowCount(task.getSampleRowCount())
                .enableLineageAnalysis(task.getEnableLineageAnalysis())
                .enableIndicatorExtraction(task.getEnableIndicatorExtraction())
                .enableSubjectClassification(task.getEnableSubjectClassification())
                .autoRegister(task.getAutoRegister())
                .executor(task.getExecutor())
                .build();
    }

    private List<List<String>> partitionList(List<String> list, int batchSize) {
        List<List<String>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }

    private String generateTaskCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "TASK-" + timestamp;
    }

    private String serializeList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize list", e);
            return null;
        }
    }

    private List<String> deserializeList(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize list", e);
            return new ArrayList<>();
        }
    }
}
