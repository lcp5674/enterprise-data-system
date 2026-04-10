/**
 * 部门服务 API
 */

import { http } from './request';
import { API_PATHS } from '../constants';
import type { Department, PageResponse, PageParams } from '../types';

/**
 * 获取部门列表
 */
export async function getDepartmentList(
  params?: {
    keyword?: string;
    parentId?: string;
  } & PageParams,
): Promise<PageResponse<Department>> {
  return http.get<PageResponse<Department>>(API_PATHS.DEPARTMENT.LIST, params);
}

/**
 * 获取部门树
 */
export async function getDepartmentTree(): Promise<Department[]> {
  return http.get<Department[]>(API_PATHS.DEPARTMENT.TREE);
}

/**
 * 获取部门详情
 */
export async function getDepartmentDetail(id: string): Promise<Department> {
  return http.get<Department>(API_PATHS.DEPARTMENT.DETAIL(id));
}

/**
 * 创建部门
 */
export async function createDepartment(data: {
  name: string;
  code: string;
  parentId?: string;
  description?: string;
  leaderId?: string;
}): Promise<Department> {
  return http.post<Department>(API_PATHS.DEPARTMENT.CREATE, data);
}

/**
 * 更新部门
 */
export async function updateDepartment(
  id: string,
  data: Partial<{
    name: string;
    code: string;
    description: string;
    leaderId: string;
  }>,
): Promise<Department> {
  return http.put<Department>(API_PATHS.DEPARTMENT.UPDATE(id), data);
}

/**
 * 删除部门
 */
export async function deleteDepartment(id: string): Promise<void> {
  return http.delete<void>(API_PATHS.DEPARTMENT.DELETE(id));
}

/**
 * 获取部门下的用户
 */
export async function getDepartmentUsers(
  id: string,
  params?: PageParams,
): Promise<PageResponse<any>> {
  return http.get<PageResponse<any>>(API_PATHS.DEPARTMENT.USERS(id), params);
}

/**
 * 获取部门子结构
 */
export async function getDepartmentSubtree(id: string): Promise<Department[]> {
  return http.get<Department[]>(API_PATHS.DEPARTMENT.SUBTREE(id));
}

/**
 * 获取部门下的资产
 */
export async function getDepartmentAssets(
  id: string,
  params?: PageParams,
): Promise<PageResponse<any>> {
  return http.get<PageResponse<any>>(API_PATHS.DEPARTMENT.ASSETS(id), params);
}

export default {
  getDepartmentList,
  getDepartmentTree,
  getDepartmentDetail,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  getDepartmentUsers,
  getDepartmentSubtree,
  getDepartmentAssets,
};
