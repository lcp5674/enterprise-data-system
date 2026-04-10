package com.enterprise.edams.edgeiot.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 传感器类型枚举
 */
@Getter
@AllArgsConstructor
public enum SensorType {
    TEMPERATURE("温度传感器"),
    HUMIDITY("湿度传感器"),
    PRESSURE("压力传感器"),
    VIBRATION("振动传感器"),
    GAS("气体传感器"),
    LIGHT("光照传感器"),
    SOUND("声音传感器"),
    MOTION("运动传感器"),
    PROXIMITY("接近传感器"),
    LEVEL("液位传感器"),
    FLOW("流量传感器"),
    WEIGHT("称重传感器"),
    ELECTRICITY("电流传感器"),
    VOLTAGE("电压传感器"),
    OTHER("其他");
    
    private final String description;
}
