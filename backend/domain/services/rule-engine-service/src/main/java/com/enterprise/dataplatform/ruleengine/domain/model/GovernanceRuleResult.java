package com.enterprise.dataplatform.ruleengine.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 治理规则评估结果对象
 */
@Data
public class GovernanceRuleResult {

    private String assetId;
    private boolean needsGovernanceAction;
    private String governanceActionType;  // QUALITY_FIX, COMPLIANCE_FIX, LIFECYCLE_MANAGE, ACCESS_REVIEW, SENSITIVITY_REVIEW
    private String governancePriority;  // CRITICAL, HIGH, MEDIUM, LOW
    private String actionDescription;
    private List<String> triggeredRules = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();
    private String evaluationSummary;
}
