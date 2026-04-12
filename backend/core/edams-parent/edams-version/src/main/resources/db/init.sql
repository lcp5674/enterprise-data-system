-- 版本管理表
CREATE TABLE IF NOT EXISTS data_version (
    id          BIGSERIAL PRIMARY KEY,
    asset_id    BIGINT NOT NULL,
    version     VARCHAR(50) NOT NULL,
    status      VARCHAR(20) DEFAULT 'DRAFT',
    change_type VARCHAR(50),
    change_desc TEXT,
    content     JSONB,
    checksum    VARCHAR(64),
    created_by  BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (asset_id, version)
);

CREATE INDEX idx_version_asset_id ON data_version(asset_id);
CREATE INDEX idx_version_status ON data_version(status);
CREATE INDEX idx_version_create_time ON data_version(create_time);

-- 版本差异记录表
CREATE TABLE IF NOT EXISTS version_diff (
    id          BIGSERIAL PRIMARY KEY,
    asset_id    BIGINT NOT NULL,
    from_ver    VARCHAR(50),
    to_ver      VARCHAR(50) NOT NULL,
    diff_type   VARCHAR(50),
    diff_detail JSONB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_diff_asset_id ON version_diff(asset_id);
