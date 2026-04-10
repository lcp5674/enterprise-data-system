-- =====================================================
-- V1__init_schema.sql
-- 激励服务 - 初始化表结构
-- =====================================================

-- 用户积分表
CREATE TABLE IF NOT EXISTS user_points (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户名',
    balance DECIMAL(12, 2) DEFAULT 0 COMMENT '积分余额',
    total_earned DECIMAL(12, 2) DEFAULT 0 COMMENT '累计获得',
    total_spent DECIMAL(12, 2) DEFAULT 0 COMMENT '累计消耗',
    level INT DEFAULT 1 COMMENT '等级',
    experience DECIMAL(12, 2) DEFAULT 0 COMMENT '经验值',
    title VARCHAR(50) DEFAULT '新手' COMMENT '头衔',
    consecutive_days INT DEFAULT 0 COMMENT '连续签到天数',
    last_sign_in TIMESTAMP COMMENT '最后签到时间',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

-- 积分交易记录表
CREATE TABLE IF NOT EXISTS point_transaction (
    id BIGSERIAL PRIMARY KEY,
    transaction_no VARCHAR(64) NOT NULL UNIQUE COMMENT '交易编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(32) NOT NULL COMMENT '交易类型',
    points DECIMAL(12, 2) NOT NULL COMMENT '积分数量',
    balance_after DECIMAL(12, 2) COMMENT '交易后余额',
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    biz_id VARCHAR(64) COMMENT '关联业务ID',
    description VARCHAR(500) COMMENT '描述',
    status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED' COMMENT '状态',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

-- 奖励规则表
CREATE TABLE IF NOT EXISTS reward_rule (
    id BIGSERIAL PRIMARY KEY,
    rule_code VARCHAR(64) NOT NULL UNIQUE COMMENT '规则编码',
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    reward_points DECIMAL(12, 2) NOT NULL COMMENT '奖励积分',
    unit VARCHAR(32) DEFAULT '次' COMMENT '单位',
    daily_limit DECIMAL(12, 2) COMMENT '每日上限',
    total_limit DECIMAL(12, 2) COMMENT '总上限',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    description TEXT COMMENT '描述',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_user_points_user_id ON user_points(user_id);
CREATE INDEX IF NOT EXISTS idx_user_points_balance ON user_points(balance DESC);

CREATE INDEX IF NOT EXISTS idx_point_transaction_user_id ON point_transaction(user_id);
CREATE INDEX IF NOT EXISTS idx_point_transaction_type ON point_transaction(type);
CREATE INDEX IF NOT EXISTS idx_point_transaction_created_time ON point_transaction(created_time DESC);

CREATE INDEX IF NOT EXISTS idx_reward_rule_biz_type ON reward_rule(biz_type);
CREATE INDEX IF NOT EXISTS idx_reward_rule_status ON reward_rule(status);

-- 初始化默认奖励规则
INSERT INTO reward_rule (rule_code, rule_name, biz_type, reward_points, unit, daily_limit, description) VALUES
('RULE_001', '数据质量检测', 'DATA_QUALITY', 20, '次', 100, '完成数据质量检测任务'),
('RULE_002', '血缘完整度维护', 'LINEAGE_COMPLETE', 15, '次', 50, '完善数据血缘关系'),
('RULE_003', '元数据完善', 'METADATA_COMPLETE', 10, '条', 200, '完善资产元数据'),
('RULE_004', '任务完成', 'TASK_COMPLETE', 5, '个', 50, '完成任务'),
('RULE_005', '审核通过', 'REVIEW_PASS', 10, '次', 30, '提交内容审核通过'),
('RULE_006', '知识贡献', 'CONTRIBUTION', 30, '篇', 20, '贡献知识文章'),
('RULE_007', '每日签到', 'SIGN_IN', 10, '天', 1, '每日签到');
