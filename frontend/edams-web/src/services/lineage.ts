/**
 * 血缘服务 API
 */

import { http } from './request';
import { API_PATHS } from '../constants';
import type { LineageGraph, ImpactAnalysis } from '../types';

/**
 * 获取表级血缘
 */
export async function getTableLineage(
  assetId: string,
  params?: {
    direction?: 'UPSTREAM' | 'DOWNSTREAM' | 'BOTH';
    depth?: number;
    includeTasks?: boolean;
  },
): Promise<LineageGraph> {
  return http.get<LineageGraph>(API_PATHS.LINEAGE.TABLE(assetId), params);
}

/**
 * 获取字段级血缘
 */
export async function getFieldLineage(
  assetId: string,
  params?: {
    direction?: 'UPSTREAM' | 'DOWNSTREAM' | 'BOTH';
    depth?: number;
  },
): Promise<LineageGraph> {
  return http.get<LineageGraph>(API_PATHS.LINEAGE.FIELD(assetId), params);
}

/**
 * 查询血缘路径
 */
export async function getLineagePath(params: {
  sourceAssetId: string;
  targetAssetId: string;
  direction?: 'UPSTREAM' | 'DOWNSTREAM';
}): Promise<LineageGraph> {
  return http.get<LineageGraph>(API_PATHS.LINEAGE.PATH, params);
}

/**
 * 获取血缘图数据
 */
export async function getLineageGraph(params: {
  assetId: string;
  direction?: 'UPSTREAM' | 'DOWNSTREAM' | 'BOTH';
  depth?: number;
  layout?: 'LR' | 'TB' | 'CIRCLE';
}): Promise<LineageGraph> {
  return http.get<LineageGraph>(API_PATHS.LINEAGE.GRAPH, params);
}

/**
 * 影响分析
 */
export async function getImpactAnalysis(
  assetId: string,
  params?: {
    depth?: number;
    includeTasks?: boolean;
    includeReports?: boolean;
  },
): Promise<ImpactAnalysis> {
  return http.get<ImpactAnalysis>(API_PATHS.LINEAGE.IMPACT(assetId), params);
}

/**
 * 追溯分析
 */
export async function getDependencyAnalysis(
  assetId: string,
  params?: {
    depth?: number;
    includeTasks?: boolean;
  },
): Promise<ImpactAnalysis> {
  return http.get<ImpactAnalysis>(API_PATHS.LINEAGE.DEPENDENCY(assetId), params);
}

/**
 * 手动添加血缘关系
 */
export async function createLineage(data: {
  sourceAssetId: string;
  sourceFieldId?: string;
  targetAssetId: string;
  targetFieldId?: string;
  lineageType?: 'ETL' | 'SQL' | 'MANUAL';
  transformDesc?: string;
  transformSql?: string;
}): Promise<void> {
  return http.post<void>(API_PATHS.LINEAGE.CREATE, data);
}

/**
 * 删除血缘关系
 */
export async function deleteLineage(lineageId: string): Promise<void> {
  return http.delete<void>(API_PATHS.LINEAGE.DELETE(lineageId));
}

/**
 * 验证血缘关系
 */
export async function verifyLineage(data: {
  assetId: string;
  verifyType?: 'TABLE' | 'FIELD';
  expectedUpstreams?: string[];
  expectedDownstreams?: string[];
}): Promise<{
  valid: boolean;
  missingUpstreams?: string[];
  missingDownstreams?: string[];
  extraUpstreams?: string[];
  extraDownstreams?: string[];
}> {
  return http.post<any>(API_PATHS.LINEAGE.VERIFY, data);
}

/**
 * 获取血缘统计
 */
export async function getLineageStatistics(): Promise<{
  totalNodes: number;
  totalEdges: number;
  coverageRate: number;
  byType: Record<string, number>;
}> {
  return http.get<any>(API_PATHS.LINEAGE.STATISTICS);
}

/**
 * 获取血缘变更历史
 */
export async function getLineageHistory(
  assetId: string,
  params?: {
    page?: number;
    pageSize?: number;
  },
): Promise<{
  list: Array<{
    id: string;
    changeType: string;
    changedFields: string[];
    beforeValue: any;
    afterValue: any;
    operator: string;
    operateTime: string;
  }>;
  total: number;
}> {
  return http.get<any>(API_PATHS.LINEAGE.HISTORY(assetId), params);
}

/**
 * 对比血缘差异
 */
export async function compareLineage(params: {
  assetId: string;
  version1: string;
  version2: string;
}): Promise<{
  added: any[];
  removed: any[];
  modified: any[];
}> {
  return http.get<any>(API_PATHS.LINEAGE.COMPARE, params);
}

export default {
  getTableLineage,
  getFieldLineage,
  getLineagePath,
  getLineageGraph,
  getImpactAnalysis,
  getDependencyAnalysis,
  createLineage,
  deleteLineage,
  verifyLineage,
  getLineageStatistics,
  getLineageHistory,
  compareLineage,
};
