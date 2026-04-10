package com.enterprise.edams.llm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LLM模型实体
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("llm_model")
public class LlmModel extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型代码
     */
    private String code;

    /**
     * 提供商ID
     */
    private String providerId;

    /**
     * 提供商代码
     */
    private String providerCode;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 模型类型: CHAT, COMPLETION, EMBEDDING
     */
    private String modelType;

    /**
     * 最大Token数
     */
    private Integer maxTokens;

    /**
     * 输入价格(元/千Token)
     */
    private BigDecimal inputPrice;

    /**
     * 输出价格(元/千Token)
     */
    private BigDecimal outputPrice;

    /**
     * 上下文窗口大小
     */
    private Integer contextWindow;

    /**
     * 支持的功能 (JSON)
     */
    private String capabilities;

    /**
     * 状态: ENABLED, DISABLED
     */
    private String status;

    /**
     * 是否支持流式输出
     */
    private Boolean streamingSupported;

    /**
     * 是否支持函数调用
     */
    private Boolean functionCallSupported;

    /**
     * 是否支持视觉
     */
    private Boolean visionSupported;

    /**
     * 所属租户ID
     */
    private String tenantId;

    /**
     * 排序
     */
    private Integer sortOrder;
}
