package com.enterprise.edams.datasource.constant;

/**
 * 健康状态枚举
 */
public enum HealthStatus {
    /**
     * 健康
     */
    HEALTHY("HEALTHY", "健康"),
    
    /**
     * 不健康
     */
    UNHEALTHY("UNHEALTHY", "不健康"),
    
    /**
     * 未知
     */
    UNKNOWN("UNKNOWN", "未知");

    private final String code;
    private final String description;

    HealthStatus(String code, String description) {
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
