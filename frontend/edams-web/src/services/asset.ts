/**
 * 资产服务 API
 */

import { http } from './request';
import { API_PATHS } from '../constants';
import type {
  DataAsset,
  AssetQueryParams,
  PageResponse,
  PageParams,
  DataField,
  ExportRequest,
  RecentAsset,
} from '../types';

/**
 * 查询资产列表
 */
export async function getAssetList(
  params: AssetQueryParams,
): Promise<PageResponse<DataAsset>> {
  return http.get<PageResponse<DataAsset>>(API_PATHS.ASSET.LIST, params);
}

/**
 * 获取资产详情
 */
export async function getAssetDetail(id: string): Promise<DataAsset> {
  return http.get<DataAsset>(API_PATHS.ASSET.DETAIL(id));
}

/**
 * 创建资产
 */
export async function createAsset(data: Partial<DataAsset>): Promise<DataAsset> {
  return http.post<DataAsset>(API_PATHS.ASSET.CREATE, data);
}

/**
 * 更新资产
 */
export async function updateAsset(
  id: string,
  data: Partial<DataAsset>,
): Promise<DataAsset> {
  return http.put<DataAsset>(API_PATHS.ASSET.UPDATE(id), data);
}

/**
 * 删除资产
 */
export async function deleteAsset(
  id: string,
  params?: { deleteType?: 'SOFT' | 'HARD'; reason?: string },
): Promise<void> {
  return http.delete<void>(API_PATHS.ASSET.DELETE(id), params);
}

/**
 * 恢复已删除资产
 */
export async function restoreAsset(id: string): Promise<DataAsset> {
  return http.post<DataAsset>(API_PATHS.ASSET.RESTORE(id));
}

/**
 * 全文搜索资产
 */
export async function searchAssets(
  params: {
    q: string;
    filters?: string;
    highlight?: boolean;
    fuzzy?: boolean;
  } & PageParams,
): Promise<PageResponse<DataAsset>> {
  return http.get<PageResponse<DataAsset>>(API_PATHS.ASSET.SEARCH, params);
}

/**
 * 高级搜索
 */
export async function advancedSearch(
  params: {
    keyword?: string;
    conditions?: Array<{
      field: string;
      operator: string;
      value?: any;
      values?: any[];
    }>;
    sort?: Array<{ field: string; order: 'asc' | 'desc' }>;
  } & PageParams,
): Promise<PageResponse<DataAsset>> {
  return http.post<PageResponse<DataAsset>>(API_PATHS.ASSET.ADVANCED_SEARCH, params);
}

/**
 * 搜索联想
 */
export async function getAssetSuggestions(
  params: {
    q: string;
    limit?: number;
    type?: 'ALL' | 'ASSET' | 'FIELD' | 'TAG';
  },
): Promise<Array<{ type: string; id: string; name: string; assetType?: string }>> {
  return http.get<any>(API_PATHS.ASSET.SUGGEST, params);
}

/**
 * 智能推荐资产
 */
export async function getAssetRecommendations(
  params: {
    scene: 'SIMILAR' | 'HISTORY' | 'TREND';
    assetId?: string;
    limit?: number;
  },
): Promise<DataAsset[]> {
  return http.get<DataAsset[]>(API_PATHS.ASSET.RECOMMEND, params);
}

/**
 * 获取资产字段列表
 */
export async function getAssetFields(assetId: string): Promise<DataField[]> {
  return http.get<DataField[]>(API_PATHS.ASSET.FIELDS(assetId));
}

/**
 * 添加资产字段
 */
export async function addAssetField(
  assetId: string,
  data: Partial<DataField>,
): Promise<DataField> {
  return http.post<DataField>(API_PATHS.ASSET.ADD_FIELD(assetId), data);
}

/**
 * 更新资产字段
 */
export async function updateAssetField(
  assetId: string,
  fieldId: string,
  data: Partial<DataField>,
): Promise<DataField> {
  return http.put<DataField>(API_PATHS.ASSET.UPDATE_FIELD(assetId, fieldId), data);
}

/**
 * 删除资产字段
 */
export async function deleteAssetField(
  assetId: string,
  fieldId: string,
): Promise<void> {
  return http.delete<void>(API_PATHS.ASSET.DELETE_FIELD(assetId, fieldId));
}

/**
 * 收藏资产
 */
export async function favoriteAsset(assetId: string): Promise<void> {
  return http.post<void>(API_PATHS.ASSET.FAVORITE(assetId));
}

/**
 * 取消收藏资产
 */
export async function unfavoriteAsset(assetId: string): Promise<void> {
  return http.delete<void>(API_PATHS.ASSET.FAVORITE(assetId));
}

/**
 * 评价资产
 */
export async function rateAsset(
  assetId: string,
  data: {
    rating: number;
    comment?: string;
    tags?: string[];
  },
): Promise<void> {
  return http.post<void>(API_PATHS.ASSET.RATING(assetId), data);
}

