package com.enterprise.edams.chatbot.service.impl;

import com.enterprise.edams.chatbot.entity.ChatContext;
import com.enterprise.edams.chatbot.entity.ChatSession;
import com.enterprise.edams.chatbot.repository.ChatSessionMapper;
import com.enterprise.edams.chatbot.service.SessionService;
import com.enterprise.edams.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话管理服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final ChatSessionMapper sessionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSession createSession(Long userId, String username, String sessionTitle, String sessionType) {
        ChatSession session = ChatSession.builder()
                .sessionTitle(sessionTitle != null ? sessionTitle : "新对话")
                .userId(userId)
                .username(username)
                .status("active")
                .sessionType(sessionType != null ? sessionType : "qa")
                .lastActiveTime(LocalDateTime.now())
                .messageCount(0)
                .tenantId(1L)
                .build();
        sessionMapper.insert(session);
        log.info("创建会话: id={}, userId={}", session.getId(), userId);
        return session;
    }

    @Override
    public ChatSession getSession(Long sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        return session;
    }

    @Override
    public List<ChatSession> getActiveSessions(Long userId) {
        return sessionMapper.findActiveByUserId(userId);
    }

    @Override
    public List<ChatSession> getUserAllSessions(Long userId) {
        return sessionMapper.findByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSession updateSession(ChatSession session) {
        sessionMapper.updateById(session);
        log.info("更新会话: {}", session.getId());
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeSession(Long sessionId, String closeReason) {
        sessionMapper.closeSession(sessionId, LocalDateTime.now(), closeReason != null ? closeReason : "用户关闭", LocalDateTime.now());
        log.info("关闭会话: {} - {}", sessionId, closeReason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId) {
        sessionMapper.deleteById(sessionId);
        log.info("删除会话: {}", sessionId);
    }

    @Override
    public ChatContext getContext(Long sessionId) {
        // 简化实现：返回空上下文
        ChatContext context = new ChatContext();
        context.setSessionId(sessionId);
        context.setSlots(new java.util.HashMap<>());
        context.setVariables(new java.util.HashMap<>());
        context.setCreatedTime(LocalDateTime.now());
        return context;
    }

    @Override
    public void updateContext(Long sessionId, ChatContext context) {
        // 简化实现：上下文存储在Redis中
        log.debug("更新会话上下文: {}", sessionId);
    }

    @Override
    public void addContextVariable(Long sessionId, String key, Object value) {
        log.debug("添加上下文变量: sessionId={}, key={}", sessionId, key);
    }

    @Override
    public Object getContextVariable(Long sessionId, String key) {
        return null;
    }

    @Override
    public void clearContext(Long sessionId) {
        log.debug("清除会话上下文: {}", sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastActiveTime(Long sessionId) {
        sessionMapper.updateLastActiveTime(sessionId, LocalDateTime.now(), LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setSatisfactionScore(Long sessionId, int score) {
        sessionMapper.updateSatisfactionScore(sessionId, score, LocalDateTime.now());
    }
}
