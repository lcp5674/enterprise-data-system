/**
 * Axios 请求封装
 */

import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { message, notification } from 'antd';
import { history } from '@umijs/max';
import { API_BASE_URL, API_TIMEOUT, STORAGE_KEYS, ERROR_CODES } from '../constants';

// 创建 Axios 实例
const request: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

// 请求拦截器
request.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    // 添加 Token
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // 添加语言
    const language = localStorage.getItem(STORAGE_KEYS.LANGUAGE) || 'zh-CN';
    if (config.headers) {
      config.headers['X-Language'] = language;
    }

    // 添加请求ID
    if (config.headers) {
      config.headers['X-Request-Id'] = generateRequestId();
    }

    return config;
  },
  (error) => {
    console.error('请求配置错误:', error);
    return Promise.reject(error);
  },
);

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message: msg, data } = response.data;

    // 成功响应
    if (code === ERROR_CODES.SUCCESS || code === 0) {
      return data;
    }

    // Token 无效或过期
    if (code === ERROR_CODES.TOKEN_INVALID || code === ERROR_CODES.TOKEN_EXPIRED) {
      handleAuthError();
      return Promise.reject(new Error(msg || '登录已过期，请重新登录'));
    }

    // 其他错误
    if (msg) {
      message.error(msg);
    }

    return Promise.reject(new Error(msg || '请求失败'));
  },
  (error) => {
    // 处理 HTTP 错误状态码
    const status = error.response?.status;

    switch (status) {
      case 400:
        message.error('请求参数错误');
        break;
      case 401:
        handleAuthError();
        message.error('登录已过期，请重新登录');
        break;
      case 403:
        message.error('没有访问权限');
        break;
      case 404:
        message.error('请求的资源不存在');
        break;
      case 429:
        message.error('请求过于频繁，请稍后再试');
        break;
      case 500:
        message.error('服务器内部错误');
        break;
      case 502:
        message.error('网关错误');
        break;
      case 503:
        message.error('服务暂不可用');
        break;
      case 504:
        message.error('网关超时');
        break;
      default:
        if (error.message === 'Network Error') {
          message.error('网络连接失败，请检查网络');
        } else if (error.code === 'ECONNABORTED') {
          message.error('请求超时，请稍后再试');
        } else {
          message.error(error.message || '请求失败');
        }
    }

    return Promise.reject(error);
  },
);

// 处理认证错误
function handleAuthError(): void {
  // 清除本地存储的认证信息
  localStorage.removeItem(STORAGE_KEYS.TOKEN);
  localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
  localStorage.removeItem(STORAGE_KEYS.USER_INFO);
  localStorage.removeItem(STORAGE_KEYS.MENUS);

  // 跳转到登录页
  if (history.location.pathname !== '/user/login') {
    history.push({
      pathname: '/user/login',
      query: { redirect: history.location.pathname },
    });
  }
}

// 生成请求ID
function generateRequestId(): string {
  return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
}

// 请求方法封装
export const http = {
  get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> {
    return request.get(url, { params, ...config });
  },

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return request.post(url, data, config);
  },

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return request.put(url, data, config);
  },

  delete<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> {
    return request.delete(url, { params, ...config });
  },

  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return request.patch(url, data, config);
  },

  head<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return request.head(url, config);
  },

  options<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return request.options(url, config);
  },

  download(url: string, params?: any, filename?: string): Promise<void> {
    return request
      .get(url, {
        params,
        responseType: 'blob',
      })
      .then((response: any) => {
        const blob = new Blob([response]);
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename || 'download';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      });
  },

  upload<T = any>(
    url: string,
    file: File | Blob,
    filename?: string,
    onProgress?: (percent: number) => void,
  ): Promise<T> {
    const formData = new FormData();
    formData.append('file', file, filename || file.name);

    return request.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total && onProgress) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(percent);
        }
      },
    });
  },
};

export default request;
