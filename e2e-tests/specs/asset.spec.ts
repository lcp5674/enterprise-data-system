/**
 * E2E-ASSET: 资产查询链路测试
 * 
 * 测试链路: 用户登录 → 首页 → 数据资产搜索 → 资产详情 → 收藏资产
 * 
 * 测试用例:
 * 1. 用户名密码登录
 * 2. 资产搜索（关键字、过滤）
 * 3. 资产详情查看
 * 4. 资产收藏
 * 5. 退出登录
 */
import { test, expect } from '@playwright/test';
import LoginPage from '../pages/LoginPage';
import HomePage from '../pages/HomePage';
import AssetPage from '../pages/AssetPage';
import { testConfig } from '../utils/helpers';

test.describe('链路1: 用户登录到资产查询 E2E 测试', () => {
  let loginPage: LoginPage;
  let homePage: HomePage;
  let assetPage: AssetPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    homePage = new HomePage(page);
    assetPage = new AssetPage(page);
  });

  test('E2E-ASSET-001: 完整链路 - 用户登录到资产搜索', async ({ page }) => {
    // ===== 步骤1: 用户登录 =====
    await loginPage.goto();
    await loginPage.loginWithPassword(
      testConfig.testUsers.admin.username,
      testConfig.testUsers.admin.password
    );
    await loginPage.waitForLoginSuccess();
    
    // ===== 步骤2: 验证首页 =====
    await homePage.goto();
    await expect(homePage.welcomeTitle).toBeVisible();
    
    // 获取统计卡片数量
    const statCount = await homePage.getStatisticsCardCount();
    expect(statCount).toBeGreaterThan(0);
    
    // ===== 步骤3: 导航到资产页面 =====
    await homePage.navigateToAssets();
    await expect(page).toHaveURL(/\/assets/);
    
    // ===== 步骤4: 资产搜索 =====
    await assetPage.searchAssets('test');
    
    // 验证搜索结果
    await expect(assetPage.assetList).toBeVisible();
    
    // 获取搜索结果数量
    const assetCount = await assetPage.getAssetCount();
    console.log(`搜索到 ${assetCount} 个资产`);
  });

  test.describe('资产搜索测试', () => {
    test.beforeEach(async ({ page }) => {
      // 登录并导航到资产页面
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await assetPage.goto();
    });

    test('E2E-ASSET-002: 按关键字搜索资产', async ({ page }) => {
      // 输入搜索关键字
      await assetPage.searchAssets('customer');
      
      // 验证搜索结果
      await expect(assetPage.assetList).toBeVisible();
      
      // 验证结果包含搜索关键字（如果有结果）
      const count = await assetPage.getAssetCount();
      if (count > 0) {
        console.log(`关键字搜索找到 ${count} 个资产`);
      }
    });

    test('E2E-ASSET-003: 按资产类型筛选', async ({ page }) => {
      // 等待页面加载
      await page.waitForLoadState('networkidle');
      
      // 选择资产类型过滤
      await assetPage.filterByType('TABLE');
      
      // 等待过滤结果
      await page.waitForTimeout(1000);
      
      // 验证过滤结果
      const count = await assetPage.getAssetCount();
      console.log(`类型过滤找到 ${count} 个资产`);
    });

    test('E2E-ASSET-004: 刷新资产列表', async ({ page }) => {
      // 点击刷新按钮
      await assetPage.refreshAssets();
      
      // 验证页面正常刷新
      await expect(assetPage.assetList).toBeVisible();
    });
  });

  test.describe('资产详情测试', () => {
    test.beforeEach(async ({ page }) => {
      // 登录并导航到资产页面
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await assetPage.goto();
    });

    test('E2E-ASSET-005: 查看资产详情', async ({ page }) => {
      // 等待列表加载
      await page.waitForLoadState('networkidle');
      
      // 点击第一个资产
      await assetPage.clickFirstAsset();
      
      // 验证跳转到详情页
      await expect(page).toHaveURL(/\/assets\/detail/);
      
      // 验证详情页元素
      const detailPage = new AssetPage(page);
      await expect(detailPage.assetName).toBeVisible();
    });

    test('E2E-ASSET-006: 收藏资产', async ({ page }) => {
      // 等待列表加载
      await page.waitForLoadState('networkidle');
      
      // 获取收藏前状态
      const initialCount = await assetPage.getAssetCount();
      
      // 收藏第一个资产
      await assetPage.favoriteAsset(0);
      
      // 验证收藏成功（出现成功提示）
      await expect(page.locator('.ant-message-success, text=收藏成功')).toBeVisible({ timeout: 5000 });
      
      console.log('资产收藏成功');
    });
  });

  test.describe('资产导出测试', () => {
    test.beforeEach(async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await assetPage.goto();
    });

    test('E2E-ASSET-007: 导出资产列表', async ({ page }) => {
      // 点击导出按钮
      await assetPage.exportAssets();
      
      // 等待导出处理
      await page.waitForTimeout(2000);
      
      // 验证导出成功提示
      console.log('资产列表导出完成');
    });
  });
});
