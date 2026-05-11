package com.enterprise.dataplatform.ethics.domain.dto.response;

import com.enterprise.dataplatform.ethics.domain.entity.EthicsReview;
import com.enterprise.dataplatform.ethics.domain.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EthicsReviewResponse {

    private Long id;
    private String reviewCode;
    private String title;
    private String description;
    private String assetId;
    private String assetName;
    private String riskLevel;
    private String reviewType;
    private String status;
    private String priority;
    private String requester;
    private String requesterDepartment;
    private String reviewer;
    private String reviewerDepartment;
    private String approver;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime executedAt;
    private LocalDateTime expiryDate;
    private List<String> reviewComments;
    private String approvalComments;
    private String rejectionReason;
    private List<String> conditions;
    private String supportingDocs;
    private String justification;
    private String alternativeAnalysis;
    private String stakeholderImpact;
    private String mitigationPlan;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer version;

    public static EthicsReviewResponse fromEntity(EthicsReview entity) {
        return EthicsReviewResponse.builder()
                .id(entity.getId())
                .reviewCode(entity.getReviewCode())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .assetId(entity.getAssetId())
                .assetName(entity.getAssetName())
                .riskLevel(entity.getRiskLevel() != null ? entity.getRiskLevel().name() : null)
                .reviewType(entity.getReviewType())
                .status(entity.getStatus().name())
                .priority(entity.getPriority())
                .requester(entity.getRequester())
                .requesterDepartment(entity.getRequesterDepartment())
                .reviewer(entity.getReviewer())
                .reviewerDepartment(entity.getReviewerDepartment())
                .approver(entity.getApprover())
                .submittedAt(entity.getSubmittedAt())
                .reviewedAt(entity.getReviewedAt())
                .approvedAt(entity.getApprovedAt())
                .executedAt(entity.getExecutedAt())
                .expiryDate(entity.getExpiryDate())
                .reviewComments(entity.getReviewComments())
                .approvalComments(entity.getApprovalComments())
                .rejectionReason(entity.getRejectionReason())
                .conditions(entity.getConditions())
                .supportingDocs(entity.getSupportingDocs())
                .justification(entity.getJustification())
                .alternativeAnalysis(entity.getAlternativeAnalysis())
                .stakeholderImpact(entity.getStakeholderImpact())
                .mitigationPlan(entity.getMitigationPlan())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .version(entity.getVersion())
                .build();
    }
}
