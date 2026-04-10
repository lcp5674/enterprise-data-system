package com.enterprise.dataplatform.governance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class TaskSchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("governance-scheduler-");
        scheduler.setErrorHandler(throwable -> {
            // 记录调度错误
            System.err.println("任务调度错误: " + throwable.getMessage());
        });
        scheduler.setRejectedExecutionHandler((runnable, executor) -> {
            // 处理任务被拒绝的情况
            System.err.println("任务被拒绝执行");
        });
        return scheduler;
    }
}
