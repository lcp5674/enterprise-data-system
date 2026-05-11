package com.enterprise.dataplatform.ethics.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class EthicsAssessmentRequest {

    @NotBlank(message = "资产ID不能为空")
    private String assetId;

    private String assetName;

    private Long frameworkId;

    private String frameworkName;

    private BigDecimal transparencyValue;

    private BigDecimal fairnessValue;

    private BigDecimal accountabilityValue;

    private BigDecimal privacyValue;

    private String methodology;

    private String notes;

    private List<String> concerns;
}
