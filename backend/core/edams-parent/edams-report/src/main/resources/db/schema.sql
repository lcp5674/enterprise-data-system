-- EDAMS 报表服务数据库表结构
-- 创建时间: 2026-04-11

-- 报表表
CREATE TABLE IF NOT EXISTS `edams_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `report_name` VARCHAR(200) NOT NULL COMMENT '报表名称',
    `report_code` VARCHAR(100) NOT NULL COMMENT '报表编码',
    `report_type` VARCHAR(50) NOT NULL COMMENT '报表类型: DATA_QUALITY/ASSET_STAT/GOVERNANCE/CUSTOM',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '报表描述',
    `datasource_id` BIGINT DEFAULT NULL COMMENT '数据源ID',
    `template_id` BIGINT DEFAULT NULL COMMENT '模板ID',
    `query_sql` TEXT DEFAULT NULL COMMENT 'SQL查询语句',
    `parameters` TEXT DEFAULT NULL COMMENT '参数配置(JSON)',
    `file_format` VARCHAR(50) DEFAULT 'PDF' COMMENT '文件格式: PDF/EXCEL/WORD/HTML',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人名称',
    `execute_count` INT DEFAULT 0 COMMENT '执行次数',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `last_execute_time` DATETIME DEFAULT NULL COMMENT '最后执行时间',
    `last_execute_status` VARCHAR(20) DEFAULT NULL COMMENT '最后执行状态: SUCCESS/FAILED',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标志',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_report_code` (`report_code`),
    KEY `idx_report_name` (`report_name`),
    KEY `idx_report_type` (`report_type`),
    KEY `idx_status` (`status`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表表';

-- 报表模板表
CREATE TABLE IF NOT EXISTS `edams_report_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_name` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `template_code` VARCHAR(100) NOT NULL COMMENT '模板编码',
    `template_type` VARCHAR(50) NOT NULL COMMENT '模板类型: JASPER/EXCEL/WORD/CUSTOM',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    `file_name` VARCHAR(200) DEFAULT NULL COMMENT '文件名称',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `content_type` VARCHAR(100) DEFAULT NULL COMMENT '内容类型',
    `layout_config` TEXT DEFAULT NULL COMMENT '布局配置(JSON)',
    `data_binding` TEXT DEFAULT NULL COMMENT '数据绑定配置(JSON)',
    `supported_formats` VARCHAR(200) DEFAULT 'PDF,EXCEL' COMMENT '支持的导出格式',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人名称',
    `usage_count` INT DEFAULT 0 COMMENT '使用次数',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标志',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_code` (`template_code`),
    UNIQUE KEY `uk_template_name` (`template_name`),
    KEY `idx_template_type` (`template_type`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表模板表';

-- 初始化报表模板
INSERT INTO `edams_report_template` (`template_name`, `template_code`, `template_type`, `description`, `supported_formats`) VALUES
('数据质量报告模板', 'TMPL_QUALITY', 'JASPER', '数据质量统计报告模板', 'PDF,EXCEL,WORD'),
('资产统计报告模板', 'TMPL_ASSET', 'JASPER', '数据资产统计报告模板', 'PDF,EXCEL'),
('治理活动报告模板', 'TMPL_GOVERNANCE', 'JASPER', '数据治理活动报告模板', 'PDF,EXCEL,WORD'),
('血缘分析报告模板', 'TMPL_LINEAGE', 'CUSTOM', '数据血缘分析报告模板', 'PDF,HTML'),
('元数据报告模板', 'TMPL_METADATA', 'EXCEL', '元数据清单报告模板', 'EXCEL,PDF')
ON DUPLICATE KEY UPDATE `template_name` = VALUES(`template_name`);
