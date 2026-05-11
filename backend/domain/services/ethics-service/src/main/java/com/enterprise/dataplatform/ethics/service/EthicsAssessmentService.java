package com.enterprise.dataplatform.ethics.service;

import com.enterprise.dataplatform.ethics.domain.dto.request.EthicsAssessmentRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.EthicsAssessmentResponse;
import com.enterprise.dataplatform.ethics.domain.entity.EthicsAssessment;
import com.enterprise.dataplatform.ethics.domain.entity.EthicsFramework;
import com.enterprise.dataplatform.ethics.domain.enums.EthicsScore;
import com.enterprise.dataplatform.ethics.domain.enums.RiskLevel;
import com.enterprise.dataplatform.ethics.repository.EthicsAssessmentRepository;
import com.enterprise.dataplatform.ethics.repository.EthicsFrameworkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EthicsAssessmentService {

    private final EthicsAssessmentRepository assessmentRepository;
    private final EthicsFrameworkRepository frameworkRepository;

    @Transactional
    public EthicsAssessmentResponse createAssessment(EthicsAssessmentRequest request, String assessor) {
        log.info("创建伦理评估: 资产={}, 评估人={}", request.getAssetId(), assessor);

        String assessmentCode = "EA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        EthicsFramework framework = null;
        if (request.getFrameworkId() != null) {
            framework = frameworkRepository.findById(request.getFrameworkId()).orElse(null);
        }

        BigDecimal overallScore = calculateOverallScore(
                request.getTransparencyValue(),
                request.getFairnessValue(),
                request.getAccountabilityValue(),
                request.getPrivacyValue()
        );

        EthicsScore transparencyScore = calculateScore(request.getTransparencyValue());
        EthicsScore fairnessScore = calculateScore(request.getFairnessValue());
        EthicsScore accountabilityScore = calculateScore(request.getAccountabilityValue());
        EthicsScore privacyScore = calculateScore(request.getPrivacyValue());

        RiskLevel overallRisk = calculateRiskLevel(overallScore);
        List<String> recommendations = generateRecommendations(
                transparencyScore, fairnessScore, accountabilityScore, privacyScore, overallRisk);
        List<String> concerns = generateConcerns(
                transparencyScore, fairnessScore, accountabilityScore, privacyScore);

        EthicsAssessment assessment = EthicsAssessment.builder()
                .assessmentCode(assessmentCode)
                .assetId(request.getAssetId())
                .assetName(request.getAssetName())
                .frameworkId(request.getFrameworkId())
                .frameworkName(request.getFrameworkName() != null ? request.getFrameworkName() :
                        (framework != null ? framework.getFrameworkName() : null))
                .transparencyScore(transparencyScore)
                .transparencyValue(request.getTransparencyValue())
                .fairnessScore(fairnessScore)
                .fairnessValue(request.getFairnessValue())
                .accountabilityScore(accountabilityScore)
                .accountabilityValue(request.getAccountabilityValue())
                .privacyScore(privacyScore)
                .privacyValue(request.getPrivacyValue())
                .overallScore(overallScore)
                .overallRisk(overallRisk)
                .recommendations(recommendations)
                .concerns(concerns)
                .methodology(request.getMethodology() != null ? request.getMethodology() : "AUTOMATED")
                .assessor(assessor)
                .completedAt(LocalDateTime.now())
                .status("COMPLETED")
                .notes(request.getNotes())
                .build();

        assessment = assessmentRepository.save(assessment);

        log.info("伦理评估创建成功: {}", assessment.getAssessmentCode());
        return EthicsAssessmentResponse.fromEntity(assessment);
    }

    public EthicsAssessmentResponse getAssessment(Long id) {
        EthicsAssessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("评估不存在: " + id));
        return EthicsAssessmentResponse.fromEntity(assessment);
    }

    public EthicsAssessmentResponse getAssessmentByCode(String assessmentCode) {
        EthicsAssessment assessment = assessmentRepository.findByAssessmentCode(assessmentCode)
                .orElseThrow(() -> new IllegalArgumentException("评估不存在: " + assessmentCode));
        return EthicsAssessmentResponse.fromEntity(assessment);
    }

    public Page<EthicsAssessmentResponse> getAssetAssessmentHistory(String assetId, Pageable pageable) {
        return assessmentRepository.findAssetAssessmentHistory(assetId, pageable)
                .map(EthicsAssessmentResponse::fromEntity);
    }

    public Page<EthicsAssessmentResponse> searchAssessments(
            String assetId, Long frameworkId, String riskLevel, String status, Pageable pageable) {
        RiskLevel risk = riskLevel != null ? RiskLevel.valueOf(riskLevel) : null;
        return assessmentRepository.searchAssessments(assetId, frameworkId, risk, status, pageable)
                .map(EthicsAssessmentResponse::fromEntity);
    }

    public Double getAverageScoreByAsset(String assetId) {
        return assessmentRepository.calculateAverageScoreByAsset(assetId);
    }

    private BigDecimal calculateOverallScore(BigDecimal transparency, BigDecimal fairness,
            BigDecimal accountability, BigDecimal privacy) {
        List<BigDecimal> scores = new ArrayList<>();
        if (transparency != null) scores.add(transparency);
        if (fairness != null) scores.add(fairness);
        if (accountability != null) scores.add(accountability);
        if (privacy != null) scores.add(privacy);

        if (scores.isEmpty()) {
            return BigDecimal.valueOf(0);
        }

        BigDecimal sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
    }

    private EthicsScore calculateScore(BigDecimal value) {
        if (value == null) {
            return EthicsScore.FAIR;
        }
        return EthicsScore.fromValue(value.doubleValue());
    }

    private RiskLevel calculateRiskLevel(BigDecimal overallScore) {
        if (overallScore == null) {
            return RiskLevel.HIGH;
        }
        double score = overallScore.doubleValue();
        if (score >= 4.0) return RiskLevel.LOW;
        if (score >= 3.0) return RiskLevel.MEDIUM;
        if (score >= 2.0) return RiskLevel.HIGH;
        return RiskLevel.CRITICAL;
    }

    private List<String> generateRecommendations(EthicsScore transparency, EthicsScore fairness,
            EthicsScore accountability, EthicsScore privacy, RiskLevel risk) {
        List<String> recommendations = new ArrayList<>();

        if (transparency.getValue() <= 2) {
            recommendations.add("建议提高数据透明度，明确披露数据来源和使用目的");
        }
        if (fairness.getValue() <= 2) {
            recommendations.add("建议进行公平性评估，确保数据使用不歧视特定群体");
        }
        if (accountability.getValue() <= 2) {
            recommendations.add("建议建立数据问责机制，明确数据责任主体");
        }
        if (privacy.getValue() <= 2) {
            recommendations.add("建议加强隐私保护措施，实施数据脱敏和加密");
        }
        if (risk.getValue() >= 4) {
            recommendations.add("高风险评估，建议立即进行伦理审查");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("继续保持当前数据伦理实践");
        }

        return recommendations;
    }

    private List<String> generateConcerns(EthicsScore transparency, EthicsScore fairness,
            EthicsScore accountability, EthicsScore privacy) {
        List<String> concerns = new ArrayList<>();

        if (transparency.getValue() <= 2) {
            concerns.add("数据透明度不足");
        }
        if (fairness.getValue() <= 2) {
            concerns.add("存在潜在的数据偏见风险");
        }
        if (accountability.getValue() <= 2) {
            concerns.add("数据问责机制不完善");
        }
        if (privacy.getValue() <= 2) {
            concerns.add("隐私保护措施不足");
        }

        return concerns;
    }
}
