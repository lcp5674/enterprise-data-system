/**
 * SLA服务
 */

import request from './request';

/**
 * 获取SLA列表
 * @param params 查询参数 {status, serviceName, page, size}
 */
export const getSlaList = (params?: {
  status?: string;
  serviceName?: string;
  page?: number;
  size?: number;
}) => request.get('/sla/api/v1/agreements', { params });

/**
 * 获取SLA详情
 * @param id SLA ID
 */
export const getSlaDetail = (id: string) =>
  request.get(`/sla/api/v1/agreements/${id}`);

/**
 * 创建SLA
 * @param data SLA数据 {name, targetService, sloTarget, description, metrics}
 */
export const createSla = (data: {
  name: string;
  targetService: string;
  sloTarget: string;
  description?: string;
  metrics?: Array<{
    name: string;
    threshold: number;
    unit: string;
  }>;
  alerts?: Array<{
    condition: string;
    notifyChannels: string[];
  }>;
}) => request.post('/sla/api/v1/agreements', data);

/**
 * 更新SLA
 * @param id SLA ID
 * @param data SLA数据
 */
export const updateSla = (id: string, data: any) =>
  request.put(`/sla/api/v1/agreements/${id}`, data);

/**
 * 删除SLA
 * @param id SLA ID
 */
export const deleteSla = (id: string) =>
  request.delete(`/sla/api/v1/agreements/${id}`);

/**
 * 获取SLA报告
 * @param id SLA ID
 * @param params 查询参数 {startDate, endDate, granularity}
 */
export const getSlaReport = (
  id: string,
  params?: { startDate?: string; endDate?: string; granularity?: 'hour' | 'day' | 'week' | 'month' }
) => request.get(`/sla/api/v1/agreements/${id}/report`, { params });

/**
 * 检查合规性
 * @param id SLA ID
 * @param params 查询参数 {checkTime, period}
 */
export const checkCompliance = (
  id: string,
  params?: { checkTime?: string; period?: 'current' | 'last_hour' | 'last_day' | 'last_week' | 'last_month' }
) => request.post(`/sla/api/v1/agreements/${id}/compliance-check`, null, { params });

/**
 * 获取SLA指标
 * @param id SLA ID
 * @param params 查询参数 {metricName, startDate, endDate}
 */
export const getSlaMetrics = (
  id: string,
  params?: { metricName?: string; startDate?: string; endDate?: string }
) => request.get(`/sla/api/v1/agreements/${id}/metrics`, { params });

/**
 * 获取SLA告警历史
 * @param id SLA ID
 * @param params 分页参数 {page, size}
 */
export const getSlaAlerts = (
  id: string,
  params?: { page?: number; size?: number }
) => request.get(`/sla/api/v1/agreements/${id}/alerts`, { params });

/**
 * 获取SLA趋势数据
 * @param id SLA ID
 * @param params 查询参数 {days}
 */
export const getSlaTrends = (
  id: string,
  params?: { days?: number }
) => request.get(`/sla/api/v1/agreements/${id}/trends`, { params });

/**
 * 导出SLA报告
 * @param id SLA ID
 * @param format 导出格式 {pdf, excel, csv}
 * @param params 查询参数 {startDate, endDate}
 */
export const exportSlaReport = (
  id: string,
  format: 'pdf' | 'excel' | 'csv' = 'pdf',
  params?: { startDate?: string; endDate?: string }
) =>
  request.get(`/sla/api/v1/agreements/${id}/report/export`, {
    params: { format, ...params },
    responseType: 'blob',
  });

/**
 * 获取服务列表
 */
export const getServices = () => request.get('/sla/api/v1/services');

/**
 * 获取SLA模板列表
 */
export const getSlaTemplates = () => request.get('/sla/api/v1/templates');

/**
 * 从模板创建SLA
 * @param templateId 模板ID
 * @param data SLA数据
 */
export const createSlaFromTemplate = (templateId: string, data: any) =>
  request.post(`/sla/api/v1/templates/${templateId}/create`, data);

/**
 * 获取SLA仪表盘数据
 * @param params 查询参数 {period}
 */
export const getSlaDashboard = (params?: { period?: 'today' | 'week' | 'month' | 'quarter' }) =>
  request.get('/sla/api/v1/dashboard', { params });

/**
 * 获取SLA违规记录
 * @param params 查询参数 {startDate, endDate, severity, page, size}
 */
export const getViolations = (params?: {
  startDate?: string;
  endDate?: string;
  severity?: 'high' | 'medium' | 'low';
  page?: number;
  size?: number;
}) => request.get('/sla/api/v1/violations', { params });
