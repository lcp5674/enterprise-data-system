package com.enterprise.dataplatform.ethics.domain.enums;

public enum EthicsScore {
    EXCELLENT("EXCELLENT", "优秀", 5),
    GOOD("GOOD", "良好", 4),
    FAIR("FAIR", "一般", 3),
    POOR("POOR", "较差", 2),
    VERY_POOR("VERY_POOR", "很差", 1);

    private final String code;
    private final String description;
    private final int value;

    EthicsScore(String code, String description, int value) {
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

    public static EthicsScore fromValue(int value) {
        for (EthicsScore score : values()) {
            if (score.value == value) {
                return score;
            }
        }
        if (value >= 4.5) return EXCELLENT;
        if (value >= 3.5) return GOOD;
        if (value >= 2.5) return FAIR;
        if (value >= 1.5) return POOR;
        return VERY_POOR;
    }

    public static EthicsScore fromCode(String code) {
        for (EthicsScore score : values()) {
            if (score.code.equals(code)) {
                return score;
            }
        }
        throw new IllegalArgumentException("Unknown EthicsScore code: " + code);
    }
}
