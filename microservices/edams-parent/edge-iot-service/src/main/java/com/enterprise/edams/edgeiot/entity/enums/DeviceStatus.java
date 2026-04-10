package com.enterprise.edams.edgeiot.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备状态枚举
 */
@Getter
@AllArgsConstructor
public enum DeviceStatus {
    ONLINE("在线"),
    OFFLINE("离线"),
    MAINTENANCE("维护中"),
    UPGRADING("升级中"),
    FAULT("故障"),
    UNKNOWN("未知");
    
    private final String description;
}
