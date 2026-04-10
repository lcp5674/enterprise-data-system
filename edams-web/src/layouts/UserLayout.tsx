/**
 * 用户布局（用于登录、注册等页面）
 */

import React from 'react';
import { Outlet, Link, useLocation } from '@umijs/max';
import { ConfigProvider, Layout, Typography, Space } from 'antd';
import { DatabaseOutlined } from '@ant-design/icons';
import zhCN from 'antd/locale/zh_CN';
import styles from './UserLayout.less';

const { Content } = Layout;
const { Text } = Typography;

const UserLayout: React.FC = () => {
  const location = useLocation();
  const isLogin = location.pathname === '/user/login';

  return (
    <ConfigProvider locale={zhCN}>
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
              <span className={styles.logoText}>企业数据资产管理系统</span>
            </div>

            {/* 子页面内容 */}
            <div className={styles.main}>
              <Outlet />
            </div>

            {/* 页脚 */}
            <div className={styles.footer}>
              <Space split={<span className={styles.footerDivider}>|</span>}>
                <Text className={styles.footerLink}>帮助</Text>
                <Text className={styles.footerLink}>隐私</Text>
                <Text className={styles.footerLink}>条款</Text>
              </Space>
              <Text className={styles.copyright}>© 2024 企业数据资产管理系统 版权所有</Text>
            </div>
          </div>
        </Content>
      </Layout>
    </ConfigProvider>
  );
};

export default UserLayout;
