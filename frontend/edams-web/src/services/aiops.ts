/**
 * AI运维服务 API
 */

import { http } from './request';

/**
 * 告警项
 */
export interface Alert {
  id: number;
  alertLevel: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  alertStatus: 'PENDING' | 'ACKNOWLEDGED' | 'RESOLVED' | 'CLOSED';
  alertTitle: string;
  alertContent: string;
  targetId?: string;
  targetName?: string;
  alertTime: string;
  ackTime?: string;
  ackBy?: string;
  resolveTime?: string;
  resolveBy?: string;
  solution?: string;
}

/**
 * 告警规则
 */
export interface AlertRule {
  id: number;
  name: string;
  targetId: string;
  metricName: string;
  condition: string;
  threshold: number;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  enabled: boolean;
  alertTemplate?: string;
}

/**
 * 异常记录
 */
export interface AnomalyRecord {
  id: number;
  anomalyType: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  description: string;
  detectedTime: string;
  status: 'DETECTED' | 'INVESTIGATING' | 'RESOLVED';
  suggestion?: string;
}

/**
 * 获取告警列表（分页）
 */
export async function getAlertPage(params: {
  pageNum?: number;
  pageSize?: number;
  alertLevel?: string;
  alertStatus?: string;
  targetId?: string;
}): Promise<{ success: boolean; data: { records: Alert[]; total: number } }> {
  return http.get('/api/v1/aiops/alert/page', { params });
}

/**
 * 获取待处理告警
 */
export async function getPendingAlerts(): Promise<{ success: boolean; data: Alert[] }> {
  return http.get('/api/v1/aiops/alert/pending');
}

/**
 * 获取活跃告警
 */
export async function getActiveAlerts(targetId?: string): Promise<{ success: boolean; data: Alert[] }> {
  return http.get('/api/v1/aiops/alert/active', { params: { targetId } });
}

/**
 * 获取告警详情
 */
export async function getAlertById(id: number): Promise<{ success: boolean; data: Alert }> {
  return http.get(`/api/v1/aiops/alert/${id}`);
}

/**
 * 确认告警
 */
export async function acknowledgeAlert(id: number, ackBy: string): Promise<{ success: boolean }> {
  return http.post(`/api/v1/aiops/alert/${id}/acknowledge`, null, { params: { ackBy } });
}

/**
 * 解决告警
 */
export async function resolveAlert(
  id: number,
  resolveBy: string,
  solution?: string
): Promise<{ success: boolean }> {
  return http.post(`/api/v1/aiops/alert/${id}/resolve`, null, { params: { resolveBy, solution } });
}

/**
 * 关闭告警
 */
export async function closeAlert(id: number, closedBy: string): Promise<{ success: boolean }> {
  return http.post(`/api/v1/aiops/alert/${id}/close`, null, { params: { closedBy } });
}

/**
 * 按级别统计告警
 */
export async function countAlertsByLevel(): Promise<{ success: boolean; data: Array<{ level: string; count: number }> }> {
  return http.get('/api/v1/aiops/alert/stats/level');
}

/**
 * 获取告警规则列表
 */
export async function getAlertRules(params?: {
  targetId?: string;
  metricName?: string;
  enabled?: boolean;
}): Promise<{ success: boolean; data: AlertRule[] }> {
  return http.get('/api/v1/aiops/alert/rule', { params });
}

/**
 * 创建告警规则
 */
export async function createAlertRule(rule: Partial<AlertRule>): Promise<{ success: boolean; data: AlertRule }> {
  return http.post('/api/v1/aiops/alert/rule', rule);
}

/**
 * 更新告警规则
 */
export async function updateAlertRule(id: number, rule: Partial<AlertRule>): Promise<{ success: boolean; data: AlertRule }> {
  return http.put(`/api/v1/aiops/alert/rule/${id}`, rule);
}

/**
 * 删除告警规则
 */
export async function deleteAlertRule(id: number): Promise<{ success: boolean }> {
  return http.delete(`/api/v1/aiops/alert/rule/${id}`);
}

/**
 * 启用/禁用告警规则
 */
export async function toggleAlertRule(id: number, enabled: boolean): Promise<{ success: boolean }> {
  return http.post(`/api/v1/aiops/alert/rule/${id}/toggle`, null, { params: { enabled } });
}

/**
 * 获取异常记录列表
 */
export async function getAnomalyRecords(params?: {
  pageNum?: number;
  pageSize?: number;
  severity?: string;
  status?: string;
}): Promise<{ success: boolean; data: { records: AnomalyRecord[]; total: number } }> {
  return http.get('/api/v1/aiops/anomaly/page', { params });
}

/**
 * 获取异常详情
 */
export async function getAnomalyById(id: number): Promise<{ success: boolean; data: AnomalyRecord }> {
  return http.get(`/api/v1/aiops/anomaly/${id}`);
}

/**
 * 获取监控指标
 */
export async function getMonitorMetrics(params?: {
  targetId?: string;
  metricName?: string;
  startTime?: string;
  endTime?: string;
}): Promise<{ success: boolean; data: any[] }> {
  return http.get('/api/v1/aiops/monitor/metrics', { params });
}

export default {
  getAlertPage,
  getPendingAlerts,
  getActiveAlerts,
  getAlertById,
  acknowledgeAlert,
  resolveAlert,
  closeAlert,
  countAlertsByLevel,
  getAlertRules,
  createAlertRule,
  updateAlertRule,
  deleteAlertRule,
  toggleAlertRule,
  getAnomalyRecords,
  getAnomalyById,
  getMonitorMetrics,
};
