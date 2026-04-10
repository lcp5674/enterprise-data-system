package com.enterprise.edams.chatbot.dto;

import lombok.Data;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 智能问答请求DTO
 *
 * @author Chatbot Team
 * @version 1.0.0
 */
@Data
@Builder
public class ChatRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID (可选，创建新会话时为空)
     */
    private String sessionId;

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 知识库ID
     */
    private String knowledgeBaseId;

    /**
     * 图谱ID
     */
    private String graphId;

    /**
     * 问题内容
     */
    @NotBlank(message = "问题内容不能为空")
    private String question;

    /**
     * 历史消息 (用于多轮对话)
     */
    private List<ChatMessageDTO> history;

    /**
     * 是否返回引用
     */
    @Builder.Default
    private Boolean includeCitations = true;

    /**
     * 是否流式输出
     */
    @Builder.Default
    private Boolean stream = false;

    /**
     * 相似度阈值
     */
    @Builder.Default
    private Double similarityThreshold = 0.7;

    /**
     * 检索数量
     */
    @Builder.Default
    private Integer topK = 5;

    /**
     * 扩展参数
     */
    private Map<String, Object> extraParams;

    /**
     * 聊天消息DTO
     */
    @Data
    @Builder
    public static class ChatMessageDTO implements Serializable {
        private String role;
        private String content;
        private String messageId;
    }
}
