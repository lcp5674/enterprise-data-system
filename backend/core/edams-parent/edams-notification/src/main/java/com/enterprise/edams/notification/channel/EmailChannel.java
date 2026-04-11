package com.enterprise.edams.notification.channel;

import com.enterprise.edams.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

/**
 * 邮件通知渠道
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class EmailChannel implements NotificationChannel {

    private final JavaMailSenderImpl mailSender;
    private final String fromAddress;

    public EmailChannel(JavaMailSenderImpl mailSender,
                        @Value("${notification.email.from:edams-noreply@enterprise.com}") String from) {
        this.mailSender = mailSender;
        this.fromAddress = from;
    }

    @Override
    public String getChannelType() {
        return "email";
    }

    @Override
    public boolean send(Notification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(notification.getTargetAddress());
            message.setSubject(notification.getTitle());
            message.setText(notification.getContent());

            log.info("发送邮件到: {}，主题: {}", notification.getTargetAddress(), notification.getTitle());
            mailSender.send(message);
            
            log.info("邮件发送成功: {}", notification.getTargetAddress());
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败: {} -> {}", notification.getTargetAddress(), e.getMessage(), e);
            notification.setErrorMessage("邮件发送失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateAddress(String targetAddress) {
        if (targetAddress == null || targetAddress.isEmpty()) return false;
        // 简单邮箱格式校验
        return targetAddress.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$");
    }

    @Override
    public boolean isAvailable() {
        try {
            mailSender.testConnection();
            return true;
        } catch (Exception e) {
            log.warn("邮件服务连接不可用: {}", e.getMessage());
            return false;
        }
    }
}
