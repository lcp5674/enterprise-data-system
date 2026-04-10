package com.enterprise.edams.notification.service;

/**
 * 短信服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface SmsService {

    /**
     * 发送短信
     */
    void send(String phone, String content);

    /**
     * 发送模板短信
     */
    void sendTemplate(String phone, String templateCode, java.util.Map<String, String> variables);

    /**
     * 发送验证码
     */
    void sendVerifyCode(String phone, String code);
}
