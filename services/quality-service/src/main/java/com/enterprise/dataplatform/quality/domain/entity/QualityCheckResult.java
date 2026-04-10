package com.enterprise.dataplatform.quality.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 质量检查结果实体
 * 记录质量检查的执行结果
 */
@Entity
@Table(name = "quality_check_result", indexes = {
    @Index(name = "idx_result_task", columnList = "task_id"),
    @Index(name = "idx_result_rule", columnList = "rule_id"),
    @Index(name = "idx_result_status", columnList = "checkStatus"),
    @Index(name = "idx_result_time", columnList = "checkTime")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityCheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 检查批次号
     */
    @Column(name = "batch_no", nullable = false, length = 64)
    private String batchNo;

    /**
     * 关联的任务ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private QualityCheckTask task;

    /**
     * 关联的规则ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private QualityRule rule;

    /**
     * 规则编码
     */
    @Column(name = "rule_code", nullable = false, length = 64)
    private String ruleCode;

    /**
     * 规则名称
     */
    @Column(name = "rule_name", length = 128)
    private String ruleName;

    /**
     * 规则类型
     */
    @Column(name = "rule_type", length = 32)
    private String ruleType;

    /**
     * 数据资产ID
     */
    @Column(name = "asset_id", nullable = false, length = 64)
    private String assetId;

    /**
     * 数据资产名称
     */
    @Column(name = "asset_name", length = 256)
    private String assetName;

    /**
     * 检查状态：RUNNING、COMPLETED、FAILED
     */
    @Column(name = "check_status", nullable = false, length = 32)
    private String checkStatus;

    /**
     * 检查结果：PASS、WARN、FAIL、ERROR
     */
    @Column(name = "check_result", nullable = false, length = 16)
    private String checkResult;

    /**
     * 质量得分（0-100）
     */
    @Column(name = "quality_score")
    private Double qualityScore;

    /**
     * 总记录数
     */
    @Column(name = "total_records")
    private Long totalRecords;

    /**
     * 检查的记录数
     */
    @Column(name = "checked_records")
    private Long checkedRecords;

    /**
     * 通过的记录数
     */
    @Column(name = "passed_records")
    private Long passedRecords;

    /**
     * 失败的记录数
     */
    @Column(name = "failed_records")
    private Long failedRecords;

    /**
     * 违规率
     */
    @Column(name = "violation_rate")
    private Double violationRate;

    /**
     * 阈值
     */
    @Column(name = "threshold")
    private Double threshold;

    /**
     * 是否超过阈值
     */
    @Column(name = "exceeds_threshold")
    private Boolean exceedsThreshold;

    /**
     * 违规记录样本（JSON格式）
     */
    @Column(name = "violation_samples", columnDefinition = "TEXT")
    private String violationSamples;

    /**
     * 违规详情（JSON格式）
     */
    @Column(name = "violation_details", columnDefinition = "TEXT")
    private String violationDetails;

    /**
     * 检查开始时间
     */
    @Column(name = "check_start_time")
    private LocalDateTime checkStartTime;

    /**
     * 检查结束时间
     */
    @Column(name = "check_end_time")
    private LocalDateTime checkEndTime;

    /**
     * 检查耗时（毫秒）
     */
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 执行人
     */
    @Column(name = "executor", length = 64)
    private String executor;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 检查时间（执行时间点）
     */
    @Column(name = "check_time", nullable = false)
    private LocalDateTime checkTime;

    /**
     * 告警是否已发送
     */
    @Column(name = "alert_sent")
    private Boolean alertSent;

    /**
     * 备注
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
