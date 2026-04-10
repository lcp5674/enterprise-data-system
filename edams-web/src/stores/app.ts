/**
 * 应用状态管理
 */

import { create } from 'zustand';

interface AppState {
  // 侧边栏状态
  sidebarCollapsed: boolean;
  sidebarWidth: number;
  
  // 主题
  theme: 'light' | 'dark';
  
  // 语言
  language: string;
  
  // 全局加载状态
  globalLoading: boolean;
  
  // 通知数量
  notificationCount: number;
  
  // 操作方法
  toggleSidebar: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  setTheme: (theme: 'light' | 'dark') => void;
  setLanguage: (language: string) => void;
  setGlobalLoading: (loading: boolean) => void;
  setNotificationCount: (count: number) => void;
  incrementNotificationCount: () => void;
  decrementNotificationCount: () => void;
}

export const useAppStore = create<AppState>((set, get) => ({
  // 初始状态
  sidebarCollapsed: false,
  sidebarWidth: 220,
  theme: 'light',
  language: 'zh-CN',
  globalLoading: false,
  notificationCount: 0,

  // 切换侧边栏
  toggleSidebar: () => {
    const collapsed = !get().sidebarCollapsed;
    set({ sidebarCollapsed: collapsed });
    localStorage.setItem('sidebarCollapsed', String(collapsed));
  },

  // 设置侧边栏折叠状态
  setSidebarCollapsed: (collapsed) => {
    set({ sidebarCollapsed: collapsed });
    localStorage.setItem('sidebarCollapsed', String(collapsed));
  },

  // 设置主题
  setTheme: (theme) => {
    set({ theme });
    localStorage.setItem('theme', theme);
  },

  // 设置语言
  setLanguage: (language) => {
    set({ language });
    localStorage.setItem('language', language);
  },

  // 设置全局加载状态
  setGlobalLoading: (loading) => set({ globalLoading: loading }),

  // 设置通知数量
  setNotificationCount: (count) => set({ notificationCount: count }),

  // 增加通知数量
  incrementNotificationCount: () => {
    set({ notificationCount: get().notificationCount + 1 });
  },

  // 减少通知数量
  decrementNotificationCount: () => {
    const count = get().notificationCount;
    if (count > 0) {
      set({ notificationCount: count - 1 });
    }
  },
}));

export default useAppStore;
