package com.enterprise.dataplatform.ruleengine.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合规检查结果对象
 */
@Data
public class ComplianceResult {

    private String assetId;
    private String complianceStatus;  // COMPLIANT, NON_COMPLIANT, PARTIAL_COMPLIANT
    private String standardCode;
    private boolean nameCompliant;
    private boolean typeCompliant;
    private boolean formatCompliant;
    private boolean nullableCompliant;
    private boolean rangeCompliant;
    private Map<String, String> violationDetails = new HashMap<>();
    private List<String> triggeredRules = new ArrayList<>();
    private String evaluationSummary;

    /**
     * 判断总体合规状态
     */
    public void evaluateOverallStatus() {
        boolean allCompliant = nameCompliant && typeCompliant && formatCompliant
                && nullableCompliant && rangeCompliant;

        int compliantCount = 0;
        int totalCount = 5;
        if (nameCompliant) compliantCount++;
        if (typeCompliant) compliantCount++;
        if (formatCompliant) compliantCount++;
        if (nullableCompliant) compliantCount++;
        if (rangeCompliant) compliantCount++;

        if (allCompliant) {
            complianceStatus = "COMPLIANT";
            evaluationSummary = "数据完全符合标准要求";
        } else if (compliantCount >= 3) {
            complianceStatus = "PARTIAL_COMPLIANT";
            evaluationSummary = "数据部分符合标准，存在" + (totalCount - compliantCount) + "项不合规";
        } else {
            complianceStatus = "NON_COMPLIANT";
            evaluationSummary = "数据严重不符合标准，需立即修复";
        }
    }
}
