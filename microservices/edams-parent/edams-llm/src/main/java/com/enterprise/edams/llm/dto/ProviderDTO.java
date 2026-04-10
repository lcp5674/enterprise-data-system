package com.enterprise.edams.llm.dto;

import lombok.Data;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 提供商DTO
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Data
@Builder
public class ProviderDTO implements Serializable {

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
     * 提供商代码
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
     * 状态
     */
    private String status;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 支持的模型
     */
    private List<ModelDTO> models;

    /**
     * 默认模型
     */
    private String defaultModel;

    /**
     * 描述
     */
    private String description;

    /**
     * 模型DTO
     */
    @Data
    @Builder
    public static class ModelDTO implements Serializable {
        private String modelId;
        private String code;
        private String name;
        private String displayName;
        private String modelType;
        private Integer maxTokens;
        private BigDecimal inputPrice;
        private BigDecimal outputPrice;
        private Boolean streamingSupported;
        private Boolean functionCallSupported;
        private Boolean visionSupported;
        private String status;
    }
}
