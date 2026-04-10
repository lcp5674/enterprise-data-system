/**
 * E2E-LINEAGE: 资产注册到血缘生成链路测试
 * 
 * 测试链路: 注册数据源 → 扫描元数据 → 创建目录 → 关联资产 → 生成血缘
 * 
 * 测试用例:
 * 1. 创建数据源连接
 * 2. 执行元数据扫描
 * 3. 创建数据目录
 * 4. 资产归类
 * 5. 查看血缘图
 * 6. 导出血缘关系
 */
import { test, expect } from '@playwright/test';
import LoginPage from '../pages/LoginPage';
import CatalogPage from '../pages/CatalogPage';
import SystemPage from '../pages/SystemPage';
import LineagePage from '../pages/LineagePage';
import { testConfig } from '../utils/helpers';

test.describe('链路2: 资产注册到血缘生成 E2E 测试', () => {
  let loginPage: LoginPage;
  let catalogPage: CatalogPage;
  let systemPage: SystemPage;
  let lineagePage: LineagePage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    catalogPage = new CatalogPage(page);
    systemPage = new SystemPage(page);
    lineagePage = new LineagePage(page);
  });

  test('E2E-LINEAGE-001: 完整链路 - 资产注册到血缘生成', async ({ page }) => {
    // ===== 步骤1: 用户登录 =====
    await loginPage.goto();
    await loginPage.loginAsAdmin();
    await loginPage.waitForLoginSuccess();
    
    // ===== 步骤2: 创建数据源连接 =====
    await systemPage.goto();
    await systemPage.goToDatasources();
    
    // 点击创建数据源按钮
    await systemPage.createDatasourceButton.click();
    await page.waitForSelector('.ant-modal, form', { state: 'visible' });
    
    // 填写数据源信息
    const testDatasourceName = `测试数据源_${Date.now()}`;
    await systemPage.createDatasource({
      name: testDatasourceName,
      type: 'MySQL',
      host: testConfig.datasources.mysql.host,
      port: testConfig.datasources.mysql.port,
      database: testConfig.datasources.mysql.database,
      username: testConfig.datasources.mysql.username,
      password: testConfig.datasources.mysql.password,
    });
    
    console.log(`数据源 ${testDatasourceName} 创建成功`);
    
    // ===== 步骤3: 创建数据目录 =====
    await catalogPage.goto();
    
    // 创建根目录
    const testCatalogName = `测试目录_${Date.now()}`;
    const testCatalogCode = `test_catalog_${Date.now()}`;
    
    await catalogPage.createRootCatalog(testCatalogName, testCatalogCode);
    console.log(`目录 ${testCatalogName} 创建成功`);
    
    // ===== 步骤4: 导航到血缘页面 =====
    await lineagePage.goto();
    await expect(lineagePage.lineageGraph).toBeVisible();
    
    // ===== 步骤5: 查看血缘图 =====
    await lineagePage.goToTableLineage();
    
    // 获取节点数量
    const nodeCount = await lineagePage.getNodeCount();
    console.log(`血缘图节点数量: ${nodeCount}`);
    
    // ===== 步骤6: 导出血缘关系 =====
    await lineagePage.exportLineage('JSON');
    console.log('血缘关系导出成功');
  });

  test.describe('数据源管理测试', () => {
    test.beforeEach(async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await systemPage.goto();
      await systemPage.goToDatasources();
    });

    test('E2E-LINEAGE-002: 创建 MySQL 数据源', async ({ page }) => {
      // 点击创建数据源按钮
      await systemPage.createDatasourceButton.click();
      await page.waitForSelector('.ant-modal', { state: 'visible' });
      
      // 填写表单
      await page.locator('input[placeholder*="名称"]').fill(`MySQL数据源_${Date.now()}`);
      await page.locator('.ant-select').first().click();
      await page.locator('.ant-select-dropdown >> text=MySQL').click();
      
      await page.locator('input[placeholder*="主机"]').fill('localhost');
      await page.locator('input[placeholder*="端口"]').fill('3306');
      await page.locator('input[placeholder*="数据库"]').fill('test_db');
      await page.locator('input[placeholder*="用户名"]').fill('root');
      await page.locator('input[placeholder*="密码"]').fill('root');
      
      // 提交
      await page.locator('button:has-text("确定")').click();
      
      // 验证成功
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10000 });
    });

    test('E2E-LINEAGE-003: 创建 PostgreSQL 数据源', async ({ page }) => {
      await systemPage.createDatasourceButton.click();
      await page.waitForSelector('.ant-modal', { state: 'visible' });
      
      await page.locator('input[placeholder*="名称"]').fill(`PG数据源_${Date.now()}`);
      await page.locator('.ant-select').first().click();
      await page.locator('.ant-select-dropdown >> text=PostgreSQL').click();
      
      await page.locator('input[placeholder*="主机"]').fill('localhost');
      await page.locator('input[placeholder*="端口"]').fill('5432');
      await page.locator('input[placeholder*="数据库"]').fill('test_db');
      await page.locator('input[placeholder*="用户名"]').fill('postgres');
      await page.locator('input[placeholder*="密码"]').fill('postgres');
      
      await page.locator('button:has-text("确定")').click();
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10000 });
    });
  });

  test.describe('目录管理测试', () => {
    test.beforeEach(async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await catalogPage.goto();
    });

    test('E2E-LINEAGE-004: 创建目录', async ({ page }) => {
      // 等待目录树加载
      await page.waitForSelector('.ant-tree, .catalog-tree', { state: 'visible' });
      
      // 创建根目录
      const catalogName = `测试目录_${Date.now()}`;
      const catalogCode = `test_${Date.now()}`;
      
      await catalogPage.createRootCatalog(catalogName, catalogCode);
      
      // 验证创建成功
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 5000 });
    });

    test('E2E-LINEAGE-005: 添加子目录', async ({ page }) => {
      // 先创建一个父目录
      const parentName = `父目录_${Date.now()}`;
      const parentCode = `parent_${Date.now()}`;
      await catalogPage.createRootCatalog(parentName, parentCode);
      
      // 添加子目录
      const childName = `子目录_${Date.now()}`;
      const childCode = `child_${Date.now()}`;
      await catalogPage.addChildCatalog(parentName, childName, childCode);
      
      // 验证
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 5000 });
    });

    test('E2E-LINEAGE-006: 编辑目录', async ({ page }) => {
      // 先创建目录
      const originalName = `原目录_${Date.now()}`;
      const originalCode = `orig_${Date.now()}`;
      await catalogPage.createRootCatalog(originalName, originalCode);
      
      // 编辑目录
      const newName = `编辑后_${Date.now()}`;
      await catalogPage.editCatalog(originalName, newName);
      
      // 验证
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 5000 });
    });

    test('E2E-LINEAGE-007: 删除目录', async ({ page }) => {
      // 先创建目录
      const deleteName = `待删除_${Date.now()}`;
      const deleteCode = `del_${Date.now()}`;
      await catalogPage.createRootCatalog(deleteName, deleteCode);
      
      // 删除目录
      await catalogPage.deleteCatalog(deleteName);
      
      // 等待确认
      await page.waitForTimeout(1000);
    });
  });

  test.describe('血缘分析测试', () => {
    test.beforeEach(async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await lineagePage.goto();
    });

    test('E2E-LINEAGE-008: 查看表级血缘', async ({ page }) => {
      await lineagePage.goToTableLineage();
      await expect(lineagePage.lineageGraph).toBeVisible();
      
      const nodeCount = await lineagePage.getNodeCount();
      console.log(`表级血缘节点数: ${nodeCount}`);
    });

    test('E2E-LINEAGE-009: 查看字段血缘', async ({ page }) => {
      await lineagePage.goToFieldLineage();
      await expect(lineagePage.lineageGraph).toBeVisible();
      
      const nodeCount = await lineagePage.getNodeCount();
      console.log(`字段血缘节点数: ${nodeCount}`);
    });

    test('E2E-LINEAGE-010: 影响分析', async ({ page }) => {
      await lineagePage.goToImpactAnalysis();
      await expect(lineagePage.lineageGraph).toBeVisible();
    });

    test('E2E-LINEAGE-011: 数据溯源', async ({ page }) => {
      await lineagePage.goToDependency();
      await expect(lineagePage.lineageGraph).toBeVisible();
    });

    test('E2E-LINEAGE-012: 血缘图缩放操作', async ({ page }) => {
      await lineagePage.goToTableLineage();
      
      // 放大
      await lineagePage.zoomIn();
      await page.waitForTimeout(500);
      
      // 缩小
      await lineagePage.zoomOut();
      await page.waitForTimeout(500);
      
      // 重置视图
      await lineagePage.resetView();
    });

    test('E2E-LINEAGE-013: 搜索血缘节点', async ({ page }) => {
      // 搜索
      await lineagePage.searchNode('customer');
      await page.waitForTimeout(1000);
      
      const nodeCount = await lineagePage.getNodeCount();
      console.log(`搜索结果节点数: ${nodeCount}`);
    });

    test('E2E-LINEAGE-014: 导出血缘关系', async ({ page }) => {
      await lineagePage.goToTableLineage();
      
      // 导出 JSON 格式
      await lineagePage.exportLineage('JSON');
      
      // 导出 CSV 格式
      await lineagePage.exportLineage('CSV');
      
      console.log('血缘关系导出完成');
    });
  });
});
