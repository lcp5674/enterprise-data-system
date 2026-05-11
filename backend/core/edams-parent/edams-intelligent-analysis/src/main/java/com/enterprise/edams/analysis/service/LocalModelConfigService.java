package com.enterprise.edams.analysis.service;

import com.enterprise.edams.analysis.dto.request.CreateModelConfigRequest;
import com.enterprise.edams.analysis.dto.request.UpdateModelConfigRequest;
import com.enterprise.edams.analysis.dto.response.ConnectionTestResponse;
import com.enterprise.edams.analysis.dto.response.ModelConfigResponse;
import com.enterprise.edams.analysis.entity.LocalModelConfig;
import com.enterprise.edams.analysis.exception.AnalysisException;
import com.enterprise.edams.analysis.llm.LLMConnector;
import com.enterprise.edams.analysis.llm.LLMConnectorFactory;
import com.enterprise.edams.analysis.llm.LLMRequest;
import com.enterprise.edams.analysis.llm.LLMResponse;
import com.enterprise.edams.analysis.repository.LocalModelConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalModelConfigService {

    private final LocalModelConfigRepository repository;
    private final LLMConnectorFactory connectorFactory;

    @Transactional
    public ModelConfigResponse createConfig(CreateModelConfigRequest request) {
        log.info("Creating model config: {}", request.getConfigCode());

        if (repository.existsByConfigCode(request.getConfigCode())) {
            throw new AnalysisException("CONFIG_EXISTS", "配置编码已存在: " + request.getConfigCode());
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultConfig();
        }

        LocalModelConfig config = LocalModelConfig.builder()
                .configCode(request.getConfigCode())
                .configName(request.getConfigName())
                .modelType(request.getModelType())
                .baseUrl(request.getBaseUrl())
                .apiKey(request.getApiKey())
                .modelName(request.getModelName())
                .modelVersion(request.getModelVersion())
                .maxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : 4096)
                .temperature(request.getTemperature() != null ? request.getTemperature() : 0.7)
                .contextWindow(request.getContextWindow() != null ? request.getContextWindow() : 8192)
                .tableAnalysisTemplate(request.getTableAnalysisTemplate())
                .lineageAnalysisTemplate(request.getLineageAnalysisTemplate())
                .indicatorExtractionTemplate(request.getIndicatorExtractionTemplate())
                .subjectClassificationTemplate(request.getSubjectClassificationTemplate())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .createdBy(request.getConfigCode())
                .build();

        config = repository.save(config);
        log.info("Model config created successfully: id={}", config.getId());

        return ModelConfigResponse.fromEntity(config);
    }

    @Transactional
    public ModelConfigResponse updateConfig(Long id, UpdateModelConfigRequest request) {
        log.info("Updating model config: {}", id);

        LocalModelConfig config = repository.findById(id)
                .orElseThrow(() -> new AnalysisException("CONFIG_NOT_FOUND", "配置不存在: " + id));

        if (request.getConfigName() != null) {
            config.setConfigName(request.getConfigName());
        }
        if (request.getModelType() != null) {
            config.setModelType(request.getModelType());
        }
        if (request.getBaseUrl() != null) {
            config.setBaseUrl(request.getBaseUrl());
        }
        if (request.getApiKey() != null) {
            config.setApiKey(request.getApiKey());
        }
        if (request.getModelName() != null) {
            config.setModelName(request.getModelName());
        }
        if (request.getModelVersion() != null) {
            config.setModelVersion(request.getModelVersion());
        }
        if (request.getMaxTokens() != null) {
            config.setMaxTokens(request.getMaxTokens());
        }
        if (request.getTemperature() != null) {
            config.setTemperature(request.getTemperature());
        }
        if (request.getContextWindow() != null) {
            config.setContextWindow(request.getContextWindow());
        }
        if (request.getTableAnalysisTemplate() != null) {
            config.setTableAnalysisTemplate(request.getTableAnalysisTemplate());
        }
        if (request.getLineageAnalysisTemplate() != null) {
            config.setLineageAnalysisTemplate(request.getLineageAnalysisTemplate());
        }
        if (request.getIndicatorExtractionTemplate() != null) {
            config.setIndicatorExtractionTemplate(request.getIndicatorExtractionTemplate());
        }
        if (request.getSubjectClassificationTemplate() != null) {
            config.setSubjectClassificationTemplate(request.getSubjectClassificationTemplate());
        }
        if (request.getEnabled() != null) {
            config.setEnabled(request.getEnabled());
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultConfig();
            config.setIsDefault(true);
        }

        config = repository.save(config);
        log.info("Model config updated successfully: id={}", id);

        return ModelConfigResponse.fromEntity(config);
    }

    @Transactional
    public void deleteConfig(Long id) {
        log.info("Deleting model config: {}", id);

        if (!repository.existsById(id)) {
            throw new AnalysisException("CONFIG_NOT_FOUND", "配置不存在: " + id);
        }

        repository.deleteById(id);
        log.info("Model config deleted: id={}", id);
    }

    public ModelConfigResponse getConfig(Long id) {
        LocalModelConfig config = repository.findById(id)
                .orElseThrow(() -> new AnalysisException("CONFIG_NOT_FOUND", "配置不存在: " + id));
        return ModelConfigResponse.fromEntity(config);
    }

    public ConnectionTestResponse testConnection(Long id, String testPrompt) {
        log.info("Testing connection for config: {}", id);

        LocalModelConfig config = repository.findById(id)
                .orElseThrow(() -> new AnalysisException("CONFIG_NOT_FOUND", "配置不存在: " + id));

        long startTime = System.currentTimeMillis();

        try {
            LLMConnector connector = connectorFactory.getConnector(config);

            if (!connector.testConnection()) {
                return ConnectionTestResponse.builder()
                        .success(false)
                        .message("无法连接到模型服务")
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            LLMRequest request = LLMRequest.builder()
                    .model(config.getModelName())
                    .prompt(testPrompt != null ? testPrompt : "Hello, please respond with 'OK' if you can hear me.")
                    .maxTokens(config.getMaxTokens())
                    .temperature(config.getTemperature())
                    .build();

            LLMResponse response = connector.generate(request);

            if (response.getSuccess()) {
                repository.incrementUsageCount(id);
                repository.incrementSuccessCount(id);
                log.info("Connection test successful for config: {}", id);
            } else {
                repository.incrementFailureCount(id);
            }

            return ConnectionTestResponse.builder()
                    .success(response.getSuccess())
                    .message(response.getSuccess() ? "连接成功" : response.getErrorMessage())
                    .responseTimeMs(response.getResponseTimeMs())
                    .modelName(config.getModelName())
                    .build();

        } catch (Exception e) {
            log.error("Connection test failed for config: {}", id, e);
            repository.incrementFailureCount(id);

            return ConnectionTestResponse.builder()
                    .success(false)
                    .message("连接测试失败: " + e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    public ModelConfigResponse getDefaultConfig() {
        LocalModelConfig config = repository.findByIsDefaultTrue()
                .orElseGet(() -> repository.findByEnabledTrue().stream().findFirst()
                        .orElseThrow(() -> new AnalysisException("NO_DEFAULT_CONFIG", "没有可用的模型配置")));
        return ModelConfigResponse.fromEntity(config);
    }

    public List<ModelConfigResponse> listConfigs() {
        return repository.findAll().stream()
                .map(ModelConfigResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<String> getAvailableModels(Long configId) {
        LocalModelConfig config = repository.findById(configId)
                .orElseThrow(() -> new AnalysisException("CONFIG_NOT_FOUND", "配置不存在: " + configId));

        LLMConnector connector = connectorFactory.getConnector(config);
        return connector.listModels();
    }

    private void clearDefaultConfig() {
        repository.findByIsDefaultTrue().ifPresent(config -> {
            config.setIsDefault(false);
            repository.save(config);
        });
    }
}
