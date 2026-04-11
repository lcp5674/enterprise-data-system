package com.enterprise.edams.common.enums;

/**
 * 资产状态枚举
 *
 * @author Architecture Team
 * @version 1.0.0
 */
public enum AssetStatus {
    /**
     * 草稿
     */
    DRAFT("DRAFT", "草稿"),

    /**
     * 已发布
     */
    PUBLISHED("PUBLISHED", "已发布"),

    /**
     * 已废弃
     */
    DEPRECATED("DEPRECATED", "已废弃"),

    /**
     * 已归档
     */
    ARCHIVED("ARCHIVED", "已归档"),

    /**
     * 审核中
     */
    UNDER_REVIEW("UNDER_REVIEW", "审核中"),

    /**
     * 已拒绝
     */
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String description;

    AssetStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AssetStatus fromCode(String code) {
        for (AssetStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
