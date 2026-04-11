import { test, expect } from '@playwright/test';

/**
 * 用户管理E2E测试
 */
test.describe('用户管理模块E2E测试', () => {

  test.beforeEach(async ({ page }) => {
    // 登录
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL('/dashboard');

    // 导航到用户管理
    await page.click('a[href="/admin/users"]');
    await expect(page).toHaveURL('/admin/users');
  });

  test('用户列表加载', async ({ page }) => {
    // 等待表格加载
    await expect(page.locator('.data-table')).toBeVisible();
    await expect(page.locator('.data-table tbody tr')).toHaveCount({ minimum: 1 });
  });

  test('分页功能', async ({ page }) => {
    // 检查分页控件
    await expect(page.locator('.pagination')).toBeVisible();

    // 点击下一页
    const nextButton = page.locator('.pagination .next');
    if (await nextButton.isEnabled()) {
      await nextButton.click();
      await expect(page.locator('.pagination .active')).toContainText('2');
    }
  });

  test('搜索用户', async ({ page }) => {
    // 输入搜索关键词
    await page.fill('input[name="search"]', 'admin');
    await page.click('button:has-text("搜索")');

    // 等待搜索结果
    await expect(page.locator('.data-table tbody tr')).toHaveCount({ minimum: 1 });
    await expect(page.locator('.data-table tbody tr:first-child')).toContainText('admin');
  });

  test('创建新用户', async ({ page }) => {
    // 点击新建按钮
    await page.click('button:has-text("新建用户")');

    // 填写表单
    const timestamp = Date.now();
    await page.fill('input[name="username"]', `newuser_${timestamp}`);
    await page.fill('input[name="email"]', `newuser_${timestamp}@example.com`);
    await page.fill('input[name="realName"]', '新用户');
    await page.fill('input[name="password"]', 'Password123');
    await page.selectOption('select[name="department"]', { label: 'IT部门' });
    await page.selectOption('select[name="role"]', { label: '普通用户' });

    // 提交
    await page.click('button:has-text("保存")');

    // 验证成功
    await expect(page.locator('.success-message')).toContainText('用户创建成功');
  });

  test('编辑用户', async ({ page }) => {
    // 点击编辑按钮（第一行）
    await page.locator('.data-table tbody tr:first-child .edit-btn').click();

    // 修改姓名
    await page.fill('input[name="realName"]', '更新后的姓名');

    // 保存
    await page.click('button:has-text("保存")');

    // 验证更新成功
    await expect(page.locator('.success-message')).toContainText('用户更新成功');
  });

  test('删除用户', async ({ page }) => {
    // 记录删除前数量
    const initialCount = await page.locator('.data-table tbody tr').count();

    // 点击删除按钮
    await page.locator('.data-table tbody tr:first-child .delete-btn').click();

    // 确认删除
    await page.click('.confirm-dialog button:has-text("确认")');

    // 验证删除成功
    await expect(page.locator('.success-message')).toContainText('用户删除成功');
    await expect(page.locator('.data-table tbody tr')).toHaveCount(initialCount - 1);
  });

  test('禁用/启用用户', async ({ page }) => {
    // 点击启用/禁用按钮
    const toggleButton = page.locator('.data-table tbody tr:first-child .toggle-btn');
    await toggleButton.click();

    // 确认操作
    await page.click('.confirm-dialog button:has-text("确认")');

    // 验证状态变更
    await expect(page.locator('.success-message')).toBeVisible();
  });

  test('重置密码', async ({ page }) => {
    // 点击重置密码按钮
    await page.locator('.data-table tbody tr:first-child .reset-password-btn').click();

    // 输入新密码
    await page.fill('input[name="newPassword"]', 'NewPassword123');
    await page.fill('input[name="confirmPassword"]', 'NewPassword123');

    // 确认
    await page.click('button:has-text("确认")');

    // 验证
    await expect(page.locator('.success-message')).toContainText('密码重置成功');
  });

  test('分配角色', async ({ page }) => {
    // 点击分配角色按钮
    await page.locator('.data-table tbody tr:first-child .assign-role-btn').click();

    // 选择角色
    await page.check('input[name="role_1"]'); // 管理员
    await page.check('input[name="role_2"]'); // 普通用户

    // 保存
    await page.click('button:has-text("保存")');

    // 验证
    await expect(page.locator('.success-message')).toContainText('角色分配成功');
  });
});

test.describe('部门管理E2E测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.click('a[href="/admin/departments"]');
  });

  test('部门列表', async ({ page }) => {
    await expect(page.locator('.department-tree')).toBeVisible();
  });

  test('创建部门', async ({ page }) => {
    await page.click('button:has-text("新建部门")');
    await page.fill('input[name="departmentName"]', '测试部门');
    await page.click('button:has-text("保存")');
    await expect(page.locator('.success-message')).toContainText('部门创建成功');
  });
});
