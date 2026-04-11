package com.enterprise.dataplatform.ruleengine.controller;

import com.enterprise.dataplatform.ruleengine.domain.model.*;
import com.enterprise.dataplatform.ruleengine.dto.*;
import com.enterprise.dataplatform.ruleengine.service.RuleEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 规则引擎评估API控制器
 * 提供规则评估相关的REST接口
 */
@RestController
@RequestMapping("/api/rules/evaluate")
@Tag(name = "规则评估", description = "规则引擎评估接口")
public class RuleEvaluationController {

    private static final Logger logger = LoggerFactory.getLogger(RuleEvaluationController.class);

    @Autowired
    private RuleEngineService ruleEngineService;

    @PostMapping("/quality")
    @Operation(summary = "质量评分评估", description = "执行数据资产质量评分规则")
    public ApiResponse<QualityScoreResult> evaluateQuality(
            @Valid @RequestBody AssetEvaluationRequest request) {
        try {
            AssetEvaluation asset = mapToAsset(request);
            QualityScoreResult result = ruleEngineService.evaluateQualityScore(asset);
            return ApiResponse.success("质量评分评估完成", result);
        } catch (Exception e) {
            logger.error("质量评分评估失败", e);
            return ApiResponse.error("质量评分评估失败: " + e.getMessage());
        }
    }

    @PostMapping("/compliance")
    @Operation(summary = "合规检查", description = "执行数据标准合规检查规则")
    public ApiResponse<ComplianceResult> checkCompliance(
            @Valid @RequestBody AssetEvaluationRequest request) {
        try {
            AssetEvaluation asset = mapToAsset(request);
            ComplianceResult result = ruleEngineService.checkCompliance(asset);
            return ApiResponse.success("合规检查完成", result);
        } catch (Exception e) {
            logger.error("合规检查失败", e);
            return ApiResponse.error("合规检查失败: " + e.getMessage());
        }
    }

    @PostMapping("/value")
    @Operation(summary = "价值评估", description = "执行数据资产价值评估规则")
    public ApiResponse<ValueScoreResult> evaluateValue(
            @Valid @RequestBody AssetEvaluationRequest request) {
        try {
            AssetEvaluation asset = mapToAsset(request);
            ValueScoreResult result = ruleEngineService.evaluateValue(asset);
            return ApiResponse.success("价值评估完成", result);
        } catch (Exception e) {
            logger.error("价值评估失败", e);
            return ApiResponse.error("价值评估失败: " + e.getMessage());
        }
    }

    @PostMapping("/lifecycle")
    @Operation(summary = "生命周期评估", description = "执行数据生命周期管理规则")
    public ApiResponse<LifecycleResult> evaluateLifecycle(
            @Valid @RequestBody AssetEvaluationRequest request) {
        try {
            AssetEvaluation asset = mapToAsset(request);
            LifecycleResult result = ruleEngineService.evaluateLifecycle(asset);
            return ApiResponse.success("生命周期评估完成", result);
        } catch (Exception e) {
            logger.error("生命周期评估失败", e);
            return ApiResponse.error("生命周期评估失败: " + e.getMessage());
        }
    }

    @PostMapping("/governance")
    @Operation(summary = "治理评估", description = "执行数据治理规则评估")
    public ApiResponse<GovernanceRuleResult> evaluateGovernance(
            @Valid @RequestBody AssetEvaluationRequest request) {
        try {
            AssetEvaluation asset = mapToAsset(request);
            GovernanceRuleResult result = ruleEngineService.evaluateGovernance(asset);
            return ApiResponse.success("治理评估完成", result);
        } catch (Exception e) {
            logger.error("治理评估失败", e);
            return ApiResponse.error("治理评估失败: " + e.getMessage());
        }
    }

    @PostMapping("/all")
    @Operation(summary = "综合评估", description = "执行所有规则集的综合评估")
    public ApiResponse<Map<String, Object>> evaluateAll(
            @Valid @RequestBody AssetEvaluationRequest request) {
        try {
            AssetEvaluation asset = mapToAsset(request);
            Map<String, Object> results = ruleEngineService.evaluateAll(asset);
            return ApiResponse.success("综合评估完成", results);
        } catch (Exception e) {
            logger.error("综合评估失败", e);
            return ApiResponse.error("综合评估失败: " + e.getMessage());
        }
    }

    @PostMapping("/test")
    @Operation(summary = "规则测试", description = "使用自定义输入测试指定规则分类")
    public ApiResponse<Map<String, Object>> testRule(
            @Valid @RequestBody RuleTestRequest request) {
        try {
            Map<String, Object> result = ruleEngineService.testRule(
                    request.getCategory(), request.getInput());
            return ApiResponse.success("规则测试完成", result);
        } catch (Exception e) {
            logger.error("规则测试失败", e);
            return ApiResponse.error("规则测试失败: " + e.getMessage());
        }
    }

    /**
     * 将请求DTO转换为AssetEvaluation对象
     */
    private AssetEvaluation mapToAsset(AssetEvaluationRequest request) {
        AssetEvaluation asset = new AssetEvaluation();
        asset.setAssetId(request.getAssetId());
        asset.setAssetName(request.getAssetName());
        asset.setAssetType(request.getAssetType());
        asset.setQualityIssues(request.getQualityIssues());
        asset.setCompleteness(request.getCompleteness());
        asset.setAccuracy(request.getAccuracy());
        asset.setConsistency(request.getConsistency());
        asset.setTimeliness(request.getTimeliness());
        asset.setUniqueness(request.getUniqueness());
        asset.setDailyAccessCount(request.getDailyAccessCount());
        asset.setDataSizeMb(request.getDataSizeMb());
        asset.setStandardCode(request.getStandardCode());
        asset.setOwner(request.getOwner());
        asset.setBusinessDomain(request.getBusinessDomain());
        asset.setSensitivityLevel(request.getSensitivityLevel());

        if (request.getCreateTime() != null) {
            asset.setCreateTime(LocalDateTime.parse(request.getCreateTime()));
        }
        if (request.getLastAccessTime() != null) {
            asset.setLastAccessTime(LocalDateTime.parse(request.getLastAccessTime()));
        } else {
            asset.setLastAccessTime(LocalDateTime.now());
        }
        if (asset.getCreateTime() == null) {
            asset.setCreateTime(LocalDateTime.now());
        }

        return asset;
    }
}
