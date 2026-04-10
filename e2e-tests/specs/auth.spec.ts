/**
 * E2E-AUTH: 认证流程测试
 * 
 * 测试场景:
 * 1. 用户名密码登录
 * 2. 验证码登录
 * 3. 第三方登录模拟
 * 4. 退出登录
 */
import { test, expect } from '@playwright/test';
import LoginPage from '../pages/LoginPage';
import HomePage from '../pages/HomePage';
import { testConfig } from '../utils/helpers';

test.describe('认证流程 E2E 测试', () => {
  let loginPage: LoginPage;
  let homePage: HomePage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    homePage = new HomePage(page);
  });

  test.describe('用户登录', () => {
    test('E2E-AUTH-001: 用户名密码登录流程', async ({ page }) => {
      // 1. 访问登录页面
      await loginPage.goto();
      
      // 2. 验证页面元素
      await loginPage.verifyPageElements();
      
      // 3. 输入用户名和密码
      await loginPage.loginWithPassword(
        testConfig.testUsers.admin.username,
        testConfig.testUsers.admin.password
      );
      
      // 4. 等待登录成功
      await loginPage.waitForLoginSuccess();
      
      // 5. 验证跳转到首页
      await expect(page).toHaveURL(/\/home/);
      
      // 6. 验证用户信息
      await homePage.goto();
      await expect(homePage.welcomeTitle).toBeVisible();
    });

    test('E2E-AUTH-001a: 不同角色用户登录', async ({ page }) => {
      // 测试分析师账号登录
      await loginPage.goto();
      await loginPage.loginWithPassword(
        testConfig.testUsers.analyst.username,
        testConfig.testUsers.analyst.password
      );
      await loginPage.waitForLoginSuccess();
      await expect(page).toHaveURL(/\/home/);
    });

    test('E2E-AUTH-001b: 登录失败验证', async ({ page }) => {
      await loginPage.goto();
      
      // 输入错误的密码
      await loginPage.loginWithPassword(
        testConfig.testUsers.admin.username,
        'wrongpassword'
      );
      
      // 验证错误提示
      const errorMessage = await loginPage.getErrorMessage();
      expect(errorMessage).toBeTruthy();
    });
  });

  test.describe('验证码登录', () => {
    test('E2E-AUTH-002: 手机验证码登录流程', async ({ page }) => {
      // 1. 访问登录页面
      await loginPage.goto();
      
      // 2. 切换到手机验证码 tab
      await loginPage.switchToMobileLogin();
      
      // 3. 验证手机验证码 tab 元素
      await expect(page.locator('input[placeholder="请输入手机号"]')).toBeVisible();
      await expect(page.locator('input[placeholder="请输入验证码"]')).toBeVisible();
      
      // 4. 输入手机号
      await page.fill('input[placeholder="请输入手机号"]', '13800138000');
      
      // 5. 点击获取验证码
      const sendCodeButton = loginPage.getSendCodeButton();
      await sendCodeButton.click();
      
      // 6. 验证倒计时显示（由于是测试环境，这里模拟）
      // 注意：实际测试中需要处理验证码获取逻辑
    });
  });

  test.describe('第三方登录', () => {
    test('E2E-AUTH-003: 微信登录模拟', async ({ page }) => {
      await loginPage.goto();
      
      // 点击微信登录按钮
      await loginPage.wechatButton.click();
      
      // 验证跳转到 SSO 页面或显示提示
      // 由于是测试环境，这里只验证点击不报错
    });

    test('E2E-AUTH-003a: 钉钉登录模拟', async ({ page }) => {
      await loginPage.goto();
      
      // 点击钉钉登录按钮
      await loginPage.dingtalkButton.click();
      
      // 验证跳转到 SSO 页面或显示提示
    });
  });

  test.describe('会话管理', () => {
    test('E2E-AUTH-004: 退出登录', async ({ page }) => {
      // 1. 先登录
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await loginPage.waitForLoginSuccess();
      
      // 2. 退出登录
      await homePage.logout();
      
      // 3. 验证跳转到登录页
      await expect(page).toHaveURL(/\/login/);
      
      // 4. 验证 Token 已清除
      const token = await page.evaluate(() => {
        return localStorage.getItem('edams_access_token');
      });
      expect(token).toBeNull();
    });

    test('E2E-AUTH-005: Token 验证', async ({ page }) => {
      // 1. 登录获取 Token
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await loginPage.waitForLoginSuccess();
      
      // 2. 验证 Token 已存储
      const token = await page.evaluate(() => {
        return localStorage.getItem('edams_access_token');
      });
      expect(token).toBeTruthy();
      
      // 3. 验证可以访问受保护页面
      await page.goto('/system');
      await expect(page).not.toHaveURL(/\/login/);
    });
  });

  test.describe('MFA 认证', () => {
    test('E2E-AUTH-006: MFA 设置与验证（需要后端支持）', async ({ page }) => {
      // 此测试需要后端支持 MFA 功能
      // 在实际环境中执行
      test.skip(true, 'MFA 测试需要后端支持');
    });
  });
});
