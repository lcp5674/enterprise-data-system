package com.enterprise.edams.workflow.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.workflow.entity.ProcessTask;
import com.enterprise.edams.workflow.service.ProcessTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务提醒定时任务
 *
 * @author EDAMS Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskReminderJob {

    private final ProcessTaskService processTaskService;

    /**
     * 每小时检查一次即将超时的任务
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkOverdueTasks() {
        log.info("开始检查即将超时的任务");

        LambdaQueryWrapper<ProcessTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessTask::getStatus, 0) // 待处理
                .isNotNull(ProcessTask::getDueTime)
                .le(ProcessTask::getDueTime, LocalDateTime.now().plusHours(24));

        // TODO: 实现任务查询并发送提醒
        log.info("即将超时的任务检查完成");
    }

    /**
     * 每天凌晨2点发送任务提醒
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void sendDailyReminders() {
        log.info("开始发送每日任务提醒");

        // TODO: 实现批量发送提醒
        log.info("每日任务提醒发送完成");
    }
}
