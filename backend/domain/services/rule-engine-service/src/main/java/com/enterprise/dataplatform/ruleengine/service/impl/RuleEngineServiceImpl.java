package com.enterprise.dataplatform.ruleengine.service.impl;

import com.enterprise.dataplatform.ruleengine.config.DroolsConfig;
import com.enterprise.dataplatform.ruleengine.domain.model.*;
import com.enterprise.dataplatform.ruleengine.domain.entity.RuleExecutionLog;
import com.enterprise.dataplatform.ruleengine.repository.RuleExecutionLogRepository;
import com.enterprise.dataplatform.ruleengine.service.RuleEngineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kie.api.KieContainer;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 规则引擎核心服务实现
 * 使用Drools执行各类业务规则评估
 */
@Service
public class RuleEngineServiceImpl implements RuleEngineService {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineServiceImpl.class);

    @Autowired
    private KieContainer kieContainer;

    @Autowired
    private DroolsConfig droolsConfig;

    @Autowired
    @Qualifier("qualitySession")
    private KieSession qualitySession;

    @Autowired
    @Qualifier("complianceSession")
    private KieSession complianceSession;

    @Autowired
    @Qualifier("valueSession")
    private KieSession valueSession;

    @Autowired
    @Qualifier("lifecycleSession")
    private KieSession lifecycleSession;

    @Autowired
    @Qualifier("governanceSession")
    private KieSession governanceSession;

    @Autowired
    private RuleExecutionLogRepository executionLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public QualityScoreResult evaluateQualityScore(AssetEvaluation asset) {
        long startTime = System.currentTimeMillis();
        QualityScoreResult result = new QualityScoreResult();

        try {
            KieSession session = kieContainer.newKieSession("qualitySession");
            session.insert(asset);
            int rulesFired = session.fireAllRules();
            session.dispose();

            result.setAssetId(asset.getAssetId());
            result.setQualityScore(asset.getQualityScore());
            result.setQualityLevel(QualityScoreResult.getQualityLevel(asset.getQualityScore()));
            result.setQualityIssues(asset.getQualityIssues());
            result.setCompleteness(asset.getCompleteness());
            result.setAccuracy(asset.getAccuracy());
            result.setConsistency(asset.getConsistency());
            result.setTimeliness(asset.getTimeliness());
            result.setUniqueness(asset.getUniqueness());
            result.setTriggeredRules(asset.getTriggeredRules());
            result.setEvaluationSummary(asset.getEvaluationSummary());
            result.setEvaluationTime(LocalDateTime.now());

            logExecution("QUALITY_SCORE", "质量评分评估", "QUALITY",
                    asset.getAssetId(), asset, result, rulesFired, startTime, "SUCCESS", null);

            logger.info("质量评分完成: assetId={}, score={}, rulesFired={}",
                    asset.getAssetId(), result.getQualityScore(), rulesFired);

        } catch (Exception e) {
            logger.error("质量评分规则执行失败: assetId={}", asset.getAssetId(), e);
            logExecution("QUALITY_SCORE", "质量评分评估", "QUALITY",
                    asset.getAssetId(), asset, null, 0, startTime, "FAILED", e.getMessage());
            throw new RuntimeException("质量评分规则执行失败: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public ComplianceResult checkCompliance(AssetEvaluation asset) {
        long startTime = System.currentTimeMillis();
        ComplianceResult result = new ComplianceResult();

        try {
            KieSession session = kieContainer.newKieSession("complianceSession");
            session.insert(asset);
            int rulesFired = session.fireAllRules();
            session.dispose();

            result.setAssetId(asset.getAssetId());
            result.setComplianceStatus(asset.getComplianceStatus());
            result.setStandardCode(asset.getStandardCode());
            result.setViolationDetails(asset.getComplianceDetails());
            result.setTriggeredRules(asset.getTriggeredRules());
            result.setEvaluationSummary(asset.getEvaluationSummary());

            // 根据违规详情设置各维度
            Map<String, String> details = asset.getComplianceDetails();
            result.setNameCompliant(!"NON_COMPLIANT".equals(details.get("naming")));
            result.setTypeCompliant(asset.getAssetType() != null && !asset.getAssetType().isEmpty());

            logExecution("COMPLIANCE_CHECK", "合规检查", "COMPLIANCE",
                    asset.getAssetId(), asset, result, rulesFired, startTime, "SUCCESS", null);

            logger.info("合规检查完成: assetId={}, status={}", asset.getAssetId(), result.getComplianceStatus());

        } catch (Exception e) {
            logger.error("合规检查规则执行失败: assetId={}", asset.getAssetId(), e);
            logExecution("COMPLIANCE_CHECK", "合规检查", "COMPLIANCE",
                    asset.getAssetId(), asset, null, 0, startTime, "FAILED", e.getMessage());
            throw new RuntimeException("合规检查规则执行失败: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public ValueScoreResult evaluateValue(AssetEvaluation asset) {
        long startTime = System.currentTimeMillis();
        ValueScoreResult result = new ValueScoreResult();

        try {
            KieSession session = kieContainer.newKieSession("valueSession");
            session.insert(asset);
            int rulesFired = session.fireAllRules();
            session.dispose();

            result.setAssetId(asset.getAssetId());
            result.setValueScore(asset.getValueScore());
            result.setValueLevel(ValueScoreResult.getValueLevel(asset.getValueScore()));
            result.setQualityScore(asset.getQualityScore());
            result.setTriggeredRules(asset.getTriggeredRules());
            result.setEvaluationSummary(asset.getEvaluationSummary());
            result.setValueDrivers(new ArrayList<>(asset.getEvaluationTags()));

            logExecution("VALUE_EVALUATION", "价值评估", "VALUE",
                    asset.getAssetId(), asset, result, rulesFired, startTime, "SUCCESS", null);

            logger.info("价值评估完成: assetId={}, score={}", asset.getAssetId(), result.getValueScore());

        } catch (Exception e) {
            logger.error("价值评估规则执行失败: assetId={}", asset.getAssetId(), e);
            logExecution("VALUE_EVALUATION", "价值评估", "VALUE",
                    asset.getAssetId(), asset, null, 0, startTime, "FAILED", e.getMessage());
            throw new RuntimeException("价值评估规则执行失败: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public LifecycleResult evaluateLifecycle(AssetEvaluation asset) {
        long startTime = System.currentTimeMillis();
        LifecycleResult result = new LifecycleResult();

        try {
            KieSession session = kieContainer.newKieSession("lifecycleSession");
            session.insert(asset);
            int rulesFired = session.fireAllRules();
            session.dispose();

            result.setAssetId(asset.getAssetId());
            result.setRecommendedPhase(asset.getLifecyclePhase());
            result.setDailyAccessCount(asset.getDailyAccessCount());
            result.setAction("RETIRED".equals(asset.getLifecyclePhase()) ? "RETIRE" :
                    "FROZEN".equals(asset.getLifecyclePhase()) ? "FREEZE" :
                    "COLD".equals(asset.getLifecyclePhase()) ? "ARCHIVE" : "KEEP");
            result.setActionReason(asset.getEvaluationSummary());
            result.setTriggeredRules(asset.getTriggeredRules());
            result.setEvaluationSummary(asset.getEvaluationSummary());

            logExecution("LIFECYCLE_EVALUATION", "生命周期评估", "LIFECYCLE",
                    asset.getAssetId(), asset, result, rulesFired, startTime, "SUCCESS", null);

            logger.info("生命周期评估完成: assetId={}, phase={}", asset.getAssetId(), result.getRecommendedPhase());

        } catch (Exception e) {
            logger.error("生命周期评估规则执行失败: assetId={}", asset.getAssetId(), e);
            logExecution("LIFECYCLE_EVALUATION", "生命周期评估", "LIFECYCLE",
                    asset.getAssetId(), asset, null, 0, startTime, "FAILED", e.getMessage());
            throw new RuntimeException("生命周期评估规则执行失败: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public GovernanceRuleResult evaluateGovernance(AssetEvaluation asset) {
        long startTime = System.currentTimeMillis();
        GovernanceRuleResult result = new GovernanceRuleResult();

        try {
            KieSession session = kieContainer.newKieSession("governanceSession");
            session.insert(asset);
            int rulesFired = session.fireAllRules();
            session.dispose();

            result.setAssetId(asset.getAssetId());
            result.setNeedsGovernanceAction(asset.isNeedsGovernanceAction());
            result.setGovernanceActionType(asset.getGovernanceActionType());
            result.setGovernancePriority(asset.getGovernancePriority());
            result.setActionDescription(asset.getEvaluationSummary());
            result.setTriggeredRules(asset.getTriggeredRules());
            result.setEvaluationSummary(asset.getEvaluationSummary());
            result.setRecommendations(new ArrayList<>(asset.getEvaluationTags()));

            logExecution("GOVERNANCE_EVALUATION", "治理评估", "GOVERNANCE",
                    asset.getAssetId(), asset, result, rulesFired, startTime, "SUCCESS", null);

            logger.info("治理评估完成: assetId={}, needsAction={}, priority={}",
                    asset.getAssetId(), result.isNeedsGovernanceAction(), result.getGovernancePriority());

        } catch (Exception e) {
            logger.error("治理评估规则执行失败: assetId={}", asset.getAssetId(), e);
            logExecution("GOVERNANCE_EVALUATION", "治理评估", "GOVERNANCE",
                    asset.getAssetId(), asset, null, 0, startTime, "FAILED", e.getMessage());
            throw new RuntimeException("治理评估规则执行失败: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public Map<String, Object> evaluateAll(AssetEvaluation asset) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> allResults = new LinkedHashMap<>();

        try {
            // 依次执行所有规则集
            QualityScoreResult qualityResult = evaluateQualityScore(asset);
            allResults.put("quality", qualityResult);

            // 重置资产状态用于下一轮评估
            resetAssetForReEvaluation(asset);

            ComplianceResult complianceResult = checkCompliance(asset);
            allResults.put("compliance", complianceResult);

            resetAssetForReEvaluation(asset);

            ValueScoreResult valueResult = evaluateValue(asset);
            allResults.put("value", valueResult);

            resetAssetForReEvaluation(asset);

            LifecycleResult lifecycleResult = evaluateLifecycle(asset);
            allResults.put("lifecycle", lifecycleResult);

            resetAssetForReEvaluation(asset);

            GovernanceRuleResult governanceResult = evaluateGovernance(asset);
            allResults.put("governance", governanceResult);

            // 汇总
            Map<String, Object> summary = new HashMap<>();
            summary.put("assetId", asset.getAssetId());
            summary.put("totalExecutionTimeMs", System.currentTimeMillis() - startTime);
            summary.put("overallQualityScore", qualityResult.getQualityScore());
            summary.put("complianceStatus", complianceResult.getComplianceStatus());
            summary.put("valueLevel", valueResult.getValueLevel());
            summary.put("recommendedLifecycle", lifecycleResult.getRecommendedPhase());
            summary.put("needsGovernanceAction", governanceResult.isNeedsGovernanceAction());
            summary.put("governancePriority", governanceResult.getGovernancePriority());
            summary.put("evaluationTime", LocalDateTime.now());
            allResults.put("summary", summary);

            logger.info("综合评估完成: assetId={}, totalTime={}ms", asset.getAssetId(),
                    System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            logger.error("综合评估失败: assetId={}", asset.getAssetId(), e);
            throw new RuntimeException("综合评估失败: " + e.getMessage(), e);
        }

        return allResults;
    }

    @Override
    public void reloadRules() {
        KieServices kieServices = KieServices.Factory.get();
        droolsConfig.reloadRules(kieServices);
        logger.info("规则重载成功");
    }

    @Override
    public List<RuleExecutionLog> getExecutionLogs(String assetId) {
        if (assetId != null && !assetId.isEmpty()) {
            return executionLogRepository.findByAssetId(assetId);
        }
        return executionLogRepository.findAll();
    }

    @Override
    public Map<String, Object> testRule(String category, Map<String, Object> input) {
        AssetEvaluation asset = mapToAssetEvaluation(input);
        Map<String, Object> result = new HashMap<>();

        switch (category.toUpperCase()) {
            case "QUALITY":
                result.put("result", evaluateQualityScore(asset));
                break;
            case "COMPLIANCE":
                result.put("result", checkCompliance(asset));
                break;
            case "VALUE":
                result.put("result", evaluateValue(asset));
                break;
            case "LIFECYCLE":
                result.put("result", evaluateLifecycle(asset));
                break;
            case "GOVERNANCE":
                result.put("result", evaluateGovernance(asset));
                break;
            default:
                throw new IllegalArgumentException("不支持的规则分类: " + category);
        }

        return result;
    }

    /**
     * 重置资产评估状态，用于下一轮规则评估
     */
    private void resetAssetForReEvaluation(AssetEvaluation asset) {
        asset.setQualityScore(0);
        asset.setValueScore(0);
        asset.setComplianceStatus("UNKNOWN");
        asset.setLifecyclePhase(null);
        asset.setEvaluationSummary(null);
        asset.setEvaluationTags(new ArrayList<>());
        asset.setTriggeredRules(new ArrayList<>());
        asset.setNeedsGovernanceAction(false);
        asset.setGovernanceActionType(null);
        asset.setGovernancePriority(null);
        asset.getComplianceDetails().clear();
    }

    /**
     * 将Map转换为AssetEvaluation对象
     */
    private AssetEvaluation mapToAssetEvaluation(Map<String, Object> input) {
        AssetEvaluation asset = new AssetEvaluation();
        if (input.containsKey("assetId")) asset.setAssetId((String) input.get("assetId"));
        if (input.containsKey("assetName")) asset.setAssetName((String) input.get("assetName"));
        if (input.containsKey("assetType")) asset.setAssetType((String) input.get("assetType"));
        if (input.containsKey("qualityIssues")) asset.setQualityIssues(((Number) input.get("qualityIssues")).intValue());
        if (input.containsKey("completeness")) asset.setCompleteness(((Number) input.get("completeness")).doubleValue());
        if (input.containsKey("accuracy")) asset.setAccuracy(((Number) input.get("accuracy")).doubleValue());
        if (input.containsKey("consistency")) asset.setConsistency(((Number) input.get("consistency")).doubleValue());
        if (input.containsKey("timeliness")) asset.setTimeliness(((Number) input.get("timeliness")).doubleValue());
        if (input.containsKey("uniqueness")) asset.setUniqueness(((Number) input.get("uniqueness")).doubleValue());
        if (input.containsKey("dailyAccessCount")) asset.setDailyAccessCount(((Number) input.get("dailyAccessCount")).doubleValue());
        if (input.containsKey("dataSizeMb")) asset.setDataSizeMb(((Number) input.get("dataSizeMb")).doubleValue());
        if (input.containsKey("standardCode")) asset.setStandardCode((String) input.get("standardCode"));
        if (input.containsKey("owner")) asset.setOwner((String) input.get("owner"));
        if (input.containsKey("businessDomain")) asset.setBusinessDomain((String) input.get("businessDomain"));
        if (input.containsKey("sensitivityLevel")) asset.setSensitivityLevel((String) input.get("sensitivityLevel"));
        return asset;
    }

    /**
     * 记录规则执行日志
     */
    private void logExecution(String ruleCode, String ruleName, String category,
                              String assetId, Object input, Object output,
                              int rulesFired, long startTime, String status, String errorMessage) {
        try {
            RuleExecutionLog log = new RuleExecutionLog();
            log.setRuleCode(ruleCode);
            log.setRuleName(ruleName);
            log.setCategory(category);
            log.setAssetId(assetId);
            log.setInputData(objectMapper.writeValueAsString(input));
            log.setOutputResult(output != null ? objectMapper.writeValueAsString(output) : null);
            log.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            log.setStatus(status);
            log.setErrorMessage(errorMessage);
            log.setTriggeredRules(String.valueOf(rulesFired));

            executionLogRepository.save(log);
        } catch (Exception e) {
            logger.warn("记录规则执行日志失败: {}", e.getMessage());
        }
    }
}
