-- =====================================================
-- V1__init_auth_schema.sql
-- 描述：初始化认证服务表结构
-- 作者：Backend Team
-- 日期：2026-04-10
-- =====================================================

-- 设置编码
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table: sys_user
-- 系统用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `password_salt` VARCHAR(32) COMMENT '密码盐值',
    `nickname` VARCHAR(100) COMMENT '昵称',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `employee_no` VARCHAR(50) COMMENT '工号',
    `avatar_url` VARCHAR(500) COMMENT '头像URL',
    `department_id` VARCHAR(32) COMMENT '部门ID',
    `position` VARCHAR(100) COMMENT '职位',
    `manager_id` VARCHAR(32) COMMENT '上级ID',
    `status` SMALLINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用，2-锁定',
    `user_type` SMALLINT DEFAULT 1 COMMENT '用户类型：1-内部用户，2-外部用户，3-系统用户',
    `source_type` VARCHAR(20) COMMENT '来源类型：LOCAL/LDAP/OAUTH2',
    `source_id` VARCHAR(100) COMMENT '第三方用户ID',
    `last_login_time` TIMESTAMP COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
    `login_fail_count` SMALLINT DEFAULT 0 COMMENT '连续登录失败次数',
    `password_expire_time` TIMESTAMP COMMENT '密码过期时间',
    `mfa_enabled` SMALLINT DEFAULT 0 COMMENT '是否启用MFA',
    `mfa_secret` VARCHAR(255) COMMENT 'MFA密钥',
    `mfa_backup_codes` TEXT COMMENT 'MFA备用码',
    `is_first_login` SMALLINT DEFAULT 1 COMMENT '是否首次登录',
    `is_deleted` SMALLINT DEFAULT 0 COMMENT '是否删除',
    `deleted_by` VARCHAR(32) COMMENT '删除人',
    `deleted_time` TIMESTAMP COMMENT '删除时间',
    `created_by` VARCHAR(32) COMMENT '创建人',
    `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(32) COMMENT '更新人',
    `updated_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    CONSTRAINT `uk_sys_user_username` UNIQUE (`username`),
    CONSTRAINT `uk_sys_user_email` UNIQUE (`email`),
    CONSTRAINT `uk_sys_user_employee_no` UNIQUE (`employee_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

CREATE INDEX `idx_sys_user_department` ON `sys_user`(`department_id`);
CREATE INDEX `idx_sys_user_status` ON `sys_user`(`status`);
CREATE INDEX `idx_sys_user_created_time` ON `sys_user`(`created_time`);
CREATE INDEX `idx_sys_user_email` ON `sys_user`(`email`);

-- ----------------------------
-- Table: sys_session
-- 系统会话表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_session` (
    `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    `session_id` VARCHAR(100) NOT NULL COMMENT '会话ID',
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `device_id` VARCHAR(100) COMMENT '设备ID',
    `device_type` VARCHAR(20) COMMENT '设备类型：WEB/APP/API',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(500) COMMENT 'User-Agent',
    `login_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `last_active_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    `expire_time` TIMESTAMP COMMENT '过期时间',
    `is_active` SMALLINT DEFAULT 1 COMMENT '是否有效',
    `created_by` VARCHAR(32) COMMENT '创建人',
    `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    CONSTRAINT `uk_sys_session_session_id` UNIQUE (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统会话表';

CREATE INDEX `idx_sys_session_user` ON `sys_session`(`user_id`);
CREATE INDEX `idx_sys_session_expire` ON `sys_session`(`expire_time`);
CREATE INDEX `idx_sys_session_active` ON `sys_session`(`is_active`);

-- ----------------------------
-- Table: login_log
-- 登录日志表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `login_log` (
    `id` VARCHAR(32) NOT NULL PRIMARY KEY COMMENT '主键ID',
    `user_id` VARCHAR(32) COMMENT '用户ID',
    `username` VARCHAR(50) COMMENT '用户名',
    `login_type` VARCHAR(20) NOT NULL COMMENT '登录类型：PASSWORD/MOBILE_CODE/SSO/OAUTH2',
    `login_source` VARCHAR(20) COMMENT '登录来源：WEB/APP/API',
    `status` VARCHAR(20) NOT NULL COMMENT '状态：SUCCESS/FAIL/LOCKED',
    `fail_reason` VARCHAR(100) COMMENT '失败原因',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(500) COMMENT 'User-Agent',
    `device_id` VARCHAR(100) COMMENT '设备ID',
    `device_type` VARCHAR(20) COMMENT '设备类型',
    `location` VARCHAR(100) COMMENT '登录地点',
    `session_id` VARCHAR(100) COMMENT '会话ID',
    `token_id` VARCHAR(100) COMMENT 'Token ID',
    `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

CREATE INDEX `idx_login_log_user` ON `login_log`(`user_id`);
CREATE INDEX `idx_login_log_time` ON `login_log`(`created_time`);
CREATE INDEX `idx_login_log_status` ON `login_log`(`status`);
CREATE INDEX `idx_login_log_ip` ON `login_log`(`ip_address`);

-- ----------------------------
-- 初始化管理员用户
-- 密码：admin123 (BCrypt加密)
-- ----------------------------
INSERT INTO `sys_user` (
    `id`, `username`, `password`, `nickname`, `email`, 
    `status`, `user_type`, `source_type`, `is_first_login`, `created_by`
) VALUES (
    'admin-001',
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
    '系统管理员',
    'admin@enterprise.com',
    1,
    1,
    'LOCAL',
    0,
    'system'
);

SET FOREIGN_KEY_CHECKS = 1;
