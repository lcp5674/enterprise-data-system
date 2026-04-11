/**
 * i18n 自定义钩子
 */

import { useTranslation } from 'react-i18next';
import { useAppStore } from '../stores';

export const useI18n = (namespace?: string) => {
  const { language, setLanguage } = useAppStore();
  const translation = useTranslation(namespace ? [namespace, 'common'] : ['common']);
  
  const { t, i18n } = translation;

  // 切换语言
  const changeLanguage = (lng: string) => {
    i18n.changeLanguage(lng);
    setLanguage(lng);
    localStorage.setItem('language', lng);
    window.location.reload(); // 重新加载页面以应用 Ant Design 语言包
  };

  // 获取当前语言
  const currentLanguage = i18n.language || language;

  return {
    t,
    i18n,
    changeLanguage,
    currentLanguage,
    isChinese: currentLanguage === 'zh-CN',
    isEnglish: currentLanguage === 'en-US',
    ...translation,
  };
};

// 命名空间别名的钩子
export const useMenuI18n = () => useI18n('menu');
export const useLoginI18n = () => useI18n('login');
export const useDashboardI18n = () => useI18n('dashboard');
export const useCommonI18n = () => useI18n();

export default useI18n;