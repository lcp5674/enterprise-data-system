/**
 * 认证服务 API
 */

import { http } from './request';
import { API_PATHS, LOGIN_TYPES } from '../constants';
import type { LoginParams, LoginResponse, MFAStatus, User } from '../types';

/**
 * 用户登录
 */
export async function login(params: LoginParams): Promise<LoginResponse> {
  return http.post<LoginResponse>(API_PATHS.AUTH.LOGIN, params);
}

/**
 * 用户登出
 */
export async function logout(): Promise<void> {
  return http.post<void>(API_PATHS.AUTH.LOGOUT);
}

/**
 * 刷新 Token
 */
export async function refreshToken(refreshToken: string): Promise<{
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}> {
  return http.post<any>(API_PATHS.AUTH.REFRESH, { refreshToken });
}

/**
 * 获取图形验证码
 */
export async function getCaptcha(): Promise<{
  captchaId: string;
  captchaImage: string;
}> {
  return http.get<any>(API_PATHS.AUTH.CAPTCHA);
}

/**
 * 发送手机验证码
 */
export async function sendMobileCode(params: {
  mobile: string;
  captcha?: string;
  captchaId?: string;
  scene: 'LOGIN' | 'REGISTER' | 'RESET_PASSWORD' | 'BIND_MOBILE';
}): Promise<void> {
  return http.post<void>(API_PATHS.AUTH.MOBILE_SEND_CODE, params);
}

/**
 * 手机号验证登录
 */
export async function verifyMobileCode(params: {
  mobile: string;
  code: string;
}): Promise<LoginResponse> {
  return http.post<LoginResponse>(API_PATHS.AUTH.MOBILE_VERIFY, params);
}

/**
 * 用户注册
 */
export async function register(params: {
  username: string;
  password: string;
  confirmPassword?: string;
  email: string;
  mobile?: string;
  nickname?: string;
  departmentId?: string;
  inviteCode?: string;
  agreeTerms?: boolean;
}): Promise<void> {
  return http.post<void>(API_PATHS.AUTH.REGISTER, params);
}

/**
 * 重置密码
 */
export async function resetPassword(params: {
  verifyType: 'EMAIL' | 'MOBILE';
  verifyCode: string;
  newPassword: string;
  confirmPassword?: string;
}): Promise<void> {
  return http.post<void>(API_PATHS.AUTH.RESET_PASSWORD, params);
}

/**
 * 修改密码
 */
export async function changePassword(params: {
  oldPassword: string;
  newPassword: string;
  confirmPassword?: string;
}): Promise<void> {
  return http.post<void>(API_PATHS.AUTH.CHANGE_PASSWORD, params);
}

/**
 * 获取 SSO 登录 URL
 */
export async function getSSOUrl(params: {
  provider: 'wxwork' | 'dingtalk' | 'ldap';
  redirectUri: string;
}): Promise<{ url: string }> {
  return http.get<any>(API_PATHS.AUTH.SSO_URL, params);
}

/**
 * SSO 回调处理
 */
export async function handleSSOCallback(params: {
  code: string;
  state?: string;
}): Promise<LoginResponse> {
  return http.post<LoginResponse>(API_PATHS.AUTH.SSO_CALLBACK, params);
}

/**
 * 获取当前会话信息
 */
export async function getSession(): Promise<{
  user: User;
  expiresAt: string;
  lastActivity: string;
}> {
  return http.get<any>(API_PATHS.AUTH.SESSION);
}

/**
 * 销毁指定会话
 */
export async function destroySession(sessionId: string): Promise<void> {
  return http.delete<void>(API_PATHS.AUTH.SESSION, { sessionId });
}

/**
 * 获取 MFA 状态
 */
export async function getMFAStatus(): Promise<MFAStatus> {
  return http.get<MFAStatus>(API_PATHS.AUTH.MFA_STATUS);
}

/**
 * 启用 MFA
 */
export async function enableMFA(): Promise<{
  secret: string;
  qrCode: string;
  backupCodes: string[];
}> {
  return http.post<any>(API_PATHS.AUTH.MFA_ENABLE);
}

/**
 * 验证 MFA 码
 */
export async function verifyMFACode(code: string): Promise<void> {
  return http.post<void>(API_PATHS.AUTH.MFA_VERIFY, { code });
}

/**
 * 禁用 MFA
 */
export async function disableMFA(params: {
  password: string;
  code: string;
}): Promise<void> {
  return http.post<void>(API_PATHS.AUTH.MFA_DISABLE, params);
}

export default {
  login,
  logout,
  refreshToken,
  getCaptcha,
  sendMobileCode,
  verifyMobileCode,
  register,
  resetPassword,
  changePassword,
  getSSOUrl,
  handleSSOCallback,
  getSession,
  destroySession,
  getMFAStatus,
  enableMFA,
  verifyMFACode,
  disableMFA,
};
