package com.enterprise.edams.llm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LLM使用记录实体
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("llm_usage_record")
public class LlmUsageRecord extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    private String recordId;

    /**
     * 会话ID
     */
    private String sessionId;

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
     * 提供商名称
     */
    private String providerName;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 输入Token数
     */
    private Integer inputTokens;

    /**
     * 输出Token数
     */
    private Integer outputTokens;

    /**
     * 总Token数
     */
    private Integer totalTokens;

    /**
     * 输入费用(元)
     */
    private BigDecimal inputCost;

    /**
     * 输出费用(元)
     */
    private BigDecimal outputCost;

    /**
     * 总费用(元)
     */
    private BigDecimal totalCost;

    /**
     * 延迟(ms)
     */
    private Long latency;

    /**
     * 请求类型: CHAT, COMPLETION, EMBEDDING
     */
    private String requestType;

    /**
     * 提示词
     */
    private String prompt;

    /**
     * 响应
     */
    private String response;

    /**
     * 状态: SUCCESS, FAILED, PARTIAL
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
