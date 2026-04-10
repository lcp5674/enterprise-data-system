package com.enterprise.edams.datasource.constant;

/**
 * 数据源状态枚举
 */
public enum DatasourceStatus {
    /**
     * 活跃
     */
    ACTIVE("ACTIVE", "活跃"),
    
    /**
     * 未激活
     */
    INACTIVE("INACTIVE", "未激活"),
    
    /**
     * 已禁用
     */
    DISABLED("DISABLED", "已禁用");

    private final String code;
    private final String description;

    DatasourceStatus(String code, String description) {
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
