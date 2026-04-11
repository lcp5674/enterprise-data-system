/**
 * 数据治理服务
 */

import request from './request';

/**
 * 获取治理任务列表
 * @param params 查询参数 {status, type, page, size}
 */
export const getGovernanceTasks = (params?: {
  status?: string;
  type?: string;
  page?: number;
  size?: number;
}) => request.get('/governance/api/v1/tasks', { params });

/**
 * 创建治理任务
 * @param data 任务数据 {name, type, targetAssets, rules, schedule}
 */
export const createTask = (data: {
  name: string;
  type: string;
  targetAssets: string[];
  rules?: string[];
  schedule?: {
    type: 'once' | 'cron';
    cron?: string;
    executeTime?: string;
  };
  description?: string;
}) => request.post('/governance/api/v1/tasks', data);

/**
 * 更新治理任务
 * @param id 任务ID
 * @param data 任务数据
 */
export const updateTask = (id: string, data: any) =>
  request.put(`/governance/api/v1/tasks/${id}`, data);

/**
 * 删除治理任务
 * @param id 任务ID
 */
export const deleteTask = (id: string) =>
  request.delete(`/governance/api/v1/tasks/${id}`);

/**
 * 获取任务详情
 * @param id 任务ID
 */
export const getTaskDetail = (id: string) =>
  request.get(`/governance/api/v1/tasks/${id}`);

/**
 * 执行任务
 * @param id 任务ID
 */
export const executeTask = (id: string) =>
  request.post(`/governance/api/v1/tasks/${id}/execute`);

/**
 * 停止任务
 * @param id 任务ID
 */
export const stopTask = (id: string) =>
  request.post(`/governance/api/v1/tasks/${id}/stop`);

/**
 * 获取治理报告
 * @param params 查询参数 {startDate, endDate, type}
 */
export const getGovernanceReport = (params?: {
  startDate?: string;
  endDate?: string;
  type?: 'summary' | 'detail' | 'trend';
}) => request.get('/governance/api/v1/reports', { params });

/**
 * 获取AI治理建议
 * @param params 查询参数 {assetType, domain, limit}
 */
export const getAiRecommendations = (params?: {
  assetType?: string;
  domain?: string;
  limit?: number;
}) => request.get('/governance/api/v1/ai/recommendations', { params });

/**
 * 应用AI建议
 * @param recommendationId 建议ID
 */
export const applyAiRecommendation = (recommendationId: string) =>
  request.post(`/governance/api/v1/ai/recommendations/${recommendationId}/apply`);

/**
 * 忽略AI建议
 * @param recommendationId 建议ID
 * @param reason 忽略原因
 */
export const dismissAiRecommendation = (recommendationId: string, reason: string) =>
  request.post(`/governance/api/v1/ai/recommendations/${recommendationId}/dismiss`, { reason });

/**
 * 获取治理规则列表
 * @param params 查询参数 {category, status}
 */
export const getGovernanceRules = (params?: { category?: string; status?: string }) =>
  request.get('/governance/api/v1/rules', { params });

/**
 * 创建治理规则
 * @param data 规则数据
 */
export const createGovernanceRule = (data: any) =>
  request.post('/governance/api/v1/rules', data);

/**
 * 获取治理指标
 * @param params 查询参数 {metricType, timeRange}
 */
export const getGovernanceMetrics = (params?: { metricType?: string; timeRange?: string }) =>
  request.get('/governance/api/v1/metrics', { params });
