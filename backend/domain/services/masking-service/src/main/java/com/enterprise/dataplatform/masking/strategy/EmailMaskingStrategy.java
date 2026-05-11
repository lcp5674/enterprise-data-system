package com.enterprise.dataplatform.masking.strategy;

import com.enterprise.dataplatform.masking.domain.entity.MaskingConfig;
import com.enterprise.dataplatform.masking.domain.enums.MaskingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailMaskingStrategy implements MaskingStrategy {

    @Override
    public String mask(String value, MaskingConfig config) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        if (!value.contains("@")) {
            log.warn("Invalid email format: {}", value);
            return partialMask(value);
        }

        String[] parts = value.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];

        if (localPart.isEmpty()) {
            return "***@" + domainPart;
        }

        if (localPart.length() == 1) {
            return localPart + "***@" + domainPart;
        }

        return localPart.charAt(0) + "***@" + domainPart;
    }

    @Override
    public boolean supports(MaskingConfig config) {
        return config != null && MaskingType.EMAIL.equals(config.getMaskingType());
    }

    @Override
    public String getStrategyName() {
        return "EMAIL_MASKING";
    }

    private String partialMask(String value) {
        if (value.length() <= 2) {
            return repeatChar('*', value.length());
        }
        return value.charAt(0) + repeatChar('*', value.length() - 2) + value.charAt(value.length() - 1);
    }

    private String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
