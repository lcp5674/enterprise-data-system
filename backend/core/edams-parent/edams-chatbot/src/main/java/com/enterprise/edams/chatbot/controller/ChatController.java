package com.enterprise.edams.chatbot.controller;

import com.enterprise.edams.chatbot.entity.ChatMessage;
import com.enterprise.edams.chatbot.entity.ChatSession;
import com.enterprise.edams.chatbot.service.ChatService;
import com.enterprise.edams.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 对话控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Tag(name = "AI对话", description = "对话相关接口")
@RestController
@RequestMapping("/api/chatbot/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "发送消息")
    @PostMapping("/{sessionId}/send")
    public R<ChatMessage> sendMessage(
            @PathVariable Long sessionId,
            @RequestParam String message,
            @RequestParam(required = false) Long userId) {
        return R.ok(chatService.sendMessage(sessionId, message, userId != null ? userId : 1L));
    }

    @Operation(summary = "创建会话")
    @PostMapping("/session")
    public R<ChatSession> createSession(
            @RequestParam Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String sessionTitle,
            @RequestParam(required = false) String sessionType) {
        return R.ok(chatService.createSession(userId, username != null ? username : "user", sessionTitle, sessionType));
    }

    @Operation(summary = "关闭会话")
    @PostMapping("/session/{sessionId}/close")
    public R<Void> closeSession(
            @PathVariable Long sessionId,
            @RequestParam(required = false) String closeReason) {
        chatService.closeSession(sessionId, closeReason);
        return R.ok();
    }

    @Operation(summary = "获取会话详情")
    @GetMapping("/session/{sessionId}")
    public R<ChatSession> getSession(@PathVariable Long sessionId) {
        return R.ok(chatService.getSessionById(sessionId));
    }

    @Operation(summary = "获取用户会话列表")
    @GetMapping("/sessions")
    public R<List<ChatSession>> getUserSessions(@RequestParam Long userId) {
        return R.ok(chatService.getUserSessions(userId));
    }

    @Operation(summary = "获取会话消息历史")
    @GetMapping("/session/{sessionId}/messages")
    public R<List<ChatMessage>> getSessionMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "50") int limit) {
        return R.ok(chatService.getSessionMessages(sessionId, limit));
    }

    @Operation(summary = "评价消息")
    @PostMapping("/message/{messageId}/rate")
    public R<Void> rateMessage(
            @PathVariable Long messageId,
            @RequestParam int rating,
            @RequestParam(required = false) String feedback) {
        chatService.rateMessage(messageId, rating, feedback);
        return R.ok();
    }

    @Operation(summary = "获取未读消息数")
    @GetMapping("/session/{sessionId}/unread")
    public R<Integer> getUnreadCount(@PathVariable Long sessionId) {
        return R.ok(chatService.getUnreadCount(sessionId));
    }

    @Operation(summary = "标记消息已读")
    @PostMapping("/message/{messageId}/read")
    public R<Void> markAsRead(@PathVariable Long messageId) {
        chatService.markAsRead(messageId);
        return R.ok();
    }

    @Operation(summary = "获取意图统计")
    @GetMapping("/stats/intent")
    public R<List<Map<String, Object>>> getIntentStatistics() {
        return R.ok(chatService.getIntentStatistics());
    }

    @Operation(summary = "清理过期会话")
    @DeleteMapping("/sessions/cleanup")
    public R<Integer> cleanExpiredSessions(@RequestParam(defaultValue = "7") int inactiveDays) {
        return R.ok(chatService.cleanExpiredSessions(inactiveDays));
    }
}
