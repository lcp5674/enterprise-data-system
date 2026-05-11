package com.enterprise.dataplatform.ethics.domain.dto.response;

import com.enterprise.dataplatform.ethics.domain.entity.EthicsAssessment;
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
public class EthicsAssessmentResponse {

    private Long id;
    private String assessmentCode;
    private String assetId;
    private String assetName;
    private Long frameworkId;
    private String frameworkName;
    private String transparencyScore;
    private BigDecimal transparencyValue;
    private String fairnessScore;
    private BigDecimal fairnessValue;
    private String accountabilityScore;
    private BigDecimal accountabilityValue;
    private String privacyScore;
    private BigDecimal privacyValue;
    private BigDecimal overallScore;
    private String overallRisk;
    private List<String> recommendations;
    private List<String> concerns;
    private String methodology;
    private String assessor;
    private LocalDateTime assessedAt;
    private LocalDateTime completedAt;
    private String status;
    private String notes;

    public static EthicsAssessmentResponse fromEntity(EthicsAssessment entity) {
        return EthicsAssessmentResponse.builder()
                .id(entity.getId())
                .assessmentCode(entity.getAssessmentCode())
                .assetId(entity.getAssetId())
                .assetName(entity.getAssetName())
                .frameworkId(entity.getFrameworkId())
                .frameworkName(entity.getFrameworkName())
                .transparencyScore(entity.getTransparencyScore() != null ? entity.getTransparencyScore().name() : null)
                .transparencyValue(entity.getTransparencyValue())
                .fairnessScore(entity.getFairnessScore() != null ? entity.getFairnessScore().name() : null)
                .fairnessValue(entity.getFairnessValue())
                .accountabilityScore(entity.getAccountabilityScore() != null ? entity.getAccountabilityScore().name() : null)
                .accountabilityValue(entity.getAccountabilityValue())
                .privacyScore(entity.getPrivacyScore() != null ? entity.getPrivacyScore().name() : null)
                .privacyValue(entity.getPrivacyValue())
                .overallScore(entity.getOverallScore())
                .overallRisk(entity.getOverallRisk() != null ? entity.getOverallRisk().name() : null)
                .recommendations(entity.getRecommendations())
                .concerns(entity.getConcerns())
                .methodology(entity.getMethodology())
                .assessor(entity.getAssessor())
                .assessedAt(entity.getAssessedAt())
                .completedAt(entity.getCompletedAt())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .build();
    }
}
