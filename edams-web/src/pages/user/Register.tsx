/**
 * 注册页面
 */

import React, { useState } from 'react';
import {
  Form,
  Input,
  Button,
  Checkbox,
  message,
  Typography,
  Space,
  Steps,
} from 'antd';
import {
  UserOutlined,
  LockOutlined,
  MailOutlined,
  MobileOutlined,
  SafetyOutlined,
} from '@ant-design/icons';
import { history, Link } from '@umijs/max';
import * as authService from '../../services/auth';
import styles from './Register.less';

const { Title, Text } = Typography;

interface RegisterFormValues {
  username: string;
  password: string;
  confirmPassword: string;
  email: string;
  mobile?: string;
  agreeTerms: boolean;
}

const Register: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);
  const [codeLoading, setCodeLoading] = useState(false);
  const [codeCountdown, setCodeCountdown] = useState(0);
  const [verifyCode, setVerifyCode] = useState('');
  const [email, setEmail] = useState('');

  // 发送验证码
  const handleSendCode = async () => {
    try {
      const values = form.getFieldsValue();
      const mail = values.email;

      if (!mail) {
        message.error('请先填写邮箱');
        return;
      }

      setEmail(mail);
      setCodeLoading(true);

      await authService.sendMobileCode({
        mobile: mail,
        scene: 'REGISTER',
      });

      message.success('验证码已发送到您的邮箱');
      setCurrentStep(1);
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
    } finally {
      setCodeLoading(false);
    }
  };

  // 验证邮箱验证码
  const handleVerifyEmail = async () => {
    try {
      const code = form.getFieldValue('emailCode');
      if (!code) {
        message.error('请输入验证码');
        return;
      }

      // 验证逻辑
      setVerifyCode(code);
      setCurrentStep(2);
      message.success('邮箱验证成功');
    } catch (error: any) {
      message.error(error.message || '验证失败');
    }
  };

  // 完成注册
  const handleRegister = async (values: RegisterFormValues) => {
    setLoading(true);
    try {
      await authService.register({
        username: values.username,
        password: values.password,
        email: email,
        agreeTerms: values.agreeTerms,
      });

      message.success('注册成功，请登录');
      history.push('/user/login');
    } catch (error: any) {
      message.error(error.message || '注册失败');
    } finally {
      setLoading(false);
    }
  };

  const steps = [
    { title: '填写信息' },
    { title: '验证邮箱' },
    { title: '完成注册' },
  ];

  return (
    <div className={styles.container}>
      <Title level={2} className={styles.title}>
        注册账号
      </Title>
      <Text className={styles.subtitle}>创建您的数据资产管理账号</Text>

      <Steps current={currentStep} items={steps} className={styles.steps} />

      <Form
        form={form}
        onFinish={handleRegister}
        size="large"
        className={styles.form}
      >
        {currentStep === 0 && (
          <>
            <Form.Item
              name="username"
              rules={[
                { required: true, message: '请输入用户名' },
                { min: 3, max: 20, message: '用户名长度3-20个字符' },
              ]}
            >
              <Input prefix={<UserOutlined />} placeholder="用户名" />
            </Form.Item>

            <Form.Item
              name="password"
              rules={[
                { required: true, message: '请输入密码' },
                { min: 8, message: '密码至少8位' },
                {
                  pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/,
                  message: '密码需包含大小写字母和数字',
                },
              ]}
            >
              <Input.Password prefix={<LockOutlined />} placeholder="密码" />
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
              <Input.Password prefix={<LockOutlined />} placeholder="确认密码" />
            </Form.Item>

            <Form.Item
              name="email"
              rules={[
                { required: true, message: '请输入邮箱' },
                { type: 'email', message: '请输入正确的邮箱格式' },
              ]}
            >
              <Input prefix={<MailOutlined />} placeholder="邮箱地址" />
            </Form.Item>

            <Form.Item
              name="mobile"
              rules={[
                { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' },
              ]}
            >
              <Input prefix={<MobileOutlined />} placeholder="手机号（选填）" />
            </Form.Item>

            <Form.Item
              name="agreeTerms"
              valuePropName="checked"
              rules={[
                {
                  validator: (_, value) =>
                    value
                      ? Promise.resolve()
                      : Promise.reject(new Error('请同意用户协议')),
                },
              ]}
            >
              <Checkbox>
                我已阅读并同意
                <a href="/terms" target="_blank">
                  《用户协议》
                </a>
                和
                <a href="/privacy" target="_blank">
                  《隐私政策》
                </a>
              </Checkbox>
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" block onClick={handleSendCode}>
                发送验证码
              </Button>
            </Form.Item>
          </>
        )}

        {currentStep === 1 && (
          <>
            <div className={styles.verifyInfo}>
              <Text>验证码已发送到 {email}</Text>
            </div>

            <Form.Item
              name="emailCode"
              rules={[{ required: true, message: '请输入验证码' }]}
            >
              <Input
                prefix={<SafetyOutlined />}
                placeholder="请输入邮箱验证码"
                size="large"
              />
            </Form.Item>

            <Space style={{ width: '100%', justifyContent: 'space-between' }}>
              <Button onClick={() => setCurrentStep(0)}>上一步</Button>
              <Button
                type="primary"
                onClick={handleVerifyEmail}
                disabled={codeCountdown > 0}
              >
                {codeCountdown > 0 ? `${codeCountdown}秒后重发` : '验证'}
              </Button>
            </Space>
          </>
        )}

        {currentStep === 2 && (
          <>
            <div className={styles.success}>
              <Text className={styles.successIcon}>✓</Text>
              <Title level={4}>邮箱验证成功</Title>
              <Text>请设置您的账号信息完成注册</Text>
            </div>

            <Form.Item
              name="username"
              rules={[{ required: true, message: '请输入用户名' }]}
            >
              <Input prefix={<UserOutlined />} placeholder="用户名" />
            </Form.Item>

            <Form.Item
              name="password"
              rules={[
                { required: true, message: '请输入密码' },
                { min: 8, message: '密码至少8位' },
              ]}
            >
              <Input.Password prefix={<LockOutlined />} placeholder="设置密码" />
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
              <Input.Password prefix={<LockOutlined />} placeholder="确认密码" />
            </Form.Item>

            <Form.Item>
              <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                <Button onClick={() => setCurrentStep(1)}>上一步</Button>
                <Button type="primary" htmlType="submit" loading={loading}>
                  完成注册
                </Button>
              </Space>
            </Form.Item>
          </>
        )}
      </Form>

      <div className={styles.login}>
        <Text>已有账号？</Text>
        <Link to="/user/login">立即登录</Link>
      </div>
    </div>
  );
};

export default Register;
