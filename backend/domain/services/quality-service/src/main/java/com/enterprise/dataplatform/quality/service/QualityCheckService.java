package com.enterprise.dataplatform.quality.service;

import com.enterprise.dataplatform.quality.domain.entity.QualityCheckResult;
import com.enterprise.dataplatform.quality.domain.entity.QualityCheckTask;
import com.enterprise.dataplatform.quality.domain.entity.QualityRule;
import com.enterprise.dataplatform.quality.dto.request.CheckExecutionRequest;
import com.enterprise.dataplatform.quality.dto.response.QualityCheckResultResponse;
import com.enterprise.dataplatform.quality.repository.QualityCheckResultRepository;
import com.enterprise.dataplatform.quality.repository.QualityCheckTaskRepository;
import com.enterprise.dataplatform.quality.repository.QualityRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 质量检查执行服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QualityCheckService {

    private final QualityCheckTaskRepository taskRepository;
    private final QualityCheckResultRepository resultRepository;
    private final QualityRuleRepository ruleRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 正在执行的任务
     */
    private final Map<String, QualityCheckTask> executingTasks = new ConcurrentHashMap<>();

    /**
     * 执行质量检查
     */
    @Transactional
    public QualityCheckResultResponse executeCheck(CheckExecutionRequest request, String executor) {
        log.info("执行质量检查: 任务ID={}, 执行人={}", request.getTaskId(), executor);

        QualityCheckTask task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + request.getTaskId()));

        QualityRule rule = task.getRule();
        String batchNo = "CHECK-" + UUID.randomUUID().toString().substring(0, 8);

        // 创建检查结果记录
        QualityCheckResult result = QualityCheckResult.builder()
                .batchNo(batchNo)
                .task(task)
                .rule(rule)
                .ruleCode(rule.getRuleCode())
                .ruleName(rule.getRuleName())
                .ruleType(rule.getRuleType())
                .assetId(task.getAssetId())
                .assetName(task.getAssetName())
                .checkStatus("RUNNING")
                .checkResult("RUNNING")
                .checkTime(LocalDateTime.now())
                .executor(executor)
                .build();

        result = resultRepository.save(result);

        // 标记任务为执行中
        task.setTaskStatus("RUNNING");
        task.setLastExecutionTime(LocalDateTime.now());
        task.setLastExecutionStatus("RUNNING");
        taskRepository.save(task);

        executingTasks.put(batchNo, task);

        // 异步执行检查
        executeCheckAsync(result.getId(), request);

        return toResponse(result);
    }

    /**
     * 异步执行检查
     */
    public void executeCheckAsync(Long resultId, CheckExecutionRequest request) {
        log.info("异步执行质量检查: {}", resultId);
        long startTime = System.currentTimeMillis();

        try {
            QualityCheckResult result = resultRepository.findById(resultId)
                    .orElseThrow(() -> new IllegalArgumentException("结果不存在: " + resultId));

            // 模拟执行检查
            performCheck(result, request);

            result.setCheckStatus("COMPLETED");
            result.setCheckEndTime(LocalDateTime.now());
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

            // 更新任务状态
            updateTaskStatus(result.getTask().getId(), "COMPLETED");

            // 发送Kafka事件
            sendCheckCompletedEvent(result);

        } catch (Exception e) {
            log.error("质量检查执行失败: {}", resultId, e);
            updateResultToFailed(resultId, e.getMessage());
        }
    }

    /**
     * 执行检查逻辑
     */
    private void performCheck(QualityCheckResult result, CheckExecutionRequest request) {
        QualityRule rule = result.getRule();
        
        // 模拟数据检查
        long totalRecords = 1000L + new Random().nextInt(1000);
        long checkedRecords = totalRecords;
        long failedRecords = (long) (totalRecords * 0.05 * new Random().nextDouble());
        long passedRecords = totalRecords - failedRecords;
        
        double violationRate = (failedRecords * 100.0 / totalRecords);
        double qualityScore = 100.0 - violationRate;
        
        String checkResult = violationRate == 0 ? "PASS" : 
                (violationRate <= (rule.getAlertThreshold() != null ? rule.getAlertThreshold() : 5.0) ? "WARN" : "FAIL");

        result.setCheckResult(checkResult);
        result.setQualityScore(qualityScore);
        result.setTotalRecords(totalRecords);
        result.setCheckedRecords(checkedRecords);
        result.setPassedRecords(passedRecords);
        result.setFailedRecords(failedRecords);
        result.setViolationRate(violationRate);
        result.setThreshold(rule.getAlertThreshold());
        result.setExceedsThreshold(violationRate > (rule.getErrorThreshold() != null ? rule.getErrorThreshold() : 10.0));
        result.setCheckStartTime(LocalDateTime.now().minusMinutes(1));

        // 生成违规样本
        List<Map<String, Object>> samples = new ArrayList<>();
        for (int i = 0; i < Math.min(5, failedRecords); i++) {
            Map<String, Object> sample = new HashMap<>();
            sample.put("rowId", UUID.randomUUID().toString().substring(0, 8));
            sample.put("violationType", rule.getRuleType());
            sample.put("description", "违规记录样本 " + (i + 1));
            samples.add(sample);
        }
        result.setViolationSamples(toJson(samples));

        resultRepository.save(result);
    }

    /**
     * 批量执行检查
     */
    @Transactional
    public List<QualityCheckResultResponse> batchExecuteCheck(List<Long> taskIds, String executor) {
        log.info("批量执行质量检查: 任务数量={}, 执行人={}", taskIds.size(), executor);

        List<QualityCheckResultResponse> results = new ArrayList<>();

        for (Long taskId : taskIds) {
            try {
                CheckExecutionRequest request = CheckExecutionRequest.builder().taskId(taskId).build();
                QualityCheckResultResponse response = executeCheck(request, executor);
                results.add(response);
            } catch (Exception e) {
                log.error("批量检查执行失败: 任务ID={}", taskId, e);
            }
        }

        return results;
    }

    /**
     * 定时任务调度
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void scheduleTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<QualityCheckTask> tasks = taskRepository.findTasksToExecute(now);

        for (QualityCheckTask task : tasks) {
            if (!executingTasks.containsKey(task.getTaskCode())) {
                try {
                    CheckExecutionRequest request = CheckExecutionRequest.builder()
                            .taskId(task.getId())
                            .executionParams(task.getExecutionParams())
                            .build();
                    executeCheck(request, "SYSTEM");
                } catch (Exception e) {
                    log.error("定时任务执行失败: {}", task.getTaskCode(), e);
                }
            }
        }
    }

    /**
     * 查询检查结果
     */
    public QualityCheckResultResponse getCheckResult(Long id) {
        QualityCheckResult result = resultRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("结果不存在: " + id));
        return toResponse(result);
    }

    /**
     * 更新结果为失败
     */
    @Transactional
    public void updateResultToFailed(Long resultId, String errorMessage) {
        resultRepository.findById(resultId).ifPresent(result -> {
            result.setCheckStatus("FAILED");
            result.setCheckResult("ERROR");
            result.setErrorMessage(errorMessage);
            result.setCheckEndTime(LocalDateTime.now());
            resultRepository.save(result);

            updateTaskStatus(result.getTask().getId(), "FAILED");
        });
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(Long taskId, String status) {
        taskRepository.findById(taskId).ifPresent(task -> {
            task.setTaskStatus(status);
            task.setLastExecutionStatus(status);
            taskRepository.save(task);
            executingTasks.remove(task.getTaskCode());
        });
    }

    /**
     * 发送检查完成事件
     */
    private void sendCheckCompletedEvent(QualityCheckResult result) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "QUALITY_CHECK_COMPLETED");
            event.put("resultId", result.getId());
            event.put("batchNo", result.getBatchNo());
            event.put("ruleCode", result.getRuleCode());
            event.put("assetId", result.getAssetId());
            event.put("checkResult", result.getCheckResult());
            event.put("qualityScore", result.getQualityScore());
            event.put("checkTime", result.getCheckTime().toString());

            String message = toJson(event);
            kafkaTemplate.send("quality-check-events", result.getBatchNo(), message);

            log.info("发送质量检查完成事件: {}", result.getId());
        } catch (Exception e) {
            log.error("发送Kafka消息失败", e);
        }
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private QualityCheckResultResponse toResponse(QualityCheckResult result) {
        return QualityCheckResultResponse.builder()
                .id(result.getId())
                .batchNo(result.getBatchNo())
                .taskId(result.getTask() != null ? result.getTask().getId() : null)
                .ruleId(result.getRule() != null ? result.getRule().getId() : null)
                .ruleCode(result.getRuleCode())
                .ruleName(result.getRuleName())
                .ruleType(result.getRuleType())
                .assetId(result.getAssetId())
                .assetName(result.getAssetName())
                .checkStatus(result.getCheckStatus())
                .checkResult(result.getCheckResult())
                .qualityScore(result.getQualityScore())
                .totalRecords(result.getTotalRecords())
                .checkedRecords(result.getCheckedRecords())
                .passedRecords(result.getPassedRecords())
                .failedRecords(result.getFailedRecords())
                .violationRate(result.getViolationRate())
                .threshold(result.getThreshold())
                .exceedsThreshold(result.getExceedsThreshold())
                .checkStartTime(result.getCheckStartTime())
                .checkEndTime(result.getCheckEndTime())
                .executionTimeMs(result.getExecutionTimeMs())
                .executor(result.getExecutor())
                .createTime(result.getCreateTime())
                .checkTime(result.getCheckTime())
                .build();
    }
}
