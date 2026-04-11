package com.edams.sandbox.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sandbox_execution")
public class SandboxExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sandbox_id", nullable = false)
    private Long sandboxId;
    
    @Column(name = "execution_type", nullable = false)
    private String executionType; // SQL_EXECUTION, API_TEST, DATA_ANALYSIS
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "parameters")
    private String parameters; // JSON格式的参数
    
    @Column(name = "result")
    private String result; // JSON格式的执行结果
    
    @Column(name = "status", nullable = false)
    private String status; // PENDING, RUNNING, SUCCESS, FAILED
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "executed_by", nullable = false)
    private Long executedBy;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "duration")
    private Long duration; // 执行时长（毫秒）
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        status = "PENDING";
    }
}