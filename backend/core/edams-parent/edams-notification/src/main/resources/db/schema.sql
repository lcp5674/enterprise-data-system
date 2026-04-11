-- EDAMS通知服务 - 通知表和模板表
USE edams;

DROP TABLE IF EXISTS `sys_notification`;
CREATE TABLE `sys_notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT COMMENT '通知内容',
    `type` TINYINT DEFAULT 1 COMMENT '类型：1-系统，2-审批，3-告警，4-任务',
    `channel` VARCHAR(20) NOT NULL COMMENT '渠道：email/sms/webhook/inapp等',
    `status` TINYINT DEFAULT 0 COMMENT '发送状态：0-待发送，1-已发送，2-失败',
    `receiver_id` BIGINT NOT NULL COMMENT '接收人ID',
    `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '接收人姓名',
    `target_address` VARCHAR(200) DEFAULT NULL COMMENT '目标地址（邮箱/手机/URL）',
    `template_id` BIGINT DEFAULT NULL COMMENT '使用的模板ID',
    `template_params` VARCHAR(1000) DEFAULT NULL COMMENT '模板参数JSON',
    `send_time` DATETIME DEFAULT NULL,
    `read_time` DATETIME DEFAULT NULL,
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `source_module` VARCHAR(50) DEFAULT NULL COMMENT '来源模块',
    `business_key` VARCHAR(100) DEFAULT NULL COMMENT '业务关联ID',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    `version` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

DROP TABLE IF EXISTS `sys_notification_template`;
CREATE TABLE `sys_notification_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `code` VARCHAR(50) NOT NULL COMMENT '模板编码（唯一）',
    `channel` VARCHAR(20) NOT NULL COMMENT '适用渠道',
    `type` TINYINT DEFAULT 1 COMMENT '类型',
    `subject` VARCHAR(200) DEFAULT NULL COMMENT '邮件主题',
    `content_template` TEXT COMMENT '内容模板（支持${var}变量）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `description` VARCHAR(200) DEFAULT NULL,
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    `version` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

INSERT INTO `sys_notification_template` (name, code, channel, type, subject, content_template)
VALUES
('系统公告', 'SYSTEM_NOTICE', 'inapp', 1, '${title}', '${content}'),
('审批通知', 'APPROVAL_NOTIFY', 'inapp', 2, '审批通知 - ${title}', '您好${username}，您有一条新的${type}审批任务需要处理。\n\n标题：${title}\n发起人：${initiator}\n时间：${time}'),
('审批结果', 'APPROVAL_RESULT', 'email', 2, '审批结果 - ${title}', '您好${username}，您提交的审批"${title}"已${result}。\n\n审批人：${approver}\n意见：${comment}\n时间：${time}'),
('质量告警', 'QUALITY_ALERT', 'sms', 3, '[EDAMS]数据质量异常', '${ruleName}检测到异常：${detail}。请及时处理！'),
('密码重置验证码', 'RESET_PWD_CODE', 'sms', 1, '[EDAMS]验证码', '您的验证码为：${code}，有效期${expireMinutes}分钟。如非本人操作请忽略。');
