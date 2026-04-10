package com.enterprise.edams.workflow.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 工作流事件Kafka消费者
 * 消费资产变更、质量检查等事件，自动触发相关工作流
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowEventConsumer {

    private final ObjectMapper objectMapper;

    /**
     * 消费资产创建事件，触发资产审批工作流
     */
    @KafkaListener(
        topics = "${kafka.topics.asset.metadata.created:asset.metadata.created}",
        groupId = "${kafka.groups.workflow-trigger:workflow-trigger}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAssetCreatedForWorkflow(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("消费资产创建事件触发工作流: topic={}, partition={}, offset={}", topic, partition, offset);
            
            AssetCreatedEvent event = objectMapper.readValue(message, AssetCreatedEvent.class);
            
            // 触发资产审批工作流
            triggerAssetApprovalWorkflow(event);
            
            acknowledgment.acknowledge();
            log.debug("资产创建事件触发工作流完成: assetId={}", event.getAssetId());
        } catch (Exception e) {
            log.error("处理资产创建事件失败: message={}", message, e);
        }
    }

    /**
     * 消费质量检查完成事件，触发质量告警处理工作流
     */
    @KafkaListener(
        topics = "${kafka.topics.quality.check.completed:quality.check.completed}",
        groupId = "${kafka.groups.workflow-trigger:workflow-trigger}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeQualityCheckCompletedForWorkflow(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("消费质量检查完成事件: topic={}, partition={}, offset={}", topic, partition, offset);
            
            QualityCheckCompletedEvent event = objectMapper.readValue(message, QualityCheckCompletedEvent.class);
            
            // 检查是否触发告警
            if (Boolean.TRUE.equals(event.getAlertTriggered())) {
                // 触发质量告警处理工作流
                triggerQualityAlertWorkflow(event);
            }
            
            acknowledgment.acknowledge();
            log.debug("质量检查事件处理完成: checkId={}", event.getCheckId());
        } catch (Exception e) {
            log.error("处理质量检查事件失败: message={}", message, e);
        }
    }

    /**
     * 消费资产安全级别变更事件，触发安全审批工作流
     */
    @KafkaListener(
        topics = "${kafka.topics.asset.security.classified:asset.security.classified}",
        groupId = "${kafka.groups.workflow-trigger:workflow-trigger}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeSecurityClassifiedForWorkflow(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("消费安全级别变更事件: topic={}, partition={}, offset={}", topic, partition, offset);
            
            SecurityClassifiedEvent event = objectMapper.readValue(message, SecurityClassifiedEvent.class);
            
            // 触发安全审批工作流
            triggerSecurityApprovalWorkflow(event);
            
            acknowledgment.acknowledge();
            log.debug("安全级别变更事件处理完成: assetId={}", event.getAssetId());
        } catch (Exception e) {
            log.error("处理安全级别变更事件失败: message={}", message, e);
        }
    }

    /**
     * 触发资产审批工作流
     */
    private void triggerAssetApprovalWorkflow(AssetCreatedEvent event) {
        log.info("触发资产审批工作流: assetId={}, assetName={}, owner={}", 
                event.getAssetId(), event.getAssetName(), event.getOwner());
        
        // 根据安全级别决定是否需要审批
        // 高敏感资产需要多级审批
        // 低敏感资产自动通过
    }

    /**
     * 触发质量告警处理工作流
     */
    private void triggerQualityAlertWorkflow(QualityCheckCompletedEvent event) {
        log.info("触发质量告警处理工作流: checkId={}, assetId={}, score={}", 
                event.getCheckId(), event.getAssetId(), event.getOverallScore());
        
        // 根据得分和告警级别决定工作流类型
        // 严重告警需要立即处理
        // 一般告警可以批量处理
    }

    /**
     * 触发安全审批工作流
     */
    private void triggerSecurityApprovalWorkflow(SecurityClassifiedEvent event) {
        log.info("触发安全审批工作流: assetId={}, oldLevel={}, newLevel={}", 
                event.getAssetId(), event.getOldSecurityLevel(), event.getNewSecurityLevel());
        
        // 根据变更级别决定审批流程
    }

    /**
     * 资产创建事件
     */
    @lombok.Data
    public static class AssetCreatedEvent {
        private String eventId;
        private String assetId;
        private String assetName;
        private String assetType;
        private String owner;
        private String securityLevel;
        private java.util.List<String> tags;
        private String userId;
        private long timestamp;
    }

    /**
     * 质量检查完成事件
     */
    @lombok.Data
    public static class QualityCheckCompletedEvent {
        private String eventId;
        private String checkId;
        private String assetId;
        private String assetName;
        private String checkType;
        private Double overallScore;
        private Integer passedRules;
        private Integer failedRules;
        private Boolean alertTriggered;
        private String executedBy;
        private long timestamp;
    }

    /**
     * 安全级别变更事件
     */
    @lombok.Data
    public static class SecurityClassifiedEvent {
        private String eventId;
        private String assetId;
        private String assetName;
        private String oldSecurityLevel;
        private String newSecurityLevel;
        private String reason;
        private String userId;
        private long timestamp;
    }
}