/**
 * 获取资产评价列表
 */
export async function getAssetRatings(
  assetId: string,
  params?: PageParams,
): Promise<PageResponse<any>> {
  return http.get<PageResponse<any>>(API_PATHS.ASSET.RATINGS(assetId), params);
}

/**
 * 更新资产敏感等级
 */
export async function updateAssetSensitivity(
  assetId: string,
  data: {
    sensitivityLevel: string;
    changeReason?: string;
  },
): Promise<void> {
  return http.put<void>(API_PATHS.ASSET.SENSITIVITY(assetId), data);
}

/**
 * 废弃资产
 */
export async function deprecateAsset(
  assetId: string,
  data: {
    reason: string;
    replacementAssetId?: string;
    effectiveTime?: string;
    notifyUsers?: boolean;
  },
): Promise<void> {
  return http.post<void>(API_PATHS.ASSET.LIFECYCLE.DEPRECATE(assetId), data);
}

/**
 * 恢复废弃资产
 */
export async function restoreDeprecatedAsset(assetId: string): Promise<void> {
  return http.post<void>(API_PATHS.ASSET.LIFECYCLE.RESTORE(assetId));
}

/**
 * 归档资产
 */
export async function archiveAsset(
  assetId: string,
  data: {
    archiveType: 'COLD' | 'DELETE';
    archiveTime?: string;
  },
): Promise<void> {
  return http.post<void>(API_PATHS.ASSET.LIFECYCLE.ARCHIVE(assetId), data);
}

/**
 * 提交资产认证
 */
export async function submitCertification(
  assetId: string,
  data: {
    certificationLevel: string;
    certificationScope?: 'FULL' | 'PARTIAL';
    certifiedFields?: string[];
    certificationBasis?: string;
    contactPerson?: string;
    contactEmail?: string;
  },
): Promise<void> {
  return http.post<void>(API_PATHS.ASSET.CERTIFICATION.APPLY(assetId), data);
}

/**
 * 审批资产认证
 */
export async function approveCertification(
  assetId: string,
  data: {
    approvedLevel?: string;
    comment?: string;
    validUntil?: string;
  },
): Promise<void> {
  return http.post<void>(API_PATHS.ASSET.CERTIFICATION.APPROVE(assetId), data);
}

/**
 * 驳回资产认证
 */
export async function rejectCertification(
  assetId: string,
  data: {
    reason: string;
    suggestions?: string;
  },
): Promise<void> {
  return http.post<void>(API_PATHS.ASSET.CERTIFICATION.REJECT(assetId), data);
}

/**
 * 获取资产敏感字段
 */
export async function getSensitiveFields(
  assetId: string,
): Promise<Array<{ field: DataField; pattern: string }>> {
  return http.get<any>(API_PATHS.ASSET.SENSITIVE_FIELDS(assetId));
}

/**
 * 获取资产质量报告
 */
export async function getAssetQuality(assetId: string): Promise<any> {
  return http.get<any>(API_PATHS.ASSET.QUALITY(assetId));
}

/**
 * 获取质量趋势
 */
export async function getQualityTrend(
  assetId: string,
  params: {
    period?: 'DAY' | 'WEEK' | 'MONTH';
    startDate: string;
    endDate: string;
  },
): Promise<Array<{ date: string; score: number }>> {
  return http.get<any>(API_PATHS.ASSET.QUALITY_TREND(assetId), params);
}

/**
 * 获取最近访问资产
 */
export async function getRecentAssets(
  params?: { limit?: number },
): Promise<RecentAsset[]> {
  return http.get<RecentAsset[]>(API_PATHS.USER.ME_RECENT, params);
}

/**
 * 获取我的收藏资产
 */
export async function getMyFavorites(
  params?: PageParams,
): Promise<PageResponse<DataAsset>> {
  return http.get<PageResponse<DataAsset>>(API_PATHS.USER.ME_FAVORITES, params);
}

/**
 * 导出资产
 */
export async function exportAssets(
  params: ExportRequest & {
    assetIds?: string[];
    filters?: AssetQueryParams;
  },
): Promise<string> {
  return http.post<string>(`${API_PATHS.ASSET.LIST}/export`, params);
}

export default {
  getAssetList,
  getAssetDetail,
  createAsset,
  updateAsset,
  deleteAsset,
  restoreAsset,
  searchAssets,
  advancedSearch,
  getAssetSuggestions,
  getAssetRecommendations,
  getAssetFields,
  addAssetField,
  updateAssetField,
  deleteAssetField,
  favoriteAsset,
  unfavoriteAsset,
  rateAsset,
  getAssetRatings,
  updateAssetSensitivity,
  deprecateAsset,
  restoreDeprecatedAsset,
  archiveAsset,
  submitCertification,
  approveCertification,
  rejectCertification,
  getSensitiveFields,
  getAssetQuality,
  getQualityTrend,
  getRecentAssets,
  getMyFavorites,
  exportAssets,
};
