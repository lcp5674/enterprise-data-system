package com.enterprise.edams.notification.channel;

import com.enterprise.edams.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Webhook通知渠道
 *
 * <p>通过HTTP POST将通知推送到指定的Webhook URL</p>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class WebhookChannel implements NotificationChannel {

    private final RestTemplate restTemplate;

    public WebhookChannel(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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

            // 构建请求体
            Map<String, Object> payload = Map.of(
                    "title", notification.getTitle() != null ? notification.getTitle() : "",
                    "content", notification.getContent() != null ? notification.getContent() : "",
                    "type", notification.getType() != null ? notification.getType() : 5,
                    "receiverId", notification.getReceiverId(),
                    "sourceModule", notification.getSourceModule() != null ? notification.getSourceModule() : "",
                    "timestamp", System.currentTimeMillis()
            );

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

    @Override
    public boolean validateAddress(String targetAddress) {
        if (targetAddress == null || targetAddress.isEmpty()) return false;
        return targetAddress.startsWith("http://") || targetAddress.startsWith("https://");
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
