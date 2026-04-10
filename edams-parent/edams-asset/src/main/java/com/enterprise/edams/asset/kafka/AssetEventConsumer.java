package com.enterprise.edams.asset.kafka;

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
 * 资产事件Kafka消费者
 * 消费资产变更事件，触发后续业务处理
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssetEventConsumer {

    private final ObjectMapper objectMapper;

    /**
     * 消费资产元数据创建事件
     */
    @KafkaListener(
        topics = "${kafka.topics.asset.metadata.created:asset.metadata.created}",
        groupId = "${kafka.groups.asset-indexer:asset-indexer}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAssetCreatedEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("消费资产创建事件: topic={}, partition={}, offset={}", topic, partition, offset);
            
            AssetEvent event = objectMapper.readValue(message, AssetEvent.class);
            
            // 处理资产创建事件
            handleAssetCreated(event);
            
            acknowledgment.acknowledge();
            log.debug("资产创建事件处理完成: assetId={}", event.getAssetId());
        } catch (Exception e) {
            log.error("处理资产创建事件失败: message={}", message, e);
            // 不确认消息，等待重试
        }
    }

    /**
     * 消费资产元数据更新事件
     */
    @KafkaListener(
        topics = "${kafka.topics.asset.metadata.updated:asset.metadata.updated}",
        groupId = "${kafka.groups.asset-indexer:asset-indexer}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAssetUpdatedEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("消费资产更新事件: topic={}, partition={}, offset={}", topic, partition, offset);
            
            AssetEvent event = objectMapper.readValue(message, AssetEvent.class);
            
            // 处理资产更新事件
            handleAssetUpdated(event);
            
            acknowledgment.acknowledge();
            log.debug("资产更新事件处理完成: assetId={}", event.getAssetId());
        } catch (Exception e) {
            log.error("处理资产更新事件失败: message={}", message, e);
        }
    }

    /**
     * 消费资产删除事件
     */
    @KafkaListener(
        topics = "${kafka.topics.asset.metadata.deleted:asset.metadata.deleted}",
        groupId = "${kafka.groups.asset-indexer:asset-indexer}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAssetDeletedEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("消费资产删除事件: topic={}, partition={}, offset={}", topic, partition, offset);
            
            AssetEvent event = objectMapper.readValue(message, AssetEvent.class);
            
            // 处理资产删除事件
            handleAssetDeleted(event);
            
            acknowledgment.acknowledge();
            log.debug("资产删除事件处理完成: assetId={}", event.getAssetId());
        } catch (Exception e) {
            log.error("处理资产删除事件失败: message={}", message, e);
        }
    }

    /**
     * 消费资产血缘变更事件
     */
    @KafkaListener(
        topics = "${kafka.topics.asset.lineage.changed:asset.lineage.changed}",
        groupId = "${kafka.groups.lineage-processor:lineage-processor}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLineageChangedEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        try {
            log.info("消费血缘变更事件: topic={}, partition={}, offset={}", topic, partition, offset);
            
            LineageEvent event = objectMapper.readValue(message, LineageEvent.class);
            
            // 处理血缘变更事件
            handleLineageChanged(event);
            
            acknowledgment.acknowledge();
            log.debug("血缘变更事件处理完成: sourceAssetId={}", event.getSourceAssetId());
        } catch (Exception e) {
            log.error("处理血缘变更事件失败: message={}", message, e);
        }
    }

    /**
     * 处理资产创建事件
     */
    private void handleAssetCreated(AssetEvent event) {
        log.info("处理资产创建: assetId={}, assetName={}, assetType={}", 
                event.getAssetId(), event.getAssetName(), event.getAssetType());
        
        // 1. 更新搜索索引（异步）
        // 2. 发送资产创建通知
        // 3. 记录审计日志
    }

    /**
     * 处理资产更新事件
     */
    private void handleAssetUpdated(AssetEvent event) {
        log.info("处理资产更新: assetId={}, assetName={}", 
                event.getAssetId(), event.getAssetName());
        
        // 1. 更新搜索索引
        // 2. 如果安全级别变更，发送告警
        // 3. 记录审计日志
    }

    /**
     * 处理资产删除事件
     */
    private void handleAssetDeleted(AssetEvent event) {
        log.info("处理资产删除: assetId={}", event.getAssetId());
        
        // 1. 删除搜索索引
        // 2. 检查下游血缘影响
        // 3. 发送通知
        // 4. 记录审计日志
    }

    /**
     * 处理血缘变更事件
     */
    private void handleLineageChanged(LineageEvent event) {
        log.info("处理血缘变更: sourceAssetId={}, targetAssetId={}, operation={}", 
                event.getSourceAssetId(), event.getTargetAssetId(), event.getOperation());
        
        // 1. 更新血缘图谱
        // 2. 检查是否影响关键资产
        // 3. 发送通知
    }

    /**
     * 资产事件
     */
    @lombok.Data
    public static class AssetEvent {
        private String eventId;
        private String eventType;
        private String assetId;
        private String assetName;
        private String assetType;
        private String sourceId;
        private String database;
        private String schema;
        private String owner;
        private String securityLevel;
        private java.util.List<String> tags;
        private String userId;
        private String tenantId;
        private long timestamp;
    }

    /**
     * 血缘变更事件
     */
    @lombok.Data
    public static class LineageEvent {
        private String eventId;
        private String operation; // upsert, delete
        private String sourceAssetId;
        private String sourceAssetName;
        private String sourceAssetType;
        private String targetAssetId;
        private String targetAssetName;
        private String targetAssetType;
        private java.util.List<FieldLineage> fieldLineage;
        private String transformation;
        private String jobId;
        private long timestamp;
    }

    /**
     * 字段级血缘
     */
    @lombok.Data
    public static class FieldLineage {
        private String sourceField;
        private String targetField;
    }
}
