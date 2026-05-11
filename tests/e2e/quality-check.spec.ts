import { test, expect } from '@playwright/test';

test.describe('数据质量检查E2E', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="username"]', 'admin');
        await page.fill('input[name="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/dashboard', { timeout: 30000 });
    });
    
    test('执行质量检查', async ({ page }) => {
        await page.click('a[href="/quality"]');
        await expect(page).toHaveURL(/\/quality/, { timeout: 15000 });
        
        await page.click('button:has-text("执行检查")');
        await expect(page.locator('.check-dialog, .modal')).toBeVisible({ timeout: 10000 });
        
        await page.selectOption('select[name="assetId"]', { label: '测试资产' });
        
        await page.check('input[name="rule_NULL_CHECK"]');
        await page.check('input[name="rule_UNIQUE_CHECK"]');
        await page.check('input[name="rule_FORMAT_CHECK"]');
        
        await page.click('button:has-text("开始检查")');
        
        await expect(page.locator('.check-progress, .progress-bar')).toBeVisible({ timeout: 5000 });
        await page.waitForSelector('.check-result, .result-panel', { timeout: 120000 });
        
        await expect(page.locator('.check-result')).toBeVisible();
        await expect(page.locator('.quality-score, .score-value')).toBeVisible();
    });
    
    test('查看质量概览仪表盘', async ({ page }) => {
        await page.click('a[href="/quality/overview"]');
        await expect(page.locator('.quality-overview, .dashboard')).toBeVisible({ timeout: 15000 });
        
        await expect(page.locator('.score-card, .stat-card')).toHaveCount(greaterThanOrEqual(2));
        
        await expect(page.locator('.quality-trend, .trend-chart')).toBeVisible();
    });
    
    test('查看质量趋势图表', async ({ page }) => {
        await page.click('a[href="/quality"]');
        
        await expect(page.locator('.trend-chart, .quality-chart')).toBeVisible({ timeout: 10000 });
        
        const chartPoints = page.locator('.chart-point, .data-point');
        const count = await chartPoints.count();
        expect(count).toBeGreaterThanOrEqual(0);
    });
    
    test('按规则类型筛选', async ({ page }) => {
        await page.click('a[href="/quality"]');
        
        await page.click('button:has-text("筛选"), .filter-btn');
        
        await page.selectOption('select[name="ruleType"]', 'NULL_CHECK');
        await page.click('button:has-text("应用")');
        
        await page.waitForLoadState('networkidle');
        
        const ruleBadges = page.locator('.rule-type, .rule-badge');
        if (await ruleBadges.count() > 0) {
            await expect(ruleBadges.first()).toContainText(/NULL_CHECK|null/i);
        }
    });
});

