-- ============================================
-- 权限管理服务数据库初始化脚本
-- ============================================

-- 创建角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    role_type VARCHAR(20) NOT NULL DEFAULT 'BUSINESS' COMMENT '角色类型: SYSTEM=系统角色, BUSINESS=业务角色',
    data_scope VARCHAR(20) NOT NULL DEFAULT 'SELF' COMMENT '数据权限范围',
    status INTEGER NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    sort_order INTEGER NOT NULL DEFAULT 0 COMMENT '排序号',
    is_default INTEGER NOT NULL DEFAULT 0 COMMENT '是否为默认角色',
    created_by VARCHAR(64) COMMENT '创建者',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) COMMENT '更新者',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_by VARCHAR(64) COMMENT '删除者',
    deleted_time TIMESTAMP COMMENT '删除时间',
    is_deleted INTEGER NOT NULL DEFAULT 0 COMMENT '是否删除'
);

-- 创建权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_type VARCHAR(20) NOT NULL COMMENT '权限类型: MENU=菜单, BUTTON=按钮, API=接口, DATA=数据, FIELD=字段',
    module VARCHAR(50) COMMENT '所属模块',
    resource_path VARCHAR(200) COMMENT '资源路径/接口路径',
    http_method VARCHAR(10) COMMENT 'HTTP方法',
    parent_id VARCHAR(64) COMMENT '父权限ID',
    description VARCHAR(200) COMMENT '权限描述',
    status INTEGER NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    sort_order INTEGER NOT NULL DEFAULT 0 COMMENT '排序号',
    created_by VARCHAR(64) COMMENT '创建者',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) COMMENT '更新者',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_by VARCHAR(64) COMMENT '删除者',
    deleted_time TIMESTAMP COMMENT '删除时间',
    is_deleted INTEGER NOT NULL DEFAULT 0 COMMENT '是否删除'
);

-- 创建菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    title VARCHAR(50) NOT NULL COMMENT '菜单标题',
    icon VARCHAR(100) COMMENT '菜单图标',
    menu_type VARCHAR(20) NOT NULL COMMENT '菜单类型: CATALOG=目录, MENU=菜单, BUTTON=按钮',
    path VARCHAR(100) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    parent_id VARCHAR(64) COMMENT '父菜单ID',
    sort_order INTEGER NOT NULL DEFAULT 0 COMMENT '菜单排序',
    is_hidden INTEGER NOT NULL DEFAULT 0 COMMENT '是否隐藏: 0=显示, 1=隐藏',
    is_cache INTEGER NOT NULL DEFAULT 0 COMMENT '是否缓存: 0=不缓存, 1=缓存',
    meta TEXT COMMENT '菜单元数据(JSON)',
    status INTEGER NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    application VARCHAR(20) NOT NULL DEFAULT 'WEB' COMMENT '所属应用: WEB=PC端, MOBILE=移动端',
    created_by VARCHAR(64) COMMENT '创建者',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) COMMENT '更新者',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_by VARCHAR(64) COMMENT '删除者',
    deleted_time TIMESTAMP COMMENT '删除时间',
    is_deleted INTEGER NOT NULL DEFAULT 0 COMMENT '是否删除'
);

-- 创建角色-权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id VARCHAR(64) PRIMARY KEY,
    role_id VARCHAR(64) NOT NULL COMMENT '角色ID',
    permission_id VARCHAR(64) NOT NULL COMMENT '权限ID',
    created_by VARCHAR(64) COMMENT '创建者',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE(role_id, permission_id)
);

-- 创建用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    role_id VARCHAR(64) NOT NULL COMMENT '角色ID',
    created_by VARCHAR(64) COMMENT '创建者',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE(user_id, role_id)
);

-- 创建数据权限配置表
CREATE TABLE IF NOT EXISTS sys_data_permission (
    id VARCHAR(64) PRIMARY KEY,
    role_id VARCHAR(64) COMMENT '角色ID',
    user_id VARCHAR(64) COMMENT '用户ID',
    permission_type VARCHAR(20) NOT NULL COMMENT '权限类型: DEPT=部门, DEPT_AND_CHILD=部门及子部门, CUSTOM_DEPT=自定义部门, DATA_RANGE=数据范围',
    allowed_dept_ids TEXT COMMENT '允许访问的部门ID列表(JSON)',
    permission_rules TEXT COMMENT '数据权限规则(JSON)',
    created_by VARCHAR(64) COMMENT '创建者',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) COMMENT '更新者',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建索引
