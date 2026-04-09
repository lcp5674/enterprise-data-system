-- 企业数据资产管理系统 - 数据库初始化脚本
-- 运行此脚本以创建所有必需的表

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT UNIQUE NOT NULL,
    name TEXT,
    avatar_url TEXT,
    role TEXT DEFAULT 'viewer' CHECK (role IN ('admin', 'editor', 'viewer')),
    department TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- 数据源表
-- ============================================
CREATE TABLE IF NOT EXISTS data_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    connection_config JSONB DEFAULT '{}',
    owner_id UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- 数据资产表
-- ============================================
CREATE TABLE IF NOT EXISTS data_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guid TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    type TEXT NOT NULL CHECK (type IN ('table', 'file', 'api', 'metric', 'dashboard')),
    source_id UUID REFERENCES data_sources(id),
    schema_definition JSONB,
    security_level TEXT DEFAULT 'internal' CHECK (security_level IN ('public', 'internal', 'sensitive', 'confidential')),
    quality_score DECIMAL(5,2) DEFAULT 0,
    usage_count INTEGER DEFAULT 0,
    owner_id UUID REFERENCES users(id),
    tags TEXT[] DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- 资产收藏表
-- ============================================
CREATE TABLE IF NOT EXISTS asset_favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    asset_id UUID REFERENCES data_assets(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, asset_id)
);

-- ============================================
-- 血缘关系表
-- ============================================
CREATE TABLE IF NOT EXISTS lineage_edges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_asset_id UUID REFERENCES data_assets(id) ON DELETE CASCADE,
    target_asset_id UUID REFERENCES data_assets(id) ON DELETE CASCADE,
    transformation_type TEXT,
    transformation_detail TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- 资产访问记录表
-- ============================================
CREATE TABLE IF NOT EXISTS asset_access_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    asset_id UUID REFERENCES data_assets(id) ON DELETE CASCADE,
    action TEXT CHECK (action IN ('view', 'search', 'favorite', 'export')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- 反馈表
-- ============================================
CREATE TABLE IF NOT EXISTS feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    content TEXT NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    category TEXT CHECK (category IN ('bug', 'feature', 'general')),
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'reviewed', 'resolved')),
    contact_email TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- 通知表
-- ============================================
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    content TEXT,
    type TEXT DEFAULT 'info' CHECK (type IN ('info', 'warning', 'success', 'error')),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ============================================
-- 索引创建
-- ============================================
CREATE INDEX IF NOT EXISTS idx_assets_type ON data_assets(type);
CREATE INDEX IF NOT EXISTS idx_assets_security_level ON data_assets(security_level);
CREATE INDEX IF NOT EXISTS idx_assets_owner ON data_assets(owner_id);
CREATE INDEX IF NOT EXISTS idx_assets_name ON data_assets(name);
CREATE INDEX IF NOT EXISTS idx_assets_tags ON data_assets USING GIN(tags);
CREATE INDEX IF NOT EXISTS idx_lineage_source ON lineage_edges(source_asset_id);
CREATE INDEX IF NOT EXISTS idx_lineage_target ON lineage_edges(target_asset_id);
CREATE INDEX IF NOT EXISTS idx_access_logs_asset ON asset_access_logs(asset_id);
CREATE INDEX IF NOT EXISTS idx_access_logs_user ON asset_access_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;

-- ============================================
-- RLS (Row Level Security) 策略
-- ============================================
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE data_assets ENABLE ROW LEVEL SECURITY;
ALTER TABLE asset_favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE feedback ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

-- 用户只能看到自己的数据
CREATE POLICY "Users can view own profile" ON users
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" ON users
    FOR UPDATE USING (auth.uid() = id);

-- 所有认证用户可以查看资产
CREATE POLICY "Authenticated users can view assets" ON data_assets
    FOR SELECT USING (auth.role() = 'authenticated');

-- 用户只能看到自己的收藏
CREATE POLICY "Users can view own favorites" ON asset_favorites
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can add own favorites" ON asset_favorites
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own favorites" ON asset_favorites
    FOR DELETE USING (auth.uid() = user_id);

-- 所有认证用户可以提交反馈
CREATE POLICY "Authenticated users can submit feedback" ON feedback
    FOR INSERT WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Users can view own feedback" ON feedback
    FOR SELECT USING (auth.uid() = user_id OR auth.uid()::text IN (SELECT id::text FROM users WHERE role = 'admin'));

-- 用户只能看到自己的通知
CREATE POLICY "Users can view own notifications" ON notifications
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can update own notifications" ON notifications
    FOR UPDATE USING (auth.uid() = user_id);

-- ============================================
-- 函数和触发器
-- ============================================

-- 更新时间戳的函数
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为需要自动更新updated_at的表创建触发器
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER update_data_assets_updated_at
    BEFORE UPDATE ON data_assets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ============================================
-- 视图
-- ============================================

