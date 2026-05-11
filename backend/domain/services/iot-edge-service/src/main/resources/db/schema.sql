-- IoT Edge Device Management Schema
-- MySQL Database Schema for Edge Device Registration and Management

CREATE DATABASE IF NOT EXISTS edams_iot DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE edams_iot;

-- Device Table
CREATE TABLE IF NOT EXISTS edge_device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL UNIQUE COMMENT 'Device unique identifier',
    device_name VARCHAR(128) NOT NULL COMMENT 'Device display name',
    device_type VARCHAR(64) NOT NULL COMMENT 'Device type (sensor, gateway, controller, etc.)',
    manufacturer VARCHAR(128) COMMENT 'Device manufacturer',
    model VARCHAR(128) COMMENT 'Device model',
    serial_number VARCHAR(128) COMMENT 'Device serial number',
    firmware_version VARCHAR(64) COMMENT 'Firmware version',
    hardware_version VARCHAR(64) COMMENT 'Hardware version',
    status VARCHAR(32) DEFAULT 'INACTIVE' COMMENT 'Device status: ACTIVE, INACTIVE, MAINTENANCE, FAULT, UNKNOWN',
    online TINYINT(1) DEFAULT 0 COMMENT 'Online status',
    last_heartbeat DATETIME COMMENT 'Last heartbeat time',
    ip_address VARCHAR(45) COMMENT 'IP address (supports IPv6)',
    mac_address VARCHAR(32) COMMENT 'MAC address',
    location VARCHAR(256) COMMENT 'Physical location description',
    latitude DECIMAL(10, 8) COMMENT 'Latitude coordinate',
    longitude DECIMAL(11, 8) COMMENT 'Longitude coordinate',
    tags JSON COMMENT 'Device tags as JSON',
    groups JSON COMMENT 'Device group IDs as JSON',
    properties JSON COMMENT 'Device custom properties as JSON',
    auth_token VARCHAR(256) COMMENT 'Device authentication token',
    auth_type VARCHAR(32) DEFAULT 'TOKEN' COMMENT 'Authentication type: TOKEN, CERTIFICATE, USERNAME_PASSWORD, API_KEY',
    connection_info VARCHAR(512) COMMENT 'Connection information',
    capabilities JSON COMMENT 'Device capabilities as JSON',
    data_threshold JSON COMMENT 'Data threshold configuration as JSON',
    sync_priority VARCHAR(16) DEFAULT 'WARM' COMMENT 'Data sync priority: HOT, WARM, COLD',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(64) COMMENT 'Creator user ID',
    updated_by VARCHAR(64) COMMENT 'Updater user ID',
    deleted TINYINT(1) DEFAULT 0 COMMENT 'Soft delete flag',
    INDEX idx_device_id (device_id),
    INDEX idx_device_type (device_type),
    INDEX idx_status (status),
    INDEX idx_online (online),
    INDEX idx_serial_number (serial_number),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Edge device information table';

