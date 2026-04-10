package com.enterprise.edams.incentive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分交易记录实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("point_transaction")
public class PointTransaction extends BaseEntity {
    
    /**
     * 交易编号
     */
    @TableField("transaction_no")
    private String transactionNo;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 交易类型
     */
    @TableField("type")
    private TransactionType type;
    
    /**
     * 积分数量
     */
    @TableField("points")
    private BigDecimal points;
    
    /**
     * 积分余额（交易后）
     */
    @TableField("balance_after")
    private BigDecimal balanceAfter;
    
    /**
     * 业务类型
     */
    @TableField("biz_type")
    private BizType bizType;
    
    /**
     * 关联业务ID
     */
    @TableField("biz_id")
    private String bizId;
    
    /**
     * 描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 状态
     */
    @TableField("status")
    private TransactionStatus status;
}

enum TransactionType {
    EARN("获得"),
    SPEND("消耗"),
    ADJUST("调整"),
    EXPIRE("过期"),
    RETURN("退回");
    
    private final String description;
    TransactionType(String description) { this.description = description; }
}

enum BizType {
    DATA_QUALITY("数据质量"),
    LINEAGE_COMPLETE("血缘完整"),
    METADATA_COMPLETE("元数据完整"),
    TASK_COMPLETE("任务完成"),
    REVIEW_PASS("审核通过"),
    CONTRIBUTION("知识贡献"),
    SIGN_IN("签到"),
    EXCHANGE("兑换");
    
    private final String description;
    BizType(String description) { this.description = description; }
}

enum TransactionStatus {
    PENDING("待处理"),
    COMPLETED("已完成"),
    FAILED("失败"),
    CANCELLED("已取消");
    
    private final String description;
    TransactionStatus(String description) { this.description = description; }
}
