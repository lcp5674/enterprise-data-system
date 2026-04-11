package com.enterprise.edams.aiops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.aiops.entity.Alert;
import com.enterprise.edams.aiops.entity.AlertRule;
import com.enterprise.edams.aiops.entity.MonitorMetric;
import com.enterprise.edams.aiops.repository.AlertMapper;
import com.enterprise.edams.aiops.repository.AlertRuleMapper;
import com.enterprise.edams.aiops.repository.MonitorMetricMapper;
import com.enterprise.edams.aiops.service.AlertService;
import com.enterprise.edams.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertMapper alertMapper;
    private final AlertRuleMapper alertRuleMapper;
    private final MonitorMetricMapper monitorMetricMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Alert createAlert(Alert alert) {
        if (alert.getAlertTime() == null) {
            alert.setAlertTime(LocalDateTime.now());
        }
        if (alert.getAlertStatus() == null) {
            alert.setAlertStatus("pending");
        }
        alert.setTenantId(alert.getTenantId() != null ? alert.getTenantId() : 1L);
        alertMapper.insert(alert);
        log.info("创建告警: {} - {}", alert.getAlertTitle(), alert.getAlertLevel());
        return alert;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Alert updateAlert(Alert alert) {
        Alert existing = alertMapper.selectById(alert.getId());
        if (existing == null) {
            throw new BusinessException("告警不存在");
        }
        alertMapper.updateById(alert);
        log.info("更新告警: {}", alert.getId());
        return alert;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAlert(Long id) {
        alertMapper.deleteById(id);
        log.info("删除告警: {}", id);
    }

    @Override
    public Alert getAlertById(Long id) {
        return alertMapper.selectById(id);
    }

    @Override
    public Page<Alert> pageAlerts(int pageNum, int pageSize, String alertLevel, String alertStatus, String targetId, LocalDateTime startTime, LocalDateTime endTime) {
        Page<Alert> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        
        if (alertLevel != null && !alertLevel.isEmpty()) {
            wrapper.eq(Alert::getAlertLevel, alertLevel);
        }
        if (alertStatus != null && !alertStatus.isEmpty()) {
            wrapper.eq(Alert::getAlertStatus, alertStatus);
        }
        if (targetId != null && !targetId.isEmpty()) {
            wrapper.eq(Alert::getTargetId, targetId);
        }
        if (startTime != null) {
            wrapper.ge(Alert::getAlertTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(Alert::getAlertTime, endTime);
        }
        
        wrapper.orderByDesc(Alert::getAlertTime);
        return alertMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acknowledgeAlert(Long id, String ackBy) {
        alertMapper.acknowledgeAlert(id, LocalDateTime.now(), ackBy, LocalDateTime.now());
        log.info("确认告警: {} by {}", id, ackBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveAlert(Long id, String resolveBy, String solution) {
        alertMapper.resolveAlert(id, LocalDateTime.now(), resolveBy, solution, LocalDateTime.now());
        log.info("解决告警: {} by {}", id, resolveBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeAlert(Long id, String closedBy) {
        Alert alert = new Alert();
        alert.setId(id);
        alert.setAlertStatus("closed");
        alert.setUpdatedBy(closedBy);
        alertMapper.updateById(alert);
        log.info("关闭告警: {} by {}", id, closedBy);
    }

    @Override
    public List<Alert> getPendingAlerts() {
        return alertMapper.findPendingAlerts();
    }

    @Override
    public List<Alert> getActiveAlerts(String targetId) {
        return alertMapper.findActiveByTargetId(targetId);
    }

    @Override
    public List<Map<String, Object>> countByLevel() {
        return alertMapper.countByLevel();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlertRule createAlertRule(AlertRule rule) {
        rule.setEnabled(rule.getEnabled() != null ? rule.getEnabled() : 1);
        rule.setTenantId(rule.getTenantId() != null ? rule.getTenantId() : 1L);
        alertRuleMapper.insert(rule);
        log.info("创建告警规则: {}", rule.getRuleName());
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlertRule updateAlertRule(AlertRule rule) {
        alertRuleMapper.updateById(rule);
        log.info("更新告警规则: {}", rule.getId());
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleRule(Long id, boolean enabled) {
        AlertRule rule = new AlertRule();
        rule.setId(id);
        rule.setEnabled(enabled ? 1 : 0);
        alertRuleMapper.updateById(rule);
        log.info("{}告警规则: {}", enabled ? "启用" : "禁用", id);
    }

    @Override
    public void evaluateRules() {
        List<MonitorMetric> metrics = monitorMetricMapper.findForAlertEvaluation(1000);
        for (MonitorMetric metric : metrics) {
            try {
                evaluateMetricAgainstRules(metric);
            } catch (Exception e) {
                log.error("评估指标 {} 时出错", metric.getId(), e);
            }
        }
    }

    private void evaluateMetricAgainstRules(MonitorMetric metric) {
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRule::getMetricName, metric.getMetricName());
        wrapper.eq(AlertRule::getEnabled, 1);
        List<AlertRule> rules = alertRuleMapper.selectList(wrapper);

        for (AlertRule rule : rules) {
            if (evaluateCondition(metric, rule)) {
                createAlertFromRule(metric, rule);
            }
        }
    }

    private boolean evaluateCondition(MonitorMetric metric, AlertRule rule) {
        if (metric.getMetricValue() == null) {
            return false;
        }
        BigDecimal value = metric.getMetricValue();
        BigDecimal threshold = new BigDecimal(rule.getThreshold());
        
        return switch (rule.getOperator()) {
            case "gt" -> value.compareTo(threshold) > 0;
            case "gte" -> value.compareTo(threshold) >= 0;
            case "lt" -> value.compareTo(threshold) < 0;
            case "lte" -> value.compareTo(threshold) <= 0;
            case "eq" -> value.compareTo(threshold) == 0;
            case "neq" -> value.compareTo(threshold) != 0;
            default -> false;
        };
    }

    private void createAlertFromRule(MonitorMetric metric, AlertRule rule) {
        Alert alert = new Alert();
        alert.setAlertTitle(rule.getRuleName() + " - " + metric.getMetricName());
        alert.setAlertDescription("触发条件: " + rule.getConditionExpr());
        alert.setAlertLevel(rule.getAlertLevel());
        alert.setAlertStatus("pending");
        alert.setTargetId(metric.getTargetId());
        alert.setTargetName(metric.getTargetName());
        alert.setTriggerCondition(rule.getConditionExpr());
        alert.setCurrentValue(metric.getMetricValue().toString());
        alert.setThreshold(rule.getThreshold());
        alert.setRuleId(rule.getId());
        alert.setTenantId(rule.getTenantId());
        createAlert(alert);
    }

    @Override
    public List<AlertRule> getAlertRules(String targetId, String metricName, Boolean enabled) {
        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        if (targetId != null) {
            wrapper.eq(AlertRule::getTargetId, targetId);
        }
        if (metricName != null) {
            wrapper.eq(AlertRule::getMetricName, metricName);
        }
        if (enabled != null) {
            wrapper.eq(AlertRule::getEnabled, enabled ? 1 : 0);
        }
        return alertRuleMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAlertRule(Long id) {
        alertRuleMapper.deleteById(id);
        log.info("删除告警规则: {}", id);
    }
}
