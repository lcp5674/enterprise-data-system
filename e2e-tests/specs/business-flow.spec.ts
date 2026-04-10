/**
 * E2E-BUSINESS: 关键业务流程端到端测试
 * 
 * 测试关键业务流程的完整链路
 */
import { test, expect } from '@playwright/test';
import LoginPage from '../pages/LoginPage';
import HomePage from '../pages/HomePage';
import AssetPage from '../pages/AssetPage';
import CatalogPage from '../pages/CatalogPage';
import QualityPage from '../pages/QualityPage';
import SystemPage from '../pages/SystemPage';
import LineagePage from '../pages/LineagePage';
import { testConfig } from '../utils/helpers';

test.describe('关键业务流程 E2E 测试', () => {
  let loginPage: LoginPage;
  let homePage: HomePage;
  let assetPage: AssetPage;
  let catalogPage: CatalogPage;
  let qualityPage: QualityPage;
  let systemPage: SystemPage;
  let lineagePage: LineagePage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    homePage = new HomePage(page);
    assetPage = new AssetPage(page);
    catalogPage = new CatalogPage(page);
    qualityPage = new QualityPage(page);
    systemPage = new SystemPage(page);
    lineagePage = new LineagePage(page);
  });

  test.describe('端到端数据治理流程', () => {
    test('E2E-BUSINESS-001: 完整数据治理流程', async ({ page }) => {
      // ===== 步骤1: 用户登录 =====
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await loginPage.waitForLoginSuccess();
      console.log('步骤1: 用户登录成功');
      
      // ===== 步骤2: 创建数据目录 =====
      await catalogPage.goto();
      const catalogName = `业务域_${Date.now()}`;
      const catalogCode = `biz_${Date.now()}`;
      await catalogPage.createRootCatalog(catalogName, catalogCode);
      console.log(`步骤2: 创建目录 ${catalogName}`);
      
      // ===== 步骤3: 创建数据源 =====
      await systemPage.goto();
      await systemPage.goToDatasources();
      const datasourceName = `数据源_${Date.now()}`;
      await systemPage.createDatasource({
        name: datasourceName,
        type: 'MySQL',
        host: 'localhost',
        port: 3306,
        database: 'test_db',
        username: 'root',
        password: 'root',
      });
      console.log(`步骤3: 创建数据源 ${datasourceName}`);
      
      // ===== 步骤4: 创建质量规则 =====
      await qualityPage.goto();
      const ruleName = `质量规则_${Date.now()}`;
      await qualityPage.createRule(ruleName, '完整性', 'SELECT * FROM table');
      console.log(`步骤4: 创建质量规则 ${ruleName}`);
      
      // ===== 步骤5: 执行质量检测 =====
      await qualityPage.executeCheck();
      console.log('步骤5: 执行质量检测');
      
      // ===== 步骤6: 查看质量报告 =====
      await qualityPage.goToReports();
      console.log('步骤6: 查看质量报告');
      
      // ===== 步骤7: 查看血缘关系 =====
      await lineagePage.goto();
      const nodeCount = await lineagePage.getNodeCount();
      console.log(`步骤7: 查看血缘图，节点数: ${nodeCount}`);
      
      // ===== 步骤8: 资产搜索 =====
      await assetPage.goto();
      await assetPage.searchAssets('test');
      console.log('步骤8: 资产搜索');
      
      console.log('✅ 完整数据治理流程测试完成');
    });
  });

  test.describe('用户权限管理完整流程', () => {
    test('E2E-BUSINESS-002: 用户权限管理流程', async ({ page }) => {
      // ===== 步骤1: 管理员登录 =====
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await loginPage.waitForLoginSuccess();
      console.log('管理员登录成功');
      
      // ===== 步骤2: 创建角色 =====
      await systemPage.goto();
      await systemPage.goToRoles();
      const roleName = `测试角色_${Date.now()}`;
      await systemPage.createRole({
        name: roleName,
        code: `test_${Date.now()}`,
        description: '测试角色',
        permissions: ['资产查看', '质量检测'],
      });
      console.log(`创建角色 ${roleName}`);
      
      // ===== 步骤3: 创建用户 =====
      await systemPage.goToUsers();
      const username = `testuser_${Date.now()}`;
      await systemPage.createUser({
        username: username,
        password: 'Test123456',
        email: `${username}@test.com`,
        role: roleName,
      });
      console.log(`创建用户 ${username}`);
      
      // ===== 步骤4: 验证用户权限 =====
      await homePage.logout();
      await loginPage.loginWithPassword(username, 'Test123456');
      await loginPage.waitForLoginSuccess();
      console.log(`用户 ${username} 登录验证`);
      
      // 验证可以访问资产页面
      await assetPage.goto();
      expect(page.url()).toContain('/assets');
      
      console.log('✅ 用户权限管理流程测试完成');
    });
  });

  test.describe('资产全生命周期管理', () => {
    test('E2E-BUSINESS-003: 资产全生命周期', async ({ page }) => {
      // ===== 登录 =====
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await loginPage.waitForLoginSuccess();
      
      // ===== 创建资产 =====
      await assetPage.goto();
      await assetPage.clickCreateAsset();
      console.log('创建资产');
      
      // ===== 查看资产详情 =====
      await assetPage.goto();
      await assetPage.clickFirstAsset();
      console.log('查看资产详情');
      
      // ===== 收藏资产 =====
      await assetPage.goto();
      await assetPage.favoriteAsset(0);
      console.log('收藏资产');
      
      // ===== 导资产 =====
      await assetPage.exportAssets();
      console.log('导资产');
      
      console.log('✅ 资产全生命周期测试完成');
    });
  });

  test.describe('数据质量闭环管理', () => {
    test('E2E-BUSINESS-004: 质量闭环管理', async ({ page }) => {
      // ===== 登录 =====
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await loginPage.waitForLoginSuccess();
      
      // ===== 创建质量规则 =====
      await qualityPage.goto();
      const ruleName = `闭环规则_${Date.now()}`;
      await qualityPage.createRule(ruleName, '完整性', 'SELECT * FROM table');
      console.log('创建质量规则');
      
      // ===== 执行检测 =====
      await qualityPage.executeCheck();
      console.log('执行质量检测');
      
      // ===== 查看问题 =====
      await qualityPage.goToIssues();
      const issueCount = await qualityPage.getIssueCount();
      console.log(`发现问题数: ${issueCount}`);
      
      // ===== 生成报告 =====
      await qualityPage.goToReports();
      await qualityPage.generateReport();
      console.log('生成质量报告');
      
      console.log('✅ 质量闭环管理测试完成');
    });
  });
});
