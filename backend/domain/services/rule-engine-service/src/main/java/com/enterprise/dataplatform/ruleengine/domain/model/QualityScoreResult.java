package com.enterprise.dataplatform.ruleengine.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 质量评分结果对象
 */
@Data
public class QualityScoreResult {

    private String assetId;
    private double qualityScore;
    private String qualityLevel;  // EXCELLENT, GOOD, ACCEPTABLE, NEEDS_IMPROVEMENT, POOR
    private int qualityIssues;
    private double completeness;
    private double accuracy;
    private double consistency;
    private double timeliness;
    private double uniqueness;
    private List<String> triggeredRules = new ArrayList<>();
    private String evaluationSummary;
    private LocalDateTime evaluationTime;

    /**
     * 根据评分获取质量等级
     */
    public static String getQualityLevel(double score) {
        if (score >= 95) return "EXCELLENT";
        if (score >= 80) return "GOOD";
        if (score >= 60) return "ACCEPTABLE";
        if (score >= 40) return "NEEDS_IMPROVEMENT";
        return "POOR";
    }
}
