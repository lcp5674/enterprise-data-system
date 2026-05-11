package com.enterprise.dataplatform.ethics.domain.enums;

public enum EthicsLevel {
    CRITICAL("CRITICAL", "关键级别"),
    HIGH("HIGH", "高风险"),
    MEDIUM("MEDIUM", "中等风险"),
    LOW("LOW", "低风险"),
    MINIMAL("MINIMAL", "最小风险");

    private final String code;
    private final String description;

    EthicsLevel(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static EthicsLevel fromCode(String code) {
        for (EthicsLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown EthicsLevel code: " + code);
    }
}
