package com.enterprise.dataplatform.masking.strategy;

import com.enterprise.dataplatform.masking.domain.entity.MaskingConfig;
import com.enterprise.dataplatform.masking.domain.enums.MaskingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BankCardMaskingStrategy implements MaskingStrategy {

    @Override
    public String mask(String value, MaskingConfig config) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        String cleanValue = value.replaceAll("[^0-9]", "");
        
        if (cleanValue.length() < 4) {
            log.warn("Bank card number too short to mask: {}", value);
            return repeatChar('*', cleanValue.length());
        }

        return "**** **** **** " + cleanValue.substring(cleanValue.length() - 4);
    }

    @Override
    public boolean supports(MaskingConfig config) {
        return config != null && MaskingType.BANK_CARD.equals(config.getMaskingType());
    }

    @Override
    public String getStrategyName() {
        return "BANK_CARD_MASKING";
    }

    private String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
