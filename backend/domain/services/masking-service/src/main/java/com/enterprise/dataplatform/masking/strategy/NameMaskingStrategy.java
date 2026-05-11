package com.enterprise.dataplatform.masking.strategy;

import com.enterprise.dataplatform.masking.domain.entity.MaskingConfig;
import com.enterprise.dataplatform.masking.domain.enums.MaskingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class NameMaskingStrategy implements MaskingStrategy {

    @Override
    public String mask(String value, MaskingConfig config) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        int visibleChars = 1;
        if (config != null && config.getCustomPattern() != null) {
            try {
                visibleChars = Integer.parseInt(config.getCustomPattern());
            } catch (NumberFormatException e) {
                visibleChars = 1;
            }
        }

        int length = value.length();
        
        if (length <= visibleChars) {
            return repeatChar('*', length);
        }

        if (length <= visibleChars + 1) {
            return value.substring(0, visibleChars) + repeatChar('*', length - visibleChars);
        }

        return value.substring(0, visibleChars) + repeatChar('*', length - visibleChars - 1) + 
               value.substring(length - 1);
    }

    @Override
    public boolean supports(MaskingConfig config) {
        return config != null && MaskingType.NAME.equals(config.getMaskingType());
    }

    @Override
    public String getStrategyName() {
        return "NAME_MASKING";
    }

    private String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
