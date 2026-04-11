/**
 * MFA 动态口令验证页面
 */

import React, { useState, useEffect, useRef } from 'react';
import { Form, Input, Button, Card, Typography, Space, message, Divider } from 'antd';
import { SafetyOutlined, MobileOutlined, MailOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { history, useNavigate } from '@umijs/max';
import { useAuthStore } from '../../stores';
import * as authService from '../../services/auth';
import styles from './MFA.less';

const { Title, Text, Paragraph } = Typography;

interface MFAFormValues {
  code: string;
}

const MFA: React.FC = () => {
  const navigate = useNavigate();
  const { verifyMFA, mfaInfo, clearMFA } = useAuthStore();
  
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [qrCodeUrl, setQrCodeUrl] = useState('');
  const [secretKey, setSecretKey] = useState('');
  
  const inputRef = useRef<any>(null);

  // 获取 MFA 信息
  useEffect(() => {
    const fetchMFAInfo = async () => {
      try {
        const info = await authService.getMFAInfo();
        setQrCodeUrl(info.qrCodeUrl || '');
        setSecretKey(info.secretKey || '');
        
        if (info.mfaType === 'TOTP' && info.qrCodeUrl) {
          setQrCodeUrl(info.qrCodeUrl);
        }
      } catch (error) {
        console.error('获取 MFA 信息失败:', error);
      }
    };
    
    fetchMFAInfo();
    
    // 自动聚焦到输入框
    setTimeout(() => {
      inputRef.current?.focus();
    }, 100);
  }, []);

  // 倒计时重发
  useEffect(() => {
    if (countdown > 0) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [countdown]);

  // 处理 MFA 验证
  const handleVerify = async (values: MFAFormValues) => {
    setLoading(true);
    try {
      const result = await verifyMFA(values.code);
      
      if (result.success) {
        message.success('验证成功');
        
        // 跳转目标页面
        const { redirect } = history.location.query as { redirect?: string };
        navigate(redirect || '/home');
      } else {
        message.error(result.message || '验证失败');
      }
    } catch (error: any) {
      message.error(error.message || '验证失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  // 重新发送验证码
  const handleResend = async () => {
    try {
      await authService.sendMFACode();
      message.success('验证码已发送');
      setCountdown(60);
    } catch (error: any) {
      message.error(error.message || '发送失败');
    }
  };

  // 返回登录页
  const handleBack = () => {
    clearMFA();
    navigate('/user/login');
  };

  const mfaType = mfaInfo?.mfaType || 'TOTP';

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
            返回
          </Button>
        </div>

        <div className={styles.content}>
          <div className={styles.icon}>
            <SafetyOutlined />
          </div>
          
          <Title level={3} className={styles.title}>
            安全验证
          </Title>
          
          <Paragraph className={styles.description}>
            请输入 {mfaType === 'TOTP' ? '您的动态口令' : '发送到手机的验证码'} 完成身份验证
          </Paragraph>

          {mfaType === 'TOTP' && qrCodeUrl && (
            <div className={styles.qrSection}>
              <img 
                src={qrCodeUrl} 
                alt="MFA QR Code" 
                className={styles.qrCode}
              />
              <Text type="secondary" className={styles.hint}>
                请使用身份验证器应用扫描二维码
              </Text>
              {secretKey && (
                <Text type="secondary" className={styles.secretKey}>
                  密钥: {secretKey}
                </Text>
              )}
            </div>
          )}

          <Form
            form={form}
            name="mfaVerify"
            onFinish={handleVerify}
            size="large"
            className={styles.form}
          >
            <Form.Item
              name="code"
              rules={[
                { required: true, message: '请输入验证码' },
                { pattern: /^\d{6}$/, message: '验证码为6位数字' },
              ]}
            >
              <Input
                ref={inputRef}
                prefix={<SafetyOutlined />}
                placeholder="请输入6位动态口令"
                maxLength={6}
                className={styles.codeInput}
              />
            </Form.Item>

            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                block
                className={styles.submitButton}
              >
                验证
              </Button>
            </Form.Item>
          </Form>

          <Divider plain />

          <Space direction="vertical" className={styles.helpSection}>
            <Text type="secondary">
              <MobileOutlined /> 未收到短信？
            </Text>
            <Button
              type="link"
              onClick={handleResend}
              disabled={countdown > 0}
              className={styles.resendButton}
            >
              {countdown > 0 ? `${countdown}秒后可重新发送` : '重新获取验证码'}
            </Button>
          </Space>
        </div>
      </Card>
    </div>
  );
};

export default MFA;
