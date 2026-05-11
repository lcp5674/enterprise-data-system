package com.enterprise.dataplatform.ethics.domain.dto.response;

import com.enterprise.dataplatform.ethics.domain.entity.SocialImpactReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialImpactResponse {

    private Long id;
    private String reportCode;
    private String assetId;
    private String assetName;
    private LocalDateTime reportDate;
    private String reportPeriod;
    private String overallRisk;
    private BigDecimal overallImpactScore;
    private BigDecimal dataUsageImpactScore;
    private BigDecimal stakeholderScore;
    private BigDecimal socialValueScore;
    private BigDecimal communityBenefitScore;
    private String dataUsageAnalysis;
    private String stakeholderAnalysis;
    private String impactAssessment;
    private List<String> positiveImpacts;
    private List<String> negativeImpacts;
    private List<String> affectedStakeholders;
    private List<String> recommendations;
    private BigDecimal dataGovernanceScore;
    private BigDecimal transparencyScore;
    private BigDecimal accountabilityScore;
    private BigDecimal complianceScore;
    private String analyst;
    private String status;
    private String approvalStatus;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createTime;
    private String notes;

    public static SocialImpactResponse fromEntity(SocialImpactReport entity) {
        return SocialImpactResponse.builder()
                .id(entity.getId())
                .reportCode(entity.getReportCode())
                .assetId(entity.getAssetId())
                .assetName(entity.getAssetName())
                .reportDate(entity.getReportDate())
                .reportPeriod(entity.getReportPeriod())
                .overallRisk(entity.getOverallRisk() != null ? entity.getOverallRisk().name() : null)
                .overallImpactScore(entity.getOverallImpactScore())
                .dataUsageImpactScore(entity.getDataUsageImpactScore())
                .stakeholderScore(entity.getStakeholderScore())
                .socialValueScore(entity.getSocialValueScore())
                .communityBenefitScore(entity.getCommunityBenefitScore())
                .dataUsageAnalysis(entity.getDataUsageAnalysis())
                .stakeholderAnalysis(entity.getStakeholderAnalysis())
                .impactAssessment(entity.getImpactAssessment())
                .positiveImpacts(entity.getPositiveImpacts())
                .negativeImpacts(entity.getNegativeImpacts())
                .affectedStakeholders(entity.getAffectedStakeholders())
                .recommendations(entity.getRecommendations())
                .dataGovernanceScore(entity.getDataGovernanceScore())
                .transparencyScore(entity.getTransparencyScore())
                .accountabilityScore(entity.getAccountabilityScore())
                .complianceScore(entity.getComplianceScore())
                .analyst(entity.getAnalyst())
                .status(entity.getStatus())
                .approvalStatus(entity.getApprovalStatus())
                .approvedBy(entity.getApprovedBy())
                .approvedAt(entity.getApprovedAt())
                .createTime(entity.getCreateTime())
                .notes(entity.getNotes())
                .build();
    }
}
