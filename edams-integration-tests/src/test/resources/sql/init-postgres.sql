-- 初始化PostgreSQL测试数据库
-- 创建基础表结构

-- 数据源表
CREATE TABLE IF NOT EXISTS datasource (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    connection_url TEXT,
    username VARCHAR(255),
    password VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 数据资产表
CREATE TABLE IF NOT EXISTS data_asset (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    datasource_id BIGINT REFERENCES datasource(id),
    schema_name VARCHAR(255),
    table_name VARCHAR(255),
    description TEXT,
    owner VARCHAR(255),
    status VARCHAR(50) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 数据目录表
CREATE TABLE IF NOT EXISTS data_catalog (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT REFERENCES data_catalog(id),
    description TEXT,
    owner VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 数据资产与目录关联表
CREATE TABLE IF NOT EXISTS catalog_asset (
    catalog_id BIGINT REFERENCES data_catalog(id),
    asset_id BIGINT REFERENCES data_asset(id),
    PRIMARY KEY (catalog_id, asset_id)
);

-- 质量规则表
CREATE TABLE IF NOT EXISTS quality_rule (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    asset_id BIGINT REFERENCES data_asset(id),
    expression TEXT NOT NULL,
    threshold DECIMAL(5,2),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 质量检测结果表
CREATE TABLE IF NOT EXISTS quality_check_result (
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT REFERENCES quality_rule(id),
    asset_id BIGINT REFERENCES data_asset(id),
    check_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_count INTEGER DEFAULT 0,
    error_count INTEGER DEFAULT 0,
    error_rate DECIMAL(5,2),
    status VARCHAR(50),
    details TEXT
);

-- 用户表
CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT REFERENCES app_user(id),
    role_id BIGINT REFERENCES role(id),
    PRIMARY KEY (user_id, role_id)
);

-- 权限表
CREATE TABLE IF NOT EXISTS permission (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    resource_type VARCHAR(50) NOT NULL,
    resource_id BIGINT,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS role_permission (
    role_id BIGINT REFERENCES role(id),
    permission_id BIGINT REFERENCES permission(id),
    PRIMARY KEY (role_id, permission_id)
);

-- 知识图谱实体表
CREATE TABLE IF NOT EXISTS knowledge_entity (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    properties JSONB,
    source VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 知识图谱关系表
CREATE TABLE IF NOT EXISTS knowledge_relation (
    id BIGSERIAL PRIMARY KEY,
    source_id BIGINT REFERENCES knowledge_entity(id),
    target_id BIGINT REFERENCES knowledge_entity(id),
    relation_type VARCHAR(50) NOT NULL,
    properties JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 通知记录表
CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    recipient VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_data_asset_datasource ON data_asset(datasource_id);
CREATE INDEX IF NOT EXISTS idx_data_asset_status ON data_asset(status);
CREATE INDEX IF NOT EXISTS idx_quality_rule_asset ON quality_rule(asset_id);
CREATE INDEX IF NOT EXISTS idx_quality_result_rule ON quality_check_result(rule_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_entity_type ON knowledge_entity(entity_type);
CREATE INDEX IF NOT EXISTS idx_knowledge_relation_source ON knowledge_relation(source_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_relation_target ON knowledge_relation(target_id);

-- 插入测试数据
INSERT INTO datasource (name, type, connection_url, username, status) VALUES
('Test MySQL DB', 'MYSQL', 'jdbc:mysql://localhost:3306/test', 'test_user', 'ACTIVE'),
('Test PostgreSQL DB', 'POSTGRESQL', 'jdbc:postgresql://localhost:5432/test', 'test_user', 'ACTIVE'),
('Test Oracle DB', 'ORACLE', 'jdbc:oracle:thin:@localhost:1521:XE', 'test_user', 'ACTIVE');

INSERT INTO data_catalog (name, description, owner, status) VALUES
('财务数据', '财务相关数据资产目录', 'admin', 'ACTIVE'),
('客户数据', '客户相关数据资产目录', 'admin', 'ACTIVE'),
('产品数据', '产品相关数据资产目录', 'admin', 'ACTIVE');

INSERT INTO app_user (username, email, password, full_name, status) VALUES
('admin', 'admin@enterprise.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '系统管理员', 'ACTIVE'),
('testuser', 'test@enterprise.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '测试用户', 'ACTIVE');

INSERT INTO role (name, description) VALUES
('ADMIN', '系统管理员角色'),
('DATA_STEWARD', '数据管理员角色'),
('DATA_VIEWER', '数据查看者角色');

INSERT INTO user_role (user_id, role_id) VALUES
(1, 1),
(2, 3);
