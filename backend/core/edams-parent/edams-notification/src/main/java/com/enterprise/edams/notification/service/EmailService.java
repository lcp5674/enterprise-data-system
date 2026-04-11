package com.enterprise.edams.notification.service;

/**
 * 邮件服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface EmailService {

    /** 发送简单文本邮件 */
    boolean sendSimpleEmail(String to, String subject, String content);

    /** 发送HTML格式邮件 */
    boolean sendHtmlEmail(String to, String subject, String htmlContent);

    /** 批量发送邮件（相同内容） */
    boolean sendBatch(String[] toAddresses, String subject, String content);
}
