-- =====================================================
-- V1__init_schema.sql
-- SLA监控服务 - 初始化表结构
-- =====================================================

-- SLA定义表
CREATE TABLE IF NOT EXISTS sla_definition (
    id BIGSERIAL PRIMARY KEY,
    sla_code VARCHAR(64) NOT NULL UNIQUE COMMENT 'SLA编码',
    sla_name VARCHAR(200) NOT NULL COMMENT 'SLA名称',
    asset_id BIGINT COMMENT '资产ID',
    sla_type VARCHAR(32) NOT NULL COMMENT 'SLA类型',
    target_value DECIMAL(14, 4) NOT NULL COMMENT '目标值',
    unit VARCHAR(32) COMMENT '单位',
    operator VARCHAR(32) NOT NULL COMMENT '比较操作符',
    window_type VARCHAR(32) NOT NULL COMMENT '窗口类型',
    window_size INT NOT NULL DEFAULT 1 COMMENT '窗口大小',
    severity VARCHAR(32) NOT NULL DEFAULT 'MEDIUM' COMMENT '严重程度',
    contact VARCHAR(500) COMMENT '联系人',
    alert_methods VARCHAR(500) COMMENT '告警方式',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    description TEXT COMMENT '描述',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

-- SLA违反记录表
CREATE TABLE IF NOT EXISTS sla_violation (
    id BIGSERIAL PRIMARY KEY,
    sla_definition_id BIGINT NOT NULL COMMENT 'SLA定义ID',
    sla_code VARCHAR(64) COMMENT 'SLA编码',
    asset_id BIGINT COMMENT '资产ID',
    violation_time TIMESTAMP NOT NULL COMMENT '违反时间',
    actual_value DECIMAL(14, 4) COMMENT '实际值',
    target_value DECIMAL(14, 4) COMMENT '目标值',
    violation_degree DECIMAL(5, 2) COMMENT '违反程度',
    severity VARCHAR(32) NOT NULL DEFAULT 'MEDIUM' COMMENT '严重程度',
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN' COMMENT '违反状态',
    alert_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '告警状态',
    escalation_status VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT '升级状态',
    handler VARCHAR(64) COMMENT '处理人',
    handle_time TIMESTAMP COMMENT '处理时间',
    handle_result VARCHAR(500) COMMENT '处理结果',
    remark TEXT COMMENT '备注',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

-- SLA指标记录表
CREATE TABLE IF NOT EXISTS sla_metric (
    id BIGSERIAL PRIMARY KEY,
    sla_definition_id BIGINT NOT NULL COMMENT 'SLA定义ID',
    asset_id BIGINT COMMENT '资产ID',
    metric_type VARCHAR(32) NOT NULL COMMENT '指标类型',
    metric_value DECIMAL(14, 4) NOT NULL COMMENT '指标值',
    unit VARCHAR(32) COMMENT '单位',
    timestamp TIMESTAMP NOT NULL COMMENT '采集时间',
    meets_target BOOLEAN COMMENT '是否达标',
    deviation DECIMAL(14, 4) COMMENT '偏差',
    data_source VARCHAR(100) COMMENT '数据来源',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

-- SLA告警记录表
CREATE TABLE IF NOT EXISTS sla_alert (
    id BIGSERIAL PRIMARY KEY,
    violation_id BIGINT NOT NULL COMMENT '违反记录ID',
    alert_type VARCHAR(32) NOT NULL COMMENT '告警类型',
    alert_channel VARCHAR(32) NOT NULL COMMENT '告警渠道',
    recipient VARCHAR(200) NOT NULL COMMENT '接收人',
    content TEXT COMMENT '告警内容',
    send_time TIMESTAMP COMMENT '发送时间',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    response_time TIMESTAMP COMMENT '响应时间',
    response VARCHAR(500) COMMENT '响应内容',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_sla_definition_sla_code ON sla_definition(sla_code);
CREATE INDEX IF NOT EXISTS idx_sla_definition_asset_id ON sla_definition(asset_id);
CREATE INDEX IF NOT EXISTS idx_sla_definition_status ON sla_definition(status);

CREATE INDEX IF NOT EXISTS idx_sla_violation_sla_definition_id ON sla_violation(sla_definition_id);
CREATE INDEX IF NOT EXISTS idx_sla_violation_status ON sla_violation(status);
CREATE INDEX IF NOT EXISTS idx_sla_violation_violation_time ON sla_violation(violation_time);

CREATE INDEX IF NOT EXISTS idx_sla_metric_sla_definition_id ON sla_metric(sla_definition_id);
CREATE INDEX IF NOT EXISTS idx_sla_metric_timestamp ON sla_metric(timestamp);
