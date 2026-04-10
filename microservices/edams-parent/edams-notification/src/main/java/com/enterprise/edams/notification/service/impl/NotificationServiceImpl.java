package com.enterprise.edams.notification.service.impl;

import com.enterprise.edams.notification.dto.NotificationSendRequest;
import com.enterprise.edams.notification.entity.NotificationMessage;
import com.enterprise.edams.notification.entity.NotificationTemplate;
import com.enterprise.edams.notification.repository.NotificationMessageRepository;
import com.enterprise.edams.notification.repository.NotificationTemplateRepository;
import com.enterprise.edams.notification.service.EmailService;
import com.enterprise.edams.notification.service.NotificationService;
import com.enterprise.edams.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 通知服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMessageRepository messageRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Override
    @Transactional
    public void sendNotification(NotificationSendRequest request) {
        sendNotificationWithId(request);
    }

    @Override
    @Transactional
    public String sendNotificationWithId(NotificationSendRequest request) {
        log.info("发送通知: type={}, title={}", request.getMessageType(), request.getTitle());

        // 创建消息记录
        NotificationMessage message = createMessage(request);
        messageRepository.insert(message);

        // 异步发送
        if (request.isAsync()) {
            sendNotificationAsync(message);
        } else {
            sendNotificationSync(message);
        }

        return message.getId();
    }

    @Override
    @Transactional
    public void batchSendNotification(NotificationSendRequest request) {
        log.info("批量发送通知: type={}, count={}", request.getMessageType(), 
                request.getUserIds() != null ? request.getUserIds().size() : 0);

        List<String> userIds = request.getUserIds();
        if (userIds == null || userIds.isEmpty()) {
            log.warn("批量发送通知失败: 用户列表为空");
            return;
        }

        for (String userId : userIds) {
            NotificationSendRequest singleRequest = new NotificationSendRequest();
            singleRequest.setMessageType(request.getMessageType());
            singleRequest.setUserId(userId);
            singleRequest.setTitle(request.getTitle());
            singleRequest.setContent(request.getContent());
            singleRequest.setBusinessType(request.getBusinessType());
            singleRequest.setBusinessId(request.getBusinessId());
            singleRequest.setVariables(request.getVariables());
            singleRequest.setAsync(true);
            
            sendNotification(singleRequest);
        }
    }

    @Override
    public void sendTemplateNotification(String templateCode, String userId, Map<String, String> variables) {
        NotificationTemplate template = templateRepository.findByCode(templateCode);
        if (template == null) {
            log.error("发送模板通知失败: 模板不存在, code={}", templateCode);
            return;
        }

        if (template.getStatus() != 1) {
            log.error("发送模板通知失败: 模板已禁用, code={}", templateCode);
            return;
        }

        // 渲染模板
        String title = renderVariables(template.getTitle(), variables);
        String content = renderVariables(template.getContent(), variables);

        NotificationSendRequest request = new NotificationSendRequest();
        request.setMessageType(template.getTemplateType());
        request.setUserId(userId);
        request.setTitle(title);
        request.setContent(content);
        request.setBusinessType("TEMPLATE");
        request.setBusinessId(templateCode);
        request.setVariables(variables);

        sendNotification(request);
    }

    @Override
    public void sendInAppMessage(String userId, String title, String content, String businessType, String businessId) {
        NotificationSendRequest request = new NotificationSendRequest();
        request.setMessageType("IN_APP");
        request.setUserId(userId);
        request.setTitle(title);
        request.setContent(content);
        request.setBusinessType(businessType);
        request.setBusinessId(businessId);
        request.setAsync(true);
        
        sendNotification(request);
    }

    @Override
    public void sendEmail(String email, String title, String content) {
        NotificationSendRequest request = new NotificationSendRequest();
        request.setMessageType("EMAIL");
        request.setEmail(email);
        request.setTitle(title);
        request.setContent(content);
        request.setAsync(true);
        
        sendNotification(request);
    }

    @Override
    public void sendSms(String phone, String content) {
        NotificationSendRequest request = new NotificationSendRequest();
        request.setMessageType("SMS");
        request.setPhone(phone);
        request.setContent(content);
        request.setAsync(true);
        
        sendNotification(request);
    }

    // ========== 私有方法 ==========

    private NotificationMessage createMessage(NotificationSendRequest request) {
        return NotificationMessage.builder()
                .id(UUID.randomUUID().toString())
                .messageType(request.getMessageType())
                .userId(request.getUserId())
                .email(request.getEmail())
                .phone(request.getPhone())
                .title(request.getTitle())
                .content(request.getContent())
                .businessType(request.getBusinessType())
                .businessId(request.getBusinessId())
                .status("PENDING")
                .retryCount(0)
                .createdBy(getCurrentUsername())
                .createdTime(LocalDateTime.now())
                .build();
    }

    @Async
    protected void sendNotificationAsync(NotificationMessage message) {
        sendNotificationSync(message);
    }

    private void sendNotificationSync(NotificationMessage message) {
        try {
            message.setStatus("SENDING");
            message.setSendTime(LocalDateTime.now());
            messageRepository.updateById(message);

            switch (message.getMessageType()) {
                case "EMAIL" -> {
                    if (message.getEmail() != null && !message.getEmail().isEmpty()) {
                        emailService.send(message.getEmail(), message.getTitle(), message.getContent());
                    }
                }
                case "SMS" -> {
                    if (message.getPhone() != null && !message.getPhone().isEmpty()) {
                        smsService.send(message.getPhone(), message.getContent());
                    }
                }
                case "IN_APP", "PUSH" -> {
                    // 站内消息已保存到数据库，WebSocket会推送
                    log.info("站内消息已保存: userId={}, title={}", message.getUserId(), message.getTitle());
                }
            }

            message.setStatus("SENT");
            messageRepository.updateById(message);
            log.info("通知发送成功: messageId={}", message.getId());

        } catch (Exception e) {
            log.error("通知发送失败: messageId={}", message.getId(), e);
            message.setStatus("FAILED");
            message.setErrorMessage(e.getMessage());
            message.setRetryCount(message.getRetryCount() + 1);
            messageRepository.updateById(message);
        }
    }

    private String renderVariables(String template, Map<String, String> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }

    private String getCurrentUsername() {
        try {
            return org.springframework.security.core.SecurityContextHolder.getContext()
                    .getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
