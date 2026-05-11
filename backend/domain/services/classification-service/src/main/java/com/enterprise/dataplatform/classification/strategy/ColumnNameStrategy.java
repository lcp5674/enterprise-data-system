package com.enterprise.dataplatform.classification.strategy;

import com.enterprise.dataplatform.classification.domain.entity.ClassificationRule;
import com.enterprise.dataplatform.classification.domain.enums.ClassificationRuleType;
import com.enterprise.dataplatform.classification.dto.response.ClassificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ColumnNameStrategy implements ClassificationStrategy {

    private static final String CAMEL_CASE_PATTERN = "([a-z])([A-Z])";
    private static final String SNAKE_CASE_PATTERN = "_([a-z])";
    private static final String UNDERSCORE_REPLACEMENT = "$1 $2";

    @Override
    public boolean match(ClassificationRule rule) {
        return rule.getRuleType() == ClassificationRuleType.COLUMN_NAME
               && rule.getColumnPattern() != null && !rule.getColumnPattern().isEmpty();
    }

    @Override
    public ClassificationResponse classify(String assetId, String columnName, String dataType,
                                         List<String> sampleValues, ClassificationRule rule) {
        if (columnName == null || columnName.isEmpty()) {
            return ClassificationResponse.builder()
                    .assetId(assetId)
                    .status("NO_MATCH")
                    .message("No column name provided")
                    .build();
        }

        String normalizedColumn = normalizeColumnName(columnName);
        String pattern = rule.getColumnPattern().toLowerCase();

        if (!matchesColumnPattern(normalizedColumn, pattern)) {
            return ClassificationResponse.builder()
                    .assetId(assetId)
                    .status("NO_MATCH")
                    .message("Column name does not match pattern")
                    .build();
        }

        double confidence = calculateConfidence(sampleValues, rule);

        return ClassificationResponse.builder()
                .assetId(assetId)
                .assetName(columnName)
                .sensitivityLevel(rule.getSensitivityLevel())
                .confidenceScore(confidence)
                .classificationMethod("COLUMN_NAME")
                .matchedRuleName(rule.getRuleName())
                .matchedKeywords(List.of(columnName))
                .status("CLASSIFIED")
                .message("Classified based on column name pattern match")
                .build();
    }

    @Override
    public double calculateConfidence(List<String> sampleValues, ClassificationRule rule) {
        return 0.9;
    }

    private String normalizeColumnName(String columnName) {
        if (columnName == null) {
            return "";
        }
        String normalized = columnName.toLowerCase();
        normalized = Pattern.compile(CAMEL_CASE_PATTERN)
                .matcher(normalized)
                .replaceAll(UNDERSCORE_REPLACEMENT);
        normalized = normalized.replace('_', ' ');
        return normalized.trim();
    }

    private boolean matchesColumnPattern(String columnName, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return true;
        }
        
        try {
            return Pattern.matches(pattern, columnName) || columnName.contains(pattern);
        } catch (Exception e) {
            log.warn("Invalid column pattern: {}", pattern, e);
            return columnName.contains(pattern);
        }
    }
}