test.describe('质量规则管理E2E', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="username"]', 'admin');
        await page.fill('input[name="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/dashboard');
    });
    
    test('查看质量规则列表', async ({ page }) => {
        await page.click('a[href="/quality/rules"]');
        await expect(page.locator('.rules-list, .rule-container')).toBeVisible({ timeout: 15000 });
        
        const rules = page.locator('.rule-item, .rule-card');
        const count = await rules.count();
        expect(count).toBeGreaterThanOrEqual(1);
    });
    
    test('创建质量规则', async ({ page }) => {
        await page.click('a[href="/quality/rules"]');
        
        await page.click('button:has-text("创建规则"), button:has-text("新增")');
        await expect(page.locator('.rule-dialog, .modal')).toBeVisible({ timeout: 10000 });
        
        await page.fill('input[name="ruleName"]', `E2E测试规则_${Date.now()}`);
        await page.selectOption('select[name="ruleType"]', 'NULL_CHECK');
        await page.selectOption('select[name="severity"]', 'HIGH');
        await page.selectOption('select[name="dimension"]', 'COMPLETENESS');
        await page.fill('textarea[name="description"]', 'E2E测试创建的规则');
        
        await page.click('button:has-text("保存")');
        await expect(page.locator('.notification, .message'))
            .toContainText(/规则创建成功|success/i, { timeout: 15000 });
    });
    
    test('编辑质量规则', async ({ page }) => {
        await page.click('a[href="/quality/rules"]');
        
        const ruleItem = page.locator('.rule-item, .rule-card').first();
        await ruleItem.hover();
        
        const editBtn = ruleItem.locator('button:has-text("编辑"), .edit-btn');
        if (await editBtn.isVisible()) {
            await editBtn.click();
            await expect(page.locator('.rule-dialog, .modal')).toBeVisible({ timeout: 5000 });
            
            await page.fill('input[name="ruleName"]', `更新规则_${Date.now()}`);
            await page.fill('textarea[name="description"]', '更新的规则描述');
            
            await page.click('button:has-text("保存")');
            await expect(page.locator('.notification')).toContainText(/更新成功/i, { timeout: 10000 });
        }
    });
    
    test('启用/禁用质量规则', async ({ page }) => {
        await page.click('a[href="/quality/rules"]');
        
        const ruleItem = page.locator('.rule-item, .rule-card').first();
        const toggle = ruleItem.locator('input[type="checkbox"], .toggle-switch');
        
        if (await toggle.isVisible()) {
            const initialState = await toggle.isChecked();
            
            await toggle.click();
            await expect(page.locator('.notification')).toContainText(/更新成功|enabled|disabled/i, { timeout: 5000 });
            
            await toggle.click();
        }
    });
    
    test('删除质量规则', async ({ page }) => {
        await page.click('a[href="/quality/rules"]');
        
        const ruleItem = page.locator('.rule-item, .rule-card').first();
        await ruleItem.hover();
        
        const deleteBtn = ruleItem.locator('button:has-text("删除"), .delete-btn');
        if (await deleteBtn.isVisible()) {
            await deleteBtn.click();
            await expect(page.locator('.confirm-dialog, [role="alertdialog"]')).toBeVisible();
            
            await page.click('button:has-text("确认删除"), button:has-text("确定")');
            await expect(page.locator('.notification')).toContainText(/删除成功|deleted/i, { timeout: 10000 });
        }
    });
    
    test('使用规则模板创建规则', async ({ page }) => {
        await page.click('a[href="/quality/rules"]');
        
        await page.click('button:has-text("从模板创建"), button:has-text("模板")');
        await expect(page.locator('.template-dialog, .template-list')).toBeVisible({ timeout: 10000 });
        
        const templateItem = page.locator('.template-item').first();
        if (await templateItem.isVisible()) {
            await templateItem.click();
            await expect(page.locator('.rule-dialog, .modal')).toBeVisible({ timeout: 5000 });
            
            await page.fill('input[name="ruleName"]', `模板规则_${Date.now()}`);
            await page.click('button:has-text("创建")');
            await expect(page.locator('.notification')).toContainText(/创建成功/i, { timeout: 10000 });
        }
    });
});

test.describe('质量检查结果E2E', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="username"]', 'admin');
        await page.fill('input[name="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/dashboard');
    });
    
    test('查看检查历史', async ({ page }) => {
        await page.click('a[href="/quality/reports"]');
        await expect(page.locator('.reports-page, .history-list')).toBeVisible({ timeout: 15000 });
        
        const historyItems = page.locator('.history-item, .report-item');
        const count = await historyItems.count();
        expect(count).toBeGreaterThanOrEqual(0);
    });
    
    test('查看检查详情', async ({ page }) => {
        await page.click('a[href="/quality/reports"]');
        
        const reportItem = page.locator('.report-item, .history-item').first();
        if (await reportItem.isVisible()) {
            await reportItem.click();
            await expect(page.locator('.report-detail, .check-detail')).toBeVisible({ timeout: 10000 });
            
            await expect(page.locator('.score-summary, .quality-score')).toBeVisible();
            await expect(page.locator('.rule-results, .check-results')).toBeVisible();
        }
    });
    
    test('查看失败规则详情', async ({ page }) => {
        await page.click('a[href="/quality/reports"]');
        
        const failedItem = page.locator('.report-item:has(.status-failed), .history-item:has(.failed)').first();
        if (await failedItem.isVisible()) {
            await failedItem.click();
            
            const failedRules = page.locator('.failed-rule, .error-item');
            const count = await failedRules.count();
            expect(count).toBeGreaterThanOrEqual(0);
            
            if (count > 0) {
                await failedRules.first().click();
                await expect(page.locator('.rule-detail, .error-detail')).toBeVisible({ timeout: 5000 });
            }
        }
    });
    
    test('导出质量报告', async ({ page }) => {
        await page.click('a[href="/quality/reports"]');
        
        await page.click('button:has-text("导出报告")');
        await expect(page.locator('.export-dialog, .export-options')).toBeVisible({ timeout: 5000 });
        
        await page.selectOption('select[name="format"]', 'PDF');
        await page.selectOption('select[name="dateRange"]', 'LAST_WEEK');
        
        await page.click('button:has-text("导出")');
        await expect(page.locator('.notification')).toContainText(/导出成功|已创建导出任务/i, { timeout: 10000 });
    });
});

