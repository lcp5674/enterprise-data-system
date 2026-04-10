package com.enterprise.edams.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.notification.dto.NotificationMessageVO;
import com.enterprise.edams.notification.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "消息管理", description = "消息相关接口")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/current-user")
    @Operation(summary = "获取当前用户消息", description = "获取当前登录用户的消息列表")
    public Result<PageResult<NotificationMessageVO>> getCurrentUserMessages(
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize) {
        String userId = getCurrentUserId();
        Page<NotificationMessageVO> page = messageService.getUserMessages(userId, status, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/current-user/unread")
    @Operation(summary = "获取当前用户未读消息", description = "获取当前登录用户的未读消息列表")
    public Result<List<NotificationMessageVO>> getUnreadMessages() {
        String userId = getCurrentUserId();
        List<NotificationMessageVO> messages = messageService.getUnreadMessages(userId);
        return Result.success(messages);
    }

    @GetMapping("/current-user/unread-count")
    @Operation(summary = "获取当前用户未读消息数量", description = "获取当前登录用户的未读消息数量")
    public Result<Long> getUnreadCount() {
        String userId = getCurrentUserId();
        long count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记消息已读", description = "标记指定消息为已读")
    public Result<Void> markAsRead(
            @Parameter(description = "消息ID") @PathVariable String id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "标记所有消息已读", description = "标记当前用户所有消息为已读")
    public Result<Void> markAllAsRead() {
        String userId = getCurrentUserId();
        messageService.markAllAsRead(userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除消息", description = "删除指定消息")
    public Result<Void> deleteMessage(
            @Parameter(description = "消息ID") @PathVariable String id) {
        messageService.deleteMessage(id);
        return Result.success();
    }

    @GetMapping("/business")
    @Operation(summary = "根据业务获取消息", description = "根据业务类型和ID获取消息")
    public Result<List<NotificationMessageVO>> getMessagesByBusiness(
            @Parameter(description = "业务类型") @RequestParam String businessType,
            @Parameter(description = "业务ID") @RequestParam String businessId) {
        List<NotificationMessageVO> messages = messageService.getMessagesByBusiness(businessType, businessId);
        return Result.success(messages);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
