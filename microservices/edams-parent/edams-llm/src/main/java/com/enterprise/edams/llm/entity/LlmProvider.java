package com.enterprise.edams.llm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LLM提供商实体
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("llm_provider")
public class LlmProvider extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提供商ID
     */
    private String providerId;

    /**
     * 提供商名称
     */
    private String name;

    /**
     * 提供商代码: OPENAI, ANTHROPIC, QIANWEN, ERNIE
     */
    private String code;

    /**
     * 提供商类型
     */
    private String type;

    /**
     * API地址
     */
    private String baseUrl;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 密钥标识(用于展示)
     */
    private String keyAlias;

    /**
     * 优先级(数字越小优先级越高)
     */
    private Integer priority;

    /**
     * 状态: ENABLED-启用, DISABLED-禁用
     */
    private String status;

    /**
     * 支持的模型列表 (JSON)
     */
    private String models;

    /**
     * 默认模型
     */
    private String defaultModel;

    /**
     * 单次最大Token数
     */
    private Integer maxTokens;

    /**
     * 默认温度
     */
    private BigDecimal defaultTemperature;

    /**
     * 认证类型: API_KEY, OAUTH2, BASIC
     */
    private String authType;

    /**
     * 额外配置 (JSON)
     */
    private String extraConfig;

    /**
     * 描述
     */
    private String description;

    /**
     * 所属租户ID
     */
    private String tenantId;

    /**
     * 创建人
     */
    private String creator;
}
