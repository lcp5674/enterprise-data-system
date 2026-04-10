/**
 * 登录页面对象
 */
import { Page, Locator, expect } from '@playwright/test';
import { testConfig } from '../utils/helpers';

export class LoginPage {
  readonly page: Page;
  readonly config = testConfig;

  // 定位器
  readonly usernameInput: Locator;
  readonly passwordInput: Locator;
  readonly captchaInput: Locator;
  readonly submitButton: Locator;
  readonly loginTab: Locator;
  readonly mobileTab: Locator;
  readonly wechatButton: Locator;
  readonly dingtalkButton: Locator;
  readonly rememberMe: Locator;
  readonly forgotPasswordLink: Locator;
  readonly registerLink: Locator;
  readonly termsCheckbox: Locator;
  readonly title: Locator;
  readonly subtitle: Locator;

  constructor(page: Page) {
    this.page = page;
    
    // 初始化定位器
    this.usernameInput = page.locator('input[placeholder*="用户名"], input[placeholder*="手机号"], input[placeholder*="邮箱"]').first();
    this.passwordInput = page.locator('input[type="password"]').first();
    this.captchaInput = page.locator('input[placeholder*="验证码"]').first();
    this.submitButton = page.locator('button[type="submit"]');
    this.loginTab = page.locator('.ant-tabs-tab:has-text("账号密码")');
    this.mobileTab = page.locator('.ant-tabs-tab:has-text("手机验证码")');
    this.wechatButton = page.locator('button').filter({ has: page.locator('.anticon-wechat, [aria-label="微信"]') }).first();
    this.dingtalkButton = page.locator('button').filter({ has: page.locator('.anticon-dingding, [aria-label="钉钉"]') }).first();
    this.rememberMe = page.locator('input[type="checkbox"]').first();
    this.forgotPasswordLink = page.locator('a:has-text("忘记密码")');
    this.registerLink = page.locator('a:has-text("立即注册")');
    this.termsCheckbox = page.locator('input[type="checkbox"]').last();
    this.title = page.locator('h2, h1').filter({ hasText: '欢迎回来' });
    this.subtitle = page.locator('text=请登录您的账号继续使用');
  }

  /**
   * 访问登录页面
   */
  async goto(): Promise<void> {
    await this.page.goto('/user/login');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 使用用户名密码登录
   */
  async loginWithPassword(username: string, password: string): Promise<void> {
    // 确保在账号密码登录 tab
    await this.loginTab.click();
    
    // 填写表单
    await this.usernameInput.fill(username);
    await this.passwordInput.fill(password);
    
    // 勾选用户协议（如果需要）
    const termsCheckbox = this.page.locator('.ant-checkbox-input');
    if (await termsCheckbox.isVisible().catch(() => false)) {
      await termsCheckbox.check();
    }
    
    // 提交表单
    await this.submitButton.click();
    
    // 等待登录成功
    await this.page.waitForURL('**/home', { timeout: 30000 }).catch(() => {
      // 如果 URL 没有变化，检查是否有错误提示
      console.log('登录后 URL 未跳转到首页');
    });
  }

  /**
   * 使用测试账号登录
   */
  async loginAsAdmin(): Promise<void> {
    await this.loginWithPassword(
      this.config.testUsers.admin.username,
      this.config.testUsers.admin.password
    );
  }

  async loginAsAnalyst(): Promise<void> {
    await this.loginWithPassword(
      this.config.testUsers.analyst.username,
      this.config.testUsers.analyst.password
    );
  }

  async loginAsOperator(): Promise<void> {
    await this.loginWithPassword(
      this.config.testUsers.operator.username,
      this.config.testUsers.operator.password
    );
  }

  async loginAsViewer(): Promise<void> {
    await this.loginWithPassword(
      this.config.testUsers.viewer.username,
      this.config.testUsers.viewer.password
    );
  }

  /**
   * 切换到手机验证码登录
   */
  async switchToMobileLogin(): Promise<void> {
    await this.mobileTab.click();
  }

  /**
   * 获取验证码按钮
   */
  getSendCodeButton(): Locator {
    return this.page.locator('button:has-text("获取验证码")');
  }

  /**
   * 验证登录页面元素
   */
  async verifyPageElements(): Promise<void> {
    await expect(this.title).toBeVisible();
    await expect(this.subtitle).toBeVisible();
    await expect(this.loginTab).toBeVisible();
    await expect(this.mobileTab).toBeVisible();
  }

  /**
   * 登出
   */
  async logout(): Promise<void> {
    // 点击用户头像或下拉菜单
    const userAvatar = this.page.locator('.ant-avatar, [class*="user"]').first();
    await userAvatar.click();
    
    // 点击退出登录
    const logoutButton = this.page.locator('text=退出登录, text=退出, text=Logout');
    await logoutButton.click();
    
    // 确认退出
    await this.page.waitForURL('**/login', { timeout: 10000 });
  }

  /**
   * 等待登录成功并跳转到首页
   */
  async waitForLoginSuccess(): Promise<void> {
    await this.page.waitForURL((url) => !url.pathname.includes('/login'), {
      timeout: 30000,
    });
  }

  /**
   * 获取错误消息
   */
  async getErrorMessage(): Promise<string | null> {
    const errorLocator = this.page.locator('.ant-message-error, [class*="error"]').first();
    if (await errorLocator.isVisible().catch(() => false)) {
      return await errorLocator.textContent();
    }
    return null;
  }
}

export default LoginPage;
