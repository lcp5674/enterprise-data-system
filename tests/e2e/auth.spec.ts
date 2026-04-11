import { test, expect } from '@playwright/test';

/**
 * 认证流程E2E测试
 * 测试用户登录、注册、Token验证等完整流程
 */
test.describe('认证模块E2E测试', () => {

  test.beforeEach(async ({ page }) => {
    // 访问登录页面
    await page.goto('/login');
  });

  test('登录成功', async ({ page }) => {
    // 输入用户名密码
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');

    // 点击登录按钮
    await page.click('button[type="submit"]');

    // 验证登录成功，跳转到首页
    await expect(page).toHaveURL('/dashboard');
    await expect(page.locator('.user-name')).toContainText('管理员');
  });

  test('登录失败 - 错误密码', async ({ page }) => {
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'wrongpassword');

    await page.click('button[type="submit"]');

    // 验证错误提示
    await expect(page.locator('.error-message')).toBeVisible();
    await expect(page.locator('.error-message')).toContainText('用户名或密码错误');
  });

  test('登录失败 - 用户不存在', async ({ page }) => {
    await page.fill('input[name="username"]', 'nonexistent');
    await page.fill('input[name="password"]', 'password');

    await page.click('button[type="submit"]');

    await expect(page.locator('.error-message')).toBeVisible();
  });

  test('注册新用户', async ({ page }) => {
    // 点击注册链接
    await page.click('a[href="/register"]');

    // 填写注册表单
    const timestamp = Date.now();
    await page.fill('input[name="username"]', `testuser_${timestamp}`);
    await page.fill('input[name="email"]', `test_${timestamp}@example.com`);
    await page.fill('input[name="password"]', 'Test123456');
    await page.fill('input[name="confirmPassword"]', 'Test123456');
    await page.fill('input[name="realName"]', '测试用户');

    // 提交注册
    await page.click('button[type="submit"]');

    // 验证注册成功
    await expect(page).toHaveURL('/login');
    await expect(page.locator('.success-message')).toContainText('注册成功');
  });

  test('注册表单验证', async ({ page }) => {
    await page.click('a[href="/register"]');

    // 用户名为空提交
    await page.fill('input[name="email"]', 'test@example.com');
    await page.fill('input[name="password"]', 'Test123');
    await page.click('button[type="submit"]');

    // 验证表单错误
    await expect(page.locator('.field-error')).toBeVisible();
  });

  test('MFA验证流程', async ({ page }) => {
    // 先登录
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');

    // 如果启用了MFA，输入验证码
    const mfaPage = page.locator('.mfa-page');
    if (await mfaPage.isVisible()) {
      await page.fill('input[name="mfaCode"]', '123456');
      await page.click('button[type="submit"]');
    }

    await expect(page).toHaveURL('/dashboard');
  });

  test('Token过期处理', async ({ page }) => {
    // 使用过期token访问
    await page.evaluate(() => {
      localStorage.setItem('token', 'expired.token.here');
    });

    await page.goto('/dashboard');

    // 应该跳转到登录页
    await expect(page).toHaveURL('/login');
  });

  test('登出功能', async ({ page }) => {
    // 先登录
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL('/dashboard');

    // 点击登出
    await page.click('.user-menu');
    await page.click('button:has-text("退出登录")');

    // 验证登出成功
    await expect(page).toHaveURL('/login');
    await expect(localStorage.getItem('token')).toBeNull();
  });
});

test.describe('会话管理E2E测试', () => {
  test('记住登录状态', async ({ page, context }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.check('input[name="rememberMe"]');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL('/dashboard');

    // 关闭并重新打开浏览器
    await context.close();

    // 应该保持登录状态
    const newContext = await browser.newContext();
    const newPage = await newContext.newPage();
    await newPage.goto('/dashboard');
    await expect(newPage).toHaveURL('/dashboard');
  });
});
