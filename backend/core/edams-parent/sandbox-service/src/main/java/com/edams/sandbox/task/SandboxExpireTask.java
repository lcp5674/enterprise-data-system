package com.edams.sandbox.task;

import com.edams.sandbox.service.SandboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SandboxExpireTask {
    
    private final SandboxService sandboxService;
    
    /**
     * 每小时检查一次过期沙箱
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkExpiredSandboxes() {
        log.info("开始执行沙箱过期检查任务");
        try {
            sandboxService.expireSandboxes();
            log.info("沙箱过期检查任务完成");
        } catch (Exception e) {
            log.error("沙箱过期检查任务执行失败", e);
        }
    }
}