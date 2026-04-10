-- =====================================================
-- V1__init_schema.sql
-- 边缘计算与IoT数据管理服务 - 初始化表结构
-- =====================================================

-- 边缘设备表
CREATE TABLE IF NOT EXISTS edge_device (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL UNIQUE COMMENT '设备ID',
    device_name VARCHAR(200) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(32) NOT NULL COMMENT '设备类型',
    status VARCHAR(32) NOT NULL DEFAULT 'OFFLINE' COMMENT '设备状态',
    gateway_id VARCHAR(64) COMMENT '网关ID',
    location VARCHAR(500) COMMENT '设备位置',
    latitude DECIMAL(10, 6) COMMENT '纬度',
    longitude DECIMAL(10, 6) COMMENT '经度',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    mac_address VARCHAR(64) COMMENT 'MAC地址',
    firmware_version VARCHAR(64) COMMENT '固件版本',
    last_heartbeat TIMESTAMP COMMENT '最后心跳时间',
    last_sync_time TIMESTAMP COMMENT '最后同步时间',
    sensor_count INT DEFAULT 0 COMMENT '传感器数量',
    cpu_usage DECIMAL(5, 2) DEFAULT 0 COMMENT 'CPU使用率',
    memory_usage DECIMAL(5, 2) DEFAULT 0 COMMENT '内存使用率',
    disk_usage DECIMAL(5, 2) DEFAULT 0 COMMENT '磁盘使用率',
    properties JSON COMMENT '扩展属性',
    description TEXT COMMENT '设备描述',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '更新人',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INTEGER NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    version INTEGER NOT NULL DEFAULT 0 COMMENT '乐观锁版本'
);

-- 边缘网关表
CREATE TABLE IF NOT EXISTS edge_gateway (
    id BIGSERIAL PRIMARY KEY,
    gateway_id VARCHAR(64) NOT NULL UNIQUE COMMENT '网关ID',
    gateway_name VARCHAR(200) NOT NULL COMMENT '网关名称',
    gateway_type VARCHAR(32) NOT NULL COMMENT '网关类型',
    status VARCHAR(32) NOT NULL DEFAULT 'DISCONNECTED' COMMENT '状态',
    location VARCHAR(500) COMMENT '部署位置',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    protocol VARCHAR(32) COMMENT '连接协议',
    firmware_version VARCHAR(64) COMMENT '固件版本',
    last_sync_time TIMESTAMP COMMENT '最后同步时间',
    device_count INT DEFAULT 0 COMMENT '连接设备数',
    sync_status VARCHAR(32) DEFAULT 'PENDING' COMMENT '同步状态',
    cpu_usage DECIMAL(5, 2) DEFAULT 0 COMMENT 'CPU使用率',
    memory_usage DECIMAL(5, 2) DEFAULT 0 COMMENT '内存使用率',
    storage_usage DECIMAL(5, 2) DEFAULT 0 COMMENT '存储使用率',
    properties JSON COMMENT '扩展属性',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '更新人',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INTEGER NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    version INTEGER NOT NULL DEFAULT 0 COMMENT '乐观锁版本'
);

-- 传感器读数表
CREATE TABLE IF NOT EXISTS sensor_reading (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    sensor_id VARCHAR(64) NOT NULL COMMENT '传感器ID',
    sensor_type VARCHAR(32) NOT NULL COMMENT '传感器类型',
    sensor_name VARCHAR(200) COMMENT '传感器名称',
    value DECIMAL(20, 6) NOT NULL COMMENT '读数值',
    unit VARCHAR(32) COMMENT '单位',
    quality VARCHAR(32) DEFAULT 'GOOD' COMMENT '数据质量',
    reading_time TIMESTAMP NOT NULL COMMENT '采集时间',
    asset_id BIGINT COMMENT '关联数据资产ID',
    tags JSON COMMENT '标签',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '更新人',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INTEGER NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    version INTEGER NOT NULL DEFAULT 0 COMMENT '乐观锁版本'
);

-- 设备元数据表
CREATE TABLE IF NOT EXISTS device_metadata (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    metadata_key VARCHAR(64) NOT NULL COMMENT '元数据键',
    metadata_value TEXT COMMENT '元数据值',
    metadata_type VARCHAR(32) DEFAULT 'STRING' COMMENT '数据类型',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '创建人',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '更新人',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INTEGER NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    version INTEGER NOT NULL DEFAULT 0 COMMENT '乐观锁版本'
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_edge_device_device_id ON edge_device(device_id);
CREATE INDEX IF NOT EXISTS idx_edge_device_status ON edge_device(status);
CREATE INDEX IF NOT EXISTS idx_edge_device_gateway_id ON edge_device(gateway_id);
CREATE INDEX IF NOT EXISTS idx_edge_device_created_time ON edge_device(created_time);

CREATE INDEX IF NOT EXISTS idx_edge_gateway_gateway_id ON edge_gateway(gateway_id);
CREATE INDEX IF NOT EXISTS idx_edge_gateway_status ON edge_gateway(status);

CREATE INDEX IF NOT EXISTS idx_sensor_reading_device_id ON sensor_reading(device_id);
CREATE INDEX IF NOT EXISTS idx_sensor_reading_sensor_id ON sensor_reading(sensor_id);
CREATE INDEX IF NOT EXISTS idx_sensor_reading_reading_time ON sensor_reading(reading_time);
CREATE INDEX IF NOT EXISTS idx_sensor_reading_asset_id ON sensor_reading(asset_id);
