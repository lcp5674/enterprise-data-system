-- =============================================
-- EDAMS Knowledge Service Database Schema
-- 知识图谱服务数据库表结构
-- =============================================

-- 本体论表
CREATE TABLE IF NOT EXISTS kb_ontology (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '本体论ID',
    name            VARCHAR(100) NOT NULL COMMENT '本体论名称',
    description     TEXT COMMENT '本体论描述',
    version         VARCHAR(50) DEFAULT '1.0.0' COMMENT '版本号',
    namespace       VARCHAR(200) COMMENT '命名空间',
    root_class_count INT DEFAULT 0 COMMENT '根类数量',
    total_class_count INT DEFAULT 0 COMMENT '总类数量',
    total_entity_count BIGINT DEFAULT 0 COMMENT '总实体数量',
    total_relation_count BIGINT DEFAULT 0 COMMENT '总关系数量',
    status          VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿, PUBLISHED-已发布, DEPRECATED-已废弃',
    creator         VARCHAR(64) COMMENT '创建者',
    modifier        VARCHAR(64) COMMENT '修改者',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_creator (creator)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本体论表';

-- 本体类表
CREATE TABLE IF NOT EXISTS kb_ontology_class (
    id                  BIGINT NOT NULL AUTO_INCREMENT COMMENT '类ID',
    ontology_id         BIGINT NOT NULL COMMENT '所属本体论ID',
    parent_class_id     BIGINT COMMENT '父类ID',
    class_name          VARCHAR(100) NOT NULL COMMENT '类名称',
    class_name_zh       VARCHAR(100) COMMENT '类中文名称',
    description         TEXT COMMENT '类描述',
    icon                VARCHAR(100) COMMENT '图标',
    color               VARCHAR(20) COMMENT '颜色',
    level               INT DEFAULT 0 COMMENT '层级深度',
    is_leaf             TINYINT DEFAULT 1 COMMENT '是否为叶子节点',
    subclass_count      INT DEFAULT 0 COMMENT '子类数量',
    instance_count      INT DEFAULT 0 COMMENT '实例数量',
    properties          JSON COMMENT '属性定义',
    sort_order          INT DEFAULT 0 COMMENT '显示顺序',
    creator             VARCHAR(64) COMMENT '创建者',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    INDEX idx_ontology_id (ontology_id),
    INDEX idx_parent_id (parent_class_id),
    INDEX idx_class_name (class_name),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本体类表';

-- 实体表
CREATE TABLE IF NOT EXISTS kb_entity (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '实体ID',
    ontology_id     BIGINT NOT NULL COMMENT '所属本体论ID',
    class_id        BIGINT COMMENT '所属类ID',
    name            VARCHAR(200) NOT NULL COMMENT '实体名称',
    unique_id       VARCHAR(200) COMMENT '实体唯一标识',
    alias           VARCHAR(500) COMMENT '别名/同义词',
    description     TEXT COMMENT '描述',
    entity_type     VARCHAR(20) DEFAULT 'OBJECT' COMMENT '实体类型: CONCEPT-概念, OBJECT-对象, EVENT-事件',
    tags            VARCHAR(500) COMMENT '标签',
    properties      JSON COMMENT '属性',
    extra_properties JSON COMMENT '扩展属性',
    image_url       VARCHAR(500) COMMENT '图片URL',
    confidence      INT DEFAULT 100 COMMENT '置信度',
    source          VARCHAR(100) COMMENT '来源',
    status          VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-活跃, INACTIVE-不活跃, DELETED-已删除',
    favorite_count  INT DEFAULT 0 COMMENT '收藏数',
    view_count      BIGINT DEFAULT 0 COMMENT '访问次数',
    creator         VARCHAR(64) COMMENT '创建者',
    department      VARCHAR(100) COMMENT '创建部门',
    asset_id        BIGINT COMMENT '关联资产ID',
    asset_type      VARCHAR(50) COMMENT '关联资产类型',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    UNIQUE INDEX idx_unique_id (unique_id),
    INDEX idx_ontology_id (ontology_id),
    INDEX idx_class_id (class_id),
    INDEX idx_name (name),
    INDEX idx_tags (tags),
    INDEX idx_status (status),
    INDEX idx_view_count (view_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体表';

-- 关系表
CREATE TABLE IF NOT EXISTS kb_relation (
    id                  BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    ontology_id         BIGINT NOT NULL COMMENT '所属本体论ID',
    source_entity_id    BIGINT NOT NULL COMMENT '源实体ID',
    source_entity_name  VARCHAR(200) COMMENT '源实体名称',
    target_entity_id    BIGINT NOT NULL COMMENT '目标实体ID',
    target_entity_name  VARCHAR(200) COMMENT '目标实体名称',
    relation_type       VARCHAR(50) NOT NULL COMMENT '关系类型',
    relation_name       VARCHAR(100) COMMENT '关系名称',
    description         TEXT COMMENT '关系描述',
    direction           VARCHAR(20) DEFAULT 'DIRECT' COMMENT '方向: DIRECT-正向, REVERSE-反向',
    properties          JSON COMMENT '属性',
    weight              DOUBLE DEFAULT 1.0 COMMENT '权重',
    confidence          INT DEFAULT 100 COMMENT '置信度',
    evidence            TEXT COMMENT '证据/来源',
    is_inferred         TINYINT DEFAULT 0 COMMENT '是否为推理关系',
    status              VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-活跃, INACTIVE-不活跃, DELETED-已删除',
    creator             VARCHAR(64) COMMENT '创建者',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    INDEX idx_ontology_id (ontology_id),
    INDEX idx_source_entity (source_entity_id),
    INDEX idx_target_entity (target_entity_id),
    INDEX idx_relation_type (relation_type),
    INDEX idx_confidence (confidence),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关系表';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入示例本体论
INSERT INTO kb_ontology (id, name, description, version, namespace, status, creator) VALUES
(1, '企业知识本体', '企业数据资产管理系统的核心本体论', '1.0.0', 'http://edams.com/ontology/enterprise', 'PUBLISHED', 'system'),
(2, '产品知识本体', '产品相关的知识图谱本体', '1.0.0', 'http://edams.com/ontology/product', 'DRAFT', 'admin');

-- 插入示例类
INSERT INTO kb_ontology_class (id, ontology_id, class_name, class_name_zh, description, level, is_leaf, sort_order) VALUES
(1, 1, 'DataAsset', '数据资产', '数据资产根类', 0, 0, 1),
(2, 1, 'Table', '数据表', '数据库表', 1, 1, 1),
(3, 1, 'API', 'API接口', 'API接口', 1, 1, 2),
(4, 1, 'File', '数据文件', '数据文件', 1, 1, 3),
(5, 1, 'Person', '人员', '人员信息', 0, 1, 2),
(6, 1, 'Department', '部门', '部门信息', 0, 1, 3);

-- 更新类之间的父子关系
UPDATE kb_ontology_class SET parent_class_id = 1 WHERE id IN (2, 3, 4);

-- 插入示例实体
INSERT INTO kb_entity (id, ontology_id, class_id, name, unique_id, description, entity_type, tags, status) VALUES
(1, 1, 2, '用户信息表', 'table:users', '存储用户基本信息', 'OBJECT', '用户,基础数据', 'ACTIVE'),
(2, 1, 2, '订单信息表', 'table:orders', '存储订单信息', 'OBJECT', '订单,交易', 'ACTIVE'),
(3, 1, 3, '用户查询API', 'api:user:query', '用户查询接口', 'OBJECT', '用户,查询', 'ACTIVE'),
(4, 1, 5, '张三', 'person:zhangsan', '技术部员工', 'OBJECT', '员工,技术', 'ACTIVE'),
(5, 1, 6, '技术部', 'dept:tech', '技术研发部门', 'OBJECT', '部门,研发', 'ACTIVE');

-- 插入示例关系
INSERT INTO kb_relation (ontology_id, source_entity_id, source_entity_name, target_entity_id, target_entity_name, relation_type, relation_name, weight) VALUES
(1, 4, '张三', 5, '技术部', 'BELONGS_TO', '属于', 1.0),
(1, 3, '用户查询API', 1, '用户信息表', 'DEPENDS_ON', '依赖', 0.8),
(1, 2, '订单信息表', 1, '用户信息表', 'DEPENDS_ON', '依赖', 0.9);
