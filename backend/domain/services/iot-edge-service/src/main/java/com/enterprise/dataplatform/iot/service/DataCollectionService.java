package com.enterprise.dataplatform.iot.service;

import com.enterprise.dataplatform.iot.entity.DeviceData;
import com.enterprise.dataplatform.iot.entity.EdgeDevice;
import com.enterprise.dataplatform.iot.repository.DeviceDataRepository;
import com.enterprise.dataplatform.iot.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataCollectionService {

    private final DeviceDataRepository deviceDataRepository;
    private final DeviceRepository deviceRepository;
    private final DataCollectionConfig config;
    private final AlertService alertService;

    private final Map<String, DataTransformer> transformers = new ConcurrentHashMap<>();
    private final Map<String, List<DeviceData>> tempStorage = new ConcurrentHashMap<>();

    public void registerTransformer(String dataType, DataTransformer transformer) {
        transformers.put(dataType, transformer);
        log.info("Registered transformer for data type: {}", dataType);
    }

    public void collectData(String deviceId, String dataType, Object rawData) {
        EdgeDevice device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            log.warn("Cannot collect data: device not found - {}", deviceId);
            return;
        }

        try {
            Object transformedData = transformData(dataType, rawData);

            DeviceData data = buildDeviceData(deviceId, dataType, transformedData);

            if (validateData(data, device)) {
                storeTempData(deviceId, data);
                processDataIfThresholdReached(deviceId);
            }

        } catch (Exception e) {
            log.error("Data collection failed for device: {}, type: {}", deviceId, dataType, e);
            handleCollectionError(deviceId, dataType, e);
        }
    }

    public void collectBatchData(String deviceId, List<Map<String, Object>> rawDataList) {
        for (Map<String, Object> rawData : rawDataList) {
            String dataType = (String) rawData.get("dataType");
            Object data = rawData.get("data");
            collectData(deviceId, dataType, data);
        }
    }

    private Object transformData(String dataType, Object rawData) {
        DataTransformer transformer = transformers.get(dataType);
        if (transformer != null) {
            return transformer.transform(rawData);
        }
        return rawData;
    }

    private DeviceData buildDeviceData(String deviceId, String dataType, Object data) {
        return DeviceData.builder()
                .deviceId(deviceId)
                .dataType(dataType)
                .dataKey(generateDataKey(dataType))
                .dataValue(data instanceof String ? (String) data : data.toString())
                .timestamp(LocalDateTime.now())
                .quality(DeviceData.DataQuality.GOOD)
                .syncStatus(DeviceData.SyncStatus.PENDING)
                .compressed(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private boolean validateData(DeviceData data, EdgeDevice device) {
        EdgeDevice.DataThreshold threshold = device.getDataThreshold();
        if (threshold == null) {
            return true;
        }

        try {
            double value = Double.parseDouble(data.getDataValue());

            if (threshold.getMinValue() != null && value < threshold.getMinValue()) {
                alertService.sendAlert(device.getDeviceId(), AlertService.AlertLevel.WARNING,
                        "Data value below threshold: " + value);
                data.setQuality(DeviceData.DataQuality.UNCERTAIN);
            }

            if (threshold.getMaxValue() != null && value > threshold.getMaxValue()) {
                alertService.sendAlert(device.getDeviceId(), AlertService.AlertLevel.WARNING,
                        "Data value above threshold: " + value);
                data.setQuality(DeviceData.DataQuality.UNCERTAIN);
            }

            return true;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private void storeTempData(String deviceId, DeviceData data) {
        tempStorage.computeIfAbsent(deviceId, k -> Collections.synchronizedList(new ArrayList<>())).add(data);
    }

    private void processDataIfThresholdReached(String deviceId) {
        List<DeviceData> dataList = tempStorage.get(deviceId);
        if (dataList == null) {
            return;
        }

        int batchSize = config.getBatchSize();
        if (dataList.size() >= batchSize) {
            flushTempData(deviceId);
        }
    }

    @Scheduled(fixedRateString = "#{@dataCollectionConfig.flushIntervalMs}")
    public void scheduledFlush() {
        Set<String> deviceIds = new HashSet<>(tempStorage.keySet());
        for (String deviceId : deviceIds) {
            try {
                flushTempData(deviceId);
            } catch (Exception e) {
                log.error("Scheduled flush failed for device: {}", deviceId, e);
            }
        }
    }

    private synchronized void flushTempData(String deviceId) {
        List<DeviceData> dataList = tempStorage.remove(deviceId);
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        try {
            deviceDataRepository.saveAll(dataList);
            log.info("Flushed {} data records for device: {}", dataList.size(), deviceId);
        } catch (Exception e) {
            log.error("Failed to flush data for device: {}", deviceId, e);
            handleCollectionError(deviceId, "BATCH", e);
        }
    }

    private void handleCollectionError(String deviceId, String dataType, Exception e) {
        alertService.sendAlert(deviceId, AlertService.AlertLevel.ERROR,
                "Data collection error: " + e.getMessage());
    }

    private String generateDataKey(String dataType) {
        return dataType + "-" + System.currentTimeMillis();
    }

    public List<DeviceData> getRecentData(String deviceId, int limit) {
        return deviceDataRepository.findByDeviceId(deviceId)
                .stream()
                .sorted(Comparator.comparing(DeviceData::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void cleanupOldData(int retentionDays) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        List<DeviceData> allData = deviceDataRepository.findAll();

        for (DeviceData data : allData) {
            if (data.getCreatedAt().isBefore(threshold) &&
                    data.getSyncStatus() == DeviceData.SyncStatus.SYNCED) {
                deviceDataRepository.delete(data);
            }
        }

        log.info("Cleaned up old device data older than {} days", retentionDays);
    }

    public interface DataTransformer {
        Object transform(Object rawData);
    }

    @lombok.Component
    @lombok.RequiredArgsConstructor
    public static class DefaultDataCollectionConfig {
        private final DataCollectionProperties properties;

        public int getBatchSize() {
            return properties.getBatchSize();
        }

        public long getFlushIntervalMs() {
            return properties.getFlushIntervalMs();
        }
    }
}
