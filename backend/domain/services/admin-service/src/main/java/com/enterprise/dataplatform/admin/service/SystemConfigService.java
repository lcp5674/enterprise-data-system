package com.enterprise.dataplatform.admin.service;

import com.enterprise.dataplatform.admin.dto.request.SystemConfigRequest;
import com.enterprise.dataplatform.admin.dto.response.SystemConfigResponse;
import com.enterprise.dataplatform.admin.entity.SystemConfig;
import com.enterprise.dataplatform.admin.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * System Config Service
 * 系统配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    // Default configurations
    private static final Map<String, String> DEFAULT_CONFIGS = new HashMap<>();

    static {
        DEFAULT_CONFIGS.put("system.name", "Enterprise Data Asset Management System");
        DEFAULT_CONFIGS.put("system.version", "1.0.0");
        DEFAULT_CONFIGS.put("system.max_upload_size", "104857600"); // 100MB
        DEFAULT_CONFIGS.put("system.session_timeout", "3600"); // 1 hour
        DEFAULT_CONFIGS.put("quality.auto_check_enabled", "true");
        DEFAULT_CONFIGS.put("lineage.auto_capture_enabled", "true");
    }

    /**
     * Get a configuration value by key
     */
    public SystemConfigResponse getConfig(String configKey) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseGet(() -> {
                    // Return default config if exists
                    if (DEFAULT_CONFIGS.containsKey(configKey)) {
                        SystemConfig defaultConfig = new SystemConfig();
                        defaultConfig.setConfigKey(configKey);
                        defaultConfig.setConfigValue(DEFAULT_CONFIGS.get(configKey));
                        defaultConfig.setConfigType(SystemConfig.ConfigType.STRING);
                        return defaultConfig;
                    }
                    throw new IllegalArgumentException("Config not found: " + configKey);
                });

        return toSystemConfigResponse(config);
    }

    /**
     * Set a configuration value
     */
    @Transactional
    public SystemConfigResponse setConfig(SystemConfigRequest request, String updatedBy) {
        SystemConfig config = systemConfigRepository.findByConfigKey(request.getConfigKey())
                .orElse(new SystemConfig());

        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setConfigType(SystemConfig.ConfigType.valueOf(
                request.getConfigType() != null ? request.getConfigType() : "STRING"));
        config.setDescription(request.getDescription());
        config.setCategory(request.getCategory());
        config.setUpdatedBy(updatedBy);

        SystemConfig saved = systemConfigRepository.save(config);
        return toSystemConfigResponse(saved);
    }

    /**
     * Batch set configurations
     */
    @Transactional
    public Map<String, SystemConfigResponse> batchSetConfig(Map<String, String> configs, String updatedBy) {
        Map<String, SystemConfigResponse> results = new HashMap<>();

        for (Map.Entry<String, String> entry : configs.entrySet()) {
            SystemConfigRequest request = new SystemConfigRequest();
            request.setConfigKey(entry.getKey());
            request.setConfigValue(entry.getValue());
            request.setCategory("batch");

            SystemConfigResponse response = setConfig(request, updatedBy);
            results.put(entry.getKey(), response);
        }

        return results;
    }

    /**
     * List configs by category
     */
    public List<SystemConfigResponse> listConfigsByCategory(String category) {
        List<SystemConfig> configs = systemConfigRepository.findByCategory(category);

        return configs.stream()
                .map(this::toSystemConfigResponse)
                .collect(Collectors.toList());
    }

    /**
     * List all configs
     */
    public List<SystemConfigResponse> listAllConfigs() {
        return systemConfigRepository.findAll().stream()
                .map(this::toSystemConfigResponse)
                .collect(Collectors.toList());
    }

    /**
     * Reset config to default value
     */
    @Transactional
    public SystemConfigResponse resetToDefault(String configKey) {
        if (!DEFAULT_CONFIGS.containsKey(configKey)) {
            throw new IllegalArgumentException("No default value for config: " + configKey);
        }

        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new IllegalArgumentException("Config not found: " + configKey));

        config.setConfigValue(DEFAULT_CONFIGS.get(configKey));
        SystemConfig saved = systemConfigRepository.save(config);
        return toSystemConfigResponse(saved);
    }

    /**
     * Delete a configuration
     */
    @Transactional
    public void deleteConfig(String configKey) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new IllegalArgumentException("Config not found: " + configKey));
        systemConfigRepository.delete(config);
    }

    private SystemConfigResponse toSystemConfigResponse(SystemConfig config) {
        SystemConfigResponse response = new SystemConfigResponse();
        response.setId(config.getId());
        response.setConfigKey(config.getConfigKey());
        response.setConfigValue(config.getConfigValue());
        response.setConfigType(config.getConfigType().name());
        response.setDescription(config.getDescription());
        response.setCategory(config.getCategory());
        response.setUpdatedBy(config.getUpdatedBy());
        response.setUpdatedAt(config.getUpdatedAt());
        return response;
    }
}
