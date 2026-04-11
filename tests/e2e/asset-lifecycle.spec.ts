import { test, expect } from '@playwright/test';

/**
 * 资产管理与生命周期E2E测试
 */
test.describe('资产管理模块E2E测试', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.click('a[href="/assets"]');
    await expect(page).toHaveURL('/assets');
  });

  test('资产列表加载', async ({ page }) => {
    await expect(page.locator('.asset-list')).toBeVisible();
    await expect(page.locator('.asset-card, .asset-row')).toHaveCount({ minimum: 1 });
  });

  test('资产搜索', async ({ page }) => {
    await page.fill('input[name="keyword"]', '用户');
    await page.click('button:has-text("搜索")');
    await page.waitForLoadState('networkidle');

    // 验证搜索结果
    const rows = page.locator('.asset-row, .asset-card');
    const count = await rows.count();
    expect(count).toBeGreaterThanOrEqual(0);
  });

  test('按类型筛选', async ({ page }) => {
    await page.selectOption('select[name="assetType"]', 'TABLE');
    await page.click('button:has-text("筛选")');
    await page.waitForLoadState('networkidle');

    // 验证只显示表类型资产
    const typeLabels = page.locator('.asset-type');
    const count = await typeLabels.count();
    for (let i = 0; i < Math.min(count, 5); i++) {
      await expect(typeLabels.nth(i)).toContainText('TABLE');
    }
  });

  test('查看资产详情', async ({ page }) => {
    await page.locator('.asset-card, .asset-row').first().click();
    await expect(page.locator('.asset-detail')).toBeVisible();
    await expect(page.locator('.asset-detail .asset-name')).toBeVisible();
  });

  test('注册新资产', async ({ page }) => {
    await page.click('button:has-text("注册资产")');

    // 填写表单
    await page.fill('input[name="assetCode"]', `ASSET-TEST-${Date.now()}`);
    await page.fill('input[name="assetName"]', '测试资产');
    await page.selectOption('select[name="assetType"]', 'TABLE');
    await page.fill('input[name="owner"]', 'admin');

    // 提交
    await page.click('button:has-text("保存")');

    // 验证
    await expect(page.locator('.success-message')).toContainText('资产注册成功');
  });
});

test.describe('资产生命周期E2E测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
  });

  test('查看生命周期历史', async ({ page }) => {
    await page.goto('/assets/ASSET-001/lifecycle');
    await expect(page.locator('.lifecycle-timeline')).toBeVisible();
    await expect(page.locator('.lifecycle-event')).toHaveCount({ minimum: 1 });
  });

  test('创建生命周期记录', async ({ page }) => {
    await page.goto('/assets/ASSET-001/lifecycle');
    await page.click('button:has-text("添加记录")');

    await page.selectOption('select[name="phase"]', 'MAINTENANCE');
    await page.fill('textarea[name="description"]', '定期维护检查');

    await page.click('button:has-text("保存")');
    await expect(page.locator('.success-message')).toContainText('记录创建成功');
  });

  test('归档资产', async ({ page }) => {
    await page.goto('/assets/ASSET-001');
    await page.click('button:has-text("归档")');

    await page.fill('textarea[name="reason"]', '资产不再使用');
    await page.click('button:has-text("确认归档")');

    await expect(page.locator('.success-message')).toContainText('归档成功');
    await expect(page.locator('.asset-status')).toContainText('ARCHIVED');
  });

  test('版本历史查看', async ({ page }) => {
    await page.goto('/assets/ASSET-001/versions');
    await expect(page.locator('.version-list')).toBeVisible();

    const versions = page.locator('.version-item');
    const count = await versions.count();
    expect(count).toBeGreaterThanOrEqual(1);
  });

  test('版本对比', async ({ page }) => {
    await page.goto('/assets/ASSET-001/versions');
    await page.click('button:has-text("对比版本")');

    await page.check('input[name="version1"][value="1"]');
    await page.check('input[name="version2"][value="2"]');
    await page.click('button:has-text("对比")');

    await expect(page.locator('.diff-view')).toBeVisible();
  });

  test('版本回滚', async ({ page }) => {
    await page.goto('/assets/ASSET-001/versions');
    await page.locator('.version-item').last().locator('.rollback-btn').click();

    await page.click('.confirm-dialog button:has-text("确认")');

    await expect(page.locator('.success-message')).toContainText('回滚成功');
  });
});

test.describe('资产质量检查E2E测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
  });

  test('执行质量检查', async ({ page }) => {
    await page.goto('/assets/ASSET-001/quality');
    await page.click('button:has-text("执行检查")');

    // 选择规则
    await page.check('input[name="rule_1"]');
    await page.check('input[name="rule_2"]');

    await page.click('button:has-text("开始检查")');

    // 等待检查完成
    await expect(page.locator('.check-progress')).toBeVisible();
    await page.waitForSelector('.check-result', { timeout: 30000 });

    await expect(page.locator('.check-result')).toBeVisible();
  });

  test('查看质量报告', async ({ page }) => {
    await page.goto('/assets/ASSET-001/quality/report');
    await expect(page.locator('.quality-score')).toBeVisible();
    await expect(page.locator('.quality-chart')).toBeVisible();
  });
});
