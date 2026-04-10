/**
 * 用户服务 API
 */

import { http } from './request';
import { API_PATHS } from '../constants';
import type { User, PageResponse, PageParams, Department } from '../types';

/**
 * 获取用户列表
 */
export async function getUserList(
  params?: {
    keyword?: string;
    departmentId?: string;
    status?: number;
  } & PageParams,
): Promise<PageResponse<User>> {
  return http.get<PageResponse<User>>(API_PATHS.USER.LIST, params);
}

/**
 * 获取用户详情
 */
export async function getUserDetail(id: string): Promise<User> {
  return http.get<User>(API_PATHS.USER.DETAIL(id));
}

/**
 * 根据用户名获取用户
 */
export async function getUserByUsername(username: string): Promise<User> {
  return http.get<User>(`/api/v1/users/username/${username}`);
}

/**
 * 创建用户
 */
export async function createUser(data: {
  username: string;
  password: string;
  email: string;
  nickname?: string;
  mobile?: string;
  departmentId?: string;
  roleIds?: string[];
}): Promise<User> {
  return http.post<User>(API_PATHS.USER.CREATE, data);
}

/**
 * 更新用户
 */
export async function updateUser(
  id: string,
  data: Partial<{
    email: string;
    nickname: string;
    mobile: string;
    departmentId: string;
  }>,
): Promise<User> {
  return http.put<User>(API_PATHS.USER.UPDATE(id), data);
}

/**
 * 删除用户
 */
export async function deleteUser(id: string): Promise<void> {
  return http.delete<void>(API_PATHS.USER.DELETE(id));
}

/**
 * 批量删除用户
 */
export async function batchDeleteUsers(userIds: string[]): Promise<number> {
  return http.post<number>('/api/v1/users/batch-delete', userIds);
}

/**
 * 启用用户
 */
export async function enableUser(id: string): Promise<void> {
  return http.put<void>(API_PATHS.USER.ENABLE(id));
}

/**
 * 禁用用户
 */
export async function disableUser(id: string): Promise<void> {
  return http.put<void>(API_PATHS.USER.DISABLE(id));
}

/**
 * 重置密码
 */
export async function resetUserPassword(id: string, newPassword: string): Promise<void> {
  return http.put<void>(API_PATHS.USER.RESET_PASSWORD(id), { newPassword });
}

/**
 * 分配角色
 */
export async function assignRoles(id: string, roleIds: string[]): Promise<void> {
  return http.put<void>(API_PATHS.USER.ASSIGN_ROLES(id), roleIds);
}

/**
 * 获取用户角色
 */
export async function getUserRoles(id: string): Promise<string[]> {
  return http.get<string[]>(`/api/v1/users/${id}/roles`);
}

/**
 * 获取用户菜单
 */
export async function getUserMenus(id: string): Promise<string[]> {
  return http.get<string[]>(`/api/v1/users/${id}/menus`);
}

/**
 * 获取用户权限
 */
export async function getUserPermissions(id: string): Promise<string[]> {
  return http.get<string[]>(API_PATHS.USER.PERMISSIONS(id));
}

/**
 * 解锁用户
 */
export async function unlockUser(id: string): Promise<void> {
  return http.post<void>(API_PATHS.USER.UNLOCK(id));
}

/**
 * 批量创建用户
 */
export async function batchCreateUsers(data: {
  users: Array<{
    username: string;
    password: string;
    email: string;
    nickname?: string;
    mobile?: string;
    departmentId?: string;
  }>;
}): Promise<{ successCount: number; failCount: number }> {
  return http.post<any>(API_PATHS.USER.BATCH_CREATE, data);
}

/**
 * 获取当前用户资料
 */
export async function getMyProfile(): Promise<User> {
  return http.get<User>(API_PATHS.USER.ME_PROFILE);
}

/**
 * 更新当前用户资料
 */
export async function updateMyProfile(data: Partial<{
  nickname: string;
  mobile: string;
  avatar: string;
}>): Promise<User> {
  return http.put<User>(API_PATHS.USER.ME_PROFILE, data);
}

/**
 * 获取当前用户偏好设置
 */
export async function getMyPreferences(): Promise<Record<string, any>> {
  return http.get<Record<string, any>>(API_PATHS.USER.ME_PREFERENCES);
}

/**
 * 更新当前用户偏好设置
 */
export async function updateMyPreferences(preferences: Record<string, any>): Promise<void> {
  return http.put<void>(API_PATHS.USER.ME_PREFERENCES, preferences);
}

/**
 * 获取工作台数据
 */
export async function getMyWorkbench(): Promise<{
  pendingApprovals: number;
  todoTasks: number;
  recentAssets: any[];
  qualityAlerts: number;
  notifications: number;
}> {
  return http.get<any>(API_PATHS.USER.ME_WORKBENCH);
}

/**
 * 获取最近访问资产
 */
export async function getRecentAssets(params?: { limit?: number }): Promise<any[]> {
  return http.get<any[]>(API_PATHS.USER.ME_RECENT, params);
}

/**
 * 获取收藏资产
 */
export async function getFavoriteAssets(params?: PageParams): Promise<PageResponse<User>> {
  return http.get<PageResponse<User>>(API_PATHS.USER.ME_FAVORITES, params);
}

/**
 * 检查用户名是否存在
 */
export async function checkUsername(username: string): Promise<boolean> {
  return http.get<boolean>('/api/v1/users/check/username', { username });
}

/**
 * 检查邮箱是否存在
 */
export async function checkEmail(email: string): Promise<boolean> {
  return http.get<boolean>('/api/v1/users/check/email', { email });
}

export default {
  getUserList,
  getUserDetail,
  getUserByUsername,
  createUser,
  updateUser,
  deleteUser,
  batchDeleteUsers,
  enableUser,
  disableUser,
  resetUserPassword,
  assignRoles,
  getUserRoles,
  getUserMenus,
  getUserPermissions,
  unlockUser,
  batchCreateUsers,
  getMyProfile,
  updateMyProfile,
  getMyPreferences,
  updateMyPreferences,
  getMyWorkbench,
  getRecentAssets,
  getFavoriteAssets,
  checkUsername,
  checkEmail,
};
