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
import styles from './BasicLayout.less';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

// 菜单配置
const menuItems = [
  {
    key: '/home',
    icon: <HomeOutlined />,
    label: '工作台',
  },
  {
    key: '/assets',
    icon: <DatabaseOutlined />,
    label: '资产管理',
    children: [
      { key: '/assets/list', label: '资产列表' },
      { key: '/assets/favorites', label: '我的收藏' },
      { key: '/assets/create', label: '注册资产' },
    ],
  },
  {
    key: '/catalog',
    icon: <FolderOutlined />,
    label: '数据目录',
    children: [
      { key: '/catalog/tree', label: '目录树' },
      { key: '/catalog/domain', label: '业务域' },
    ],
  },
  {
    key: '/lineage',
    icon: <ShareAltOutlined />,
    label: '数据地图',
    children: [
      { key: '/lineage/graph', label: '血缘图' },
      { key: '/lineage/impact', label: '影响分析' },
    ],
  },
  {
    key: '/quality',
    icon: <CheckCircleOutlined />,
    label: '质量管理',
    children: [
      { key: '/quality/overview', label: '质量概览' },
      { key: '/quality/rules', label: '质量规则' },
      { key: '/quality/issues', label: '问题追踪' },
    ],
  },
  {
    key: '/system',
    icon: <SettingOutlined />,
    label: '系统管理',
    children: [
      { key: '/system/users', label: '用户管理' },
      { key: '/system/roles', label: '角色权限' },
      { key: '/system/datasources', label: '数据源配置' },
    ],
  },
];

const BasicLayout: React.FC = () => {
  const navigate = useNavigate();
  const { user, logout, initialize, isAuthenticated } = useAuthStore();
  const { sidebarCollapsed, toggleSidebar, language, notificationCount } = useAppStore();

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
      label: '个人中心',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '账号设置',
    },
    { type: 'divider' as const },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
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
                <span>数据资产管理系统</span>
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
                  onClick={() => navigate('/notifications')}
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
