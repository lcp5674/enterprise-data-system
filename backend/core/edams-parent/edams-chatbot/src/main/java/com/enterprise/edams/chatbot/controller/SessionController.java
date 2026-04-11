package com.enterprise.edams.chatbot.controller;

import com.enterprise.edams.chatbot.entity.ChatContext;
import com.enterprise.edams.chatbot.entity.ChatSession;
import com.enterprise.edams.chatbot.service.SessionService;
import com.enterprise.edams.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Tag(name = "会话管理", description = "会话管理相关接口")
@RestController
@RequestMapping("/api/chatbot/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "创建会话")
    @PostMapping
    public R<ChatSession> createSession(
            @RequestParam Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String sessionTitle,
            @RequestParam(required = false) String sessionType) {
        return R.ok(sessionService.createSession(userId, username, sessionTitle, sessionType));
    }

    @Operation(summary = "获取会话")
    @GetMapping("/{sessionId}")
    public R<ChatSession> getSession(@PathVariable Long sessionId) {
        return R.ok(sessionService.getSession(sessionId));
    }

    @Operation(summary = "获取用户活跃会话")
    @GetMapping("/user/{userId}/active")
    public R<List<ChatSession>> getActiveSessions(@PathVariable Long userId) {
        return R.ok(sessionService.getActiveSessions(userId));
    }

    @Operation(summary = "获取用户所有会话")
    @GetMapping("/user/{userId}")
    public R<List<ChatSession>> getUserSessions(@PathVariable Long userId) {
        return R.ok(sessionService.getUserAllSessions(userId));
    }

    @Operation(summary = "更新会话")
    @PutMapping("/{sessionId}")
    public R<ChatSession> updateSession(@PathVariable Long sessionId, @RequestBody ChatSession session) {
        session.setId(sessionId);
        return R.ok(sessionService.updateSession(session));
    }

    @Operation(summary = "关闭会话")
    @PostMapping("/{sessionId}/close")
    public R<Void> closeSession(@PathVariable Long sessionId, @RequestParam(required = false) String closeReason) {
        sessionService.closeSession(sessionId, closeReason);
        return R.ok();
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/{sessionId}")
    public R<Void> deleteSession(@PathVariable Long sessionId) {
        sessionService.deleteSession(sessionId);
        return R.ok();
    }

    @Operation(summary = "获取会话上下文")
    @GetMapping("/{sessionId}/context")
    public R<ChatContext> getContext(@PathVariable Long sessionId) {
        return R.ok(sessionService.getContext(sessionId));
    }

    @Operation(summary = "更新会话上下文")
    @PutMapping("/{sessionId}/context")
    public R<Void> updateContext(@PathVariable Long sessionId, @RequestBody ChatContext context) {
        sessionService.updateContext(sessionId, context);
        return R.ok();
    }

    @Operation(summary = "添加上下文变量")
    @PostMapping("/{sessionId}/context/variable")
    public R<Void> addContextVariable(
            @PathVariable Long sessionId,
            @RequestParam String key,
            @RequestParam Object value) {
        sessionService.addContextVariable(sessionId, key, value);
        return R.ok();
    }

    @Operation(summary = "获取上下文变量")
    @GetMapping("/{sessionId}/context/variable")
    public R<Object> getContextVariable(@PathVariable Long sessionId, @RequestParam String key) {
        return R.ok(sessionService.getContextVariable(sessionId, key));
    }

    @Operation(summary = "清除会话上下文")
    @DeleteMapping("/{sessionId}/context")
    public R<Void> clearContext(@PathVariable Long sessionId) {
        sessionService.clearContext(sessionId);
        return R.ok();
    }

    @Operation(summary = "设置满意度评分")
    @PostMapping("/{sessionId}/satisfaction")
    public R<Void> setSatisfactionScore(@PathVariable Long sessionId, @RequestParam int score) {
        sessionService.setSatisfactionScore(sessionId, score);
        return R.ok();
    }
}
