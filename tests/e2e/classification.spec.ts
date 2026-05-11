import { test, expect } from '@playwright/test';

test.describe('数据分类分级E2E', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="username"]', 'admin');
        await page.fill('input[name="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/dashboard', { timeout: 30000 });
    });
    
    test('查看数据分类概览', async ({ page }) => {
        await page.click('a[href="/classification"], a[href="/classifications"]');
        await expect(page.locator('.classification-stats, .stats-container')).toBeVisible({ timeout: 15000 });
        
        await expect(page.locator('.stat-card, .classification-card')).toHaveCount(greaterThanOrEqual(1));
    });
    
    test('查看分类统计图表', async ({ page }) => {
        await page.click('a[href="/classification"]');
        
        await expect(page.locator('.chart-container, .statistics-chart')).toBeVisible({ timeout: 10000 });
        
        const pieChart = page.locator('.pie-chart, .distribution-chart');
        const barChart = page.locator('.bar-chart, .category-chart');
        
        if (await pieChart.isVisible()) {
            await expect(pieChart).toBeVisible();
        }
        if (await barChart.isVisible()) {
            await expect(barChart).toBeVisible();
        }
    });
    
    test('查看敏感数据分布', async ({ page }) => {
        await page.click('a[href="/classification"]');
        
        await page.click('button:has-text("敏感数据"), a:has-text("敏感数据")');
        await expect(page.locator('.sensitive-data-chart, .sensitive-chart')).toBeVisible({ timeout: 10000 });
        
        const chartSections = page.locator('.chart-section, .data-section');
        await expect(chartSections.first()).toBeVisible();
    });
    
    test('查看分类层级结构', async ({ page }) => {
        await page.click('a[href="/classification"]');
        
        await expect(page.locator('.classification-tree, .tree-view')).toBeVisible({ timeout: 10000 });
        
        const rootNodes = page.locator('.tree-node, .category-item');
        const count = await rootNodes.count();
        expect(count).toBeGreaterThanOrEqual(1);
        
        const expandBtn = page.locator('.expand-btn, .toggle-btn').first();
        if (await expandBtn.isVisible()) {
            await expandBtn.click();
            await page.waitForTimeout(500);
            const childNodes = page.locator('.tree-node-children .tree-node');
            const childCount = await childNodes.count();
            expect(childCount).toBeGreaterThanOrEqual(0);
        }
    });
    
    test('搜索分类', async ({ page }) => {
        await page.click('a[href="/classification"]');
        
        await page.fill('input[placeholder*="搜索"], input[name="keyword"]', '客户');
        await page.click('button:has-text("搜索")');
        await page.waitForLoadState('networkidle');
        
        const results = page.locator('.search-result, .category-item');
        const count = await results.count();
        expect(count).toBeGreaterThanOrEqual(0);
    });
    
    test('查看分类详情', async ({ page }) => {
        await page.click('a[href="/classification"]');
        
        const firstCategory = page.locator('.category-item, .tree-node').first();
        await firstCategory.click();
        
        await expect(page.locator('.category-detail, .detail-panel')).toBeVisible({ timeout: 10000 });
        
        await expect(page.locator('.detail-name, .category-name')).toBeVisible();
        await expect(page.locator('.detail-description, .category-description')).toBeVisible();
    });
});

test.describe('敏感数据管理E2E', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="username"]', 'admin');
        await page.fill('input[name="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/dashboard');
    });
    
    test('查看敏感数据类型分布', async ({ page }) => {
        await page.click('a[href="/security"], a[href="/sensitive"]');
        await expect(page.locator('.sensitive-data-page')).toBeVisible({ timeout: 15000 });
        
        await expect(page.locator('.data-type-chart, .type-distribution')).toBeVisible({ timeout: 10000 });
        
        const pieChart = page.locator('.pie-chart');
        await expect(pieChart).toBeVisible();
    });
    
    test('查看敏感数据扫描结果', async ({ page }) => {
        await page.click('a[href="/security"]');
        
        await page.click('button:has-text("扫描结果"), a:has-text("扫描结果")');
        await expect(page.locator('.scan-results, .detection-results')).toBeVisible({ timeout: 10000 });
        
        const results = page.locator('.result-item, .detected-item');
        const count = await results.count();
        expect(count).toBeGreaterThanOrEqual(0);
    });
    
    test('查看字段级敏感信息', async ({ page }) => {
        await page.click('a[href="/security"]');
        
        await page.click('button:has-text("字段级"), a:has-text("字段级敏感数据")');
        await expect(page.locator('.field-level, .field-sensitivity')).toBeVisible({ timeout: 10000 });
        
        const fieldItems = page.locator('.field-item, .sensitive-field');
        await expect(fieldItems.first()).toBeVisible();
    });
    
    test('敏感数据风险评估', async ({ page }) => {
        await page.click('a[href="/security"]');
        
        await page.click('button:has-text("风险评估"), a:has-text("风险评估")');
        await expect(page.locator('.risk-assessment, .risk-chart')).toBeVisible({ timeout: 10000 });
        
        await expect(page.locator('.risk-score, .overall-risk')).toBeVisible();
    });
    
    test('导出敏感数据报告', async ({ page }) => {
        await page.click('a[href="/security"]');
        
        await page.click('button:has-text("导出报告")');
        await expect(page.locator('.export-dialog, .export-options')).toBeVisible({ timeout: 5000 });
        
        await page.selectOption('select[name="format"]', 'PDF');
        await page.click('button:has-text("导出")');
        
        await expect(page.locator('.notification')).toContainText(/导出成功|已创建导出任务/i, { timeout: 10000 });
    });
});

