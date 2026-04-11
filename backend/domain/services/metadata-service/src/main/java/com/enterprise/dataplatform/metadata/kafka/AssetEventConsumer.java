package com.enterprise.dataplatform.metadata.kafka;

import com.enterprise.dataplatform.metadata.service.MetadataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka Consumer for Asset Events
 * Listens to asset creation and update events from other services
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssetEventConsumer {

    private final MetadataService metadataService;
    private final ObjectMapper objectMapper;

    /**
     * Listen to asset created events
     */
    @KafkaListener(topics = "dam.asset.created", groupId = "metadata-service-asset-consumer")
    public void handleAssetCreated(String message) {
        log.info("Received asset created event: {}", message);
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String assetId = (String) event.get("assetId");
            Map<String, Object> assetInfo = (Map<String, Object>) event.get("assetInfo");

            if (assetId != null && assetInfo != null) {
                metadataService.syncFromAsset(assetId, assetInfo);
                log.info("Successfully synced metadata for asset: {}", assetId);
            } else {
                log.warn("Invalid asset created event - missing assetId or assetInfo");
            }
        } catch (Exception e) {
            log.error("Failed to process asset created event", e);
        }
    }

    /**
     * Listen to asset updated events
     */
    @KafkaListener(topics = "dam.asset.updated", groupId = "metadata-service-asset-consumer")
    public void handleAssetUpdated(String message) {
        log.info("Received asset updated event: {}", message);
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String assetId = (String) event.get("assetId");
            Map<String, Object> assetInfo = (Map<String, Object>) event.get("assetInfo");

            if (assetId != null && assetInfo != null) {
                metadataService.syncFromAsset(assetId, assetInfo);
                log.info("Successfully updated metadata for asset: {}", assetId);
            } else {
                log.warn("Invalid asset updated event - missing assetId or assetInfo");
            }
        } catch (Exception e) {
            log.error("Failed to process asset updated event", e);
        }
    }
}
