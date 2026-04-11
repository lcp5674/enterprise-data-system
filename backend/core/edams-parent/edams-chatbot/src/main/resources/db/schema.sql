-- =============================================
-- EDAMS Chatbot AI对话服务数据库脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS edams_chatbot DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE edams_chatbot;

-- =============================================
-- 1. 聊天会话表
-- =============================================
CREATE TABLE IF NOT EXISTS `chat_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_title` VARCHAR(200) COMMENT '会话标题',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(64) COMMENT '用户名',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '会话状态：active, closed',
    `session_type` VARCHAR(50) DEFAULT 'qa' COMMENT '会话类型：qa, task, analysis, faq',
    `context_data` JSON COMMENT '上下文数据(JSON)',
    `last_active_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    `message_count` INT DEFAULT 0 COMMENT '消息数量',
    `avg_response_time` BIGINT COMMENT '平均响应时间(ms)',
    `satisfaction_score` INT COMMENT '满意度评分',
    `closed_time` DATETIME COMMENT '关闭时间',
    `close_reason` VARCHAR(500) COMMENT '关闭原因',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `created_by` VARCHAR(64) COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(64) COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_last_active` (`last_active_time`),
    INDEX `idx_session_type` (`session_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- =============================================
-- 2. 聊天消息表
-- =============================================
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '消息角色：user, assistant, system',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `message_type` VARCHAR(20) DEFAULT 'text' COMMENT '消息类型：text, image, card, action',
    `intent_type` VARCHAR(50) COMMENT '意图类型',
    `intent_confidence` DECIMAL(5,4) COMMENT '意图置信度',
    `entities` JSON COMMENT '关联实体(JSON)',
    `response_time` BIGINT COMMENT '响应时间(ms)',
    `message_time` DATETIME NOT NULL COMMENT '消息时间',
    `quoted_message_id` BIGINT COMMENT '引用消息ID',
    `attachments` JSON COMMENT '附件信息(JSON)',
    `metadata` JSON COMMENT '元数据(JSON)',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
    `rating` INT COMMENT '用户评分',
    `feedback` TEXT COMMENT '用户反馈',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `created_by` VARCHAR(64) COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(64) COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_role` (`role`),
    INDEX `idx_intent_type` (`intent_type`),
    INDEX `idx_message_time` (`message_time`),
    CONSTRAINT `fk_message_session` FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- =============================================
-- 3. 聊天意图表
-- =============================================
CREATE TABLE IF NOT EXISTS `chat_intent` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `intent_type` VARCHAR(50) NOT NULL COMMENT '意图类型：QUERY_METADATA, QUERY_QUALITY, QUERY_LINEAGE, GENERATE_REPORT, CHECK_STATUS, FAQ',
    `description` TEXT COMMENT '意图描述',
    `keywords` JSON COMMENT '关键词(JSON数组)',
    `pattern` VARCHAR(500) COMMENT '匹配模式',
    `examples` TEXT COMMENT '意图示例',
    `response_template` TEXT COMMENT '默认响应模板',
    `target_service` VARCHAR(100) COMMENT '关联的服务',
    `api_endpoint` VARCHAR(200) COMMENT '关联的API端点',
    `priority` INT DEFAULT 100 COMMENT '优先级',
    `confidence_threshold` DECIMAL(5,4) DEFAULT 0.6000 COMMENT '置信度阈值',
    `enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
    `slots` JSON COMMENT '槽位定义(JSON)',
    `slot_rules` JSON COMMENT '槽位提取规则(JSON)',
    `follow_up_intents` VARCHAR(500) COMMENT '后续意图',
    `parent_intent_id` BIGINT COMMENT '父意图ID',
    `tags` JSON COMMENT '标签(JSON)',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `created_by` VARCHAR(64) COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(64) COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_intent_type` (`intent_type`),
    INDEX `idx_enabled` (`enabled`),
    INDEX `idx_priority` (`priority`),
    INDEX `idx_parent_id` (`parent_intent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天意图表';

-- =============================================
-- 初始化意图数据
-- =============================================

INSERT INTO `chat_intent` (`intent_type`, `description`, `keywords`, `pattern`, `priority`, `confidence_threshold`, `enabled`, `slots`, `follow_up_intents`) VALUES
('QUERY_METADATA', '查询元数据信息', '["元数据", "表结构", "字段", "属性", "metadata"]', NULL, 10, 0.6000, 1, '{"assetName": {"type": "string", "required": true}}', 'QUERY_QUALITY,QUERY_LINEAGE'),
('QUERY_QUALITY', '查询数据质量信息', '["质量", "完整性", "准确性", "评分"]', NULL, 20, 0.6000, 1, '{"assetName": {"type": "string", "required": true}, "qualityDimension": {"type": "string", "required": false}}', 'GENERATE_REPORT'),
('QUERY_LINEAGE', '查询数据血缘关系', '["血缘", "上下游", "数据流"]', NULL, 30, 0.6000, 1, '{"assetName": {"type": "string", "required": true}}', 'QUERY_METADATA'),
('GENERATE_REPORT', '生成报表报告', '["报表", "报告", "导出", "生成"]', NULL, 40, 0.6000, 1, '{"reportType": {"type": "string", "required": true, "options": ["quality", "asset", "lineage"]}}', 'FAQ'),
('CHECK_STATUS', '检查系统或资产状态', '["状态", "健康", "监控"]', NULL, 50, 0.6000, 1, '{"targetType": {"type": "string", "required": false}, "targetId": {"type": "string", "required": false}}', 'FAQ'),
('FAQ', '常见问题解答', '["如何", "怎么", "帮助", "使用"]', NULL, 100, 0.5000, 1, '{}', 'QUERY_METADATA,QUERY_QUALITY,QUERY_LINEAGE');

-- =============================================
-- 初始化测试会话数据
-- =============================================

INSERT INTO `chat_session` (`session_title`, `user_id`, `username`, `status`, `session_type`, `message_count`) VALUES
('数据质量查询', 1, 'admin', 'active', 'qa', 3),
('资产血缘分析', 1, 'admin', 'active', 'analysis', 2),
('报表生成会话', 2, 'user001', 'closed', 'task', 5);

-- =============================================
-- 初始化测试消息数据
-- =============================================

INSERT INTO `chat_message` (`session_id`, `role`, `content`, `message_type`, `intent_type`, `intent_confidence`, `message_time`) VALUES
(1, 'user', '查询客户表的质量评分', 'text', 'QUERY_QUALITY', 0.8500, NOW()),
(1, 'assistant', '客户表的质量评分：\n\n综合评分：88分（良好）\n- 完整性：95%%\n- 准确性：85%%\n- 一致性：82%%\n- 时效性：90%%', 'text', 'QUERY_QUALITY', 0.8500, NOW()),
(1, 'user', '生成质量报告', 'text', 'GENERATE_REPORT', 0.9000, NOW()),
(1, 'assistant', '正在为您生成质量报告，请稍候...', 'text', 'GENERATE_REPORT', 0.9000, NOW()),
(2, 'user', '查看订单表的血缘关系', 'text', 'QUERY_LINEAGE', 0.9200, NOW()),
(2, 'assistant', '订单表的数据血缘：\n\n上游数据源：\n- source_db.order_raw\n- etl.order_cleaning\n\n下游数据：\n- dw.dwd_orders_detail\n- report.order_summary', 'text', 'QUERY_LINEAGE', 0.9200, NOW());
