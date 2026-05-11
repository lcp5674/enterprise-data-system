package com.enterprise.dataplatform.classification.domain.enums;

public enum SensitivityLevel {
    PUBLIC("公开", 1),
    INTERNAL("内部", 2),
    CONFIDENTIAL("机密", 3),
    HIGHLY_CONFIDENTIAL("高度机密", 4);

    private final String description;
    private final int level;

    SensitivityLevel(String description, int level) {
        this.description = description;
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }
}
