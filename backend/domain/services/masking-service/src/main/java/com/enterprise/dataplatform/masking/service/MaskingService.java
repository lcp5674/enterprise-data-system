package com.enterprise.dataplatform.masking.service;

import com.enterprise.dataplatform.masking.domain.entity.MaskingConfig;
import com.enterprise.dataplatform.masking.domain.entity.MaskingRule;
import com.enterprise.dataplatform.masking.domain.enums.MaskingType;
import com.enterprise.dataplatform.masking.dto.request.MaskingRequest;
import com.enterprise.dataplatform.masking.dto.response.MaskingResponse;
import com.enterprise.dataplatform.masking.repository.MaskingConfigRepository;
import com.enterprise.dataplatform.masking.repository.MaskingRuleRepository;
import com.enterprise.dataplatform.masking.strategy.MaskingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaskingService {

    private final List<MaskingStrategy> strategies;
    private final MaskingConfigRepository configRepository;
    private final MaskingRuleRepository ruleRepository;

    private static final String DEFAULT_MASK_CHAR = "*";

    public String maskValue(String value, MaskingType type) {
        return maskValue(value, type, null);
    }

    public String maskValue(String value, MaskingType type, String customPattern) {
        if (value == null) {
            return null;
        }

        MaskingConfig config = MaskingConfig.builder()
                .maskingType(type)
                .customPattern(customPattern)
                .maskChar(DEFAULT_MASK_CHAR)
                .build();

        MaskingStrategy strategy = findStrategy(config);
        if (strategy == null) {
            log.warn("No masking strategy found for type: {}", type);
            return maskWithAsterisks(value);
        }

        return strategy.mask(value, config);
    }

    public MaskingResponse maskData(MaskingRequest request) {
        log.info("开始数据脱敏: assetId={}, columns={}", request.getAssetId(), 
                request.getColumns() != null ? request.getColumns().size() : 0);

        if (request.getData() == null || request.getData().isEmpty()) {
            return MaskingResponse.builder()
                    .success(true)
                    .maskedData(Collections.emptyList())
                    .build();
        }

        Map<String, MaskingType> columnMaskingTypes = getColumnMaskingRules(request.getAssetId(), request.getColumns());
        
        List<Map<String, Object>> maskedData = new ArrayList<>();
        
        for (Map<String, Object> row : request.getData()) {
            Map<String, Object> maskedRow = new HashMap<>(row);
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String column = entry.getKey();
                Object value = entry.getValue();
                
                MaskingType type = columnMaskingTypes.get(column);
                if (type != null && value != null) {
                    String maskedValue = maskValue(String.valueOf(value), type);
                    maskedRow.put(column, maskedValue);
                }
            }
            maskedData.add(maskedRow);
        }

        log.info("数据脱敏完成: assetId={}, maskedRows={}", request.getAssetId(), maskedData.size());

        return MaskingResponse.builder()
                .success(true)
                .maskedData(maskedData)
                .maskedColumns(columnMaskingTypes.keySet())
                .recordCount(maskedData.size())
                .build();
    }

    private Map<String, MaskingType> getColumnMaskingRules(String assetId, List<String> columns) {
        Map<String, MaskingType> rules = new HashMap<>();

        List<MaskingRule> configuredRules = ruleRepository.findByAssetIdAndIsActiveTrue(assetId);
        for (MaskingRule rule : configuredRules) {
            if (columns == null || columns.isEmpty() || columns.contains(rule.getColumnName())) {
                if (rule.getMaskingType() != null) {
                    rules.put(rule.getColumnName(), MaskingType.valueOf(rule.getMaskingType()));
                }
            }
        }

        return rules;
    }

    public String maskFieldValue(String fieldName, String value, String classificationLevel) {
        MaskingType type = inferMaskingType(fieldName, classificationLevel);
        if (type == null) {
            return value;
        }
        return maskValue(value, type);
    }

    private MaskingType inferMaskingType(String fieldName, String classificationLevel) {
        String lowerField = fieldName.toLowerCase();
        
        if (lowerField.contains("phone") || lowerField.contains("mobile") || lowerField.contains("tel")) {
            return MaskingType.PHONE;
        }
        if (lowerField.contains("email")) {
            return MaskingType.EMAIL;
        }
        if (lowerField.contains("idcard") || lowerField.contains("id_card") || lowerField.contains("身份证")) {
            return MaskingType.ID_CARD;
        }
        if (lowerField.contains("bank") || lowerField.contains("card") || lowerField.contains("银行卡")) {
            return MaskingType.BANK_CARD;
        }
        if (lowerField.contains("address") || lowerField.contains("addr") || lowerField.contains("地址")) {
            return MaskingType.ADDRESS;
        }
        if (lowerField.contains("name") || lowerField.contains("姓名")) {
            return MaskingType.NAME;
        }
        
        if (classificationLevel != null) {
            String level = classificationLevel.toUpperCase();
            if ("HIGHLY_CONFIDENTIAL".equals(level) || "CONFIDENTIAL".equals(level)) {
                return MaskingType.HASH;
            }
        }
        
        return null;
    }

    private MaskingStrategy findStrategy(MaskingConfig config) {
        for (MaskingStrategy strategy : strategies) {
            if (strategy.supports(config)) {
                return strategy;
            }
        }
        return null;
    }

    private String maskWithAsterisks(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        int length = value.length();
        if (length <= 2) {
            return repeatChar('*', length);
        }
        return value.charAt(0) + repeatChar('*', length - 2) + value.charAt(length - 1);
    }

    private String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    @Transactional
    public MaskingConfig createConfig(MaskingConfig config) {
        config.setIsActive(true);
        return configRepository.save(config);
    }

    public List<MaskingConfig> getAllConfigs() {
        return configRepository.findAll();
    }

    public List<MaskingConfig> getActiveConfigs() {
        return configRepository.findByIsActiveTrue();
    }

    @Transactional
    public MaskingConfig updateConfig(Long configId, MaskingConfig updates) {
        MaskingConfig existing = configRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Config not found: " + configId));

        if (updates.getConfigName() != null) existing.setConfigName(updates.getConfigName());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getMaskingType() != null) existing.setMaskingType(updates.getMaskingType());
        if (updates.getCustomPattern() != null) existing.setCustomPattern(updates.getCustomPattern());
        if (updates.getMaskChar() != null) existing.setMaskChar(updates.getMaskChar());
        if (updates.getShowFirstN() != null) existing.setShowFirstN(updates.getShowFirstN());
        if (updates.getShowLastN() != null) existing.setShowLastN(updates.getShowLastN());

        return configRepository.save(existing);
    }

    @Transactional
    public void deleteConfig(Long configId) {
        configRepository.deleteById(configId);
    }

    @Transactional
    public MaskingRule createRule(MaskingRule rule) {
        rule.setIsActive(true);
        return ruleRepository.save(rule);
    }

    public List<MaskingRule> getRulesByAsset(String assetId) {
        return ruleRepository.findByAssetId(assetId);
    }

    public List<MaskingRule> getActiveRules() {
        return ruleRepository.findByIsActiveTrue();
    }

    @Transactional
    public MaskingRule updateRule(Long ruleId, MaskingRule updates) {
        MaskingRule existing = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        if (updates.getColumnName() != null) existing.setColumnName(updates.getColumnName());
        if (updates.getMaskingType() != null) existing.setMaskingType(updates.getMaskingType());
        if (updates.getMaskingConfig() != null) existing.setMaskingConfig(updates.getMaskingConfig());
        if (updates.getPriority() != null) existing.setPriority(updates.getPriority());
        if (updates.getConditionExpression() != null) existing.setConditionExpression(updates.getConditionExpression());

        return ruleRepository.save(existing);
    }

    @Transactional
    public void deleteRule(Long ruleId) {
        ruleRepository.deleteById(ruleId);
    }

    @Transactional
    public void toggleRuleStatus(Long ruleId) {
        MaskingRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        rule.setIsActive(!rule.getIsActive());
        ruleRepository.save(rule);
    }

    public Map<String, Long> getMaskingStatistics() {
        List<MaskingRule> allRules = ruleRepository.findAll();
        return allRules.stream()
                .collect(Collectors.groupingBy(
                        MaskingRule::getMaskingType,
                        Collectors.counting()
                ));
    }
}
