package com.enterprise.dataplatform.classification.domain.enums;

public enum ClassificationRuleType {
    PATTERN_MATCH("正则匹配"),
    DATA_TYPE("数据类型"),
    KEYWORD("关键词"),
    CONTENT_ANALYSIS("内容分析"),
    COLUMN_NAME("列名匹配"),
    USER_DEFINED("用户定义");

    private final String description;

    ClassificationRuleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
