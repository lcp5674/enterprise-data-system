package com.enterprise.edams.incentive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

/**
 * 用户积分实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("user_points")
public class UserPoints extends BaseEntity {
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 用户名
     */
    @TableField("user_name")
    private String userName;
    
    /**
     * 积分余额
     */
    @TableField("balance")
    private BigDecimal balance;
    
    /**
     * 历史累计积分
     */
    @TableField("total_earned")
    private BigDecimal totalEarned;
    
    /**
     * 历史消耗积分
     */
    @TableField("total_spent")
    private BigDecimal totalSpent;
    
    /**
     * 等级
     */
    @TableField("level")
    private Integer level;
    
    /**
     * 经验值
     */
    @TableField("experience")
    private BigDecimal experience;
    
    /**
     * 头衔
     */
    @TableField("title")
    private String title;
    
    /**
     * 连续签到天数
     */
    @TableField("consecutive_days")
    private Integer consecutiveDays;
    
    /**
     * 最后签到时间
     */
    @TableField("last_sign_in")
    private java.time.LocalDateTime lastSignIn;
}
