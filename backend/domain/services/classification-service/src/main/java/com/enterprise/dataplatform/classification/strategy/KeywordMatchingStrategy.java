package com.enterprise.dataplatform.classification.strategy;

import com.enterprise.dataplatform.classification.domain.entity.ClassificationRule;
import com.enterprise.dataplatform.classification.domain.enums.ClassificationRuleType;
import com.enterprise.dataplatform.classification.dto.response.ClassificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class KeywordMatchingStrategy implements ClassificationStrategy {

    private static final Map<String, List<String>> SENSITIVITY_KEYWORDS = new HashMap<>();

    static {
        SENSITIVITY_KEYWORDS.put("HIGHLY_CONFIDENTIAL", Arrays.asList(
            "password", "secret", "key", "token", "credential", "private_key",
            "ssn", "social_security", "passport", "credit_card", "cvv", "pin"));
        
        SENSITIVITY_KEYWORDS.put("CONFIDENTIAL", Arrays.asList(
            "salary", "income", "address", "phone", "mobile", "email", 
            "birthdate", "birth_date", "age", "account", "bank", "card",
            "salary", "bonus", "compensation", "tax"));
        
        SENSITIVITY_KEYWORDS.put("INTERNAL", Arrays.asList(
            "internal", "private", "confidential", "restricted", "hr",
            "employee", "department", "project", "budget"));
        
        SENSITIVITY_KEYWORDS.put("PUBLIC", Arrays.asList(
            "public", "open", "external", "published", "marketing", 
            "announcement", "news", "blog", "faq"));
    }

    @Override
    public boolean match(ClassificationRule rule) {
        return rule.getRuleType() == ClassificationRuleType.KEYWORD 
               && (rule.getKeywords() != null && !rule.getKeywords().isEmpty()
                   || SENSITIVITY_KEYWORDS.containsKey(rule.getSensitivityLevel().name()));
    }

    @Override
    public ClassificationResponse classify(String assetId, String columnName, String dataType,
                                         List<String> sampleValues, ClassificationRule rule) {
        Set<String> keywords = new HashSet<>();
        
        if (rule.getKeywords() != null && !rule.getKeywords().isEmpty()) {
            keywords.addAll(Arrays.asList(rule.getKeywords().split("[,\\n]")));
        }
        
        String sensitivityName = rule.getSensitivityLevel().name();
        if (SENSITIVITY_KEYWORDS.containsKey(sensitivityName)) {
            keywords.addAll(SENSITIVITY_KEYWORDS.get(sensitivityName));
        }

        if (keywords.isEmpty()) {
            return ClassificationResponse.builder()
                    .assetId(assetId)
                    .status("NO_MATCH")
                    .message("No keywords configured")
                    .build();
        }

        Set<String> matchedKeywords = new HashSet<>();
        Set<String> columnMatchedKeywords = new HashSet<>();
        
        if (sampleValues != null) {
            for (String value : sampleValues) {
                if (value != null) {
                    String lowerValue = value.toLowerCase();
                    for (String keyword : keywords) {
                        if (lowerValue.contains(keyword.toLowerCase())) {
                            matchedKeywords.add(keyword);
                        }
                    }
                }
            }
        }
        
        if (columnName != null) {
            String lowerColumnName = columnName.toLowerCase();
            for (String keyword : keywords) {
                if (lowerColumnName.contains(keyword.toLowerCase())) {
                    columnMatchedKeywords.add(keyword);
                }
            }
        }

        Set<String> allMatched = new HashSet<>();
        allMatched.addAll(matchedKeywords);
        allMatched.addAll(columnMatchedKeywords);

        if (allMatched.isEmpty()) {
            return ClassificationResponse.builder()
                    .assetId(assetId)
                    .status("NO_MATCH")
                    .message("No keywords matched")
                    .build();
        }

        double confidence = calculateConfidenceFromMatches(allMatched.size(), keywords.size(), columnMatchedKeywords);

        return ClassificationResponse.builder()
                .assetId(assetId)
                .assetName(columnName)
                .sensitivityLevel(rule.getSensitivityLevel())
                .confidenceScore(confidence)
                .classificationMethod("KEYWORD")
                .matchedRuleName(rule.getRuleName())
                .matchedKeywords(new ArrayList<>(allMatched))
                .status("CLASSIFIED")
                .message("Classified based on keyword match: " + String.join(", ", allMatched))
                .build();
    }

    @Override
    public double calculateConfidence(List<String> sampleValues, ClassificationRule rule) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return 0.0;
        }
        
        Set<String> keywords = new HashSet<>();
        if (rule.getKeywords() != null) {
            keywords.addAll(Arrays.asList(rule.getKeywords().split("[,\\n]")));
        }
        
        int matchCount = 0;
        for (String value : sampleValues) {
            if (value != null) {
                String lower = value.toLowerCase();
                for (String keyword : keywords) {
                    if (lower.contains(keyword.toLowerCase())) {
                        matchCount++;
                        break;
                    }
                }
            }
        }
        
        return (double) matchCount / sampleValues.size();
    }

    private double calculateConfidenceFromMatches(int matchCount, int totalKeywords, Set<String> columnMatches) {
        double baseConfidence = Math.min(0.7 + (matchCount * 0.05), 0.95);
        
        if (!columnMatches.isEmpty()) {
            baseConfidence = Math.min(baseConfidence + 0.15, 0.99);
        }
        
        return Math.round(baseConfidence * 100.0) / 100.0;
    }
}
