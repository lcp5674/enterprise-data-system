package com.enterprise.dataplatform.quality.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 质量评分卡实体
 * 用于综合评估数据资产的质量
 */
@Entity
@Table(name = "quality_scorecard", indexes = {
    @Index(name = "idx_scorecard_asset", columnList = "asset_id"),
    @Index(name = "idx_scorecard_time", columnList = "scoreTime")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityScorecard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 评分卡编码
     */
    @Column(name = "scorecard_code", nullable = false, unique = true, length = 64)
    private String scorecardCode;

    /**
     * 评分卡名称
     */
    @Column(name = "scorecard_name", nullable = false, length = 128)
    private String scorecardName;

    /**
     * 数据资产ID
     */
    @Column(name = "asset_id", nullable = false, length = 64)
    private String assetId;

    /**
     * 数据资产名称
     */
    @Column(name = "asset_name", length = 256)
    private String assetName;

    /**
     * 资产类型
     */
    @Column(name = "asset_type", length = 32)
    private String assetType;

    /**
     * 评估时间
     */
    @Column(name = "score_time", nullable = false)
    private LocalDateTime scoreTime;

    /**
     * 综合质量得分
     */
    @Column(name = "overall_score")
    private Double overallScore;

    /**
     * 完整性得分
     */
    @Column(name = "completeness_score")
    private Double completenessScore;

    /**
     * 准确性得分
     */
    @Column(name = "accuracy_score")
    private Double accuracyScore;

    /**
     * 一致性得分
     */
    @Column(name = "consistency_score")
    private Double consistencyScore;

    /**
     * 时效性得分
     */
    @Column(name = "timeliness_score")
    private Double timelinessScore;

    /**
     * 唯一性得分
     */
    @Column(name = "uniqueness_score")
    private Double uniquenessScore;

    /**
     * 有效性得分
     */
    @Column(name = "validity_score")
    private Double validityScore;

    /**
     * 评估规则总数
     */
    @Column(name = "total_rules")
    private Integer totalRules;

    /**
     * 通过的规则数
     */
    @Column(name = "passed_rules")
    private Integer passedRules;

    /**
     * 失败的规则数
     */
    @Column(name = "failed_rules")
    private Integer failedRules;

    /**
     * 告警的规则数
     */
    @Column(name = "warned_rules")
    private Integer warnedRules;

    /**
     * 评分等级：A、B、C、D、E
     */
    @Column(name = "score_grade", length = 1)
    private String scoreGrade;

    /**
     * 评分状态：PENDING、COMPLETED、FAILED
     */
    @Column(name = "score_status", nullable = false, length = 32)
    private String scoreStatus;

    /**
     * 详细评估结果（JSON格式）
     */
    @Column(name = "evaluation_details", columnDefinition = "TEXT")
    private String evaluationDetails;

    /**
     * 改进建议（JSON格式）
     */
    @Column(name = "improvement_suggestions", columnDefinition = "TEXT")
    private String improvementSuggestions;

    /**
     * 评估人
     */
    @Column(name = "evaluator", length = 64)
    private String evaluator;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
