package com.enterprise.dataplatform.masking.strategy;

import com.enterprise.dataplatform.masking.domain.entity.MaskingConfig;

public interface MaskingStrategy {
    
    String mask(String value, MaskingConfig config);
    
    boolean supports(MaskingConfig config);
    
    String getStrategyName();
}