-- Device Group Table (MySQL backup for synchronization)
CREATE TABLE IF NOT EXISTS device_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id VARCHAR(64) NOT NULL UNIQUE COMMENT 'Group unique identifier',
    group_name VARCHAR(128) NOT NULL COMMENT 'Group name',
    description VARCHAR(512) COMMENT 'Group description',
    parent_id VARCHAR(64) COMMENT 'Parent group ID',
    hierarchy_path VARCHAR(512) COMMENT 'Hierarchy path',
    device_count INT DEFAULT 0 COMMENT 'Number of devices in group',
    tags JSON COMMENT 'Group tags as JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) DEFAULT 0 COMMENT 'Soft delete flag',
    INDEX idx_group_id (group_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Device group table';

-- Sync Record Table (MySQL backup for synchronization)
CREATE TABLE IF NOT EXISTS sync_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL COMMENT 'Device ID',
    sync_type VARCHAR(32) NOT NULL COMMENT 'Sync type: FULL, INCREMENTAL, DELTA',
    sync_direction VARCHAR(32) NOT NULL COMMENT 'Sync direction: EDGE_TO_CENTER, CENTER_TO_EDGE',
    record_count BIGINT DEFAULT 0 COMMENT 'Number of records synced',
    data_size_bytes BIGINT DEFAULT 0 COMMENT 'Original data size in bytes',
    compressed_size_bytes BIGINT DEFAULT 0 COMMENT 'Compressed data size in bytes',
    compression_ratio DECIMAL(5, 2) COMMENT 'Compression ratio',
    status VARCHAR(32) DEFAULT 'PENDING' COMMENT 'Sync status: PENDING, RUNNING, COMPLETED, FAILED, CANCELLED',
    error_message TEXT COMMENT 'Error message if failed',
    retry_count INT DEFAULT 0 COMMENT 'Retry count',
    started_at DATETIME COMMENT 'Sync start time',
    completed_at DATETIME COMMENT 'Sync completion time',
    duration_ms BIGINT COMMENT 'Duration in milliseconds',
    priority VARCHAR(16) COMMENT 'Sync priority',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_id (device_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_sync_type (sync_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Data sync record table';

-- Device Data Table (MySQL backup for temporary storage)
CREATE TABLE IF NOT EXISTS device_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL COMMENT 'Device ID',
    data_type VARCHAR(64) COMMENT 'Data type',
    data_key VARCHAR(128) COMMENT 'Data key',
    data_value TEXT COMMENT 'Data value',
    unit VARCHAR(32) COMMENT 'Data unit',
    timestamp DATETIME COMMENT 'Data timestamp',
    quality VARCHAR(32) DEFAULT 'GOOD' COMMENT 'Data quality: GOOD, UNCERTAIN, BAD, NO_DATA',
    tags JSON COMMENT 'Data tags as JSON',
    metadata JSON COMMENT 'Data metadata as JSON',
    sync_status VARCHAR(32) DEFAULT 'PENDING' COMMENT 'Sync status: PENDING, SYNCING, SYNCED, FAILED',
    sync_time DATETIME COMMENT 'Sync time',
    compressed TINYINT(1) DEFAULT 0 COMMENT 'Is compressed',
    compression_ratio DECIMAL(5, 2) COMMENT 'Compression ratio',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_id (device_id),
    INDEX idx_data_type (data_type),
    INDEX idx_timestamp (timestamp),
    INDEX idx_sync_status (sync_status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Device data temporary storage table';

-- Alert History Table
CREATE TABLE IF NOT EXISTS alert_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_id VARCHAR(64) NOT NULL UNIQUE COMMENT 'Alert unique identifier',
    device_id VARCHAR(64) NOT NULL COMMENT 'Device ID',
    alert_type VARCHAR(64) NOT NULL COMMENT 'Alert type',
    alert_level VARCHAR(32) NOT NULL COMMENT 'Alert level: INFO, WARNING, ERROR, CRITICAL',
    message TEXT NOT NULL COMMENT 'Alert message',
    alert_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Alert time',
    acknowledged TINYINT(1) DEFAULT 0 COMMENT 'Is acknowledged',
    acknowledged_by VARCHAR(64) COMMENT 'Acknowledged by user',
    acknowledged_at DATETIME COMMENT 'Acknowledged time',
    INDEX idx_device_id (device_id),
    INDEX idx_alert_level (alert_level),
    INDEX idx_alert_time (alert_time),
    INDEX idx_acknowledged (acknowledged)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Alert history table';

-- Device Command Table
CREATE TABLE IF NOT EXISTS device_command (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    command_id VARCHAR(64) NOT NULL UNIQUE COMMENT 'Command unique identifier',
    device_id VARCHAR(64) NOT NULL COMMENT 'Target device ID',
    command VARCHAR(128) NOT NULL COMMENT 'Command name',
    params JSON COMMENT 'Command parameters',
    status VARCHAR(32) DEFAULT 'PENDING' COMMENT 'Command status: PENDING, SENT, ACKNOWLEDGED, COMPLETED, FAILED',
    result TEXT COMMENT 'Command result',
    sent_at DATETIME COMMENT 'Command sent time',
    completed_at DATETIME COMMENT 'Command completed time',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_id (device_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Device command table';
