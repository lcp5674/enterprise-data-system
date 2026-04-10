package com.enterprise.edams.value.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ROI追踪实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("roi_tracking")
public class RoiTracking extends BaseEntity {
    
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
     * 投资金额
     */
    @TableField("investment")
    private BigDecimal investment;
    
    /**
     * 回报金额
     */
    @TableField("return_amount")
    private BigDecimal returnAmount;
    
    /**
     * ROI百分比
     */
    @TableField("roi_percentage")
    private BigDecimal roiPercentage;
    
    /**
     * 追踪周期类型
     */
    @TableField("period_type")
    private PeriodType periodType;
    
    /**
     * 周期开始日期
     */
    @TableField("period_start")
    private LocalDateTime periodStart;
    
    /**
     * 周期结束日期
     */
    @TableField("period_end")
    private LocalDateTime periodEnd;
    
    /**
     * 直接收益
     */
    @TableField("direct_benefit")
    private BigDecimal directBenefit;
    
    /**
     * 间接收益
     */
    @TableField("indirect_benefit")
    private BigDecimal indirectBenefit;
    
    /**
     * 成本节省
     */
    @TableField("cost_saving")
    private BigDecimal costSaving;
    
    /**
     * 收益明细(JSON)
     */
    @TableField("benefit_detail")
    private String benefitDetail;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}

/**
 * 周期类型枚举
 */
enum PeriodType {
    DAILY("日"),
    WEEKLY("周"),
    MONTHLY("月"),
    QUARTERLY("季度"),
    YEARLY("年");
    
    private final String description;
    
    PeriodType(String description) {
        this.description = description;
    }
}
