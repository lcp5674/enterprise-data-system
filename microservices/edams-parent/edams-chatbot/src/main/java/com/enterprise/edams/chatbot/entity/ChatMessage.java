package com.enterprise.edams.chatbot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息实体
 *
 * @author Chatbot Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_message")
public class ChatMessage extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 消息角色: USER, ASSISTANT, SYSTEM
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型: TEXT, IMAGE, FILE, AUDIO
     */
    private String messageType;

    /**
     * 父消息ID (用于消息引用/回复)
     */
    private String parentMessageId;

    /**
     * 引用的消息ID
     */
    private String referencedMessageId;

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
     * 上下文信息 (JSON)
     */
    private String contextInfo;

    /**
     * 引用的知识 (JSON)
     */
    private String citations;

    /**
     * 状态: SUCCESS, FAILED, PARTIAL
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 延迟(ms)
     */
    private Long latency;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
