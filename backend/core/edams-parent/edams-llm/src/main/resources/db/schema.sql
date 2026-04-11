-- =============================================
-- EDAMS LLM Service Database Schema
-- 大模型服务数据库表结构
-- =============================================

-- 大模型表
CREATE TABLE IF NOT EXISTS llm_model (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '模型ID',
    model_code      VARCHAR(100) NOT NULL COMMENT '模型代码',
    model_name      VARCHAR(200) NOT NULL COMMENT '模型名称',
    provider        VARCHAR(50) NOT NULL COMMENT '提供商: OPENAI, ANTHROPIC, GOOGLE, AZURE, CUSTOM',
    provider_name   VARCHAR(100) COMMENT '提供商名称',
    model_type      VARCHAR(20) DEFAULT 'CHAT' COMMENT '模型类型: CHAT, EMBEDDING, IMAGE, AUDIO',
    description     TEXT COMMENT '模型描述',
    api_version     VARCHAR(50) COMMENT 'API版本',
    api_endpoint    VARCHAR(500) COMMENT 'API端点',
    input_price     DECIMAL(10, 6) DEFAULT 0 COMMENT '输入价格 (元/1000 tokens)',
    output_price    DECIMAL(10, 6) DEFAULT 0 COMMENT '输出价格 (元/1000 tokens)',
    max_context_length INT DEFAULT 4096 COMMENT '最大上下文长度',
    max_output_length INT DEFAULT 2048 COMMENT '最大输出长度',
    capabilities    JSON COMMENT '支持的功能',
    config_params   JSON COMMENT '配置参数',
    request_limit   INT DEFAULT 60 COMMENT '请求限制 (次/分钟)',
    concurrent_limit INT DEFAULT 10 COMMENT '并发限制',
    enabled         TINYINT DEFAULT 1 COMMENT '是否启用',
    priority        INT DEFAULT 50 COMMENT '优先级 (1-100)',
    status          VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, INACTIVE, MAINTENANCE',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    UNIQUE INDEX idx_model_code (model_code),
    INDEX idx_provider (provider),
    INDEX idx_model_type (model_type),
    INDEX idx_enabled (enabled),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大模型表';

-- 配额表
CREATE TABLE IF NOT EXISTS llm_quota (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '配额ID',
    tenant_id       BIGINT COMMENT '租户ID',
    user_id         BIGINT COMMENT '用户ID (NULL表示租户级别配额)',
    quota_type      VARCHAR(20) DEFAULT 'DAILY' COMMENT '配额类型: DAILY, MONTHLY, TOTAL',
    model_id        BIGINT COMMENT '模型ID',
    model_code      VARCHAR(100) COMMENT '模型代码',
    quota_limit     BIGINT DEFAULT 0 COMMENT '配额上限 (tokens)',
    quota_used      BIGINT DEFAULT 0 COMMENT '已使用量 (tokens)',
    cost_limit      DECIMAL(10, 2) DEFAULT 0 COMMENT '配额上限 (金额，元)',
    cost_used       DECIMAL(10, 2) DEFAULT 0 COMMENT '已消费金额',
    request_limit   INT DEFAULT 0 COMMENT '请求次数上限',
    request_used    INT DEFAULT 0 COMMENT '已请求次数',
    period_start    DATETIME COMMENT '周期开始时间',
    period_end      DATETIME COMMENT '周期结束时间',
    enabled         TINYINT DEFAULT 1 COMMENT '是否启用',
    alert_threshold INT DEFAULT 80 COMMENT '告警阈值 (百分比)',
    status          VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, EXPIRED, EXHAUSTED',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_user_id (user_id),
    INDEX idx_model_id (model_id),
    INDEX idx_status (status),
    INDEX idx_period_end (period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配额表';

-- 使用日志表
CREATE TABLE IF NOT EXISTS llm_usage_log (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    request_id      VARCHAR(64) NOT NULL COMMENT '请求ID',
    trace_id        VARCHAR(64) COMMENT '追踪ID',
    tenant_id       BIGINT COMMENT '租户ID',
    user_id         BIGINT COMMENT '用户ID',
    user_name       VARCHAR(100) COMMENT '用户名称',
    model_id        BIGINT COMMENT '模型ID',
    model_code      VARCHAR(100) COMMENT '模型代码',
    provider        VARCHAR(50) COMMENT '提供商',
    request_type    VARCHAR(20) DEFAULT 'CHAT' COMMENT '请求类型: CHAT, COMPLETION, EMBEDDING',
    input_tokens    INT DEFAULT 0 COMMENT '输入tokens',
    output_tokens   INT DEFAULT 0 COMMENT '输出tokens',
    total_tokens    INT DEFAULT 0 COMMENT '总tokens',
    input_cost      DECIMAL(10, 4) DEFAULT 0 COMMENT '输入费用',
    output_cost     DECIMAL(10, 4) DEFAULT 0 COMMENT '输出费用',
    total_cost      DECIMAL(10, 4) DEFAULT 0 COMMENT '总费用',
    latency_ms      BIGINT DEFAULT 0 COMMENT '延迟(ms)',
    status          VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '状态: SUCCESS, FAILED, TIMEOUT',
    error_code      VARCHAR(50) COMMENT '错误码',
    error_message   TEXT COMMENT '错误信息',
    module          VARCHAR(100) COMMENT '应用模块',
    app_name        VARCHAR(100) COMMENT '应用名称',
    ip_address      VARCHAR(50) COMMENT 'IP地址',
    request_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    INDEX idx_request_id (request_id),
    INDEX idx_trace_id (trace_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_user_id (user_id),
    INDEX idx_model_code (model_code),
    INDEX idx_request_time (request_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用日志表';

-- 成本记录表
CREATE TABLE IF NOT EXISTS llm_cost_record (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    tenant_id       BIGINT COMMENT '租户ID',
    department_id   BIGINT COMMENT '部门ID',
    user_id         BIGINT COMMENT '用户ID',
    model_id        BIGINT COMMENT '模型ID',
    model_code      VARCHAR(100) COMMENT '模型代码',
    provider        VARCHAR(50) COMMENT '提供商',
    cost_type       VARCHAR(20) DEFAULT 'DAILY' COMMENT '成本类型: DAILY, MONTHLY',
    stat_date       DATETIME NOT NULL COMMENT '统计日期',
    input_tokens    BIGINT DEFAULT 0 COMMENT '输入tokens',
    output_tokens   BIGINT DEFAULT 0 COMMENT '输出tokens',
    total_tokens    BIGINT DEFAULT 0 COMMENT '总tokens',
    input_cost      DECIMAL(10, 2) DEFAULT 0 COMMENT '输入费用',
    output_cost     DECIMAL(10, 2) DEFAULT 0 COMMENT '输出费用',
    total_cost      DECIMAL(10, 2) DEFAULT 0 COMMENT '总费用',
    request_count   BIGINT DEFAULT 0 COMMENT '请求次数',
    success_count   BIGINT DEFAULT 0 COMMENT '成功次数',
    failed_count    BIGINT DEFAULT 0 COMMENT '失败次数',
    avg_latency     DOUBLE DEFAULT 0 COMMENT '平均延迟(ms)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_user_id (user_id),
    INDEX idx_model_id (model_id),
    INDEX idx_stat_date (stat_date),
    INDEX idx_cost_type (cost_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成本记录表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入示例模型
INSERT INTO llm_model (model_code, model_name, provider, provider_name, model_type, description, input_price, output_price, max_context_length, max_output_length, capabilities, request_limit, concurrent_limit, priority, status) VALUES
('gpt-4', 'GPT-4', 'OPENAI', 'OpenAI', 'CHAT', 'OpenAI GPT-4 模型', 0.07, 0.21, 8192, 4096, '["chat", "function_call", "vision"]', 200, 20, 90, 'ACTIVE'),
('gpt-3.5-turbo', 'GPT-3.5 Turbo', 'OPENAI', 'OpenAI', 'CHAT', 'OpenAI GPT-3.5 Turbo 模型', 0.001, 0.002, 16385, 4096, '["chat", "function_call"]', 500, 50, 80, 'ACTIVE'),
('chatglm-pro', 'ChatGLM Pro', 'CUSTOM', '智谱AI', 'CHAT', '智谱AI ChatGLM Pro', 0.01, 0.01, 32768, 2048, '["chat", "function_call"]', 300, 30, 70, 'ACTIVE'),
('chatglm-standard', 'ChatGLM Standard', 'CUSTOM', '智谱AI', 'CHAT', '智谱AI ChatGLM Standard', 0.002, 0.002, 32768, 2048, '["chat"]', 400, 40, 60, 'ACTIVE'),
('embedding-3', 'Embedding V3', 'OPENAI', 'OpenAI', 'EMBEDDING', 'OpenAI Embedding V3', 0.00002, 0, 8191, 0, '["embedding"]', 1000, 100, 50, 'ACTIVE');

-- 插入示例配额
INSERT INTO llm_quota (tenant_id, user_id, quota_type, model_id, model_code, quota_limit, cost_limit, request_limit, period_start, period_end, alert_threshold) VALUES
(1, NULL, 'MONTHLY', 1, 'gpt-4', 1000000, 100.00, 10000, DATE_FORMAT(NOW(), '%Y-%m-01'), DATE_FORMAT(LAST_DAY(NOW()), '%Y-%m-%d'), 80),
(1, NULL, 'MONTHLY', 2, 'gpt-3.5-turbo', 5000000, 50.00, 50000, DATE_FORMAT(NOW(), '%Y-%m-01'), DATE_FORMAT(LAST_DAY(NOW()), '%Y-%m-%d'), 80),
(1, 1, 'MONTHLY', 1, 'gpt-4', 100000, 10.00, 1000, DATE_FORMAT(NOW(), '%Y-%m-01'), DATE_FORMAT(LAST_DAY(NOW()), '%Y-%m-%d'), 80),
(1, 1, 'MONTHLY', 2, 'gpt-3.5-turbo', 500000, 5.00, 5000, DATE_FORMAT(NOW(), '%Y-%m-01'), DATE_FORMAT(LAST_DAY(NOW()), '%Y-%m-%d'), 80),
(1, 2, 'MONTHLY', 3, 'chatglm-pro', 1000000, 50.00, 10000, DATE_FORMAT(NOW(), '%Y-%m-01'), DATE_FORMAT(LAST_DAY(NOW()), '%Y-%m-%d'), 80);
