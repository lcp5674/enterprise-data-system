/**
 * SSO回调页面
 * 处理OAuth2授权码回调
 */

import React, { useEffect, useState } from 'react';
import { history, useModel } from '@umijs/max';
import { Result, Button, Card, Spin, Typography, Space } from 'antd';
import { CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import { processOAuthCallback, saveSSOSession } from '@/services/sso';
import type { SSOLoginResponse } from '@/services/sso';
import './SSOCallback.less';

const { Title, Text, Paragraph } = Typography;

const SSOCallback: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [result, setResult] = useState<'success' | 'error' | null>(null);
  const [message, setMessage] = useState<string>('');
  const { setInitialState } = useModel('@@initialState');

  useEffect(() => {
    handleCallback();
  }, []);

  const handleCallback = async () => {
    try {
      // 获取回调URL
      const callbackUrl = window.location.href;
      
      // 处理授权码回调
      const response: SSOLoginResponse = await processOAuthCallback(callbackUrl);

      if (response.success) {
        // 保存会话
        saveSSOSession(response);

        // 更新全局状态
        if (response.userInfo) {
          await setInitialState((prev: any) => ({
            ...prev,
            currentUser: {
              ...prev.currentUser,
              ...response.userInfo,
              accessToken: response.accessToken,
            },
          }));
        }

        setResult('success');
        setMessage('SSO登录成功！正在跳转...');

        // 延迟跳转
        setTimeout(() => {
          const redirectUrl = sessionStorage.getItem('sso_redirect_url') || '/';
          sessionStorage.removeItem('sso_redirect_url');
          history.push(redirectUrl);
        }, 1500);
      } else {
        setResult('error');
        setMessage(response.message || 'SSO登录失败');
      }
    } catch (e: any) {
      setResult('error');
      setMessage(e.message || '处理回调失败');
    }
    setLoading(false);
  };

  const handleRetry = () => {
    history.push('/sso/login');
  };

  const handleBackToLogin = () => {
    history.push('/user/login');
  };

  return (
    <div className="sso-callback-container">
      <Card className="sso-callback-card" bordered={false}>
        {loading ? (
          <div className="sso-callback-loading">
            <Spin size="large" />
            <Title level={4}>正在处理登录...</Title>
            <Paragraph type="secondary">
              请稍候，正在验证您的身份
            </Paragraph>
          </div>
        ) : result === 'success' ? (
          <Result
            icon={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
            title="登录成功"
            subTitle={message}
            extra={
              <Button type="primary" onClick={() => history.push('/')}>
                前往首页
              </Button>
            }
          />
        ) : (
          <Result
            icon={<CloseCircleOutlined style={{ color: '#ff4d4f' }} />}
            title="登录失败"
            subTitle={message}
            extra={
              <Space>
                <Button type="primary" onClick={handleRetry}>
                  重试
                </Button>
                <Button onClick={handleBackToLogin}>
                  返回登录页
                </Button>
              </Space>
            }
          />
        )}
      </Card>
    </div>
  );
};

export default SSOCallback;
