package com.enterprise.dataplatform.standard.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 标准映射实体
 * 建立数据标准与具体数据资产字段之间的映射关系
 */
@Entity
@Table(name = "standard_mapping", indexes = {
    @Index(name = "idx_mapping_standard", columnList = "standard_id"),
    @Index(name = "idx_mapping_asset", columnList = "asset_id"),
    @Index(name = "idx_mapping_field", columnList = "field_name"),
    @Index(name = "idx_mapping_status", columnList = "mapping_status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 数据标准ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id", nullable = false)
    private DataStandard dataStandard;

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
     * 资产类型：TABLE、VIEW、COLUMN、API、FILE等
     */
    @Column(name = "asset_type", nullable = false, length = 32)
    private String assetType;

    /**
     * 字段名称（针对列级映射）
     */
    @Column(name = "field_name", length = 128)
    private String fieldName;

    /**
     * 字段中文名
     */
    @Column(name = "field_chinese_name", length = 128)
    private String fieldChineseName;

    /**
     * 数据类型
     */
    @Column(name = "field_data_type", length = 64)
    private String fieldDataType;

    /**
     * 映射状态：PENDING、APPROVED、REJECTED、REVIEWING
     */
    @Column(name = "mapping_status", nullable = false, length = 32)
    private String mappingStatus;

    /**
     * 映射类型：DIRECT、DERIVED、TRANSFORMED
     */
    @Column(name = "mapping_type", length = 32)
    private String mappingType;

    /**
     * 转换规则（当mapping_type为DERIVED或TRANSFORMED时）
     */
    @Column(name = "transform_rule", columnDefinition = "TEXT")
    private String transformRule;

    /**
     * 映射覆盖率（0-100）
     */
    @Column(name = "coverage_rate")
    private Integer coverageRate;

    /**
     * 数据质量得分
     */
    @Column(name = "quality_score")
    private Double qualityScore;

    /**
     * 映射说明
     */
    @Column(name = "mapping_description", columnDefinition = "TEXT")
    private String mappingDescription;

    /**
     * 映射来源：MANUAL、AUTO_DISCOVER、IMPORT
     */
    @Column(name = "mapping_source", length = 32)
    private String mappingSource;

    /**
     * 是否关键字段
     */
    @Column(name = "is_key_field")
    private Boolean isKeyField;

    /**
     * 敏感级别
     */
    @Column(name = "sensitivity_level", length = 16)
    private String sensitivityLevel;

    /**
     * 映射版本
     */
    @Column(name = "version", nullable = false)
    private Integer version;

    /**
     * 映射有效期开始
     */
    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    /**
     * 映射有效期结束
     */
    @Column(name = "valid_to")
    private LocalDateTime validTo;

    /**
     * 创建人
     */
    @Column(name = "creator", length = 64)
    private String creator;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @Column(name = "updater", length = 64)
    private String updater;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 审批人
     */
    @Column(name = "approver", length = 64)
    private String approver;

    /**
     * 审批时间
     */
    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    /**
     * 审批意见
     */
    @Column(name = "approval_comment", columnDefinition = "TEXT")
    private String approvalComment;
}
