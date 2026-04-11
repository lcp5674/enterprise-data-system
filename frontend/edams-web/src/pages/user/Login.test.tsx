/**
 * Login Page Tests
 */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import LoginPage from './Login';
import { useNavigate } from '@umijs/max';

// Mock router
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: jest.fn(),
}));

// Mock store
jest.mock('@/stores/auth', () => ({
  useAuthStore: jest.fn(() => ({
    login: jest.fn().mockResolvedValue({ success: true }),
    setUser: jest.fn(),
    setToken: jest.fn(),
    loading: false,
    mfaRequired: false,
  })),
}));

// Mock message
jest.mock('antd/es/message', () => ({
  default: {
    error: jest.fn(),
    success: jest.fn(),
  },
}));

describe('LoginPage', () => {
  const mockNavigate = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useNavigate as jest.Mock).mockReturnValue(mockNavigate);
  });

  it('should render login form', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    expect(screen.getByText('企业数据资产管理系统')).toBeInTheDocument();
    expect(screen.getByText('用户登录')).toBeInTheDocument();
  });

  it('should have account login tab by default', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    expect(screen.getByText('账号密码')).toBeInTheDocument();
    expect(screen.getByText('手机验证码')).toBeInTheDocument();
  });

  it('should switch to mobile code tab', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    const mobileTab = screen.getByText('手机验证码');
    fireEvent.click(mobileTab);

    expect(screen.getByPlaceholderText('请输入手机号')).toBeInTheDocument();
  });

  it('should update username field', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    const usernameInput = screen.getByPlaceholderText('用户名 / 手机号 / 邮箱') as HTMLInputElement;
    fireEvent.change(usernameInput, { target: { value: 'testuser' } });

    expect(usernameInput.value).toBe('testuser');
  });

  it('should update password field', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    const passwordInput = screen.getByPlaceholderText('请输入密码') as HTMLInputElement;
    fireEvent.change(passwordInput, { target: { value: 'testpass' } });

    expect(passwordInput.value).toBe('testpass');
  });

  it('should show forgot password link', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    expect(screen.getByText('忘记密码？')).toBeInTheDocument();
  });

  it('should show register link', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    expect(screen.getByText('还没有账号？')).toBeInTheDocument();
    expect(screen.getByText('立即注册')).toBeInTheDocument();
  });

  it('should have third-party login options', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    expect(screen.getByText('微信')).toBeInTheDocument();
    expect(screen.getByText('钉钉')).toBeInTheDocument();
  });

  it('should have terms agreement checkbox', () => {
    render(
      <BrowserRouter>
        <LoginPage />
      </BrowserRouter>,
    );

    expect(screen.getByText('我已阅读并同意')).toBeInTheDocument();
    expect(screen.getByText('《用户服务协议》')).toBeInTheDocument();
    expect(screen.getByText('《隐私政策》')).toBeInTheDocument();
  });
});
