package com.enterprise.edams.model.constant;

/**
 * 模型层级枚举
 */
public enum ModelLevel {

    DOMAIN("DOMAIN", "领域层"),
    SUBJECT("SUBJECT", "主题层"),
    TEMPLATE("TEMPLATE", "模板层"),
    DATAMART("DATAMART", "数据集市层");

    private final String code;
    private final String description;

    ModelLevel(String code, String description) {
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
