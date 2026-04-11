/**
 * SSO单点登录页面
 * 支持Keycloak/OAuth2登录
 */

import React, { useEffect, useState } from 'react';
import { history, useModel } from '@umijs/max';
import { Alert, Button, Card, Spin, Space, Typography, Divider, message } from 'antd';
import {
  SafetyCertificateOutlined,
  UserOutlined,
  LogoutOutlined,
  KeyOutlined,
  GlobalOutlined,
} from '@ant-design/icons';
import { getSSOConfig, initiateSSOLogin, getSSOUserInfo, ssoLogout, getSSOSession, saveSSOSession } from '@/services/sso';
import type { SSOConfig, SSOUserInfo } from '@/services/sso';
import './SSOLogin.less';

const { Title, Text, Paragraph } = Typography;

const SSOLogin: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [config, setConfig] = useState<SSOConfig | null>(null);
  const [userInfo, setUserInfo] = useState<SSOUserInfo | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    checkSSOStatus();
  }, []);

  const checkSSOStatus = async () => {
    try {
      // 获取SSO配置
      const ssoConfig = await getSSOConfig();
      setConfig(ssoConfig);

      // 检查当前会话
      const session = getSSOSession();
      if (session.accessToken) {
        try {
          const info = await getSSOUserInfo();
          setUserInfo(info);
        } catch (e) {
          // Token可能过期
          console.log('Token may be expired');
        }
      }
    } catch (e: any) {
      setError(e.message || 'Failed to load SSO configuration');
    }
  };

  const handleSSOLogin = async () => {
    setLoading(true);
    setError(null);

    try {
      const redirectUri = encodeURIComponent(window.location.origin + '/sso/callback');
      const state = encodeURIComponent(redirectUri); // 使用redirectUri作为state

      const response = await initiateSSOLogin(redirectUri, state);

      if (response.success && response.authorizationUri) {
        // 跳转到Keycloak登录页面
        window.location.href = response.authorizationUri;
      } else {
        setError('Failed to initiate SSO login');
      }
    } catch (e: any) {
      setError(e.message || 'SSO login failed');
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    setLoading(true);
    try {
      const session = getSSOSession();
      await ssoLogout();
      localStorage.removeItem('sso_access_token');
      localStorage.removeItem('sso_refresh_token');
      localStorage.removeItem('sso_token_expires_at');
      localStorage.removeItem('sso_user_info');
      setUserInfo(null);
      message.success('Logged out successfully');
    } catch (e: any) {
      message.error(e.message || 'Logout failed');
    }
    setLoading(false);
  };

  const handleContinue = () => {
    // 将用户重定向到之前页面或首页
    history.push('/');
  };

  // 渲染已登录状态
  if (userInfo) {
    return (
      <div className="sso-login-container">
        <Card className="sso-login-card" bordered={false}>
          <div className="sso-header">
            <SafetyCertificateOutlined className="sso-icon success" />
            <Title level={3}>SSO登录成功</Title>
          </div>

          <div className="sso-user-info">
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <div className="user-info-item">
                <UserOutlined />
                <Text strong>用户名：</Text>
                <Text>{userInfo.username}</Text>
              </div>
              <div className="user-info-item">
                <KeyOutlined />
                <Text strong>邮箱：</Text>
                <Text>{userInfo.email}</Text>
              </div>
              {userInfo.firstName && (
                <div className="user-info-item">
                  <GlobalOutlined />
                  <Text strong>姓名：</Text>
                  <Text>{userInfo.firstName} {userInfo.lastName}</Text>
                </div>
              )}
              <div className="user-info-item">
                <SafetyCertificateOutlined />
                <Text strong>角色：</Text>
                <Text>{userInfo.roles?.join(', ') || 'N/A'}</Text>
              </div>
            </Space>
          </div>

          <Divider />

          <Space style={{ width: '100%', justifyContent: 'center' }}>
            <Button type="primary" onClick={handleContinue} size="large">
              继续使用
            </Button>
            <Button onClick={handleLogout} size="large">
              <LogoutOutlined /> 登出
            </Button>
          </Space>
        </Card>
      </div>
    );
  }

  // 渲染未登录状态
  return (
    <div className="sso-login-container">
      <Card className="sso-login-card" bordered={false}>
        <div className="sso-header">
          <SafetyCertificateOutlined className="sso-icon" />
          <Title level={3}>企业单点登录 (SSO)</Title>
        </div>

        <Paragraph className="sso-description">
          使用企业账号通过Keycloak单点登录系统
        </Paragraph>

        {error && (
          <Alert
            type="error"
            message={error}
            showIcon
            closable
            style={{ marginBottom: 24 }}
          />
        )}

        {loading ? (
          <div className="sso-loading">
            <Spin size="large" />
            <Text>正在准备登录...</Text>
          </div>
        ) : (
          <div className="sso-actions">
            <Button
              type="primary"
              size="large"
              block
              icon={<SafetyCertificateOutlined />}
              onClick={handleSSOLogin}
              className="sso-login-btn"
            >
              企业账号登录
            </Button>

            {config && (
              <div className="sso-info">
                <Text type="secondary">
                  登录后将使用您的企业邮箱账号：{config.realm}
                </Text>
              </div>
            )}
          </div>
        )}

        <Divider plain>或</Divider>

        <div className="sso-alternative">
          <Button type="link" onClick={() => history.push('/user/login')}>
            使用本地账号登录
          </Button>
        </div>

        <div className="sso-footer">
          <Text type="secondary">
            遇到问题？请联系系统管理员
          </Text>
        </div>
      </Card>
    </div>
  );
};

export default SSOLogin;
