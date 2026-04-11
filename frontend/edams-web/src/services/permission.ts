/**
 * 权限服务 API
 */

import { http } from './request';
import { API_PATHS } from '../constants';
import type { PageResponse, PageParams } from '../types';

/**
 * 权限项
 */
export interface PermissionItem {
  id: string;
  code: string;
  name: string;
  type: string;
  module?: string;
  description?: string;
  status?: number;
}

/**
 * 获取权限列表
 */
export async function getPermissionList(
  params?: {
    keyword?: string;
    permissionType?: string;
    module?: string;
  } & PageParams,
): Promise<PageResponse<PermissionItem>> {
  return http.get<PageResponse<PermissionItem>>(API_PATHS.PERMISSION.LIST, params);
}

/**
 * 获取权限详情
 */
export async function getPermissionDetail(id: string): Promise<PermissionItem> {
  return http.get<PermissionItem>(`/api/v1/permissions/${id}`);
}

/**
 * 根据编码获取权限
 */
export async function getPermissionByCode(code: string): Promise<PermissionItem> {
  return http.get<PermissionItem>(`/api/v1/permissions/code/${code}`);
}

/**
 * 创建权限
 */
export async function createPermission(data: {
  code: string;
  name: string;
  type: string;
  module?: string;
  description?: string;
}): Promise<PermissionItem> {
  return http.post<PermissionItem>('/api/v1/permissions', data);
}

/**
 * 更新权限
 */
export async function updatePermission(
  id: string,
  data: Partial<{
    name: string;
    type: string;
    module: string;
    description: string;
  }>,
): Promise<PermissionItem> {
  return http.put<PermissionItem>(`/api/v1/permissions/${id}`, data);
}

/**
 * 删除权限
 */
export async function deletePermission(id: string): Promise<void> {
  return http.delete<void>(`/api/v1/permissions/${id}`);
}

/**
 * 获取所有权限
 */
export async function getAllPermissions(): Promise<PermissionItem[]> {
  return http.get<PermissionItem[]>('/api/v1/permissions/all');
}

/**
 * 获取权限树
 */
export async function getPermissionTree(): Promise<PermissionItem[]> {
  return http.get<PermissionItem[]>(API_PATHS.PERMISSION.TREE);
}

/**
 * 根据模块获取权限
 */
export async function getPermissionsByModule(module: string): Promise<PermissionItem[]> {
  return http.get<PermissionItem[]>(`/api/v1/permissions/module/${module}`);
}

/**
 * 根据类型获取权限
 */
export async function getPermissionsByType(type: string): Promise<PermissionItem[]> {
  return http.get<PermissionItem[]>(`/api/v1/permissions/type/${type}`);
}

/**
 * 检查权限编码是否存在
 */
export async function checkPermissionCode(code: string): Promise<boolean> {
  return http.get<boolean>('/api/v1/permissions/check/code', { code });
}

/**
 * 获取角色的权限
 */
export async function getRolePermissions(roleId: string): Promise<PermissionItem[]> {
  return http.get<PermissionItem[]>(`/api/v1/permissions/role/${roleId}`);
}

/**
 * 获取用户的权限
 */
export async function getUserPermissions(userId: string): Promise<PermissionItem[]> {
  return http.get<PermissionItem[]>(`/api/v1/permissions/user/${userId}`);
}

/**
 * 获取用户的权限编码
 */
export async function getUserPermissionCodes(userId: string): Promise<string[]> {
  return http.get<string[]>(`/api/v1/permissions/user/${userId}/codes`);
}

/**
 * 检查用户权限
 */
export async function checkUserPermission(userId: string, code: string): Promise<boolean> {
  return http.get<boolean>(`/api/v1/permissions/user/${userId}/check/${code}`);
}

/**
 * 获取权限审计日志
 */
export async function getPermissionAuditLogs(
  params?: {
    startTime?: string;
    endTime?: string;
    operatorId?: string;
    action?: string;
  } & PageParams,
): Promise<PageResponse<any>> {
  return http.get<PageResponse<any>>(API_PATHS.PERMISSION.AUDIT_LOGS, params);
}

/**
 * 获取权限审计报告
 */
export async function getPermissionAuditReport(params?: {
  startTime?: string;
  endTime?: string;
}): Promise<any> {
  return http.get<any>(API_PATHS.PERMISSION.AUDIT_REPORT, params);
}

export default {
  getPermissionList,
  getPermissionDetail,
  getPermissionByCode,
  createPermission,
  updatePermission,
  deletePermission,
  getAllPermissions,
  getPermissionTree,
  getPermissionsByModule,
  getPermissionsByType,
  checkPermissionCode,
  getRolePermissions,
  getUserPermissions,
  getUserPermissionCodes,
  checkUserPermission,
  getPermissionAuditLogs,
  getPermissionAuditReport,
};
