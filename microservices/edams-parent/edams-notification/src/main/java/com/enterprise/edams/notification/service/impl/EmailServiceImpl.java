package com.enterprise.edams.notification.service.impl;

import com.enterprise.edams.notification.service.EmailService;
import com.enterprise.edams.notification.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * 邮件服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final NotificationTemplateService templateService;

    @Value("${notification.email.from:noreply@edams.com}")
    private String fromEmail;

    @Value("${notification.email.from-name:EDAMS系统通知}")
    private String fromName;

    @Override
    public void send(String to, String subject, String content) {
        log.info("发送邮件: to={}, subject={}", to, subject);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            message.setSentDate(java.util.Date.from(java.time.LocalDateTime.now()
                    .atZone(java.time.ZoneId.systemDefault()).toInstant()));
            
            mailSender.send(message);
            log.info("邮件发送成功: to={}", to);
        } catch (Exception e) {
            log.error("邮件发送失败: to={}", to, e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendHtml(String to, String subject, String htmlContent) {
        log.info("发送HTML邮件: to={}, subject={}", to, subject);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML邮件发送成功: to={}", to);
        } catch (Exception e) {
            log.error("HTML邮件发送失败: to={}", to, e);
            throw new RuntimeException("HTML邮件发送失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendTemplate(String to, String templateCode, java.util.Map<String, String> variables) {
        log.info("发送模板邮件: to={}, templateCode={}", to, templateCode);
        
        String content = templateService.renderTemplate(templateCode, variables);
        var template = templateService.getTemplateByCode(templateCode);
        
        send(to, template.getTitle(), content);
    }
}
