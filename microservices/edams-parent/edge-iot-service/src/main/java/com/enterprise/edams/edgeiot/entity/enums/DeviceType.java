package com.enterprise.edams.edgeiot.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备类型枚举
 */
@Getter
@AllArgsConstructor
public enum DeviceType {
    SENSOR("传感器"),
    CAMERA("摄像头"),
    PLC("可编程控制器"),
    RFID_READER("RFID读写器"),
    BARCODE_SCANNER("条码扫描器"),
    GPS_TRACKER("GPS追踪器"),
    ENVIRONMENT_MONITOR("环境监测仪"),
    SMART_METER("智能电表"),
    GATEWAY("网关设备"),
    CONTROLLER("控制器"),
    ACTUATOR("执行器"),
    OTHER("其他");
    
    private final String description;
}
