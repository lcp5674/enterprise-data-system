package com.enterprise.dataplatform.classification.strategy;

import com.enterprise.dataplatform.classification.domain.entity.ClassificationRule;
import com.enterprise.dataplatform.classification.domain.enums.ClassificationRuleType;
import com.enterprise.dataplatform.classification.dto.response.ClassificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Slf4j
@Component
public class PatternMatchingStrategy implements ClassificationStrategy {

    @Override
    public boolean match(ClassificationRule rule) {
        return rule.getRuleType() == ClassificationRuleType.PATTERN_MATCH 
               && rule.getPattern() != null && !rule.getPattern().isEmpty();
    }

    @Override
    public ClassificationResponse classify(String assetId, String columnName, String dataType,
                                         List<String> sampleValues, ClassificationRule rule) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return ClassificationResponse.builder()
                    .assetId(assetId)
                    .status("NO_MATCH")
                    .message("No sample values provided for pattern matching")
                    .build();
        }

        double confidence = calculateConfidence(sampleValues, rule);
        boolean matches = confidence >= (rule.getConfidenceThreshold() != null ? rule.getConfidenceThreshold() : 0.7);

        List<String> matchedValues = new ArrayList<>();
        for (String value : sampleValues) {
            if (value != null && matchesPattern(value, rule.getPattern())) {
                matchedValues.add(value);
            }
        }

        if (matches) {
            return ClassificationResponse.builder()
                    .assetId(assetId)
                    .assetName(columnName)
                    .sensitivityLevel(rule.getSensitivityLevel())
                    .confidenceScore(confidence)
                    .classificationMethod("PATTERN_MATCH")
                    .matchedRuleName(rule.getRuleName())
                    .matchedKeywords(matchedValues.size() > 5 ? matchedValues.subList(0, 5) : matchedValues)
                    .status("CLASSIFIED")
                    .message("Classified based on pattern match")
                    .build();
        }

        return ClassificationResponse.builder()
                .assetId(assetId)
                .status("NO_MATCH")
                .message("Pattern did not match with sufficient confidence")
                .build();
    }

    @Override
    public double calculateConfidence(List<String> sampleValues, ClassificationRule rule) {
        if (sampleValues == null || sampleValues.isEmpty() || rule.getPattern() == null) {
            return 0.0;
        }

        int matchCount = 0;
        for (String value : sampleValues) {
            if (value != null && matchesPattern(value, rule.getPattern())) {
                matchCount++;
            }
        }

        return (double) matchCount / sampleValues.size();
    }

    private boolean matchesPattern(String value, String pattern) {
        try {
            return Pattern.matches(pattern, value);
        } catch (PatternSyntaxException e) {
            log.warn("Invalid regex pattern: {}", pattern, e);
            return value.contains(pattern);
        }
    }
}
