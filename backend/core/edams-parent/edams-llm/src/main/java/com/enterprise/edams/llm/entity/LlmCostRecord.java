package com.enterprise.edams.llm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 大模型成本记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("llm_cost_record")
public class LlmCostRecord extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 模型代码
     */
    private String modelCode;

    /**
     * 提供商
     */
    private String provider;

    /**
     * 成本类型: DAILY, MONTHLY
     */
    private String costType;

    /**
     * 统计日期
     */
    private LocalDateTime statDate;

    /**
     * 输入tokens
     */
    private Long inputTokens;

    /**
     * 输出tokens
     */
    private Long outputTokens;

    /**
     * 总tokens
     */
    private Long totalTokens;

    /**
     * 输入费用
     */
    private BigDecimal inputCost;

    /**
     * 输出费用
     */
    private BigDecimal outputCost;

    /**
     * 总费用
     */
    private BigDecimal totalCost;

    /**
     * 请求次数
     */
    private Long requestCount;

    /**
     * 成功次数
     */
    private Long successCount;

    /**
     * 失败次数
     */
    private Long failedCount;

    /**
     * 平均延迟(ms)
     */
    private Double avgLatency;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
