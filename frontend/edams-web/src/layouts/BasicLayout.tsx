/**
 * 基础布局组件
 */

import React, { useEffect } from 'react';
import { Outlet, useNavigate, history } from '@umijs/max';
import { ConfigProvider, Layout, Menu, Avatar, Dropdown, Badge, Space, Typography } from 'antd';
import {
  HomeOutlined,
  DatabaseOutlined,
  FolderOutlined,
  ShareAltOutlined,
  CheckCircleOutlined,
  SettingOutlined,
  BellOutlined,
  UserOutlined,
  LogoutOutlined,
  GlobalOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons';
import zhCN from 'antd/locale/zh_CN';
import enUS from 'antd/locale/en_US';
import { useAuthStore, useAppStore } from '../stores';
import { useMenuI18n, useCommonI18n } from '../hooks/useI18n';
import styles from './BasicLayout.less';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

const BasicLayout: React.FC = () => {
  const navigate = useNavigate();
  const { user, logout, initialize, isAuthenticated } = useAuthStore();
  const { sidebarCollapsed, toggleSidebar, language, notificationCount, setLanguage } = useAppStore();
  const { t: tMenu } = useMenuI18n();
  const { t: tCommon } = useCommonI18n();

  // 菜单配置
  const menuItems = [
    {
      key: '/home',
      icon: <HomeOutlined />,
      label: tMenu('dashboard'),
    },
    {
      key: '/assets',
      icon: <DatabaseOutlined />,
      label: tMenu('asset.title'),
      children: [
        { key: '/assets/list', label: tMenu('asset.list') },
        { key: '/assets/favorites', label: tMenu('asset.favorites') },
        { key: '/assets/create', label: tMenu('asset.create') },
      ],
    },
    {
      key: '/catalog',
      icon: <FolderOutlined />,
      label: tMenu('catalog.title'),
      children: [
        { key: '/catalog/tree', label: tMenu('catalog.tree') },
        { key: '/catalog/domain', label: tMenu('catalog.domain') },
      ],
    },
    {
      key: '/lineage',
      icon: <ShareAltOutlined />,
      label: tMenu('lineage.title'),
      children: [
        { key: '/lineage/graph', label: tMenu('lineage.graph') },
        { key: '/lineage/impact', label: tMenu('lineage.impact') },
      ],
    },
    {
      key: '/quality',
      icon: <CheckCircleOutlined />,
      label: tMenu('quality.title'),
      children: [
        { key: '/quality/overview', label: tMenu('quality.overview') },
        { key: '/quality/rules', label: tMenu('quality.rules') },
        { key: '/quality/issues', label: tMenu('quality.issues') },
      ],
    },
    {
      key: '/system',
      icon: <SettingOutlined />,
      label: tMenu('system.title'),
      children: [
        { key: '/system/users', label: tMenu('system.users') },
        { key: '/system/roles', label: tMenu('system.roles') },
        { key: '/system/datasources', label: tMenu('system.datasources') },
      ],
    },
  ];

  // 初始化认证状态
  useEffect(() => {
    initialize();
  }, [initialize]);

  // 检查认证状态
  useEffect(() => {
    if (!isAuthenticated) {
      history.push({
        pathname: '/user/login',
        query: { redirect: history.location.pathname },
      });
    }
  }, [isAuthenticated]);

  // 处理菜单点击
  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key);
  };

  // 获取当前选中菜单
  const getSelectedKeys = () => {
    const path = history.location.pathname;
    const selectedKeys: string[] = [];
    const openKeys: string[] = [];

    menuItems.forEach((item) => {
      if (path.startsWith(item.key)) {
        openKeys.push(item.key);
        if (item.children) {
          item.children.forEach((child) => {
            if (path === child.key) {
              selectedKeys.push(child.key);
            }
          });
        } else {
          selectedKeys.push(item.key);
        }
      }
    });

    return { selectedKeys, openKeys };
  };

  // 用户下拉菜单
  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: tMenu('user.profile'),
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: tMenu('user.settings'),
    },
    { type: 'divider' as const },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: tMenu('user.logout'),
      danger: true,
    },
  ];

  const handleUserMenuClick = async ({ key }: { key: string }) => {
    switch (key) {
      case 'profile':
        navigate('/profile');
        break;
      case 'settings':
        navigate('/settings');
        break;
      case 'logout':
        await logout();
        navigate('/user/login');
        break;
    }
  };

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
        {/* 侧边栏 */}
        <Sider
          trigger={null}
          collapsible
          collapsed={sidebarCollapsed}
          width={220}
          className={styles.sider}
        >
          {/* Logo */}
          <div className={styles.logo}>
            {!sidebarCollapsed ? (
              <div className={styles.logoText}>
                <DatabaseOutlined className={styles.logoIcon} />
                <span>{tCommon('app.title')}</span>
              </div>
            ) : (
              <DatabaseOutlined className={styles.logoIconCollapsed} />
            )}
          </div>

          {/* 菜单 */}
          <Menu
            mode="inline"
            theme="dark"
            selectedKeys={getSelectedKeys().selectedKeys}
            defaultOpenKeys={getSelectedKeys().openKeys}
            items={menuItems}
            onClick={handleMenuClick}
            className={styles.menu}
          />
        </Sider>

        <Layout>
          {/* 头部 */}
          <Header className={styles.header}>
            <div className={styles.headerLeft}>
              {React.createElement(
                sidebarCollapsed ? MenuUnfoldOutlined : MenuFoldOutlined,
                {
                  className: styles.trigger,
                  onClick: toggleSidebar,
                },
              )}
            </div>

            <div className={styles.headerRight}>
              {/* 通知 */}
              <Badge count={notificationCount} size="small">
                <BellOutlined
                  className={styles.headerIcon}
                  onClick={() => navigate(tMenu('system.notifications') ? '/notifications' : '/system')}
                />
              </Badge>

              {/* 语言切换 */}
              <GlobalOutlined className={styles.headerIcon} onClick={handleLanguageChange} />

              {/* 用户信息 */}
              <Dropdown
                menu={{ items: userMenuItems, onClick: handleUserMenuClick }}
                placement="bottomRight"
                trigger={['click']}
              >
                <Space className={styles.userInfo}>
                  <Avatar size={32} icon={<UserOutlined />} src={user?.avatar} />
                  {!sidebarCollapsed && (
                    <Text className={styles.username}>{user?.nickname || user?.username}</Text>
                  )}
                </Space>
              </Dropdown>
            </div>
          </Header>

          {/* 内容区域 */}
          <Content className={styles.content}>
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </ConfigProvider>
  );
};

export default BasicLayout;