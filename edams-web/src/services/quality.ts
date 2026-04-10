/**
 * 质量服务 API
 */

import { http } from './request';
import { API_PATHS } from '../constants';
import type { QualityRule, QualityCheckResult, QualityIssue, PageResponse, PageParams } from '../types';

/**
 * 获取质量规则列表
 */
export async function getQualityRules(
  params?: {
    ruleType?: string;
    targetType?: 'TABLE' | 'FIELD';
    severity?: string;
    status?: number;
    keyword?: string;
  } & PageParams,
): Promise<PageResponse<QualityRule>> {
  return http.get<PageResponse<QualityRule>>(API_PATHS.QUALITY.RULES.LIST, params);
}

/**
 * 获取质量规则详情
 */
export async function getQualityRuleDetail(id: string): Promise<QualityRule> {
  return http.get<QualityRule>(API_PATHS.QUALITY.RULES.DETAIL(id));
}

/**
 * 创建质量规则
 */
export async function createQualityRule(data: Partial<QualityRule>): Promise<QualityRule> {
  return http.post<QualityRule>(API_PATHS.QUALITY.RULES.CREATE, data);
}

/**
 * 更新质量规则
 */
export async function updateQualityRule(
  id: string,
  data: Partial<QualityRule>,
): Promise<QualityRule> {
  return http.put<QualityRule>(API_PATHS.QUALITY.RULES.UPDATE(id), data);
}

/**
 * 删除质量规则
 */
export async function deleteQualityRule(id: string): Promise<void> {
  return http.delete<void>(API_PATHS.QUALITY.RULES.DELETE(id));
}

/**
 * 启用质量规则
 */
export async function enableQualityRule(id: string): Promise<void> {
  return http.post<void>(API_PATHS.QUALITY.RULES.ENABLE(id));
}

/**
 * 禁用质量规则
 */
export async function disableQualityRule(id: string): Promise<void> {
  return http.post<void>(API_PATHS.QUALITY.RULES.DISABLE(id));
}

/**
 * 获取规则模板列表
 */
export async function getRuleTemplates(): Promise<QualityRule[]> {
  return http.get<QualityRule[]>(API_PATHS.QUALITY.RULES.TEMPLATES);
}

/**
 * 触发质量检测
 */
export async function triggerQualityCheck(params: {
  assetId: string;
  ruleIds?: string[];
  triggerType?: 'MANUAL' | 'SCHEDULED' | 'API';
  async?: boolean;
}): Promise<QualityCheckResult | { checkId: string }> {
  return http.post<any>(API_PATHS.QUALITY.CHECK.TRIGGER, params);
}

/**
 * 批量触发质量检测
 */
export async function batchTriggerQualityCheck(params: {
  assetIds: string[];
  ruleIds?: string[];
  triggerType?: 'MANUAL' | 'SCHEDULED';
}): Promise<{ taskId: string; total: number }> {
  return http.post<any>(API_PATHS.QUALITY.CHECK.BATCH, params);
}

/**
 * 获取检测结果
 */
export async function getCheckResult(checkId: string): Promise<QualityCheckResult> {
  return http.get<QualityCheckResult>(API_PATHS.QUALITY.CHECK.RESULT(checkId));
}

/**
 * 获取检测进度
 */
export async function getCheckProgress(
  checkId: string,
): Promise<{
  status: string;
  progress: number;
  currentStep?: string;
  message?: string;
}> {
  return http.get<any>(API_PATHS.QUALITY.CHECK.PROGRESS(checkId));
}

/**
 * 获取质量概览
 */
export async function getQualityOverview(): Promise<{
  overallScore: number;
  trend: number;
  grade: string;
  categoryScores: Record<string, number>;
  issueSummary: {
    total: number;
    critical: number;
    high: number;
    medium: number;
    low: number;
  };
  recentChecks: QualityCheckResult[];
}> {
  return http.get<any>(`/api/v1/quality/overview`);
}

/**
 * 查询质量问题列表
 */
