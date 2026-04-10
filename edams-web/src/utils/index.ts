/**
 * 通用工具函数
 */

import dayjs from 'dayjs';
import { DATE_FORMATS } from '../constants';

/**
 * 格式化日期
 */
export function formatDate(date: string | Date | number, format: string = DATE_FORMATS.DATE): string {
  if (!date) return '-';
  return dayjs(date).format(format);
}

/**
 * 格式化日期时间
 */
export function formatDateTime(date: string | Date | number): string {
  if (!date) return '-';
  return dayjs(date).format(DATE_FORMATS.DATETIME);
}

/**
 * 获取相对时间
 */
export function getRelativeTime(date: string | Date | number): string {
  if (!date) return '-';
  const now = dayjs();
  const target = dayjs(date);
  const diff = now.diff(target, 'minute');

  if (diff < 1) return '刚刚';
  if (diff < 60) return `${diff}分钟前`;
  if (diff < 1440) return `${Math.floor(diff / 60)}小时前`;
  if (diff < 10080) return `${Math.floor(diff / 1440)}天前`;
  if (diff < 43200) return `${Math.floor(diff / 10080)}周前`;
  if (diff < 525600) return `${Math.floor(diff / 43200)}月前`;
  return `${Math.floor(diff / 525600)}年前`;
}

/**
 * 深拷贝
 */
export function deepClone<T>(obj: T): T {
  if (obj === null || typeof obj !== 'object') return obj;
  if (obj instanceof Date) return new Date(obj.getTime()) as any;
  if (obj instanceof Array) return obj.map(item => deepClone(item)) as any;
  if (obj instanceof Object) {
    const clonedObj: any = {};
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key]);
      }
    }
    return clonedObj;
  }
  return obj;
}

/**
 * 防抖函数
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number,
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout | null = null;
  return function (...args: Parameters<T>) {
    if (timeout) clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
}

/**
 * 节流函数
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  wait: number,
): (...args: Parameters<T>) => void {
  let lastTime: number | null = null;
  return function (...args: Parameters<T>) {
    const now = Date.now();
    if (!lastTime || now - lastTime >= wait) {
      lastTime = now;
      func(...args);
    }
  };
}

/**
 * 生成随机字符串
 */
