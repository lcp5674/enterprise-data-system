package com.enterprise.edams.aiops.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.aiops.entity.Alert;
import com.enterprise.edams.aiops.entity.AlertRule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface AlertService {

    /**
     * 创建告警
     */
    Alert createAlert(Alert alert);

    /**
     * 更新告警
     */
    Alert updateAlert(Alert alert);

    /**
     * 删除告警
     */
    void deleteAlert(Long id);

    /**
     * 根据ID查询告警
     */
    Alert getAlertById(Long id);

    /**
     * 分页查询告警
     */
    Page<Alert> pageAlerts(int pageNum, int pageSize, String alertLevel, String alertStatus, String targetId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 确认告警
     */
    void acknowledgeAlert(Long id, String ackBy);

    /**
     * 解决告警
     */
    void resolveAlert(Long id, String resolveBy, String solution);

    /**
     * 关闭告警
     */
    void closeAlert(Long id, String closedBy);

    /**
     * 查询待处理告警
     */
    List<Alert> getPendingAlerts();

    /**
     * 查询活跃告警
     */
    List<Alert> getActiveAlerts(String targetId);

    /**
     * 按级别统计告警
     */
    List<Map<String, Object>> countByLevel();

    /**
     * 创建告警规则
     */
    AlertRule createAlertRule(AlertRule rule);

    /**
     * 更新告警规则
     */
    AlertRule updateAlertRule(AlertRule rule);

    /**
     * 启用/禁用告警规则
     */
    void toggleRule(Long id, boolean enabled);

    /**
     * 评估告警规则
     */
    void evaluateRules();

    /**
     * 查询告警规则
     */
    List<AlertRule> getAlertRules(String targetId, String metricName, Boolean enabled);

    /**
     * 删除告警规则
     */
    void deleteAlertRule(Long id);
}
