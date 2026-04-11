/**
 * 知识图谱服务
 */

import request from './request';

/**
 * 获取知识图谱数据
 * @param params 查询参数 {domain, nodeType, limit}
 */
export const getGraph = (params?: { domain?: string; nodeType?: string; limit?: number }) =>
  request.get('/knowledge/api/v1/graph', { params });

/**
 * 创建节点
 * @param data 节点数据 {name, type, properties, domain}
 */
export const createNode = (data: {
  name: string;
  type: string;
  properties?: Record<string, any>;
  domain?: string;
}) => request.post('/knowledge/api/v1/nodes', data);

/**
 * 创建关系
 * @param data 关系数据 {sourceId, targetId, relationType, properties}
 */
export const createRelation = (data: {
  sourceId: string;
  targetId: string;
  relationType: string;
  properties?: Record<string, any>;
}) => request.post('/knowledge/api/v1/relations', data);

/**
 * 删除节点
 * @param id 节点ID
 */
export const deleteNode = (id: string) =>
  request.delete(`/knowledge/api/v1/nodes/${id}`);

/**
 * 删除关系
 * @param id 关系ID
 */
export const deleteRelation = (id: string) =>
  request.delete(`/knowledge/api/v1/relations/${id}`);

/**
 * 搜索节点
 * @param keyword 关键词
 * @param params 其他参数 {type, domain, limit}
 */
export const searchNodes = (keyword: string, params?: { type?: string; domain?: string; limit?: number }) =>
  request.get('/knowledge/api/v1/nodes/search', { params: { keyword, ...params } });

/**
 * 获取节点详情
 * @param id 节点ID
 */
export const getNodeDetail = (id: string) =>
  request.get(`/knowledge/api/v1/nodes/${id}`);

/**
 * 获取节点的相关节点
 * @param id 节点ID
 * @param params 参数 {relationType, direction, limit}
 */
export const getRelatedNodes = (
  id: string,
  params?: { relationType?: string; direction?: 'in' | 'out' | 'both'; limit?: number }
) => request.get(`/knowledge/api/v1/nodes/${id}/related`, { params });

/**
 * 获取路径分析
 * @param sourceId 源节点ID
 * @param targetId 目标节点ID
 * @param params 参数 {maxDepth, relationTypes}
 */
export const getPathAnalysis = (
  sourceId: string,
  targetId: string,
  params?: { maxDepth?: number; relationTypes?: string[] }
) =>
  request.get('/knowledge/api/v1/analysis/path', {
    params: { sourceId, targetId, ...params },
  });

/**
 * 获取血缘分析
 * @param nodeId 节点ID
 * @param params 参数 {direction, maxDepth}
 */
export const getLineageAnalysis = (
  nodeId: string,
  params?: { direction?: 'upstream' | 'downstream' | 'both'; maxDepth?: number }
) => request.get(`/knowledge/api/v1/analysis/lineage/${nodeId}`, { params });

/**
 * 获取影响分析
 * @param nodeId 节点ID
 * @param params 参数 {maxDepth}
 */
export const getImpactAnalysis = (nodeId: string, params?: { maxDepth?: number }) =>
  request.get(`/knowledge/api/v1/analysis/impact/${nodeId}`, { params });