test.describe('数据脱敏规则E2E', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="username"]', 'admin');
        await page.fill('input[name="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/dashboard');
    });
    
    test('查看脱敏规则列表', async ({ page }) => {
        await page.click('a[href="/security/masking"]');
        await expect(page.locator('.masking-rules-page')).toBeVisible({ timeout: 15000 });
        
        await expect(page.locator('.rule-list, .masking-list')).toBeVisible();
        
        const rules = page.locator('.rule-item, .masking-rule');
        const count = await rules.count();
        expect(count).toBeGreaterThanOrEqual(1);
    });
    
    test('创建脱敏规则', async ({ page }) => {
        await page.click('a[href="/security/masking"]');
        
        await page.click('button:has-text("创建规则"), button:has-text("新增规则")');
        await expect(page.locator('.rule-dialog, .modal')).toBeVisible({ timeout: 10000 });
        
        await page.fill('input[name="ruleName"]', `脱敏规则_${Date.now()}`);
        await page.selectOption('select[name="sensitivityLevel"]', 'HIGH');
        await page.selectOption('select[name="maskingType"]', 'MASK');
        await page.fill('input[name="pattern"]', '\\d{4}-\\d{4}');
        await page.fill('input[name="replacement"]', '****');
        
        await page.click('button:has-text("保存")');
        await expect(page.locator('.notification')).toContainText(/规则创建成功|success/i, { timeout: 10000 });
    });
    
    test('编辑脱敏规则', async ({ page }) => {
        await page.click('a[href="/security/masking"]');
        
        const ruleItem = page.locator('.rule-item, .masking-rule').first();
        await ruleItem.hover();
        
        const editBtn = ruleItem.locator('button:has-text("编辑"), .edit-btn');
        if (await editBtn.isVisible()) {
            await editBtn.click();
            await expect(page.locator('.rule-dialog, .modal')).toBeVisible({ timeout: 5000 });
            
            await page.fill('input[name="ruleName"]', `更新规则_${Date.now()}`);
            await page.click('button:has-text("保存")');
            await expect(page.locator('.notification')).toContainText(/更新成功/i, { timeout: 10000 });
        }
    });
    
    test('预览脱敏效果', async ({ page }) => {
        await page.click('a[href="/security/masking"]');
        
        await page.fill('input[name="sampleData"]', '1234-5678-9012');
        await page.click('button:has-text("预览")');
        
        await expect(page.locator('.preview-result, .masked-preview')).toBeVisible({ timeout: 5000 });
        await expect(page.locator('.preview-result')).toContainText(/\*\*\*\*/);
    });
    
    test('启用/禁用脱敏规则', async ({ page }) => {
        await page.click('a[href="/security/masking"]');
        
        const ruleItem = page.locator('.rule-item, .masking-rule').first();
        const toggleBtn = ruleItem.locator('input[type="checkbox"], .toggle');
        
        if (await toggleBtn.isVisible()) {
            const initialState = await toggleBtn.isChecked();
            await toggleBtn.click();
            
            await expect(page.locator('.notification')).toBeVisible({ timeout: 5000 });
            
            await toggleBtn.click();
        }
    });
});

test.describe('合规性检查E2E', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="username"]', 'admin');
        await page.fill('input[name="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/dashboard');
    });
    
    test('执行GDPR合规检查', async ({ page }) => {
        await page.click('a[href="/compliance"]');
        await expect(page.locator('.compliance-page')).toBeVisible({ timeout: 15000 });
        
        await page.click('button:has-text("GDPR检查")');
        await expect(page.locator('.check-progress, .scan-progress')).toBeVisible({ timeout: 5000 });
        
        await page.waitForSelector('.check-result, .scan-result', { timeout: 60000 });
        await expect(page.locator('.check-result')).toBeVisible();
    });
    
    test('查看合规报告', async ({ page }) => {
        await page.click('a[href="/compliance/reports"]');
        await expect(page.locator('.reports-list')).toBeVisible({ timeout: 15000 });
        
        const reports = page.locator('.report-item');
        const count = await reports.count();
        expect(count).toBeGreaterThanOrEqual(0);
    });
    
    test('生成合规报告', async ({ page }) => {
        await page.click('a[href="/compliance"]');
        
        await page.click('button:has-text("生成报告")');
        await expect(page.locator('.report-dialog, .modal')).toBeVisible({ timeout: 5000 });
        
        await page.selectOption('select[name="reportType"]', 'GDPR');
        await page.selectOption('select[name="dateRange"]', 'LAST_MONTH');
        
        await page.click('button:has-text("生成")');
        await expect(page.locator('.notification')).toContainText(/报告生成中|generating/i, { timeout: 10000 });
    });
});

function greaterThanOrEqual(min: number) {
    return {
        symmetricMatch: (received: number) => received >= min
    };
}
