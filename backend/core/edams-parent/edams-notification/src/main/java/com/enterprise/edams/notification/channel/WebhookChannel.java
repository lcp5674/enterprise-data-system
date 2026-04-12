package com.enterprise.edams.notification.channel;

import com.enterprise.edams.notification.entity.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Webhook通知渠道
 *
 * <p>通过HTTP POST将通知推送到指定的Webhook URL，支持企业微信、钉钉、飞书等平台</p>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class WebhookChannel implements NotificationChannel {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${notification.webhook.wechat-webhook-enabled:true}")
    private boolean wechatWebhookEnabled;

    @Value("${notification.webhook.dingtalk-webhook-enabled:true}")
    private boolean dingtalkWebhookEnabled;

    @Value("${notification.webhook.feishu-webhook-enabled:true}")
    private boolean feishuWebhookEnabled;

    public WebhookChannel(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getChannelType() {
        return "webhook";
    }

    @Override
    public boolean send(Notification notification) {
        try {
            String webhookUrl = notification.getTargetAddress();
            if (webhookUrl == null || !webhookUrl.startsWith("http")) {
                notification.setErrorMessage("无效的Webhook URL: " + webhookUrl);
                return false;
            }

            log.info("发送Webhook到: {}", maskUrl(webhookUrl));

            // 根据URL类型选择不同的消息格式
            Map<String, Object> payload;
            if (webhookUrl.contains("oapi.dingtalk.com")) {
                // 钉钉格式
                payload = buildDingTalkPayload(notification);
            } else if (webhookUrl.contains("qyapi.weixin.qq.com") || webhookUrl.contains("hooks.dingtalk.cn")) {
                // 企业微信格式
                payload = buildWeChatPayload(notification);
            } else if (webhookUrl.contains("open.feishu.cn")) {
                // 飞书格式
                payload = buildFeiShuPayload(notification);
            } else {
                // 通用JSON格式
                payload = buildGenericPayload(notification);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            String response = restTemplate.postForEntity(webhookUrl, request, String.class).getBody();
            
            log.info("Webhook发送成功，响应: {}", response != null ? response.substring(0, Math.min(100, response.length())) : "");
            return true;
        } catch (Exception e) {
            log.error("Webhook发送失败: {}", e.getMessage());
            notification.setErrorMessage("Webhook发送失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 构建企业微信消息格式
     */
    private Map<String, Object> buildWeChatPayload(Notification notification) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgtype", "markdown");
        
        Map<String, Object> markdown = new LinkedHashMap<>();
        markdown.put("content", buildMarkdownContent(notification));
        payload.put("markdown", markdown);
        
        return payload;
    }

    /**
     * 构建钉钉消息格式
     */
    private Map<String, Object> buildDingTalkPayload(Notification notification) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgtype", "markdown");
        
        Map<String, Object> markdown = new LinkedHashMap<>();
        markdown.put("title", notification.getTitle() != null ? notification.getTitle() : "EDAMS通知");
        markdown.put("text", buildMarkdownContent(notification));
        payload.put("markdown", markdown);
        
        // 钉钉需要设置Webhook机器人关键词验证
        payload.put("at", Map.of("isAtAll", false));
        
        return payload;
    }

    /**
     * 构建飞书消息格式
     */
    private Map<String, Object> buildFeiShuPayload(Notification notification) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msg_type", "interactive");
        
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("tag", "div");
        card.put("text", notification.getContent() != null ? notification.getContent() : "");
        
        Map<String, Object> element = new LinkedHashMap<>();
        element.put("tag", "markdown");
        element.put("content", buildMarkdownContent(notification));
        
        Map<String, Object> contentBlock = new LinkedHashMap<>();
        contentBlock.put("tag", "div");
        contentBlock.put("text", buildMarkdownContent(notification));
        
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("title", Map.of("tag", "plain_text", "content", 
            notification.getTitle() != null ? notification.getTitle() : "EDAMS通知"));
        header.put("template", "blue");
        
        payload.put("card", Map.of(
            "header", header,
            "elements", List.of(contentBlock)
        ));
        
        return payload;
    }

    /**
     * 构建通用JSON消息格式
     */
    private Map<String, Object> buildGenericPayload(Notification notification) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", notification.getTitle() != null ? notification.getTitle() : "");
        payload.put("content", notification.getContent() != null ? notification.getContent() : "");
        payload.put("type", notification.getType() != null ? notification.getType() : 5);
        payload.put("receiverId", notification.getReceiverId());
        payload.put("sourceModule", notification.getSourceModule() != null ? notification.getSourceModule() : "");
        payload.put("timestamp", System.currentTimeMillis());
        return payload;
    }

    /**
     * 构建Markdown格式内容
     */
    private String buildMarkdownContent(Notification notification) {
        StringBuilder sb = new StringBuilder();
        sb.append("### ").append(notification.getTitle() != null ? notification.getTitle() : "EDAMS通知").append("\n\n");
        sb.append(notification.getContent() != null ? notification.getContent() : "").append("\n\n");
        sb.append("---\n");
        sb.append("> 来源: ").append(notification.getSourceModule() != null ? notification.getSourceModule() : "EDAMS系统").append("\n");
        sb.append("> 时间: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        return sb.toString();
    }

    @Override
    public boolean validateAddress(String targetAddress) {
        if (targetAddress == null || targetAddress.isEmpty()) return false;
        // 支持企业微信、钉钉、飞书的webhook URL格式
        return targetAddress.startsWith("http://") || targetAddress.startsWith("https://")
            || targetAddress.contains("oapi.dingtalk.com")
            || targetAddress.contains("qyapi.weixin.qq.com")
            || targetAddress.contains("hooks.dingtalk.cn")
            || targetAddress.contains("open.feishu.cn");
    }

    @Override
    public boolean isAvailable() {
        return true; // Webhook渠道始终可用（只要网络正常）
    }

    private String maskUrl(String url) {
        try {
            java.net.URL u = new java.net.URL(url);
            // 隐藏路径中的敏感信息
            return u.getProtocol() + "://" + u.getHost() +
                   (u.getPort() > 0 && u.getPort() != 80 && u.getPort() != 443 ? ":" + u.getPort() : "") + "/***";
        } catch (Exception e) {
            return "***";
        }
    }
}
