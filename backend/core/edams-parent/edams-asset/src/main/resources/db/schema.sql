-- EDAMS 资产管理服务数据库表结构
-- 创建时间: 2026-04-11

-- 资产表
CREATE TABLE IF NOT EXISTS `edams_asset` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `asset_name` VARCHAR(200) NOT NULL COMMENT '资产名称',
    `asset_code` VARCHAR(100) NOT NULL COMMENT '资产编码',
    `asset_type` VARCHAR(50) NOT NULL COMMENT '资产类型: DATABASE/TABLE/VIEW/FILE/API',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '资产描述',
    `owner_id` BIGINT DEFAULT NULL COMMENT '负责人ID',
    `owner_name` VARCHAR(100) DEFAULT NULL COMMENT '负责人名称',
    `sensitivity` VARCHAR(20) DEFAULT 'INTERNAL' COMMENT '敏感级别: PUBLIC/INTERNAL/CONFIDENTIAL/SECRET',
    `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '资产状态: DRAFT/REVIEWING/PUBLISHED/ARCHIVED/DEPRECATED',
    `datasource_id` BIGINT DEFAULT NULL COMMENT '数据源ID',
    `datasource_name` VARCHAR(100) DEFAULT NULL COMMENT '数据源名称',
    `database_name` VARCHAR(100) DEFAULT NULL COMMENT '数据库名称',
    `schema_name` VARCHAR(100) DEFAULT NULL COMMENT 'Schema名称',
    `table_name` VARCHAR(200) DEFAULT NULL COMMENT '表/视图名称',
    `column_count` INT DEFAULT 0 COMMENT '字段数量',
    `row_count` BIGINT DEFAULT 0 COMMENT '数据行数',
    `data_size` BIGINT DEFAULT 0 COMMENT '数据大小(字节)',
    `quality_score` DECIMAL(5,2) DEFAULT 0.00 COMMENT '质量评分(0-100)',
    `catalog_id` BIGINT DEFAULT NULL COMMENT '所属目录ID',
    `domain_id` BIGINT DEFAULT NULL COMMENT '所属业务域ID',
    `domain_name` VARCHAR(100) DEFAULT NULL COMMENT '所属业务域名称',
    `tags` JSON DEFAULT NULL COMMENT '标签(JSON数组)',
    `properties` JSON DEFAULT NULL COMMENT '扩展属性(JSON对象)',
    `last_sync_time` DATETIME DEFAULT NULL COMMENT '最后同步时间',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `version` INT DEFAULT 1 COMMENT '版本号',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标志: 0-未删除, 1-已删除',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_asset_code` (`asset_code`),
    KEY `idx_asset_name` (`asset_name`),
    KEY `idx_asset_type` (`asset_type`),
    KEY `idx_status` (`status`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_catalog_id` (`catalog_id`),
    KEY `idx_domain_id` (`domain_id`),
    KEY `idx_datasource_id` (`datasource_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据资产表';

-- 资产目录表
CREATE TABLE IF NOT EXISTS `edams_asset_catalog` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '目录名称',
    `code` VARCHAR(100) NOT NULL COMMENT '目录编码',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父目录ID(0表示根目录)',
    `level` INT DEFAULT 0 COMMENT '目录层级',
    `path` VARCHAR(500) DEFAULT NULL COMMENT '目录路径',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '目录描述',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标志: 0-未删除, 1-已删除',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_catalog_code` (`code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`level`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产目录表';

-- 资产标签表
CREATE TABLE IF NOT EXISTS `edams_asset_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tag_name` VARCHAR(100) NOT NULL COMMENT '标签名称',
    `tag_code` VARCHAR(100) NOT NULL COMMENT '标签编码',
    `color` VARCHAR(20) DEFAULT '#4ECDC4' COMMENT '标签颜色',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '标签描述',
    `category` VARCHAR(50) DEFAULT '自定义' COMMENT '所属分类',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `usage_count` INT DEFAULT 0 COMMENT '使用次数统计',
    `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `last_used_time` DATETIME DEFAULT NULL COMMENT '最后使用时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标志: 0-未删除, 1-已删除',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_code` (`tag_code`),
    UNIQUE KEY `uk_tag_name` (`tag_name`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产标签表';

-- 资产标签关联表
CREATE TABLE IF NOT EXISTS `edams_asset_tag_relation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `asset_id` BIGINT NOT NULL COMMENT '资产ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `tagger_id` BIGINT DEFAULT NULL COMMENT '打标签人ID',
    `tag_time` DATETIME DEFAULT NULL COMMENT '打标签时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标志: 0-未删除, 1-已删除',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_asset_tag` (`asset_id`, `tag_id`),
    KEY `idx_asset_id` (`asset_id`),
    KEY `idx_tag_id` (`tag_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产标签关联表';

-- 初始化根目录
INSERT INTO `edams_asset_catalog` (`name`, `code`, `parent_id`, `level`, `path`, `sort_order`, `description`, `status`) VALUES
('全部资产', 'ROOT', 0, 0, '/ROOT', 0, '根目录', 1),
('数据库', 'DATABASE', 0, 1, '/ROOT/DATABASE', 1, '数据库资产', 1),
('数据表', 'TABLE', 0, 1, '/ROOT/TABLE', 2, '数据表资产', 1),
('数据视图', 'VIEW', 0, 1, '/ROOT/VIEW', 3, '视图资产', 1),
('文件', 'FILE', 0, 1, '/ROOT/FILE', 4, '文件资产', 1),
('API', 'API', 0, 1, '/ROOT/API', 5, 'API资产', 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 初始化标签
INSERT INTO `edams_asset_tag` (`tag_name`, `tag_code`, `color`, `description`, `category`, `status`) VALUES
('核心数据', 'CORE_DATA', '#FF6B6B', '核心业务数据', '优先级', 1),
('高优先级', 'HIGH_PRIORITY', '#FFEAA7', '高优先级数据', '优先级', 1),
('中优先级', 'MEDIUM_PRIORITY', '#74B9FF', '中优先级数据', '优先级', 1),
('低优先级', 'LOW_PRIORITY', '#A29BFE', '低优先级数据', '优先级', 1),
('财务域', 'FINANCE_DOMAIN', '#00B894', '财务业务域', '业务域', 1),
('销售域', 'SALES_DOMAIN', '#E17055', '销售业务域', '业务域', 1),
('生产域', 'PRODUCTION_DOMAIN', '#6C5CE7', '生产业务域', '业务域', 1),
('客户域', 'CUSTOMER_DOMAIN', '#FDCB6E', '客户业务域', '业务域', 1),
('敏感数据', 'SENSITIVE_DATA', '#D63031', '敏感级别数据', '状态', 1),
('已审核', 'VERIFIED', '#00CEC9', '已通过审核', '状态', 1),
('待审核', 'PENDING', '#FD79A8', '待审核状态', '状态', 1),
('废弃', 'DEPRECATED', '#636E72', '已废弃数据', '状态', 1)
ON DUPLICATE KEY UPDATE `tag_name` = VALUES(`tag_name`);
