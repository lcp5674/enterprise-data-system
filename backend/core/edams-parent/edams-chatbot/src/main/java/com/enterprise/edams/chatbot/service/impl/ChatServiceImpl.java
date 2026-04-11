package com.enterprise.edams.chatbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.chatbot.entity.ChatContext;
import com.enterprise.edams.chatbot.entity.ChatIntent;
import com.enterprise.edams.chatbot.entity.ChatMessage;
import com.enterprise.edams.chatbot.entity.ChatSession;
import com.enterprise.edams.chatbot.repository.ChatMessageMapper;
import com.enterprise.edams.chatbot.repository.ChatSessionMapper;
import com.enterprise.edams.chatbot.service.ChatService;
import com.enterprise.edams.chatbot.service.IntentRecognizer;
import com.enterprise.edams.chatbot.service.ResponseGenerator;
import com.enterprise.edams.chatbot.service.SessionService;
import com.enterprise.edams.common.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 对话服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final IntentRecognizer intentRecognizer;
    private final ResponseGenerator responseGenerator;
    private final SessionService sessionService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CONTEXT_KEY_PREFIX = "edams:chatbot:context:";
    private static final Duration CONTEXT_TTL = Duration.ofHours(24);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage sendMessage(Long sessionId, String userMessage, Long userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        if (!"active".equalsIgnoreCase(session.getStatus())) {
            throw new BusinessException("会话已关闭");
        }

        long startTime = System.currentTimeMillis();

        // 1. 保存用户消息
        ChatMessage userMsg = saveUserMessage(sessionId, userMessage, userId);

        // 2. 获取会话上下文
        ChatContext context = getSessionContext(sessionId);
        context.setUserId(userId);
        context.setUsername(session.getUsername());

        // 3. 识别意图
        IntentRecognizer.RecognitionResult recognition = intentRecognizer.recognizeIntent(userMessage, context);
        String intentType = recognition.getIntentType();
        BigDecimal confidence = recognition.getConfidence();
        Map<String, String> slots = recognition.getExtractedSlots();

        // 4. 生成响应
        String responseContent = responseGenerator.generateResponse(intentType, slots, context);

        // 5. 保存助手消息
        ChatMessage assistantMsg = saveAssistantMessage(sessionId, responseContent, intentType, confidence, slots, System.currentTimeMillis() - startTime);

        // 6. 更新会话上下文
        context.setCurrentIntent(intentType);
        context.setIntentConfidence(confidence);
        if (context.getSlots() == null) {
            context.setSlots(new HashMap<>());
        }
        context.getSlots().putAll(slots);
        updateSessionContext(sessionId, context);

        // 7. 更新会话活跃时间
        sessionService.updateLastActiveTime(sessionId);

        log.info("处理对话消息: sessionId={}, intent={}, responseTime={}ms", sessionId, intentType, System.currentTimeMillis() - startTime);
        return assistantMsg;
    }

    private ChatMessage saveUserMessage(Long sessionId, String content, Long userId) {
        ChatMessage message = ChatMessage.builder()
                .sessionId(sessionId)
                .role("user")
                .content(content)
                .messageType("text")
                .messageTime(LocalDateTime.now())
                .isRead(1)
                .tenantId(1L)
                .createdBy(String.valueOf(userId))
                .build();
        messageMapper.insert(message);
        return message;
    }

    private ChatMessage saveAssistantMessage(Long sessionId, String content, String intentType, BigDecimal confidence, Map<String, String> slots, long responseTime) {
        ChatMessage message = ChatMessage.builder()
                .sessionId(sessionId)
                .role("assistant")
                .content(content)
                .messageType("text")
                .intentType(intentType)
                .intentConfidence(confidence)
                .entities(slots != null ? toJson(slots) : null)
                .responseTime(responseTime)
                .messageTime(LocalDateTime.now())
                .isRead(0)
                .tenantId(1L)
                .build();
        messageMapper.insert(message);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSession createSession(Long userId, String username, String sessionTitle, String sessionType) {
        return sessionService.createSession(userId, username, sessionTitle, sessionType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeSession(Long sessionId, String closeReason) {
        sessionService.closeSession(sessionId, closeReason);
    }

    @Override
    public ChatSession getSessionById(Long sessionId) {
        return sessionService.getSession(sessionId);
    }

    @Override
    public List<ChatSession> getUserSessions(Long userId) {
        return sessionService.getUserAllSessions(userId);
    }

    @Override
    public List<ChatMessage> getSessionMessages(Long sessionId, int limit) {
        if (limit > 0) {
            return messageMapper.findRecentBySessionId(sessionId, limit);
        }
        return messageMapper.findBySessionId(sessionId);
    }

    @Override
    public ChatContext getSessionContext(Long sessionId) {
        String key = CONTEXT_KEY_PREFIX + sessionId;
        String json = redisTemplate.opsForValue().get(key);
        if (json != null) {
            try {
                return objectMapper.readValue(json, ChatContext.class);
            } catch (Exception e) {
                log.error("解析上下文失败", e);
            }
        }
        ChatContext context = new ChatContext();
        context.setSessionId(sessionId);
        context.setCreatedTime(LocalDateTime.now());
        context.setRecentMessages(new ArrayList<>());
        return context;
    }

    @Override
    public void updateSessionContext(Long sessionId, ChatContext context) {
        String key = CONTEXT_KEY_PREFIX + sessionId;
        context.setUpdatedTime(LocalDateTime.now());
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(context), CONTEXT_TTL);
        } catch (Exception e) {
            log.error("保存上下文失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rateMessage(Long messageId, int rating, String feedback) {
        messageMapper.updateRating(messageId, rating, feedback, LocalDateTime.now());
    }

    @Override
    public int getUnreadCount(Long sessionId) {
        return messageMapper.countUnreadMessages(sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long messageId) {
        messageMapper.markAsRead(messageId, LocalDateTime.now());
    }

    @Override
    public List<Map<String, Object>> getIntentStatistics() {
        return messageMapper.countByIntentType();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredSessions(int inactiveDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(inactiveDays);
        List<ChatSession> inactiveSessions = sessionMapper.findInactiveSessions(cutoffTime);
        for (ChatSession session : inactiveSessions) {
            sessionService.closeSession(session.getId(), "超时自动关闭");
        }
        log.info("清理过期会话: {}条", inactiveSessions.size());
        return inactiveSessions.size();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
