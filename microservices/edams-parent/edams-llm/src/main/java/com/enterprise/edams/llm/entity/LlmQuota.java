package com.enterprise.edams.llm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LLM配额实体
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("llm_quota")
public class LlmQuota extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配额ID
     */
    private String quotaId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 提供商ID
     */
    private String providerId;

    /**
     * 配额类型: DAILY, MONTHLY, TOTAL
     */
    private String quotaType;

    /**
     * Token配额上限
     */
    private Long tokenLimit;

    /**
     * 已使用Token数
     */
    private Long tokenUsed;

    /**
     * 请求次数配额上限
     */
    private Long requestLimit;

    /**
     * 已使用请求次数
     */
    private Long requestUsed;

    /**
     * 配额重置时间
     */
    private LocalDateTime resetTime;

    /**
     * 状态: ACTIVE-激活, EXCEEDED-超限, FROZEN-冻结
     */
    private String status;

    /**
     * 费用配额(元)
     */
    private BigDecimal costLimit;

    /**
     * 已消费费用(元)
     */
    private BigDecimal costUsed;

    /**
     * 创建人
     */
    private String creator;
}
