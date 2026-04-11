package com.enterprise.edams.chatbot.service;

import com.enterprise.edams.chatbot.entity.ChatMessage;
import com.enterprise.edams.chatbot.entity.ChatSession;
import com.enterprise.edams.chatbot.entity.ChatContext;

import java.util.List;
import java.util.Map;

/**
 * 对话服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface ChatService {

    /**
     * 发送消息并获取响应
     */
    ChatMessage sendMessage(Long sessionId, String userMessage, Long userId);

    /**
     * 创建新会话
     */
    ChatSession createSession(Long userId, String username, String sessionTitle, String sessionType);

    /**
     * 关闭会话
     */
    void closeSession(Long sessionId, String closeReason);

    /**
     * 获取会话详情
     */
    ChatSession getSessionById(Long sessionId);

    /**
     * 获取用户的所有会话
     */
    List<ChatSession> getUserSessions(Long userId);

    /**
     * 获取会话消息历史
     */
    List<ChatMessage> getSessionMessages(Long sessionId, int limit);

    /**
     * 获取会话上下文
     */
    ChatContext getSessionContext(Long sessionId);

    /**
     * 更新会话上下文
     */
    void updateSessionContext(Long sessionId, ChatContext context);

    /**
     * 评价消息
     */
    void rateMessage(Long messageId, int rating, String feedback);

    /**
     * 获取未读消息数
     */
    int getUnreadCount(Long sessionId);

    /**
     * 标记消息已读
     */
    void markAsRead(Long messageId);

    /**
     * 统计意图使用情况
     */
    List<Map<String, Object>> getIntentStatistics();

    /**
     * 清理过期会话
     */
    int cleanExpiredSessions(int inactiveDays);
}
