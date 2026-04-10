package com.enterprise.dataplatform.standard.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 标准执行记录实体
 * 记录标准映射的执行和应用情况
 */
@Entity
@Table(name = "standard_execution", indexes = {
    @Index(name = "idx_execution_mapping", columnList = "mapping_id"),
    @Index(name = "idx_execution_status", columnList = "execution_status"),
    @Index(name = "idx_execution_time", columnList = "execution_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 执行批次号
     */
    @Column(name = "batch_no", nullable = false, length = 64)
    private String batchNo;

    /**
     * 标准映射ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mapping_id", nullable = false)
    private StandardMapping mapping;

    /**
     * 数据标准ID
     */
    @Column(name = "standard_id")
    private Long standardId;

    /**
     * 数据标准编码
     */
    @Column(name = "standard_code", length = 64)
    private String standardCode;

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
     * 字段名称
     */
    @Column(name = "field_name", length = 128)
    private String fieldName;

    /**
     * 执行类型：INITIAL、MAPPING_APPLY、TRANSFORM
     */
    @Column(name = "execution_type", nullable = false, length = 32)
    private String executionType;

    /**
     * 执行状态：PENDING、RUNNING、COMPLETED、FAILED
     */
    @Column(name = "execution_status", nullable = false, length = 32)
    private String executionStatus;

    /**
     * 执行结果：SUCCESS、PARTIAL、FAILED
     */
    @Column(name = "result_status", length = 16)
    private String resultStatus;

    /**
     * 处理记录数
     */
    @Column(name = "processed_records")
    private Long processedRecords;

    /**
     * 成功记录数
     */
    @Column(name = "success_records")
    private Long successRecords;

    /**
     * 失败记录数
     */
    @Column(name = "failed_records")
    private Long failedRecords;

    /**
     * 转换后数据样本（JSON格式）
     */
    @Column(name = "transform_samples", columnDefinition = "TEXT")
    private String transformSamples;

    /**
     * 错误详情（JSON格式）
     */
    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    /**
     * 执行开始时间
     */
    @Column(name = "execution_start_time")
    private LocalDateTime executionStartTime;

    /**
     * 执行结束时间
     */
    @Column(name = "execution_end_time")
    private LocalDateTime executionEndTime;

    /**
     * 执行耗时（毫秒）
     */
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    /**
     * 执行人
     */
    @Column(name = "executor", length = 64)
    private String executor;

    /**
     * 执行方式：MANUAL、SCHEDULED、EVENT_TRIGGER
     */
    @Column(name = "execution_mode", length = 32)
    private String executionMode;

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
