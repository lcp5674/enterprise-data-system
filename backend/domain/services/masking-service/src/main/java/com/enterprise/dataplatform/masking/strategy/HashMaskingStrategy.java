package com.enterprise.dataplatform.masking.strategy;

import com.enterprise.dataplatform.masking.domain.entity.MaskingConfig;
import com.enterprise.dataplatform.masking.domain.enums.MaskingType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HashMaskingStrategy implements MaskingStrategy {

    @Override
    public String mask(String value, MaskingConfig config) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        String algorithm = "SHA-256";
        if (config != null && config.getCustomPattern() != null) {
            algorithm = config.getCustomPattern();
        }

        try {
            return switch (algorithm.toUpperCase()) {
                case "SHA-256", "SHA256" -> DigestUtils.sha256Hex(value);
                case "SHA-512", "SHA512" -> DigestUtils.sha512Hex(value);
                case "MD5" -> DigestUtils.md5Hex(value);
                default -> DigestUtils.sha256Hex(value);
            };
        } catch (Exception e) {
            log.error("Error hashing value", e);
            return "***HASH_ERROR***";
        }
    }

    @Override
    public boolean supports(MaskingConfig config) {
        return config != null && MaskingType.HASH.equals(config.getMaskingType());
    }

    @Override
    public String getStrategyName() {
        return "HASH_MASKING";
    }
}
