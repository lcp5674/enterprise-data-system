package com.enterprise.dataplatform.governance.domain.entity;

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
 * 治理策略实体
 * 定义数据治理的策略规则
 */
@Entity
@Table(name = "governance_policy", indexes = {
    @Index(name = "idx_policy_code", columnList = "policyCode"),
    @Index(name = "idx_policy_type", columnList = "policyType"),
    @Index(name = "idx_policy_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 策略编码
     */
    @Column(name = "policy_code", nullable = false, unique = true, length = 64)
    private String policyCode;

    /**
     * 策略名称
     */
    @Column(name = "policy_name", nullable = false, length = 128)
    private String policyName;

    /**
     * 策略描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 策略类型：DATA_CLASSIFICATION、ACCESS_CONTROL、RETENTION、AUDIT、COMPLIANCE
     */
    @Column(name = "policy_type", nullable = false, length = 32)
    private String policyType;

    /**
     * 策略分类
     */
    @Column(name = "policy_category", length = 64)
    private String policyCategory;

    /**
     * 策略内容（JSON格式）
     */
    @Column(name = "policy_content", columnDefinition = "JSONB")
    private String policyContent;

    /**
     * 适用范围（JSON格式）
     */
    @Column(name = "applicable_scope", columnDefinition = "JSONB")
    private String applicableScope;

    /**
     * 优先级
     */
    @Column(name = "priority", length = 16)
    private String priority;

    /**
     * 严重级别
     */
    @Column(name = "severity_level", length = 16)
    private String severityLevel;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 策略状态：DRAFT、ACTIVE、DEPRECATED、ARCHIVED
     */
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    /**
     * 版本号
     */
    @Column(name = "version", nullable = false)
    private Integer version;

    /**
     * 关联的治理任务
     */
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL)
    @Builder.Default
    private List<GovernanceTask> tasks = new ArrayList<>();

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
}
