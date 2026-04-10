package com.enterprise.edams.edgeiot.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据质量枚举
 */
@Getter
@AllArgsConstructor
public enum DataQuality {
    GOOD("优良"),
    FAIR("一般"),
    POOR("较差"),
    BAD("恶劣"),
    UNKNOWN("未知");
    
    private final String description;
}
