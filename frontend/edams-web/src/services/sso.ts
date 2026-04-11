/**
 * SSO单点登录服务 API
 * 支持Keycloak/OAuth2 SSO
 */

import { http } from './request';
import type { User } from '../types';

/**
 * SSO提供商配置
 */
export interface SSOProvider {
  name: string;
  displayName: string;
  icon?: string;
  enabled: boolean;
}

/**
 * SSO登录响应
 */
export interface SSOLoginResponse {
  success: boolean;
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userInfo?: SSOUserInfo;
  authorizationUri?: string;
  message?: string;
  provider?: string;
}

/**
 * SSO用户信息
 */
export interface SSOUserInfo {
  subject: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  displayName?: string;
  roles: string[];
  enabled?: boolean;
  emailVerified?: boolean;
}

/**
 * SSO配置
 */
export interface SSOConfig {
  enabled: boolean;
  provider: string;
  realm: string;
  issuerUri: string;
}

/**
 * 获取SSO配置
 */
export async function getSSOConfig(): Promise<SSOConfig> {
  return http.get<SSOConfig>('/auth/api/v1/sso/health');
}

/**
 * 发起SSO登录
 * 返回Keycloak授权URL
 */
export async function initiateSSOLogin(redirectUri?: string, state?: string): Promise<{
  success: boolean;
  authorizationUri: string;
  provider: string;
  realm: string;
}> {
  const params: Record<string, string> = {};
  if (redirectUri) params.redirectUri = redirectUri;
  if (state) params.state = state;
  
  return http.get<any>('/auth/api/v1/sso/login', params);
}

/**
 * 处理OAuth2回调
 * 使用授权码交换访问令牌
 */
export async function handleSSOCallback(code: string, redirectUri: string, state?: string): Promise<SSOLoginResponse> {
  return http.post<SSOLoginResponse>('/auth/api/v1/sso/callback', {
    code,
    redirectUri,
    state,
  });
}

/**
 * 获取SSO用户信息
 */
export async function getSSOUserInfo(): Promise<SSOUserInfo> {
  return http.get<SSOUserInfo>('/auth/api/v1/sso/userinfo');
}

/**
 * SSO登出
 */
export async function ssoLogout(idToken?: string): Promise<{
  success: boolean;
  message: string;
  keycloakLogoutUrl?: string;
}> {
  return http.post<any>('/auth/api/v1/sso/logout', { idToken });
}

/**
 * 刷新SSO Token
 */
export async function refreshSSOToken(refreshToken: string): Promise<{
  success: boolean;
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}> {
  return http.post<any>('/auth/api/v1/sso/refresh', { refreshToken });
}

/**
 * 验证Token有效性
 */
export async function validateToken(token: string): Promise<{
  valid: boolean;
  provider: string;
}> {
  return http.post<any>('/auth/api/v1/sso/validate', { token });
}

/**
 * 获取可用Realm列表（多租户）
 */
export async function getAvailableRealms(): Promise<{
  defaultRealm: string;
  multiTenancyEnabled: boolean;
  realms: Array<{
    name: string;
    displayName: string;
    default: string;
  }>;
}> {
  return http.get<any>('/auth/api/v1/sso/realms');
}

/**
 * SSO登录状态检查
 */
export async function checkSSOStatus(): Promise<{
  authenticated: boolean;
  provider: string;
  userInfo?: SSOUserInfo;
}> {
  try {
    const userInfo = await getSSOUserInfo();
    return {
      authenticated: true,
      provider: 'keycloak',
      userInfo,
    };
  } catch (error) {
    return {
      authenticated: false,
      provider: 'keycloak',
    };
  }
}

/**
 * 处理OAuth2授权码回调（简化版）
 * 用于前端处理回调并存储Token
 */
export async function processOAuthCallback(callbackUrl: string): Promise<SSOLoginResponse> {
  const url = new URL(callbackUrl, window.location.origin);
  const code = url.searchParams.get('code');
  const state = url.searchParams.get('state');
  const error = url.searchParams.get('error');
  const errorDescription = url.searchParams.get('error_description');

  if (error) {
    return {
      success: false,
      accessToken: '',
      refreshToken: '',
      tokenType: '',
      expiresIn: 0,
      message: errorDescription || error,
      provider: 'keycloak',
    };
  }

  if (!code) {
    return {
      success: false,
      accessToken: '',
      refreshToken: '',
      tokenType: '',
      expiresIn: 0,
      message: 'No authorization code received',
      provider: 'keycloak',
    };
  }

  // 使用state中存储的redirectUri
  const redirectUri = state ? decodeURIComponent(state) : window.location.origin;

  return handleSSOCallback(code, redirectUri);
}

/**
 * 清除SSO登录状态
 */
export function clearSSOSession(): void {
  localStorage.removeItem('sso_access_token');
  localStorage.removeItem('sso_refresh_token');
  localStorage.removeItem('sso_token_expires_at');
  localStorage.removeItem('sso_user_info');
}

/**
 * 获取SSO登录状态
 */
export function getSSOSession(): {
  accessToken: string | null;
  refreshToken: string | null;
  expiresAt: number | null;
} {
  return {
    accessToken: localStorage.getItem('sso_access_token'),
    refreshToken: localStorage.getItem('sso_refresh_token'),
    expiresAt: localStorage.getItem('sso_token_expires_at')
      ? parseInt(localStorage.getItem('sso_token_expires_at') || '0')
      : null,
  };
}

/**
 * 保存SSO登录状态
 */
export function saveSSOSession(response: SSOLoginResponse): void {
  localStorage.setItem('sso_access_token', response.accessToken);
  localStorage.setItem('sso_refresh_token', response.refreshToken);
  localStorage.setItem('sso_token_expires_at', String(Date.now() + response.expiresIn * 1000));
  if (response.userInfo) {
    localStorage.setItem('sso_user_info', JSON.stringify(response.userInfo));
  }
}

/**
 * 检查Token是否即将过期
 */
export function isTokenExpiringSoon(expiresInSeconds: number = 300): boolean {
  const session = getSSOSession();
  if (!session.expiresAt) return true;
  
  const timeUntilExpiry = session.expiresAt - Date.now();
  return timeUntilExpiry < expiresInSeconds * 1000;
}

export default {
  getSSOConfig,
  initiateSSOLogin,
  handleSSOCallback,
  getSSOUserInfo,
  ssoLogout,
  refreshSSOToken,
  validateToken,
  getAvailableRealms,
  checkSSOStatus,
  processOAuthCallback,
  clearSSOSession,
  getSSOSession,
  saveSSOSession,
  isTokenExpiringSoon,
};
