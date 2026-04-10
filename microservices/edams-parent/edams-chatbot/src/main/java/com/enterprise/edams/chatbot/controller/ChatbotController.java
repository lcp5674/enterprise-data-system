package com.enterprise.edams.chatbot.controller;

import com.enterprise.edams.chatbot.dto.ChatRequestDTO;
import com.enterprise.edams.chatbot.dto.ChatResponseDTO;
import com.enterprise.edams.chatbot.dto.SessionDTO;
import com.enterprise.edams.chatbot.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 智能问答接口
 *
 * @author Chatbot Team
 * @version 1.0.0
 */
@Slf4j
@Tag(name = "智能问答", description = "基于知识图谱的智能问答接口")
@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Operation(summary = "智能问答", description = "发送问题并获取基于知识的回答")
    @PostMapping("/ask")
    public com.enterprise.edams.common.result.Result<ChatResponseDTO> ask(
            @Valid @RequestBody ChatRequestDTO request) {
        log.info("收到问答请求: userId={}, question={}", 
                request.getUserId(), request.getQuestion().substring(0, Math.min(50, request.getQuestion().length())));
        ChatResponseDTO response = chatbotService.ask(request);
        return com.enterprise.edams.common.result.Result.success(response);
    }

    @Operation(summary = "流式问答", description = "发送问题并获取流式回答")
    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponseDTO> askStream(@Valid @RequestBody ChatRequestDTO request) {
        log.info("收到流式问答请求: userId={}", request.getUserId());
        request.setStream(true);
        return chatbotService.askStream(request);
    }

    @Operation(summary = "批量问答", description = "批量发送问题并获取回答")
    @PostMapping("/ask/batch")
    public com.enterprise.edams.common.result.Result<List<ChatResponseDTO>> batchAsk(
            @RequestBody List<ChatRequestDTO> requests) {
        log.info("收到批量问答请求: count={}", requests.size());
        List<ChatResponseDTO> responses = chatbotService.batchAsk(requests);
        return com.enterprise.edams.common.result.Result.success(responses);
    }

    @Operation(summary = "创建会话", description = "创建一个新的问答会话")
    @PostMapping("/sessions")
    public com.enterprise.edams.common.result.Result<SessionDTO> createSession(
            @RequestBody SessionDTO session) {
        SessionDTO created = chatbotService.createSession(session);
        return com.enterprise.edams.common.result.Result.success("会话创建成功", created);
    }

    @Operation(summary = "获取会话", description = "获取指定会话的信息")
    @GetMapping("/sessions/{sessionId}")
    public com.enterprise.edams.common.result.Result<SessionDTO> getSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        SessionDTO session = chatbotService.getSession(sessionId);
        return com.enterprise.edams.common.result.Result.success(session);
    }

    @Operation(summary = "获取会话历史", description = "获取指定会话的聊天历史")
    @GetMapping("/sessions/{sessionId}/history")
    public com.enterprise.edams.common.result.Result<List<ChatResponseDTO.ChatMessageDTO>> getSessionHistory(
            @Parameter(description = "会话ID") @PathVariable String sessionId,
            @Parameter(description = "消息数量限制") @RequestParam(defaultValue = "20") int limit) {
        List<ChatResponseDTO.ChatMessageDTO> history = chatbotService.getSessionHistory(sessionId, limit);
        return com.enterprise.edams.common.result.Result.success(history);
    }

    @Operation(summary = "获取用户会话列表", description = "获取指定用户的所有会话")
    @GetMapping("/sessions")
    public com.enterprise.edams.common.result.Result<List<SessionDTO>> listUserSessions(
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status) {
        List<SessionDTO> sessions = chatbotService.listUserSessions(userId, status);
        return com.enterprise.edams.common.result.Result.success(sessions);
    }

    @Operation(summary = "更新会话", description = "更新指定会话的信息")
    @PutMapping("/sessions/{sessionId}")
    public com.enterprise.edams.common.result.Result<SessionDTO> updateSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId,
            @RequestBody SessionDTO session) {
        SessionDTO updated = chatbotService.updateSession(sessionId, session);
        return com.enterprise.edams.common.result.Result.success("会话更新成功", updated);
    }

    @Operation(summary = "关闭会话", description = "关闭指定的问答会话")
    @PostMapping("/sessions/{sessionId}/close")
    public com.enterprise.edams.common.result.Result<Void> closeSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        chatbotService.closeSession(sessionId);
        return com.enterprise.edams.common.result.Result.success("会话已关闭", null);
    }

    @Operation(summary = "删除会话", description = "删除指定的会话及其历史")
    @DeleteMapping("/sessions/{sessionId}")
    public com.enterprise.edams.common.result.Result<Void> deleteSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        chatbotService.deleteSession(sessionId);
        return com.enterprise.edams.common.result.Result.success("会话已删除", null);
    }

    @Operation(summary = "评价会话", description = "对会话进行满意度评价")
    @PostMapping("/sessions/{sessionId}/rate")
    public com.enterprise.edams.common.result.Result<Void> rateSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId,
            @Parameter(description = "评分 1-5") @RequestParam int score,
            @Parameter(description = "反馈内容") @RequestParam(required = false) String feedback) {
        chatbotService.rateSession(sessionId, score, feedback);
        return com.enterprise.edams.common.result.Result.success("评价已提交", null);
    }
}
