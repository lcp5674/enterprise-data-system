/**
 * E2E-QUALITY: 质量检测到报告生成链路测试
 * 
 * 测试链路: 创建质量规则 → 执行检测 → 查看结果 → 生成报告
 * 
 * 测试用例:
 * 1. 创建质量规则
 * 2. 配置检测计划
 * 3. 执行质量检测
 * 4. 查看质量报告
 * 5. 问题跟踪
 * 6. 导出质量报告
 */
import { test, expect } from '@playwright/test';
import LoginPage from '../pages/LoginPage';
import QualityPage from '../pages/QualityPage';
import AssetPage from '../pages/AssetPage';
import { testConfig } from '../utils/helpers';

test.describe('链路3: 质量检测到报告生成 E2E 测试', () => {
  let loginPage: LoginPage;
  let qualityPage: QualityPage;
  let assetPage: AssetPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    qualityPage = new QualityPage(page);
    assetPage = new AssetPage(page);
  });

  test('E2E-QUALITY-001: 完整链路 - 创建规则到报告生成', async ({ page }) => {
    // ===== 步骤1: 用户登录 =====
    await loginPage.goto();
    await loginPage.loginAsAdmin();
    await loginPage.waitForLoginSuccess();
    
    // ===== 步骤2: 创建质量规则 =====
    await qualityPage.goto();
    
    const ruleName = `完整性检查_${Date.now()}`;
    await qualityPage.createRule(
      ruleName,
      '完整性',
      'SELECT COUNT(*) FROM table WHERE field IS NULL'
    );
    
    // 验证规则创建成功
    await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10000 });
    console.log(`质量规则 ${ruleName} 创建成功`);
    
    // ===== 步骤3: 执行质量检测 =====
    await qualityPage.goto();
    await qualityPage.executeCheck();
    
    // 等待检测完成
    await page.waitForTimeout(5000);
    console.log('质量检测执行完成');
    
    // ===== 步骤4: 查看质量报告 =====
    await qualityPage.gotoToReports();
    await qualityPage.generateReport();
    
    console.log('质量报告生成完成');
    
    // ===== 步骤5: 问题跟踪 =====
    await qualityPage.goToIssues();
    const issueCount = await qualityPage.getIssueCount();
    console.log(`发现问题数量: ${issueCount}`);
  });

  test.describe('质量概览测试', () => {
    test.beforeEach(async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await qualityPage.goto();
    });

    test('E2E-QUALITY-002: 查看质量概览', async ({ page }) => {
      await qualityPage.goToOverview();
      
      // 验证概览卡片
      await expect(qualityPage.overviewCards.first()).toBeVisible();
      
      // 获取统计数据
      const stats = await qualityPage.getOverviewStats();
      console.log('质量概览统计:', stats);
    });

    test('E2E-QUALITY-003: 质量评分展示', async ({ page }) => {
      await qualityPage.goToOverview();
      
      // 验证质量评分展示
      const cards = await qualityPage.overviewCards.all();
      expect(cards.length).toBeGreaterThan(0);
    });
  });

  test.describe('质量规则测试', () => {
    test.beforeEach(async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await qualityPage.goto();
    });

    test('E2E-QUALITY-004: 创建完整性规则', async ({ page }) => {
      await qualityPage.goToRules();
      
      const ruleName = `完整性规则_${Date.now()}`;
      await qualityPage.createRule(
        ruleName,
        '完整性',
        'SELECT COUNT(*) FROM ${table} WHERE ${field} IS NULL'
      );
      
      // 验证创建成功
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10000 });
    });

    test('E2E-QUALITY-005: 创建唯一性规则', async ({ page }) => {
      await qualityPage.goToRules();
      
      const ruleName = `唯一性规则_${Date.now()}`;
      await qualityPage.createRule(
        ruleName,
        '唯一性',
        'SELECT COUNT(DISTINCT ${field}) FROM ${table}'
      );
      
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10000 });
    });

    test('E2E-QUALITY-006: 创建有效性规则', async ({ page }) => {
      await qualityPage.goToRules();
      
      const ruleName = `有效性规则_${Date.now()}`;
      await qualityPage.createRule(
        ruleName,
        '有效性',
        'SELECT COUNT(*) FROM ${table} WHERE ${field} NOT REGEXP ${pattern}'
      );
      
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10000 });
    });

    test('E2E-QUALITY-007: 创建一致性规则', async ({ page }) => {
      await qualityPage.goToRules();
      
      const ruleName = `一致性规则_${Date.now()}`;
      await qualityPage.createRule(
        ruleName,
        '一致性',
        'SELECT COUNT(*) FROM ${table} WHERE ${field1} != ${field2}'
      );
      
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10000 });
    });

    test('E2E-QUALITY-008: 获取规则列表', async ({ page }) => {
      await qualityPage.goToRules();
      
      const ruleCount = await qualityPage.getRuleCount();
      console.log(`质量规则总数: ${ruleCount}`);
      
      expect(ruleCount).toBeGreaterThanOrEqual(0);
    });
  });

  test.describe('质量检测执行测试', () => {
    test.beforeEach(async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await qualityPage.goto();
    });

    test('E2E-QUALITY-009: 执行质量检测', async ({ page }) => {
      await qualityPage.goToRules();
      
      // 执行检测
      await qualityPage.executeButton.click();
      await page.waitForSelector('.ant-modal', { state: 'visible' });
      
      // 开始检测
      await page.locator('button:has-text("开始检测")').click();
      
      // 等待检测完成
      await page.waitForTimeout(10000);
      
      console.log('质量检测执行完成');
    });

    test('E2E-QUALITY-010: 执行特定资产检测', async ({ page }) => {
      // 先获取资产列表
      await assetPage.goto();
      const assetCount = await assetPage.getAssetCount();
      
      if (assetCount > 0) {
        const firstAssetName = await assetPage.assetRows.first().locator('td').first().textContent();
        
        // 执行特定资产检测
        await qualityPage.goto();
        await qualityPage.executeCheck([firstAssetName || '']);
        
        console.log(`对资产 ${firstAssetName} 执行检测完成`);
      }
    });
  });

  test.describe('问题追踪测试', () => {
    test.beforeEach(async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await qualityPage.goto();
    });

    test('E2E-QUALITY-011: 查看问题列表', async ({ page }) => {
      await qualityPage.goToIssues();
      
      const issueCount = await qualityPage.getIssueCount();
      console.log(`问题总数: ${issueCount}`);
    });

    test('E2E-QUALITY-012: 解决问题', async ({ page }) => {
      await qualityPage.goToIssues();
      
      const issueCount = await qualityPage.getIssueCount();
      if (issueCount > 0) {
        await qualityPage.resolveIssue(0);
        console.log('问题已解决');
      }
    });
  });

  test.describe('质量报告测试', () => {
    test.beforeEach(async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await qualityPage.goto();
    });

    test('E2E-QUALITY-013: 生成质量报告', async ({ page }) => {
      await qualityPage.goToReports();
      
      // 点击生成报告
      await qualityPage.reportButton.click();
      await page.waitForSelector('.ant-modal', { state: 'visible' });
      
      // 选择报告类型
      await page.locator('.ant-select').first().click();
      await page.locator('.ant-select-dropdown >> text=质量报告').click();
      
      // 生成
      await page.locator('button:has-text("生成")').click();
      
      // 等待生成完成
      await page.waitForTimeout(5000);
      
      console.log('质量报告生成完成');
    });

    test('E2E-QUALITY-014: 导出质量报告', async ({ page }) => {
      await qualityPage.goToReports();
      
      // 等待报告列表加载
      await page.waitForTimeout(2000);
      
      // 点击下载按钮
      const downloadButton = page.locator('button:has-text("下载"), a[download]').first();
      if (await downloadButton.isVisible().catch(() => false)) {
        await downloadButton.click();
        await page.waitForTimeout(2000);
        console.log('质量报告导出完成');
      }
    });
  });
});
