package com.enterprise.edams.notification.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.notification.dto.NotificationSendRequest;
import com.enterprise.edams.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知控制器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "通知发送相关接口")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @Operation(summary = "发送通知", description = "发送通知消息")
    public Result<String> sendNotification(@Valid @RequestBody NotificationSendRequest request) {
        String messageId = notificationService.sendNotificationWithId(request);
        return Result.success(messageId);
    }

    @PostMapping("/send/batch")
    @Operation(summary = "批量发送通知", description = "批量发送通知消息")
    public Result<Void> batchSendNotification(@Valid @RequestBody NotificationSendRequest request) {
        notificationService.batchSendNotification(request);
        return Result.success();
    }

    @PostMapping("/send/template")
    @Operation(summary = "发送模板通知", description = "使用模板发送通知")
    public Result<Void> sendTemplateNotification(
            @Parameter(description = "模板编码") @RequestParam String templateCode,
            @Parameter(description = "用户ID") @RequestParam String userId,
            @RequestBody Map<String, String> variables) {
        notificationService.sendTemplateNotification(templateCode, userId, variables);
        return Result.success();
    }

    @PostMapping("/send/in-app")
    @Operation(summary = "发送站内消息", description = "发送站内消息")
    public Result<Void> sendInAppMessage(
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "标题") @RequestParam String title,
            @Parameter(description = "内容") @RequestParam String content,
            @Parameter(description = "业务类型") @RequestParam(required = false) String businessType,
            @Parameter(description = "业务ID") @RequestParam(required = false) String businessId) {
        notificationService.sendInAppMessage(userId, title, content, businessType, businessId);
        return Result.success();
    }

    @PostMapping("/send/email")
    @Operation(summary = "发送邮件", description = "发送邮件通知")
    public Result<Void> sendEmail(
            @Parameter(description = "邮箱") @RequestParam String email,
            @Parameter(description = "标题") @RequestParam String title,
            @Parameter(description = "内容") @RequestParam String content) {
        notificationService.sendEmail(email, title, content);
        return Result.success();
    }

    @PostMapping("/send/sms")
    @Operation(summary = "发送短信", description = "发送短信通知")
    public Result<Void> sendSms(
            @Parameter(description = "手机号") @RequestParam String phone,
            @Parameter(description = "内容") @RequestParam String content) {
        notificationService.sendSms(phone, content);
        return Result.success();
    }
}
