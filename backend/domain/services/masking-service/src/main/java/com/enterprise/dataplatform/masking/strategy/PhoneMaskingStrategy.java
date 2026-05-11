package com.enterprise.dataplatform.masking.strategy;

import com.enterprise.dataplatform.masking.domain.entity.MaskingConfig;
import com.enterprise.dataplatform.masking.domain.enums.MaskingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PhoneMaskingStrategy implements MaskingStrategy {

    @Override
    public String mask(String value, MaskingConfig config) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        String cleanValue = value.replaceAll("[^0-9]", "");
        
        if (cleanValue.length() < 7) {
            log.warn("Phone number too short to mask: {}", value);
            return repeatMask(value, value.length());
        }

        return cleanValue.substring(0, 3) + "****" + cleanValue.substring(cleanValue.length() - 4);
    }

    @Override
    public boolean supports(MaskingConfig config) {
        return config != null && MaskingType.PHONE.equals(config.getMaskingType());
    }

    @Override
    public String getStrategyName() {
        return "PHONE_MASKING";
    }

    private String repeatMask(String value, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (Character.isDigit(value.charAt(i))) {
                sb.append("*");
            } else {
                sb.append(value.charAt(i));
            }
        }
        return sb.toString();
    }
}
