package com.enterprise.edams.edgeiot.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 网关状态枚举
 */
@Getter
@AllArgsConstructor
public enum GatewayStatus {
    CONNECTED("已连接"),
    DISCONNECTED("已断开"),
    SYNCING("同步中"),
    ERROR("异常"),
    MAINTENANCE("维护中");
    
    private final String description;
}
