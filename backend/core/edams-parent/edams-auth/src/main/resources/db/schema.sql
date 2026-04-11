-- EDAMS认证服务数据库表结构
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4

CREATE DATABASE IF NOT EXISTS edams DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE edams;

-- ==================== 用户表 ====================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名（登录账号）',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `department_id` BIGINT DEFAULT NULL COMMENT '部门ID',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(45) DEFAULT NULL COMMENT '最后登录IP（支持IPv6）',
    `login_fail_count` INT DEFAULT 0 COMMENT '登录失败次数',
    `lock_time` DATETIME DEFAULT NULL COMMENT '账户锁定时间',
    `mfa_enabled` TINYINT DEFAULT 0 COMMENT 'MFA是否启用：0-未启用，1-已启用',
    `mfa_secret` VARCHAR(100) DEFAULT NULL COMMENT 'MFA密钥',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ==================== 登录日志表 ====================
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `ip` VARCHAR(45) DEFAULT NULL COMMENT '登录IP地址',
    `user_agent` VARCHAR(1000) DEFAULT NULL COMMENT '浏览器User-Agent',
    `status` TINYINT NOT NULL COMMENT '登录状态：0-失败，1-成功',
    `message` VARCHAR(200) DEFAULT NULL COMMENT '消息描述',
    `login_time` DATETIME NOT NULL COMMENT '登录时间',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '地理位置',
    `os` VARCHAR(50) DEFAULT NULL COMMENT '操作系统',
    `browser` VARCHAR(50) DEFAULT NULL COMMENT '浏览器类型',
    `login_type` VARCHAR(20) DEFAULT 'password' COMMENT '登录方式：password/MFA/token/sso',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_login_time` (`login_time`),
    KEY `idx_status` (`status`),
    KEY `idx_ip` (`ip`),
    KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- ==================== 初始数据：默认管理员账号 ====================
-- 默认密码: admin@2026 （BCrypt加密后的值）
INSERT INTO `sys_user` (username, password, real_name, email, phone, status, department_id, tenant_id, mfa_enabled)
VALUES ('admin', '$2a$12$LJ3kYvNNF2B3QXqNpKZwOeKmHf5GzRtVYxWnE7PqUcD8sA2bM3i', 
        '系统管理员', 'admin@edams.com', '13800138000', 1, NULL, 1, 0);

INSERT INTO `sys_user` (username, password, real_name, email, phone, status, department_id, tenant_id, mfa_enabled)
VALUES ('testuser', '$2a$12$LJ3kYvNNF2B3QXqNpKZwOeKmHf5GzRtVYxWnE7PqUcD8sA2bM3i',
        '测试用户', 'testuser@edams.com', '13800138001', 1, NULL, 1, 0);
