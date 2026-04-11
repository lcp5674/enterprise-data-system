package com.enterprise.dataplatform.ruleengine.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 价值评估结果对象
 */
@Data
public class ValueScoreResult {

    private String assetId;
    private double valueScore;
    private String valueLevel;  // HIGH_VALUE, MEDIUM_VALUE, LOW_VALUE, NO_VALUE
    private double usageScore;
    private double businessScore;
    private double qualityScore;
    private double uniquenessScore;
    private List<String> triggeredRules = new ArrayList<>();
    private String evaluationSummary;
    private List<String> valueDrivers = new ArrayList<>();

    /**
     * 根据评分获取价值等级
     */
    public static String getValueLevel(double score) {
        if (score >= 80) return "HIGH_VALUE";
        if (score >= 60) return "MEDIUM_VALUE";
        if (score >= 30) return "LOW_VALUE";
        return "NO_VALUE";
    }
}
