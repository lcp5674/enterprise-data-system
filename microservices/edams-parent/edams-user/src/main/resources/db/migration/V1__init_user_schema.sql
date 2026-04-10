-- =====================================================
-- V1__init_user_schema.sql
-- 描述：初始化用户管理服务表结构
-- 作者：Backend Team
-- 日期：2026-04-10
-- =====================================================

-- 设置编码
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table: sys_department
-- 部门表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_department` (
    `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '部门名称',
    `code` VARCHAR(50) NOT NULL COMMENT '部门编码',
    `parent_id` VARCHAR(32) COMMENT '上级部门ID',
    `level` SMALLINT DEFAULT 1 COMMENT '层级',
    `path` VARCHAR(500) COMMENT '部门路径',
    `tree_path` VARCHAR(1000) COMMENT '树形路径',
    `sort_order` SMALLINT DEFAULT 0 COMMENT '排序',
    `leader_id` VARCHAR(32) COMMENT '负责人ID',
    `leader_name` VARCHAR(100) COMMENT '负责人姓名',
    `contact_person` VARCHAR(100) COMMENT '联系人',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `contact_email` VARCHAR(100) COMMENT '联系邮箱',
    `status` SMALLINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `description` TEXT COMMENT '描述',
    `is_deleted` SMALLINT DEFAULT 0 COMMENT '是否删除',
    `deleted_by` VARCHAR(32) COMMENT '删除人',
    `deleted_time` TIMESTAMP COMMENT '删除时间',
    `created_by` VARCHAR(32) COMMENT '创建人',
    `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(32) COMMENT '更新人',
    `updated_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    CONSTRAINT `uk_sys_department_code` UNIQUE (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

CREATE INDEX `idx_sys_department_parent` ON `sys_department`(`parent_id`);
CREATE INDEX `idx_sys_department_path` ON `sys_department`(`path`);
CREATE INDEX `idx_sys_department_tree_path` ON `sys_department`(`tree_path`);
CREATE INDEX `idx_sys_department_status` ON `sys_department`(`status`);

-- ----------------------------
-- Table: sys_user_preference
-- 用户偏好设置表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_user_preference` (
    `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `preference_key` VARCHAR(100) NOT NULL COMMENT '偏好键',
    `preference_value` TEXT COMMENT '偏好值',
    `preference_type` VARCHAR(20) DEFAULT 'USER' COMMENT '偏好类型：USER-用户级 SYSTEM-系统级',
    `created_by` VARCHAR(32) COMMENT '创建人',
    `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(32) COMMENT '更新人',
    `updated_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    CONSTRAINT `uk_sys_user_preference` UNIQUE (`user_id`, `preference_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好设置表';

CREATE INDEX `idx_sys_user_preference_user` ON `sys_user_preference`(`user_id`);

-- ----------------------------
-- 初始化顶级部门
-- ----------------------------
INSERT INTO `sys_department` (
    `id`, `name`, `code`, `parent_id`, `level`, `path`, `tree_path`, 
    `sort_order`, `status`, `created_by`
) VALUES (
    'dept-root',
    '企业总部',
    'ROOT',
    NULL,
    1,
    '/企业总部',
    '/dept-root',
    0,
    1,
    'system'
);

INSERT INTO `sys_department` (
    `id`, `name`, `code`, `parent_id`, `level`, `path`, `tree_path`, 
    `sort_order`, `status`, `created_by`
) VALUES (
    'dept-tech',
    '技术中心',
    'TECH',
    'dept-root',
    2,
    '/企业总部/技术中心',
    '/dept-root/dept-tech',
    1,
    1,
    'system'
);

INSERT INTO `sys_department` (
    `id`, `name`, `code`, `parent_id`, `level`, `path`, `tree_path`, 
    `sort_order`, `status`, `created_by`
) VALUES (
    'dept-ops',
    '运维部',
    'OPS',
    'dept-tech',
    3,
    '/企业总部/技术中心/运维部',
    '/dept-root/dept-tech/dept-ops',
    1,
    1,
    'system'
);

SET FOREIGN_KEY_CHECKS = 1;
