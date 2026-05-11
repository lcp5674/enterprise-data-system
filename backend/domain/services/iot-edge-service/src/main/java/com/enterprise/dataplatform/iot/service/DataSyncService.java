package com.enterprise.dataplatform.iot.service;

import com.enterprise.dataplatform.iot.config.MqttProperties;
import com.enterprise.dataplatform.iot.dto.DataSyncRequest;
import com.enterprise.dataplatform.iot.entity.DeviceData;
import com.enterprise.dataplatform.iot.entity.EdgeDevice;
import com.enterprise.dataplatform.iot.entity.SyncRecord;
import com.enterprise.dataplatform.iot.repository.DeviceDataRepository;
import com.enterprise.dataplatform.iot.repository.DeviceRepository;
import com.enterprise.dataplatform.iot.repository.SyncRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncService {

    private final DeviceDataRepository deviceDataRepository;
    private final DeviceRepository deviceRepository;
    private final SyncRecordRepository syncRecordRepository;
    private final MqttProperties mqttProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String HOT_SYNC_TOPIC = "iot.sync.hot";
    private static final String WARM_SYNC_TOPIC = "iot.sync.warm";
    private static final String COLD_SYNC_TOPIC = "iot.sync.cold";
    private static final String SYNC_LOCK_PREFIX = "iot:sync:lock:";

    @Transactional
    public SyncRecord syncDeviceData(String deviceId, SyncRecord.SyncType syncType, EdgeDevice.SyncPriority priority) {
        String lockKey = SYNC_LOCK_PREFIX + deviceId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 5, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(acquired)) {
            log.warn("Sync already in progress for device: {}", deviceId);
            return null;
        }

        SyncRecord record = SyncRecord.builder()
                .deviceId(deviceId)
                .syncType(syncType)
                .syncDirection(SyncRecord.SyncDirection.EDGE_TO_CENTER)
                .status(SyncRecord.Status.RUNNING)
                .priority(priority.name())
                .startedAt(LocalDateTime.now())
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            List<DeviceData> pendingData = getPendingDataForSync(deviceId, priority);

            if (pendingData.isEmpty()) {
                record.setStatus(SyncRecord.Status.COMPLETED);
                record.setRecordCount(0L);
                record.setCompletedAt(LocalDateTime.now());
                record.setDurationMs(0L);
                syncRecordRepository.save(record);
                return record;
            }

            long originalSize = calculateDataSize(pendingData);

            List<DeviceData> processedData = transformData(pendingData);

            byte[] compressedData = compressData(processedData);

            String kafkaTopic = getKafkaTopicForPriority(priority);

            String message = buildSyncMessage(deviceId, processedData);
            kafkaTemplate.send(kafkaTopic, deviceId, message).get();

            markDataAsSynced(pendingData);

            record.setRecordCount((long) pendingData.size());
            record.setDataSizeBytes(originalSize);
            record.setCompressedSizeBytes((long) compressedData.length);
            record.setCompressionRatio((double) compressedData.length / originalSize);
            record.setStatus(SyncRecord.Status.COMPLETED);
            record.setCompletedAt(LocalDateTime.now());
            record.setDurationMs(System.currentTimeMillis() - record.getStartedAt().atZone(
                    java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());

            syncRecordRepository.save(record);
            log.info("Data sync completed for device: {}, records: {}", deviceId, pendingData.size());

            return record;

        } catch (Exception e) {
            log.error("Data sync failed for device: {}", deviceId, e);
            record.setStatus(SyncRecord.Status.FAILED);
            record.setErrorMessage(e.getMessage());
            record.setCompletedAt(LocalDateTime.now());
            syncRecordRepository.save(record);
            throw new RuntimeException("Sync failed", e);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    public List<SyncRecord> syncAllDevices(DataSyncRequest request) {
        List<EdgeDevice> devices;

        if (request.getDeviceId() != null && !request.getDeviceId().isEmpty()) {
            EdgeDevice device = deviceRepository.findByDeviceId(request.getDeviceId());
            devices = device != null ? List.of(device) : Collections.emptyList();
        } else {
            devices = deviceRepository.findOnlineDevices();
        }

        List<SyncRecord> records = new ArrayList<>();

        for (EdgeDevice device : devices) {
            try {
                EdgeDevice.SyncPriority priority = device.getSyncPriority();
                if (priority == null) {
                    priority = EdgeDevice.SyncPriority.WARM;
                }

                SyncRecord.SyncType syncType = request.getSyncType() != null ?
                        SyncRecord.SyncType.valueOf(request.getSyncType()) : SyncRecord.SyncType.INCREMENTAL;

                SyncRecord record = syncDeviceData(device.getDeviceId(), syncType, priority);
                if (record != null) {
                    records.add(record);
                }
            } catch (Exception e) {
                log.error("Failed to sync device: {}", device.getDeviceId(), e);
            }
        }

        return records;
    }

    @Scheduled(fixedRate = 60000)
    public void scheduledHotDataSync() {
        log.debug("Running hot data sync scheduler");
        syncByPriority(EdgeDevice.SyncPriority.HOT);
    }

    @Scheduled(fixedRate = 300000)
    public void scheduledWarmDataSync() {
        log.debug("Running warm data sync scheduler");
        syncByPriority(EdgeDevice.SyncPriority.WARM);
    }

    @Scheduled(fixedRate = 900000)
    public void scheduledColdDataSync() {
        log.debug("Running cold data sync scheduler");
        syncByPriority(EdgeDevice.SyncPriority.COLD);
    }

    private void syncByPriority(EdgeDevice.SyncPriority priority) {
        List<EdgeDevice> devices = deviceRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EdgeDevice>()
                        .eq(EdgeDevice::getSyncPriority, priority)
                        .eq(EdgeDevice::getOnline, true)
                        .eq(EdgeDevice::getDeleted, false)
        );

        for (EdgeDevice device : devices) {
            try {
                syncDeviceData(device.getDeviceId(), SyncRecord.SyncType.INCREMENTAL, priority);
            } catch (Exception e) {
                log.error("Scheduled sync failed for device: {}", device.getDeviceId(), e);
            }
        }
    }

    private List<DeviceData> getPendingDataForSync(String deviceId, EdgeDevice.SyncPriority priority) {
        LocalDateTime threshold = switch (priority) {
            case HOT -> LocalDateTime.now().minusMinutes(1);
            case WARM -> LocalDateTime.now().minusMinutes(5);
            case COLD -> LocalDateTime.now().minusHours(1);
        };

        return deviceDataRepository.findByDeviceIdAndTimeRange(deviceId, threshold, LocalDateTime.now())
                .stream()
                .filter(d -> d.getSyncStatus() == DeviceData.SyncStatus.PENDING)
                .limit(1000)
                .collect(Collectors.toList());
    }

    private List<DeviceData> transformData(List<DeviceData> dataList) {
        return dataList.stream()
                .map(data -> {
                    data.setSyncStatus(DeviceData.SyncStatus.SYNCING);
                    return data;
                })
                .collect(Collectors.toList());
    }

    private byte[] compressData(List<DeviceData> dataList) {
        try {
            String json = objectMapper.writeValueAsString(dataList);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            java.util.zip.GZIPOutputStream gzipOut = new java.util.zip.GZIPOutputStream(baos);
            gzipOut.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            gzipOut.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to compress data", e);
            return new byte[0];
        }
    }

    private String buildSyncMessage(String deviceId, List<DeviceData> dataList) {
        Map<String, Object> message = new HashMap<>();
        message.put("deviceId", deviceId);
        message.put("timestamp", LocalDateTime.now().toString());
        message.put("recordCount", dataList.size());
        message.put("data", dataList);
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build sync message", e);
        }
    }

    private String getKafkaTopicForPriority(EdgeDevice.SyncPriority priority) {
        return switch (priority) {
            case HOT -> HOT_SYNC_TOPIC;
            case WARM -> WARM_SYNC_TOPIC;
            case COLD -> COLD_SYNC_TOPIC;
        };
    }

    private void markDataAsSynced(List<DeviceData> dataList) {
        LocalDateTime now = LocalDateTime.now();
        for (DeviceData data : dataList) {
            data.setSyncStatus(DeviceData.SyncStatus.SYNCED);
            data.setSyncTime(now);
            deviceDataRepository.save(data);
        }
    }

    private long calculateDataSize(List<DeviceData> dataList) {
        return dataList.stream()
                .mapToLong(d -> d.getDataValue() != null ? d.getDataValue().getBytes().length : 0)
                .sum();
    }

    public Map<String, Object> getSyncStatus(String deviceId) {
        Map<String, Object> status = new HashMap<>();

        List<SyncRecord> recentRecords = syncRecordRepository.findByDeviceId(deviceId)
                .stream()
                .sorted(Comparator.comparing(SyncRecord::getCreatedAt).reversed())
                .limit(10)
                .collect(Collectors.toList());

        long pendingCount = deviceDataRepository.countByDeviceIdAndSyncStatus(deviceId, DeviceData.SyncStatus.PENDING);

        status.put("recentSyncs", recentRecords);
        status.put("pendingRecords", pendingCount);
        status.put("lastSyncTime", recentRecords.isEmpty() ? null : recentRecords.get(0).getCompletedAt());

        return status;
    }

    @Transactional
    public void retryFailedSyncs(String deviceId, int maxRetries) {
        List<SyncRecord> failedRecords = syncRecordRepository.findByDeviceIdAndStatus(
                deviceId, SyncRecord.Status.FAILED);

        for (SyncRecord record : failedRecords) {
            if (record.getRetryCount() < maxRetries) {
                record.setRetryCount(record.getRetryCount() + 1);
                record.setStatus(SyncRecord.Status.PENDING);
                syncRecordRepository.save(record);

                try {
                    syncDeviceData(deviceId, record.getSyncType(),
                            EdgeDevice.SyncPriority.valueOf(record.getPriority()));
                } catch (Exception e) {
                    log.error("Retry sync failed for device: {}", deviceId, e);
                }
            }
        }
    }
}
