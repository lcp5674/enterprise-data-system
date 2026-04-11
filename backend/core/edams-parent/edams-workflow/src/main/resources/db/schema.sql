-- EDAMS审批流服务 - 工作流相关表
USE edams;

-- ==================== 工作流定义表 ====================
DROP TABLE IF EXISTS `wf_definition`;
CREATE TABLE `wf_definition` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '流程名称',
    `code` VARCHAR(50) NOT NULL COMMENT '流程编码（唯一）',
    `type` TINYINT DEFAULT 1 COMMENT '类型：1-资产审批，2-数据申请，3-权限申请，4-通用',
    `description` VARCHAR(500) DEFAULT NULL,
    `process_def_key` VARCHAR(100) DEFAULT NULL COMMENT 'Flowable流程定义Key',
    `process_def_id` VARCHAR(100) DEFAULT NULL COMMENT 'Flowable流程定义ID',
    `version` INT DEFAULT 1,
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-已禁用，3-归档',
    `bpmn_content` LONGTEXT DEFAULT NULL COMMENT 'BPMN XML内容',
    `form_config` TEXT DEFAULT NULL COMMENT '表单配置JSON',
    `allow_cancel` TINYINT DEFAULT 1 COMMENT '是否允许撤销：0-否，1-是',
    `allow_add_signee` TINYINT DEFAULT 0 COMMENT '是否允许加签',
    `allow_delegate` TINYINT DEFAULT 1 COMMENT '是否允许转办',
    `timeout_hours` INT DEFAULT 72 COMMENT '默认超时时间（小时）',
    `sort_order` INT DEFAULT 0,
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    `version_opt` INT DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流定义表';

-- ==================== 工作流实例表 ====================
DROP TABLE IF EXISTS `wf_instance`;
CREATE TABLE `wf_instance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `definition_id` BIGINT NOT NULL COMMENT '流程定义ID',
    `process_instance_id` VARCHAR(100) DEFAULT NULL COMMENT 'Flowable实例ID',
    `process_def_key` VARCHAR(100) DEFAULT NULL,
    `business_title` VARCHAR(200) NOT NULL COMMENT '业务标题',
    `business_type` TINYINT DEFAULT NULL COMMENT '业务类型',
    `business_key` VARCHAR(64) NOT NULL COMMENT '业务关联键',
    `initiator_id` BIGINT NOT NULL COMMENT '发起人ID',
    `initiator_name` VARCHAR(50) DEFAULT NULL,
    `initiator_dept` VARCHAR(100) DEFAULT NULL,
    `current_node_name` VARCHAR(100) DEFAULT NULL COMMENT '当前节点名',
    `current_assignees` VARCHAR(500) DEFAULT NULL COMMENT '当前处理人列表JSON',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-运行中，1-已完成，2-撤销，3-驳回，4-超时',
    `priority` TINYINT DEFAULT 1 COMMENT '优先级：1-普通，2-紧急，3-特急',
    `form_data` LONGTEXT DEFAULT NULL COMMENT '业务表单数据JSON',
    `completed_time` DATETIME DEFAULT NULL,
    `duration_ms` BIGINT DEFAULT NULL COMMENT '总耗时毫秒',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_def_id` (`definition_id`),
    KEY `idx_initiator_id` (`initiator_id`),
    KEY `idx_status` (`status`),
    KEY `idx_business_key` (`business_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流实例表';

-- ==================== 审批任务表 ====================
DROP TABLE IF EXISTS `wf_task`;
CREATE TABLE `wf_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `instance_id` BIGINT NOT NULL COMMENT '流程实例ID',
    `flowable_task_id` VARCHAR(100) DEFAULT NULL COMMENT 'Flowable任务ID',
    `task_name` VARCHAR(100) NOT NULL COMMENT '任务/节点名称',
    `task_def_key` VARCHAR(100) DEFAULT NULL,
    `assignee_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
    `assignee_name` VARCHAR(50) DEFAULT NULL COMMENT '处理人姓名',
    `candidate_groups` VARCHAR(500) DEFAULT NULL COMMENT '候选组JSON',
    `candidate_users` VARCHAR(500) DEFAULT NULL COMMENT '候选人JSON',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-待处理，1-已处理，2-委托，3-转办，4-撤回',
    `result` TINYINT DEFAULT NULL COMMENT '结果：1-同意，2-拒绝，3-驳回，4-撤回',
    `comment` VARCHAR(2000) DEFAULT NULL COMMENT '审批意见',
    `attachments` VARCHAR(500) DEFAULT NULL COMMENT '附件路径JSON',
    `completed_time` DATETIME DEFAULT NULL,
    `due_date` DATETIME DEFAULT NULL COMMENT '到期时间',
    `is_timeout` TINYINT DEFAULT 0 COMMENT '是否超时',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_instance_id` (`instance_id`),
    KEY `idx_assignee_id` (`assignee_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批任务表';

-- ==================== 初始数据：预置流程定义 ====================
INSERT INTO `wf_definition` (name, code, type, description, status, allow_cancel, allow_delegate)
VALUES
('数据资产申请', 'DATA_ASSET_APPLY', 1, '申请新增或修改数据资产的审批流程', 1, 1, 1),
('数据权限申请', 'DATA_PERMISSION_APPLY', 2, '申请访问特定数据资源的权限', 1, 1, 1),
('元数据变更审批', 'METADATA_CHANGE_APPROVE', 1, '对核心元数据的变更需要审批', 1, 1, 1),
('质量规则变更', 'QUALITY_RULE_CHANGE', 1, '修改数据质量规则的审批流程', 1, 1, 1),
('通用审批流程', 'GENERAL_APPROVAL', 4, '通用型审批流程模板，可自定义配置', 0, 1, 1);
