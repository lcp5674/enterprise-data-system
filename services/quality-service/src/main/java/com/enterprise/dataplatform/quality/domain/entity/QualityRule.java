package com.enterprise.dataplatform.quality.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 质量规则实体
 * 定义数据质量检查的规则
 */
@Entity
@Table(name = "quality_rule", indexes = {
    @Index(name = "idx_rule_code", columnList = "ruleCode"),
    @Index(name = "idx_rule_type", columnList = "ruleType"),
    @Index(name = "idx_rule_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 规则编码
     */
    @Column(name = "rule_code", nullable = false, unique = true, length = 64)
    private String ruleCode;

    /**
     * 规则名称
     */
    @Column(name = "rule_name", nullable = false, length = 128)
    private String ruleName;

    /**
     * 规则描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 规则类型：COMPLETENESS、ACCURACY、CONSISTENCY、TIMELINESS、UNIQUENESS、VALIDITY
     */
    @Column(name = "rule_type", nullable = false, length = 32)
    private String ruleType;

    /**
     * 规则分类：NULL_CHECK、FORMAT_CHECK、RANGE_CHECK、REFERENCE_CHECK、CUSTOM
     */
    @Column(name = "rule_category", nullable = false, length = 32)
    private String ruleCategory;

    /**
     * 规则表达式（JSON格式）
     * 例如：{"type": "not_null", "column": "user_id"}
     */
    @Column(name = "rule_expression", columnDefinition = "JSONB")
    private String ruleExpression;

    /**
     * 关联的数据资产ID
     */
    @Column(name = "asset_id", length = 64)
    private String assetId;

    /**
     * 关联的资产类型
     */
    @Column(name = "asset_type", length = 32)
    private String assetType;

    /**
     * 关联的字段名称
     */
    @Column(name = "field_name", length = 128)
    private String fieldName;

    /**
     * 质量维度
     */
    @Column(name = "quality_dimension", length = 32)
    private String qualityDimension;

    /**
     * 严重级别：BLOCKER、CRITICAL、MAJOR、MINOR、WARN
     */
    @Column(name = "severity_level", nullable = false, length = 16)
    private String severityLevel;

    /**
     * 阈值表达式
     */
    @Column(name = "threshold_expression", columnDefinition = "TEXT")
    private String thresholdExpression;

    /**
     * 期望值
     */
    @Column(name = "expected_value", length = 256)
    private String expectedValue;

    /**
     * 告警阈值
     */
    @Column(name = "alert_threshold")
    private Double alertThreshold;

    /**
     * 错误阈值
     */
    @Column(name = "error_threshold")
    private Double errorThreshold;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 规则状态：DRAFT、ACTIVE、DEPRECATED、ARCHIVED
     */
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    /**
     * 优先级
     */
    @Column(name = "priority", length = 16)
    private String priority;

    /**
     * 版本号
     */
    @Column(name = "version", nullable = false)
    private Integer version;

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
     * 检查任务列表
     */
    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL)
    @Builder.Default
    private List<QualityCheckTask> checkTasks = new ArrayList<>();

    /**
     * 检查结果列表
     */
    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL)
    @Builder.Default
    private List<QualityCheckResult> checkResults = new ArrayList<>();
}
