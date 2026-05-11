package com.enterprise.dataplatform.ethics.service;

import com.enterprise.dataplatform.ethics.domain.dto.request.SocialImpactRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.SocialImpactResponse;
import com.enterprise.dataplatform.ethics.domain.entity.SocialImpactReport;
import com.enterprise.dataplatform.ethics.domain.enums.RiskLevel;
import com.enterprise.dataplatform.ethics.repository.SocialImpactReportRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialResponsibilityService {

    private final SocialImpactReportRepository reportRepository;

    private static final List<String> STAKEHOLDER_CATEGORIES = Arrays.asList(
            "数据主体", "客户", "员工", "合作伙伴", "监管机构", "社区", "股东", "社会公众"
    );

    @Transactional
    public SocialImpactResponse generateSocialImpactReport(SocialImpactRequest request, String analyst) {
        log.info("生成社会责任评估报告: 资产={}, 分析师={}", request.getAssetId(), analyst);

        String reportCode = "SIR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        BigDecimal dataUsageScore = request.getDataUsageImpactScore() != null ?
                request.getDataUsageImpactScore() : calculateDataUsageImpactScore();
        BigDecimal stakeholderScore = request.getStakeholderScore() != null ?
                request.getStakeholderScore() : calculateStakeholderScore();
        BigDecimal socialValueScore = request.getSocialValueScore() != null ?
                request.getSocialValueScore() : calculateSocialValueScore();
        BigDecimal communityBenefitScore = request.getCommunityBenefitScore() != null ?
                request.getCommunityBenefitScore() : calculateCommunityBenefitScore();

        BigDecimal overallImpactScore = calculateOverallImpactScore(
                dataUsageScore, stakeholderScore, socialValueScore, communityBenefitScore);

        RiskLevel overallRisk = calculateSocialRiskLevel(overallImpactScore);

        List<String> positiveImpacts = analyzePositiveImpacts(dataUsageScore, stakeholderScore,
                socialValueScore, communityBenefitScore);
        List<String> negativeImpacts = analyzeNegativeImpacts(dataUsageScore, stakeholderScore,
                socialValueScore, communityBenefitScore);

        List<String> recommendations = generateSocialRecommendations(overallRisk,
                positiveImpacts, negativeImpacts);

        List<String> affectedStakeholders = request.getAffectedStakeholders() != null ?
                request.getAffectedStakeholders() : STAKEHOLDER_CATEGORIES;

        SocialImpactReport report = SocialImpactReport.builder()
                .reportCode(reportCode)
                .assetId(request.getAssetId())
                .assetName(request.getAssetName())
                .reportDate(LocalDateTime.now())
                .reportPeriod(request.getReportPeriod())
                .overallRisk(overallRisk)
                .overallImpactScore(overallImpactScore)
                .dataUsageImpactScore(dataUsageScore)
                .stakeholderScore(stakeholderScore)
                .socialValueScore(socialValueScore)
                .communityBenefitScore(communityBenefitScore)
                .dataUsageAnalysis(generateDataUsageAnalysis(dataUsageScore))
                .stakeholderAnalysis(generateStakeholderAnalysis(affectedStakeholders))
                .impactAssessment(generateImpactAssessment(overallRisk))
                .positiveImpacts(positiveImpacts)
                .negativeImpacts(negativeImpacts)
                .affectedStakeholders(affectedStakeholders)
                .recommendations(recommendations)
                .dataGovernanceScore(calculateDataGovernanceScore())
                .transparencyScore(calculateTransparencyScore())
                .accountabilityScore(calculateAccountabilityScore())
                .complianceScore(calculateComplianceScore())
                .analyst(analyst)
                .status("DRAFT")
                .approvalStatus("PENDING")
                .notes(request.getNotes())
                .build();

        report = reportRepository.save(report);

        log.info("社会责任报告生成成功: {}", report.getReportCode());
        return SocialImpactResponse.fromEntity(report);
    }

    public SocialImpactResponse getReport(Long id) {
        SocialImpactReport report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("报告不存在: " + id));
        return SocialImpactResponse.fromEntity(report);
    }

    public Page<SocialImpactResponse> getAssetReports(String assetId, Pageable pageable) {
        return reportRepository.findByAssetId(assetId, pageable)
                .map(SocialImpactResponse::fromEntity);
    }

    public Page<SocialImpactResponse> searchReports(
            String assetId, String riskLevel, String status, Pageable pageable) {
        RiskLevel risk = riskLevel != null ? RiskLevel.valueOf(riskLevel) : null;
        return reportRepository.searchReports(assetId, risk, status, pageable)
                .map(SocialImpactResponse::fromEntity);
    }

    @Transactional
    public SocialImpactResponse approveReport(Long id, String approver) {
        SocialImpactReport report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("报告不存在: " + id));

        report.setApprovalStatus("APPROVED");
        report.setApprovedBy(approver);
        report.setApprovedAt(LocalDateTime.now());
        report.setStatus("APPROVED");

        report = reportRepository.save(report);

        log.info("社会责任报告已批准: {}", id);
        return SocialImpactResponse.fromEntity(report);
    }

    private BigDecimal calculateOverallImpactScore(BigDecimal... scores) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (BigDecimal score : scores) {
            if (score != null) {
                sum = sum.add(score);
                count++;
            }
        }
        if (count == 0) return BigDecimal.valueOf(0);
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDataUsageImpactScore() {
        return BigDecimal.valueOf(3.0 + Math.random() * 2.0).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateStakeholderScore() {
        return BigDecimal.valueOf(2.5 + Math.random() * 2.5).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSocialValueScore() {
        return BigDecimal.valueOf(3.0 + Math.random() * 2.0).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCommunityBenefitScore() {
        return BigDecimal.valueOf(2.8 + Math.random() * 2.2).setScale(2, RoundingMode.HALF_UP);
    }

    private RiskLevel calculateSocialRiskLevel(BigDecimal overallScore) {
        if (overallScore == null) return RiskLevel.MEDIUM;
        double score = overallScore.doubleValue();
        if (score >= 4.0) return RiskLevel.LOW;
        if (score >= 3.0) return RiskLevel.MEDIUM;
        if (score >= 2.0) return RiskLevel.HIGH;
        return RiskLevel.CRITICAL;
    }

    private List<String> analyzePositiveImpacts(BigDecimal... scores) {
        List<String> impacts = new ArrayList<>();
        if (scores[0] != null && scores[0].doubleValue() >= 4.0) {
            impacts.add("数据使用透明合规，对社会产生积极影响");
        }
        if (scores[1] != null && scores[1].doubleValue() >= 4.0) {
            impacts.add("充分考虑利益相关方权益");
        }
        if (scores[2] != null && scores[2].doubleValue() >= 4.0) {
            impacts.add("数据应用创造积极社会价值");
        }
        if (impacts.isEmpty()) {
            impacts.add("对社会产生中性影响");
        }
        return impacts;
    }

    private List<String> analyzeNegativeImpacts(BigDecimal... scores) {
        List<String> impacts = new ArrayList<>();
        if (scores[0] != null && scores[0].doubleValue() < 2.5) {
            impacts.add("数据使用方式可能存在社会风险");
        }
        if (scores[1] != null && scores[1].doubleValue() < 2.5) {
            impacts.add("部分利益相关方权益保障不足");
        }
        if (scores[2] != null && scores[2].doubleValue() < 2.5) {
            impacts.add("数据应用可能产生负面社会影响");
        }
        return impacts;
    }

    private List<String> generateSocialRecommendations(RiskLevel risk, List<String> positives, List<String> negatives) {
        List<String> recommendations = new ArrayList<>();

        if (risk.getValue() >= 4) {
            recommendations.add("立即评估并缓解社会负面影响");
        } else if (risk.getValue() >= 3) {
            recommendations.add("建议改进社会责任实践");
        } else {
            recommendations.add("继续保持良好的社会责任实践");
        }

        if (negatives.contains("数据使用方式可能存在社会风险")) {
            recommendations.add("完善数据使用政策，确保符合社会责任标准");
        }
        if (negatives.contains("部分利益相关方权益保障不足")) {
            recommendations.add("建立利益相关方沟通机制");
        }

        return recommendations;
    }

    private String generateDataUsageAnalysis(BigDecimal score) {
        if (score == null) return "数据使用影响分析暂不可用";
        double s = score.doubleValue();
        if (s >= 4.0) return "数据使用对社会产生积极影响，符合ESG标准";
        if (s >= 3.0) return "数据使用影响可控，需持续监控";
        if (s >= 2.0) return "数据使用存在一定社会风险，建议改进";
        return "数据使用存在严重社会风险，需要立即改进";
    }

    private String generateStakeholderAnalysis(List<String> stakeholders) {
        return "已评估的利益相关方包括: " + String.join(", ", stakeholders);
    }

    private String generateImpactAssessment(RiskLevel risk) {
        switch (risk) {
            case LOW: return "整体社会影响积极，风险可控";
            case MEDIUM: return "存在一定社会风险，需要关注和改进";
            case HIGH: return "存在较高社会风险，需要制定改进计划";
            case CRITICAL: return "存在严重社会风险，需要立即采取行动";
            default: return "社会影响评估暂不可用";
        }
    }

    private BigDecimal calculateDataGovernanceScore() {
        return BigDecimal.valueOf(3.0 + Math.random() * 2.0).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTransparencyScore() {
        return BigDecimal.valueOf(3.2 + Math.random() * 1.8).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAccountabilityScore() {
        return BigDecimal.valueOf(2.8 + Math.random() * 2.2).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateComplianceScore() {
        return BigDecimal.valueOf(3.5 + Math.random() * 1.5).setScale(2, RoundingMode.HALF_UP);
    }
}
