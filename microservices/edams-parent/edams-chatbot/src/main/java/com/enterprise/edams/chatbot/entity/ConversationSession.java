package com.enterprise.edams.chatbot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话会话实体
 *
 * @author Chatbot Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_conversation_session")
public class ConversationSession extends BaseEntity implements Serializable {

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
     * 会话类型: QA-问答, KNOWLEDGE-知识查询, ANALYSIS-分析
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
     * 关联的知识库ID
     */
    private String knowledgeBaseId;

    /**
     * 关联的图谱ID
     */
    private String graphId;

    /**
     * 状态: ACTIVE-活跃, CLOSED-已关闭, ARCHIVED-已归档
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
    private String tags;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String creator;
}
