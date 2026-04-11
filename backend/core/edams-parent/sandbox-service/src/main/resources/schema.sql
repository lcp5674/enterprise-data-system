-- 沙箱环境管理数据库表结构
-- 创建时间: 2026-04-11

-- 沙箱环境表
CREATE TABLE IF NOT EXISTS sandbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '沙箱名称',
    description VARCHAR(500) COMMENT '描述',
    owner_id BIGINT NOT NULL COMMENT '所有者ID',
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED' COMMENT '状态: CREATED,RUNNING,STOPPED,EXPIRED',
    sandbox_type VARCHAR(20) NOT NULL COMMENT '沙箱类型: SQL,API,DATA',
    resource_config TEXT COMMENT '资源配置(JSON格式)',
    expire_time DATETIME COMMENT '过期时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME COMMENT '更新时间',
    INDEX idx_owner_id (owner_id),
    INDEX idx_status (status),
    INDEX idx_sandbox_type (sandbox_type),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='沙箱环境表';

-- 沙箱执行记录表
CREATE TABLE IF NOT EXISTS sandbox_execution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    sandbox_id BIGINT NOT NULL COMMENT '沙箱ID',
    execution_type VARCHAR(30) NOT NULL COMMENT '执行类型: SQL_EXECUTION,API_TEST,DATA_SIMULATION',
    content TEXT NOT NULL COMMENT '执行内容',
    parameters TEXT COMMENT '参数(JSON格式)',
    result TEXT COMMENT '执行结果(JSON格式)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING,RUNNING,SUCCESS,FAILED',
    error_message TEXT COMMENT '错误信息',
    executed_by BIGINT NOT NULL COMMENT '执行用户ID',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    duration BIGINT COMMENT '执行时长(毫秒)',
    INDEX idx_sandbox_id (sandbox_id),
    INDEX idx_execution_type (execution_type),
    INDEX idx_status (status),
    INDEX idx_executed_by (executed_by),
    INDEX idx_created_time (created_time),
    CONSTRAINT fk_execution_sandbox FOREIGN KEY (sandbox_id) REFERENCES sandbox(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='沙箱执行记录表';

-- 沙箱资源表（可选）
CREATE TABLE IF NOT EXISTS sandbox_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    sandbox_id BIGINT NOT NULL COMMENT '沙箱ID',
    resource_type VARCHAR(30) NOT NULL COMMENT '资源类型: DATABASE,API,DATA_FILE',
    resource_config TEXT NOT NULL COMMENT '资源配置(JSON格式)',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE,INACTIVE',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME COMMENT '更新时间',
    INDEX idx_sandbox_id (sandbox_id),
    INDEX idx_resource_type (resource_type),
    CONSTRAINT fk_resource_sandbox FOREIGN KEY (sandbox_id) REFERENCES sandbox(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='沙箱资源表';

-- 沙箱执行结果缓存表（可选）
CREATE TABLE IF NOT EXISTS execution_cache (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    execution_id BIGINT NOT NULL COMMENT '执行记录ID',
    cache_key VARCHAR(200) NOT NULL COMMENT '缓存键',
    cache_value LONGTEXT NOT NULL COMMENT '缓存值',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_cache_key (cache_key),
    INDEX idx_execution_id (execution_id),
    INDEX idx_expire_time (expire_time),
    CONSTRAINT fk_cache_execution FOREIGN KEY (execution_id) REFERENCES sandbox_execution(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='执行结果缓存表';

-- 初始化数据
INSERT INTO sandbox (name, description, owner_id, status, sandbox_type, resource_config, expire_time) VALUES
('测试沙箱1', '用于SQL演练的测试沙箱', 1, 'RUNNING', 'SQL', '{"database": "test_db", "max_connections": 10}', DATE_ADD(NOW(), INTERVAL 7 DAY)),
('API测试环境', 'API接口测试环境', 2, 'CREATED', 'API', '{"base_url": "http://localhost:8080", "auth_type": "bearer"}', DATE_ADD(NOW(), INTERVAL 3 DAY)),
('数据模拟沙箱', '数据生成和模拟环境', 3, 'STOPPED', 'DATA', '{"data_size": 10000, "data_type": "sample"}', DATE_ADD(NOW(), INTERVAL 1 DAY));

INSERT INTO sandbox_execution (sandbox_id, execution_type, content, result, status, executed_by, start_time, end_time, duration) VALUES
(1, 'SQL_EXECUTION', 'SELECT * FROM users LIMIT 10', '{"rows": 10, "execution_time": 120}', 'SUCCESS', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 59 MINUTE), 120),
(1, 'SQL_EXECUTION', 'UPDATE users SET status = 1 WHERE id = 1', '{"affected_rows": 1, "execution_time": 80}', 'SUCCESS', 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 29 MINUTE), 80),
(2, 'API_TEST', 'GET /api/users', '{"status_code": 200, "response_time": 150}', 'SUCCESS', 2, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 150);