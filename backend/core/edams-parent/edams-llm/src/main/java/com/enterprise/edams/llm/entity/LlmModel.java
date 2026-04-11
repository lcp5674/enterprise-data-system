package com.enterprise.edams.llm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 大模型实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("llm_model")
public class LlmModel extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模型代码 (如: gpt-4, chatglm-pro)
     */
    private String modelCode;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 提供商: OPENAI, ANTHROPIC, GOOGLE, AZURE, CUSTOM
     */
    private String provider;

    /**
     * 提供商名称
     */
    private String providerName;

    /**
     * 模型类型: CHAT, EMBEDDING, IMAGE, AUDIO
     */
    private String modelType;

    /**
     * 模型描述
     */
    private String description;

    /**
     * API版本
     */
    private String apiVersion;

    /**
     * API端点
     */
    private String apiEndpoint;

    /**
     * 输入价格 (元/1000 tokens)
     */
    private BigDecimal inputPrice;

    /**
     * 输出价格 (元/1000 tokens)
     */
    private BigDecimal outputPrice;

    /**
     * 最大上下文长度
     */
    private Integer maxContextLength;

    /**
     * 最大输出长度
     */
    private Integer maxOutputLength;

    /**
     * 支持的功能 (JSON数组)
     */
    private String capabilities;

    /**
     * 配置参数 (JSON)
     */
    private String configParams;

    /**
     * 请求限制 (次/分钟)
     */
    private Integer requestLimit;

    /**
     * 并发限制
     */
    private Integer concurrentLimit;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 优先级 (1-100, 越高越优先)
     */
    private Integer priority;

    /**
     * 状态: ACTIVE-活跃, INACTIVE-停用, MAINTENANCE-维护中
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