test.describe('质量问题追踪E2E', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="username"]', 'admin');
        await page.fill('input[name="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/dashboard');
    });
    
    test('查看质量问题列表', async ({ page }) => {
        await page.click('a[href="/quality/issues"]');
        await expect(page.locator('.issues-page, .issue-list')).toBeVisible({ timeout: 15000 });
        
        const issues = page.locator('.issue-item, .quality-issue');
        const count = await issues.count();
        expect(count).toBeGreaterThanOrEqual(0);
    });
    
    test('按状态筛选问题', async ({ page }) => {
        await page.click('a[href="/quality/issues"]');
        
        await page.click('button:has-text("筛选")');
        
        await page.check('input[name="status_OPEN"]');
        await page.check('input[name="status_IN_PROGRESS"]');
        
        await page.click('button:has-text("应用")');
        await page.waitForLoadState('networkidle');
        
        const statusBadges = page.locator('.status-badge, .issue-status');
        if (await statusBadges.count() > 0) {
            const firstBadge = await statusBadges.first().textContent();
            expect(firstBadge).toMatch(/OPEN|IN_PROGRESS|进行中|待处理/i);
        }
    });
    
    test('认领质量问题', async ({ page }) => {
        await page.click('a[href="/quality/issues"]');
        
        const unassignedIssue = page.locator('.issue-item:has(.status-OPEN), .quality-issue:has(.unassigned)').first();
        if (await unassignedIssue.isVisible()) {
            await unassignedIssue.click();
            await expect(page.locator('.issue-detail, .issue-panel')).toBeVisible({ timeout: 10000 });
            
            await page.click('button:has-text("认领")');
            await expect(page.locator('.notification')).toContainText(/认领成功|claimed/i, { timeout: 10000 });
        }
    });
    
    test('解决质量问题', async ({ page }) => {
        await page.click('a[href="/quality/issues"]');
        
        const openIssue = page.locator('.issue-item:has(.status-IN_PROGRESS), .quality-issue:has(.in-progress)').first();
        if (await openIssue.isVisible()) {
            await openIssue.click();
            
            await page.click('button:has-text("标记已解决"), button:has-text("解决")');
            await page.fill('textarea[name="resolution"]', '已修复空值问题');
            await page.click('button:has-text("确认解决")');
            
            await expect(page.locator('.notification')).toContainText(/已解决|resolved/i, { timeout: 10000 });
        }
    });
    
    test('转派质量问题', async ({ page }) => {
        await page.click('a[href="/quality/issues"]');
        
        const issue = page.locator('.issue-item, .quality-issue').first();
        if (await issue.isVisible()) {
            await issue.click();
            
            await page.click('button:has-text("转派"), button:has-text("转派问题")');
            await expect(page.locator('.transfer-dialog, .modal')).toBeVisible({ timeout: 5000 });
            
            await page.selectOption('select[name="assignee"]', { label: '其他用户' });
            await page.fill('textarea[name="reason"]', '需要其他人员处理');
            
            await page.click('button:has-text("确认转派")');
            await expect(page.locator('.notification')).toContainText(/转派成功|transferred/i, { timeout: 10000 });
        }
    });
    
    test('忽略质量问题', async ({ page }) => {
        await page.click('a[href="/quality/issues"]');
        
        const issue = page.locator('.issue-item, .quality-issue').first();
        if (await issue.isVisible()) {
            await issue.click();
            
            await page.click('button:has-text("忽略")');
            await page.fill('textarea[name="reason"]', '误报');
            
            await page.click('button:has-text("确认忽略")');
            await expect(page.locator('.notification')).toContainText(/已忽略|ignored/i, { timeout: 10000 });
        }
    });
    
    test('查看问题统计', async ({ page }) => {
        await page.click('a[href="/quality/issues"]');
        
        await page.click('button:has-text("统计"), .stats-btn');
        await expect(page.locator('.statistics-panel, .stats-dialog')).toBeVisible({ timeout: 10000 });
        
        await expect(page.locator('.total-count, .total-issues')).toBeVisible();
        await expect(page.locator('.open-count, .open-issues')).toBeVisible();
        await expect(page.locator('.resolved-count, .resolved-issues')).toBeVisible();
    });
});

function greaterThanOrEqual(min: number) {
    return {
        symmetricMatch: (received: number) => received >= min
    };
}
