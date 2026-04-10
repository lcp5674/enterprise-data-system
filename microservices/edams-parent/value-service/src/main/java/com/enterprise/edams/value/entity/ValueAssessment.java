package com.enterprise.edams.value.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 数据价值评估实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("value_assessment")
public class ValueAssessment extends BaseEntity {
    
    /**
     * 资产ID
     */
    @TableField("asset_id")
    private Long assetId;
    
    /**
     * 资产名称
     */
    @TableField("asset_name")
    private String assetName;
    
    /**
     * 资产类型
     */
    @TableField("asset_type")
    private String assetType;
    
    /**
     * 综合评分 (0-100)
     */
    @TableField("overall_score")
    private BigDecimal overallScore;
    
    /**
     * 业务价值评分
     */
    @TableField("business_score")
    private BigDecimal businessScore;
    
    /**
     * 技术价值评分
     */
    @TableField("technical_score")
    private BigDecimal technicalScore;
    
    /**
     * 经济价值评分
     */
    @TableField("economic_score")
    private BigDecimal economicScore;
    
    /**
     * 使用价值评分
     */
    @TableField("usage_score")
    private BigDecimal usageScore;
    
    /**
     * 稀缺性评分
     */
    @TableField("scarcity_score")
    private BigDecimal scarcityScore;
    
    /**
     * 评估日期
     */
    @TableField("assessment_date")
    private LocalDateTime assessmentDate;
    
    /**
     * 评估方法
     */
    @TableField("assessment_method")
    private String assessmentMethod;
    
    /**
     * 评估详情(JSON)
     */
    @TableField("assessment_detail")
    private String assessmentDetail;
    
    /**
     * 评估状态
     */
    @TableField("status")
    private AssessmentStatus status;
}

/**
 * 评估状态枚举
 */
enum AssessmentStatus {
    PENDING("待评估"),
    IN_PROGRESS("评估中"),
    COMPLETED("已完成"),
    FAILED("评估失败");
    
    private final String description;
    
    AssessmentStatus(String description) {
        this.description = description;
    }
}
