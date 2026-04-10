-- 流程定义表
CREATE TABLE IF NOT EXISTS wf_process_definition (
    id VARCHAR(64) PRIMARY KEY,
    process_key VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    version INT DEFAULT 1,
    bpmn_xml TEXT,
    diagram_svg TEXT,
    status INT DEFAULT 0 COMMENT '0-草稿，1-已发布，2-已停用',
    is_latest BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(64),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_wf_process_key ON wf_process_definition(process_key);
CREATE INDEX idx_wf_process_status ON wf_process_definition(status);
CREATE INDEX idx_wf_process_latest ON wf_process_definition(is_latest);

-- 流程实例表
CREATE TABLE IF NOT EXISTS wf_process_instance (
    id VARCHAR(64) PRIMARY KEY,
    flowable_instance_id VARCHAR(100),
    process_definition_id VARCHAR(64),
    business_type VARCHAR(100),
    business_id VARCHAR(100),
    business_title VARCHAR(500),
    starter_id VARCHAR(64),
    starter_name VARCHAR(100),
    starter_dept_id VARCHAR(64),
    starter_dept_name VARCHAR(200),
    current_node_id VARCHAR(100),
    current_node_name VARCHAR(200),
    current_assignee_id VARCHAR(64),
    current_assignee_name VARCHAR(100),
    status INT DEFAULT 0 COMMENT '0-运行中，1-已完成，2-已终止，3-已挂起',
    priority INT DEFAULT 2 COMMENT '1-低，2-中，3-高，4-紧急',
    form_data TEXT,
    variables TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration BIGINT,
    result INT COMMENT '0-通过，1-拒绝，2-撤回',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_wf_instance_flowable ON wf_process_instance(flowable_instance_id);
CREATE INDEX idx_wf_instance_business ON wf_process_instance(business_id);
CREATE INDEX idx_wf_instance_starter ON wf_process_instance(starter_id);
CREATE INDEX idx_wf_instance_status ON wf_process_instance(status);

-- 流程任务表
CREATE TABLE IF NOT EXISTS wf_process_task (
    id VARCHAR(64) PRIMARY KEY,
    flowable_task_id VARCHAR(100),
    process_instance_id VARCHAR(64),
    process_definition_id VARCHAR(64),
    task_node_id VARCHAR(100),
    task_node_name VARCHAR(200),
    task_type INT DEFAULT 1 COMMENT '1-审批，2-抄送，3-会签',
    assignee_id VARCHAR(64),
    assignee_name VARCHAR(100),
    candidate_ids TEXT,
    candidate_names TEXT,
    status INT DEFAULT 0 COMMENT '0-待处理，1-已处理，2-已转办，3-已委托',
    result INT COMMENT '0-通过，1-拒绝，2-退回，3-转办，4-委托',
    comment TEXT,
    form_data TEXT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_time TIMESTAMP,
    handle_time TIMESTAMP,
    reminder_count INT DEFAULT 0,
    last_reminder_time TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_wf_task_flowable ON wf_process_task(flowable_task_id);
CREATE INDEX idx_wf_task_instance ON wf_process_task(process_instance_id);
CREATE INDEX idx_wf_task_assignee ON wf_process_task(assignee_id);
CREATE INDEX idx_wf_task_status ON wf_process_task(status);

-- 流程历史记录表
CREATE TABLE IF NOT EXISTS wf_process_history (
    id VARCHAR(64) PRIMARY KEY,
    process_instance_id VARCHAR(64),
    process_task_id VARCHAR(64),
    process_definition_id VARCHAR(64),
    node_id VARCHAR(100),
    node_name VARCHAR(200),
    node_type INT COMMENT '1-开始节点，2-任务节点，3-网关，4-结束节点',
    operator_type INT DEFAULT 1 COMMENT '1-用户，2-系统',
    operator_id VARCHAR(64),
    operator_name VARCHAR(100),
    operation_type INT COMMENT '1-发起，2-审批，3-转办，4-委托，5-退回，6-终止，7-撤回',
    operation_result INT COMMENT '0-通过，1-拒绝，2-退回，3-转办，4-委托，5-终止',
    comment TEXT,
    form_data TEXT,
    variables TEXT,
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration BIGINT,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_wf_history_instance ON wf_process_history(process_instance_id);
CREATE INDEX idx_wf_history_node ON wf_process_history(node_id);
CREATE INDEX idx_wf_history_time ON wf_process_history(operation_time);

-- 审批规则表
CREATE TABLE IF NOT EXISTS wf_approval_rule (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(100) NOT NULL,
    business_type VARCHAR(100),
    process_definition_id VARCHAR(64),
    conditions TEXT,
    approver_config TEXT,
    approval_mode INT DEFAULT 1 COMMENT '1-或签，2-会签，3-顺序签',
    sign_pass_rate INT DEFAULT 100,
    timeout_hours INT,
    timeout_action INT COMMENT '1-自动通过，2-自动拒绝，3-提醒上级',
    allow_transfer BOOLEAN DEFAULT TRUE,
    allow_delegate BOOLEAN DEFAULT TRUE,
    allow_back BOOLEAN DEFAULT TRUE,
    status INT DEFAULT 1 COMMENT '0-禁用，1-启用',
    priority INT DEFAULT 0,
    created_by VARCHAR(64),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_wf_rule_code ON wf_approval_rule(code);
CREATE INDEX idx_wf_rule_business ON wf_approval_rule(business_type);
CREATE INDEX idx_wf_rule_status ON wf_approval_rule(status);
