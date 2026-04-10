/**
 * 忘记密码页面
 */

import React, { useState, useEffect } from 'react';
import { Form, Input, Button, Card, Typography, Steps, Space, message, Result } from 'antd';
import {
  UserOutlined,
  MailOutlined,
  LockOutlined,
  SafetyOutlined,
  ArrowLeftOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import { history, useNavigate, Link } from '@umijs/max';
import * as authService from '../../services/auth';
import styles from './ForgotPassword.less';

const { Title, Text, Paragraph } = Typography;

interface Step1FormValues {
  username: string;
}

interface Step2FormValues {
  email: string;
}

interface Step3FormValues {
  code: string;
}

interface Step4FormValues {
  password: string;
  confirmPassword: string;
}

const ForgotPassword: React.FC = () => {
  const navigate = useNavigate();
  const [form1] = Form.useForm<Step1FormValues>();
  const [form2] = Form.useForm<Step2FormValues>();
  const [form3] = Form.useForm<Step3FormValues>();
  const [form4] = Form.useForm<Step4FormValues>();

  const [currentStep, setCurrentStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [userEmail, setUserEmail] = useState('');
  const [resetToken, setResetToken] = useState('');
  const [verifyId, setVerifyId] = useState('');

  // 倒计时
  useEffect(() => {
    if (countdown > 0) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [countdown]);

  // 步骤 1: 验证用户名
  const handleVerifyUsername = async (values: Step1FormValues) => {
    setLoading(true);
    try {
      const result = await authService.verifyUsername(values.username);
      setUserEmail(result.email || '');
      setCurrentStep(1);
      message.success('用户名验证成功');
    } catch (error: any) {
      message.error(error.message || '用户名验证失败');
    } finally {
      setLoading(false);
    }
  };

  // 步骤 2: 发送验证码
  const handleSendCode = async (values: Step2FormValues) => {
    setLoading(true);
    try {
      const result = await authService.sendResetPasswordCode({
        email: values.email,
        username: form1.getFieldValue('username'),
      });
      setVerifyId(result.verifyId || '');
      setCurrentStep(2);
      setCountdown(60);
      message.success('验证码已发送到您的邮箱');
    } catch (error: any) {
      message.error(error.message || '发送验证码失败');
    } finally {
      setLoading(false);
    }
  };

  // 步骤 3: 验证验证码
  const handleVerifyCode = async (values: Step3FormValues) => {
    setLoading(true);
    try {
      const result = await authService.verifyResetCode({
        verifyId,
        code: values.code,
      });
      setResetToken(result.resetToken || '');
      setCurrentStep(3);
      message.success('验证码验证成功');
    } catch (error: any) {
      message.error(error.message || '验证码验证失败');
    } finally {
      setLoading(false);
    }
  };

  // 步骤 4: 重置密码
  const handleResetPassword = async (values: Step4FormValues) => {
    setLoading(true);
    try {
      await authService.resetPassword({
        resetToken,
        newPassword: values.password,
      });
      setCurrentStep(4);
      message.success('密码重置成功');
    } catch (error: any) {
      message.error(error.message || '密码重置失败');
    } finally {
      setLoading(false);
    }
  };

  // 返回上一步
  const handleBack = () => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1);
    } else {
      navigate('/user/login');
    }
  };

  const steps = [
    { title: '验证账号', icon: <UserOutlined /> },
    { title: '验证邮箱', icon: <MailOutlined /> },
    { title: '安全验证', icon: <SafetyOutlined /> },
    { title: '重置密码', icon: <LockOutlined /> },
  ];

  const renderStepContent = () => {
    switch (currentStep) {
      case 0:
        return (
          <Form
            form={form1}
            onFinish={handleVerifyUsername}
            size="large"
            className={styles.form}
          >
            <Form.Item
              name="username"
              rules={[{ required: true, message: '请输入用户名' }]}
            >
              <Input
                prefix={<UserOutlined />}
                placeholder="请输入用户名"
              />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} block>
                下一步
              </Button>
            </Form.Item>
          </Form>
        );

      case 1:
        return (
          <Form
            form={form2}
            onFinish={handleSendCode}
            size="large"
            className={styles.form}
          >
            <Paragraph type="secondary" className={styles.hint}>
              验证码将发送到您绑定的邮箱: {userEmail || '请确认您的邮箱'}
            </Paragraph>

            <Form.Item
              name="email"
              rules={[
                { required: true, message: '请输入邮箱' },
                { type: 'email', message: '请输入正确的邮箱格式' },
              ]}
            >
              <Input prefix={<MailOutlined />} placeholder="请输入绑定的邮箱" />
            </Form.Item>

            <Form.Item>
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button type="primary" htmlType="submit" loading={loading} block>
                  发送验证码
                </Button>
                <Button block onClick={handleBack}>
                  返回上一步
                </Button>
              </Space>
            </Form.Item>
          </Form>
        );

      case 2:
        return (
          <Form
            form={form3}
            onFinish={handleVerifyCode}
            size="large"
            className={styles.form}
          >
            <Paragraph type="secondary" className={styles.hint}>
              请输入发送到 {userEmail} 的验证码
            </Paragraph>

            <Form.Item
              name="code"
              rules={[
                { required: true, message: '请输入验证码' },
                { pattern: /^\d{6}$/, message: '验证码为6位数字' },
              ]}
            >
              <Input
                prefix={<SafetyOutlined />}
                placeholder="请输入6位验证码"
                maxLength={6}
              />
            </Form.Item>

            <Form.Item>
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button type="primary" htmlType="submit" loading={loading} block>
                  验证
                </Button>
                <Button
                  type="link"
                  disabled={countdown > 0}
                  onClick={() => handleSendCode({ email: userEmail })}
                  className={styles.resendButton}
                >
                  {countdown > 0 ? `${countdown}秒后重新发送` : '重新获取验证码'}
                </Button>
                <Button block onClick={handleBack}>
                  返回上一步
                </Button>
              </Space>
            </Form.Item>
          </Form>
        );

      case 3:
        return (
          <Form
            form={form4}
            onFinish={handleResetPassword}
            size="large"
            className={styles.form}
          >
            <Form.Item
              name="password"
              rules={[
                { required: true, message: '请输入新密码' },
                { min: 8, message: '密码至少8位' },
                {
                  pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/,
                  message: '密码需包含大小写字母和数字',
                },
              ]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="请输入新密码"
              />
            </Form.Item>

            <Form.Item
              name="confirmPassword"
              dependencies={['password']}
              rules={[
                { required: true, message: '请确认密码' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('password') === value) {
                      return Promise.resolve();
                    }
                    return Promise.reject(new Error('两次输入的密码不一致'));
                  },
                }),
              ]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="请确认新密码"
              />
            </Form.Item>

            <Form.Item>
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button type="primary" htmlType="submit" loading={loading} block>
                  重置密码
                </Button>
                <Button block onClick={handleBack}>
                  返回上一步
                </Button>
              </Space>
            </Form.Item>
          </Form>
        );

      case 4:
        return (
          <Result
            icon={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
            title="密码重置成功"
            subTitle="请使用新密码登录您的账号"
            extra={[
              <Button type="primary" key="login" onClick={() => navigate('/user/login')}>
                返回登录
              </Button>,
              <Link to="/" key="home">
                <Button>返回首页</Button>
              </Link>,
            ]}
          />
        );

      default:
        return null;
    }
  };

  return (
    <div className={styles.container}>
      <Card className={styles.card}>
        <div className={styles.header}>
          <Button
            type="text"
            icon={<ArrowLeftOutlined />}
            onClick={handleBack}
            className={styles.backButton}
          >
            {currentStep === 0 ? '返回登录' : '返回'}
          </Button>
        </div>

        <div className={styles.content}>
          <Title level={3} className={styles.title}>
            找回密码
          </Title>

          {currentStep < 4 && (
            <>
              <Steps
                current={currentStep}
                items={steps}
                className={styles.steps}
                size="small"
              />
              <div className={styles.stepContent}>{renderStepContent()}</div>
            </>
          )}

          {currentStep === 4 && renderStepContent()}
        </div>
      </Card>
    </div>
  );
};

export default ForgotPassword;
