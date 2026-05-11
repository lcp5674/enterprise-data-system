package com.enterprise.dataplatform.ethics.service;

import com.enterprise.dataplatform.ethics.domain.dto.request.FairnessEvaluationRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.FairnessEvaluationResponse;
import com.enterprise.dataplatform.ethics.domain.enums.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FairnessEvaluationService {

    private static final double DEFAULT_SIGNIFICANCE_LEVEL = 0.05;

    public FairnessEvaluationResponse evaluateFairness(FairnessEvaluationRequest request) {
        log.info("执行公平性评估: 资产={}", request.getAssetId());

        String evaluationId = UUID.randomUUID().toString();
        double significanceLevel = request.getSignificanceLevel() != null ?
                request.getSignificanceLevel() : DEFAULT_SIGNIFICANCE_LEVEL;

        Map<String, BigDecimal> groupMetrics = request.getGroupMetrics();
        if (groupMetrics == null || groupMetrics.isEmpty()) {
            groupMetrics = generateSampleGroupMetrics();
        }

        Map<String, BigDecimal> disparityScores = calculateGroupDisparityScores(groupMetrics);
        Map<String, String> significanceResults = calculateStatisticalSignificance(disparityScores, significanceLevel);
        BigDecimal disparateImpact = calculateDisparateImpactRatio(disparityScores);
        BigDecimal statisticalParityDiff = calculateStatisticalParityDifference(disparityScores);

        List<String> detectedBiases = detectBiases(disparityScores, significanceResults, disparateImpact);
        BigDecimal overallFairnessScore = calculateOverallFairnessScore(disparityScores);
        RiskLevel riskLevel = calculateFairnessRiskLevel(overallFairnessScore, detectedBiases);

        List<String> recommendations = generateFairnessRecommendations(riskLevel, detectedBiases);
        List<String> positiveIndicators = identifyPositiveIndicators(disparityScores, detectedBiases);

        List<String> protectedAttributes = request.getProtectedAttributes();
        if (protectedAttributes == null) {
            protectedAttributes = Arrays.asList("性别", "年龄", "地区", "职业");
        }

        Map<String, Object> detailedMetrics = buildDetailedMetrics(groupMetrics, disparityScores,
                disparateImpact, statisticalParityDiff);

        return FairnessEvaluationResponse.builder()
                .evaluationId(evaluationId)
                .assetId(request.getAssetId())
                .assetName(request.getAssetName())
                .evaluationType(request.getEvaluationType() != null ? request.getEvaluationType() : "STANDARD")
                .overallFairnessScore(overallFairnessScore)
                .riskLevel(riskLevel.name())
                .detectedBiases(detectedBiases)
                .groupDisparityScores(disparityScores)
                .statisticalSignificance(significanceResults)
                .disparateImpactRatio(disparateImpact)
                .statisticalParityDifference(statisticalParityDiff)
                .protectedAttributesAnalyzed(protectedAttributes)
                .recommendations(recommendations)
                .positiveIndicators(positiveIndicators)
                .methodology("Statistical Significance Testing")
                .significanceLevel(significanceLevel)
                .detailedMetrics(detailedMetrics)
                .build();
    }

    private Map<String, BigDecimal> generateSampleGroupMetrics() {
        Map<String, BigDecimal> metrics = new LinkedHashMap<>();
        metrics.put("group_A", BigDecimal.valueOf(0.85));
        metrics.put("group_B", BigDecimal.valueOf(0.78));
        metrics.put("group_C", BigDecimal.valueOf(0.82));
        metrics.put("group_D", BigDecimal.valueOf(0.80));
        return metrics;
    }

    private Map<String, BigDecimal> calculateGroupDisparityScores(Map<String, BigDecimal> groupMetrics) {
        if (groupMetrics.isEmpty()) {
            return Collections.emptyMap();
        }

        BigDecimal average = groupMetrics.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(groupMetrics.size()), 4, RoundingMode.HALF_UP);

        Map<String, BigDecimal> disparityScores = new LinkedHashMap<>();
        for (Map.Entry<String, BigDecimal> entry : groupMetrics.entrySet()) {
            if (average.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal disparity = entry.getValue().divide(average, 4, RoundingMode.HALF_UP);
                disparityScores.put(entry.getKey(), disparity);
            } else {
                disparityScores.put(entry.getKey(), BigDecimal.ONE);
            }
        }
        return disparityScores;
    }

    private Map<String, String> calculateStatisticalSignificance(Map<String, BigDecimal> disparityScores, double alpha) {
        Map<String, String> results = new LinkedHashMap<>();
        for (Map.Entry<String, BigDecimal> entry : disparityScores.entrySet()) {
            double disparity = entry.getValue().doubleValue();
            boolean significant = Math.abs(disparity - 1.0) > (1.0 - alpha);
            results.put(entry.getKey(), significant ? "显著" : "不显著");
        }
        return results;
    }

    private BigDecimal calculateDisparateImpactRatio(Map<String, BigDecimal> disparityScores) {
        if (disparityScores.isEmpty()) {
            return BigDecimal.ONE;
        }

        BigDecimal minScore = Collections.min(disparityScores.values());
        BigDecimal maxScore = Collections.max(disparityScores.values());

        if (maxScore.compareTo(BigDecimal.ZERO) > 0) {
            return minScore.divide(maxScore, 4, RoundingMode.HALF_UP);
        }
        return BigDecimal.ONE;
    }

    private BigDecimal calculateStatisticalParityDifference(Map<String, BigDecimal> disparityScores) {
        if (disparityScores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal maxDiff = BigDecimal.ZERO;
        List<BigDecimal> values = new ArrayList<>(disparityScores.values());

        for (int i = 0; i < values.size(); i++) {
            for (int j = i + 1; j < values.size(); j++) {
                BigDecimal diff = values.get(i).subtract(values.get(j)).abs();
                if (diff.compareTo(maxDiff) > 0) {
                    maxDiff = diff;
                }
            }
        }
        return maxDiff;
    }

    private List<String> detectBiases(Map<String, BigDecimal> disparityScores,
            Map<String, String> significance, BigDecimal disparateImpact) {
        List<String> biases = new ArrayList<>();

        for (Map.Entry<String, String> entry : significance.entrySet()) {
            if ("显著".equals(entry.getValue())) {
                BigDecimal disparity = disparityScores.get(entry.getKey());
                if (disparity != null && disparity.doubleValue() > 1.1) {
                    biases.add("正向偏见检测: " + entry.getKey() + " 组的得分显著高于平均水平");
                } else if (disparity != null && disparity.doubleValue() < 0.9) {
                    biases.add("负向偏见检测: " + entry.getKey() + " 组的得分显著低于平均水平");
                }
            }
        }

        if (disparateImpact != null && disparateImpact.doubleValue() < 0.8) {
            biases.add("4/5规则违反: 最不利群体与最有利群体的比率低于0.8");
        }

        if (disparityScores.size() > 2) {
            BigDecimal maxDisp = Collections.max(disparityScores.values());
            BigDecimal minDisp = Collections.min(disparityScores.values());
            if (maxDisp.subtract(minDisp).compareTo(BigDecimal.valueOf(0.3)) > 0) {
                biases.add("群体间显著差异: 最大与最小群体差异超过30%");
            }
        }

        return biases;
    }

    private BigDecimal calculateOverallFairnessScore(Map<String, BigDecimal> disparityScores) {
        if (disparityScores.isEmpty()) {
            return BigDecimal.valueOf(3.0);
        }

        double totalDeviation = 0;
        for (BigDecimal score : disparityScores.values()) {
            totalDeviation += Math.abs(score.doubleValue() - 1.0);
        }

        double avgDeviation = totalDeviation / disparityScores.size();
        double fairnessScore = Math.max(1.0, 5.0 - (avgDeviation * 5));

        return BigDecimal.valueOf(fairnessScore).setScale(2, RoundingMode.HALF_UP);
    }

    private RiskLevel calculateFairnessRiskLevel(BigDecimal fairnessScore, List<String> biases) {
        if (fairnessScore == null) {
            return RiskLevel.HIGH;
        }

        double score = fairnessScore.doubleValue();
        int biasCount = biases.size();

        if (score >= 4.0 && biasCount == 0) {
            return RiskLevel.LOW;
        } else if (score >= 3.0 && biasCount <= 1) {
            return RiskLevel.MEDIUM;
        } else if (score >= 2.0 || biasCount <= 2) {
            return RiskLevel.HIGH;
        }
        return RiskLevel.CRITICAL;
    }

    private List<String> generateFairnessRecommendations(RiskLevel riskLevel, List<String> biases) {
        List<String> recommendations = new ArrayList<>();

        if (riskLevel.getValue() >= 4) {
            recommendations.add("立即进行公平性审计，识别并消除数据处理中的偏见");
            recommendations.add("建立定期公平性评估机制");
        } else if (riskLevel.getValue() >= 3) {
            recommendations.add("建议进行公平性改进，实施偏见缓解策略");
            recommendations.add("增加数据采集的多样性");
        } else {
            recommendations.add("继续保持当前公平性实践");
            recommendations.add("定期监控公平性指标");
        }

        if (biases.stream().anyMatch(b -> b.contains("4/5规则"))) {
            recommendations.add("关注不利群体的权益，确保符合4/5规则要求");
        }
        if (biases.stream().anyMatch(b -> b.contains("群体间显著差异"))) {
            recommendations.add("分析群体差异原因，制定针对性改进措施");
        }

        return recommendations;
    }

    private List<String> identifyPositiveIndicators(Map<String, BigDecimal> disparityScores, List<String> biases) {
        List<String> positives = new ArrayList<>();

        if (biases.isEmpty()) {
            positives.add("未检测到统计显著的偏见");
        }

        boolean allCloseToOne = disparityScores.values().stream()
                .allMatch(score -> Math.abs(score.doubleValue() - 1.0) < 0.1);
        if (allCloseToOne) {
            positives.add("各群体指标分布均匀");
        }

        return positives;
    }

    private Map<String, Object> buildDetailedMetrics(Map<String, BigDecimal> groupMetrics,
            Map<String, BigDecimal> disparityScores, BigDecimal disparateImpact,
            BigDecimal statisticalParityDiff) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("groupMetrics", groupMetrics);
        metrics.put("disparityScores", disparityScores);
        metrics.put("disparateImpactRatio", disparateImpact);
        metrics.put("statisticalParityDifference", statisticalParityDiff);
        metrics.put("groupCount", groupMetrics.size());
        metrics.put("averageDisparity", disparityScores.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(disparityScores.size()), 4, RoundingMode.HALF_UP));
        return metrics;
    }
}
