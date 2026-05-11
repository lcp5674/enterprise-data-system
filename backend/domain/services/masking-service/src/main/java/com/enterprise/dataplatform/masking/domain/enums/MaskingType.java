package com.enterprise.dataplatform.masking.domain.enums;

public enum MaskingType {
    PHONE("手机号脱敏"),
    EMAIL("邮箱脱敏"),
    ID_CARD("身份证号脱敏"),
    BANK_CARD("银行卡号脱敏"),
    ADDRESS("地址脱敏"),
    NAME("姓名脱敏"),
    HASH("哈希脱敏"),
    PARTIAL("部分脱敏"),
    CUSTOM("自定义脱敏");

    private final String description;

    MaskingType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
