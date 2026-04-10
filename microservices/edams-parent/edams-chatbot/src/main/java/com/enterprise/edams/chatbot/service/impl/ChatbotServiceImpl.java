package com.enterprise.edams.chatbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.chatbot.config.ChatbotConfig;
import com.enterprise.edams.chatbot.dto.ChatRequestDTO;
import com.enterprise.edams.chatbot.dto.ChatResponseDTO;
import com.enterprise.edams.chatbot.dto.SessionDTO;
import com.enterprise.edams.chatbot.entity.ChatMessage;
import com.enterprise.edams.chatbot.entity.ConversationSession;
import com.enterprise.edams.chatbot.mapper.ChatMessageMapper;
import com.enterprise.edams.chatbot.mapper.ConversationSessionMapper;
import com.enterprise.edams.chatbot.service.ChatbotService;
import com.enterprise.edams.chatbot.service.LlmServiceClient;
import com.enterprise.edams.chatbot.service.KnowledgeRetrievalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 智能问答服务实现类
 *
 * @author Chatbot Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final ConversationSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final LlmServiceClient llmServiceClient;
    private final KnowledgeRetrievalService knowledgeRetrievalService;
    private final ChatbotConfig chatbotConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String SESSION_CACHE_PREFIX = "chatbot:session:";
    private static final String HISTORY_CACHE_PREFIX = "chatbot:history:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatResponseDTO ask(ChatRequestDTO request) {
        log.info("处理问答请求: userId={}, question={}", request.getUserId(), 
                request.getQuestion().substring(0, Math.min(50, request.getQuestion().length())));

        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取或创建会话
            String sessionId = getOrCreateSession(request);

            // 2. 检索相关知识
            List<ChatResponseDTO.ContextDTO> contexts = knowledgeRetrievalService.retrieve(
                    request.getQuestion(),
                    request.getGraphId(),
                    request.getSimilarityThreshold(),
                    request.getTopK()
            );

            // 3. 构建提示词
            String prompt = buildPrompt(request, contexts);

            // 4. 调用LLM服务
            ChatResponseDTO llmResponse = llmServiceClient.chat(
                    request.getUserId(),
                    prompt,
                    buildHistoryPrompt(request.getHistory()),
                    chatbotConfig.getConversation().getMaxHistorySize()
            );

            // 5. 保存消息
            saveMessages(sessionId, request, llmResponse, contexts);

            // 6. 生成建议问题
            List<String> suggestions = generateSuggestions(llmResponse.getAnswer(), contexts);

            // 7. 更新会话
            updateSession(sessionId);

            return ChatResponseDTO.builder()
                    .sessionId(sessionId)
                    .messageId(llmResponse.getMessageId())
                    .answer(llmResponse.getContent())
                    .finishReason(llmResponse.getFinishReason())
                    .citations(buildCitations(contexts))
                    .suggestedQuestions(suggestions)
                    .contexts(contexts)
                    .model(llmResponse.getModel())
                    .inputTokens(llmResponse.getInputTokens())
                    .outputTokens(llmResponse.getOutputTokens())
                    .latency(System.currentTimeMillis() - startTime)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("问答处理失败", e);
            return ChatResponseDTO.builder()
                    .sessionId(request.getSessionId())
                    .error(e.getMessage())
                    .latency(System.currentTimeMillis() - startTime)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public Flux<ChatResponseDTO> askStream(ChatRequestDTO request) {
        log.info("处理流式问答请求: userId={}", request.getUserId());

        return Flux.create(emitter -> {
            try {
                String sessionId = getOrCreateSession(request);

                // 检索相关知识
                List<ChatResponseDTO.ContextDTO> contexts = knowledgeRetrievalService.retrieve(
                        request.getQuestion(),
                        request.getGraphId(),
                        request.getSimilarityThreshold(),
                        request.getTopK()
                );

                String prompt = buildPrompt(request, contexts);

                // 流式调用LLM
                llmServiceClient.chatStream(
                        request.getUserId(),
                        prompt,
                        buildHistoryPrompt(request.getHistory()),
                        chatbotConfig.getConversation().getMaxHistorySize()
                ).subscribe(
                        chunk -> emitter.next(buildChunkResponse(sessionId, chunk)),
                        error -> {
                            log.error("流式问答失败", error);
                            emitter.error(error);
                        },
                        () -> {
                            updateSession(sessionId);
                            emitter.complete();
                        }
                );

            } catch (Exception e) {
                log.error("流式问答处理失败", e);
                emitter.error(e);
            }
        });
    }

    @Override
    public List<ChatResponseDTO> batchAsk(List<ChatRequestDTO> requests) {
        log.info("批量处理问答请求: count={}", requests.size());
        return requests.stream().map(this::ask).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SessionDTO createSession(SessionDTO sessionDTO) {
        log.info("创建会话: userId={}, name={}", sessionDTO.getUserId(), sessionDTO.getName());

        ConversationSession session = new ConversationSession();
        session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        session.setName(sessionDTO.getName() != null ? sessionDTO.getName() : "新会话");
        session.setSessionType(sessionDTO.getSessionType() != null ? sessionDTO.getSessionType() : "QA");
        session.setUserId(sessionDTO.getUserId());
        session.setTenantId(sessionDTO.getTenantId());
        session.setKnowledgeBaseId(sessionDTO.getKnowledgeBaseId());
        session.setGraphId(sessionDTO.getGraphId() != null ? 
                sessionDTO.getGraphId() : chatbotConfig.getKnowledge().getDefaultGraphId());
        session.setStatus("ACTIVE");
        session.setMessageCount(0);
        session.setCreator(sessionDTO.getUserId());
        session.setLastMessageTime(LocalDateTime.now());

        sessionMapper.insert(session);

        // 缓存会话
        cacheSession(session);

        return toSessionDTO(session);
    }

    @Override
    public SessionDTO getSession(String sessionId) {
        // 先从缓存获取
        String cacheKey = SESSION_CACHE_PREFIX + sessionId;
        SessionDTO cached = getCachedSession(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 从数据库获取
        ConversationSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<ConversationSession>()
                        .eq(ConversationSession::getSessionId, sessionId));

        if (session == null) {
            throw new RuntimeException("会话不存在: " + sessionId);
        }

        SessionDTO dto = toSessionDTO(session);
        cacheSession(session);

        return dto;
    }

    @Override
    public List<ChatResponseDTO.ChatMessageDTO> getSessionHistory(String sessionId, int limit) {
        // 先从缓存获取
        String cacheKey = HISTORY_CACHE_PREFIX + sessionId;
        @SuppressWarnings("unchecked")
        List<ChatResponseDTO.ChatMessageDTO> cached = 
                (List<ChatResponseDTO.ChatMessageDTO>) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null && cached.size() <= limit) {
            return cached;
        }

        // 从数据库获取
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getStatus, "SUCCESS")
                .orderByDesc(ChatMessage::getCreatedTime)
                .last("LIMIT " + limit);

        List<ChatMessage> messages = messageMapper.selectList(wrapper);

        List<ChatResponseDTO.ChatMessageDTO> history = messages.stream()
                .map(m -> ChatResponseDTO.ChatMessageDTO.builder()
                        .messageId(m.getMessageId())
                        .role(m.getRole())
                        .content(m.getContent())
                        .build())
                .collect(Collectors.toList());

        Collections.reverse(history);

        // 缓存
        redisTemplate.opsForValue().set(cacheKey, history, Duration.ofHours(1));

        return history;
    }

    @Override
    public List<SessionDTO> listUserSessions(String userId, String status) {
        LambdaQueryWrapper<ConversationSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationSession::getUserId, userId);

        if (status != null) {
            wrapper.eq(ConversationSession::getStatus, status);
        }

        wrapper.orderByDesc(ConversationSession::getLastMessageTime);

        return sessionMapper.selectList(wrapper).stream()
                .map(this::toSessionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SessionDTO updateSession(String sessionId, SessionDTO sessionDTO) {
        ConversationSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<ConversationSession>()
                        .eq(ConversationSession::getSessionId, sessionId));

        if (session == null) {
            throw new RuntimeException("会话不存在: " + sessionId);
        }

        if (sessionDTO.getName() != null) {
            session.setName(sessionDTO.getName());
        }
        if (sessionDTO.getTags() != null) {
            session.setTags(String.join(",", sessionDTO.getTags()));
        }
        if (sessionDTO.getRemark() != null) {
            session.setRemark(sessionDTO.getRemark());
        }

        sessionMapper.updateById(session);

        // 清除缓存
        clearSessionCache(sessionId);

        return toSessionDTO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeSession(String sessionId) {
        ConversationSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<ConversationSession>()
                        .eq(ConversationSession::getSessionId, sessionId));

        if (session == null) {
            throw new RuntimeException("会话不存在: " + sessionId);
        }

        session.setStatus("CLOSED");
        sessionMapper.updateById(session);

        clearSessionCache(sessionId);
        log.info("会话已关闭: {}", sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(String sessionId) {
        // 删除会话
        sessionMapper.delete(new LambdaQueryWrapper<ConversationSession>()
                .eq(ConversationSession::getSessionId, sessionId));

        // 删除消息
        messageMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId));

        clearSessionCache(sessionId);
        log.info("会话已删除: {}", sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rateSession(String sessionId, int score, String feedback) {
        ConversationSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<ConversationSession>()
                        .eq(ConversationSession::getSessionId, sessionId));

        if (session == null) {
            throw new RuntimeException("会话不存在: " + sessionId);
        }

        session.setSatisfactionScore(score);
        if (feedback != null) {
            session.setRemark(session.getRemark() + "\n用户反馈: " + feedback);
        }

        sessionMapper.updateById(session);
        log.info("会话已评价: sessionId={}, score={}", sessionId, score);
    }

    // ==================== 私有方法 ====================

    private String getOrCreateSession(ChatRequestDTO request) {
        if (request.getSessionId() != null && !request.getSessionId().isEmpty()) {
            return request.getSessionId();
        }

        SessionDTO session = createSession(SessionDTO.builder()
                .userId(request.getUserId())
                .tenantId(request.getTenantId())
                .knowledgeBaseId(request.getKnowledgeBaseId())
                .graphId(request.getGraphId())
                .name("新会话")
                .sessionType("QA")
                .build());

        return session.getSessionId();
    }

    private String buildPrompt(ChatRequestDTO request, List<ChatResponseDTO.ContextDTO> contexts) {
        StringBuilder prompt = new StringBuilder();

        // 添加系统提示
        prompt.append(chatbotConfig.getPrompt().getSystem()).append("\n\n");

        // 添加上下文
        if (!contexts.isEmpty()) {
            String contextText = contexts.stream()
                    .map(c -> String.format("[来源: %s] %s", c.getSource(), c.getContent()))
                    .collect(Collectors.joining("\n\n"));

            prompt.append("参考知识:\n")
                    .append(contextText)
                    .append("\n\n");
        }

        // 添加用户问题
        prompt.append("用户问题: ").append(request.getQuestion());

        return prompt.toString();
    }

    private String buildHistoryPrompt(List<ChatRequestDTO.ChatMessageDTO> history) {
        if (history == null || history.isEmpty()) {
            return "";
        }

        StringBuilder historyText = new StringBuilder();
        int count = 0;

        for (ChatRequestDTO.ChatMessageDTO msg : history) {
            if (count >= chatbotConfig.getConversation().getContextWindow()) {
                break;
            }
            historyText.append(msg.getRole().toUpperCase())
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
            count++;
        }

        return historyText.toString();
    }

    private List<String> generateSuggestions(String answer, List<ChatResponseDTO.ContextDTO> contexts) {
        // 简单实现：基于上下文生成建议问题
        List<String> suggestions = new ArrayList<>();

        if (!contexts.isEmpty()) {
            suggestions.add("能否详细介绍一下这个内容？");
            suggestions.add("这个信息有什么应用场景？");
            suggestions.add("还有其他相关的知识吗？");
        }

        return suggestions;
    }

    private List<ChatResponseDTO.CitationDTO> buildCitations(List<ChatResponseDTO.ContextDTO> contexts) {
        return contexts.stream()
                .map(c -> ChatResponseDTO.CitationDTO.builder()
                        .sourceId(c.getDocumentId())
                        .sourceName(c.getSource())
                        .text(c.getContent())
                        .relevance(c.getSimilarity())
                        .score((int) (c.getSimilarity() * 100))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    private void saveMessages(String sessionId, ChatRequestDTO request, 
                              ChatResponseDTO llmResponse, List<ChatResponseDTO.ContextDTO> contexts) {
        // 保存用户消息
        ChatMessage userMessage = new ChatMessage();
        userMessage.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        userMessage.setSessionId(sessionId);
        userMessage.setRole("USER");
        userMessage.setContent(request.getQuestion());
        userMessage.setMessageType("TEXT");
        userMessage.setStatus("SUCCESS");
        userMessage.setUserId(request.getUserId());
        userMessage.setCreatedTime(LocalDateTime.now());
        messageMapper.insert(userMessage);

        // 保存助手消息
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setMessageId(llmResponse.getMessageId());
        assistantMessage.setSessionId(sessionId);
        assistantMessage.setRole("ASSISTANT");
        assistantMessage.setContent(llmResponse.getContent());
        assistantMessage.setMessageType("TEXT");
        assistantMessage.setModel(llmResponse.getModel());
        assistantMessage.setInputTokens(llmResponse.getInputTokens());
        assistantMessage.setOutputTokens(llmResponse.getOutputTokens());
        assistantMessage.setCitations(toJson(contexts));
        assistantMessage.setStatus("SUCCESS");
        assistantMessage.setLatency(llmResponse.getLatency());
        assistantMessage.setUserId(request.getUserId());
        assistantMessage.setCreatedTime(LocalDateTime.now());
        messageMapper.insert(assistantMessage);
    }

    private void updateSession(String sessionId) {
        ConversationSession session = sessionMapper.selectOne(
                new LambdaQueryWrapper<ConversationSession>()
                        .eq(ConversationSession::getSessionId, sessionId));

        if (session != null) {
            session.setLastMessageTime(LocalDateTime.now());
            session.setMessageCount(session.getMessageCount() + 2);
            sessionMapper.updateById(session);

            // 清除缓存
            clearSessionCache(sessionId);
        }
    }

    private SessionDTO toSessionDTO(ConversationSession session) {
        return SessionDTO.builder()
                .sessionId(session.getSessionId())
                .name(session.getName())
                .sessionType(session.getSessionType())
                .userId(session.getUserId())
                .tenantId(session.getTenantId())
                .knowledgeBaseId(session.getKnowledgeBaseId())
                .graphId(session.getGraphId())
                .status(session.getStatus())
                .lastMessageTime(session.getLastMessageTime())
                .messageCount(session.getMessageCount())
                .satisfactionScore(session.getSatisfactionScore())
                .tags(session.getTags() != null ? List.of(session.getTags().split(",")) : List.of())
                .remark(session.getRemark())
                .createdTime(session.getCreatedTime())
                .build();
    }

    private void cacheSession(ConversationSession session) {
        String cacheKey = SESSION_CACHE_PREFIX + session.getSessionId();
        redisTemplate.opsForValue().set(cacheKey, toSessionDTO(session), Duration.ofHours(1));
    }

    private SessionDTO getCachedSession(String cacheKey) {
        return objectMapper.convertValue(redisTemplate.opsForValue().get(cacheKey), SessionDTO.class);
    }

    private void clearSessionCache(String sessionId) {
        String sessionKey = SESSION_CACHE_PREFIX + sessionId;
        String historyKey = HISTORY_CACHE_PREFIX + sessionId;
        redisTemplate.delete(List.of(sessionKey, historyKey));
    }

    private ChatResponseDTO buildChunkResponse(String sessionId, String chunk) {
        return ChatResponseDTO.builder()
                .sessionId(sessionId)
                .answer(chunk)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON转换失败", e);
            return null;
        }
    }
}
