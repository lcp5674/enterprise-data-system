/**
 * 数据标准服务
 */

import request from './request';

/**
 * 获取标准列表
 * @param params 查询参数
 */
export const getStandards = (params: any) => request.get('/standard/api/v1/standards', { params });

/**
 * 创建标准
 * @param data 标准数据
 */
export const createStandard = (data: any) => request.post('/standard/api/v1/standards', data);

/**
 * 更新标准
 * @param id 标准ID
 * @param data 标准数据
 */
export const updateStandard = (id: string, data: any) =>
  request.put(`/standard/api/v1/standards/${id}`, data);

/**
 * 删除标准
 * @param id 标准ID
 */
export const deleteStandard = (id: string) => request.delete(`/standard/api/v1/standards/${id}`);

/**
 * 发布标准
 * @param id 标准ID
 */
export const publishStandard = (id: string) =>
  request.post(`/standard/api/v1/standards/${id}/publish`);

/**
 * 获取标准详情
 * @param id 标准ID
 */
export const getStandardDetail = (id: string) =>
  request.get(`/standard/api/v1/standards/${id}`);

/**
 * 获取标准分类树
 */
export const getStandardCategories = () =>
  request.get('/standard/api/v1/categories');

/**
 * 导入标准
 * @param file 文件
 */
export const importStandards = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/standard/api/v1/standards/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/**
 * 导出标准
 * @param ids 标准ID列表
 */
export const exportStandards = (ids?: string[]) =>
  request.post('/standard/api/v1/standards/export', { ids }, {
    responseType: 'blob',
  });
