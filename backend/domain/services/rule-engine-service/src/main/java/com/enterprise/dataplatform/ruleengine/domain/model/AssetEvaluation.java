package com.enterprise.dataplatform.ruleengine.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据资产评估基础对象（Drools Fact）
 * 所有规则引擎评估操作的基础事实对象
 */
@Data
public class AssetEvaluation {

    /** 资产ID */
    private String assetId;

    /** 资产名称 */
    private String assetName;

    /** 资产类型 */
    private String assetType;

    /** 质量问题数量 */
    private int qualityIssues;

    /** 数据完整度 (0.0 ~ 1.0) */
    private double completeness;

    /** 数据准确度 (0.0 ~ 1.0) */
    private double accuracy;

    /** 数据一致性 (0.0 ~ 1.0) */
    private double consistency;

    /** 数据及时性 (0.0 ~ 1.0) */
    private double timeliness;

    /** 数据唯一性 (0.0 ~ 1.0) */
    private double uniqueness;

    /** 访问频率（每日） */
    private double dailyAccessCount;

    /** 上次访问时间 */
    private LocalDateTime lastAccessTime;

    /** 数据量大小（MB） */
    private double dataSizeMb;

    /** 数据标准编码 */
    private String standardCode;

    /** 是否符合标准 */
    private boolean standardCompliant;

    /** 标准检查详情 */
    private Map<String, String> complianceDetails = new HashMap<>();

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 资产负责人 */
    private String owner;

    /** 业务域 */
    private String businessDomain;

    /** 敏感级别 */
    private String sensitivityLevel;

    // ---- 评估输出字段 ----

    /** 质量评分 (0 ~ 100) */
    private double qualityScore;

    /** 价值评分 (0 ~ 100) */
    private double valueScore;

    /** 合规状态 */
    private String complianceStatus;

    /** 建议的生命周期阶段 */
    private String lifecyclePhase;

    /** 评估标签 */
    private List<String> evaluationTags = new ArrayList<>();

    /** 触发的规则列表 */
    private List<String> triggeredRules = new ArrayList<>();

    /** 评估时间 */
    private LocalDateTime evaluationTime;

    /** 评估摘要 */
    private String evaluationSummary;

    /** 是否需要治理动作 */
    private boolean needsGovernanceAction;

    /** 治理动作类型 */
    private String governanceActionType;

    /** 治理优先级 */
    private String governancePriority;

    public AssetEvaluation() {
        this.evaluationTime = LocalDateTime.now();
        this.qualityScore = 0;
        this.valueScore = 0;
        this.complianceStatus = "UNKNOWN";
    }

    /**
     * 计算综合质量维度得分
     */
    public double getOverallQualityDimension() {
        return (completeness + accuracy + consistency + timeliness + uniqueness) / 5.0;
    }
}
