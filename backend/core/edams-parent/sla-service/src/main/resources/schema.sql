-- SLA监控管理数据库表结构
-- 创建时间: 2026-04-11

-- SLA协议表
CREATE TABLE IF NOT EXISTS sla_agreement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '协议名称',
    description VARCHAR(500) COMMENT '描述',
    service_name VARCHAR(100) NOT NULL COMMENT '服务名称',
    service_type VARCHAR(20) NOT NULL COMMENT '服务类型: API,DATABASE,SYSTEM',
    target_object VARCHAR(200) NOT NULL COMMENT '监控对象',
    metric_type VARCHAR(20) NOT NULL COMMENT '指标类型: RESPONSE_TIME,AVAILABILITY,ERROR_RATE',
    threshold_value DOUBLE NOT NULL COMMENT '阈值数值',
    threshold_unit VARCHAR(10) NOT NULL COMMENT '阈值单位: ms,%,count',
    warning_level DOUBLE NOT NULL COMMENT '警告级别',
    critical_level DOUBLE NOT NULL COMMENT '严重级别',
    owner_id BIGINT NOT NULL COMMENT '所有者ID',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE,INACTIVE',
    start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME COMMENT '更新时间',
    INDEX idx_service_name (service_name),
    INDEX idx_service_type (service_type),
    INDEX idx_owner_id (owner_id),
    INDEX idx_status (status),
    INDEX idx_metric_type (metric_type),
    INDEX idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SLA协议表';

-- SLA报告表
CREATE TABLE IF NOT EXISTS sla_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    agreement_id BIGINT NOT NULL COMMENT '协议ID',
    report_period VARCHAR(20) NOT NULL COMMENT '报告周期: DAILY,WEEKLY,MONTHLY',
    period_start DATETIME NOT NULL COMMENT '周期开始时间',
    period_end DATETIME NOT NULL COMMENT '周期结束时间',
    metric_value DOUBLE NOT NULL COMMENT '指标数值',
    compliance_rate DOUBLE NOT NULL COMMENT '合规率',
    violation_count INT NOT NULL COMMENT '违规次数',
    warning_count INT NOT NULL COMMENT '警告次数',
    critical_count INT NOT NULL COMMENT '严重次数',
    total_samples INT NOT NULL COMMENT '总样本数',
    avg_value DOUBLE NOT NULL COMMENT '平均值',
    max_value DOUBLE NOT NULL COMMENT '最大值',
    min_value DOUBLE NOT NULL COMMENT '最小值',
    analysis_result VARCHAR(20) NOT NULL COMMENT '分析结果: COMPLIANT,WARNING,CRITICAL',
    report_content TEXT COMMENT '报告内容',
    generated_by BIGINT NOT NULL COMMENT '生成用户ID',
    generated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    notification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '通知状态: PENDING,SENT',
    INDEX idx_agreement_id (agreement_id),
    INDEX idx_report_period (report_period),
    INDEX idx_analysis_result (analysis_result),
    INDEX idx_generated_time (generated_time),
    CONSTRAINT fk_report_agreement FOREIGN KEY (agreement_id) REFERENCES sla_agreement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SLA报告表';

-- SLA监控记录表（可选）
CREATE TABLE IF NOT EXISTS sla_monitoring_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    agreement_id BIGINT NOT NULL COMMENT '协议ID',
    metric_value DOUBLE NOT NULL COMMENT '指标数值',
    monitored_time DATETIME NOT NULL COMMENT '监控时间',
    status VARCHAR(20) NOT NULL COMMENT '状态: COMPLIANT,WARNING,VIOLATION,CRITICAL',
    analysis_result TEXT COMMENT '分析结果',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_agreement_id (agreement_id),
    INDEX idx_status (status),
    INDEX idx_monitored_time (monitored_time),
    CONSTRAINT fk_monitoring_agreement FOREIGN KEY (agreement_id) REFERENCES sla_agreement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SLA监控记录表';

-- SLA警报表（可选）
CREATE TABLE IF NOT EXISTS sla_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    agreement_id BIGINT NOT NULL COMMENT '协议ID',
    alert_type VARCHAR(20) NOT NULL COMMENT '警报类型: WARNING,VIOLATION,CRITICAL',
    alert_message TEXT NOT COMMENT '警报消息',
    alert_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '警报时间',
    handled_by BIGINT COMMENT '处理用户ID',
    handled_time DATETIME COMMENT '处理时间',
    handled_status VARCHAR(20) COMMENT '处理状态: PENDING,RESOLVED,IGNORED',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_agreement_id (agreement_id),
    INDEX idx_alert_type (alert_type),
    INDEX idx_handled_status (handled_status),
    CONSTRAINT fk_alert_agreement FOREIGN KEY (agreement_id) REFERENCES sla_agreement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SLA警报表';

-- 初始化数据
INSERT INTO sla_agreement (name, description, service_name, service_type, target_object, metric_type, threshold_value, threshold_unit, warning_level, critical_level, owner_id) VALUES
('API响应时间SLA', 'API接口响应时间监控协议', 'auth-service', 'API', '/api/auth/login', 'RESPONSE_TIME', 200, 'ms', 300, 500, 1),
('数据库可用性SLA', '数据库服务可用性监控协议', 'mysql-database', 'DATABASE', 'edams_core', 'AVAILABILITY', 99.9, '%', 99.5, 98.0, 1),
('系统错误率SLA', '系统错误率监控协议', 'gateway-service', 'SYSTEM', 'gateway-01', 'ERROR_RATE', 0.1, '%', 0.5, 1.0, 2);

INSERT INTO sla_report (agreement_id, report_period, period_start, period_end, metric_value, compliance_rate, violation_count, warning_count, critical_count, total_samples, avg_value, max_value, min_value, analysis_result, generated_by) VALUES
(1, 'DAILY', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 150, 0.98, 2, 5, 0, 1000, 150, 450, 50, 'COMPLIANT', 1),
(2, 'DAILY', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 99.8, 0.99, 1, 0, 0, 1000, 99.8, 100, 99.5, 'COMPLIANT', 1),
(3, 'DAILY', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 0.2, 0.95, 10, 20, 0, 1000, 0.2, 0.8, 0, 'WARNING', 1);