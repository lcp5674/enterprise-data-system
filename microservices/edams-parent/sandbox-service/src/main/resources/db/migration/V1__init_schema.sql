-- =====================================================
-- V1__init_schema.sql
-- 沙箱服务 - 初始化表结构
-- =====================================================

-- 沙箱实例表
CREATE TABLE IF NOT EXISTS sandbox_instance (
    id BIGSERIAL PRIMARY KEY,
    instance_code VARCHAR(64) NOT NULL UNIQUE,
    instance_name VARCHAR(200) NOT NULL,
    sandbox_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATING',
    user_id BIGINT,
    user_name VARCHAR(100),
    asset_ids TEXT,
    description TEXT,
    expire_time TIMESTAMP,
    deleted_time TIMESTAMP,
    resource_config JSONB,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

-- 样本数据请求表
CREATE TABLE IF NOT EXISTS sample_data_request (
    id BIGSERIAL PRIMARY KEY,
    request_no VARCHAR(64) NOT NULL UNIQUE,
    asset_id BIGINT,
    asset_name VARCHAR(200),
    user_id BIGINT,
    user_name VARCHAR(100),
    sample_type VARCHAR(32) NOT NULL,
    sample_count INT,
    size_limit DECIMAL(10, 2),
    desensitization_rule_id BIGINT,
    purpose VARCHAR(500),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    process_time TIMESTAMP,
    complete_time TIMESTAMP,
    download_url VARCHAR(500),
    remark TEXT,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

-- 脱敏规则表
CREATE TABLE IF NOT EXISTS desensitization_rule (
    id BIGSERIAL PRIMARY KEY,
    rule_code VARCHAR(64) NOT NULL UNIQUE,
    rule_name VARCHAR(200) NOT NULL,
    data_type VARCHAR(32) NOT NULL,
    method VARCHAR(32) NOT NULL,
    params JSONB,
    priority INT DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    description TEXT,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_sandbox_instance_code ON sandbox_instance(instance_code);
CREATE INDEX IF NOT EXISTS idx_sandbox_instance_user ON sandbox_instance(user_id);
CREATE INDEX IF NOT EXISTS idx_sandbox_instance_status ON sandbox_instance(status);
CREATE INDEX IF NOT EXISTS idx_sandbox_instance_expire ON sandbox_instance(expire_time);

CREATE INDEX IF NOT EXISTS idx_sample_request_no ON sample_data_request(request_no);
CREATE INDEX IF NOT EXISTS idx_sample_request_user ON sample_data_request(user_id);
CREATE INDEX IF NOT EXISTS idx_sample_request_asset ON sample_data_request(asset_id);

CREATE INDEX IF NOT EXISTS idx_desensitization_rule_code ON desensitization_rule(rule_code);
CREATE INDEX IF NOT EXISTS idx_desensitization_rule_type ON desensitization_rule(data_type);