export function generateRandomString(length: number = 8): string {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

/**
 * 生成唯一ID
 */
export function generateUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`;
}

/**
 * 格式化数字
 */
export function formatNumber(num: number, decimals: number = 0): string {
  if (num === null || num === undefined) return '-';
  return num.toLocaleString('zh-CN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

/**
 * 格式化百分比
 */
export function formatPercent(value: number, total: number, decimals: number = 2): string {
  if (total === 0) return '0%';
  return `${((value / total) * 100).toFixed(decimals)}%`;
}

/**
 * 判断是否为空
 */
export function isEmpty(value: any): boolean {
  if (value === null || value === undefined) return true;
  if (typeof value === 'string') return value.trim() === '';
  if (Array.isArray(value)) return value.length === 0;
  if (typeof value === 'object') return Object.keys(value).length === 0;
  return false;
}

/**
 * 获取对象嵌套属性
 */
export function getNestedValue(obj: any, path: string, defaultValue: any = null): any {
  const keys = path.split('.');
  let result = obj;
  for (const key of keys) {
    if (result === null || result === undefined) return defaultValue;
    result = result[key];
  }
  return result ?? defaultValue;
}

/**
 * 设置对象嵌套属性
 */
export function setNestedValue(obj: any, path: string, value: any): void {
  const keys = path.split('.');
  const lastKey = keys.pop();
  let current = obj;
  for (const key of keys) {
    if (!(key in current)) current[key] = {};
    current = current[key];
  }
  if (lastKey) current[lastKey] = value;
}

/**
 * 移除对象空属性
 */
export function removeEmpty<T extends Record<string, any>>(obj: T): Partial<T> {
  const result: any = {};
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      const value = obj[key];
      if (!isEmpty(value)) {
        result[key] = value;
      }
    }
  }
  return result;
}

/**
 * 合并对象
 */
export function merge<T extends Record<string, any>, U extends Record<string, any>>(
  target: T,
  source: U,
): T & U {
  const result: any = { ...target };
  for (const key in source) {
    if (source.hasOwnProperty(key)) {
      const sourceValue = source[key];
      const targetValue = result[key];
      if (
        typeof sourceValue === 'object' &&
        sourceValue !== null &&
        !Array.isArray(sourceValue) &&
        typeof targetValue === 'object' &&
        targetValue !== null &&
        !Array.isArray(targetValue)
      ) {
        result[key] = merge(targetValue, sourceValue);
      } else {
        result[key] = sourceValue;
      }
    }
  }
  return result;
}

/**
 * 校验手机号
 */
export function isValidPhone(phone: string): boolean {
  return /^1[3-9]\d{9}$/.test(phone);
}

/**
 * 校验邮箱
 */
export function isValidEmail(email: string): boolean {
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email);
}

/**
 * 校验密码强度
 */
export function isValidPassword(password: string): {
  valid: boolean;
  message: string;
  strength: 'weak' | 'medium' | 'strong';
} {
  if (password.length < 8) {
    return { valid: false, message: '密码长度至少8位', strength: 'weak' };
  }
  if (!/[a-z]/.test(password)) {
    return { valid: false, message: '密码必须包含小写字母', strength: 'weak' };
  }
  if (!/[A-Z]/.test(password)) {
    return { valid: false, message: '密码必须包含大写字母', strength: 'medium' };
  }
  if (!/\d/.test(password)) {
    return { valid: false, message: '密码必须包含数字', strength: 'medium' };
  }
  if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
    return { valid: true, message: '密码强度：中等', strength: 'medium' };
  }
  return { valid: true, message: '密码强度：强', strength: 'strong' };
}

/**
 * 隐藏手机号中间4位
 */
export function maskPhone(phone: string): string {
  if (!phone) return '-';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
}

/**
 * 隐藏邮箱
 */
export function maskEmail(email: string): string {
  if (!email) return '-';
  return email.replace(/(.{2}).*(@.*)/, '$1***$2');
}

/**
 * 获取状态颜色
 */
export function getStatusColor(status: string): string {
  const colorMap: Record<string, string> = {
    DRAFT: 'default',
    PENDING: 'processing',
    APPROVED: 'success',
    REJECTED: 'error',
    PUBLISHED: 'success',
    DEPRECATED: 'warning',
    ARCHIVED: 'default',
    OPEN: 'error',
    IN_PROGRESS: 'processing',
    RESOLVED: 'success',
    CLOSED: 'default',
    IGNORED: 'default',
    PASS: 'success',
    FAIL: 'error',
    ERROR: 'error',
    RUNNING: 'processing',
    ACTIVE: 'success',
    INACTIVE: 'default',
    DELETED: 'default',
  };
  return colorMap[status] || 'default';
}

/**
 * 获取敏感级别颜色
 */
export function getSensitivityColor(level: string): string {
  const colorMap: Record<string, string> = {
    L1: '#52c41a',
    L2: '#faad14',
    L3: '#fa8c16',
    L4: '#f5222d',
  };
  return colorMap[level] || '#8c8c8c';
}

/**
 * 获取认证等级颜色
 */
export function getCertificationColor(level: string): string {
  const colorMap: Record<string, string> = {
    BRONZE: '#cd7f32',
    SILVER: '#c0c0c0',
    GOLD: '#ffd700',
  };
  return colorMap[level] || '#8c8c8c';
}

/**
 * 复制到剪贴板
 */
export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(text);
      return true;
    }
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    const result = document.execCommand('copy');
    document.body.removeChild(textarea);
    return result;
  } catch {
    return false;
  }
}

/**
 * 下载文件
 */
export function downloadFile(url: string, filename?: string): void {
  const link = document.createElement('a');
  link.href = url;
  link.download = filename || '';
  link.target = '_blank';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

/**
 * 字符串首字母大写
 */
export function capitalizeFirstLetter(str: string): string {
  if (!str) return '';
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}

/**
 * 转换为驼峰命名
 */
export function toCamelCase(str: string): string {
  return str.replace(/[-_](\w)/g, (_, c) => (c ? c.toUpperCase() : ''));
}

/**
 * 转换为短横线命名
 */
export function toKebabCase(str: string): string {
  return str.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();
}

/**
 * 下载JSON为文件
 */
export function downloadJSON(data: any, filename: string = 'download.json'): void {
  const jsonStr = JSON.stringify(data, null, 2);
  const blob = new Blob([jsonStr], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  downloadFile(url, filename);
  URL.revokeObjectURL(url);
}

/**
 * 获取URL参数
 */
export function getQueryParams(url: string = window.location.href): URLSearchParams {
  const urlObj = new URL(url);
  return urlObj.searchParams;
}

/**
 * 获取单个URL参数
 */
export function getQueryParam(key: string, url?: string): string | null {
  return getQueryParams(url).get(key);
}

/**
 * 构建URL参数
 */
export function buildQueryString(params: Record<string, any>): string {
  const searchParams = new URLSearchParams();
  for (const key in params) {
    if (params.hasOwnProperty(key) && params[key] !== null && params[key] !== undefined) {
      const value = params[key];
      if (Array.isArray(value)) {
        value.forEach(v => searchParams.append(key, String(v)));
      } else {
        searchParams.append(key, String(value));
      }
    }
  }
  return searchParams.toString();
}

export default {
  formatDate,
  formatDateTime,
  getRelativeTime,
  deepClone,
  debounce,
  throttle,
  generateRandomString,
  generateUUID,
  formatFileSize,
  formatNumber,
  formatPercent,
  isEmpty,
  getNestedValue,
  setNestedValue,
  removeEmpty,
  merge,
  isValidPhone,
  isValidEmail,
  isValidPassword,
  maskPhone,
  maskEmail,
  getStatusColor,
  getSensitivityColor,
  getCertificationColor,
  copyToClipboard,
  downloadFile,
  capitalizeFirstLetter,
  toCamelCase,
  toKebabCase,
  downloadJSON,
  getQueryParams,
  getQueryParam,
  buildQueryString,
};
