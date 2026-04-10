/**
 * 通知服务 API
 */

import { http } from './request';
import { API_PATHS } from '../constants';
import type { Notification, PageResponse, PageParams } from '../types';

/**
 * 获取通知列表
 */
export async function getNotificationList(
  params?: {
    type?: string;
    status?: string;
  } & PageParams,
): Promise<PageResponse<Notification>> {
  return http.get<PageResponse<Notification>>(API_PATHS.NOTIFICATION.LIST, params);
}

/**
 * 获取通知详情
 */
export async function getNotificationDetail(id: string): Promise<Notification> {
  return http.get<Notification>(API_PATHS.NOTIFICATION.DETAIL(id));
}

/**
 * 标记通知为已读
 */
export async function markNotificationAsRead(id: string): Promise<void> {
  return http.post<void>(API_PATHS.NOTIFICATION.MARK_READ(id));
}

/**
 * 批量标记通知为已读
 */
export async function batchMarkAsRead(ids: string[]): Promise<void> {
  return http.post<void>(API_PATHS.NOTIFICATION.BATCH_MARK_READ, ids);
}

/**
 * 删除通知
 */
export async function deleteNotification(id: string): Promise<void> {
  return http.delete<void>(API_PATHS.NOTIFICATION.DELETE(id));
}

/**
 * 获取通知模板列表
 */
export async function getNotificationTemplates(
  params?: PageParams,
): Promise<PageResponse<any>> {
  return http.get<PageResponse<any>>(API_PATHS.NOTIFICATION.TEMPLATES, params);
}

/**
 * 获取订阅列表
 */
export async function getSubscriptions(
  params?: PageParams,
): Promise<PageResponse<any>> {
  return http.get<PageResponse<any>>(API_PATHS.NOTIFICATION.SUBSCRIPTIONS, params);
}

/**
 * 发送通知
 */
export async function sendNotification(data: {
  userId?: string;
  userIds?: string[];
  type: string;
  title: string;
  content: string;
  businessType?: string;
  businessId?: string;
}): Promise<{ messageId: string }> {
  return http.post<{ messageId: string }>('/api/v1/notifications/send', data);
}

/**
 * 批量发送通知
 */
export async function batchSendNotification(data: {
  userIds: string[];
  type: string;
  title: string;
  content: string;
}): Promise<void> {
  return http.post<void>('/api/v1/notifications/send/batch', data);
}

/**
 * 发送站内消息
 */
export async function sendInAppMessage(
  userId: string,
  title: string,
  content: string,
  businessType?: string,
  businessId?: string,
): Promise<void> {
  return http.post<void>('/api/v1/notifications/send/in-app', {
    userId,
    title,
    content,
    businessType,
    businessId,
  });
}

/**
 * 发送邮件
 */
export async function sendEmail(
  email: string,
  title: string,
  content: string,
): Promise<void> {
  return http.post<void>('/api/v1/notifications/send/email', {
    email,
    title,
    content,
  });
}

/**
 * 发送短信
 */
export async function sendSms(phone: string, content: string): Promise<void> {
  return http.post<void>('/api/v1/notifications/send/sms', {
    phone,
    content,
  });
}

/**
 * 使用模板发送通知
 */
export async function sendTemplateNotification(
  templateCode: string,
  userId: string,
  variables: Record<string, string>,
): Promise<void> {
  return http.post<void>('/api/v1/notifications/send/template', variables, {
    params: { templateCode, userId },
  });
}

export default {
  getNotificationList,
  getNotificationDetail,
  markNotificationAsRead,
  batchMarkAsRead,
  deleteNotification,
  getNotificationTemplates,
  getSubscriptions,
  sendNotification,
  batchSendNotification,
  sendInAppMessage,
  sendEmail,
  sendSms,
  sendTemplateNotification,
};
