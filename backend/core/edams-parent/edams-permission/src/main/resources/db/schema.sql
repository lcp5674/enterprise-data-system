-- EDAMS权限服务 - 权限、菜单、角色权限关联表
USE edams;

-- ==================== 权限表 ====================
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `type` TINYINT DEFAULT 1 COMMENT '类型：1-菜单，2-按钮，3-数据，4-API接口',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父级权限ID',
    `menu_id` BIGINT DEFAULT NULL COMMENT '关联菜单ID',
    `path` VARCHAR(200) DEFAULT NULL COMMENT 'API请求路径',
    `method` VARCHAR(10) DEFAULT NULL COMMENT 'HTTP方法',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    `version` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_type` (`type`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ==================== 菜单表 ====================
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `code` VARCHAR(50) NOT NULL COMMENT '菜单编码（唯一）',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID，0为顶级',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `type` TINYINT DEFAULT 1 COMMENT '类型：0-目录，1-菜单，2-按钮',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '前端路由路径',
    `component` VARCHAR(300) DEFAULT NULL COMMENT '组件路径',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `permission` VARCHAR(200) DEFAULT NULL COMMENT '权限标识',
    `is_external` TINYINT DEFAULT 0 COMMENT '是否外链',
    `is_cache` TINYINT DEFAULT 0 COMMENT '是否缓存页面',
    `is_hidden` TINYINT DEFAULT 0 COMMENT '是否隐藏',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `tenant_id` BIGINT DEFAULT 1,
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    `version` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表';

-- ==================== 角色-权限关联表 ====================
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== 角色-菜单关联表 ====================
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== 初始数据 ====================
INSERT INTO `sys_permission` (name, code, type, parent_id, description)
VALUES
('资产管理', 'asset:manage', 1, 0, '资产模块总权限'),
('资产查询', 'asset:query', 2, 1, '查看资产列表和详情'),
('资产创建', 'asset:create', 2, 1, '创建新资产'),
('资产编辑', 'asset:update', 2, 1, '编辑资产信息'),
('资产删除', 'asset:delete', 2, 1, '删除资产'),
('用户管理', 'user:manage', 1, 0, '用户管理总权限'),
('用户查询', 'user:query', 2, 6, '查看用户列表'),
('用户新增', 'user:create', 2, 6, '创建新用户'),
('用户编辑', 'user:update', 2, 6, '编辑用户信息'),
('用户删除', 'user:delete', 2, 6, '删除用户'),
('角色管理', 'role:manage', 1, 0, '角色管理总权限'),
('权限管理', 'permission:manage', 1, 0, '权限配置总权限'),
('数据治理', 'governance:manage', 1, 0, '数据治理模块总权限');

INSERT INTO `sys_menu` (name, code, parent_id, type, path, component, icon, permission)
VALUES
('工作台', 'DASHBOARD', 0, 0, '/dashboard', '/views/dashboard/index.vue', 'DashboardOutlined', NULL),
('数据资产', 'ASSET', 0, 0, '/assets', 'Layout', 'DatabaseOutlined', 'asset:manage'),
('资产目录', 'ASSET_CATALOG', 2, 1, '/assets/catalog', '/views/assets/catalog/index.vue', 'AppstoreOutlined', 'asset:query'),
('元数据管理', 'METADATA', 0, 0, '/metadata', 'Layout', 'CodeOutlined', NULL),
('数据质量', 'QUALITY', 0, 0, '/quality', 'Layout', 'SafetyCertificateOutlined', 'quality:manage'),
('血缘分析', 'LINEAGE', 0, 0, '/lineage', 'Layout', 'ApartmentOutlined', NULL),
('治理中心', 'GOVERNANCE', 0, 0, '/governance', 'Layout', 'ToolOutlined', 'governance:manage'),
('系统管理', 'SYSTEM', 0, 0, '/system', 'Layout', 'SettingOutlined', NULL),
('用户管理', 'SYS_USER', 8, 1, '/system/users', '/views/system/user/index.vue', 'UserOutlined', 'user:manage'),
('角色管理', 'SYS_ROLE', 8, 1, '/system/roles', '/views/system/role/index.vue', 'TeamOutlined', 'role:manage'),
('菜单管理', 'SYS_MENU', 8, 1, '/system/menus', '/views/system/menu/index.vue', 'MenuOutlined', 'permission:manage');
