/**
 * 数据源服务 API
 */

import { http } from './request';
import { API_PATHS } from '../constants';
import type { DataSourceConfig, PageResponse, PageParams } from '../types';

/**
 * 获取数据源列表
 */
export async function getDatasourceList(
  params?: {
    keyword?: string;
    type?: string;
    status?: string;
  } & PageParams,
): Promise<PageResponse<DataSourceConfig>> {
  return http.get<PageResponse<DataSourceConfig>>(API_PATHS.DATASOURCE.LIST, params);
}

/**
 * 获取数据源详情
 */
export async function getDatasourceDetail(id: string): Promise<DataSourceConfig> {
  return http.get<DataSourceConfig>(API_PATHS.DATASOURCE.DETAIL(id));
}

/**
 * 创建数据源
 */
export async function createDatasource(data: {
  name: string;
  datasourceType: string;
  host?: string;
  port?: number;
  database?: string;
  username?: string;
  password?: string;
  connectionUrl?: string;
  properties?: Record<string, any>;
  description?: string;
  tags?: string[];
  maxConnections?: number;
  connectionTimeout?: number;
  idleTimeout?: number;
}): Promise<DataSourceConfig> {
  return http.post<DataSourceConfig>(API_PATHS.DATASOURCE.CREATE, data);
}

/**
 * 更新数据源
 */
export async function updateDatasource(
  id: string,
  data: Partial<{
    name: string;
    host: string;
    port: number;
    database: string;
    username: string;
    password: string;
    connectionUrl: string;
    properties: Record<string, any>;
    description: string;
    tags: string[];
    maxConnections: number;
    connectionTimeout: number;
    idleTimeout: number;
  }>,
): Promise<DataSourceConfig> {
  return http.put<DataSourceConfig>(API_PATHS.DATASOURCE.UPDATE(id), data);
}

/**
 * 删除数据源
 */
export async function deleteDatasource(id: string): Promise<void> {
  return http.delete<void>(API_PATHS.DATASOURCE.DELETE(id));
}

/**
 * 测试数据源连接
 */
export async function testDatasourceConnection(id: string): Promise<{
  success: boolean;
  message: string;
  responseTime?: number;
}> {
  return http.post<any>(API_PATHS.DATASOURCE.TEST(id));
}

/**
 * 同步数据源
 */
export async function syncDatasource(id: string): Promise<{ taskId: string }> {
  return http.post<any>(API_PATHS.DATASOURCE.SYNC(id));
}

/**
 * 获取同步状态
 */
export async function getDatasourceSyncStatus(id: string): Promise<{
  status: string;
  progress: number;
  lastSyncTime?: string;
  message?: string;
}> {
  return http.get<any>(API_PATHS.DATASOURCE.SYNC_STATUS(id));
}

/**
 * 获取数据源下的表列表
 */
export async function getDatasourceTables(
  id: string,
  params?: {
    database?: string;
    keyword?: string;
  } & PageParams,
): Promise<PageResponse<any>> {
  return http.get<PageResponse<any>>(API_PATHS.DATASOURCE.TABLES(id), params);
}

/**
 * 获取表结构
 */
export async function getTableStructure(
  id: string,
  database: string,
  tableName: string,
): Promise<{
  tableName: string;
  database: string;
  columns: Array<{
    name: string;
    dataType: string;
    comment?: string;
    nullable: boolean;
    primaryKey: boolean;
    defaultValue?: any;
  }>;
  primaryKeys?: string[];
  indexes?: Array<{
    name: string;
    columns: string[];
    type: string;
  }>;
}> {
  return http.get<any>(API_PATHS.DATASOURCE.TABLE_STRUCTURE(id, database, tableName));
}

export default {
  getDatasourceList,
  getDatasourceDetail,
  createDatasource,
  updateDatasource,
  deleteDatasource,
  testDatasourceConnection,
  syncDatasource,
  getDatasourceSyncStatus,
  getDatasourceTables,
  getTableStructure,
};
