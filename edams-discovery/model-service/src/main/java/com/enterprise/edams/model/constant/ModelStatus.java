package com.enterprise.edams.model.constant;

/**
 * 模型状态枚举
 */
public enum ModelStatus {

    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    DEPRECATED("DEPRECATED", "已废弃"),
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String description;

    ModelStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
