package com.enterprise.dataplatform.ruleengine.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 规则执行日志实体
 */
@Data
@Entity
@Table(name = "rule_execution_log")
public class RuleExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 规则编码 */
    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    /** 规则名称 */
    @Column(name = "rule_name", length = 200)
    private String ruleName;

    /** 规则分类 */
    @Column(name = "category", length = 50)
    private String category;

    /** 目标资产ID */
    @Column(name = "asset_id", length = 100)
    private String assetId;

    /** 输入数据（JSON） */
    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;

    /** 输出结果（JSON） */
    @Column(name = "output_result", columnDefinition = "TEXT")
    private String outputResult;

    /** 触发的规则列表 */
    @Column(name = "triggered_rules", columnDefinition = "TEXT")
    private String triggeredRules;

    /** 执行耗时（毫秒） */
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    /** 执行状态: SUCCESS, FAILED, TIMEOUT */
    @Column(name = "status", length = 20)
    private String status;

    /** 错误信息 */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** 执行者 */
    @Column(name = "executed_by", length = 100)
    private String executedBy;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;
}
