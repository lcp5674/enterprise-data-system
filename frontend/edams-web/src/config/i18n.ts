/**
 * i18n 配置文件
 */

import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import Backend from 'i18next-http-backend';

// 导入语言资源
import zhCNCommon from '../locales/zh-CN/common.json';
import zhCNMenu from '../locales/zh-CN/menu.json';
import zhCNLogin from '../locales/zh-CN/login.json';
import zhCNDashboard from '../locales/zh-CN/dashboard.json';
import enUSCommon from '../locales/en-US/common.json';
import enUSMenu from '../locales/en-US/menu.json';
import enUSLogin from '../locales/en-US/login.json';
import enUSDashboard from '../locales/en-US/dashboard.json';

// 定义资源结构
const resources = {
  'zh-CN': {
    common: zhCNCommon,
    menu: zhCNMenu,
    login: zhCNLogin,
    dashboard: zhCNDashboard,
  },
  'en-US': {
    common: enUSCommon,
    menu: enUSMenu,
    login: enUSLogin,
    dashboard: enUSDashboard,
  },
};

// 初始化 i18n
i18n
  .use(Backend)
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    fallbackLng: 'zh-CN', // 默认语言
    lng: localStorage.getItem('language') || 'zh-CN', // 初始语言
    interpolation: {
      escapeValue: false, // React 已经转义了
    },
    defaultNS: 'common', // 默认命名空间
    ns: ['common', 'menu', 'login', 'dashboard'], // 命名空间列表
    keySeparator: false, // 不使用key分隔符
    nsSeparator: false, // 命名空间分隔符
    debug: process.env.NODE_ENV === 'development', // 开发环境开启调试
    detection: {
      order: ['localStorage', 'navigator', 'htmlTag'],
      caches: ['localStorage'],
      lookupLocalStorage: 'language',
      htmlTag: document.documentElement,
    },
    react: {
      useSuspense: false, // 不使用 suspense
    },
  });

// 语言切换函数
export const changeLanguage = (lng: string) => {
  i18n.changeLanguage(lng);
  localStorage.setItem('language', lng);
  window.location.reload(); // 重新加载页面以应用 Ant Design 语言包
};

// 简化 t 函数别名
export const t = i18n.t;
export const { init } = i18n;

export default i18n;