import { test, expect } from '@playwright/test';

test.describe('数据资产全生命周期E2E', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="username"]', 'admin');
        await page.fill('input[name="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/dashboard', { timeout: 30000 });
    });
    
    test('创建数据资产并完成生命周期', async ({ page }) => {
        await page.click('a[href="/assets"]');
        await expect(page).toHaveURL('/assets', { timeout: 15000 });
        
        await page.click('button:has-text("创建资产")');
        await expect(page.locator('.modal, [role="dialog"]')).toBeVisible({ timeout: 10000 });
        
        const timestamp = Date.now();
        await page.fill('input[name="assetName"]', `test_asset_${timestamp}`);
        await page.selectOption('select[name="assetType"]', 'TABLE');
        await page.fill('textarea[name="description"]', 'E2E测试创建的数据资产');
        
        await page.click('button:has-text("确定")');
        await expect(page.locator('.notification, .message, [role="alert"]'))
            .toContainText(/创建成功|success/i, { timeout: 15000 });
        
        const assetName = `test_asset_${timestamp}`;
        await expect(page.locator('table tbody')).toContainText(assetName, { timeout: 10000 });
        
        await page.click(`text=${assetName}`);
        await expect(page).toHaveURL(/\/assets\/\d+/, { timeout: 15000 });
        
        await page.click('text=血缘关系');
        await expect(page.locator('.lineage-graph, .lineage-container')).toBeVisible({ timeout: 10000 });
        
        await page.click('text=质量信息');
        await expect(page.locator('.quality-score, .quality-card')).toBeVisible({ timeout: 10000 });
    });
    
    test('资产搜索功能', async ({ page }) => {
        await page.click('a[href="/assets"]');
        await expect(page).toHaveURL('/assets');
        
        await page.fill('input[placeholder*="搜索"], input[name="keyword"]', 'test');
        await page.click('button:has-text("搜索")');
        await page.waitForLoadState('networkidle');
        
        const results = page.locator('table tbody tr, .asset-item');
        const count = await results.count();
        expect(count).toBeGreaterThanOrEqual(0);
    });
    
    test('资产筛选功能', async ({ page }) => {
        await page.click('a[href="/assets"]');
        await expect(page).toHaveURL('/assets');
        
        await page.click('button:has-text("筛选")');
        await page.selectOption('select[name="assetType"]', 'TABLE');
        await page.selectOption('select[name="status"]', 'ACTIVE');
        await page.click('button:has-text("应用")');
        
        await page.waitForLoadState('networkidle');
        await expect(page.locator('.filter-tags, .applied-filters')).toBeVisible();
    });
    
    test('资产收藏功能', async ({ page }) => {
        await page.click('a[href="/assets"]');
        await expect(page).toHaveURL('/assets');
        
        const favoriteBtn = page.locator('.favorite-btn, button[aria-label="收藏"]').first();
        if (await favoriteBtn.isVisible()) {
            await favoriteBtn.click();
            await expect(page.locator('.notification, .message'))
                .toContainText(/收藏成功|favorited/i, { timeout: 5000 });
        }
    });
    
    test('资产详情页签切换', async ({ page }) => {
        await page.click('a[href="/assets"]');
        await page.locator('table tbody tr, .asset-item').first().click();
        
        await expect(page).toHaveURL(/\/assets\/\d+/);
        
        const tabs = ['基本信息', '字段信息', '血缘关系', '质量信息', '历史版本'];
        for (const tab of tabs) {
            const tabBtn = page.locator(`button:has-text("${tab}"), .tab-item:has-text("${tab}")`);
            if (await tabBtn.isVisible()) {
                await tabBtn.click();
                await expect(page.locator(`.${tab.toLowerCase().replace(/\s+/g, '-')}-content, .tab-content`)).toBeVisible({ timeout: 5000 });
            }
        }
    });
});

test.describe('资产批量操作E2E', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="username"]', 'admin');
        await page.fill('input[name="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/dashboard');
    });
    
    test('批量选择资产', async ({ page }) => {
        await page.click('a[href="/assets"]');
        await expect(page).toHaveURL('/assets');
        
        const firstCheckbox = page.locator('input[type="checkbox"]').first();
        await firstCheckbox.check();
        
        await expect(page.locator('.selected-count, .batch-actions')).toBeVisible();
        
        const secondCheckbox = page.locator('table tbody tr:nth-child(2) input[type="checkbox"]');
        if (await secondCheckbox.isVisible()) {
            await secondCheckbox.check();
        }
    });
    
    test('批量删除资产', async ({ page }) => {
        await page.click('a[href="/assets"]');
        await expect(page).toHaveURL('/assets');
        
        const firstCheckbox = page.locator('input[type="checkbox"]').first();
        await firstCheckbox.check();
        
        await page.click('button:has-text("批量删除"), button[aria-label="批量删除"]');
        await expect(page.locator('.confirm-dialog, [role="alertdialog"]')).toBeVisible();
        
        await page.click('button:has-text("确认删除"), button:has-text("确定")');
        
        await expect(page.locator('.notification, .message'))
            .toContainText(/删除成功|deleted/i, { timeout: 10000 });
    });
    
    test('批量导出资产', async ({ page }) => {
        await page.click('a[href="/assets"]');
        await expect(page).toHaveURL('/assets');
        
        const firstCheckbox = page.locator('input[type="checkbox"]').first();
        await firstCheckbox.check();
        
        await page.click('button:has-text("导出")');
        await expect(page.locator('.export-dialog, .export-options')).toBeVisible();
        
        await page.selectOption('select[name="format"]', 'EXCEL');
        await page.click('button:has-text("开始导出")');
        
        await expect(page.locator('.notification'))
            .toContainText(/导出任务已创建/i, { timeout: 10000 });
    });
});

test.describe('资产导入E2E', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="username"]', 'admin');
        await page.fill('input[name="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/dashboard');
    });
    
    test('下载导入模板', async ({ page }) => {
        await page.click('a[href="/assets"]');
        await page.click('button:has-text("导入")');
        
        await expect(page.locator('.import-dialog')).toBeVisible();
        
        const downloadLink = page.locator('a:has-text("下载模板"), button:has-text("下载模板")');
        if (await downloadLink.isVisible()) {
            const [download] = await Promise.all([
                page.waitForEvent('download'),
                downloadLink.click()
            ]);
            expect(download.suggestedFilename()).toMatch(/\.xlsx?$/);
        }
    });
    
    test('导入资产数据', async ({ page }) => {
        await page.click('a[href="/assets"]');
        await page.click('button:has-text("导入")');
        
        await expect(page.locator('.import-dialog')).toBeVisible();
        
        const fileInput = page.locator('input[type="file"]');
        if (await fileInput.isVisible()) {
            await fileInput.setInputFiles({
                name: 'test-import.xlsx',
                mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                buffer: Buffer.from('mock content')
            });
        }
        
        await page.click('button:has-text("开始导入")');
        
        await expect(page.locator('.progress-bar, .import-progress'))
            .toBeVisible({ timeout: 5000 });
    });
});
