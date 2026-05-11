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

import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
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
    private final AlertService alertService;

    private static final String DEVICE_STATUS_KEY_PREFIX = "iot:device:status:";
    private static final String DEVICE_ONLINE_KEY_PREFIX = "iot:device:online:";
    private static final long DEVICE_HEARTBEAT_TIMEOUT_SECONDS = 180;
    private static final int MAX_AUTH_FAILURE_COUNT = 5;
    private static final int MAX_RECONNECT_COUNT = 3;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    private static final int RETRY_INTERVAL_MINUTES = 5;

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
    public AuthenticationResult authenticateDevice(String deviceId, AuthenticationRequest request) {
        log.info("开始设备认证: deviceId={}, authType={}", deviceId, request.getAuthType());

        EdgeDevice device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("设备不存在: " + deviceId));

        AuthenticationResult.AuthenticationResultBuilder resultBuilder = AuthenticationResult.builder();

        if (device.getStatus() == EdgeDevice.DeviceStatus.INACTIVE) {
            log.warn("设备认证失败-设备已停用: deviceId={}", deviceId);
            return resultBuilder
                    .success(false)
                    .errorCode("DEVICE_INACTIVE")
                    .errorMessage("设备已停用，请联系管理员启用")
                    .build();
        }

        if (device.getStatus() == EdgeDevice.DeviceStatus.FAULT) {
            log.warn("设备认证失败-设备故障: deviceId={}", deviceId);
            return resultBuilder
                    .success(false)
                    .errorCode("DEVICE_FAULT")
                    .errorMessage("设备处于故障状态，请先修复设备问题")
                    .build();
        }

        if (device.getStatus() == EdgeDevice.DeviceStatus.MAINTENANCE) {
            log.warn("设备认证失败-设备维护中: deviceId={}", deviceId);
            return resultBuilder
                    .success(false)
                    .errorCode("DEVICE_MAINTENANCE")
                    .errorMessage("设备正在维护中，请稍后再试")
                    .build();
        }

        boolean authenticated = false;
        String errorMessage = null;

        try {
            switch (device.getAuthType()) {
                case TOKEN:
                    authenticated = authenticateWithToken(device, request.getToken());
                    break;
                case CERTIFICATE:
                    authenticated = authenticateWithCertificate(device, request.getCertificate(), request.getCertificateSignature());
                    break;
                case API_KEY:
                    authenticated = authenticateWithApiKey(device, request.getApiKey());
                    break;
                case USERNAME_PASSWORD:
                    authenticated = authenticateWithPassword(device, request.getUsername(), request.getPassword());
                    break;
                default:
                    log.error("不支持的认证类型: authType={}", device.getAuthType());
                    return resultBuilder
                            .success(false)
                            .errorCode("UNSUPPORTED_AUTH_TYPE")
                            .errorMessage("不支持的认证方式: " + device.getAuthType())
                            .build();
            }
        } catch (Exception e) {
            log.error("设备认证执行异常: deviceId={}", deviceId, e);
            authenticated = false;
            errorMessage = "认证执行异常: " + e.getMessage();
        }

        if (authenticated) {
            String accessToken = generateAccessToken(device);
            String refreshToken = generateRefreshToken(device);

            device.setLastAuthTime(LocalDateTime.now());
            device.setAuthFailureCount(0);
            device.setStatus(EdgeDevice.DeviceStatus.ACTIVE);
            device.setLastConnectedTime(LocalDateTime.now());
            deviceRepository.updateById(device);

            log.info("设备认证成功: deviceId={}", deviceId);

            return resultBuilder
                    .success(true)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(3600)
                    .tokenType("Bearer")
                    .deviceId(deviceId)
                    .build();

        } else {
            int failureCount = device.getAuthFailureCount() + 1;
            device.setAuthFailureCount(failureCount);

            if (failureCount >= MAX_AUTH_FAILURE_COUNT) {
                device.setStatus(EdgeDevice.DeviceStatus.FAULT);
                device.setLastErrorMessage("认证失败次数过多，设备已锁定");
                log.warn("设备认证失败次数过多，已标记为故障: deviceId={}, failureCount={}", deviceId, failureCount);

                alertService.sendDeviceAlert(device, AlertService.AlertType.SECURITY,
                        "设备认证失败过多，已自动锁定");
            }

            deviceRepository.updateById(device);

            int remainingAttempts = MAX_AUTH_FAILURE_COUNT - failureCount;

            log.warn("设备认证失败: deviceId={}, failureCount={}, remainingAttempts={}", deviceId, failureCount, remainingAttempts);

            return resultBuilder
                    .success(false)
                    .errorCode("AUTHENTICATION_FAILED")
                    .errorMessage(errorMessage != null ? errorMessage : "认证失败")
                    .remainingAttempts(Math.max(0, remainingAttempts))
                    .lockoutUntil(failureCount >= MAX_AUTH_FAILURE_COUNT ? LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES) : null)
                    .build();
        }
    }

    private boolean authenticateWithToken(EdgeDevice device, String token) {
        if (token == null || token.isEmpty()) {
            log.warn("Token认证失败-Token为空: deviceId={}", device.getDeviceId());
            return false;
        }

        try {
            String decodedToken = new String(Base64.getDecoder().decode(token));
            String expectedToken = device.getAuthToken();

            if (decodedToken.equals(expectedToken)) {
                return true;
            }

            log.warn("Token认证失败-Token不匹配: deviceId={}", device.getDeviceId());
            return false;

        } catch (Exception e) {
            log.warn("Token认证失败-Token格式错误: deviceId={}, error={}", device.getDeviceId(), e.getMessage());
            return false;
        }
    }

    private boolean authenticateWithCertificate(EdgeDevice device, String certificate, String signature) {
        if (certificate == null || certificate.isEmpty() || signature == null || signature.isEmpty()) {
            log.warn("证书认证失败-证书或签名为空: deviceId={}", device.getDeviceId());
            return false;
        }

        try {
            PublicKey publicKey = device.getCredentialsPublicKey();
            if (publicKey == null) {
                log.warn("证书认证失败-设备公钥未配置: deviceId={}", device.getDeviceId());
                return false;
            }

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update((device.getDeviceId() + ":" + System.currentTimeMillis()).getBytes());

            boolean verified = sig.verify(Base64.getDecoder().decode(signature));

            if (!verified) {
                log.warn("证书认证失败-签名验证失败: deviceId={}", device.getDeviceId());
            }

            return verified;

        } catch (Exception e) {
            log.error("证书认证异常: deviceId={}", device.getDeviceId(), e);
            return false;
        }
    }

    private boolean authenticateWithApiKey(EdgeDevice device, String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API Key认证失败-Key为空: deviceId={}", device.getDeviceId());
            return false;
        }

        String expectedApiKey = device.getAuthToken();

        if (MessageDigest.isEqual(
                apiKey.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                expectedApiKey.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            return true;
        }

        log.warn("API Key认证失败-Key不匹配: deviceId={}", device.getDeviceId());
        return false;
    }

    private boolean authenticateWithPassword(EdgeDevice device, String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            log.warn("用户名密码认证失败-用户名或密码为空: deviceId={}", device.getDeviceId());
            return false;
        }

        if (!username.equals(device.getDeviceId())) {
            log.warn("用户名密码认证失败-用户名不匹配: deviceId={}", device.getDeviceId());
            return false;
        }

        String storedPassword = device.getAuthToken();
        try {
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = 
                    new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            return encoder.matches(password, storedPassword);
        } catch (Exception e) {
            log.error("密码验证异常: deviceId={}", device.getDeviceId(), e);
            return false;
        }
    }

    private String generateAccessToken(EdgeDevice device) {
        String tokenData = device.getDeviceId() + ":" + System.currentTimeMillis() + ":" + device.getDeviceId().hashCode();
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
    }

    private String generateRefreshToken(EdgeDevice device) {
        String tokenData = "refresh:" + device.getDeviceId() + ":" + System.currentTimeMillis() + ":" + UUID.randomUUID().toString();
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
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

        if (offlineDevices.isEmpty()) {
            log.debug("无离线设备");
            return;
        }

        log.info("检测到离线设备: count={}", offlineDevices.size());

        for (EdgeDevice device : offlineDevices) {
            try {
                processOfflineDevice(device);
            } catch (Exception e) {
                log.error("处理离线设备异常: deviceId={}", device.getDeviceId(), e);
            }
        }

        log.info("心跳检查完成: offline={}", offlineDevices.size());
    }

    private void processOfflineDevice(EdgeDevice device) {
        EdgeDevice.DeviceStatus previousStatus = device.getStatus();

        if (isInMaintenancePeriod(device)) {
            log.debug("设备在维护期间，跳过状态变更: deviceId={}, until={}", 
                    device.getDeviceId(), device.getMaintenanceUntil());
            return;
        }

        int reconnectCount = device.getReconnectCount() != null ? device.getReconnectCount() : 0;
        int failureCount = device.getAuthFailureCount() != null ? device.getAuthFailureCount() : 0;

        if (reconnectCount >= MAX_RECONNECT_COUNT || failureCount >= MAX_AUTH_FAILURE_COUNT) {
            device.setStatus(EdgeDevice.DeviceStatus.FAULT);
            device.setLastErrorMessage(String.format("心跳超时: 重连%d次, 认证失败%d次", reconnectCount, failureCount));

            alertService.sendDeviceAlert(device, AlertService.AlertType.DEVICE_FAULT,
                    String.format("设备心跳超时，已达到最大重连次数(%d)或认证失败次数(%d)，已自动标记为故障",
                            reconnectCount, failureCount));

            log.warn("设备标记为故障: deviceId={}, reconnectCount={}, authFailureCount={}",
                    device.getDeviceId(), reconnectCount, failureCount);

        } else {
            device.setOnline(false);
            device.setStatus(EdgeDevice.DeviceStatus.INACTIVE);
            device.setOfflineTime(LocalDateTime.now());

            LocalDateTime estimatedRecovery = LocalDateTime.now()
                    .plusSeconds(DEVICE_HEARTBEAT_TIMEOUT_SECONDS * (MAX_RECONNECT_COUNT - reconnectCount));
            device.setEstimatedRecoveryTime(estimatedRecovery);

            log.info("设备标记为离线: deviceId={}, reconnectCount={}/{}, estimatedRecovery={}",
                    device.getDeviceId(), reconnectCount, MAX_RECONNECT_COUNT, estimatedRecovery);
        }

        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.updateById(device);

        redisTemplate.delete(DEVICE_ONLINE_KEY_PREFIX + device.getDeviceId());
    }

    private boolean isInMaintenancePeriod(EdgeDevice device) {
        LocalDateTime maintenanceUntil = device.getMaintenanceUntil();
        if (maintenanceUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(maintenanceUntil);
    }

    public void handleDeviceReconnected(String deviceId) {
        EdgeDevice device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("设备不存在: " + deviceId));

        EdgeDevice.DeviceStatus previousStatus = device.getStatus();

        device.setStatus(EdgeDevice.DeviceStatus.ACTIVE);
        device.setLastConnectedTime(LocalDateTime.now());
        device.setReconnectCount(0);
        device.setLastErrorMessage(null);
        deviceRepository.updateById(device);

        if (previousStatus == EdgeDevice.DeviceStatus.INACTIVE || previousStatus == EdgeDevice.DeviceStatus.FAULT) {
            log.info("设备恢复上线: deviceId={}, previousStatus={}", deviceId, previousStatus);

            alertService.sendDeviceAlert(device, AlertService.AlertType.DEVICE_RECOVERY,
                    "设备已恢复上线");
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

    public static class DeviceNotFoundException extends RuntimeException {
        public DeviceNotFoundException(String message) {
            super(message);
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class AuthenticationRequest {
        private String authType;
        private String token;
        private String certificate;
        private String certificateSignature;
        private String apiKey;
        private String username;
        private String password;
    }

    @lombok.Data
    @lombok.Builder
    public static class AuthenticationResult {
        private boolean success;
        private String accessToken;
        private String refreshToken;
        private int expiresIn;
        private String tokenType;
        private String deviceId;
        private String errorCode;
        private String errorMessage;
        private Integer remainingAttempts;
        private LocalDateTime lockoutUntil;
    }
}
