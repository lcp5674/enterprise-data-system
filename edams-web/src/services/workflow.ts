/**
 * 工作流服务 API
 */

import { http } from './request';
import { API_PATHS } from '../constants';
import type { PageResponse, PageParams } from '../types';

/**
 * 流程任务项
 */
export interface ProcessTask {
  taskId: string;
  taskName: string;
  processInstanceId: string;
  processDefinitionName: string;
  assignee?: string;
  assigneeName?: string;
  status: number;
  priority?: number;
  createTime: string;
  endTime?: string;
  dueDate?: string;
  comment?: string;
  variables?: Record<string, any>;
}

/**
 * 流程实例
 */
export interface ProcessInstance {
  instanceId: string;
  processDefinitionId: string;
  processDefinitionName: string;
  startUserId: string;
  startUserName: string;
  status: string;
  startTime: string;
  endTime?: string;
  duration?: number;
  currentNodes?: string[];
  variables?: Record<string, any>;
}

/**
 * 流程历史
 */
export interface ProcessHistory {
  id: string;
  taskId?: string;
  nodeName: string;
  assignee?: string;
  assigneeName?: string;
  action: string;
  comment?: string;
  time: string;
  duration?: number;
}

/**
 * 获取待办任务列表
 */
export async function getTodoTasks(
  params?: PageParams,
): Promise<PageResponse<ProcessTask>> {
  return http.get<PageResponse<ProcessTask>>('/api/v1/process-tasks/todo', params);
}

/**
 * 获取已办任务列表
 */
export async function getDoneTasks(
  params?: PageParams,
): Promise<PageResponse<ProcessTask>> {
  return http.get<PageResponse<ProcessTask>>('/api/v1/process-tasks/done', params);
}

/**
 * 获取抄送任务列表
 */
export async function getCcTasks(
  params?: PageParams,
): Promise<PageResponse<ProcessTask>> {
  return http.get<PageResponse<ProcessTask>>('/api/v1/process-tasks/cc', params);
}

/**
 * 获取任务详情
 */
export async function getTaskDetail(taskId: string): Promise<ProcessTask> {
  return http.get<ProcessTask>(`/api/v1/process-tasks/${taskId}`);
}

/**
 * 审批通过任务
 */
export async function approveTask(
  taskId: string,
  data?: {
    comment?: string;
    variables?: Record<string, any>;
  },
): Promise<void> {
  return http.post<void>(`/api/v1/process-tasks/${taskId}/approve`, data || {});
}

/**
 * 审批拒绝任务
 */
export async function rejectTask(
  taskId: string,
  data?: {
    comment?: string;
  },
): Promise<void> {
  return http.post<void>(`/api/v1/process-tasks/${taskId}/reject`, data || {});
}

/**
 * 退回任务
 */
export async function backTask(
  taskId: string,
  data?: {
    comment?: string;
    targetNodeId?: string;
  },
): Promise<void> {
  return http.post<void>(`/api/v1/process-tasks/${taskId}/back`, data || {});
}

/**
 * 转办任务
 */
export async function transferTask(
  taskId: string,
  data: {
    assigneeId: string;
    comment?: string;
  },
): Promise<void> {
  return http.post<void>(`/api/v1/process-tasks/${taskId}/transfer`, data);
}

/**
 * 委托任务
 */
export async function delegateTask(
  taskId: string,
  data: {
    delegateId: string;
    comment?: string;
  },
): Promise<void> {
  return http.post<void>(`/api/v1/process-tasks/${taskId}/delegate`, data);
}

/**
 * 发送任务提醒
 */
export async function sendTaskReminder(taskId: string): Promise<void> {
  return http.post<void>(`/api/v1/process-tasks/${taskId}/remind`);
}

/**
 * 批量审批
 */
export async function batchApprove(
  taskIds: string[],
  result: number,
  comment?: string,
): Promise<void> {
  return http.post<void>(
    '/api/v1/process-tasks/batch-approve',
    { taskIds, result, comment },
    { params: { result } },
  );
}

/**
 * 获取流程实例详情
 */
export async function getProcessInstance(instanceId: string): Promise<ProcessInstance> {
  return http.get<ProcessInstance>(API_PATHS.WORKFLOW.DETAIL(instanceId));
}

/**
 * 审批流程
 */
export async function approveWorkflow(
  instanceId: string,
  data?: {
    comment?: string;
    variables?: Record<string, any>;
  },
): Promise<void> {
  return http.post<void>(API_PATHS.WORKFLOW.APPROVE(instanceId), data || {});
}

/**
 * 拒绝流程
 */
export async function rejectWorkflow(
  instanceId: string,
  data?: {
    comment?: string;
    reason?: string;
  },
): Promise<void> {
  return http.post<void>(API_PATHS.WORKFLOW.REJECT(instanceId), data || {});
}

/**
 * 获取流程历史
 */
export async function getProcessHistory(instanceId: string): Promise<ProcessHistory[]> {
  return http.get<ProcessHistory[]>(API_PATHS.WORKFLOW.HISTORY(instanceId));
}

/**
 * 获取我的审批列表
 */
export async function getMyApprovals(params?: PageParams): Promise<PageResponse<ProcessTask>> {
  return http.get<PageResponse<ProcessTask>>(API_PATHS.WORKFLOW.APPROVALS, params);
}

export default {
  getTodoTasks,
  getDoneTasks,
  getCcTasks,
  getTaskDetail,
  approveTask,
  rejectTask,
  backTask,
  transferTask,
  delegateTask,
  sendTaskReminder,
  batchApprove,
  getProcessInstance,
  approveWorkflow,
  rejectWorkflow,
  getProcessHistory,
  getMyApprovals,
};
