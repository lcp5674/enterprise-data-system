package com.enterprise.dataplatform.classification.domain.entity;

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
@Table(name = "asset_classification", indexes = {
    @Index(name = "idx_asset_id", columnList = "assetId"),
    @Index(name = "idx_classification_status", columnList = "classificationStatus")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false, length = 64)
    private String assetId;

    @Column(name = "asset_name", length = 256)
    private String assetName;

    @Column(name = "asset_type", length = 64)
    private String assetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensitivity_level", nullable = false, length = 32)
    private SensitivityLevel sensitivityLevel;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "classification_status", nullable = false, length = 32)
    private String classificationStatus;

    @Column(name = "classification_method", length = 32)
    private String classificationMethod;

    @Column(name = "matched_rule_id")
    private Long matchedRuleId;

    @Column(name = "matched_rule_name", length = 128)
    private String matchedRuleName;

    @Column(name = "manual_override", nullable = false)
    private Boolean manualOverride;

    @Column(name = "approval_status", length = 32)
    private String approvalStatus;

    @Column(name = "approved_by", length = 64)
    private String approvedBy;

    @Column(name = "approved_time")
    private LocalDateTime approvedTime;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "previous_level", length = 32)
    private String previousLevel;

    @Column(name = "data_categories", columnDefinition = "TEXT")
    private String dataCategories;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Column(name = "reviewed_by", length = 64)
    private String reviewedBy;

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
