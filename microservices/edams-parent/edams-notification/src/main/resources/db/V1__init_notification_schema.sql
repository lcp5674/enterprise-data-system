-- ============================================
-- 通知服务数据库初始化脚本
-- ============================================

-- 创建通知模板表
CREATE TABLE IF NOT EXISTS notification_template (
    id VARCHAR(64) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '模板编码',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(20) NOT NULL COMMENT '模板类型: EMAIL=邮件, SMS=短信, IN_APP=站内消息, PUSH=推送',
    title VARCHAR(200) COMMENT '模板标题',
    content TEXT NOT NULL COMMENT '模板内容',
    variables TEXT COMMENT '模板变量(JSON格式)',
    description VARCHAR(500) COMMENT '模板描述',
    status INTEGER NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    created_by VARCHAR(64) COMMENT '创建者',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) COMMENT '更新者',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_by VARCHAR(64) COMMENT '删除者',
    deleted_time TIMESTAMP COMMENT '删除时间',
    is_deleted INTEGER NOT NULL DEFAULT 0 COMMENT '是否删除'
);

-- 创建通知消息表
CREATE TABLE IF NOT EXISTS notification_message (
    id VARCHAR(64) PRIMARY KEY,
    message_type VARCHAR(20) NOT NULL COMMENT '消息类型: EMAIL=邮件, SMS=短信, IN_APP=站内消息, PUSH=推送',
    user_id VARCHAR(64) COMMENT '接收者用户ID',
    email VARCHAR(100) COMMENT '接收者邮箱',
    phone VARCHAR(20) COMMENT '接收者手机号',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT NOT NULL COMMENT '消息内容',
    business_type VARCHAR(50) COMMENT '关联的业务类型',
    business_id VARCHAR(64) COMMENT '关联的业务ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '消息状态: PENDING=待发送, SENDING=发送中, SENT=已发送, FAILED=发送失败, READ=已读',
    send_time TIMESTAMP COMMENT '发送时间',
    read_time TIMESTAMP COMMENT '阅读时间',
    error_message VARCHAR(500) COMMENT '发送失败原因',
    retry_count INTEGER NOT NULL DEFAULT 0 COMMENT '重试次数',
    created_by VARCHAR(64) COMMENT '创建者',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) COMMENT '更新者',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建通知订阅表
CREATE TABLE IF NOT EXISTS notification_subscription (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    notification_channel VARCHAR(20) NOT NULL COMMENT '通知方式: EMAIL=邮件, SMS=短信, IN_APP=站内消息, PUSH=推送',
    enabled INTEGER NOT NULL DEFAULT 1 COMMENT '是否启用: 0=禁用, 1=启用',
    created_by VARCHAR(64) COMMENT '创建者',
    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) COMMENT '更新者',
    updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE(user_id, event_type, notification_channel)
);

-- 创建索引
CREATE INDEX idx_template_code ON notification_template(code);
CREATE INDEX idx_template_type ON notification_template(template_type);
CREATE INDEX idx_template_status ON notification_template(status);
CREATE INDEX idx_message_user ON notification_message(user_id);
CREATE INDEX idx_message_type ON notification_message(message_type);
CREATE INDEX idx_message_status ON notification_message(status);
CREATE INDEX idx_message_business ON notification_message(business_type, business_id);
CREATE INDEX idx_subscription_user ON notification_subscription(user_id);
CREATE INDEX idx_subscription_event ON notification_subscription(event_type);

-- ============================================
-- 初始化通知模板
-- ============================================

-- 用户注册欢迎邮件
INSERT INTO notification_template (id, code, name, template_type, title, content, variables, description, status, created_by, created_time, updated_by, updated_time)
VALUES (
    'tmpl-welcome-email',
    'welcome_email',
    '用户注册欢迎邮件',
    'EMAIL',
    '欢迎加入EDAMS企业数据资产管理系统',
    '亲爱的 ${username}：

欢迎加入EDAMS企业数据资产管理系统！

您的账号信息如下：
- 用户名：${username}
- 注册邮箱：${email}
- 注册时间：${registerTime}

系统地址：${systemUrl}

如有任何问题，请联系系统管理员。

祝您使用愉快！

EDAMS管理团队',
    '{"username": "用户名", "email": "邮箱", "registerTime": "注册时间", "systemUrl": "系统地址"}',
    '新用户注册时发送的欢迎邮件',
    1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
);

