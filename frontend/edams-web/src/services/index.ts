/**
 * 服务统一导出
 * EDAMS - 企业数据资产管理系统 API Services
 */

// 核心服务
export * from './request';
export { default as http } from './request';

// 认证与用户
export { default as auth } from './auth';
export * from './auth';

export { default as user } from './user';
export * from './user';

export { default as role } from './role';
export * from './role';

export { default as permission } from './permission';
export * from './permission';

export { default as department } from './department';
export * from './department';

// 数据资产
export { default as asset } from './asset';
export * from './asset';

export { default as metadata } from './metadata';
export * from './metadata';

export { default as lineage } from './lineage';
export * from './lineage';

export { default as lineageDetail } from './lineage-detail';
export * from './lineage-detail';

export { default as datasource } from './datasource';
export * from './datasource';

// 数据治理
export { default as quality } from './quality';
export * from './quality';

export { default as standard } from './standard';
export * from './standard';

export { default as governance } from './governance';
export * from './governance';

// 工作流与通知
export { default as workflow } from './workflow';
export * from './workflow';

export { default as notification } from './notification';
export * from './notification';

// 统计与报表
export { default as statistics } from './statistics';
export * from './statistics';

// 扩展服务
export { default as knowledge } from './knowledge';
export * from './knowledge';

export { default as chatbot } from './chatbot';
export * from './chatbot';

export { default as sla } from './sla';
export * from './sla';

export { default as sandbox } from './sandbox';
export * from './sandbox';

export { default as rules } from './rules';
export * from './rules';

export { default as sso } from './sso';
export * from './sso';

// 统一API对象
import auth from './auth';
import user from './user';
import role from './role';
import permission from './permission';
import department from './department';
import asset from './asset';
import metadata from './metadata';
import lineage from './lineage';
import lineageDetail from './lineage-detail';
import datasource from './datasource';
import quality from './quality';
import standard from './standard';
import governance from './governance';
import workflow from './workflow';
import notification from './notification';
import statistics from './statistics';
import knowledge from './knowledge';
import chatbot from './chatbot';
import sla from './sla';
import sandbox from './sandbox';
import rules from './rules';
import sso from './sso';

/**
 * 统一API入口
 * 使用示例: api.auth.login(), api.user.getUserList()
 */
export const api = {
  auth,
  user,
  role,
  permission,
  department,
  asset,
  metadata,
  lineage,
  lineageDetail,
  datasource,
  quality,
  standard,
  governance,
  workflow,
  notification,
  statistics,
  knowledge,
  chatbot,
  sla,
  sandbox,
  rules,
  sso,
};

export default api;
