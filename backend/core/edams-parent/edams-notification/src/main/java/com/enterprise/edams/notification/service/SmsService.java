package com.enterprise.edams.notification.service;

/**
 * 短信服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface SmsService {

    /** 发送短信验证码 */
    boolean sendVerificationCode(String phone, String code, int expireMinutes);

    /** 发送通知短信 */
    boolean sendNotificationSms(String phone, String content);

    /** 批量发送短信 */
    int sendBatch(String[] phones, String content);
}
