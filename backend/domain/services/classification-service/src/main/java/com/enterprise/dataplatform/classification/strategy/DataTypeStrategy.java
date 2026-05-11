package com.enterprise.dataplatform.classification.strategy;

import com.enterprise.dataplatform.classification.domain.entity.ClassificationRule;
import com.enterprise.dataplatform.classification.domain.enums.ClassificationRuleType;
import com.enterprise.dataplatform.classification.dto.response.ClassificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class DataTypeStrategy implements ClassificationStrategy {

    private static final Map<String, Set<SensitivityLevel>> DATA_TYPE_SENSITIVITY_MAP = new HashMap<>();

    static {
        DATA_TYPE_SENSITIVITY_MAP.put("string", Set.of(
            SensitivityLevel.PUBLIC, SensitivityLevel.INTERNAL, 
            SensitivityLevel.CONFIDENTIAL, SensitivityLevel.HIGHLY_CONFIDENTIAL));
        DATA_TYPE_SENSITIVITY_MAP.put("varchar", Set.of(
            SensitivityLevel.PUBLIC, SensitivityLevel.INTERNAL, 
            SensitivityLevel.CONFIDENTIAL, SensitivityLevel.HIGHLY_CONFIDENTIAL));
        DATA_TYPE_SENSITIVITY_MAP.put("text", Set.of(
            SensitivityLevel.PUBLIC, SensitivityLevel.INTERNAL));
        DATA_TYPE_SENSITIVITY_MAP.put("int", Set.of(
            SensitivityLevel.PUBLIC, SensitivityLevel.INTERNAL, SensitivityLevel.CONFIDENTIAL));
        DATA_TYPE_SENSITIVITY_MAP.put("bigint", Set.of(
            SensitivityLevel.PUBLIC, SensitivityLevel.INTERNAL, SensitivityLevel.CONFIDENTIAL));
        DATA_TYPE_SENSITIVITY_MAP.put("decimal", Set.of(
            SensitivityLevel.INTERNAL, SensitivityLevel.CONFIDENTIAL));
        DATA_TYPE_SENSITIVITY_MAP.put("float", Set.of(
            SensitivityLevel.INTERNAL, SensitivityLevel.CONFIDENTIAL));
        DATA_TYPE_SENSITIVITY_MAP.put("double", Set.of(
            SensitivityLevel.INTERNAL, SensitivityLevel.CONFIDENTIAL));
        DATA_TYPE_SENSITIVITY_MAP.put("date", Set.of(
            SensitivityLevel.PUBLIC, SensitivityLevel.INTERNAL));
        DATA_TYPE_SENSITIVITY_MAP.put("datetime", Set.of(
            SensitivityLevel.PUBLIC, SensitivityLevel.INTERNAL));
        DATA_TYPE_SENSITIVITY_MAP.put("timestamp", Set.of(
            SensitivityLevel.PUBLIC, SensitivityLevel.INTERNAL));
        DATA_TYPE_SENSITIVITY_MAP.put("boolean", Set.of(
            SensitivityLevel.PUBLIC));
        DATA_TYPE_SENSITIVITY_MAP.put("json", Set.of(
            SensitivityLevel.CONFIDENTIAL, SensitivityLevel.HIGHLY_CONFIDENTIAL));
        DATA_TYPE_SENSITIVITY_MAP.put("blob", Set.of(
            SensitivityLevel.HIGHLY_CONFIDENTIAL));
        DATA_TYPE_SENSITIVITY_MAP.put("binary", Set.of(
            SensitivityLevel.HIGHLY_CONFIDENTIAL));
    }

    @Override
    public boolean match(ClassificationRule rule) {
        return rule.getRuleType() == ClassificationRuleType.DATA_TYPE
               && rule.getDataType() != null && !rule.getDataType().isEmpty();
    }

    @Override
    public ClassificationResponse classify(String assetId, String columnName, String dataType,
                                         List<String> sampleValues, ClassificationRule rule) {
        if (dataType == null || dataType.isEmpty()) {
            return ClassificationResponse.builder()
                    .assetId(assetId)
                    .status("NO_MATCH")
                    .message("No data type provided")
                    .build();
        }

        String normalizedDataType = dataType.toLowerCase().split("\\(")[0].trim();
        Set<SensitivityLevel> possibleLevels = DATA_TYPE_SENSITIVITY_MAP.get(normalizedDataType);

        if (possibleLevels == null || !possibleLevels.contains(rule.getSensitivityLevel())) {
            return ClassificationResponse.builder()
                    .assetId(assetId)
                    .status("NO_MATCH")
                    .message("Data type does not match the rule")
                    .build();
        }

        double confidence = calculateConfidence(sampleValues, rule);

        return ClassificationResponse.builder()
                .assetId(assetId)
                .assetName(columnName)
                .sensitivityLevel(rule.getSensitivityLevel())
                .confidenceScore(confidence)
                .classificationMethod("DATA_TYPE")
                .matchedRuleName(rule.getRuleName())
                .status("CLASSIFIED")
                .message("Classified based on data type: " + dataType)
                .build();
    }

    @Override
    public double calculateConfidence(List<String> sampleValues, ClassificationRule rule) {
        if (rule.getDataType() == null) {
            return 0.0;
        }
        return 0.85;
    }
}
