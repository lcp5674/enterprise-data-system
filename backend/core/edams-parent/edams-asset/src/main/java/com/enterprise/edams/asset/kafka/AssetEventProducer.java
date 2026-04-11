package com.enterprise.edams.asset.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 资产事件Kafka生产者
 * 用于资产服务发布资产变更事件
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssetEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.asset.metadata.created:asset.metadata.created}")
    private String assetCreatedTopic;

    @Value("${kafka.topics.asset.metadata.updated:asset.metadata.updated}")
    private String assetUpdatedTopic;

    @Value("${kafka.topics.asset.metadata.deleted:asset.metadata.deleted}")
    private String assetDeletedTopic;

    @Value("${kafka.topics.asset.lineage.changed:asset.lineage.changed}")
    private String lineageChangedTopic;

    /**
     * 发送资产创建事件
     */
    public void sendAssetCreatedEvent(AssetEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            CompletableFuture<SendResult<String, String>> future = 
                    kafkaTemplate.send(assetCreatedTopic, event.getAssetId(), message);
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("发送资产创建事件失败: assetId={}, error={}", 
                            event.getAssetId(), ex.getMessage());
                } else {
                    log.info("资产创建事件发送成功: assetId={}, topic={}, partition={}, offset={}",
                            event.getAssetId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("序列化资产创建事件失败: assetId={}", event.getAssetId(), e);
        }
    }

    /**
     * 发送资产更新事件
     */
    public void sendAssetUpdatedEvent(AssetEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            CompletableFuture<SendResult<String, String>> future = 
                    kafkaTemplate.send(assetUpdatedTopic, event.getAssetId(), message);
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("发送资产更新事件失败: assetId={}, error={}", 
                            event.getAssetId(), ex.getMessage());
                } else {
                    log.info("资产更新事件发送成功: assetId={}, topic={}, partition={}, offset={}",
                            event.getAssetId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("序列化资产更新事件失败: assetId={}", event.getAssetId(), e);
        }
    }

    /**
     * 发送资产删除事件
     */
    public void sendAssetDeletedEvent(AssetEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            CompletableFuture<SendResult<String, String>> future = 
                    kafkaTemplate.send(assetDeletedTopic, event.getAssetId(), message);
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("发送资产删除事件失败: assetId={}, error={}", 
                            event.getAssetId(), ex.getMessage());
                } else {
                    log.info("资产删除事件发送成功: assetId={}, topic={}, partition={}, offset={}",
                            event.getAssetId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("序列化资产删除事件失败: assetId={}", event.getAssetId(), e);
        }
    }

    /**
     * 发送血缘变更事件
     */
    public void sendLineageChangedEvent(LineageEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            String key = event.getSourceAssetId() + "_" + event.getTargetAssetId();
            CompletableFuture<SendResult<String, String>> future = 
                    kafkaTemplate.send(lineageChangedTopic, key, message);
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("发送血缘变更事件失败: sourceAssetId={}, targetAssetId={}, error={}", 
                            event.getSourceAssetId(), event.getTargetAssetId(), ex.getMessage());
                } else {
                    log.info("血缘变更事件发送成功: sourceAssetId={}, topic={}, partition={}, offset={}",
                            event.getSourceAssetId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("序列化血缘变更事件失败: sourceAssetId={}", event.getSourceAssetId(), e);
        }
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
        private String operation;
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
