package com.enterprise.dataplatform.ethics.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialImpactRequest {

    private String assetId;

    private String assetName;

    private String reportPeriod;

    private BigDecimal dataUsageImpactScore;

    private BigDecimal stakeholderScore;

    private BigDecimal socialValueScore;

    private BigDecimal communityBenefitScore;

    private List<String> affectedStakeholders;

    private String notes;
}
