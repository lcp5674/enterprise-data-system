package com.enterprise.edams.notification.service.impl;

import com.enterprise.edams.notification.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    private static final String SMS_CODE_PREFIX = "edams:sms:code:";
    private static final long SMS_CODE_EXPIRE_MINUTES = 5;

    public SmsServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void send(String phone, String content) {
        if (!smsEnabled) {
            log.warn("短信服务未启用: phone={}", phone);
            return;
        }

        log.info("发送短信: phone={}, content={}", phone, content);
        
        // TODO: 集成实际的短信服务商（如阿里云、腾讯云等）
        // 这里只是一个简单的实现示例
        try {
            // 实际项目中需要调用短信网关API
            log.info("短信发送成功: phone={}", phone);
        } catch (Exception e) {
            log.error("短信发送失败: phone={}", phone, e);
            throw new RuntimeException("短信发送失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendTemplate(String phone, String templateCode, java.util.Map<String, String> variables) {
        if (!smsEnabled) {
            log.warn("短信服务未启用: phone={}", phone);
            return;
        }

        // TODO: 使用短信模板渲染
        StringBuilder content = new StringBuilder();
        variables.forEach((key, value) -> content.append(value).append(", "));
        
        send(phone, content.toString());
    }

    @Override
    public void sendVerifyCode(String phone, String code) {
        if (!smsEnabled) {
            log.warn("短信服务未启用: phone={}", phone);
            return;
        }

        // 保存验证码到Redis
        String cacheKey = SMS_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(cacheKey, code, SMS_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 发送验证码短信
        send(phone, "您的验证码是：" + code + "，有效期5分钟。");
        
        log.info("验证码已发送: phone={}", phone);
    }
}
