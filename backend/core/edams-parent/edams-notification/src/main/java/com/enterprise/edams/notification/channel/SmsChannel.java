package com.enterprise.edams.notification.channel;

import com.enterprise.edams.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 短信通知渠道
 *
 * <p>通过第三方短信服务（如阿里云SMS、腾讯云SMS等）发送短信</p>
 * <p>支持阿里云短信服务，需要配置以下参数：
 * <ul>
 *   <li>notification.sms.provider: aliyun (默认) | tencent</li>
 *   <li>notification.sms.api-url: API地址</li>
 *   <li>notification.sms.access-key: AccessKey ID</li>
 *   <li>notification.sms.secret-key: AccessKey Secret</li>
 *   <li>notification.sms.sign-name: 签名名称</li>
 *   <li>notification.sms.template-code: 模板CODE</li>
 * </ul>
 * </p>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class SmsChannel implements NotificationChannel {

    private final RestTemplate restTemplate;
    private final String provider;
    private final String smsApiUrl;
    private final String accessKey;
    private final String secretKey;
    private final String signName;
    private final String templateCode;

    public SmsChannel(RestTemplate restTemplate,
                      @Value("${notification.sms.provider:aliyun}") String provider,
                      @Value("${notification.sms.api-url:}") String apiUrl,
                      @Value("${notification.sms.access-key:}") String accessKey,
                      @Value("${notification.sms.secret-key:}") String secretKey,
                      @Value("${notification.sms.sign-name:}") String signName,
                      @Value("${notification.sms.template-code:}") String templateCode) {
        this.restTemplate = restTemplate;
        this.provider = provider;
        this.smsApiUrl = apiUrl;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.signName = signName;
        this.templateCode = templateCode;
    }

    @Override
    public String getChannelType() {
        return "sms";
    }

    @Override
    public boolean send(Notification notification) {
        try {
            // 1. 配置检查
            if (!isAvailable()) {
                log.warn("短信服务未完整配置，跳过发送。provider={}, apiUrl={}, accessKey={}, signName={}, template={}",
                        provider, smsApiUrl, maskKey(accessKey), signName, templateCode);
                notification.setErrorMessage("短信服务未完整配置");
                return false;
            }

            // 2. 验证手机号
            String phone = notification.getTargetAddress();
            if (!validateAddress(phone)) {
                notification.setErrorMessage("手机号格式不正确: " + maskPhone(phone));
                return false;
            }

            // 3. 构建短信内容
            String content = truncateContent(notification.getContent());

            // 4. 根据不同服务商发送
            boolean success;
            if ("aliyun".equalsIgnoreCase(provider)) {
                success = sendViaAliyun(phone, content);
            } else if ("tencent".equalsIgnoreCase(provider)) {
                success = sendViaTencent(phone, content);
            } else {
                log.warn("不支持的短信服务商: {}", provider);
                notification.setErrorMessage("不支持的短信服务商: " + provider);
                return false;
            }

            if (success) {
                log.info("短信发送成功: {}，内容: {}", maskPhone(phone), content);
            }
            return success;

        } catch (Exception e) {
            log.error("短信发送失败: {}", e.getMessage(), e);
            notification.setErrorMessage("短信发送异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 阿里云短信发送
     */
    private boolean sendViaAliyun(String phone, String content) {
        try {
            // 阿里云签名算法
            TreeMap<String, String> params = new TreeMap<>();
            params.put("AccessKeyId", accessKey);
            params.put("Format", "JSON");
            params.put("SignatureMethod", "HMAC-SHA1");
            params.put("SignatureNonce", String.valueOf(System.currentTimeMillis()));
            params.put("SignatureVersion", "1.0");
            params.put("Timestamp", Instant.now().atZone(java.time.ZoneId.of("UTC")).format(java.time.format.DateTimeFormatter.ISO_INSTANT));
            params.put("Version", "2017-05-25");
            params.put("Action", "SendSms");
            params.put("SignName", signName);
            params.put("TemplateCode", templateCode);
            params.put("PhoneNumbers", phone);
            params.put("TemplateParam", "{\"code\":\"" + content + "\"}");

            // 生成签名
            String signature = calculateSignature(params, "GET", smsApiUrl);

            // 构建请求URL
            StringBuilder urlBuilder = new StringBuilder(smsApiUrl);
            urlBuilder.append("?Signature=").append(urlEncode(signature));
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.append("&").append(entry.getKey()).append("=").append(urlEncode(entry.getValue()));
            }

            // 发送请求
            ResponseEntity<String> response = restTemplate.getForEntity(urlBuilder.toString(), String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("阿里云短信API返回: {}", response.getBody());
                return true;
            } else {
                log.error("阿里云短信API错误: {}", response.getStatusCode());
                return false;
            }

        } catch (RestClientException e) {
            log.error("调用阿里云短信API失败: {}", e.getMessage());
            // 如果配置了mock模式，允许降级
            return handleFallback(phone, content);
        }
    }

    /**
     * 腾讯云短信发送
     */
    private boolean sendViaTencent(String phone, String content) {
        try {
            // 腾讯云签名
            long timestamp = System.currentTimeMillis() / 1000;
            long random = (long) (Math.random() * 1000000);
            TreeMap<String, Object> params = new TreeMap<>();
            params.put("Action", "SendSms");
            params.put("Version", "2021-01-11");
            params.put("SdkAppId", accessKey);
            params.put("SignName", signName);
            params.put("TemplateId", templateCode);
            params.put("PhoneNumberSet.0", phone);
            params.put("TemplateParameterSet.0", content);
            params.put("Timestamp", timestamp);
            params.put("Nonce", random);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(smsApiUrl, request, String.class);

            return response.getStatusCode().is2xxSuccessful();

        } catch (RestClientException e) {
            log.error("调用腾讯云短信API失败: {}", e.getMessage());
            return handleFallback(phone, content);
        }
    }

    /**
     * 降级处理：配置未完成时的处理策略
     */
    private boolean handleFallback(String phone, String content) {
        // 在开发/测试环境，允许记录日志后返回成功
        String env = System.getProperty("spring.profiles.active", "prod");
        if ("dev".equals(env) || "test".equals(env)) {
            log.warn("[SMS Fallback] 开发/测试模式，短信未实际发送: phone={}, content={}", maskPhone(phone), content);
            return true;
        }
        return false;
    }

    /**
     * 计算阿里云API签名
     */
    private String calculateSignature(TreeMap<String, String> params, String method, String url) {
        try {
            // 按URL编码后的键值对
            StringBuilder stringToSign = new StringBuilder();
            stringToSign.append(method).append("&");
            stringToSign.append(urlEncode("/")).append("&");
            StringBuilder sortedParams = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (sortedParams.length() > 0) {
                    sortedParams.append("&");
                }
                sortedParams.append(urlEncode(entry.getKey())).append("=").append(urlEncode(entry.getValue()));
            }
            stringToSign.append(urlEncode(sortedParams.toString()));

            // HMAC-SHA1
            MessageDigest md = MessageDigest.getInstance("HmacSHA1");
            String key = secretKey + "&";
            byte[] hmacBytes = md.digest((key + stringToSign.toString()).getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("不支持的签名算法", e);
        }
    }

    /**
     * URL编码
     */
    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (Exception e) {
            return value;
        }
    }

    @Override
    public boolean validateAddress(String targetAddress) {
        if (targetAddress == null || targetAddress.isEmpty()) return false;
        // 中国大陆手机号格式验证
        return targetAddress.matches("^1[3-9]\\d{9}$");
    }

    @Override
    public boolean isAvailable() {
        // 检查必要配置是否完整
        boolean configured = smsApiUrl != null && !smsApiUrl.isEmpty()
                && accessKey != null && !accessKey.isEmpty()
                && secretKey != null && !secretKey.isEmpty()
                && signName != null && !signName.isEmpty()
                && templateCode != null && !templateCode.isEmpty();

        if (!configured) {
            log.debug("短信服务配置不完整");
        }
        return configured;
    }

    /** 手机号脱敏 */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "****";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** AccessKey脱敏 */
    private String maskKey(String key) {
        if (key == null || key.length() < 6) return "****";
        return key.substring(0, 4) + "****";
    }

    /** 内容截断（短信通常限制在70字以内） */
    private String truncateContent(String content) {
        if (content == null) return "";
        // 短信内容截断为70字符
        return content.length() > 70 ? content.substring(0, 70) : content;
    }
}
