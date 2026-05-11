package com.enterprise.dataplatform.classification.strategy;

import com.enterprise.dataplatform.classification.domain.entity.ClassificationRule;
import com.enterprise.dataplatform.classification.dto.response.ClassificationResponse;

import java.util.List;

public interface ClassificationStrategy {
    
    boolean match(ClassificationRule rule);
    
    ClassificationResponse classify(String assetId, String columnName, String dataType, 
                                  List<String> sampleValues, ClassificationRule rule);
    
    double calculateConfidence(List<String> sampleValues, ClassificationRule rule);
}
