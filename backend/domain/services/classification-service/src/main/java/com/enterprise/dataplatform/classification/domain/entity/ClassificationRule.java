package com.enterprise.dataplatform.classification.domain.entity;

import com.enterprise.dataplatform.classification.domain.enums.ClassificationRuleType;
import com.enterprise.dataplatform.classification.domain.enums.SensitivityLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "classification_rule", indexes = {
    @Index(name = "idx_rule_name", columnList = "ruleName"),
    @Index(name = "idx_rule_type", columnList = "ruleType"),
    @Index(name = "idx_sensitivity_level", columnList = "sensitivityLevel")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false, length = 128)
    private String ruleName;

    @Column(name = "rule_description", columnDefinition = "TEXT")
    private String ruleDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 32)
    private ClassificationRuleType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensitivity_level", nullable = false, length = 32)
    private SensitivityLevel sensitivityLevel;

    @Column(name = "pattern", length = 512)
    private String pattern;

    @Column(name = "data_type", length = 64)
    private String dataType;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "column_pattern", length = 256)
    private String columnPattern;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "confidence_threshold")
    private Double confidenceThreshold;

    @Column(name = "auto_classify", nullable = false)
    private Boolean autoClassify;

    @Column(name = "trigger_approval", nullable = false)
    private Boolean triggerApproval;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
