package com.enterprise.edams.chatbot.dto;

import lombok.Data;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能问答响应DTO
 *
 * @author Chatbot Team
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
     * 回复内容
     */
    private String answer;

    /**
     * 完成原因
     */
    private String finishReason;

    /**
     * 引用的知识列表
     */
    private List<CitationDTO> citations;

    /**
     * 建议的追问
     */
    private List<String> suggestedQuestions;

    /**
     * 检索到的上下文
     */
    private List<ContextDTO> contexts;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 输入Token数
     */
    private Integer inputTokens;

    /**
     * 输出Token数
     */
    private Integer outputTokens;

    /**
     * 延迟(ms)
     */
    private Long latency;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 元数据
     */
    private Object metadata;

    /**
     * 引用DTO
     */
    @Data
    @Builder
    public static class CitationDTO implements Serializable {
        private String sourceId;
        private String sourceName;
        private String text;
        private Double relevance;
        private String url;
        private Integer score;
    }

    /**
     * 上下文DTO
     */
    @Data
    @Builder
    public static class ContextDTO implements Serializable {
        private String content;
        private String source;
        private String documentId;
        private Double similarity;
        private Integer position;
    }
}
