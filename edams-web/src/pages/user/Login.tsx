/**
 * 登录页面
 */

import React, { useState, useEffect } from 'react';
import { Form, Input, Button, Checkbox, Tabs, Divider, message, Space, Typography } from 'antd';
import {
  UserOutlined,
  LockOutlined,
  MobileOutlined,
  SafetyOutlined,
  WechatOutlined,
  DingdingOutlined,
} from '@ant-design/icons';
import { history, useNavigate } from '@umijs/max';
import type { TabsProps } from 'antd';
import { useAuthStore } from '../../stores';
import * as authService from '../../services/auth';
import styles from './Login.less';

const { Title, Text, Link } = Typography;

interface LoginFormValues {
  username: string;
  password: string;
  remember: boolean;
}

interface MobileFormValues {
  mobile: string;
  code: string;
}

const Login: React.FC = () => {
  const navigate = useNavigate();
  const { login, setMFARequired } = useAuthStore();
  const [form] = Form.useForm();
  const [mobileForm] = Form.useForm();

  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('account');
  const [captchaLoading, setCaptchaLoading] = useState(false);
  const [captchaImage, setCaptchaImage] = useState('');
  const [captchaId, setCaptchaId] = useState('');
  const [codeLoading, setCodeLoading] = useState(false);
  const [codeCountdown, setCodeCountdown] = useState(0);

  // 获取图形验证码
  const fetchCaptcha = async () => {
    setCaptchaLoading(true);
    try {
      const result = await authService.getCaptcha();
      setCaptchaImage(result.captchaImage);
      setCaptchaId(result.captchaId);
    } catch (error) {
      console.error('获取验证码失败:', error);
    } finally {
      setCaptchaLoading(false);
    }
  };

  useEffect(() => {
    fetchCaptcha();
  }, []);

  // 发送手机验证码
  const handleSendCode = async () => {
    try {
      const mobile = mobileForm.getFieldValue('mobile');
      if (!mobile) {
        message.error('请输入手机号');
        return;
      }

      setCodeLoading(true);
      await authService.sendMobileCode({
        mobile,
        captchaId,
        scene: 'LOGIN',
      });

      message.success('验证码已发送');
      setCodeCountdown(60);

      const timer = setInterval(() => {
        setCodeCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (error: any) {
      message.error(error.message || '发送验证码失败');
      fetchCaptcha();
    } finally {
      setCodeLoading(false);
    }
  };

  // 账号密码登录
  const handleAccountLogin = async (values: LoginFormValues) => {
    setLoading(true);
    try {
      await login({
        loginType: 'PASSWORD',
        username: values.username,
        password: values.password,
        loginSource: 'WEB',
      });

      message.success('登录成功');

      // 跳转目标页面
      const { redirect } = history.location.query as { redirect?: string };
      navigate(redirect || '/home');
    } catch (error: any) {
      message.error(error.message || '登录失败');
      fetchCaptcha();
    } finally {
      setLoading(false);
    }
  };

  // 手机验证码登录
  const handleMobileLogin = async (values: MobileFormValues) => {
    setLoading(true);
    try {
      const response = await authService.verifyMobileCode({
        mobile: values.mobile,
        code: values.code,
      });

      // 存储Token
      await login({
        loginType: 'MOBILE_CODE',
        mobile: values.mobile,
      } as any);

      message.success('登录成功');

      const { redirect } = history.location.query as { redirect?: string };
      navigate(redirect || '/home');
    } catch (error: any) {
      message.error(error.message || '登录失败');
    } finally {
      setLoading(false);
    }
  };

  // 第三方登录
  const handleThirdPartyLogin = async (provider: 'wxwork' | 'dingtalk') => {
    try {
      const result = await authService.getSSOUrl({
        provider,
        redirectUri: window.location.origin + '/user/login/callback',
      });

      // 跳转到第三方登录页面
      window.location.href = result.url;
    } catch (error) {
      message.error('获取登录地址失败');
    }
  };

  // Tab 配置
  const tabItems: TabsProps['items'] = [
    {
      key: 'account',
      label: (
        <span>
          <UserOutlined /> 账号密码
        </span>
      ),
      children: (
        <Form
          form={form}
          name="accountLogin"
          onFinish={handleAccountLogin}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名 / 邮箱 / 手机号"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="请输入密码"
            />
          </Form.Item>

          <Form.Item>
            <Space style={{ width: '100%', justifyContent: 'space-between' }}>
              <Form.Item name="remember" valuePropName="checked" noStyle>
                <Checkbox>记住我</Checkbox>
              </Form.Item>
              <Link to="/user/forgot-password">忘记密码？</Link>
            </Space>
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              className={styles.submitButton}
            >
              登录
            </Button>
          </Form.Item>
        </Form>
      ),
    },
    {
      key: 'mobile',
      label: (
        <span>
          <MobileOutlined /> 手机验证码
        </span>
      ),
      children: (
        <Form
          form={mobileForm}
          name="mobileLogin"
          onFinish={handleMobileLogin}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="mobile"
            rules={[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' },
            ]}
          >
            <Input prefix={<MobileOutlined />} placeholder="请输入手机号" />
          </Form.Item>

          <Form.Item
            name="code"
            rules={[{ required: true, message: '请输入验证码' }]}
          >
            <Space.Compact style={{ width: '100%' }}>
              <Input
                prefix={<SafetyOutlined />}
                placeholder="请输入验证码"
                style={{ flex: 1 }}
              />
              <Button
                onClick={handleSendCode}
                loading={codeLoading}
                disabled={codeCountdown > 0}
                style={{ width: 120 }}
              >
                {codeCountdown > 0 ? `${codeCountdown}秒后重试` : '获取验证码'}
              </Button>
            </Space.Compact>
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              className={styles.submitButton}
            >
              登录
            </Button>
          </Form.Item>
        </Form>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      <Title level={2} className={styles.title}>
        欢迎回来
      </Title>
      <Text className={styles.subtitle}>请登录您的账号继续使用</Text>

      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        items={tabItems}
        className={styles.tabs}
      />

      <Divider plain>其他登录方式</Divider>

      <Space size={16} className={styles.thirdParty}>
        <Button
          icon={<WechatOutlined />}
          size="large"
          shape="circle"
          onClick={() => handleThirdPartyLogin('wxwork')}
        />
        <Button
          icon={<DingdingOutlined />}
          size="large"
          shape="circle"
          onClick={() => handleThirdPartyLogin('dingtalk')}
        />
      </Space>

      <div className={styles.register}>
        <Text>还没有账号？</Text>
        <Link to="/user/register">立即注册</Link>
      </div>
    </div>
  );
};

export default Login;
