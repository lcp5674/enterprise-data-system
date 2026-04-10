/**
 * 运行时配置
 */

import './config/i18n'; // 导入 i18n 配置
import { history } from '@umijs/max';
import { useAppStore } from './stores';

// 全局初始化数据
export async function getInitialState(): Promise<{
  currentUser?: API.CurrentUser;
  settings?: Partial<API.Settings>;
}> {
  // 这里可以初始化一些全局状态
  return {
    settings: {},
  };
}

// layout 运行时配置
export const layout = {
  // 菜单配置
  menu: {
    locale: true,
  },
};

// 请求运行时配置
export const request = {
  timeout: 10000,
  // 更多配置请参考：https://umijs.org/docs/max/request
};

// 路由运行时配置
export const onRouteChange = ({ location }: { location: any }) => {
  // 可以在这里进行页面访问权限控制
  console.log('路由变化:', location.pathname);
};

// 应用渲染前配置
export const render = (oldRender: () => void) => {
  // 确保 i18n 已初始化
  const language = localStorage.getItem('language') || 'zh-CN';
  localStorage.setItem('language', language);
  
  oldRender();
};

// 页面容器配置
export const rootContainer = (container: React.ReactElement) => {
  // 可以在这里包裹额外的 Provider
  
  return container;
};

// 错误处理
export const dva = {
  config: {
    onError(err: Error) {
      err.preventDefault();
      console.error(err);
    },
  },
};