package com.enterprise.edams.notification.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendBatchSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendBatchSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.enterprise.edams.notification.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 短信服务实现 - 集成阿里云短信服务
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

    @Value("${notification.sms.aliyun.access-key-id:}")
    private String accessKeyId;

    @Value("${notification.sms.aliyun.access-key-secret:}")
    private String accessKeySecret;

    @Value("${notification.sms.aliyun.endpoint:dysmsapi.aliyuncs.com}")
    private String endpoint;

    @Value("${notification.sms.aliyun.sign-name:}")
    private String signName;

    @Value("${notification.sms.frequency.limit:5}")
    private int frequencyLimit;

    @Value("${notification.sms.frequency.window-minutes:60}")
    private int frequencyWindowMinutes;

    private Client aliyunSmsClient;

    private static final String SMS_CODE_PREFIX = "edams:sms:code:";
    private static final String SMS_FREQUENCY_PREFIX = "edams:sms:frequency:";
    private static final long SMS_CODE_EXPIRE_MINUTES = 5;

    // 手机号正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    public SmsServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 初始化阿里云短信客户端
     */
    @PostConstruct
    public void init() {
        if (smsEnabled && hasValidAliyunConfig()) {
            try {
                Config config = new Config()
                        .setAccessKeyId(accessKeyId)
                        .setAccessKeySecret(accessKeySecret)
                        .setEndpoint(endpoint);
                aliyunSmsClient = new Client(config);
                log.info("阿里云短信客户端初始化成功");
            } catch (Exception e) {
                log.error("阿里云短信客户端初始化失败", e);
            }
        } else {
            log.warn("短信服务未启用或未配置阿里云短信参数");
        }
    }

    /**
     * 检查是否配置了有效的阿里云短信参数
     */
    private boolean hasValidAliyunConfig() {
        return accessKeyId != null && !accessKeyId.isEmpty()
                && accessKeySecret != null && !accessKeySecret.isEmpty();
    }

    @Override
    public void send(String phone, String content) {
        if (!smsEnabled) {
            log.warn("短信服务未启用: phone={}", phone);
            return;
        }

        // 验证手机号
        if (!validatePhone(phone)) {
            throw new IllegalArgumentException("手机号格式不正确: " + phone);
        }

        // 检查发送频率限制
        if (!checkFrequencyLimit(phone)) {
            log.warn("短信发送频率超限: phone={}", phone);
            throw new RuntimeException("短信发送频率超限，请稍后再试");
        }

        log.info("发送短信: phone={}, content={}", phone, content);

        try {
            if (aliyunSmsClient != null) {
                // 使用阿里云短信API发送
                SendSmsRequest request = SendSmsRequest.builder()
                        .phoneNumbers(phone)
                        .signName(signName)
                        .templateCode("SMS_DIRECT_TEMPLATE")
                        .templateParam("{\"content\":\"" + content + "\"}")
                        .build();

                SendSmsResponse response = aliyunSmsClient.sendSms(request);
                log.info("阿里云短信发送响应: RequestId={}, Code={}, Message={}",
                        response.getBody().getRequestId(),
                        response.getBody().getCode(),
                        response.getBody().getMessage());

                if (!"OK".equals(response.getBody().getCode())) {
                    throw new RuntimeException("短信发送失败: " + response.getBody().getMessage());
                }
            } else {
                // 模拟发送（当未配置阿里云短信时）
                log.info("短信发送成功(模拟): phone={}", phone);
            }

            // 增加发送频率计数
            incrementFrequencyCount(phone);

            // 记录短信日志
            logSmsSend(phone, "DIRECT", content, true, null);

        } catch (Exception e) {
            log.error("短信发送失败: phone={}", phone, e);
            logSmsSend(phone, "DIRECT", content, false, e.getMessage());
            throw new RuntimeException("短信发送失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendTemplate(String phone, String templateCode, Map<String, String> variables) {
        if (!smsEnabled) {
            log.warn("短信服务未启用: phone={}", phone);
            return;
        }

        // 验证手机号
        if (!validatePhone(phone)) {
            throw new IllegalArgumentException("手机号格式不正确: " + phone);
        }

        // 检查发送频率限制
        if (!checkFrequencyLimit(phone)) {
            log.warn("短信发送频率超限: phone={}", phone);
            throw new RuntimeException("短信发送频率超限，请稍后再试");
        }

        // 使用短信模板渲染
        String content = renderTemplate(templateCode, variables);
        log.info("发送模板短信: phone={}, templateCode={}, variables={}", phone, templateCode, variables);

        try {
            if (aliyunSmsClient != null) {
                // 构建模板参数JSON
                String templateParam = buildTemplateParam(variables);

                SendSmsRequest request = SendSmsRequest.builder()
                        .phoneNumbers(phone)
                        .signName(signName)
                        .templateCode(templateCode)
                        .templateParam(templateParam)
                        .build();

                SendSmsResponse response = aliyunSmsClient.sendSms(request);
                log.info("阿里云模板短信发送响应: RequestId={}, Code={}, Message={}",
                        response.getBody().getRequestId(),
                        response.getBody().getCode(),
                        response.getBody().getMessage());

                if (!"OK".equals(response.getBody().getCode())) {
                    throw new RuntimeException("短信发送失败: " + response.getBody().getMessage());
                }
            } else {
                log.info("模板短信发送成功(模拟): phone={}, templateCode={}", phone, templateCode);
            }

            // 增加发送频率计数
            incrementFrequencyCount(phone);

            // 记录短信日志
            logSmsSend(phone, templateCode, content, true, null);

        } catch (Exception e) {
            log.error("模板短信发送失败: phone={}, templateCode={}", phone, templateCode, e);
            logSmsSend(phone, templateCode, content, false, e.getMessage());
            throw new RuntimeException("短信发送失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendVerifyCode(String phone, String code) {
        if (!smsEnabled) {
            log.warn("短信服务未启用: phone={}", phone);
            return;
        }

        // 验证手机号
        if (!validatePhone(phone)) {
            throw new IllegalArgumentException("手机号格式不正确: " + phone);
        }

        // 检查发送频率限制
        if (!checkFrequencyLimit(phone)) {
            log.warn("验证码发送频率超限: phone={}", phone);
            throw new RuntimeException("验证码发送过于频繁，请稍后再试");
        }

        // 保存验证码到Redis
        String cacheKey = SMS_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(cacheKey, code, SMS_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 发送验证码短信
        String templateCode = "SMS_VERIFY_CODE_TEMPLATE";
        Map<String, String> variables = new HashMap<>();
        variables.put("code", code);
        variables.put("minutes", String.valueOf(SMS_CODE_EXPIRE_MINUTES));

        log.info("发送验证码: phone={}, code={}", phone, code);

        try {
            if (aliyunSmsClient != null) {
                String templateParam = buildTemplateParam(variables);

                SendSmsRequest request = SendSmsRequest.builder()
                        .phoneNumbers(phone)
                        .signName(signName)
                        .templateCode(templateCode)
                        .templateParam(templateParam)
                        .build();

                SendSmsResponse response = aliyunSmsClient.sendSms(request);
                log.info("阿里云验证码短信发送响应: RequestId={}, Code={}, Message={}",
                        response.getBody().getRequestId(),
                        response.getBody().getCode(),
                        response.getBody().getMessage());

                if (!"OK".equals(response.getBody().getCode())) {
                    throw new RuntimeException("验证码发送失败: " + response.getBody().getMessage());
                }
            } else {
                log.info("验证码短信发送成功(模拟): phone={}, code={}", phone, code);
            }

            // 增加发送频率计数
            incrementFrequencyCount(phone);

            // 记录短信日志
            logSmsSend(phone, templateCode, "验证码: " + code, true, null);

        } catch (Exception e) {
            log.error("验证码短信发送失败: phone={}", phone, e);
            logSmsSend(phone, templateCode, "验证码: " + code, false, e.getMessage());
            throw new RuntimeException("验证码发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证手机号格式
     */
    private boolean validatePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 检查发送频率限制
     */
    private boolean checkFrequencyLimit(String phone) {
        String frequencyKey = SMS_FREQUENCY_PREFIX + phone;
        Object countObj = redisTemplate.opsForValue().get(frequencyKey);
        int count = countObj != null ? ((Number) countObj).intValue() : 0;
        return count < frequencyLimit;
    }

    /**
     * 增加发送频率计数
     */
    private void incrementFrequencyCount(String phone) {
        String frequencyKey = SMS_FREQUENCY_PREFIX + phone;
        Long newCount = redisTemplate.opsForValue().increment(frequencyKey);
        if (newCount != null && newCount == 1) {
            // 首次发送，设置过期时间
            redisTemplate.expire(frequencyKey, frequencyWindowMinutes, TimeUnit.MINUTES);
        }
    }

    /**
     * 模板渲染 - 替换模板中的变量
     */
    private String renderTemplate(String templateCode, Map<String, String> variables) {
        // 这里可以使用模板引擎进行更复杂的渲染
        // 简单的字符串替换
        String template = getTemplateContent(templateCode);
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    /**
     * 获取模板内容（实际应从数据库或配置中心获取）
     */
    private String getTemplateContent(String templateCode) {
        // 示例模板映射
        Map<String, String> templates = new HashMap<>();
        templates.put("SMS_DIRECT_TEMPLATE", "【{{signName}}】{{content}}");
        templates.put("SMS_VERIFY_CODE_TEMPLATE", "【{{signName}}】您的验证码是{{code}}，有效期{{minutes}}分钟。");
        templates.put("SMS_TASK_REMINDER_TEMPLATE", "【{{signName}}】您有待办任务：{{taskName}}，请及时处理。");
        templates.put("SMS_WORKFLOW_APPROVAL_TEMPLATE", "【{{signName}}】您有新的审批：{{workflowName}}，申请人：{{applicantName}}。");
        return templates.getOrDefault(templateCode, "【{{signName}}】{{content}}");
    }

    /**
     * 构建模板参数JSON
     */
    private String buildTemplateParam(Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 记录短信发送日志
     */
    private void logSmsSend(String phone, String templateCode, String content, boolean success, String errorMessage) {
        // 实际应保存到数据库
        if (success) {
            log.info("短信发送日志: phone={}, templateCode={}, success=true", phone, templateCode);
        } else {
            log.warn("短信发送失败日志: phone={}, templateCode={}, error={}", phone, templateCode, errorMessage);
        }
    }

    /**
     * 批量发送短信（可用于发送任务提醒）
     */
    public void batchSendTemplate(List<String> phones, String templateCode, Map<String, String> variables) {
        if (!smsEnabled) {
            log.warn("短信服务未启用");
            return;
        }

        if (phones == null || phones.isEmpty()) {
            throw new IllegalArgumentException("手机号列表不能为空");
        }

        log.info("批量发送模板短信: count={}, templateCode={}", phones.size(), templateCode);

        try {
            if (aliyunSmsClient != null && phones.size() <= 100) {
                // 阿里云支持最多100个手机号批量发送
                String phoneString = String.join(",", phones);
                String templateParam = buildTemplateParam(variables);

                SendBatchSmsRequest request = SendBatchSmsRequest.builder()
                        .phoneNumberJson("[\"" + String.join("\",\"", phones) + "\"]")
                        .signNameJson("[\"" + signName + "\"]")
                        .templateCode(templateCode)
                        .templateParamJson("[\"" + templateParam + "\"]")
                        .build();

                SendBatchSmsResponse response = aliyunSmsClient.sendBatchSms(request);
                log.info("阿里云批量短信发送响应: RequestId={}, Code={}, Message={}",
                        response.getBody().getRequestId(),
                        response.getBody().getCode(),
                        response.getBody().getMessage());

                if (!"OK".equals(response.getBody().getCode())) {
                    throw new RuntimeException("批量短信发送失败: " + response.getBody().getMessage());
                }
            } else {
                // 逐个发送
                for (String phone : phones) {
                    try {
                        sendTemplate(phone, templateCode, variables);
                    } catch (Exception e) {
                        log.error("批量发送中单个失败: phone={}", phone, e);
                    }
                }
            }

            log.info("批量短信发送完成: count={}", phones.size());

        } catch (Exception e) {
            log.error("批量短信发送失败", e);
            throw new RuntimeException("批量短信发送失败: " + e.getMessage(), e);
        }
    }
}
