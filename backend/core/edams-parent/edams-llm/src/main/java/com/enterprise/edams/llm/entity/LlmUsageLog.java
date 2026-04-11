package com.enterprise.edams.llm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 大模型使用日志实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("llm_usage_log")
public class LlmUsageLog extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 追踪ID
     */
    private String traceId;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

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
     * 请求类型: CHAT, COMPLETION, EMBEDDING
     */
    private String requestType;

    /**
     * 输入tokens
     */
    private Integer inputTokens;

    /**
     * 输出tokens
     */
    private Integer outputTokens;

    /**
     * 总tokens
     */
    private Integer totalTokens;

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
     * 延迟(ms)
     */
    private Long latencyMs;

    /**
     * 状态: SUCCESS, FAILED, TIMEOUT
     */
    private String status;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 应用模块
     */
    private String module;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 请求时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime requestTime;
}
