import request from './request';

/**
 * 血缘详情API服务
 *
 * 对接lineage-service血缘详情相关接口
 */
export const getLineageDetail = (assetId: string) =>
  request.get(`/lineage/api/v1/lineage/${assetId}/detail`);

export const getLineageGraph = (assetId: string, direction?: 'upstream' | 'downstream' | 'both') =>
  request.get(`/lineage/api/v1/lineage/${assetId}/graph`, { params: { direction } });

export const getImpactAnalysis = (assetId: string) =>
  request.get(`/lineage/api/v1/impact/${assetId}`);

export const getDataFlow = (startAssetId: string, endAssetId: string) =>
  request.get(`/lineage/api/v1/flow`, { params: { startAssetId, endAssetId } });

export const getColumnLineage = (tableId: string, columnName: string) =>
  request.get(`/lineage/api/v1/column/${tableId}/${columnName}`);

export const exportLineage = (assetId: string, format: 'json' | 'csv' | 'png') =>
  request.get(`/lineage/api/v1/lineage/${assetId}/export`, { params: { format }, responseType: 'blob' });
