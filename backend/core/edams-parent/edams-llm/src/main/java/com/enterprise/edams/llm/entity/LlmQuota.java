package com.enterprise.edams.llm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 大模型配额实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("llm_quota")
public class LlmQuota extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配额ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 用户ID (NULL表示租户级别配额)
     */
    private Long userId;

    /**
     * 配额类型: DAILY-每日, MONTHLY-每月, TOTAL-总量
     */
    private String quotaType;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 模型代码
     */
    private String modelCode;

    /**
     * 配额上限 (tokens)
     */
    private Long quotaLimit;

    /**
     * 已使用量 (tokens)
     */
    private Long quotaUsed;

    /**
     * 配额上限 (金额，元)
     */
    private BigDecimal costLimit;

    /**
     * 已消费金额
     */
    private BigDecimal costUsed;

    /**
     * 请求次数上限
     */
    private Integer requestLimit;

    /**
     * 已请求次数
     */
    private Integer requestUsed;

    /**
     * 周期开始时间
     */
    private LocalDateTime periodStart;

    /**
     * 周期结束时间
     */
    private LocalDateTime periodEnd;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 告警阈值 (百分比)
     */
    private Integer alertThreshold;

    /**
     * 状态: ACTIVE-有效, EXPIRED-过期, EXHAUSTED-耗尽
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
