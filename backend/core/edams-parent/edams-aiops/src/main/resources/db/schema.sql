-- =============================================
-- EDAMS AIOps 智能运维服务数据库脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS edams_aiops DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE edams_aiops;

-- =============================================
-- 1. 监控指标表
-- =============================================
CREATE TABLE IF NOT EXISTS `monitor_metric` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `metric_name` VARCHAR(100) NOT NULL COMMENT '指标名称',
    `metric_type` VARCHAR(50) NOT NULL COMMENT '指标类型：cpu, memory, disk, network, custom',
    `target_id` VARCHAR(100) NOT NULL COMMENT '服务/系统标识',
    `target_name` VARCHAR(200) COMMENT '目标名称',
    `metric_value` DECIMAL(20,4) NOT NULL COMMENT '指标值',
    `metric_unit` VARCHAR(20) COMMENT '指标单位',
    `collect_time` DATETIME NOT NULL COMMENT '采集时间',
    `interval_seconds` INT DEFAULT 60 COMMENT '采集间隔（秒）',
    `min_value` DECIMAL(20,4) COMMENT '最小值',
    `max_value` DECIMAL(20,4) COMMENT '最大值',
    `avg_value` DECIMAL(20,4) COMMENT '平均值',
    `tags` JSON COMMENT '标签(JSON)',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `created_by` VARCHAR(64) COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(64) COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    INDEX `idx_target_id` (`target_id`),
    INDEX `idx_metric_name` (`metric_name`),
    INDEX `idx_collect_time` (`collect_time`),
    INDEX `idx_metric_type` (`metric_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监控指标表';

-- =============================================
-- 2. 告警表
-- =============================================
CREATE TABLE IF NOT EXISTS `aiop_alert` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `alert_title` VARCHAR(200) NOT NULL COMMENT '告警标题',
    `alert_description` TEXT COMMENT '告警描述',
    `alert_level` VARCHAR(20) NOT NULL COMMENT '告警级别：critical, warning, info',
    `alert_status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '告警状态：pending, acknowledged, resolved, closed',
    `target_id` VARCHAR(100) COMMENT '关联的服务/系统ID',
    `target_name` VARCHAR(200) COMMENT '关联的目标名称',
    `trigger_condition` VARCHAR(500) COMMENT '触发条件',
    `current_value` VARCHAR(200) COMMENT '当前值',
    `threshold` VARCHAR(200) COMMENT '告警阈值',
    `alert_time` DATETIME NOT NULL COMMENT '告警时间',
    `ack_time` DATETIME COMMENT '确认时间',
    `ack_by` VARCHAR(64) COMMENT '确认人',
    `resolve_time` DATETIME COMMENT '解决时间',
    `resolve_by` VARCHAR(64) COMMENT '解决人',
    `solution` TEXT COMMENT '解决方案',
    `rule_id` BIGINT COMMENT '关联规则ID',
    `notification_sent` TINYINT DEFAULT 0 COMMENT '通知状态',
    `notification_time` DATETIME COMMENT '通知时间',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `created_by` VARCHAR(64) COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(64) COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    INDEX `idx_alert_level` (`alert_level`),
    INDEX `idx_alert_status` (`alert_status`),
    INDEX `idx_target_id` (`target_id`),
    INDEX `idx_alert_time` (`alert_time`),
    INDEX `idx_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警表';

-- =============================================
-- 3. 异常记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `anomaly_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `anomaly_type` VARCHAR(50) NOT NULL COMMENT '异常类型：spike, dip, trend_change, pattern, outlier',
    `severity` VARCHAR(20) NOT NULL COMMENT '异常级别：high, medium, low',
    `metric_id` BIGINT COMMENT '关联指标ID',
    `metric_name` VARCHAR(100) COMMENT '指标名称',
    `target_id` VARCHAR(100) COMMENT '目标ID',
    `target_name` VARCHAR(200) COMMENT '目标名称',
    `description` TEXT COMMENT '异常描述',
    `detect_time` DATETIME NOT NULL COMMENT '检测时间',
    `start_time` DATETIME COMMENT '异常开始时间',
    `end_time` DATETIME COMMENT '异常结束时间',
    `duration_minutes` INT COMMENT '持续时间（分钟）',
    `anomaly_value` DECIMAL(20,4) COMMENT '异常值',
    `expected_value` DECIMAL(20,4) COMMENT '期望值',
    `deviation_percent` DECIMAL(10,4) COMMENT '偏差百分比',
    `algorithm` VARCHAR(100) COMMENT '检测算法',
    `confidence` DECIMAL(5,4) COMMENT '置信度',
    `status` VARCHAR(20) DEFAULT 'detected' COMMENT '处理状态：detected, investigating, resolved, ignored',
    `alert_id` BIGINT COMMENT '关联告警ID',
    `analysis_result` TEXT COMMENT '分析结果',
    `suggestion` TEXT COMMENT '建议措施',
    `features` JSON COMMENT '特征数据(JSON)',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `created_by` VARCHAR(64) COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(64) COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    INDEX `idx_severity` (`severity`),
    INDEX `idx_status` (`status`),
    INDEX `idx_target_id` (`target_id`),
    INDEX `idx_detect_time` (`detect_time`),
    INDEX `idx_metric_id` (`metric_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异常记录表';

-- =============================================
-- 4. 运维任务表
-- =============================================
CREATE TABLE IF NOT EXISTS `ops_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_title` VARCHAR(200) NOT NULL COMMENT '任务标题',
    `description` TEXT COMMENT '任务描述',
    `task_type` VARCHAR(50) NOT NULL COMMENT '任务类型：maintenance, deployment, backup, recovery, scaling, inspection',
    `task_status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '任务状态：pending, running, completed, failed, cancelled',
    `priority` VARCHAR(20) DEFAULT 'medium' COMMENT '优先级：low, medium, high, critical',
    `target_id` VARCHAR(100) COMMENT '目标系统ID',
    `target_name` VARCHAR(200) COMMENT '目标系统名称',
    `execution_mode` VARCHAR(20) DEFAULT 'manual' COMMENT '执行方式：manual, automatic, scheduled',
    `executor` VARCHAR(64) COMMENT '执行人',
    `planned_start_time` DATETIME COMMENT '计划开始时间',
    `planned_end_time` DATETIME COMMENT '计划结束时间',
    `actual_start_time` DATETIME COMMENT '实际开始时间',
    `actual_end_time` DATETIME COMMENT '实际结束时间',
    `task_params` JSON COMMENT '任务参数(JSON)',
    `script` TEXT COMMENT '执行脚本',
    `result` TEXT COMMENT '执行结果',
    `output_log` TEXT COMMENT '输出日志',
    `error_message` TEXT COMMENT '错误信息',
    `progress_percent` INT DEFAULT 0 COMMENT '进度百分比',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `max_retries` INT DEFAULT 3 COMMENT '最大重试次数',
    `alert_id` BIGINT COMMENT '关联告警ID',
    `trigger_source` VARCHAR(20) DEFAULT 'manual' COMMENT '触发来源：manual, alert, schedule, api',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `created_by` VARCHAR(64) COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(64) COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    INDEX `idx_task_status` (`task_status`),
    INDEX `idx_task_type` (`task_type`),
    INDEX `idx_target_id` (`target_id`),
    INDEX `idx_planned_start` (`planned_start_time`),
    INDEX `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运维任务表';

-- =============================================
-- 5. 告警规则表
-- =============================================
CREATE TABLE IF NOT EXISTS `alert_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `description` TEXT COMMENT '规则描述',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型：threshold, trend, pattern, anomaly',
    `metric_name` VARCHAR(100) NOT NULL COMMENT '指标名称',
    `target_type` VARCHAR(50) COMMENT '目标类型：service, system, component',
    `target_id` VARCHAR(100) COMMENT '目标ID',
    `condition_expr` VARCHAR(500) COMMENT '条件表达式',
    `threshold` VARCHAR(200) COMMENT '阈值',
    `operator` VARCHAR(10) COMMENT '比较操作符：gt, lt, eq, gte, lte, neq',
    `alert_level` VARCHAR(20) NOT NULL COMMENT '告警级别：critical, warning, info',
    `duration_seconds` INT DEFAULT 0 COMMENT '持续时间（秒）',
    `evaluate_interval` INT DEFAULT 60 COMMENT '评估间隔（秒）',
    `enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
    `notify_enabled` TINYINT DEFAULT 1 COMMENT '是否发送通知',
    `notify_channels` VARCHAR(200) COMMENT '通知渠道：email, sms, webhook, dingtalk, wechat',
    `template_id` BIGINT COMMENT '通知模板ID',
    `aggregation_enabled` TINYINT DEFAULT 0 COMMENT '告警收敛',
    `aggregation_window` INT DEFAULT 5 COMMENT '收敛时间窗口（分钟）',
    `max_alerts` INT DEFAULT 100 COMMENT '最大告警次数',
    `silence_start` VARCHAR(10) COMMENT '沉默开始时间',
    `silence_end` VARCHAR(10) COMMENT '沉默结束时间',
    `action_ids` VARCHAR(500) COMMENT '关联的动作ID',
    `tags` JSON COMMENT '标签(JSON)',
    `tenant_id` BIGINT DEFAULT 1 COMMENT '租户ID',
    `created_by` VARCHAR(64) COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(64) COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    INDEX `idx_metric_name` (`metric_name`),
    INDEX `idx_target_id` (`target_id`),
    INDEX `idx_enabled` (`enabled`),
    INDEX `idx_alert_level` (`alert_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';

-- =============================================
-- 初始化测试数据
-- =============================================

-- 插入测试监控指标
INSERT INTO `monitor_metric` (`metric_name`, `metric_type`, `target_id`, `target_name`, `metric_value`, `metric_unit`, `collect_time`) VALUES
('cpu_usage', 'cpu', 'server-001', '应用服务器1', 45.50, '%', NOW()),
('memory_usage', 'memory', 'server-001', '应用服务器1', 68.30, '%', NOW()),
('disk_usage', 'disk', 'server-001', '应用服务器1', 55.20, '%', NOW()),
('response_time', 'custom', 'api-gateway', 'API网关', 125.00, 'ms', NOW());

-- 插入测试告警
INSERT INTO `aiop_alert` (`alert_title`, `alert_description`, `alert_level`, `alert_status`, `target_id`, `target_name`, `alert_time`) VALUES
('CPU使用率过高', 'CPU使用率超过80%阈值', 'warning', 'pending', 'server-001', '应用服务器1', NOW()),
('内存使用率告警', '内存使用率接近临界值', 'info', 'acknowledged', 'server-001', '应用服务器1', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('服务响应超时', 'API响应时间超过500ms', 'critical', 'resolved', 'api-gateway', 'API网关', DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- 插入测试运维任务
INSERT INTO `ops_task` (`task_title`, `description`, `task_type`, `task_status`, `priority`, `target_id`, `target_name`, `planned_start_time`) VALUES
('系统例行维护', '每周系统例行维护检查', 'maintenance', 'pending', 'medium', 'server-001', '应用服务器1', DATE_ADD(NOW(), INTERVAL 1 DAY)),
('数据备份任务', '每日数据备份', 'backup', 'completed', 'high', 'db-master', '主数据库', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('服务部署', '新版本服务部署', 'deployment', 'running', 'critical', 'server-002', '应用服务器2', NOW());
