package com.enterprise.dataplatform.ruleengine.service;

import com.enterprise.dataplatform.ruleengine.domain.model.*;
import com.enterprise.dataplatform.ruleengine.domain.entity.RuleExecutionLog;

import java.util.List;
import java.util.Map;

/**
 * 规则引擎核心服务接口
 * 定义所有规则评估操作的统一接口
 */
public interface RuleEngineService {

    /**
     * 执行质量评分规则
     */
    QualityScoreResult evaluateQualityScore(AssetEvaluation asset);

    /**
     * 执行合规检查规则
     */
    ComplianceResult checkCompliance(AssetEvaluation asset);

    /**
     * 执行价值评估规则
     */
    ValueScoreResult evaluateValue(AssetEvaluation asset);

    /**
     * 执行生命周期评估规则
     */
    LifecycleResult evaluateLifecycle(AssetEvaluation asset);

    /**
     * 执行治理规则评估
     */
    GovernanceRuleResult evaluateGovernance(AssetEvaluation asset);

    /**
     * 综合评估（执行所有规则）
     */
    Map<String, Object> evaluateAll(AssetEvaluation asset);

    /**
     * 动态重载规则
     */
    void reloadRules();

    /**
     * 获取规则执行日志
     */
    List<RuleExecutionLog> getExecutionLogs(String assetId);

    /**
     * 测试规则（使用自定义输入）
     */
    Map<String, Object> testRule(String category, Map<String, Object> input);
}
