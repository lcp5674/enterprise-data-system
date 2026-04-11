package com.enterprise.edams.chatbot.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 聊天消息实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("chat_message")
public class ChatMessage extends BaseEntity {

    /**
     * 会话ID
     */
    @TableField("session_id")
    private Long sessionId;

    /**
     * 消息角色：user, assistant, system
     */
    @TableField("role")
    private String role;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 消息类型：text, image, card, action
     */
    @TableField("message_type")
    private String messageType;

    /**
     * 意图类型
     */
    @TableField("intent_type")
    private String intentType;

    /**
     * 意图置信度
     */
    @TableField("intent_confidence")
    private java.math.BigDecimal intentConfidence;

    /**
     * 关联实体(JSON)
     */
    @TableField("entities")
    private String entities;

    /**
     * 响应时间(ms)
     */
    @TableField("response_time")
    private Long responseTime;

    /**
     * 消息时间
     */
    @TableField("message_time")
    private LocalDateTime messageTime;

    /**
     * 引用消息ID
     */
    @TableField("quoted_message_id")
    private Long quotedMessageId;

    /**
     * 附件信息(JSON)
     */
    @TableField("attachments")
    private String attachments;

    /**
     * 元数据(JSON)
     */
    @TableField("metadata")
    private String metadata;

    /**
     * 是否已读
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 用户评分
     */
    @TableField("rating")
    private Integer rating;

    /**
     * 用户反馈
     */
    @TableField("feedback")
    private String feedback;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 判断是否为用户消息
     */
    public boolean isUserMessage() {
        return "user".equalsIgnoreCase(role);
    }

    /**
     * 判断是否为助手消息
     */
    public boolean isAssistantMessage() {
        return "assistant".equalsIgnoreCase(role);
    }
}
