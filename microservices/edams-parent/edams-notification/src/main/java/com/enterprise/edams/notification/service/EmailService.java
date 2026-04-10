package com.enterprise.edams.notification.service;

/**
 * 邮件服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface EmailService {

    /**
     * 发送邮件
     */
    void send(String to, String subject, String content);

    /**
     * 发送HTML邮件
     */
    void sendHtml(String to, String subject, String htmlContent);

    /**
     * 发送模板邮件
     */
    void sendTemplate(String to, String templateCode, java.util.Map<String, String> variables);
}
