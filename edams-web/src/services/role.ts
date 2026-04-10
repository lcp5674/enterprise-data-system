/**
 * 角色服务 API
 */

import { http } from './request';
import { API_PATHS } from '../constants';
import type { Role, PageResponse, PageParams } from '../types';

/**
 * 获取角色列表
 */
export async function getRoleList(
  params?: {
    keyword?: string;
    status?: number;
  } & PageParams,
): Promise<PageResponse<Role>> {
  return http.get<PageResponse<Role>>(API_PATHS.ROLE.LIST, params);
}

/**
 * 获取角色详情
 */
export async function getRoleDetail(id: string): Promise<Role> {
  return http.get<Role>(API_PATHS.ROLE.DETAIL(id));
}

/**
 * 获取角色树
 */
export async function getRoleTree(): Promise<any[]> {
  return http.get<any[]>(API_PATHS.ROLE.TREE);
}

/**
 * 创建角色
 */
export async function createRole(data: {
  name: string;
  code: string;
  description?: string;
  roleType?: number;
  permissionIds?: string[];
}): Promise<Role> {
  return http.post<Role>(API_PATHS.ROLE.CREATE, data);
}

/**
 * 更新角色
 */
export async function updateRole(
  id: string,
  data: Partial<{
    name: string;
    code: string;
    description: string;
    roleType: number;
    status: number;
  }>,
): Promise<Role> {
  return http.put<Role>(API_PATHS.ROLE.UPDATE(id), data);
}

/**
 * 删除角色
 */
export async function deleteRole(id: string): Promise<void> {
  return http.delete<void>(API_PATHS.ROLE.DELETE(id));
}

/**
 * 获取角色权限
 */
export async function getRolePermissions(id: string): Promise<string[]> {
  return http.get<string[]>(API_PATHS.ROLE.PERMISSIONS(id));
}

/**
 * 更新角色权限
 */
export async function updateRolePermissions(
  id: string,
  permissionIds: string[],
): Promise<void> {
  return http.put<void>(API_PATHS.ROLE.UPDATE_PERMISSIONS(id), permissionIds);
}

/**
 * 获取角色的用户
 */
export async function getRoleUsers(
  id: string,
  params?: PageParams,
): Promise<PageResponse<any>> {
  return http.get<PageResponse<any>>(API_PATHS.ROLE.USERS(id), params);
}

export default {
  getRoleList,
  getRoleDetail,
  getRoleTree,
  createRole,
  updateRole,
  deleteRole,
  getRolePermissions,
  updateRolePermissions,
  getRoleUsers,
};
