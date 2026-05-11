package com.enterprise.dataplatform.iot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.dataplatform.iot.config.MqttConfig;
import com.enterprise.dataplatform.iot.config.MqttProperties;
import com.enterprise.dataplatform.iot.dto.*;
import com.enterprise.dataplatform.iot.entity.DeviceGroup;
import com.enterprise.dataplatform.iot.entity.EdgeDevice;
import com.enterprise.dataplatform.iot.repository.DeviceGroupRepository;
import com.enterprise.dataplatform.iot.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceGroupRepository deviceGroupRepository;
    private final MqttConfig mqttConfig;
    private final MqttProperties mqttProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DEVICE_STATUS_KEY_PREFIX = "iot:device:status:";
    private static final String DEVICE_ONLINE_KEY_PREFIX = "iot:device:online:";
    private static final long DEVICE_HEARTBEAT_TIMEOUT_SECONDS = 180;

    @Transactional
    public DeviceResponse registerDevice(DeviceRegistrationRequest request) {
        if (deviceRepository.findByDeviceId(request.getDeviceName()) != null) {
            throw new IllegalArgumentException("Device already exists with name: " + request.getDeviceName());
        }

        String deviceId = generateDeviceId(request.getDeviceType());
        String authToken = generateAuthToken();

        EdgeDevice device = EdgeDevice.builder()
                .deviceId(deviceId)
                .deviceName(request.getDeviceName())
                .deviceType(request.getDeviceType())
                .manufacturer(request.getManufacturer())
                .model(request.getModel())
                .serialNumber(request.getSerialNumber())
                .firmwareVersion(request.getFirmwareVersion())
                .hardwareVersion(request.getHardwareVersion())
                .ipAddress(request.getIpAddress())
                .macAddress(request.getMacAddress())
                .location(request.getLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(EdgeDevice.DeviceStatus.INACTIVE)
                .online(false)
                .tags(request.getTags())
                .groups(request.getGroups())
                .properties(request.getProperties())
                .authToken(authToken)
                .authType(request.getAuthType() != null ? request.getAuthType() : EdgeDevice.AuthType.TOKEN)
                .capabilities(request.getCapabilities())
                .dataThreshold(request.getDataThreshold())
                .syncPriority(request.getSyncPriority() != null ? request.getSyncPriority() : EdgeDevice.SyncPriority.WARM)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        deviceRepository.insert(device);

        subscribeToDeviceTopics(deviceId);

        log.info("Device registered successfully: {}", deviceId);
        return toDeviceResponse(device);
    }

    public DeviceResponse getDeviceById(String deviceId) {
        EdgeDevice device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }
        return toDeviceResponse(device);
    }

    public DeviceResponse getDeviceByInternalId(Long id) {
        EdgeDevice device = deviceRepository.selectById(id);
        if (device == null) {
            throw new IllegalArgumentException("Device not found with id: " + id);
        }
        return toDeviceResponse(device);
    }

    public List<DeviceResponse> getAllDevices() {
        return deviceRepository.selectList(null).stream()
                .map(this::toDeviceResponse)
                .collect(Collectors.toList());
    }

    public IPage<DeviceResponse> getDevicesPage(int page, int size, String keyword,
                                                EdgeDevice.DeviceStatus status, String deviceType) {
        Page<EdgeDevice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<EdgeDevice> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(EdgeDevice::getDeviceName, keyword)
                    .or().like(EdgeDevice::getDeviceId, keyword)
                    .or().like(EdgeDevice::getSerialNumber, keyword));
        }
        if (status != null) {
            wrapper.eq(EdgeDevice::getStatus, status);
        }
        if (deviceType != null) {
            wrapper.eq(EdgeDevice::getDeviceType, deviceType);
        }
        wrapper.eq(EdgeDevice::getDeleted, false);
        wrapper.orderByDesc(EdgeDevice::getCreatedAt);

        IPage<EdgeDevice> devicePage = deviceRepository.selectPage(pageParam, wrapper);

        return devicePage.convert(this::toDeviceResponse);
    }

    @Transactional
    public DeviceResponse updateDevice(String deviceId, DeviceUpdateRequest request) {
        EdgeDevice device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }

        if (request.getDeviceName() != null) {
            device.setDeviceName(request.getDeviceName());
        }
        if (request.getDeviceType() != null) {
            device.setDeviceType(request.getDeviceType());
        }
        if (request.getManufacturer() != null) {
            device.setManufacturer(request.getManufacturer());
        }
        if (request.getModel() != null) {
            device.setModel(request.getModel());
        }
        if (request.getFirmwareVersion() != null) {
            device.setFirmwareVersion(request.getFirmwareVersion());
        }
        if (request.getHardwareVersion() != null) {
            device.setHardwareVersion(request.getHardwareVersion());
        }
        if (request.getLocation() != null) {
            device.setLocation(request.getLocation());
        }
        if (request.getLatitude() != null) {
            device.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            device.setLongitude(request.getLongitude());
        }
        if (request.getTags() != null) {
            device.setTags(request.getTags());
        }
        if (request.getProperties() != null) {
            device.setProperties(request.getProperties());
        }

        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.updateById(device);

        log.info("Device updated: {}", deviceId);
        return toDeviceResponse(device);
    }

    @Transactional
    public void deleteDevice(String deviceId) {
        EdgeDevice device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }

        unsubscribeFromDeviceTopics(deviceId);

        device.setDeleted(true);
        device.setStatus(EdgeDevice.DeviceStatus.INACTIVE);
        device.setOnline(false);
        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.updateById(device);

        redisTemplate.delete(DEVICE_STATUS_KEY_PREFIX + deviceId);
        redisTemplate.delete(DEVICE_ONLINE_KEY_PREFIX + deviceId);

        log.info("Device deleted: {}", deviceId);
    }

    @Transactional
    public boolean authenticateDevice(DeviceAuthRequest request) {
        EdgeDevice device = deviceRepository.findByDeviceId(request.getDeviceId());
        if (device == null) {
            log.warn("Authentication failed: device not found - {}", request.getDeviceId());
            return false;
        }

        if (!device.getAuthToken().equals(request.getAuthToken())) {
            log.warn("Authentication failed: invalid token for device - {}", request.getDeviceId());
            return false;
        }

        updateDeviceOnlineStatus(device.getDeviceId(), true);
        log.info("Device authenticated successfully: {}", request.getDeviceId());
        return true;
    }

    public void updateDeviceOnlineStatus(String deviceId, boolean online) {
        EdgeDevice device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            log.warn("Cannot update online status: device not found - {}", deviceId);
            return;
        }

        device.setOnline(online);
        device.setLastHeartbeat(LocalDateTime.now());
        if (online) {
            device.setStatus(EdgeDevice.DeviceStatus.ACTIVE);
        }
        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.updateById(device);

        String onlineKey = DEVICE_ONLINE_KEY_PREFIX + deviceId;
        if (online) {
            redisTemplate.opsForValue().set(onlineKey, true, DEVICE_HEARTBEAT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } else {
            redisTemplate.delete(onlineKey);
            device.setStatus(EdgeDevice.DeviceStatus.INACTIVE);
            deviceRepository.updateById(device);
        }
    }

    public void updateHeartbeat(String deviceId) {
        EdgeDevice device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            log.warn("Cannot update heartbeat: device not found - {}", deviceId);
            return;
        }

        device.setLastHeartbeat(LocalDateTime.now());
        device.setOnline(true);
        device.setStatus(EdgeDevice.DeviceStatus.ACTIVE);
        deviceRepository.updateById(device);

        String onlineKey = DEVICE_ONLINE_KEY_PREFIX + deviceId;
        redisTemplate.opsForValue().set(onlineKey, true, DEVICE_HEARTBEAT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public List<DeviceResponse> getDevicesByGroup(String groupId) {
        List<EdgeDevice> devices = deviceRepository.selectList(
                new LambdaQueryWrapper<EdgeDevice>()
                        .like(EdgeDevice::getGroups, groupId)
                        .eq(EdgeDevice::getDeleted, false)
        );
        return devices.stream().map(this::toDeviceResponse).collect(Collectors.toList());
    }

    public List<DeviceResponse> getDevicesByTag(String tagKey, String tagValue) {
        List<EdgeDevice> devices = deviceRepository.selectList(
                new LambdaQueryWrapper<EdgeDevice>()
                        .like(EdgeDevice::getTags, "\"" + tagKey + "\":\"" + tagValue + "\"")
                        .eq(EdgeDevice::getDeleted, false)
        );
        return devices.stream().map(this::toDeviceResponse).collect(Collectors.toList());
    }

    public List<DeviceResponse> getOnlineDevices() {
        return deviceRepository.findOnlineDevices().stream()
                .map(this::toDeviceResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getDeviceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDevices", deviceRepository.selectCount(null));
        stats.put("onlineDevices", deviceRepository.countOnlineDevices());
        stats.put("offlineDevices", deviceRepository.selectCount(
                new LambdaQueryWrapper<EdgeDevice>()
                        .eq(EdgeDevice::getOnline, false)
                        .eq(EdgeDevice::getDeleted, false)
        ));
        stats.put("activeDevices", deviceRepository.countByStatus(EdgeDevice.DeviceStatus.ACTIVE));
        stats.put("inactiveDevices", deviceRepository.countByStatus(EdgeDevice.DeviceStatus.INACTIVE));
        stats.put("maintenanceDevices", deviceRepository.countByStatus(EdgeDevice.DeviceStatus.MAINTENANCE));
        stats.put("faultDevices", deviceRepository.countByStatus(EdgeDevice.DeviceStatus.FAULT));
        return stats;
    }

    @Transactional
    public DeviceResponse updateDeviceStatus(String deviceId, EdgeDevice.DeviceStatus status) {
        EdgeDevice device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }

        device.setStatus(status);
        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.updateById(device);

        log.info("Device status updated: {} -> {}", deviceId, status);
        return toDeviceResponse(device);
    }

    @Transactional
    public String regenerateAuthToken(String deviceId) {
        EdgeDevice device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }

        String newToken = generateAuthToken();
        device.setAuthToken(newToken);
        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.updateById(device);

        log.info("Auth token regenerated for device: {}", deviceId);
        return newToken;
    }

    @Transactional
    public DeviceResponse addDeviceToGroup(String deviceId, String groupId) {
        EdgeDevice device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }

        List<String> groups = device.getGroups();
        if (groups == null) {
            groups = new ArrayList<>();
        }
        if (!groups.contains(groupId)) {
            groups.add(groupId);
            device.setGroups(groups);
            device.setUpdatedAt(LocalDateTime.now());
            deviceRepository.updateById(device);
        }

        return toDeviceResponse(device);
    }

    @Transactional
    public DeviceResponse removeDeviceFromGroup(String deviceId, String groupId) {
        EdgeDevice device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }

        List<String> groups = device.getGroups();
        if (groups != null) {
            groups.remove(groupId);
            device.setGroups(groups);
            device.setUpdatedAt(LocalDateTime.now());
            deviceRepository.updateById(device);
        }

        return toDeviceResponse(device);
    }

    @Transactional
    public DeviceResponse updateDeviceTags(String deviceId, Map<String, String> tags) {
        EdgeDevice device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }

        device.setTags(tags);
        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.updateById(device);

        return toDeviceResponse(device);
    }

    @Scheduled(fixedRate = 60000)
    public void checkDeviceHeartbeats() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(DEVICE_HEARTBEAT_TIMEOUT_SECONDS);
        List<EdgeDevice> offlineDevices = deviceRepository.findOfflineDevicesByHeartbeat(threshold);

        for (EdgeDevice device : offlineDevices) {
            device.setOnline(false);
            device.setStatus(EdgeDevice.DeviceStatus.INACTIVE);
            device.setUpdatedAt(LocalDateTime.now());
            deviceRepository.updateById(device);

            redisTemplate.delete(DEVICE_ONLINE_KEY_PREFIX + device.getDeviceId());

            log.info("Device marked offline due to heartbeat timeout: {}", device.getDeviceId());
        }
    }

    private void subscribeToDeviceTopics(String deviceId) {
        String dataTopic = mqttProperties.getDataTopic() + "/" + deviceId + "/#";
        String commandTopic = mqttProperties.getCommandTopic() + "/" + deviceId;

        mqttConfig.subscribe(dataTopic, mqttProperties.getQos(),
                (topic, payload, qos) -> handleDeviceData(deviceId, payload, qos));
        mqttConfig.subscribe(commandTopic, mqttProperties.getQos(),
                (topic, payload, qos) -> handleDeviceCommand(deviceId, payload, qos));
    }

    private void unsubscribeFromDeviceTopics(String deviceId) {
        log.info("Unsubscribing device {} from MQTT topics", deviceId);
    }

    private void handleDeviceData(String deviceId, byte[] payload, int qos) {
        log.debug("Received data from device: {}, QoS: {}", deviceId, qos);
        updateHeartbeat(deviceId);
    }

    private void handleDeviceCommand(String deviceId, byte[] payload, int qos) {
        log.debug("Received command for device: {}, QoS: {}", deviceId, qos);
    }

    private String generateDeviceId(String deviceType) {
        return deviceType.toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private DeviceResponse toDeviceResponse(EdgeDevice device) {
        return DeviceResponse.builder()
                .id(device.getId().toString())
                .deviceId(device.getDeviceId())
                .deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType())
                .manufacturer(device.getManufacturer())
                .model(device.getModel())
                .serialNumber(device.getSerialNumber())
                .firmwareVersion(device.getFirmwareVersion())
                .hardwareVersion(device.getHardwareVersion())
                .status(device.getStatus())
                .online(device.isOnline())
                .lastHeartbeat(device.getLastHeartbeat())
                .ipAddress(device.getIpAddress())
                .macAddress(device.getMacAddress())
                .location(device.getLocation())
                .latitude(device.getLatitude())
                .longitude(device.getLongitude())
                .tags(device.getTags())
                .groups(device.getGroups())
                .properties(device.getProperties())
                .authType(device.getAuthType())
                .capabilities(device.getCapabilities())
                .dataThreshold(device.getDataThreshold())
                .syncPriority(device.getSyncPriority())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}
