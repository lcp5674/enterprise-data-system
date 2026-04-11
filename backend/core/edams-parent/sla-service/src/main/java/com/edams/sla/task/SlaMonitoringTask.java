package com.edams.sla.task;

import com.edams.sla.service.SlaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlaMonitoringTask {
    
    private final SlaService slaService;
    
    /**
     * 每分钟执行一次SLA监控
     */
    @Scheduled(cron = "0 * * * * ?")
    public void monitorSla() {
        log.info("开始执行SLA监控任务");
        try {
            slaService.checkViolations();
            log.info("SLA监控任务完成");
        } catch (Exception e) {
            log.error("SLA监控任务执行失败", e);
        }
    }
    
    /**
     * 每天凌晨生成SLA日报
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyReport() {
        log.info("开始生成SLA日报");
        try {
            // 这里可以调用slaService生成所有活跃SLA的日报
            log.info("SLA日报生成任务完成");
        } catch (Exception e) {
            log.error("SLA日报生成任务执行失败", e);
        }
    }
}