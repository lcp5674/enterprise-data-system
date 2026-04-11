package com.enterprise.dataplatform.index.kafka;

import com.enterprise.dataplatform.index.document.AssetIndexDocument;
import com.enterprise.dataplatform.index.service.IndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka事件消费者 - 监听资产变更事件并同步ES索引
 *
 * @author EDAMS Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssetEventConsumer {

    private final IndexService indexService;

    @KafkaListener(topics = "dam.asset.created", groupId = "index-service")
    public void handleAssetCreated(String message) {
        log.info("Received asset created event: {}", message);
        try {
            AssetEvent event = parseEvent(message);
            if (event != null) {
                // 从metadata-service获取完整资产信息后索引
                AssetIndexDocument doc = buildIndexDocument(event);
                indexService.indexAsset(doc);
                log.info("Asset indexed from event: {}", event.getAssetId());
            }
        } catch (Exception e) {
            log.error("Failed to handle asset created event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "dam.asset.updated", groupId = "index-service")
    public void handleAssetUpdated(String message) {
        log.info("Received asset updated event: {}", message);
        try {
            AssetEvent event = parseEvent(message);
            if (event != null) {
                AssetIndexDocument doc = buildIndexDocument(event);
                indexService.indexAsset(doc);
                log.info("Asset re-indexed from event: {}", event.getAssetId());
            }
        } catch (Exception e) {
            log.error("Failed to handle asset updated event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "dam.asset.deleted", groupId = "index-service")
    public void handleAssetDeleted(String message) {
        log.info("Received asset deleted event: {}", message);
        try {
            AssetEvent event = parseEvent(message);
            if (event != null) {
                indexService.deleteIndex(event.getAssetId());
                log.info("Asset deleted from index: {}", event.getAssetId());
            }
        } catch (Exception e) {
            log.error("Failed to handle asset deleted event: {}", e.getMessage(), e);
        }
    }

    private AssetEvent parseEvent(String message) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(message, AssetEvent.class);
        } catch (Exception e) {
            log.warn("Failed to parse event message: {}", message);
            return null;
        }
    }

    private AssetIndexDocument buildIndexDocument(AssetEvent event) {
        return AssetIndexDocument.builder()
                .id(event.getAssetId())
                .assetId(event.getAssetId())
                .name(event.getAssetName() != null ? event.getAssetName() : "Unknown")
                .description("")
                .domainCode(event.getDomainCode() != null ? event.getDomainCode() : "")
                .objectType(event.getAssetType() != null ? event.getAssetType() : "TABLE")
                .tags("[]")
                .owner(event.getOwner() != null ? event.getOwner() : "")
                .status(event.getStatus() != null ? event.getStatus() : "PUBLISHED")
                .build();
    }

    /**
     * 内部类：解析Kafka消息
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AssetEvent {
        private String eventType;
        private String assetId;
        private String assetName;
        private String assetCode;
        private String assetType;
        private String domainCode;
        private String owner;
        private String status;
        private String timestamp;
    }
}
