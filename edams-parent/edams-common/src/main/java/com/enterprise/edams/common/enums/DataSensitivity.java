package com.enterprise.edams.common.enums;

/**
 * 数据敏感级别枚举
 *
 * @author Architecture Team
 * @version 1.0.0
 */
public enum DataSensitivity {
    /**
     * 公开
     */
    PUBLIC("PUBLIC", "公开", 0),

    /**
     * 内部
     */
    INTERNAL("INTERNAL", "内部", 1),

    /**
     * 机密
     */
    CONFIDENTIAL("CONFIDENTIAL", "机密", 2),

    /**
     * 绝密
     */
    SECRET("SECRET", "绝密", 3);

    private final String code;
    private final String description;
    private final int level;

    DataSensitivity(String code, String description, int level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }

    public static DataSensitivity fromCode(String code) {
        for (DataSensitivity sensitivity : values()) {
            if (sensitivity.code.equals(code)) {
                return sensitivity;
            }
        }
        return INTERNAL;
    }

    /**
     * 判断当前级别是否高于指定级别
     */
    public boolean higherThan(DataSensitivity other) {
        return this.level > other.level;
    }

    /**
     * 判断当前级别是否低于指定级别
     */
    public boolean lowerThan(DataSensitivity other) {
        return this.level < other.level;
    }
}
