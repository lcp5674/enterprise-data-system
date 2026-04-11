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
}) => request.post('/chatbot/api/v1/messages', data);

/**
 * 获取会话历史
 * @param sessionId 会话ID
 * @param params 分页参数 {limit, before}
 */
export const getHistory = (
  sessionId: string,
  params?: { limit?: number; before?: string }
) => request.get(`/chatbot/api/v1/sessions/${sessionId}/messages`, { params });

/**
 * 获取会话列表
 * @param params 查询参数 {status, page, size}
 */
export const getConversations = (params?: { status?: string; page?: number; size?: number }) =>
  request.get('/chatbot/api/v1/sessions', { params });

/**
 * 创建新会话
 * @param data 会话数据 {title, context}
 */
export const createConversation = (data?: { title?: string; context?: Record<string, any> }) =>
  request.post('/chatbot/api/v1/sessions', data);

/**
 * 删除会话
 * @param id 会话ID
 */
export const deleteConversation = (id: string) =>
  request.delete(`/chatbot/api/v1/sessions/${id}`);

/**
 * 更新会话
 * @param id 会话ID
 * @param data 会话数据 {title}
 */
export const updateConversation = (id: string, data: { title: string }) =>
  request.put(`/chatbot/api/v1/sessions/${id}`, data);

/**
 * 清空会话消息
 * @param id 会话ID
 */
export const clearConversation = (id: string) =>
  request.post(`/chatbot/api/v1/sessions/${id}/clear`);

/**
 * 流式发送消息（使用EventSource）
 * @param data 消息数据
 * @param onMessage 消息回调
 * @param onError 错误回调
 */
export const sendMessageStream = (
  data: {
    sessionId: string;
    message: string;
    context?: Record<string, any>;
  },
  onMessage: (chunk: string) => void,
  onError?: (error: any) => void,
  onComplete?: () => void
) => {
  const url = `/chatbot/api/v1/messages/stream?sessionId=${encodeURIComponent(
    data.sessionId
  )}&message=${encodeURIComponent(data.message)}`;

  const eventSource = new EventSource(url);

  eventSource.onmessage = (event) => {
    try {
      const parsed = JSON.parse(event.data);
      if (parsed.type === 'content') {
        onMessage(parsed.content);
      } else if (parsed.type === 'done') {
        eventSource.close();
        onComplete?.();
      }
    } catch (e) {
      onMessage(event.data);
    }
  };

  eventSource.onerror = (error) => {
    eventSource.close();
    onError?.(error);
  };

  return () => eventSource.close();
};

/**
 * 获取AI助手能力列表
 */
export const getAiCapabilities = () =>
  request.get('/chatbot/api/v1/capabilities');

/**
 * 上传文件进行对话
 * @param sessionId 会话ID
 * @param file 文件
 * @param onProgress 进度回调
 */
export const uploadFileForChat = (
  sessionId: string,
  file: File,
  onProgress?: (percent: number) => void
) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('sessionId', sessionId);

  return request.post('/chatbot/api/v1/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (progressEvent) => {
      if (progressEvent.total && onProgress) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        onProgress(percent);
      }
    },
  });
};

/**
 * 评价AI回复
 * @param messageId 消息ID
 * @param rating 评价 { helpful: boolean, comment?: string }
 */
export const rateMessage = (
  messageId: string,
  rating: { helpful: boolean; comment?: string }
) => request.post(`/chatbot/api/v1/messages/${messageId}/rate`, rating);
