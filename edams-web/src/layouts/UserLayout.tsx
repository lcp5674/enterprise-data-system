/**
 * 用户布局（用于登录、注册等页面）
 */

import React from 'react';
import { Outlet, Link, useLocation } from '@umijs/max';
import { ConfigProvider, Layout, Typography, Space } from 'antd';
import { DatabaseOutlined, GlobalOutlined } from '@ant-design/icons';
import zhCN from 'antd/locale/zh_CN';
import enUS from 'antd/locale/en_US';
import { useAppStore } from '../stores';
import { useCommonI18n } from '../hooks/useI18n';
import styles from './UserLayout.less';

const { Content } = Layout;
const { Text } = Typography;

const UserLayout: React.FC = () => {
  const location = useLocation();
  const isLogin = location.pathname === '/user/login';
  const { language, setLanguage } = useAppStore();
  const { t: tCommon } = useCommonI18n();

  // 切换语言
  const handleLanguageChange = () => {
    const newLanguage = language === 'zh-CN' ? 'en-US' : 'zh-CN';
    setLanguage(newLanguage);
    localStorage.setItem('language', newLanguage);
    window.location.reload();
  };

  // 语言配置
  const locale = language === 'zh-CN' ? zhCN : enUS;

  return (
    <ConfigProvider locale={locale}>
      <Layout className={styles.layout}>
        <div className={styles.background}>
          <div className={styles.backgroundShape1} />
          <div className={styles.backgroundShape2} />
          <div className={styles.backgroundShape3} />
        </div>

        <Content className={styles.content}>
          <div className={styles.container}>
            {/* Logo */}
            <div className={styles.logo}>
              <DatabaseOutlined className={styles.logoIcon} />
              <span className={styles.logoText}>{tCommon('app.title')}</span>
            </div>

            {/* 子页面内容 */}
            <div className={styles.main}>
              <Outlet />
            </div>

            {/* 语言切换 */}
            <div className={styles.languageSwitch}>
              <GlobalOutlined className={styles.languageIcon} onClick={handleLanguageChange} />
              <span className={styles.languageText}>
                {language === 'zh-CN' ? 'English' : '中文'}
              </span>
            </div>

            {/* 页脚 */}
            <div className={styles.footer}>
              <Space split={<span className={styles.footerDivider}>|</span>}>
                <Text className={styles.footerLink}>帮助</Text>
                <Text className={styles.footerLink}>隐私</Text>
                <Text className={styles.footerLink}>条款</Text>
              </Space>
              <Text className={styles.copyright}>{tCommon('app.copyright')}</Text>
            </div>
          </div>
        </Content>
      </Layout>
    </ConfigProvider>
  );
};

export default UserLayout;