export async function getQualityIssues(
  params?: {
    assetId?: string;
    severity?: string;
    status?: string;
    assigneeId?: string;
    startTime?: string;
    endTime?: string;
  } & PageParams,
): Promise<PageResponse<QualityIssue>> {
  return http.get<PageResponse<QualityIssue>>(API_PATHS.QUALITY.ISSUES.LIST, params);
}

/**
 * 获取质量问题详情
 */
export async function getQualityIssueDetail(id: string): Promise<QualityIssue> {
  return http.get<QualityIssue>(API_PATHS.QUALITY.ISSUES.DETAIL(id));
}

/**
 * 更新质量问题
 */
export async function updateQualityIssue(
  id: string,
  data: {
    status?: string;
    assigneeId?: string;
    dueTime?: string;
    comment?: string;
  },
): Promise<QualityIssue> {
  return http.put<QualityIssue>(API_PATHS.QUALITY.ISSUES.UPDATE(id), data);
}

/**
 * 解决问题
 */
export async function resolveQualityIssue(
  id: string,
  data: {
    resolution: string;
    rootCause?: string;
    fixMethod?: string;
    preventiveMeasure?: string;
    verifyCheckId?: string;
  },
): Promise<void> {
  return http.post<void>(API_PATHS.QUALITY.ISSUES.RESOLVE(id), data);
}

/**
 * 关闭问题
 */
export async function closeQualityIssue(id: string): Promise<void> {
  return http.post<void>(API_PATHS.QUALITY.ISSUES.CLOSE(id));
}

/**
 * 转移问题
 */
export async function transferQualityIssue(
  id: string,
  data: {
    assigneeId: string;
    reason?: string;
  },
): Promise<void> {
  return http.post<void>(API_PATHS.QUALITY.ISSUES.TRANSFER(id), data);
}

/**
 * 忽略问题
 */
export async function ignoreQualityIssue(
  id: string,
  data: {
    reason: string;
  },
): Promise<void> {
  return http.post<void>(API_PATHS.QUALITY.ISSUES.IGNORE(id), data);
}

/**
 * 获取问题统计
 */
export async function getIssueStatistics(): Promise<{
  total: number;
  byStatus: Record<string, number>;
  bySeverity: Record<string, number>;
  byCategory: Record<string, number>;
  trend: Array<{ date: string; count: number }>;
}> {
  return http.get<any>(API_PATHS.QUALITY.ISSUES.STATISTICS);
}

/**
 * 获取资产质量规则配置
 */
export async function getAssetQualityRules(assetId: string): Promise<any[]> {
  return http.get<any[]>(API_PATHS.ASSET.RULES(assetId));
}

/**
 * 为资产配置质量规则
 */
export async function configureAssetQualityRules(
  assetId: string,
  data: {
    rules: Array<{
      ruleId: string;
      fieldId?: string;
      scheduleType?: 'IMMEDIATE' | 'SCHEDULED';
      cronExpression?: string;
      enabled?: boolean;
      customParams?: Record<string, any>;
    }>;
  },
): Promise<void> {
  return http.post<void>(API_PATHS.ASSET.ADD_RULES(assetId), data);
}

/**
 * 移除资产质量规则
 */
export async function removeAssetQualityRule(
  assetId: string,
  ruleConfigId: string,
): Promise<void> {
  return http.delete<void>(API_PATHS.ASSET.REMOVE_RULES(assetId, ruleConfigId));
}

export default {
  getQualityRules,
  getQualityRuleDetail,
  createQualityRule,
  updateQualityRule,
  deleteQualityRule,
  enableQualityRule,
  disableQualityRule,
  getRuleTemplates,
  triggerQualityCheck,
  batchTriggerQualityCheck,
  getCheckResult,
  getCheckProgress,
  getQualityOverview,
  getQualityIssues,
  getQualityIssueDetail,
  updateQualityIssue,
  resolveQualityIssue,
  closeQualityIssue,
  transferQualityIssue,
  ignoreQualityIssue,
  getIssueStatistics,
  getAssetQualityRules,
  configureAssetQualityRules,
  removeAssetQualityRule,
};
