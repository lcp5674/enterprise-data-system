package com.enterprise.edams.notification.service;

import com.enterprise.edams.notification.dto.NotificationSendRequest;

/**
 * 通知服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface NotificationService {

    /**
     * 发送通知
     */
    void sendNotification(NotificationSendRequest request);

    /**
     * 发送通知并返回消息ID
     */
    String sendNotificationWithId(NotificationSendRequest request);

    /**
     * 批量发送通知
     */
    void batchSendNotification(NotificationSendRequest request);

    /**
     * 使用模板发送通知
     */
    void sendTemplateNotification(String templateCode, String userId, java.util.Map<String, String> variables);

    /**
     * 发送站内消息
     */
    void sendInAppMessage(String userId, String title, String content, String businessType, String businessId);

    /**
     * 发送邮件
     */
    void sendEmail(String email, String title, String content);

    /**
     * 发送短信
     */
    void sendSms(String phone, String content);
}