-- 密码重置邮件
INSERT INTO notification_template (id, code, name, template_type, title, content, variables, description, status, created_by, created_time, updated_by, updated_time)
VALUES (
    'tmpl-password-reset',
    'password_reset',
    '密码重置邮件',
    'EMAIL',
    'EDAMS密码重置验证码',
    '亲爱的 ${username}：

您正在申请重置密码，验证码为：${verifyCode}

验证码有效期：${expireMinutes}分钟

如果不是您本人操作，请忽略此邮件。

EDAMS管理团队',
    '{"username": "用户名", "verifyCode": "验证码", "expireMinutes": "有效期"}',
    '用户申请密码重置时发送的验证码邮件',
    1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
);

-- 资产审批通知
INSERT INTO notification_template (id, code, name, template_type, title, content, variables, description, status, created_by, created_time, updated_by, updated_time)
VALUES (
    'tmpl-asset-approval',
    'asset_approval',
    '资产审批通知',
    'IN_APP',
    '资产审批${status}',
    '${operator} 提交了资产 "${assetName}" 的 ${operation} 申请，请您审批。

申请时间：${applyTime}
资产类型：${assetType}

请登录系统进行审批处理。',
    '{"status": "状态", "operator": "操作人", "assetName": "资产名称", "operation": "操作类型", "applyTime": "申请时间", "assetType": "资产类型"}',
    '资产审批流程通知',
    1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
);

-- 系统公告
INSERT INTO notification_template (id, code, name, template_type, title, content, variables, description, status, created_by, created_time, updated_by, updated_time)
VALUES (
    'tmpl-system-announcement',
    'system_announcement',
    '系统公告',
    'IN_APP',
    '【系统公告】${title}',
    '${content}

发布时间：${publishTime}

${additionalInfo}

如有问题，请联系系统管理员。',
    '{"title": "公告标题", "content": "公告内容", "publishTime": "发布时间", "additionalInfo": "附加信息"}',
    '系统公告通知模板',
    1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
);

-- 资产变更通知
INSERT INTO notification_template (id, code, name, template_type, title, content, variables, description, status, created_by, created_time, updated_by, updated_time)
VALUES (
    'tmpl-asset-change',
    'asset_change',
    '资产变更通知',
    'IN_APP',
    '资产 "${assetName}" 发生变更',
    '您的资产 "${assetName}" 发生了以下变更：

变更类型：${changeType}
变更字段：${changeFields}
变更前：${beforeValue}
变更后：${afterValue}
变更时间：${changeTime}
变更人：${operator}

如有任何疑问，请联系相关人员。',
    '{"assetName": "资产名称", "changeType": "变更类型", "changeFields": "变更字段", "beforeValue": "变更前", "afterValue": "变更后", "changeTime": "变更时间", "operator": "操作人"}',
    '资产变更通知模板',
    1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
);

-- 数据同步完成通知
INSERT INTO notification_template (id, code, name, template_type, title, content, variables, description, status, created_by, created_time, updated_by, updated_time)
VALUES (
    'tmpl-sync-complete',
    'sync_complete',
    '数据同步完成通知',
    'IN_APP',
    '数据同步任务已完成',
    '数据同步任务已完成！

任务名称：${taskName}
数据源：${source}
目标库：${target}
同步记录数：${recordCount}
开始时间：${startTime}
完成时间：${endTime}
执行状态：${status}

详情请查看同步日志。',
    '{"taskName": "任务名称", "source": "数据源", "target": "目标库", "recordCount": "记录数", "startTime": "开始时间", "endTime": "完成时间", "status": "状态"}',
    '数据同步任务完成通知',
    1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
);

-- ============================================
-- 初始化订阅配置
-- ============================================

-- 默认订阅：系统公告
INSERT INTO notification_subscription (id, user_id, event_type, notification_channel, enabled, created_by, created_time)
VALUES ('sub-default-announcement', 'default', 'SYSTEM_ANNOUNCEMENT', 'IN_APP', 1, 'system', CURRENT_TIMESTAMP);

-- 默认订阅：资产变更
INSERT INTO notification_subscription (id, user_id, event_type, notification_channel, enabled, created_by, created_time)
VALUES ('sub-default-asset-change', 'default', 'ASSET_CHANGE', 'IN_APP', 1, 'system', CURRENT_TIMESTAMP);
