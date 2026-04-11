package com.enterprise.edams.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.notification.entity.Notification;
import com.enterprise.edams.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知管理控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "通知发送、查询、标记已读等接口")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @Operation(summary = "发送通知", description="发送单条通知（支持邮件/短信/Webhook等渠道）")
    public Result<Notification> send(@RequestBody Notification notification) {
        return Result.success(notificationService.send(notification));
    }

    @PostMapping("/send-batch")
    @Operation(summary = "批量发送通知", description="同一内容发送给多个接收人")
    public Result<List<Notification>> sendBatch(@RequestBody Map<String, Object> request) {
        // 简化处理：从request中提取参数
        @SuppressWarnings("unchecked")
        List<Long> receiverIds = (List<Long>) request.get("receiverIds");
        String channel = (String) request.get("channel");
        String title = (String) request.get("title");
        String content = (String) request.get("content");

        Long[] ids = receiverIds != null ? receiverIds.toArray(new Long[0]) : new Long[0];
        return Result.success(notificationService.sendBatch(ids, channel, title, content));
    }

    @PostMapping("/send-by-template")
    @Operation(summary = "使用模板发送通知", description="通过模板编码发送格式化的通知")
    public Result<Notification> sendByTemplate(@RequestBody Map<String, Object> request) {
        String templateCode = (String) request.get("templateCode");
        Long receiverId = ((Number) request.get("receiverId")).longValue();
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.get("params");

        return Result.success(
                notificationService.sendByTemplate(templateCode, receiverId, params));
    }

    @GetMapping
    @Operation(summary = "分页查询通知")
    public PageResult<Notification> queryNotifications(
            @RequestParam(required = false) Long receiverId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        IPage<Notification> page = notificationService.queryNotifications(
                receiverId, type, status, isRead, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记为已读")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读")
    public Result<Void> markAllAsRead(@RequestParam Long receiverId) {
        notificationService.markAllAsRead(receiverId);
        return Result.success();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读数量")
    public Result<Long> getUnreadCount(@RequestParam Long receiverId) {
        return Result.success(notificationService.getUnreadCount(receiverId));
    }
}
