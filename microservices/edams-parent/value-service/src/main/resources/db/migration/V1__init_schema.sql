-- =====================================================
-- V1__init_schema.sql
-- 数据价值服务 - 初始化表结构
-- =====================================================

-- 数据价值评估表
CREATE TABLE IF NOT EXISTS value_assessment (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    asset_name VARCHAR(200) COMMENT '资产名称',
    asset_type VARCHAR(32) COMMENT '资产类型',
    overall_score DECIMAL(5, 2) DEFAULT 0 COMMENT '综合评分',
    business_score DECIMAL(5, 2) DEFAULT 0 COMMENT '业务价值评分',
    technical_score DECIMAL(5, 2) DEFAULT 0 COMMENT '技术价值评分',
    economic_score DECIMAL(5, 2) DEFAULT 0 COMMENT '经济价值评分',
    usage_score DECIMAL(5, 2) DEFAULT 0 COMMENT '使用价值评分',
    scarcity_score DECIMAL(5, 2) DEFAULT 0 COMMENT '稀缺性评分',
    assessment_date TIMESTAMP COMMENT '评估日期',
    assessment_method VARCHAR(64) COMMENT '评估方法',
    assessment_detail JSON COMMENT '评估详情',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '评估状态',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

-- 数据产品表
CREATE TABLE IF NOT EXISTS data_product (
    id BIGSERIAL PRIMARY KEY,
    product_code VARCHAR(64) NOT NULL UNIQUE COMMENT '产品编码',
    product_name VARCHAR(200) NOT NULL COMMENT '产品名称',
    description TEXT COMMENT '产品描述',
    product_type VARCHAR(32) NOT NULL COMMENT '产品类型',
    version VARCHAR(32) DEFAULT '1.0.0' COMMENT '版本号',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '产品状态',
    asset_ids JSON COMMENT '关联资产ID',
    price DECIMAL(12, 2) DEFAULT 0 COMMENT '价格',
    price_unit VARCHAR(32) COMMENT '单位',
    sales_count INT DEFAULT 0 COMMENT '销量',
    rating DECIMAL(3, 2) DEFAULT 0 COMMENT '评分',
    category_id BIGINT COMMENT '分类ID',
    tags JSON COMMENT '标签',
    preview_data TEXT COMMENT '预览数据',
    documentation TEXT COMMENT '产品文档',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

-- ROI追踪表
CREATE TABLE IF NOT EXISTS roi_tracking (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    asset_name VARCHAR(200) COMMENT '资产名称',
    investment DECIMAL(14, 2) DEFAULT 0 COMMENT '投资金额',
    return_amount DECIMAL(14, 2) DEFAULT 0 COMMENT '回报金额',
    roi_percentage DECIMAL(8, 2) DEFAULT 0 COMMENT 'ROI百分比',
    period_type VARCHAR(32) NOT NULL COMMENT '周期类型',
    period_start TIMESTAMP COMMENT '周期开始日期',
    period_end TIMESTAMP COMMENT '周期结束日期',
    direct_benefit DECIMAL(14, 2) DEFAULT 0 COMMENT '直接收益',
    indirect_benefit DECIMAL(14, 2) DEFAULT 0 COMMENT '间接收益',
    cost_saving DECIMAL(14, 2) DEFAULT 0 COMMENT '成本节省',
    benefit_detail JSON COMMENT '收益明细',
    remark TEXT COMMENT '备注',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

-- 产品分类表
CREATE TABLE IF NOT EXISTS product_category (
    id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT COMMENT '父分类ID',
    level INT DEFAULT 1 COMMENT '层级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    description TEXT COMMENT '描述',
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_value_assessment_asset_id ON value_assessment(asset_id);
CREATE INDEX IF NOT EXISTS idx_value_assessment_status ON value_assessment(status);
CREATE INDEX IF NOT EXISTS idx_value_assessment_overall_score ON value_assessment(overall_score);
CREATE INDEX IF NOT EXISTS idx_value_assessment_assessment_date ON value_assessment(assessment_date);

CREATE INDEX IF NOT EXISTS idx_data_product_product_code ON data_product(product_code);
CREATE INDEX IF NOT EXISTS idx_data_product_status ON data_product(status);
CREATE INDEX IF NOT EXISTS idx_data_product_category_id ON data_product(category_id);

CREATE INDEX IF NOT EXISTS idx_roi_tracking_asset_id ON roi_tracking(asset_id);
CREATE INDEX IF NOT EXISTS idx_roi_tracking_period_type ON roi_tracking(period_type);

CREATE INDEX IF NOT EXISTS idx_product_category_parent_id ON product_category(parent_id);
