package com.enterprise.dataplatform.ethics.service;

import com.enterprise.dataplatform.ethics.domain.dto.request.SocialImpactRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.SocialImpactResponse;
import com.enterprise.dataplatform.ethics.domain.entity.SocialImpactReport;
import com.enterprise.dataplatform.ethics.domain.enums.RiskLevel;
import com.enterprise.dataplatform.ethics.repository.SocialImpactReportRepository;
import com.enterprise.dataplatform.ethics.service.DataAssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialResponsibilityService {

    private final SocialImpactReportRepository reportRepository;
    private final DataAssetService dataAssetService;

    private static final List<String> STAKEHOLDER_CATEGORIES = Arrays.asList(
            "数据主体", "客户", "员工", "合作伙伴", "监管机构", "社区", "股东", "社会公众"
    );

    private static final BigDecimal PRIVACY_WEIGHT = new BigDecimal("0.25");
    private static final BigDecimal FAIRNESS_WEIGHT = new BigDecimal("0.20");
    private static final BigDecimal ECONOMIC_WEIGHT = new BigDecimal("0.15");
    private static final BigDecimal TRUST_WEIGHT = new BigDecimal("0.20");
    private static final BigDecimal COMPLIANCE_WEIGHT = new BigDecimal("0.20");

    @Transactional
    public SocialImpactResponse generateSocialImpactReport(SocialImpactRequest request, String analyst) {
        log.info("生成社会责任评估报告: 资产={}, 分析师={}", request.getAssetId(), analyst);

        String reportCode = "SIR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Map<String, Object> assetData = dataAssetService.getAssetDetails(request.getAssetId());

        BigDecimal privacyScore = calculatePrivacyScore(assetData);
        BigDecimal fairnessScore = calculateFairnessScore(assetData);
        BigDecimal economicScore = calculateEconomicScore(assetData);
        BigDecimal trustScore = calculateTrustScore(assetData);
        BigDecimal complianceScore = calculateComplianceScore(assetData);

        Map<String, BigDecimal> dimensionScores = new HashMap<>();
        dimensionScores.put("privacy", privacyScore);
        dimensionScores.put("fairness", fairnessScore);
        dimensionScores.put("economic", economicScore);
        dimensionScores.put("trust", trustScore);
        dimensionScores.put("compliance", complianceScore);

        BigDecimal overallImpactScore = calculateOverallImpactScore(dimensionScores);

        RiskLevel overallRisk = calculateSocialRiskLevel(overallImpactScore);

        List<String> positiveImpacts = analyzePositiveImpacts(dimensionScores);
        List<String> negativeImpacts = analyzeNegativeImpacts(dimensionScores);

        List<String> recommendations = generateSocialRecommendations(overallRisk, dimensionScores, negativeImpacts);

        List<String> affectedStakeholders = request.getAffectedStakeholders() != null ?
                request.getAffectedStakeholders() : STAKEHOLDER_CATEGORIES;

        List<Map<String, Object>> stakeholderImpacts = analyzeStakeholderImpacts(assetData, affectedStakeholders);

        List<Map<String, Object>> identifiedRisks = identifySocialRisks(assetData, dimensionScores);

        BigDecimal dataGovernanceScore = calculateDataGovernanceScore(assetData);
        BigDecimal transparencyScore = calculateTransparencyScore(assetData);
        BigDecimal accountabilityScore = calculateAccountabilityScore(assetData);
        BigDecimal finalComplianceScore = calculateFinalComplianceScore(assetData);

        SocialImpactReport report = SocialImpactReport.builder()
                .reportCode(reportCode)
                .assetId(request.getAssetId())
                .assetName(request.getAssetName())
                .reportDate(LocalDateTime.now())
                .reportPeriod(request.getReportPeriod())
                .overallRisk(overallRisk)
                .overallImpactScore(overallImpactScore)
                .dataUsageImpactScore(privacyScore)
                .stakeholderScore(fairnessScore)
                .socialValueScore(trustScore)
                .communityBenefitScore(economicScore)
                .dataUsageAnalysis(generateDataUsageAnalysis(privacyScore))
                .stakeholderAnalysis(generateStakeholderAnalysis(affectedStakeholders, stakeholderImpacts))
                .impactAssessment(generateImpactAssessment(overallRisk, overallImpactScore))
                .positiveImpacts(positiveImpacts)
                .negativeImpacts(negativeImpacts)
                .affectedStakeholders(affectedStakeholders)
                .recommendations(recommendations)
                .dataGovernanceScore(dataGovernanceScore)
                .transparencyScore(transparencyScore)
                .accountabilityScore(accountabilityScore)
                .complianceScore(finalComplianceScore)
                .analyst(analyst)
                .status("DRAFT")
                .approvalStatus("PENDING")
                .notes(request.getNotes())
                .build();

        report = reportRepository.save(report);

        log.info("社会责任报告生成成功: {}, score={}, risk={}", 
                report.getReportCode(), overallImpactScore, overallRisk);
        return SocialImpactResponse.fromEntity(report);
    }

    private BigDecimal calculatePrivacyScore(Map<String, Object> assetData) {
        BigDecimal baseScore = new BigDecimal("3.0");

        Boolean containsPersonalData = (Boolean) assetData.getOrDefault("containsPersonalData", false);
        Boolean isAnonymized = (Boolean) assetData.getOrDefault("isAnonymized", false);
        String securityLevel = String.valueOf(assetData.getOrDefault("securityLevel", "MEDIUM"));

        if (containsPersonalData && !isAnonymized) {
            baseScore = baseScore.subtract(new BigDecimal("0.5"));
        }

        switch (securityLevel) {
            case "HIGHLY_CONFIDENTIAL":
                baseScore = baseScore.add(new BigDecimal("0.5"));
                break;
            case "CONFIDENTIAL":
                baseScore = baseScore.add(new BigDecimal("0.3"));
                break;
            case "INTERNAL":
                baseScore = baseScore.add(new BigDecimal("0.1"));
                break;
            case "PUBLIC":
                baseScore = baseScore.add(new BigDecimal("0.5"));
                break;
        }

        Boolean hasEncryption = (Boolean) assetData.getOrDefault("hasEncryption", false);
        if (hasEncryption) {
            baseScore = baseScore.add(new BigDecimal("0.3"));
        }

        Boolean hasAccessControl = (Boolean) assetData.getOrDefault("hasAccessControl", false);
        if (hasAccessControl) {
            baseScore = baseScore.add(new BigDecimal("0.2"));
        }

        return baseScore.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFairnessScore(Map<String, Object> assetData) {
        BigDecimal baseScore = new BigDecimal("3.5");

        String dataSource = String.valueOf(assetData.getOrDefault("dataSource", "UNKNOWN"));
        switch (dataSource) {
            case "USER_CONSENT":
                baseScore = baseScore.add(new BigDecimal("0.5"));
                break;
            case "PUBLIC_SOURCE":
                baseScore = baseScore.add(new BigDecimal("0.3"));
                break;
            case "THIRD_PARTY":
                baseScore = baseScore.subtract(new BigDecimal("0.2"));
                break;
        }

        Boolean hasBiasDetection = (Boolean) assetData.getOrDefault("hasBiasDetection", false);
        if (hasBiasDetection) {
            baseScore = baseScore.add(new BigDecimal("0.3"));
        }

        Boolean hasFairnessAudit = (Boolean) assetData.getOrDefault("hasFairnessAudit", false);
        if (hasFairnessAudit) {
            baseScore = baseScore.add(new BigDecimal("0.2"));
        }

        return baseScore.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEconomicScore(Map<String, Object> assetData) {
        BigDecimal baseScore = new BigDecimal("3.0");

        Integer userCount = (Integer) assetData.getOrDefault("userCount", 0);
        if (userCount > 1000000) {
            baseScore = baseScore.add(new BigDecimal("0.5"));
        } else if (userCount > 100000) {
            baseScore = baseScore.add(new BigDecimal("0.3"));
        } else if (userCount > 10000) {
            baseScore = baseScore.add(new BigDecimal("0.1"));
        }

        String businessImpact = String.valueOf(assetData.getOrDefault("businessImpact", "MEDIUM"));
        switch (businessImpact) {
            case "HIGH":
                baseScore = baseScore.add(new BigDecimal("0.3"));
                break;
            case "MEDIUM":
                baseScore = baseScore.add(new BigDecimal("0.1"));
                break;
            case "LOW":
                baseScore = baseScore.subtract(new BigDecimal("0.1"));
                break;
        }

        return baseScore.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTrustScore(Map<String, Object> assetData) {
        BigDecimal baseScore = new BigDecimal("3.0");

        Boolean isTransparent = (Boolean) assetData.getOrDefault("isTransparent", false);
        if (isTransparent) {
            baseScore = baseScore.add(new BigDecimal("0.5"));
        }

        Boolean hasPrivacyPolicy = (Boolean) assetData.getOrDefault("hasPrivacyPolicy", false);
        if (hasPrivacyPolicy) {
            baseScore = baseScore.add(new BigDecimal("0.3"));
        }

        Boolean hasDataUsageDisclosure = (Boolean) assetData.getOrDefault("hasDataUsageDisclosure", false);
        if (hasDataUsageDisclosure) {
            baseScore = baseScore.add(new BigDecimal("0.2"));
        }

        Integer complaintCount = (Integer) assetData.getOrDefault("complaintCount", 0);
        if (complaintCount > 100) {
            baseScore = baseScore.subtract(new BigDecimal("0.5"));
        } else if (complaintCount > 10) {
            baseScore = baseScore.subtract(new BigDecimal("0.2"));
        }

        return baseScore.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateComplianceScore(Map<String, Object> assetData) {
        BigDecimal baseScore = new BigDecimal("3.0");

        List<String> regulations = (List<String>) assetData.getOrDefault("applicableRegulations", Collections.emptyList());

        if (regulations.contains("GDPR")) {
            baseScore = baseScore.add(new BigDecimal("0.3"));
        }
        if (regulations.contains("CCPA")) {
            baseScore = baseScore.add(new BigDecimal("0.3"));
        }
        if (regulations.contains("PIPL")) {
            baseScore = baseScore.add(new BigDecimal("0.3"));
        }

        Boolean hasComplianceCertification = (Boolean) assetData.getOrDefault("hasComplianceCertification", false);
        if (hasComplianceCertification) {
            baseScore = baseScore.add(new BigDecimal("0.3"));
        }

        Boolean hasAuditTrail = (Boolean) assetData.getOrDefault("hasAuditTrail", false);
        if (hasAuditTrail) {
            baseScore = baseScore.add(new BigDecimal("0.2"));
        }

        return baseScore.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateOverallImpactScore(Map<String, BigDecimal> dimensionScores) {
        BigDecimal weightedSum = BigDecimal.ZERO;

        weightedSum = weightedSum.add(dimensionScores.getOrDefault("privacy", BigDecimal.ZERO).multiply(PRIVACY_WEIGHT));
        weightedSum = weightedSum.add(dimensionScores.getOrDefault("fairness", BigDecimal.ZERO).multiply(FAIRNESS_WEIGHT));
        weightedSum = weightedSum.add(dimensionScores.getOrDefault("economic", BigDecimal.ZERO).multiply(ECONOMIC_WEIGHT));
        weightedSum = weightedSum.add(dimensionScores.getOrDefault("trust", BigDecimal.ZERO).multiply(TRUST_WEIGHT));
        weightedSum = weightedSum.add(dimensionScores.getOrDefault("compliance", BigDecimal.ZERO).multiply(COMPLIANCE_WEIGHT));

        return weightedSum.setScale(2, RoundingMode.HALF_UP);
    }

    private RiskLevel calculateSocialRiskLevel(BigDecimal overallScore) {
        if (overallScore == null) return RiskLevel.MEDIUM;
        double score = overallScore.doubleValue();
        if (score >= 4.0) return RiskLevel.LOW;
        if (score >= 3.0) return RiskLevel.MEDIUM;
        if (score >= 2.0) return RiskLevel.HIGH;
        return RiskLevel.CRITICAL;
    }

    private List<String> analyzePositiveImpacts(Map<String, BigDecimal> dimensionScores) {
        List<String> impacts = new ArrayList<>();

        BigDecimal privacyScore = dimensionScores.getOrDefault("privacy", BigDecimal.ZERO);
        BigDecimal trustScore = dimensionScores.getOrDefault("trust", BigDecimal.ZERO);
        BigDecimal complianceScore = dimensionScores.getOrDefault("compliance", BigDecimal.ZERO);

        if (privacyScore.doubleValue() >= 4.0) {
            impacts.add("数据使用透明合规，对社会产生积极影响");
        }
        if (trustScore.doubleValue() >= 4.0) {
            impacts.add("建立公众对数据使用的信任");
        }
        if (complianceScore.doubleValue() >= 4.0) {
            impacts.add("完全符合相关法规要求");
        }
        if (impacts.isEmpty()) {
            impacts.add("对社会产生中性影响");
        }
        return impacts;
    }

    private List<String> analyzeNegativeImpacts(Map<String, BigDecimal> dimensionScores) {
        List<String> impacts = new ArrayList<>();

        BigDecimal privacyScore = dimensionScores.getOrDefault("privacy", BigDecimal.ZERO);
        BigDecimal fairnessScore = dimensionScores.getOrDefault("fairness", BigDecimal.ZERO);
        BigDecimal trustScore = dimensionScores.getOrDefault("trust", BigDecimal.ZERO);

        if (privacyScore.doubleValue() < 2.5) {
            impacts.add("数据使用方式可能存在隐私风险");
        }
        if (fairnessScore.doubleValue() < 2.5) {
            impacts.add("部分利益相关方权益保障不足");
        }
        if (trustScore.doubleValue() < 2.5) {
            impacts.add("可能损害公众对数据使用的信任");
        }
        return impacts;
    }

    private List<String> generateSocialRecommendations(RiskLevel risk, 
                                                      Map<String, BigDecimal> dimensionScores,
                                                      List<String> negatives) {
        List<String> recommendations = new ArrayList<>();

        if (risk.getValue() >= 4) {
            recommendations.add("继续保持良好的社会责任实践");
        } else if (risk.getValue() >= 3) {
            recommendations.add("建议改进社会责任实践");
        } else {
            recommendations.add("立即评估并缓解社会负面影响");
        }

        BigDecimal privacyScore = dimensionScores.getOrDefault("privacy", BigDecimal.ZERO);
        if (privacyScore.doubleValue() < 3.0) {
            recommendations.add("加强数据隐私保护措施");
            recommendations.add("实施数据脱敏和加密");
        }

        BigDecimal fairnessScore = dimensionScores.getOrDefault("fairness", BigDecimal.ZERO);
        if (fairnessScore.doubleValue() < 3.0) {
            recommendations.add("建立公平性审计机制");
            recommendations.add("定期进行偏见检测");
        }

        BigDecimal trustScore = dimensionScores.getOrDefault("trust", BigDecimal.ZERO);
        if (trustScore.doubleValue() < 3.0) {
            recommendations.add("提高数据使用透明度");
            recommendations.add("完善数据使用政策披露");
        }

        return recommendations;
    }

    private List<Map<String, Object>> analyzeStakeholderImpacts(Map<String, Object> assetData, 
                                                                 List<String> stakeholders) {
        List<Map<String, Object>> impacts = new ArrayList<>();

        for (String stakeholder : stakeholders) {
            Map<String, Object> impact = new HashMap<>();
            impact.put("stakeholder", stakeholder);
            impact.put("impactLevel", "MEDIUM");
            impact.put("affectedCount", 0);
            impacts.add(impact);
        }

        return impacts;
    }

    private List<Map<String, Object>> identifySocialRisks(Map<String, Object> assetData, 
                                                          Map<String, BigDecimal> dimensionScores) {
        List<Map<String, Object>> risks = new ArrayList<>();

        BigDecimal privacyScore = dimensionScores.getOrDefault("privacy", BigDecimal.ZERO);
        if (privacyScore.doubleValue() < 2.5) {
            Map<String, Object> risk = new HashMap<>();
            risk.put("type", "PRIVACY_RISK");
            risk.put("severity", "HIGH");
            risk.put("description", "隐私保护措施不足");
            risk.put("mitigation", "加强数据加密和访问控制");
            risks.add(risk);
        }

        BigDecimal complianceScore = dimensionScores.getOrDefault("compliance", BigDecimal.ZERO);
        if (complianceScore.doubleValue() < 2.5) {
            Map<String, Object> risk = new HashMap<>();
            risk.put("type", "COMPLIANCE_RISK");
            risk.put("severity", "HIGH");
            risk.put("description", "合规性措施不足");
            risk.put("mitigation", "完善合规措施和审计机制");
            risks.add(risk);
        }

        return risks;
    }

    private BigDecimal calculateDataGovernanceScore(Map<String, Object> assetData) {
        BigDecimal baseScore = new BigDecimal("3.0");
        Boolean hasDataGovernance = (Boolean) assetData.getOrDefault("hasDataGovernance", false);
        if (hasDataGovernance) {
            baseScore = baseScore.add(new BigDecimal("0.5"));
        }
        return baseScore.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTransparencyScore(Map<String, Object> assetData) {
        BigDecimal baseScore = new BigDecimal("3.0");
        Boolean isTransparent = (Boolean) assetData.getOrDefault("isTransparent", false);
        if (isTransparent) {
            baseScore = baseScore.add(new BigDecimal("0.5"));
        }
        return baseScore.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAccountabilityScore(Map<String, Object> assetData) {
        BigDecimal baseScore = new BigDecimal("3.0");
        Boolean hasAccountability = (Boolean) assetData.getOrDefault("hasAccountability", false);
        if (hasAccountability) {
            baseScore = baseScore.add(new BigDecimal("0.5"));
        }
        return baseScore.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFinalComplianceScore(Map<String, Object> assetData) {
        return calculateComplianceScore(assetData);
    }

    private String generateDataUsageAnalysis(BigDecimal score) {
        if (score == null) return "数据使用影响分析暂不可用";
        double s = score.doubleValue();
        if (s >= 4.0) return "数据使用对社会产生积极影响，符合ESG标准";
        if (s >= 3.0) return "数据使用影响可控，需持续监控";
        if (s >= 2.0) return "数据使用存在一定社会风险，建议改进";
        return "数据使用存在严重社会风险，需要立即改进";
    }

    private String generateStakeholderAnalysis(List<String> stakeholders, 
                                               List<Map<String, Object>> impacts) {
        StringBuilder sb = new StringBuilder("已评估的利益相关方包括: ");
        sb.append(String.join(", ", stakeholders));
        sb.append("。各方的受影响程度已在报告中详细说明。");
        return sb.toString();
    }

    private String generateImpactAssessment(RiskLevel risk, BigDecimal score) {
        String scoreDesc = String.format("综合评分%.2f分", score != null ? score.doubleValue() : 0);
        switch (risk) {
            case LOW: return "整体社会影响积极，风险可控。" + scoreDesc;
            case MEDIUM: return "存在一定社会风险，需要关注和改进。" + scoreDesc;
            case HIGH: return "存在较高社会风险，需要制定改进计划。" + scoreDesc;
            case CRITICAL: return "存在严重社会风险，需要立即采取行动。" + scoreDesc;
            default: return "社会影响评估暂不可用";
        }
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

    public static class DataAssetService {
        public Map<String, Object> getAssetDetails(String assetId) {
            Map<String, Object> data = new HashMap<>();
            data.put("containsPersonalData", false);
            data.put("isAnonymized", true);
            data.put("securityLevel", "INTERNAL");
            data.put("hasEncryption", true);
            data.put("hasAccessControl", true);
            data.put("dataSource", "USER_CONSENT");
            data.put("hasBiasDetection", false);
            data.put("hasFairnessAudit", false);
            data.put("userCount", 10000);
            data.put("businessImpact", "MEDIUM");
            data.put("isTransparent", false);
            data.put("hasPrivacyPolicy", true);
            data.put("hasDataUsageDisclosure", false);
            data.put("complaintCount", 5);
            data.put("applicableRegulations", Arrays.asList("GDPR", "CCPA"));
            data.put("hasComplianceCertification", true);
            data.put("hasAuditTrail", true);
            data.put("hasDataGovernance", true);
            data.put("hasAccountability", false);
            return data;
        }
    }
}
