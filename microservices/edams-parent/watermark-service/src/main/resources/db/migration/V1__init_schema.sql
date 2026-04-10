-- =====================================================
-- V1__init_schema.sql
-- 水印服务 - 初始化表结构
-- =====================================================

-- 水印模板表
CREATE TABLE IF NOT EXISTS watermark_template (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(64) NOT NULL UNIQUE,
    template_name VARCHAR(200) NOT NULL,
    watermark_type VARCHAR(32) NOT NULL,
    content TEXT,
    opacity DECIMAL(5, 2) DEFAULT 0.3,
    font_size INT DEFAULT 12,
    font_color VARCHAR(32) DEFAULT '#CCCCCC',
    rotation INT DEFAULT -30,
    position_x VARCHAR(32),
    position_y VARCHAR(32),
    repeatable BOOLEAN DEFAULT TRUE,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    description TEXT,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

-- 水印记录表
CREATE TABLE IF NOT EXISTS watermark_record (
    id BIGSERIAL PRIMARY KEY,
    record_no VARCHAR(64) NOT NULL UNIQUE,
    asset_id BIGINT,
    asset_name VARCHAR(200),
    file_type VARCHAR(32),
    original_path VARCHAR(500),
    watermarked_path VARCHAR(500),
    watermark_type VARCHAR(32) NOT NULL,
    template_id BIGINT,
    user_id BIGINT,
    user_name VARCHAR(100),
    dept_id BIGINT,
    watermark_content TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    process_time BIGINT,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

-- 泄露溯源表
CREATE TABLE IF NOT EXISTS leak_trace (
    id BIGSERIAL PRIMARY KEY,
    trace_no VARCHAR(64) NOT NULL UNIQUE,
    file_name VARCHAR(200),
    leak_type VARCHAR(32) NOT NULL,
    discovery_channel VARCHAR(100),
    discovery_time TIMESTAMP NOT NULL,
    watermark_record_id BIGINT,
    suspect_user_id BIGINT,
    suspect_user_name VARCHAR(100),
    suspect_dept VARCHAR(200),
    estimated_leak_time TIMESTAMP,
    confidence DECIMAL(5, 2),
    status VARCHAR(32) NOT NULL DEFAULT 'REPORTED',
    disposal_result VARCHAR(500),
    attachments TEXT,
    remark TEXT,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_watermark_template_type ON watermark_template(watermark_type);
CREATE INDEX IF NOT EXISTS idx_watermark_template_status ON watermark_template(status);

CREATE INDEX IF NOT EXISTS idx_watermark_record_asset_id ON watermark_record(asset_id);
CREATE INDEX IF NOT EXISTS idx_watermark_record_user_id ON watermark_record(user_id);
CREATE INDEX IF NOT EXISTS idx_watermark_record_created_time ON watermark_record(created_time DESC);

CREATE INDEX IF NOT EXISTS idx_leak_trace_status ON leak_trace(status);
CREATE INDEX IF NOT EXISTS idx_leak_trace_suspect_user ON leak_trace(suspect_user_id);
CREATE INDEX IF NOT EXISTS idx_leak_trace_discovery_time ON leak_trace(discovery_time DESC);
