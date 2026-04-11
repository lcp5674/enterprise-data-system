-- 数据源配置表
CREATE TABLE IF NOT EXISTS datasource_config (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '数据源编码',
    datasource_type VARCHAR(50) NOT NULL COMMENT '数据源类型',
    host VARCHAR(255) COMMENT '主机地址',
    port INTEGER COMMENT '端口',
    database_name VARCHAR(100) COMMENT '数据库名称',
    username VARCHAR(100) COMMENT '用户名',
    password_enc VARCHAR(500) COMMENT '加密后的密码',
    connection_url VARCHAR(1000) COMMENT 'JDBC连接URL',
    properties TEXT COMMENT '扩展属性(JSON)',
    jdbc_params TEXT COMMENT 'JDBC参数(JSON)',
    http_headers TEXT COMMENT 'HTTP请求头(JSON)',
    auth_type VARCHAR(50) COMMENT '认证类型',
    status VARCHAR(20) DEFAULT 'INACTIVE' COMMENT '状态',
    health_status VARCHAR(20) DEFAULT 'UNKNOWN' COMMENT '健康状态',
    last_test_time TIMESTAMP COMMENT '最后测试时间',
    last_test_result TEXT COMMENT '最后测试结果(JSON)',
    last_sync_time TIMESTAMP COMMENT '最后同步时间',
    sync_interval INTEGER DEFAULT 60 COMMENT '同步间隔(分钟)',
    sync_enabled INTEGER DEFAULT 0 COMMENT '是否启用同步',
    catalog_name VARCHAR(100) COMMENT '目录名称',
    description TEXT COMMENT '描述',
    tags TEXT COMMENT '标签(JSON)',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system' COMMENT '创建人',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INTEGER DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '数据源配置表';

-- 创建索引
CREATE INDEX idx_datasource_config_code ON datasource_config(code);
CREATE INDEX idx_datasource_config_type ON datasource_config(datasource_type);
CREATE INDEX idx_datasource_config_status ON datasource_config(status);
CREATE INDEX idx_datasource_config_catalog ON datasource_config(catalog_name);
CREATE INDEX idx_datasource_config_deleted ON datasource_config(deleted);

-- 数据源凭证表
CREATE TABLE IF NOT EXISTS datasource_credential (
    id BIGSERIAL PRIMARY KEY,
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    credential_type VARCHAR(50) NOT NULL COMMENT '凭证类型',
    key_store_name VARCHAR(100) COMMENT '密钥库名称',
    key_identifier VARCHAR(200) COMMENT '密钥标识',
    key_version VARCHAR(50) COMMENT '密钥版本',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '凭证状态',
    expire_time TIMESTAMP COMMENT '过期时间',
    last_rotated_time TIMESTAMP COMMENT '最后轮换时间',
    next_rotation_time TIMESTAMP COMMENT '下次轮换时间',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system' COMMENT '创建人',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INTEGER DEFAULT 0 COMMENT '逻辑删除',
    CONSTRAINT fk_credential_datasource FOREIGN KEY (datasource_id) REFERENCES datasource_config(id) ON DELETE CASCADE
) COMMENT '数据源凭证表';

-- 创建索引
CREATE INDEX idx_credential_datasource ON datasource_credential(datasource_id);
CREATE INDEX idx_credential_key_identifier ON datasource_credential(key_identifier);
CREATE INDEX idx_credential_deleted ON datasource_credential(deleted);

-- 数据源同步任务表
CREATE TABLE IF NOT EXISTS datasource_sync_task (
    id BIGSERIAL PRIMARY KEY,
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    task_name VARCHAR(200) COMMENT '任务名称',
    task_type VARCHAR(50) DEFAULT 'MANUAL' COMMENT '任务类型',
    sync_type VARCHAR(20) DEFAULT 'FULL' COMMENT '同步类型',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
    started_time TIMESTAMP COMMENT '开始时间',
    completed_time TIMESTAMP COMMENT '完成时间',
    duration BIGINT COMMENT '执行时长(毫秒)',
    tables_total INTEGER DEFAULT 0 COMMENT '表总数',
    tables_synced INTEGER DEFAULT 0 COMMENT '已同步表数',
    records_total BIGINT DEFAULT 0 COMMENT '记录总数',
    records_synced BIGINT DEFAULT 0 COMMENT '已同步记录数',
    error_message VARCHAR(500) COMMENT '错误消息',
    error_detail TEXT COMMENT '错误详情',
    created_by VARCHAR(50) NOT NULL DEFAULT 'system' COMMENT '创建人',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INTEGER DEFAULT 0 COMMENT '逻辑删除',
    CONSTRAINT fk_sync_task_datasource FOREIGN KEY (datasource_id) REFERENCES datasource_config(id) ON DELETE CASCADE
) COMMENT '数据源同步任务表';

-- 创建索引
CREATE INDEX idx_sync_task_datasource ON datasource_sync_task(datasource_id);
CREATE INDEX idx_sync_task_status ON datasource_sync_task(status);
CREATE INDEX idx_sync_task_created ON datasource_sync_task(created_time);
CREATE INDEX idx_sync_task_deleted ON datasource_sync_task(deleted);
