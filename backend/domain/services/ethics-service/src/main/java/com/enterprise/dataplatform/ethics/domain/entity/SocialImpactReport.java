package com.enterprise.dataplatform.ethics.domain.entity;

import com.enterprise.dataplatform.ethics.domain.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "social_impact_report", indexes = {
    @Index(name = "idx_social_asset", columnList = "assetId"),
    @Index(name = "idx_social_report_time", columnList = "reportDate"),
    @Index(name = "idx_social_risk", columnList = "overallRisk")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialImpactReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_code", nullable = false, unique = true, length = 64)
    private String reportCode;

    @Column(name = "asset_id", nullable = false, length = 64)
    private String assetId;

    @Column(name = "asset_name", length = 256)
    private String assetName;

    @Column(name = "report_date", nullable = false)
    private LocalDateTime reportDate;

    @Column(name = "report_period", length = 64)
    private String reportPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_risk", length = 32)
    private RiskLevel overallRisk;

    @Column(name = "overall_impact_score")
    private BigDecimal overallImpactScore;

    @Column(name = "data_usage_impact_score")
    private BigDecimal dataUsageImpactScore;

    @Column(name = "stakeholder_score")
    private BigDecimal stakeholderScore;

    @Column(name = "social_value_score")
    private BigDecimal socialValueScore;

    @Column(name = "community_benefit_score")
    private BigDecimal communityBenefitScore;

    @Column(name = "data_usage_analysis", columnDefinition = "TEXT")
    private String dataUsageAnalysis;

    @Column(name = "stakeholder_analysis", columnDefinition = "TEXT")
    private String stakeholderAnalysis;

    @Column(name = "impact_assessment", columnDefinition = "TEXT")
    private String impactAssessment;

    @Column(name = "positive_impacts", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    @Builder.Default
    private List<String> positiveImpacts = new ArrayList<>();

    @Column(name = "negative_impacts", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    @Builder.Default
    private List<String> negativeImpacts = new ArrayList<>();

    @Column(name = "affected_stakeholders", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    @Builder.Default
    private List<String> affectedStakeholders = new ArrayList<>();

    @Column(name = "recommendations", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    @Builder.Default
    private List<String> recommendations = new ArrayList<>();

    @Column(name = "data_governance_score")
    private BigDecimal dataGovernanceScore;

    @Column(name = "transparency_score")
    private BigDecimal transparencyScore;

    @Column(name = "accountability_score")
    private BigDecimal accountabilityScore;

    @Column(name = "compliance_score")
    private BigDecimal complianceScore;

    @Column(name = "analyst", length = 64)
    private String analyst;

    @Column(name = "status", length = 32)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "approval_status", length = 32)
    @Builder.Default
    private String approvalStatus = "PENDING";

    @Column(name = "approved_by", length = 64)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
