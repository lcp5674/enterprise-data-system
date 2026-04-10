package com.enterprise.edams.edgeiot.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 同步状态枚举
 */
@Getter
@AllArgsConstructor
public enum SyncStatus {
    SYNCED("已同步"),
    PENDING("待同步"),
    SYNCING("同步中"),
    FAILED("同步失败"),
    PARTIAL("部分同步");
    
    private final String description;
}
