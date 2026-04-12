-- 积分规则表
CREATE TABLE IF NOT EXISTS incentive_rule (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    event_type  VARCHAR(50) NOT NULL,
    points      INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    status      VARCHAR(20) DEFAULT 'ACTIVE',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 积分记录表
CREATE TABLE IF NOT EXISTS points_record (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    points      INTEGER NOT NULL,
    event_type  VARCHAR(50),
    rule_id     BIGINT,
    remark      TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_points_user_id ON points_record(user_id);
CREATE INDEX idx_points_event_type ON points_record(event_type);
CREATE INDEX idx_points_create_time ON points_record(create_time);

-- 用户积分汇总表
CREATE TABLE IF NOT EXISTS user_points_summary (
    user_id      BIGINT PRIMARY KEY,
    total_points INTEGER DEFAULT 0,
    level        VARCHAR(20) DEFAULT 'BRONZE',
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始化常用积分规则
INSERT INTO incentive_rule(name, event_type, points, description, status) VALUES
('上传数据资产', 'ASSET_UPLOAD', 10, '每次上传数据资产获得10积分', 'ACTIVE'),
('完善元数据', 'METADATA_COMPLETE', 5, '完善资产元数据获得5积分', 'ACTIVE'),
('添加标签', 'TAG_ADD', 2, '为资产添加标签获得2积分', 'ACTIVE'),
('数据质量检测', 'QUALITY_CHECK', 8, '发起质量检测获得8积分', 'ACTIVE'),
('分享资产', 'ASSET_SHARE', 5, '分享数据资产获得5积分', 'ACTIVE')
ON CONFLICT DO NOTHING;
