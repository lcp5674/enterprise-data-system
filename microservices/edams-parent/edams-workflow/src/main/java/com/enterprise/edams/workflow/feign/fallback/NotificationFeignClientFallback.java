package com.enterprise.edams.workflow.feign.fallback;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.workflow.feign.NotificationFeignClient;
import com.enterprise.edams.workflow.feign.dto.EmailRequest;
import com.enterprise.edams.workflow.feign.dto.InAppMessageRequest;
import com.enterprise.edams.workflow.feign.dto.SmsRequest;
import com.enterprise.edams.workflow.feign.dto.TemplateNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通知服务Feign客户端降级处理
 * 当通知服务不可用时，返回默认值并记录日志
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class NotificationFeignClientFallback implements NotificationFeignClient {

    @Override
    public Result<String> sendNotification(Object request) {
        log.error("Feign调用通知服务发送通知失败, 返回null, 请求: {}", request);
        return Result.success(null);
    }

    @Override
    public Result<Void> batchSendNotification(Object request) {
        log.error("Feign调用通知服务批量发送通知失败, 请求: {}", request);
        return Result.success(null);
    }

    @Override
    public Result<Void> sendTemplateNotification(TemplateNotificationRequest request) {
        log.error("Feign调用通知服务发送模板通知失败, templateCode: {}, userId: {}", 
                request != null ? request.getTemplateCode() : "null",
                request != null ? request.getUserId() : "null");
        return Result.success(null);
    }

    @Override
    public Result<Void> sendInAppMessage(InAppMessageRequest request) {
        log.error("Feign调用通知服务发送站内消息失败, userId: {}", 
                request != null ? request.getUserId() : "null");
        return Result.success(null);
    }

    @Override
    public Result<Void> sendEmail(EmailRequest request) {
        log.error("Feign调用通知服务发送邮件失败, email: {}", 
                request != null ? request.getEmail() : "null");
        return Result.success(null);
    }

    @Override
    public Result<Void> sendSms(SmsRequest request) {
        log.error("Feign调用通知服务发送短信失败, phone: {}", 
                request != null ? request.getPhone() : "null");
        return Result.success(null);
    }
}
