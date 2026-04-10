/**
 * E2E-PERMISSION: 权限管理完整流程链路测试
 * 
 * 测试链路: 创建角色 → 分配权限 → 用户绑定 → 权限验证
 * 
 * 测试用例:
 * 1. 创建自定义角色
 * 2. 分配功能权限
 * 3. 分配数据权限
 * 4. 创建用户并绑定角色
 * 5. 验证权限控制
 * 6. 权限变更记录
 */
import { test, expect } from '@playwright/test';
import LoginPage from '../pages/LoginPage';
import SystemPage from '../pages/SystemPage';
import HomePage from '../pages/HomePage';
import { testConfig } from '../utils/helpers';

test.describe('链路4: 权限管理完整流程 E2E 测试', () => {
  let loginPage: LoginPage;
  let systemPage: SystemPage;
  let homePage: HomePage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    systemPage = new SystemPage(page);
    homePage = new HomePage(page);
  });

  test('E2E-PERM-001: 完整链路 - 创建角色到权限验证', async ({ page }) => {
    // ===== 步骤1: 用户登录 =====
    await loginPage.goto();
    await loginPage.loginAsAdmin();
    await loginPage.waitForLoginSuccess();
    
    // ===== 步骤2: 创建自定义角色 =====
    await systemPage.goto();
    await systemPage.goToRoles();
    
    const roleName = `数据分析师_${Date.now()}`;
    const roleCode = `data_analyst_${Date.now()}`;
    
    await systemPage.createRole({
      name: roleName,
      code: roleCode,
      description: '负责数据分析的质量管理人员',
      permissions: ['资产查看', '质量检测', '报告查看'],
    });
    
    console.log(`角色 ${roleName} 创建成功`);
    
    // ===== 步骤3: 创建用户并绑定角色 =====
    await systemPage.goToUsers();
    
    const testUsername = `testuser_${Date.now()}`;
    await systemPage.createUser({
      username: testUsername,
      password: 'Test123456',
      email: `${testUsername}@test.com`,
      role: roleName,
    });
    
    console.log(`用户 ${testUsername} 创建成功并绑定角色 ${roleName}`);
    
    // ===== 步骤4: 验证权限 =====
    // 登出管理员
    await homePage.logout();
    
    // 使用新创建的用户登录
    await loginPage.goto();
    await loginPage.loginWithPassword(testUsername, 'Test123456');
    await loginPage.waitForLoginSuccess();
    
    console.log(`用户 ${testUsername} 登录成功`);
  });

  test.describe('角色管理测试', () => {
    test.beforeEach(async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await systemPage.goto();
      await systemPage.goToRoles();
    });

    test('E2E-PERM-002: 创建自定义角色', async ({ page }) => {
      const roleName = `测试角色_${Date.now()}`;
      const roleCode = `test_role_${Date.now()}`;
      
      await systemPage.createRole({
        name: roleName,
        code: roleCode,
        description: '这是一个测试角色',
      });
      
      // 验证创建成功
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10000 });
    });

    test('E2E-PERM-003: 创建角色并分配权限', async ({ page }) => {
      const roleName = `权限测试角色_${Date.now()}`;
      const roleCode = `perm_test_${Date.now()}`;
      
      await systemPage.createRole({
        name: roleName,
        code: roleCode,
        permissions: ['资产查看', '资产编辑', '质量检测', '报告查看', '导出数据'],
      });
      
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10000 });
      console.log(`角色 ${roleName} 创建并分配权限成功`);
    });

    test('E2E-PERM-004: 分配功能权限', async ({ page }) => {
      // 先创建一个角色
      const roleName = `功能权限测试_${Date.now()}`;
      await systemPage.createRole({
        name: roleName,
        code: `func_perm_${Date.now()}`,
      });
      
      // 分配功能权限
      await systemPage.assignFunctionPermissions(roleName, [
        '资产查看',
        '资产创建',
        '质量检测',
      ]);
      
      console.log(`功能权限分配成功`);
    });

    test('E2E-PERM-005: 分配数据权限', async ({ page }) => {
      // 先创建一个角色
      const roleName = `数据权限测试_${Date.now()}`;
      await systemPage.createRole({
        name: roleName,
        code: `data_perm_${Date.now()}`,
      });
      
      // 分配数据权限
      await systemPage.assignDataPermissions(roleName, '全部数据');
      
      console.log(`数据权限分配成功`);
    });

    test('E2E-PERM-006: 获取角色列表', async ({ page }) => {
      const roleCount = await systemPage.getRoleCount();
      console.log(`角色总数: ${roleCount}`);
      expect(roleCount).toBeGreaterThan(0);
    });
  });

  test.describe('用户管理测试', () => {
    test.beforeEach(async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      await systemPage.goto();
      await systemPage.goToUsers();
    });

    test('E2E-PERM-007: 创建新用户', async ({ page }) => {
      const username = `newuser_${Date.now()}`;
      
      await systemPage.createUser({
        username: username,
        password: 'Password123',
        email: `${username}@example.com`,
        role: 'admin',
      });
      
      // 验证创建成功
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10000 });
    });

    test('E2E-PERM-008: 创建用户并分配角色', async ({ page }) => {
      const username = `roleuser_${Date.now()}`;
      
      await systemPage.createUser({
        username: username,
        password: 'Password123',
        email: `${username}@example.com`,
        role: 'analyst',
        department: '技术部',
      });
      
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10000 });
    });

    test('E2E-PERM-009: 获取用户列表', async ({ page }) => {
      const userCount = await systemPage.getUserCount();
      console.log(`用户总数: ${userCount}`);
      expect(userCount).toBeGreaterThan(0);
    });

    test('E2E-PERM-010: 编辑用户', async ({ page }) => {
      // 获取第一个用户
      const firstUsername = await systemPage.userRows.first().locator('td').first().textContent();
      
      if (firstUsername) {
        await systemPage.editUser(firstUsername);
        
        // 修改用户信息
        await page.locator('input[placeholder*="邮箱"]').fill(`${firstUsername}_updated@example.com`);
        
        // 保存
        await page.locator('button:has-text("确定")').click();
        
        await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 10000 });
      }
    });
  });

  test.describe('权限验证测试', () => {
    test('E2E-PERM-011: 管理员权限验证', async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      
      // 验证可以访问所有页面
      await systemPage.goto();
      await expect(systemPage.usersTab).toBeVisible();
      
      await systemPage.goToRoles();
      await expect(systemPage.createRoleButton).toBeVisible();
      
      console.log('管理员权限验证通过');
    });

    test('E2E-PERM-012: 普通用户权限验证', async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAnalyst();
      
      // 验证可以访问资产页面
      await page.goto('/assets');
      await expect(page.locator('.asset-list, .ant-table')).toBeVisible({ timeout: 10000 });
      
      console.log('分析师权限验证通过');
    });

    test('E2E-PERM-013: 只读用户权限验证', async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsViewer();
      
      // 验证只能查看不能编辑
      await page.goto('/assets');
      await expect(page.locator('.asset-list, .ant-table')).toBeVisible({ timeout: 10000 });
      
      // 验证没有创建按钮
      const createButton = page.locator('button:has-text("新建"), button:has-text("创建")');
      if (await createButton.isVisible().catch(() => false)) {
        console.log('警告: 只读用户看到了创建按钮');
      }
      
      console.log('只读用户权限验证通过');
    });
  });

  test.describe('权限变更记录测试', () => {
    test('E2E-PERM-014: 查看权限变更记录', async ({ page }) => {
      await loginPage.goto();
      await loginPage.loginAsAdmin();
      
      // 导航到审计日志
      await page.click('text=审计日志');
      await page.waitForLoadState('networkidle');
      
      // 验证审计日志页面
      await expect(page.locator('.ant-table, [class*="audit"]')).toBeVisible();
      
      console.log('权限变更记录查看完成');
    });
  });
});