CREATE INDEX idx_role_code ON sys_role(code);
CREATE INDEX idx_role_type ON sys_role(role_type);
CREATE INDEX idx_role_status ON sys_role(status);
CREATE INDEX idx_permission_code ON sys_permission(code);
CREATE INDEX idx_permission_type ON sys_permission(permission_type);
CREATE INDEX idx_permission_module ON sys_permission(module);
CREATE INDEX idx_permission_parent ON sys_permission(parent_id);
CREATE INDEX idx_menu_parent ON sys_menu(parent_id);
CREATE INDEX idx_menu_application ON sys_menu(application);
CREATE INDEX idx_role_permission_role ON sys_role_permission(role_id);
CREATE INDEX idx_role_permission_permission ON sys_role_permission(permission_id);
CREATE INDEX idx_user_role_user ON sys_user_role(user_id);
CREATE INDEX idx_user_role_role ON sys_user_role(role_id);
CREATE INDEX idx_data_permission_role ON sys_data_permission(role_id);
CREATE INDEX idx_data_permission_user ON sys_data_permission(user_id);

-- ============================================
-- 初始化数据
-- ============================================

-- 插入超级管理员角色
INSERT INTO sys_role (id, name, code, description, role_type, data_scope, status, sort_order, is_default, created_by, created_time, updated_by, updated_time)
VALUES ('role-super-admin', '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 'SYSTEM', 'ALL', 1, 0, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- 插入普通用户角色
INSERT INTO sys_role (id, name, code, description, role_type, data_scope, status, sort_order, is_default, created_by, created_time, updated_by, updated_time)
VALUES ('role-normal-user', '普通用户', 'NORMAL_USER', '普通用户，默认角色', 'BUSINESS', 'SELF', 1, 100, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- 插入系统管理员角色
INSERT INTO sys_role (id, name, code, description, role_type, data_scope, status, sort_order, is_default, created_by, created_time, updated_by, updated_time)
VALUES ('role-system-admin', '系统管理员', 'SYSTEM_ADMIN', '系统管理员，负责系统配置', 'SYSTEM', 'ALL', 1, 10, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- 插入数据管理员角色
INSERT INTO sys_role (id, name, code, description, role_type, data_scope, status, sort_order, is_default, created_by, created_time, updated_by, updated_time)
VALUES ('role-data-admin', '数据管理员', 'DATA_ADMIN', '数据管理员，负责数据管理', 'BUSINESS', 'DEPT', 1, 20, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- 插入审计员角色
INSERT INTO sys_role (id, name, code, description, role_type, data_scope, status, sort_order, is_default, created_by, created_time, updated_by, updated_time)
VALUES ('role-auditor', '审计员', 'AUDITOR', '审计员，负责审计日志查看', 'SYSTEM', 'ALL', 1, 30, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- 插入系统管理模块权限
INSERT INTO sys_permission (id, name, code, permission_type, module, resource_path, http_method, description, status, sort_order, created_by, created_time, updated_by, updated_time)
VALUES 
('perm-system-user-view', '查看用户', 'system:user:view', 'BUTTON', 'system', '/api/v1/users', 'GET', '查看用户列表', 1, 10, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('perm-system-user-create', '创建用户', 'system:user:create', 'BUTTON', 'system', '/api/v1/users', 'POST', '创建用户', 1, 11, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('perm-system-user-update', '更新用户', 'system:user:update', 'BUTTON', 'system', '/api/v1/users/*', 'PUT', '更新用户', 1, 12, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('perm-system-user-delete', '删除用户', 'system:user:delete', 'BUTTON', 'system', '/api/v1/users/*', 'DELETE', '删除用户', 1, 13, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('perm-system-role-view', '查看角色', 'system:role:view', 'BUTTON', 'system', '/api/v1/roles', 'GET', '查看角色列表', 1, 20, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('perm-system-role-create', '创建角色', 'system:role:create', 'BUTTON', 'system', '/api/v1/roles', 'POST', '创建角色', 1, 21, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('perm-system-role-update', '更新角色', 'system:role:update', 'BUTTON', 'system', '/api/v1/roles/*', 'PUT', '更新角色', 1, 22, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('perm-system-role-delete', '删除角色', 'system:role:delete', 'BUTTON', 'system', '/api/v1/roles/*', 'DELETE', '删除角色', 1, 23, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('perm-system-permission-view', '查看权限', 'system:permission:view', 'BUTTON', 'system', '/api/v1/permissions', 'GET', '查看权限列表', 1, 30, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('perm-system-permission-manage', '管理权限', 'system:permission:manage', 'BUTTON', 'system', '/api/v1/permissions/*', 'POST,PUT,DELETE', '管理权限', 1, 31, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- 插入资产管理模块权限
INSERT INTO sys_permission (id, name, code, permission_type, module, resource_path, http_method, description, status, sort_order, created_by, created_time, updated_by, updated_time)
VALUES 
('perm-asset-view', '查看资产', 'asset:view', 'BUTTON', 'asset', '/api/v1/assets', 'GET', '查看资产列表', 1, 100, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('perm-asset-create', '创建资产', 'asset:create', 'BUTTON', 'asset', '/api/v1/assets', 'POST', '创建资产', 1, 101, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('perm-asset-update', '更新资产', 'asset:update', 'BUTTON', 'asset', '/api/v1/assets/*', 'PUT', '更新资产', 1, 102, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('perm-asset-delete', '删除资产', 'asset:delete', 'BUTTON', 'asset', '/api/v1/assets/*', 'DELETE', '删除资产', 1, 103, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('perm-asset-export', '导出资产', 'asset:export', 'BUTTON', 'asset', '/api/v1/assets/export', 'POST', '导出资产数据', 1, 104, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- 插入菜单数据
INSERT INTO sys_menu (id, name, title, icon, menu_type, path, component, parent_id, sort_order, is_hidden, is_cache, status, application, created_by, created_time, updated_by, updated_time)
VALUES
-- 顶级目录
('menu-system', '系统管理', '系统管理', 'Setting', 'CATALOG', '/system', NULL, NULL, 100, 0, 0, 1, 'WEB', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('menu-asset', '资产管理', '资产管理', 'Database', 'CATALOG', '/asset', NULL, NULL, 200, 0, 0, 1, 'WEB', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),

-- 系统管理子菜单
('menu-system-user', '用户管理', '用户管理', 'User', 'MENU', '/system/user', 'system/User', 'menu-system', 1, 0, 0, 1, 'WEB', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('menu-system-role', '角色管理', '角色管理', 'Role', 'MENU', '/system/role', 'system/Role', 'menu-system', 2, 0, 0, 1, 'WEB', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('menu-system-permission', '权限管理', '权限管理', 'Key', 'MENU', '/system/permission', 'system/Permission', 'menu-system', 3, 0, 0, 1, 'WEB', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('menu-system-menu', '菜单管理', '菜单管理', 'Menu', 'MENU', '/system/menu', 'system/Menu', 'menu-system', 4, 0, 0, 1, 'WEB', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),

-- 资产管理子菜单
('menu-asset-list', '资产列表', '资产列表', 'List', 'MENU', '/asset/list', 'asset/AssetList', 'menu-asset', 1, 0, 0, 1, 'WEB', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('menu-asset-import', '资产导入', '资产导入', 'Upload', 'MENU', '/asset/import', 'asset/AssetImport', 'menu-asset', 2, 0, 0, 1, 'WEB', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('menu-asset-classify', '资产分类', '资产分类', 'Category', 'MENU', '/asset/classify', 'asset/AssetClassify', 'menu-asset', 3, 0, 0, 1, 'WEB', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- 为超级管理员角色分配所有权限
INSERT INTO sys_role_permission (id, role_id, permission_id, created_by, created_time)
SELECT 'rp-sa-' || perm.id, 'role-super-admin', perm.id, 'system', CURRENT_TIMESTAMP
FROM sys_permission perm;

-- 为系统管理员角色分配系统管理权限
INSERT INTO sys_role_permission (id, role_id, permission_id, created_by, created_time)
SELECT 'rp-sysadm-' || perm.id, 'role-system-admin', perm.id, 'system', CURRENT_TIMESTAMP
FROM sys_permission perm
WHERE perm.module = 'system';

-- 为普通用户角色分配基础资产查看权限
INSERT INTO sys_role_permission (id, role_id, permission_id, created_by, created_time)
VALUES 
('rp-nu-1', 'role-normal-user', 'perm-asset-view', 'system', CURRENT_TIMESTAMP);
