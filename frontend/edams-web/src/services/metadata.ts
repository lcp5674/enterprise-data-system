/**
 * 元数据服务
 */

import request from './request';

/**
 * 搜索元数据
 * @param params 搜索参数 {keyword, type, domain, page, size}
 */
export const searchMetadata = (params?: {
  keyword?: string;
  type?: string;
  domain?: string;
  page?: number;
  size?: number;
  tags?: string[];
}) => request.get('/metadata/api/v1/search', { params });

/**
 * 获取元数据详情
 * @param objectId 对象ID
 */
export const getMetadataDetail = (objectId: string) =>
  request.get(`/metadata/api/v1/objects/${objectId}`);

/**
 * 注册元数据
 * @param data 元数据对象
 */
export const registerMetadata = (data: {
  name: string;
  type: string;
  domain: string;
  description?: string;
  schema?: Record<string, any>;
  properties?: Record<string, any>;
  tags?: string[];
  owner?: string;
}) => request.post('/metadata/api/v1/objects', data);

/**
 * 更新元数据
 * @param objectId 对象ID
 * @param data 元数据对象
 */
export const updateMetadata = (objectId: string, data: any) =>
  request.put(`/metadata/api/v1/objects/${objectId}`, data);

/**
 * 删除元数据
 * @param objectId 对象ID
 */
export const deleteMetadata = (objectId: string) =>
  request.delete(`/metadata/api/v1/objects/${objectId}`);

/**
 * 获取元数据统计
 * @param params 统计参数 {type, domain}
 */
export const getMetadataStats = (params?: { type?: string; domain?: string }) =>
  request.get('/metadata/api/v1/stats', { params });

/**
 * 按领域获取元数据
 * @param domainCode 领域编码
 * @param params 分页参数 {page, size}
 */
export const getMetadataByDomain = (
  domainCode: string,
  params?: { page?: number; size?: number }
) => request.get(`/metadata/api/v1/domains/${domainCode}/objects`, { params });

/**
 * 获取领域列表
 */
export const getDomains = () => request.get('/metadata/api/v1/domains');

/**
 * 获取对象类型列表
 */
export const getObjectTypes = () => request.get('/metadata/api/v1/types');

/**
 * 获取元数据变更历史
 * @param objectId 对象ID
 * @param params 分页参数 {page, size}
 */
export const getMetadataHistory = (
  objectId: string,
  params?: { page?: number; size?: number }
) => request.get(`/metadata/api/v1/objects/${objectId}/history`, { params });

/**
 * 获取元数据血缘
 * @param objectId 对象ID
 * @param params 参数 {direction, depth}
 */
export const getMetadataLineage = (
  objectId: string,
  params?: { direction?: 'upstream' | 'downstream' | 'both'; depth?: number }
) => request.get(`/metadata/api/v1/objects/${objectId}/lineage`, { params });

/**
 * 添加元数据标签
 * @param objectId 对象ID
 * @param tags 标签列表
 */
export const addMetadataTags = (objectId: string, tags: string[]) =>
  request.post(`/metadata/api/v1/objects/${objectId}/tags`, { tags });

/**
 * 删除元数据标签
 * @param objectId 对象ID
 * @param tag 标签
 */
export const removeMetadataTag = (objectId: string, tag: string) =>
  request.delete(`/metadata/api/v1/objects/${objectId}/tags/${encodeURIComponent(tag)}`);

/**
 * 导出元数据
 * @param objectIds 对象ID列表（为空则导出全部）
 * @param format 导出格式 {json, csv, excel}
 */
export const exportMetadata = (
  objectIds?: string[],
  format: 'json' | 'csv' | 'excel' = 'json'
) =>
  request.post(
    '/metadata/api/v1/export',
    { objectIds, format },
    { responseType: 'blob' }
  );

/**
 * 导入元数据
 * @param file 文件
 * @param options 导入选项 {skipValidation, updateExisting}
 */
export const importMetadata = (
  file: File,
  options?: { skipValidation?: boolean; updateExisting?: boolean }
) => {
  const formData = new FormData();
  formData.append('file', file);
  if (options) {
    formData.append('options', JSON.stringify(options));
  }
  return request.post('/metadata/api/v1/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/**
 * 获取元数据模板
 * @param type 对象类型
 */
export const getMetadataTemplate = (type: string) =>
  request.get(`/metadata/api/v1/templates/${type}`);

/**
 * 获取热门搜索
 * @param limit 数量限制
 */
export const getPopularSearches = (limit: number = 10) =>
  request.get('/metadata/api/v1/search/popular', { params: { limit } });

/**
 * 获取搜索建议
 * @param keyword 关键词
 * @param limit 数量限制
 */
export const getSearchSuggestions = (keyword: string, limit: number = 10) =>
  request.get('/metadata/api/v1/search/suggestions', { params: { keyword, limit } });
