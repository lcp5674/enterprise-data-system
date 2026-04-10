package com.enterprise.dataplatform.standard.domain.entity;

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
 * 数据标准实体
 * 定义企业级数据标准规范
 */
@Entity
@Table(name = "data_standard", indexes = {
    @Index(name = "idx_standard_code", columnList = "standardCode"),
    @Index(name = "idx_standard_category", columnList = "category"),
    @Index(name = "idx_standard_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataStandard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 标准编码，唯一标识
     */
    @Column(name = "standard_code", nullable = false, unique = true, length = 64)
    private String standardCode;

    /**
     * 标准名称
     */
    @Column(name = "standard_name", nullable = false, length = 128)
    private String standardName;

    /**
     * 标准描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 标准类别：命名规范、数据类型、格式规范、取值范围、业务规则等
     */
    @Column(name = "category", nullable = false, length = 32)
    private String category;

    /**
     * 标准类型：命名标准、格式标准、取值标准、关系标准等
     */
    @Column(name = "standard_type", nullable = false, length = 32)
    private String standardType;

    /**
     * 标准内容/规则表达式（JSON格式）
     * 例如：{"pattern": "^[A-Z]{2}[0-9]{6}$", "description": "部门编码格式"}
     */
    @Column(name = "rule_content", columnDefinition = "JSONB")
    private String ruleContent;

    /**
     * 数据类型：STRING、NUMBER、DATE、BOOLEAN、ENUM等
     */
    @Column(name = "data_type", length = 32)
    private String dataType;

    /**
     * 取值范围（JSON格式）
     */
    @Column(name = "value_range", columnDefinition = "JSONB")
    private String valueRange;

    /**
     * 精度要求（适用于数值类型）
     */
    @Column(name = "precision_required")
    private Integer precisionRequired;

    /**
     * 最大长度（适用于字符串类型）
     */
    @Column(name = "max_length")
    private Integer maxLength;

    /**
     * 是否必填
     */
    @Column(name = "required", nullable = false)
    private Boolean required = false;

    /**
     * 默认值
     */
    @Column(name = "default_value", length = 256)
    private String defaultValue;

    /**
     * 标准状态：DRAFT、ACTIVE、DEPRECATED、ARCHIVED
     */
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    /**
     * 优先级：HIGH、MEDIUM、LOW
     */
    @Column(name = "priority", length = 16)
    private String priority;

    /**
     * 版本号
     */
    @Column(name = "version", nullable = false)
    private Integer version;

    /**
     * 标准来源：INTERNAL、EXTERNAL、REGULATORY
     */
    @Column(name = "source", length = 32)
    private String source;

    /**
     * 关联的外部标准编号
     */
    @Column(name = "external_ref", length = 128)
    private String externalRef;

    /**
     * 适用范围（JSON格式，存储适用场景）
     */
    @Column(name = "applicable_scope", columnDefinition = "JSONB")
    private String applicableScope;

    /**
     * 违规处理建议
     */
    @Column(name = "violation_handling", columnDefinition = "TEXT")
    private String violationHandling;

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
     * 版本历史列表
     */
    @OneToMany(mappedBy = "dataStandard", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StandardVersion> versions = new ArrayList<>();

    /**
     * 标准映射列表
     */
    @OneToMany(mappedBy = "dataStandard", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StandardMapping> mappings = new ArrayList<>();

    /**
     * 合规检查记录列表
     */
    @OneToMany(mappedBy = "dataStandard", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ComplianceCheck> complianceChecks = new ArrayList<>();
}
