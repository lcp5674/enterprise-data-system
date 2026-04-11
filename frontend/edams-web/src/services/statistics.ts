/**
 * 统计服务 API
 */

import { http } from './request';
import type { DashboardStats, AssetDistribution, QualityTrend } from '../types';

/**
 * 获取仪表盘统计数据
 */
export async function getDashboardStats(): Promise<DashboardStats> {
  return http.get<DashboardStats>('/api/v1/statistics/overview');
}

/**
 * 获取资产总览统计
 */
export async function getAssetStatistics(): Promise<{
  totalAssets: number;
  todayAssets: number;
  weekAssets: number;
  monthAssets: number;
  byType: AssetDistribution[];
  byDomain: AssetDistribution[];
  byLevel: AssetDistribution[];
  byStatus: AssetDistribution[];
}> {
  return http.get<any>('/api/v1/statistics/overview');
}

/**
 * 获取资产趋势统计
 */
export async function getAssetTrend(params: {
  period?: 'DAY' | 'WEEK' | 'MONTH';
  startDate: string;
  endDate: string;
  groupBy?: 'TYPE' | 'DOMAIN' | 'LEVEL' | 'STATUS';
}): Promise<{
  trend: Array<{
    date: string;
    count: number;
    groupValue?: string;
  }>;
  summary: {
    total: number;
    increase: number;
    increaseRate: number;
  };
}> {
  return http.get<any>('/api/v1/statistics/trend', params);
}

/**
 * 获取资产分布统计
 */
export async function getAssetDistribution(params?: {
  dimension?: 'TYPE' | 'DOMAIN' | 'LEVEL' | 'STATUS' | 'OWNER' | 'DEPARTMENT';
}): Promise<AssetDistribution[]> {
  return http.get<AssetDistribution[]>('/api/v1/statistics/distribution', params);
}

/**
 * 获取质量统计
 */
export async function getQualityStatistics(): Promise<{
  overallScore: number;
  grade: string;
  trend: number;
  categoryScores: Record<string, number>;
  issueCounts: {
    total: number;
    critical: number;
    high: number;
    medium: number;
    low: number;
  };
  checkCounts: {
    total: number;
    passed: number;
    failed: number;
  };
}> {
  return http.get<any>('/api/v1/assets/statistics/quality');
}

/**
 * 获取质量趋势
 */
export async function getQualityTrendByPeriod(params: {
  period?: 'DAY' | 'WEEK' | 'MONTH';
  startDate: string;
  endDate: string;
}): Promise<QualityTrend[]> {
  return http.get<QualityTrend[]>('/api/v1/assets/statistics/quality/trend', params);
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
  return http.get<any>('/api/v1/lineage/statistics');
}

/**
 * 获取用户活跃统计
 */
export async function getUserActivityStats(params?: {
  period?: 'DAY' | 'WEEK' | 'MONTH';
  startDate?: string;
  endDate?: string;
}): Promise<{
  totalUsers: number;
  activeUsers: number;
  activeRate: number;
  trend: Array<{
    date: string;
    activeUsers: number;
    totalUsers: number;
  }>;
}> {
  return http.get<any>('/api/v1/statistics/users', params);
}

/**
 * 获取数据源统计
 */
export async function getDatasourceStats(): Promise<{
  total: number;
  byType: AssetDistribution[];
  byStatus: AssetDistribution[];
  topByAssets: Array<{
    id: string;
    name: string;
    type: string;
    assetCount: number;
  }>;
}> {
  return http.get<any>('/api/v1/datasources/statistics');
}

export default {
  getDashboardStats,
  getAssetStatistics,
  getAssetTrend,
  getAssetDistribution,
  getQualityStatistics,
  getQualityTrendByPeriod,
  getLineageStatistics,
  getUserActivityStats,
  getDatasourceStats,
};
