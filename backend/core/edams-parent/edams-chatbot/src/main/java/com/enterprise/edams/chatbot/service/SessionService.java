package com.enterprise.edams.chatbot.service;

import com.enterprise.edams.chatbot.entity.ChatSession;
import com.enterprise.edams.chatbot.entity.ChatContext;

import java.util.List;

/**
 * 会话管理服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface SessionService {

    /**
     * 创建会话
     */
    ChatSession createSession(Long userId, String username, String sessionTitle, String sessionType);

    /**
     * 获取会话
     */
    ChatSession getSession(Long sessionId);

    /**
     * 获取用户活跃会话
     */
    List<ChatSession> getActiveSessions(Long userId);

    /**
     * 获取用户所有会话
     */
    List<ChatSession> getUserAllSessions(Long userId);

    /**
     * 更新会话
     */
    ChatSession updateSession(ChatSession session);

    /**
     * 关闭会话
     */
    void closeSession(Long sessionId, String closeReason);

    /**
     * 删除会话
     */
    void deleteSession(Long sessionId);

    /**
     * 获取会话上下文
     */
    ChatContext getContext(Long sessionId);

    /**
     * 更新会话上下文
     */
    void updateContext(Long sessionId, ChatContext context);

    /**
     * 添加上下文变量
     */
    void addContextVariable(Long sessionId, String key, Object value);

    /**
     * 获取上下文变量
     */
    Object getContextVariable(Long sessionId, String key);

    /**
     * 清除会话上下文
     */
    void clearContext(Long sessionId);

    /**
     * 更新会话活跃时间
     */
    void updateLastActiveTime(Long sessionId);

    /**
     * 设置会话满意度
     */
    void setSatisfactionScore(Long sessionId, int score);
}
