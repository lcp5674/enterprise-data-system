-- EDAMS用户服务 - 部门表和角色表
USE edams;

-- ==================== 部门表 ====================
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '部门名称',
    `code` VARCHAR(50) NOT NULL COMMENT '部门编码（唯一）',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父部门ID，0为顶级',
    `level` INT DEFAULT 0 COMMENT '部门层级',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `leader_id` BIGINT DEFAULT NULL COMMENT '负责人用户ID',
    `leader_name` VARCHAR(50) DEFAULT NULL COMMENT '负责人姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '部门描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `tree_path` VARCHAR(200) DEFAULT '/' COMMENT '树形路径编码',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- ==================== 角色表 ====================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(50) NOT NULL COMMENT '角色编码（唯一）',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `data_scope` TINYINT DEFAULT 3 COMMENT '数据权限：1-全部，2-本部门及下级，3-本部门，4-本人',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ==================== 用户-角色关联表 ====================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ==================== 初始数据：部门和角色 ====================
INSERT INTO `sys_department` (id, name, code, parent_id, level, sort_order, description)
VALUES 
(1, '总公司', 'ROOT', 0, 0, 1, '企业总部'),
(2, '技术部', 'TECH', 1, 1, 1, '技术研发部门'),
(3, '产品部', 'PRODUCT', 1, 1, 2, '产品管理部门'),
(4, '数据治理部', 'GOVERNANCE', 1, 1, 3, '数据治理部门'),
(5, '运维部', 'OPS', 1, 1, 4, '运维保障部门'),
(6, '后端开发组', 'BACKEND', 2, 2, 1, '后端开发小组'),
(7, '前端开发组', 'FRONTEND', 2, 2, 2, '前端开发小组'),
(8, '数据架构组', 'DATA_ARCH', 4, 2, 1, '数据架构小组'),
(9, '数据质量组', 'DATA_QUALITY', 4, 2, 2, '数据质量管理小组');

INSERT INTO `sys_role` (id, name, code, description, data_scope)
VALUES 
(1, '超级管理员', 'ROLE_ADMIN', '拥有系统所有权限', 1),
(2, '普通用户', 'ROLE_USER', '标准系统用户', 4),
(3, '数据管理员', 'ROLE_DATA_ADMIN', '数据资产管理员', 3),
(4, '数据分析师', 'ROLE_ANALYST', '数据分析人员', 3),
(5, '审计员', 'ROLE_AUDITOR', '安全审计人员', 2);
