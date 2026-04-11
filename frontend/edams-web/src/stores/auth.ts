/**
 * 认证状态管理
 */

import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User, MenuItem, UserConfig } from '../types';
import { STORAGE_KEYS } from '../constants';
import * as authService from '../services/auth';

interface AuthState {
  // 认证状态
  isAuthenticated: boolean;
  token: string | null;
  refreshToken: string | null;
  user: User | null;
  menus: MenuItem[];
  config: UserConfig;
  
  // MFA状态
  mfaRequired: boolean;
  mfaPending: boolean;
  
  // 加载状态
  loading: boolean;
  initialized: boolean;
  
  // 操作方法
  login: (params: Parameters<typeof authService.login>[0]) => Promise<void>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<void>;
  setUser: (user: User) => void;
  setMenus: (menus: MenuItem[]) => void;
  setConfig: (config: UserConfig) => void;
  setMFARequired: (required: boolean) => void;
  setMFAPending: (pending: boolean) => void;
  checkAuth: () => Promise<boolean>;
  initialize: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // 初始状态
      isAuthenticated: false,
      token: null,
      refreshToken: null,
      user: null,
      menus: [],
      config: {
        theme: 'light',
        language: 'zh-CN',
        timezone: 'Asia/Shanghai',
      },
      mfaRequired: false,
      mfaPending: false,
      loading: false,
      initialized: false,

      // 登录
      login: async (params) => {
        set({ loading: true });
        try {
          const response = await authService.login(params);
          
          // 存储Token
          localStorage.setItem(STORAGE_KEYS.TOKEN, response.accessToken);
          localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, response.refreshToken);
          localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(response.user));
          localStorage.setItem(STORAGE_KEYS.MENUS, JSON.stringify(response.menus));
          
          // 更新状态
          set({
            isAuthenticated: true,
            token: response.accessToken,
            refreshToken: response.refreshToken,
            user: response.user,
            menus: response.menus,
            config: response.config,
            loading: false,
            mfaRequired: false,
            mfaPending: false,
          });
        } catch (error) {
          set({ loading: false });
          throw error;
        }
      },

      // 登出
      logout: async () => {
        set({ loading: true });
        try {
          await authService.logout();
        } catch (error) {
          console.error('Logout error:', error);
        } finally {
          // 清除存储
          localStorage.removeItem(STORAGE_KEYS.TOKEN);
          localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.USER_INFO);
          localStorage.removeItem(STORAGE_KEYS.MENUS);
          
          set({
            isAuthenticated: false,
            token: null,
            refreshToken: null,
            user: null,
            menus: [],
            loading: false,
            mfaRequired: false,
            mfaPending: false,
          });
        }
      },

      // 刷新Token
      refreshToken: async () => {
        const { refreshToken: currentRefreshToken } = get();
        if (!currentRefreshToken) {
          throw new Error('No refresh token');
        }
        
        try {
          const response = await authService.refreshToken(currentRefreshToken);
          
          localStorage.setItem(STORAGE_KEYS.TOKEN, response.accessToken);
          localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, response.refreshToken);
          
          set({
            token: response.accessToken,
            refreshToken: response.refreshToken,
          });
        } catch (error) {
          // Token刷新失败，清除认证状态
          get().logout();
          throw error;
        }
      },

      // 设置用户信息
      setUser: (user) => {
        localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(user));
        set({ user });
      },

      // 设置菜单
      setMenus: (menus) => {
        localStorage.setItem(STORAGE_KEYS.MENUS, JSON.stringify(menus));
        set({ menus });
      },

      // 设置配置
      setConfig: (config) => {
        set({ config });
      },

      // 设置MFA需求
      setMFARequired: (required) => set({ mfaRequired: required }),

      // 设置MFA待处理
      setMFAPending: (pending) => set({ mfaPending: pending }),

      // 检查认证状态
      checkAuth: async () => {
        const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
        const userStr = localStorage.getItem(STORAGE_KEYS.USER_INFO);
        
        if (!token || !userStr) {
          return false;
        }

        try {
          // 验证会话
          const session = await authService.getSession();
          set({
            isAuthenticated: true,
            token,
            user: session.user,
            initialized: true,
          });
          return true;
        } catch (error) {
          // 会话验证失败，尝试刷新Token
          try {
            await get().refreshToken();
            return true;
          } catch {
            // 刷新也失败，清除认证状态
            get().logout();
            return false;
          }
        }
      },

      // 初始化
      initialize: async () => {
        if (get().initialized) return;
        
        const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
        const userStr = localStorage.getItem(STORAGE_KEYS.USER_INFO);
        const menusStr = localStorage.getItem(STORAGE_KEYS.MENUS);
        
        if (token && userStr) {
          try {
            const user = JSON.parse(userStr);
            const menus = menusStr ? JSON.parse(menusStr) : [];
            
            set({
              isAuthenticated: true,
              token,
              user,
              menus,
              initialized: true,
            });

            // 后台验证会话
            await get().checkAuth();
          } catch (error) {
            console.error('Initialize error:', error);
            set({ initialized: true });
          }
        } else {
          set({ initialized: true });
        }
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        isAuthenticated: state.isAuthenticated,
        token: state.token,
        refreshToken: state.refreshToken,
        user: state.user,
        menus: state.menus,
        config: state.config,
      }),
    },
  ),
);

export default useAuthStore;
