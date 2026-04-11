/**
 * 规则引擎服务
 */

import request from './request';

/**
 * 获取规则列表
 * @param params 查询参数 {category, status, page, size}
 */
export const getRules = (params?: {
  category?: string;
  status?: string;
  page?: number;
  size?: number;
}) => request.get('/rules/api/rules', { params });

/**
 * 获取规则详情
 * @param id 规则ID
 */
export const getRuleById = (id: number) =>
  request.get(`/rules/api/rules/${id}`);

/**
 * 创建规则
 * @param data 规则数据
 */
export const createRule = (data: {
  ruleName: string;
  ruleCode: string;
  category: string;
  description?: string;
  ruleContent?: string;
  ruleFilePath?: string;
  status?: string;
  priority?: number;
  triggerCondition?: string;
  parameters?: string;
}) => request.post('/rules/api/rules', data);

/**
 * 更新规则
 * @param id 规则ID
 * @param data 规则数据
 */
export const updateRule = (id: number, data: any) =>
  request.put(`/rules/api/rules/${id}`, data);

/**
 * 删除规则
 * @param id 规则ID
 */
export const deleteRule = (id: number) =>
  request.delete(`/rules/api/rules/${id}`);

/**
 * 切换规则状态
 * @param id 规则ID
 * @param status 状态
 */
export const toggleRuleStatus = (id: number, status: string) =>
  request.put(`/rules/api/rules/${id}/status`, null, { params: { status } });

/**
 * 重载规则
 */
export const reloadRules = () =>
  request.post('/rules/api/rules/reload');

/**
 * 获取规则统计信息
 */
export const getRuleStatistics = () =>
  request.get('/rules/api/rules/statistics');

/**
 * 获取规则分类列表
 */
export const getCategories = () =>
  request.get('/rules/api/rules/categories');

/**
 * 质量评分评估
 * @param data 评估数据
 */
export const evaluateQuality = (data: any) =>
  request.post('/rules/api/rules/evaluate/quality', data);

/**
 * 合规检查
 * @param data 检查数据
 */
export const checkCompliance = (data: any) =>
  request.post('/rules/api/rules/evaluate/compliance', data);

/**
 * 价值评估
 * @param data 评估数据
 */
export const evaluateValue = (data: any) =>
  request.post('/rules/api/rules/evaluate/value', data);

/**
 * 生命周期评估
 * @param data 评估数据
 */
export const evaluateLifecycle = (data: any) =>
  request.post('/rules/api/rules/evaluate/lifecycle', data);

/**
 * 治理评估
 * @param data 评估数据
 */
export const evaluateGovernance = (data: any) =>
  request.post('/rules/api/rules/evaluate/governance', data);

/**
 * 综合评估
 * @param data 评估数据
 */
export const evaluateAll = (data: any) =>
  request.post('/rules/api/rules/evaluate/all', data);

/**
 * 规则测试
 * @param data 测试数据 {category, input}
 */
export const testRule = (data: { category: string; input: any }) =>
  request.post('/rules/api/rules/evaluate/test', data);

/**
 * 获取执行日志
 * @param assetId 资产ID
 */
export const getExecutionLogs = (assetId?: string) =>
  request.get('/rules/api/rules/logs', { params: { assetId } });
