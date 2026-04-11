package com.enterprise.edams.notification.channel;

import com.enterprise.edams.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 短信通知渠道
 *
 * <p>通过第三方短信服务（如阿里云SMS、腾讯云SMS等）发送短信</p>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class SmsChannel implements NotificationChannel {

    private final RestTemplate restTemplate;
    private final String smsApiUrl;
    private final String smsAccessKey;

    public SmsChannel(RestTemplate restTemplate,
                      @Value("${notification.sms.api-url:}") String apiUrl,
                      @Value("${notification.sms.access-key:}") String accessKey) {
        this.restTemplate = restTemplate;
        this.smsApiUrl = apiUrl;
        this.smsAccessKey = accessKey;
    }

    @Override
    public String getChannelType() {
        return "sms";
    }

    @Override
    public boolean send(Notification notification) {
        try {
            if (smsApiUrl == null || smsApiUrl.isEmpty()) {
                log.warn("短信服务未配置，跳过发送");
                notification.setErrorMessage("短信服务未配置");
                return false;
            }

            log.info("发送短信到: {}，内容: {}", 
                    maskPhone(notification.getTargetAddress()), 
                    truncateContent(notification.getContent()));

            // TODO: 实际调用短信服务API
            // 这里模拟调用成功
            boolean success = mockSmsSend(notification.getTargetAddress(), notification.getContent());
            
            if (success) {
                log.info("短信发送成功: {}", maskPhone(notification.getTargetAddress()));
            } else {
                notification.setErrorMessage("短信服务返回失败");
            }
            return success;

        } catch (Exception e) {
            log.error("短信发送失败: {}", e.getMessage(), e);
            notification.setErrorMessage("短信发送异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateAddress(String targetAddress) {
        if (targetAddress == null || targetAddress.isEmpty()) return false;
        return targetAddress.matches("^1[3-9]\\d{9}$");
    }

    @Override
    public boolean isAvailable() {
        // 简化：检查配置是否完整
        return smsApiUrl != null && !smsApiUrl.isEmpty();
    }

    /** 模拟短信发送（实际应替换为真实API调用） */
    private boolean mockSmsSend(String phone, String content) {
        log.info("[SMS Mock] 发送到: {}，内容长度: {} 字符", phone, content.length());
        return true; // 模拟成功
    }

    /** 手机号脱敏 */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "****";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 内容截断（短信通常限制在70字以内） */
    private String truncateContent(String content) {
        if (content == null) return "";
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }
}