-- 带所有者信息的资产视图
CREATE OR REPLACE VIEW data_assets_with_owners AS
SELECT 
    da.*,
    u.name AS owner_name,
    u.email AS owner_email,
    u.department AS owner_department,
    ds.name AS source_name,
    ds.type AS source_type,
    (SELECT COUNT(*) FROM lineage_edges WHERE source_asset_id = da.id OR target_asset_id = da.id) AS lineage_count
FROM data_assets da
LEFT JOIN users u ON da.owner_id = u.id
LEFT JOIN data_sources ds ON da.source_id = ds.id;

-- ============================================
-- 模拟数据（用于演示）
-- ============================================

-- 插入演示用户
INSERT INTO users (id, email, name, role, department) VALUES
    ('11111111-1111-1111-1111-111111111111', 'demo@example.com', '演示用户', 'editor', '数据平台部'),
    ('22222222-2222-2222-2222-222222222222', 'zhangsan@company.com', '张三', 'editor', '数据平台部'),
    ('33333333-3333-3333-3333-333333333333', 'lisi@company.com', '李四', 'editor', '数据平台部'),
    ('44444444-4444-4444-4444-444444444444', 'wangwu@company.com', '王五', 'viewer', '财务部');

-- 插入数据源
INSERT INTO data_sources (id, name, type, owner_id) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'MySQL - 业务数据库集群', 'mysql', '22222222-2222-2222-2222-222222222222'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Kafka - 实时数据流', 'kafka', '33333333-3333-3333-3333-333333333333');

-- 插入数据资产
INSERT INTO data_assets (id, guid, name, description, type, source_id, security_level, quality_score, usage_count, owner_id, tags) VALUES
    ('11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'asset-001', 'ods_orders', '订单原始数据表，包含所有渠道订单原始数据', 'table', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'internal', 92, 156, '22222222-2222-2222-2222-222222222222', ARRAY['ods', '订单', '原始数据']),
    ('22222222-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'asset-002', 'dim_customer', '客户维度表，存储客户维度信息', 'table', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'sensitive', 88, 234, '33333333-3333-3333-3333-333333333333', ARRAY['dim', '客户', '维度']),
    ('33333333-cccc-cccc-cccc-cccccccccccc', 'asset-003', 'dwd_orders', '订单明细宽表', 'table', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'internal', 85, 189, '22222222-2222-2222-2222-222222222222', ARRAY['dwd', '订单', '明细']),
    ('44444444-dddd-dddd-dddd-dddddddddddd', 'asset-004', 'ads_revenue_report', '收入报表指标', 'metric', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'internal', 95, 78, '44444444-4444-4444-4444-444444444444', ARRAY['ads', '收入', '报表']),
    ('55555555-eeee-eeee-eeee-eeeeeeeeeeee', 'asset-005', 'CustomerAPI', '客户信息服务接口', 'api', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'sensitive', 91, 56, '33333333-3333-3333-3333-333333333333', ARRAY['api', '客户', '服务']);

-- 插入血缘关系
INSERT INTO lineage_edges (source_asset_id, target_asset_id, transformation_type, transformation_detail) VALUES
    ('11111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-cccc-cccc-cccc-cccccccccccc', 'ETL', '订单数据清洗'),
    ('22222222-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-cccc-cccc-cccc-cccccccccccc', 'ETL', '客户数据关联'),
    ('33333333-cccc-cccc-cccc-cccccccccccc', '44444444-dddd-dddd-dddd-dddddddddddd', 'Aggregation', '收入汇总计算');

-- 插入示例通知
INSERT INTO notifications (user_id, title, content, type, is_read) VALUES
    ('11111111-1111-1111-1111-111111111111', '数据质量告警', 'dwd_orders 表的质量评分下降至 72 分', 'warning', false),
    ('11111111-1111-1111-1111-111111111111', '血缘变更通知', 'dim_customer 表新增下游依赖', 'info', false),
    ('11111111-1111-1111-1111-111111111111', '新功能上线', '数据血缘追踪功能已升级，支持字段级血缘', 'success', true);

-- 插入示例反馈
INSERT INTO feedback (user_id, content, rating, category, status, contact_email) VALUES
    ('22222222-2222-2222-2222-222222222222', '建议增加数据血缘的批量导出功能，方便在会议中展示。', 5, 'feature', 'reviewed', 'zhangsan@company.com'),
    ('33333333-3333-3333-3333-333333333333', '资产搜索的结果分页加载有些慢，希望能优化一下。', 3, 'bug', 'resolved', 'lisi@company.com'),
    ('44444444-4444-4444-4444-444444444444', '整体使用体验不错，特别是数据地图的分类功能很实用！', 5, 'general', 'resolved', 'wangwu@company.com');

-- ============================================
-- 权限授予（用于演示）
-- ============================================
-- 将演示用户的ID设置到public schema中以便RLS测试
-- 注意：实际部署时应该使用proper authentication

COMMENT ON TABLE users IS '用户表';
COMMENT ON TABLE data_assets IS '数据资产表';
COMMENT ON TABLE data_sources IS '数据源表';
COMMENT ON TABLE lineage_edges IS '血缘关系表';
COMMENT ON TABLE feedback IS '用户反馈表';
COMMENT ON TABLE notifications IS '通知表';
