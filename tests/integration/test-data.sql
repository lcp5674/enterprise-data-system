-- ============================================
-- EDAMS 测试数据
-- 用于集成测试和E2E测试
-- ============================================

-- 用户表测试数据
INSERT INTO test_users (id, username, email, password, full_name, department, status, created_at)
VALUES
    (1, 'admin', 'admin@edams.com', '$2a$10$...', '系统管理员', 'IT部门', 'ACTIVE', NOW()),
    (2, 'test_user', 'test@edams.com', '$2a$10$...', '测试用户', '测试部门', 'ACTIVE', NOW()),
    (3, 'guest', 'guest@edams.com', '$2a$10$...', '访客用户', '访客部', 'INACTIVE', NOW());

-- 资产表测试数据
INSERT INTO test_assets (id, asset_code, asset_name, asset_type, owner, status, quality_score, created_at)
VALUES
    (1, 'ASSET-001', '用户信息表', 'TABLE', 'admin', 'ACTIVE', 95.5, NOW()),
    (2, 'ASSET-002', '订单流水表', 'TABLE', 'admin', 'ACTIVE', 88.2, NOW()),
    (3, 'ASSET-003', '产品目录API', 'API', 'test_user', 'DRAFT', 75.0, NOW());

-- 质量规则测试数据
INSERT INTO test_quality_rules (id, rule_code, rule_name, rule_type, quality_dimension, severity_level, enabled, status)
VALUES
    (1, 'RULE-001', '非空检查规则', 'COMPLETENESS', '完整性', 'HIGH', true, 'ACTIVE'),
    (2, 'RULE-002', '格式校验规则', 'VALIDITY', '有效性', 'MEDIUM', true, 'ACTIVE'),
    (3, 'RULE-003', '唯一性规则', 'UNIQUENESS', '唯一性', 'HIGH', false, 'DRAFT');

-- 数据血缘测试数据
INSERT INTO test_lineage (id, source_asset, target_asset, relation_type, transformation)
VALUES
    (1, 'ASSET-001', 'ASSET-002', 'DERIVES_FROM', 'SELECT * FROM user_orders'),
    (2, 'ASSET-002', 'ASSET-003', 'FEEDS_INTO', '聚合统计');

-- 数据标准测试数据
INSERT INTO test_standards (id, standard_code, standard_name, standard_type, standard_category, status)
VALUES
    (1, 'STD-001', '手机号格式标准', 'FORMAT', '个人信息', 'PUBLISHED'),
    (2, 'STD-002', '邮箱格式标准', 'FORMAT', '个人信息', 'PUBLISHED'),
    (3, 'STD-003', '日期格式标准', 'FORMAT', '日期时间', 'DRAFT');
