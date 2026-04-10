package com.enterprise.edams.workflow.feign;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.workflow.feign.fallback.NotificationFeignClientFallback;
import com.enterprise.edams.workflow.dto.NotificationSendRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 通知服务Feign客户端
 * 用于工作流服务调用通知服务发送通知消息
 *
 * @author Backend Team
 * @version 1.0.0
 */
@FeignClient(
    name = "edams-notification",
    url = "${feign.notification.url:}",
    fallback = NotificationFeignClientFallback.class
)
public interface NotificationFeignClient {

    /**
     * 发送通知
     *
     * @param request 通知发送请求
     * @return 消息ID
     */
    @PostMapping("/api/v1/notifications/send")
    Result<String> sendNotification(@RequestBody NotificationSendRequest request);

    /**
     * 批量发送通知
     *
     * @param request 批量通知发送请求
     * @return 操作结果
     */
    @PostMapping("/api/v1/notifications/send/batch")
    Result<Void> batchSendNotification(@RequestBody NotificationSendRequest request);

    /**
     * 发送模板通知
     *
     * @param templateCode 模板编码
     * @param userId       用户ID
     * @param variables    模板变量
     * @return 操作结果
     */
    @PostMapping("/api/v1/notifications/send/template")
    Result<Void> sendTemplateNotification(
            @RequestBody TemplateNotificationRequest request);

    /**
     * 发送站内消息
     *
     * @param userId      用户ID
     * @param title       标题
     * @param content     内容
     * @param businessType 业务类型
     * @param businessId  业务ID
     * @return 操作结果
     */
    @PostMapping("/api/v1/notifications/send/in-app")
    Result<Void> sendInAppMessage(@RequestBody InAppMessageRequest request);

    /**
     * 发送邮件
     *
     * @param email   邮箱
     * @param title   标题
     * @param content 内容
     * @return 操作结果
     */
    @PostMapping("/api/v1/notifications/send/email")
    Result<Void> sendEmail(@RequestBody EmailRequest request);

    /**
     * 发送短信
     *
     * @param phone   手机号
     * @param content 内容
     * @return 操作结果
     */
    @PostMapping("/api/v1/notifications/send/sms")
    Result<Void> sendSms(@RequestBody SmsRequest request);
}
