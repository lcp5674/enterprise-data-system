-- =====================================================
-- V1.0.0__init_schema.sql
-- 描述：初始化数据资产表结构
-- 作者：架构师团队
-- 日期：2026-04-10
-- =====================================================

-- 设置编码
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table: data_asset
-- 数据资产主表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `data_asset` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `asset_name` VARCHAR(200) NOT NULL COMMENT '资产名称',
    `asset_type` VARCHAR(32) NOT NULL COMMENT '资产类型:DATABASE/TABLE/VIEW/COLUMN/FILE/API',
    `description` TEXT COMMENT '资产描述',
    `owner_id` BIGINT COMMENT '负责人ID',
    `sensitivity` VARCHAR(32) NOT NULL DEFAULT 'INTERNAL' COMMENT '敏感级别:PUBLIC/INTERNAL/CONFIDENTIAL/SECRET',
    `status` VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态:DRAFT/PUBLISHED/DEPRECATED/ARCHIVED',
    `datasource_id` BIGINT COMMENT '数据源ID',
    `database_name` VARCHAR(128) COMMENT '数据库名称',
    `schema_name` VARCHAR(128) COMMENT 'Schema名称',
    `table_name` VARCHAR(128) COMMENT '表名称',
    `column_count` INT DEFAULT 0 COMMENT '字段数量',
    `row_count` BIGINT DEFAULT 0 COMMENT '行数',
    `data_size` BIGINT DEFAULT 0 COMMENT '数据大小(字节)',
    `quality_score` DECIMAL(5,2) DEFAULT 0 COMMENT '质量评分',
    `tags` VARCHAR(500) COMMENT '标签,json数组',
    `properties` JSON COMMENT '扩展属性,json对象',
    `created_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记:0-未删除,1-已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_asset_name` (`asset_name`, `deleted`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_asset_type` (`asset_type`),
    KEY `idx_sensitivity` (`sensitivity`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`),
    KEY `idx_datasource_id` (`datasource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据资产表';

-- ----------------------------
-- Table: asset_metadata
-- 资产元数据表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `asset_metadata` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `asset_id` BIGINT NOT NULL COMMENT '资产ID',
    `metadata_key` VARCHAR(64) NOT NULL COMMENT '元数据键',
    `metadata_value` TEXT COMMENT '元数据值',
    `metadata_type` VARCHAR(32) DEFAULT 'STRING' COMMENT '数据类型:STRING/NUMBER/BOOLEAN/DATE/JSON',
    `created_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_asset_id` (`asset_id`),
    KEY `idx_metadata_key` (`metadata_key`),
    UNIQUE KEY `uk_asset_metadata` (`asset_id`, `metadata_key`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产元数据表';

-- ----------------------------
-- Table: asset_tag
-- 资产标签关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `asset_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `asset_id` BIGINT NOT NULL COMMENT '资产ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `created_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_asset_tag` (`asset_id`, `tag_id`, `deleted`),
    KEY `idx_asset_id` (`asset_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产标签关联表';

-- ----------------------------
-- Table: tag
-- 标签表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tag_name` VARCHAR(100) NOT NULL COMMENT '标签名称',
    `tag_category` VARCHAR(32) DEFAULT 'DEFAULT' COMMENT '标签分类:业务/合规/使用',
    `color` VARCHAR(16) DEFAULT '#1890FF' COMMENT '标签颜色',
    `description` VARCHAR(500) COMMENT '标签描述',
    `created_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name` (`tag_name`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ----------------------------
-- Table: datasource
-- 数据源表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `datasource` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `datasource_name` VARCHAR(200) NOT NULL COMMENT '数据源名称',
    `datasource_type` VARCHAR(32) NOT NULL COMMENT '数据源类型:MYSQL/POSTGRESQL/ORACLE/KAFKA/ES/Neo4j等',
    `connection_info` JSON COMMENT '连接信息(JSON加密存储)',
    `description` VARCHAR(500) COMMENT '描述',
    `owner_id` BIGINT COMMENT '负责人ID',
    `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态:ACTIVE/INACTIVE/ERROR',
    `last_sync_time` DATETIME COMMENT '最后同步时间',
    `created_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_datasource_name` (`datasource_name`, `deleted`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源表';

-- ----------------------------
-- 初始化数据
-- ----------------------------
INSERT INTO `tag` (`id`, `tag_name`, `tag_category`, `color`, `description`, `created_by`)
VALUES 
    (1, '核心数据', '业务', '#FF6B6B', '核心业务数据', 'system'),
    (2, 'PII数据', '合规', '#FFE66D', '个人身份信息', 'system'),
    (3, '高频访问', '使用', '#4ECDC4', '高频访问的数据', 'system'),
    (4, '测试数据', 'DEFAULT', '#95DE64', '测试数据', 'system');

SET FOREIGN_KEY_CHECKS = 1;
