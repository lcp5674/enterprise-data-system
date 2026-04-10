package com.enterprise.edams.chatbot.dto;

import lombok.Data;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话DTO
 *
 * @author Chatbot Team
 * @version 1.0.0
 */
@Data
@Builder
public class SessionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 会话名称
     */
    private String name;

    /**
     * 会话类型
     */
    private String sessionType;

    /**
     * 用户ID
     */
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
     * 状态
     */
    private String status;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 满意度评分
     */
    private Integer satisfactionScore;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 历史消息摘要
     */
    private List<ChatResponseDTO.ChatMessageDTO> recentMessages;
}
