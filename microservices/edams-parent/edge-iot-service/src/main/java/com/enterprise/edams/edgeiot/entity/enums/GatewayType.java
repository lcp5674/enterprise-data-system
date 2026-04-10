package com.enterprise.edams.edgeiot.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 网关类型枚举
 */
@Getter
@AllArgsConstructor
public enum GatewayType {
    MQTT_BROKER("MQTT代理"),
    OPC_UA_SERVER("OPC UA服务器"),
    MODBUS_GATEWAY("Modbus网关"),
    HTTP_GATEWAY("HTTP网关"),
    EDGE_COMPUTING("边缘计算网关"),
    INDUSTRIAL_IOT("工业物联网网关"),
    OTHER("其他");
    
    private final String description;
}
