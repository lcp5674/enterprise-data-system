package com.enterprise.edams.incentive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

/**
 * 奖励规则实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("reward_rule")
public class RewardRule extends BaseEntity {
    
    /**
     * 规则编码
     */
    @TableField("rule_code")
    private String ruleCode;
    
    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;
    
    /**
     * 业务类型
     */
    @TableField("biz_type")
    private BizType bizType;
    
    /**
     * 奖励积分
     */
    @TableField("reward_points")
    private BigDecimal rewardPoints;
    
    /**
     * 单位(次/个)
     */
    @TableField("unit")
    private String unit;
    
    /**
     * 每日上限
     */
    @TableField("daily_limit")
    private BigDecimal dailyLimit;
    
    /**
     * 总上限
     */
    @TableField("total_limit")
    private BigDecimal totalLimit;
    
    /**
     * 状态
     */
    @TableField("status")
    private RuleStatus status;
    
    /**
     * 描述
     */
    @TableField("description")
    private String description;
}

enum RuleStatus {
    ACTIVE("生效中"),
    INACTIVE("已停用"),
    DRAFT("草稿");
    
    private final String description;
    RuleStatus(String description) { this.description = description; }
}
