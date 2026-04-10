package com.enterprise.edams.llm.dto;

import lombok.Data;
import lombok.Builder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 聊天响应DTO
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Data
@Builder
public class ChatResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 响应内容
     */
    private String content;

    /**
     * 完成原因
     */
    private String finishReason;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 提供商
     */
    private String provider;

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
    private Long latency;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 工具调用列表
     */
    private List<ToolCall> toolCalls;

    /**
     * 引用列表
     */
    private List<Citation> citations;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 工具调用
     */
    @Data
    @Builder
    public static class ToolCall implements Serializable {
        private String id;
        private String type;
        private String functionName;
        private String arguments;
        private String result;
    }

    /**
     * 引用
     */
    @Data
    @Builder
    public static class Citation implements Serializable {
        private String sourceId;
        private String text;
        private Double relevance;
        private String url;
    }
}
