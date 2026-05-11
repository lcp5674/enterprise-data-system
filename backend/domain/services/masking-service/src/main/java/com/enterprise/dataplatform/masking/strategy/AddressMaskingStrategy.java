package com.enterprise.dataplatform.masking.strategy;

import com.enterprise.dataplatform.masking.domain.entity.MaskingConfig;
import com.enterprise.dataplatform.masking.domain.enums.MaskingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AddressMaskingStrategy implements MaskingStrategy {

    @Override
    public String mask(String value, MaskingConfig config) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        int keepPrefixLength = 6;
        if (config != null && config.getCustomPattern() != null) {
            try {
                keepPrefixLength = Integer.parseInt(config.getCustomPattern());
            } catch (NumberFormatException e) {
                keepPrefixLength = 6;
            }
        }

        int length = value.length();
        
        if (length <= keepPrefixLength) {
            return value;
        }

        int maskLength = length - keepPrefixLength;
        if (maskLength > 10) {
            maskLength = 10;
        }

        String prefix = value.substring(0, keepPrefixLength);
        String suffix = value.substring(length - Math.min(3, length - keepPrefixLength - maskLength));
        String mask = repeatChar('*', maskLength);

        return prefix + mask + suffix;
    }

    @Override
    public boolean supports(MaskingConfig config) {
        return config != null && MaskingType.ADDRESS.equals(config.getMaskingType());
    }

    @Override
    public String getStrategyName() {
        return "ADDRESS_MASKING";
    }

    private String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
