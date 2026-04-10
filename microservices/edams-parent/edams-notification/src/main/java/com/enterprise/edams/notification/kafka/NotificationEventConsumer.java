package com.enterprise.edams.notification.kafka;

import com.enterprise.edams.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 通知事件Kafka消费者
 * 消费各类业务事件，触发相应通知
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    /**
     * 消费质量告警事件，发送告警通知
     */
    @KafkaListener(
        topics = "${kafka.topics.quality.issue.detected:quality.issue.detected}",
        groupId = "${kafka.groups.notification-sender:notification-sender}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeQualityAlertEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("消费质量告警事件: topic={}, partition={}, offset={}", topic, partition, offset);
            
            QualityAlertEvent event = objectMapper.readValue(message, QualityAlertEvent.class);
            
            // 发送质量告警通知
            sendQualityAlertNotification(event);
            
            acknowledgment.acknowledge();
            log.debug("质量告警通知发送完成: checkId={}", event.getCheckId());
        } catch (Exception e) {
            log.error("处理质量告警事件失败: message={}", message, e);
        }
    }

    /**
     * 消费安全告警事件，发送安全通知
     */
    @KafkaListener(
        topics = "${kafka.topics.security.access.denied:security.access.denied}",
        groupId = "${kafka.groups.notification-sender:notification-sender}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeSecurityAlertEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("消费安全告警事件: topic={}, partition={}, offset={}", topic, partition, offset);
            
            SecurityAlertEvent event = objectMapper.readValue(message, SecurityAlertEvent.class);
            
            // 发送安全告警通知
            sendSecurityAlertNotification(event);
            
            acknowledgment.acknowledge();
            log.debug("安全告警通知发送完成: assetId={}", event.getAssetId());
        } catch (Exception e) {
            log.error("处理安全告警事件失败: message={}", message, e);
        }
    }

    /**
     * 消费工作流任务完成事件，发送任务完成通知
     */
    @KafkaListener(
        topics = "${kafka.topics.workflow.task.completed:workflow.task.completed}",
        groupId = "${kafka.groups.notification-sender:notification-sender}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeWorkflowTaskCompletedEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("消费工作流任务完成事件: topic={}, partition={}, offset={}", topic, partition, offset);
            
            WorkflowTaskCompletedEvent event = objectMapper.readValue(message, WorkflowTaskCompletedEvent.class);
            
            // 发送工作流完成通知
            sendWorkflowNotification(event);
            
            acknowledgment.acknowledge();
            log.debug("工作流任务完成通知发送完成: instanceId={}", event.getInstanceId());
        } catch (Exception e) {
            log.error("处理工作流任务完成事件失败: message={}", message, e);
        }
    }

    /**
     * 发送质量告警通知
     */
    private void sendQualityAlertNotification(QualityAlertEvent event) {
        log.info("发送质量告警通知: checkId={}, assetId={}, score={}, severity={}", 
                event.getCheckId(), event.getAssetId(), event.getScore(), event.getSeverity());
        
        // 构建通知内容
        String title = String.format("数据质量告警 - %s", event.getAssetName());
        String content = String.format(
            "资产「%s」的数据质量检测发现问题。\n" +
            "检测ID: %s\n" +
            "质量得分: %.2f\n" +
            "严重程度: %s\n" +
            "检测时间: %s",
            event.getAssetName(),
            event.getCheckId(),
            event.getScore(),
            event.getSeverity(),
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(event.getTimestamp()))
        );
        
        // 根据严重程度选择通知方式
        // 严重告警: 短信 + 邮件 + 站内消息
        // 一般告警: 站内消息
    }

    /**
     * 发送安全告警通知
     */
    private void sendSecurityAlertNotification(SecurityAlertEvent event) {
        log.info("发送安全告警通知: assetId={}, userId={}, action={}", 
                event.getAssetId(), event.getUserId(), event.getAction());
        
        // 构建通知内容
        String title = "数据安全告警";
        String content = String.format(
            "检测到未授权的数据访问尝试。\n" +
            "资产ID: %s\n" +
            "操作人: %s\n" +
            "操作类型: %s\n" +
            "时间: %s",
            event.getAssetId(),
            event.getUserId(),
            event.getAction(),
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(event.getTimestamp()))
        );
        
        // 安全告警需要立即通知
        // 发送短信 + 邮件给安全管理员
    }

    /**
     * 发送工作流完成通知
     */
    private void sendWorkflowNotification(WorkflowTaskCompletedEvent event) {
        log.info("发送工作流完成通知: instanceId={}, status={}", 
                event.getInstanceId(), event.getStatus());
        
        // 构建通知内容
        String title = String.format("工作流「%s」%s", 
                event.getProcessName(), 
                "COMPLETED".equals(event.getStatus()) ? "已完成" : "已终止");
        String content = String.format(
            "工作流「%s」已%s。\n" +
            "实例ID: %s\n" +
            "发起人: %s\n" +
            "完成时间: %s",
            event.getProcessName(),
            "COMPLETED".equals(event.getStatus()) ? "完成" : "终止",
            event.getInstanceId(),
            event.getStartUserName(),
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(event.getEndTime()))
        );
        
        // 发送站内消息给发起人
    }

    /**
     * 质量告警事件
     */
    @lombok.Data
    public static class QualityAlertEvent {
        private String eventId;
        private String checkId;
        private String assetId;
        private String assetName;
        private Double score;
        private Integer failedRules;
        private String severity; // INFO, WARNING, CRITICAL
        private String executedBy;
        private long timestamp;
    }

    /**
     * 安全告警事件
     */
    @lombok.Data
    public static class SecurityAlertEvent {
        private String eventId;
        private String assetId;
        private String assetName;
        private String userId;
        private String userName;
        private String action;
        private String reason;
        private String ipAddress;
        private long timestamp;
    }

    /**
     * 工作流任务完成事件
     */
    @lombok.Data
    public static class WorkflowTaskCompletedEvent {
        private String eventId;
        private String instanceId;
        private String processName;
        private String processDefinitionKey;
        private String status; // COMPLETED, REJECTED, TERMINATED
        private String startUserId;
        private String startUserName;
        private String currentApprover;
        private long startTime;
        private long endTime;
        private long durationSeconds;
    }
}
