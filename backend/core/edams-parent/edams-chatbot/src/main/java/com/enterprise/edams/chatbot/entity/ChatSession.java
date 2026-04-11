package com.enterprise.edams.chatbot.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 聊天会话实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("chat_session")
public class ChatSession extends BaseEntity {

    /**
     * 会话标题
     */
    @TableField("session_title")
    private String sessionTitle;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 会话状态：active, closed
     */
    @TableField("status")
    private String status;

    /**
     * 会话类型：qa, task, analysis, faq
     */
    @TableField("session_type")
    private String sessionType;

    /**
     * 上下文数据(JSON)
     */
    @TableField("context_data")
    private String contextData;

    /**
     * 最后活跃时间
     */
    @TableField("last_active_time")
    private LocalDateTime lastActiveTime;

    /**
     * 消息数量
     */
    @TableField("message_count")
    private Integer messageCount;

    /**
     * 平均响应时间(ms)
     */
    @TableField("avg_response_time")
    private Long avgResponseTime;

    /**
     * 满意度评分
     */
    @TableField("satisfaction_score")
    private Integer satisfactionScore;

    /**
     * 关闭时间
     */
    @TableField("closed_time")
    private LocalDateTime closedTime;

    /**
     * 关闭原因
     */
    @TableField("close_reason")
    private String closeReason;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 判断会话是否活跃
     */
    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }
}
