package com.enterprise.dataplatform.quality.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 质量报告实体
 * 存储定期生成的质量报告
 */
@Entity
@Table(name = "quality_report", indexes = {
    @Index(name = "idx_report_code", columnList = "reportCode"),
    @Index(name = "idx_report_type", columnList = "reportType"),
    @Index(name = "idx_report_time", columnList = "reportTime")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 报告编码
     */
    @Column(name = "report_code", nullable = false, unique = true, length = 64)
    private String reportCode;

    /**
     * 报告名称
     */
    @Column(name = "report_name", nullable = false, length = 256)
    private String reportName;

    /**
     * 报告类型：DAILY、WEEKLY、MONTHLY、QUARTERLY、CUSTOM
     */
    @Column(name = "report_type", nullable = false, length = 32)
    private String reportType;

    /**
     * 报告周期开始时间
     */
    @Column(name = "period_start_time", nullable = false)
    private LocalDateTime periodStartTime;

    /**
     * 报告周期结束时间
     */
    @Column(name = "period_end_time", nullable = false)
    private LocalDateTime periodEndTime;

    /**
     * 报告生成时间
     */
    @Column(name = "report_time", nullable = false)
    private LocalDateTime reportTime;

    /**
     * 报告范围（JSON格式）
     */
    @Column(name = "report_scope", columnDefinition = "TEXT")
    private String reportScope;

    /**
     * 汇总统计（JSON格式）
     */
    @Column(name = "summary_statistics", columnDefinition = "TEXT")
    private String summaryStatistics;

    /**
     * 详细数据（JSON格式）
     */
    @Column(name = "detailed_data", columnDefinition = "TEXT")
    private String detailedData;

    /**
     * 趋势分析（JSON格式）
     */
    @Column(name = "trend_analysis", columnDefinition = "TEXT")
    private String trendAnalysis;

    /**
     * 问题汇总（JSON格式）
     */
    @Column(name = "issue_summary", columnDefinition = "TEXT")
    private String issueSummary;

    /**
     * 改进建议（JSON格式）
     */
    @Column(name = "improvement_suggestions", columnDefinition = "TEXT")
    private String improvementSuggestions;

    /**
     * 总体质量得分
     */
    @Column(name = "overall_score")
    private Double overallScore;

    /**
     * 报告格式：HTML、PDF、JSON
     */
    @Column(name = "report_format", length = 16)
    private String reportFormat;

    /**
     * 报告文件路径
     */
    @Column(name = "file_path", length = 512)
    private String filePath;

    /**
     * 报告状态：GENERATING、GENERATED、FAILED
     */
    @Column(name = "report_status", nullable = false, length = 32)
    private String reportStatus;

    /**
     * 生成人
     */
    @Column(name = "generator", length = 64)
    private String generator;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 审核状态
     */
    @Column(name = "review_status", length = 32)
    private String reviewStatus;

    /**
     * 审核人
     */
    @Column(name = "reviewer", length = 64)
    private String reviewer;

    /**
     * 审核时间
     */
    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    /**
     * 是否已发布
     */
    @Column(name = "published")
    private Boolean published;

    /**
     * 发布时间
     */
    @Column(name = "publish_time")
    private LocalDateTime publishTime;
}
