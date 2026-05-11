package com.enterprise.dataplatform.ethics.domain.enums;

public enum RiskLevel {
    CRITICAL("CRITICAL", "严重风险", 5),
    HIGH("HIGH", "高风险", 4),
    MEDIUM("MEDIUM", "中等风险", 3),
    LOW("LOW", "低风险", 2),
    MINIMAL("MINIMAL", "最小风险", 1);

    private final String code;
    private final String description;
    private final int value;

    RiskLevel(String code, String description, int value) {
        this.code = code;
        this.description = description;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getValue() {
        return value;
    }

    public static RiskLevel fromValue(int value) {
        for (RiskLevel level : values()) {
            if (level.value == value) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown RiskLevel value: " + value);
    }

    public static RiskLevel fromCode(String code) {
        for (RiskLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown RiskLevel code: " + code);
    }
}
