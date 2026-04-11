package com.enterprise.dataplatform.ruleengine.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 生命周期评估结果对象
 */
@Data
public class LifecycleResult {

    private String assetId;
    private String currentPhase;
    private String recommendedPhase;  // ACTIVE, ARCHIVED, FROZEN, RETIRED
    private int inactiveDays;
    private double dailyAccessCount;
    private String action;  // KEEP, ARCHIVE, FREEZE, RETIRE
    private String actionReason;
    private List<String> triggeredRules = new ArrayList<>();
    private String evaluationSummary;
}
