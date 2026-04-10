package com.enterprise.edams.workflow.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.workflow.entity.ProcessTask;
import com.enterprise.edams.workflow.repository.ProcessTaskRepository;
import com.enterprise.edams.workflow.service.ProcessTaskService;
import com.enterprise.edams.workflow.service.impl.ProcessTaskServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 任务提醒定时任务
 *
 * @author EDAMS Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskReminderJob {

    private final ProcessTaskRepository processTaskRepository;
    private final ProcessTaskService processTaskService;
    private final ProcessTaskServiceImpl processTaskServiceImpl;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 即将到期提醒阈值（小时）
     */
    @Value("${workflow.reminder.due-soon-hours:1}")
    private int dueSoonHours;

    /**
     * 即将到期提醒最大阈值（小时）
     */
    @Value("${workflow.reminder.due-warning-hours:24}")
    private int dueWarningHours;

    /**
     * 未处理超时阈值（天）
     */
    @Value("${workflow.reminder.timeout-days:7}")
    private int timeoutDays;

    /**
     * 批量处理大小
     */
    @Value("${workflow.reminder.batch-size:100}")
    private int batchSize;

    /**
     * Kafka主题 - 任务提醒
     */
    @Value("${workflow.kafka.topic.task-reminder:edams-task-reminder}")
    private String taskReminderTopic;

    /**
     * 每小时检查一次即将超时的任务
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkOverdueTasks() {
        log.info("开始检查即将超时的任务");

        long startTime = System.currentTimeMillis();
        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            // 1. 查询即将到期的任务（1小时内到期）
            List<ProcessTask> dueSoonTasks = findTasksDueSoon();
            log.info("查询到即将到期的任务数量: {}", dueSoonTasks.size());
            totalCount += dueSoonTasks.size();

            // 2. 批量发送即将到期提醒
            for (ProcessTask task : dueSoonTasks) {
                try {
                    processTaskServiceImpl.sendTaskReminder(task.getId());
                    successCount++;
                } catch (Exception e) {
                    log.error("发送即将到期提醒失败: taskId={}", task.getId(), e);
                    failCount++;
                }
            }

            // 3. 查询超时未处理的任务
            List<ProcessTask> overdueTasks = findOverdueTasks();
            log.info("查询到超时未处理的任务数量: {}", overdueTasks.size());
            totalCount += overdueTasks.size();

            // 4. 批量发送超时提醒
            for (ProcessTask task : overdueTasks) {
                try {
                    processTaskServiceImpl.sendTaskReminder(task.getId());
                    successCount++;
                } catch (Exception e) {
                    log.error("发送超时提醒失败: taskId={}", task.getId(), e);
                    failCount++;
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("即将超时任务检查完成: 总数={}, 成功={}, 失败={}, 耗时={}ms",
                    totalCount, successCount, failCount, duration);

        } catch (Exception e) {
            log.error("检查即将超时任务失败", e);
        }
    }

    /**
     * 每天凌晨2点发送每日任务提醒
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void sendDailyReminders() {
        log.info("开始发送每日任务提醒");

        long startTime = System.currentTimeMillis();
        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            // 1. 发送每日待办汇总提醒
            int dailySummaryCount = sendDailySummaryReminders();
            totalCount += dailySummaryCount;

            // 2. 发送待处理任务提醒（按优先级）
            int pendingCount = sendPendingTaskReminders();
            totalCount += pendingCount;

            // 3. 发送流程待办提醒
            int workflowCount = sendWorkflowReminders();
            totalCount += workflowCount;

            successCount = totalCount;

            long duration = System.currentTimeMillis() - startTime;
            log.info("每日任务提醒发送完成: 总数={}, 成功={}, 失败={}, 耗时={}ms",
                    totalCount, successCount, failCount, duration);

        } catch (Exception e) {
            log.error("发送每日任务提醒失败", e);
        }
    }

    /**
     * 查询即将到期的任务（1小时内到期）
     */
    private List<ProcessTask> findTasksDueSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueSoonThreshold = now.plusHours(dueSoonHours);
        LocalDateTime dueWarningThreshold = now.plusHours(dueWarningHours);

        LambdaQueryWrapper<ProcessTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessTask::getStatus, 0) // 待处理
                .isNotNull(ProcessTask::getDueTime)
                .ge(ProcessTask::getDueTime, now)
                .le(ProcessTask::getDueTime, dueWarningThreshold)
                .orderByAsc(ProcessTask::getDueTime); // 按到期时间升序

        return processTaskRepository.selectList(wrapper);
    }

    /**
     * 查询超时未处理的任务
     */
    private List<ProcessTask> findOverdueTasks() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusDays(timeoutDays);

        LambdaQueryWrapper<ProcessTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessTask::getStatus, 0) // 待处理
                .isNotNull(ProcessTask::getDueTime)
                .lt(ProcessTask::getDueTime, timeoutThreshold)
                .orderByAsc(ProcessTask::getDueTime);

        return processTaskRepository.selectList(wrapper);
    }

    /**
     * 查询所有待处理任务
     */
    private List<ProcessTask> findAllPendingTasks() {
        LambdaQueryWrapper<ProcessTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessTask::getStatus, 0) // 待处理
                .orderByDesc(ProcessTask::getPriority) // 按优先级降序
                .orderByAsc(ProcessTask::getDueTime); // 按到期时间升序

        return processTaskRepository.selectList(wrapper);
    }

    /**
     * 发送每日汇总提醒
     */
    private int sendDailySummaryReminders() {
        log.info("开始发送每日汇总提醒");

        List<ProcessTask> pendingTasks = findAllPendingTasks();
        if (pendingTasks.isEmpty()) {
            log.info("没有待处理任务，跳过每日汇总提醒");
            return 0;
        }

        // 按处理人分组
        Map<String, List<ProcessTask>> tasksByAssignee = pendingTasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getAssigneeId() != null ? task.getAssigneeId() : "unassigned"));

        int sentCount = 0;

        for (Map.Entry<String, List<ProcessTask>> entry : tasksByAssignee.entrySet()) {
            String assigneeId = entry.getKey();
            List<ProcessTask> tasks = entry.getValue();

            try {
                // 构建每日汇总消息
                Map<String, Object> summaryMessage = new HashMap<>();
                summaryMessage.put("messageId", java.util.UUID.randomUUID().toString());
                summaryMessage.put("messageType", "DAILY_SUMMARY");
                summaryMessage.put("userId", assigneeId);
                summaryMessage.put("title", "每日待办汇总");
                summaryMessage.put("summary", String.format("您有%d个待处理任务", tasks.size()));
                summaryMessage.put("taskCount", tasks.size());

                // 统计各优先级的任务数量
                long urgentCount = tasks.stream().filter(t -> t.getPriority() != null && t.getPriority() >= 3).count();
                long normalCount = tasks.size() - urgentCount;

                summaryMessage.put("urgentCount", urgentCount);
                summaryMessage.put("normalCount", normalCount);

                // 按优先级分类的任务摘要
                summaryMessage.put("topTasks", tasks.stream()
                        .limit(5)
                        .map(t -> Map.of(
                                "taskId", t.getId(),
                                "taskName", t.getTaskNodeName() != null ? t.getTaskNodeName() : "未知任务",
                                "priority", t.getPriority() != null ? t.getPriority() : 0,
                                "dueTime", t.getDueTime() != null ? t.getDueTime().toString() : "无期限"
                        ))
                        .collect(Collectors.toList()));

                summaryMessage.put("timestamp", LocalDateTime.now().toString());
                summaryMessage.put("channels", List.of("IN_APP"));

                // 发送Kafka消息
                kafkaTemplate.send(taskReminderTopic, assigneeId, summaryMessage)
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                log.debug("每日汇总提醒发送成功: userId={}", assigneeId);
                            } else {
                                log.error("每日汇总提醒发送失败: userId={}", assigneeId, ex);
                            }
                        });

                sentCount++;

            } catch (Exception e) {
                log.error("发送每日汇总提醒失败: assigneeId={}", assigneeId, e);
            }
        }

        log.info("每日汇总提醒发送完成: count={}", sentCount);
        return sentCount;
    }

    /**
     * 发送待处理任务提醒（按优先级）
     */
    private int sendPendingTaskReminders() {
        log.info("开始发送待处理任务提醒");

        List<ProcessTask> pendingTasks = findAllPendingTasks();
        if (pendingTasks.isEmpty()) {
            return 0;
        }

        int sentCount = 0;
        int batchNumber = 0;

        // 分批处理
        for (int i = 0; i < pendingTasks.size(); i += batchSize) {
            batchNumber++;
            int endIndex = Math.min(i + batchSize, pendingTasks.size());
            List<ProcessTask> batch = pendingTasks.subList(i, endIndex);

            for (ProcessTask task : batch) {
                try {
                    // 只发送一次提醒，避免重复
                    if (shouldSendReminder(task)) {
                        processTaskServiceImpl.sendTaskReminder(task.getId());
                        sentCount++;
                    }
                } catch (Exception e) {
                    log.error("发送待处理任务提醒失败: taskId={}", task.getId(), e);
                }
            }

            log.debug("待处理任务提醒批次{}完成: {}/{}", batchNumber, endIndex, pendingTasks.size());

            // 每批之间短暂休息，避免给系统造成压力
            if (endIndex < pendingTasks.size()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.info("待处理任务提醒发送完成: count={}", sentCount);
        return sentCount;
    }

    /**
     * 发送流程待办提醒
     */
    private int sendWorkflowReminders() {
        log.info("开始发送流程待办提醒");

        List<ProcessTask> pendingTasks = findAllPendingTasks();
        if (pendingTasks.isEmpty()) {
            return 0;
        }

        // 按流程实例分组
        Map<String, List<ProcessTask>> tasksByProcessInstance = pendingTasks.stream()
                .filter(t -> t.getProcessInstanceId() != null)
                .collect(Collectors.groupingBy(ProcessTask::getProcessInstanceId));

        int sentCount = 0;

        for (Map.Entry<String, List<ProcessTask>> entry : tasksByProcessInstance.entrySet()) {
            String processInstanceId = entry.getKey();
            List<ProcessTask> tasks = entry.getValue();

            try {
                // 检查是否需要发送流程汇总
                boolean needsReminder = tasks.stream().anyMatch(this::shouldSendReminder);

                if (needsReminder) {
                    // 构建流程汇总消息
                    Map<String, Object> workflowMessage = new HashMap<>();
                    workflowMessage.put("messageId", java.util.UUID.randomUUID().toString());
                    workflowMessage.put("messageType", "WORKFLOW_SUMMARY");
                    workflowMessage.put("processInstanceId", processInstanceId);
                    workflowMessage.put("processDefinitionId", tasks.get(0).getProcessDefinitionId());
                    workflowMessage.put("taskCount", tasks.size());
                    workflowMessage.put("title", String.format("流程待办汇总 (%d个任务)", tasks.size()));
                    workflowMessage.put("timestamp", LocalDateTime.now().toString());
                    workflowMessage.put("channels", List.of("IN_APP"));

                    // 获取所有涉及的处理人
                    List<String> assigneeIds = tasks.stream()
                            .map(ProcessTask::getAssigneeId)
                            .filter(id -> id != null)
                            .distinct()
                            .collect(Collectors.toList());
                    workflowMessage.put("assigneeIds", assigneeIds);

                    kafkaTemplate.send(taskReminderTopic, processInstanceId, workflowMessage)
                            .whenComplete((result, ex) -> {
                                if (ex == null) {
                                    log.debug("流程待办提醒发送成功: processInstanceId={}", processInstanceId);
                                } else {
                                    log.error("流程待办提醒发送失败: processInstanceId={}", processInstanceId, ex);
                                }
                            });

                    sentCount++;
                }

            } catch (Exception e) {
                log.error("发送流程待办提醒失败: processInstanceId={}", processInstanceId, e);
            }
        }

        log.info("流程待办提醒发送完成: count={}", sentCount);
        return sentCount;
    }

    /**
     * 判断是否应该发送提醒
     */
    private boolean shouldSendReminder(ProcessTask task) {
        LocalDateTime now = LocalDateTime.now();

        // 如果任务已处理，不发送
        if (task.getStatus() != 0) {
            return false;
        }

        // 如果有到期时间，检查是否临近
        if (task.getDueTime() != null) {
            // 已到期或即将到期（1小时内）
            if (task.getDueTime().isBefore(now) || task.getDueTime().isBefore(now.plusHours(1))) {
                return true;
            }
        }

        // 如果创建时间较早但一直未处理
        if (task.getCreatedTime() != null) {
            Duration duration = Duration.between(task.getCreatedTime(), now);
            if (duration.toDays() >= timeoutDays) {
                return true;
            }
        }

        // 检查是否已经发送过提醒，避免重复
        if (task.getReminderCount() != null && task.getReminderCount() > 0) {
            // 如果上次提醒是1小时前，可以再次提醒
            if (task.getLastReminderTime() != null) {
                Duration sinceLastReminder = Duration.between(task.getLastReminderTime(), now);
                return sinceLastReminder.toHours() >= 1; // 至少1小时前
            }
        }

        // 默认返回false，避免过度提醒
        return false;
    }
}
