/**
 * AI聊天机器人服务
 */

import request from './request';

/**
 * 发送消息
 * @param data 消息数据 {sessionId, message, context}
 */
export const sendMessage = (data: {
  sessionId: string;
  message: string;
  context?: {
    assetId?: string;
    domain?: string;
    previousMessages?: Array<{ role: string; content: string }>;
  };
}) => request.post('/api/v1/chatbot/chat/session/send', {
  sessionId: data.sessionId,
  message: data.message,
});

/**
 * 获取会话历史
 * @param sessionId 会话ID
 * @param params 分页参数 {limit, before}
 */
export const getHistory = (
  sessionId: string,
  params?: { limit?: number; before?: string }
) => request.get(`/api/v1/chatbot/chat/session/${sessionId}/messages`, { params });

/**
 * 获取会话列表
 * @param params 查询参数 {status, page, size}
 */
export const getConversations = (params?: { status?: string; page?: number; size?: number }) => {
  // 转换参数以适配后端
  const userId = 1; // 默认用户ID，实际应从登录状态获取
  return request.get('/api/v1/chatbot/chat/sessions', { params: { userId, ...params } });
};

/**
 * 创建新会话
 * @param data 会话数据 {title, context}
 */
export const createConversation = (data?: { title?: string; context?: Record<string, any> }) => {
  const userId = 1; // 默认用户ID
  return request.post('/api/v1/chatbot/chat/session', {
    userId,
    username: 'user',
    sessionTitle: data?.title || '新会话',
  });
};

/**
 * 删除会话
 * @param id 会话ID
 */
export const deleteConversation = (id: string) =>
  request.delete(`/api/v1/chatbot/chat/session/${id}/close`);

/**
 * 更新会话
 * @param id 会话ID
 * @param data 会话数据 {title}
 */
export const updateConversation = (id: string, data: { title: string }) =>
  request.put(`/api/v1/chatbot/chat/session/${id}`, data);

/**
 * 清空会话消息
 * @param id 会话ID
 */
export const clearConversation = (id: string) =>
  request.post(`/api/v1/chatbot/chat/session/${id}/clear`);

/**
 * 获取AI助手能力列表
 */
export const getAiCapabilities = () =>
  request.get('/api/v1/chatbot/capabilities');

/**
 * 评价AI回复
 * @param messageId 消息ID
 * @param rating 评价 { helpful: boolean, comment?: string }
 */
export const rateMessage = (
  messageId: string,
  rating: { helpful: boolean; comment?: string }
) => request.post(`/api/v1/chatbot/chat/message/${messageId}/rate`, {
  rating: rating.helpful ? 1 : 0,
  feedback: rating.comment,
});

export default {
  sendMessage,
  getHistory,
  getConversations,
  createConversation,
  deleteConversation,
  updateConversation,
  clearConversation,
  getAiCapabilities,
  rateMessage,
};
