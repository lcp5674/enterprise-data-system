package com.enterprise.dataplatform.ethics.domain.entity;

import com.enterprise.dataplatform.ethics.domain.enums.EthicsLevel;
import com.enterprise.dataplatform.ethics.domain.enums.ReviewStatus;
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

@Entity
@Table(name = "ethics_review", indexes = {
    @Index(name = "idx_review_asset", columnList = "assetId"),
    @Index(name = "idx_review_status", columnList = "status"),
    @Index(name = "idx_review_type", columnList = "reviewType"),
    @Index(name = "idx_review_priority", columnList = "priority")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EthicsReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_code", nullable = false, unique = true, length = 64)
    private String reviewCode;

    @Column(name = "title", nullable = false, length = 256)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "asset_id", nullable = false, length = 64)
    private String assetId;

    @Column(name = "asset_name", length = 256)
    private String assetName;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 32)
    private EthicsLevel riskLevel;

    @Column(name = "review_type", nullable = false, length = 32)
    private String reviewType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.DRAFT;

    @Column(name = "priority", length = 16)
    @Builder.Default
    private String priority = "MEDIUM";

    @Column(name = "requester", length = 64)
    private String requester;

    @Column(name = "requester_department", length = 128)
    private String requesterDepartment;

    @Column(name = "reviewer", length = 64)
    private String reviewer;

    @Column(name = "reviewer_department", length = 128)
    private String reviewerDepartment;

    @Column(name = "approver", length = 64)
    private String approver;

    @CreationTimestamp
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "review_comments", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    @Builder.Default
    private List<String> reviewComments = new ArrayList<>();

    @Column(name = "approval_comments", columnDefinition = "TEXT")
    private String approvalComments;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "conditions", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    @Builder.Default
    private List<String> conditions = new ArrayList<>();

    @Column(name = "supporting_docs", length = 1024)
    private String supportingDocs;

    @Column(name = "justification", columnDefinition = "TEXT")
    private String justification;

    @Column(name = "alternative_analysis", columnDefinition = "TEXT")
    private String alternativeAnalysis;

    @Column(name = "stakeholder_impact", columnDefinition = "TEXT")
    private String stakeholderImpact;

    @Column(name = "mitigation_plan", columnDefinition = "TEXT")
    private String mitigationPlan;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;
}
