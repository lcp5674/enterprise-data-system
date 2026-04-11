import { test, expect } from '@playwright/test';

/**
 * 数据治理与审批流E2E测试
 */
test.describe('数据治理E2E测试', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
  });

  test('查看治理仪表盘', async ({ page }) => {
    await page.click('a[href="/governance"]');
    await expect(page).toHaveURL('/governance');

    // 验证统计卡片
    await expect(page.locator('.stat-card')).toHaveCount({ minimum: 4 });
    await expect(page.locator('.stat-card:has-text("策略总数")')).toBeVisible();
    await expect(page.locator('.stat-card:has-text("待处理任务")')).toBeVisible();
  });

  test('创建治理策略', async ({ page }) => {
    await page.goto('/governance/policies');
    await page.click('button:has-text("新建策略")');

    // 填写表单
    await page.fill('input[name="policyCode"]', `POL-TEST-${Date.now()}`);
    await page.fill('input[name="policyName"]', '测试治理策略');
    await page.selectOption('select[name="policyType"]', 'QUALITY');
    await page.fill('textarea[name="description"]', '测试策略描述');
    await page.fill('input[name="targetAssets"]', 'ASSET-001,ASSET-002');

    await page.click('button:has-text("保存")');
    await expect(page.locator('.success-message')).toContainText('策略创建成功');
  });

  test('启用/禁用策略', async ({ page }) => {
    await page.goto('/governance/policies');

    // 获取第一个策略的切换按钮
    const toggleBtn = page.locator('.policy-item .toggle-btn').first();
    const initialState = await toggleBtn.textContent();

    await toggleBtn.click();
    await page.click('.confirm-dialog button:has-text("确认")');

    await expect(page.locator('.success-message')).toBeVisible();
  });

  test('AI推荐建议', async ({ page }) => {
    await page.goto('/governance/recommendations/ASSET-001');

    await expect(page.locator('.recommendation-list')).toBeVisible();
    await expect(page.locator('.recommendation-item')).toHaveCount({ minimum: 1 });

    // 验证推荐详情
    await page.locator('.recommendation-item').first().click();
    await expect(page.locator('.recommendation-detail')).toBeVisible();
  });

  test('质量趋势分析', async ({ page }) => {
    await page.goto('/governance/analysis/ASSET-001');

    await expect(page.locator('.trend-chart')).toBeVisible();
    await expect(page.locator('.issue-summary')).toBeVisible();
  });
});

test.describe('审批流E2E测试', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
  });

  test('发起审批流程', async ({ page }) => {
    await page.goto('/workflow/start');

    // 选择流程类型
    await page.selectOption('select[name="workflowType"]', 'ASSET_ACCESS');
    await page.fill('input[name="assetId"]', 'ASSET-001');
    await page.fill('textarea[name="reason"]', '业务需要访问数据');

    // 选择审批人
    await page.selectOption('select[name="approver"]', { label: '部门主管' });

    await page.click('button:has-text("提交")');

    await expect(page.locator('.success-message')).toContainText('审批流程已提交');
    await expect(page.locator('.workflow-id')).toBeVisible();
  });

  test('查看待我审批', async ({ page }) => {
    await page.goto('/workflow/pending');

    await expect(page.locator('.workflow-list')).toBeVisible();

    const workflows = page.locator('.workflow-item');
    const count = await workflows.count();

    if (count > 0) {
      await expect(workflows.first()).toBeVisible();
    }
  });

  test('审批通过', async ({ page }) => {
    await page.goto('/workflow/pending');
    await page.locator('.workflow-item').first().click();

    await page.fill('textarea[name="comment"]', '同意该申请');
    await page.click('button:has-text("通过")');

    await expect(page.locator('.success-message')).toContainText('审批已通过');
  });

  test('审批拒绝', async ({ page }) => {
    await page.goto('/workflow/pending');
    await page.locator('.workflow-item').first().click();

    await page.fill('textarea[name="comment"]', '申请理由不充分');
    await page.click('button:has-text("拒绝")');

    await expect(page.locator('.success-message')).toContainText('审批已拒绝');
  });

  test('审批流程查询', async ({ page }) => {
    await page.goto('/workflow/history');

    await page.fill('input[name="workflowId"]', 'WF-001');
    await page.click('button:has-text("查询")');

    await expect(page.locator('.workflow-detail')).toBeVisible();
  });

  test('查看流程图', async ({ page }) => {
    await page.goto('/workflow/WF-001');

    await expect(page.locator('.workflow-diagram')).toBeVisible();
    await expect(page.locator('.node')).toHaveCount({ minimum: 2 });
  });
});

test.describe('数据标准E2E测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
  });

  test('查看数据标准列表', async ({ page }) => {
    await page.click('a[href="/standards"]');
    await expect(page).toHaveURL('/standards');

    await expect(page.locator('.standard-list')).toBeVisible();
    await expect(page.locator('.standard-item')).toHaveCount({ minimum: 1 });
  });

  test('创建数据标准', async ({ page }) => {
    await page.goto('/standards');
    await page.click('button:has-text("新建标准")');

    await page.fill('input[name="standardCode"]', `STD-TEST-${Date.now()}`);
    await page.fill('input[name="standardName"]', '测试数据标准');
    await page.selectOption('select[name="standardType"]', 'FORMAT');
    await page.fill('textarea[name="ruleExpression"]', '^1[3-9]\\d{9}$');

    await page.click('button:has-text("保存")');
    await expect(page.locator('.success-message')).toContainText('标准创建成功');
  });

  test('执行合规检查', async ({ page }) => {
    await page.goto('/standards/compliance');

    await page.selectOption('select[name="assetId"]', 'ASSET-001');
    await page.selectOption('select[name="standardId"]', '1');
    await page.click('button:has-text("执行检查")');

    // 等待检查结果
    await page.waitForSelector('.check-result', { timeout: 60000 });
    await expect(page.locator('.check-result')).toBeVisible();
  });
});

test.describe('血缘关系E2E测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
  });

  test('查看资产血缘', async ({ page }) => {
    await page.goto('/lineage/ASSET-001');

    await expect(page.locator('.lineage-graph')).toBeVisible();
    await expect(page.locator('.node')).toHaveCount({ minimum: 1 });
  });

  test('血缘关系可视化', async ({ page }) => {
    await page.goto('/lineage/ASSET-001');

    // 验证图形渲染
    await expect(page.locator('.lineage-canvas, .lineage-graph svg')).toBeVisible();

    // 点击节点查看详情
    await page.locator('.node').first().click();
    await expect(page.locator('.node-detail')).toBeVisible();
  });

  test('上游/下游血缘', async ({ page }) => {
    await page.goto('/lineage/ASSET-001');

    // 查看上游
    await page.click('button:has-text("上游血缘")');
    await expect(page.locator('.lineage-graph')).toBeVisible();

    // 查看下游
    await page.click('button:has-text("下游血缘")');
    await expect(page.locator('.lineage-graph')).toBeVisible();
  });

  test('影响分析', async ({ page }) => {
    await page.goto('/lineage/ASSET-001/impact');

    await expect(page.locator('.impact-analysis')).toBeVisible();
    await expect(page.locator('.affected-asset')).toHaveCount({ minimum: 0 });
  });
});
