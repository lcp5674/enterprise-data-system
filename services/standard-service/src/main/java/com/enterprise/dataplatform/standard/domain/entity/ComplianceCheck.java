package com.enterprise.dataplatform.standard.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 合规检查实体
 * 记录数据标准合规性检查的执行结果
 */
@Entity
@Table(name = "compliance_check", indexes = {
    @Index(name = "idx_check_standard", columnList = "standard_id"),
    @Index(name = "idx_check_asset", columnList = "asset_id"),
    @Index(name = "idx_check_result", columnList = "check_result"),
    @Index(name = "idx_check_time", columnList = "check_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 检查批次号
     */
    @Column(name = "batch_no", nullable = false, length = 64)
    private String batchNo;

    /**
     * 数据标准ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id", nullable = false)
    private DataStandard dataStandard;

    /**
     * 数据标准编码
     */
    @Column(name = "standard_code", nullable = false, length = 64)
    private String standardCode;

    /**
     * 数据标准名称
     */
    @Column(name = "standard_name", length = 128)
    private String standardName;

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
     * 资产类型
     */
    @Column(name = "asset_type", length = 32)
    private String assetType;

    /**
     * 字段名称
     */
    @Column(name = "field_name", length = 128)
    private String fieldName;

    /**
     * 检查类型：MAPPING_CHECK、VALUE_CHECK、FORMAT_CHECK、FULL_CHECK
     */
    @Column(name = "check_type", nullable = false, length = 32)
    private String checkType;

    /**
     * 检查结果：PASS、FAIL、WARN、ERROR
     */
    @Column(name = "check_result", nullable = false, length = 16)
    private String checkResult;

    /**
     * 合规率（0-100）
     */
    @Column(name = "compliance_rate")
    private Double complianceRate;

    /**
     * 违规数量
     */
    @Column(name = "violation_count")
    private Integer violationCount;

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
     * 检查方法
     */
    @Column(name = "check_method", length = 64)
    private String checkMethod;

    /**
     * 检查执行时间
     */
    @Column(name = "check_time", nullable = false)
    private LocalDateTime checkTime;

    /**
     * 检查耗时（毫秒）
     */
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    /**
     * 检查状态：RUNNING、COMPLETED、FAILED
     */
    @Column(name = "check_status", nullable = false, length = 32)
    private String checkStatus;

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
     * 备注
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
