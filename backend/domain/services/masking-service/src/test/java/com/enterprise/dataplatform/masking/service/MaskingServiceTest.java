package com.enterprise.dataplatform.masking.service;

import com.enterprise.dataplatform.masking.domain.entity.MaskingConfig;
import com.enterprise.dataplatform.masking.domain.enums.MaskingType;
import com.enterprise.dataplatform.masking.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("数据脱敏服务测试")
class MaskingServiceTest {

    private MaskingService maskingService;
    private List<MaskingStrategy> strategies;

    @BeforeEach
    void setUp() {
        strategies = Arrays.asList(
                new PhoneMaskingStrategy(),
                new EmailMaskingStrategy(),
                new IdCardMaskingStrategy(),
                new BankCardMaskingStrategy(),
                new HashMaskingStrategy(),
                new NameMaskingStrategy(),
                new AddressMaskingStrategy()
        );
        maskingService = new MaskingService(null, null, null);
    }

    @Test
    @DisplayName("手机号脱敏 - 13812345678 应显示为 138****5678")
    void testPhoneMasking() {
        PhoneMaskingStrategy strategy = new PhoneMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.PHONE)
                .build();

        String result = strategy.mask("13812345678", config);
        assertThat(result).isEqualTo("138****5678");
    }

    @Test
    @DisplayName("手机号脱敏 - 带特殊字符应正确处理")
    void testPhoneMaskingWithSpecialChars() {
        PhoneMaskingStrategy strategy = new PhoneMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.PHONE)
                .build();

        String result = strategy.mask("+86 138-1234-5678", config);
        assertThat(result).isEqualTo("138****5678");
    }

    @Test
    @DisplayName("手机号脱敏 - 短号码应全部脱敏")
    void testPhoneMaskingShortNumber() {
        PhoneMaskingStrategy strategy = new PhoneMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.PHONE)
                .build();

        String result = strategy.mask("12345", config);
        assertThat(result).hasSize(5);
        assertThat(result).containsOnly("*");
    }

    @Test
    @DisplayName("手机号脱敏 - null值应返回null")
    void testPhoneMaskingNull() {
        PhoneMaskingStrategy strategy = new PhoneMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.PHONE)
                .build();

        String result = strategy.mask(null, config);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("邮箱脱敏 - john@example.com 应显示为 j***@example.com")
    void testEmailMasking() {
        EmailMaskingStrategy strategy = new EmailMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.EMAIL)
                .build();

        String result = strategy.mask("john@example.com", config);
        assertThat(result).isEqualTo("j***@example.com");
    }

    @Test
    @DisplayName("邮箱脱敏 - 短用户名字段应正确处理")
    void testEmailMaskingShortLocal() {
        EmailMaskingStrategy strategy = new EmailMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.EMAIL)
                .build();

        String result = strategy.mask("a@example.com", config);
        assertThat(result).isEqualTo("a***@example.com");
    }

    @Test
    @DisplayName("邮箱脱敏 - 无@符号应返回原始值")
    void testEmailMaskingInvalidFormat() {
        EmailMaskingStrategy strategy = new EmailMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.EMAIL)
                .build();

        String result = strategy.mask("invalid-email", config);
        assertThat(result).hasSize("invalid-email".length());
        assertThat(result).containsOnly("*");
    }

    @Test
    @DisplayName("身份证号脱敏 - 18位身份证应正确脱敏")
    void testIdCardMasking() {
        IdCardMaskingStrategy strategy = new IdCardMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.ID_CARD)
                .build();

        String result = strategy.mask("110101199001011234", config);
        assertThat(result).isEqualTo("110101********1234");
    }

    @Test
    @DisplayName("身份证号脱敏 - 15位旧版身份证应正确脱敏")
    void testIdCardMaskingOld() {
        IdCardMaskingStrategy strategy = new IdCardMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.ID_CARD)
                .build();

        String result = strategy.mask("110101901011234", config);
        assertThat(result).isEqualTo("1101****1234");
    }

    @Test
    @DisplayName("银行卡号脱敏 - 应显示后4位")
    void testBankCardMasking() {
        BankCardMaskingStrategy strategy = new BankCardMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.BANK_CARD)
                .build();

        String result = strategy.mask("6222021234567890123", config);
        assertThat(result).isEqualTo("**** **** **** 0123");
    }

    @Test
    @DisplayName("银行卡号脱敏 - 短卡号应全部脱敏")
    void testBankCardMaskingShort() {
        BankCardMaskingStrategy strategy = new BankCardMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.BANK_CARD)
                .build();

        String result = strategy.mask("123", config);
        assertThat(result).hasSize(3);
        assertThat(result).containsOnly("*");
    }

    @Test
    @DisplayName("哈希脱敏 - SHA-256哈希应生成64位十六进制字符串")
    void testHashMasking() {
        HashMaskingStrategy strategy = new HashMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.HASH)
                .build();

        String result = strategy.mask("sensitive-data", config);
        assertThat(result).hasSize(64);
        assertThat(result).matches("[a-f0-9]{64}");
    }

    @Test
    @DisplayName("哈希脱敏 - MD5应生成32位十六进制字符串")
    void testHashMaskingMD5() {
        HashMaskingStrategy strategy = new HashMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.HASH)
                .customPattern("MD5")
                .build();

        String result = strategy.mask("sensitive-data", config);
        assertThat(result).hasSize(32);
        assertThat(result).matches("[a-f0-9]{32}");
    }

    @Test
    @DisplayName("姓名脱敏 - 应保留首尾字符")
    void testNameMasking() {
        NameMaskingStrategy strategy = new NameMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.NAME)
                .build();

        String result = strategy.mask("张三丰", config);
        assertThat(result).startsWith("张");
        assertThat(result).endsWith("丰");
        assertThat(result).contains("*");
    }

    @Test
    @DisplayName("姓名脱敏 - 双字姓名应正确脱敏")
    void testNameMaskingDoubleChar() {
        NameMaskingStrategy strategy = new NameMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.NAME)
                .build();

        String result = strategy.mask("欧阳", config);
        assertThat(result).startsWith("欧");
        assertThat(result).contains("*");
    }

    @Test
    @DisplayName("地址脱敏 - 应保留前缀")
    void testAddressMasking() {
        AddressMaskingStrategy strategy = new AddressMaskingStrategy();
        MaskingConfig config = MaskingConfig.builder()
                .maskingType(MaskingType.ADDRESS)
                .build();

        String result = strategy.mask("北京市朝阳区建国路88号SOHO现代城", config);
        assertThat(result).startsWith("北京市");
        assertThat(result).contains("*");
    }

    @Test
    @DisplayName("脱敏策略应支持正确的类型")
    void testStrategySupports() {
        PhoneMaskingStrategy phoneStrategy = new PhoneMaskingStrategy();
        IdCardMaskingStrategy idCardStrategy = new IdCardMaskingStrategy();
        
        MaskingConfig phoneConfig = MaskingConfig.builder().maskingType(MaskingType.PHONE).build();
        MaskingConfig idCardConfig = MaskingConfig.builder().maskingType(MaskingType.ID_CARD).build();
        MaskingConfig emailConfig = MaskingConfig.builder().maskingType(MaskingType.EMAIL).build();

        assertThat(phoneStrategy.supports(phoneConfig)).isTrue();
        assertThat(phoneStrategy.supports(idCardConfig)).isFalse();
        assertThat(idCardStrategy.supports(idCardConfig)).isTrue();
        assertThat(idCardStrategy.supports(emailConfig)).isFalse();
    }
}
