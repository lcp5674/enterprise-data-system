/**
 * 数据沙箱服务 API
 */

import { http } from './request';

/**
 * 获取沙箱列表
 */
export async function getSandboxList(): Promise<{
  success: boolean;
  data: SandboxItem[];
}> {
  return http.get<any>('/api/v1/sandbox/list');
}

/**
 * 获取沙箱详情
 */
export async function getSandboxDetail(id: string): Promise<{
  success: boolean;
  data: SandboxItem;
}> {
  return http.get<any>(`/api/v1/sandbox/${id}`);
}

/**
 * 创建沙箱
 */
export async function createSandbox(data: {
  name: string;
  description?: string;
  spec: SandboxSpec;
}): Promise<{ success: boolean; data: { id: string } }> {
  return http.post<any>('/api/v1/sandbox/create', data);
}

/**
 * 启动沙箱
 */
export async function startSandbox(id: string): Promise<{ success: boolean }> {
  return http.post<any>(`/api/v1/sandbox/${id}/start`, {});
}

/**
 * 停止沙箱
 */
export async function stopSandbox(id: string): Promise<{ success: boolean }> {
  return http.post<any>(`/api/v1/sandbox/${id}/stop`, {});
}

/**
 * 删除沙箱
 */
export async function deleteSandbox(id: string): Promise<{ success: boolean }> {
  return http.delete<any>(`/api/v1/sandbox/${id}`);
}

/**
 * 沙箱项
 */
export interface SandboxItem {
  id: string;
  name: string;
  description?: string;
  status: 'RUNNING' | 'STOPPED' | 'CREATING' | 'FAILED';
  spec: SandboxSpec;
  expireTime: string;
  createdAt: string;
  accessUrl?: string;
}

/**
 * 沙箱规格
 */
export interface SandboxSpec {
  cpu: string;
  memory: string;
  disk: string;
  gpu?: string;
}

export default {
  getSandboxList,
  getSandboxDetail,
  createSandbox,
  startSandbox,
  stopSandbox,
  deleteSandbox,
};
