package com.enterprise.dataplatform.ethics.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EthicsReviewRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "资产ID不能为空")
    private String assetId;

    private String assetName;

    private String riskLevel;

    @NotBlank(message = "审查类型不能为空")
    private String reviewType;

    private String priority;

    private String requesterDepartment;

    private String justification;

    private String alternativeAnalysis;

    private String stakeholderImpact;

    private String mitigationPlan;

    private String supportingDocs;

    private String conditions;
}
