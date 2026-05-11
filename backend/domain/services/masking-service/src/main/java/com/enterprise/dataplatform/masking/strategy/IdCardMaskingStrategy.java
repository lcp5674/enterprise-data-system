package com.enterprise.dataplatform.masking.strategy;

import com.enterprise.dataplatform.masking.domain.entity.MaskingConfig;
import com.enterprise.dataplatform.masking.domain.enums.MaskingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IdCardMaskingStrategy implements MaskingStrategy {

    private static final int ID_CARD_LENGTH = 18;
    private static final int OLD_ID_CARD_LENGTH = 15;

    @Override
    public String mask(String value, MaskingConfig config) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        String cleanValue = value.replaceAll("[^0-9Xx]", "");
        
        if (cleanValue.length() == ID_CARD_LENGTH) {
            return mask18DigitIdCard(cleanValue);
        } else if (cleanValue.length() == OLD_ID_CARD_LENGTH) {
            return mask15DigitIdCard(cleanValue);
        }
        
        log.warn("ID card number length is invalid: {}", cleanValue.length());
        return maskByLength(cleanValue);
    }

    private String mask18DigitIdCard(String value) {
        return value.substring(0, 6) + "********" + value.substring(value.length() - 4);
    }

    private String mask15DigitIdCard(String value) {
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }

    private String maskByLength(String value) {
        if (value.length() <= 4) {
            return repeatChar('*', value.length());
        }
        int prefixLength = Math.min(2, value.length() / 3);
        int suffixLength = 4;
        int maskLength = value.length() - prefixLength - suffixLength;
        if (maskLength < 0) maskLength = 0;
        
        return value.substring(0, prefixLength) + repeatChar('*', maskLength) + value.substring(value.length() - suffixLength);
    }

    private String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    @Override
    public boolean supports(MaskingConfig config) {
        return config != null && MaskingType.ID_CARD.equals(config.getMaskingType());
    }

    @Override
    public String getStrategyName() {
        return "ID_CARD_MASKING";
    }
}
