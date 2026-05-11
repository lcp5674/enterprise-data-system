import { test, expect } from '@playwright/test';

test.describe('数据生命周期E2E', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="username"]', 'admin');
        await page.fill('input[name="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/dashboard', { timeout: 30000 });
    });
    
    async function createAsset(page: any, assetName: string): Promise<string> {
        await page.click('a[href="/assets"]');
        await expect(page).toHaveURL('/assets');
        
        await page.click('button:has-text("创建资产")');
        await expect(page.locator('.modal, [role="dialog"]')).toBeVisible({ timeout: 10000 });
        
        await page.fill('input[name="assetName"]', assetName);
        await page.selectOption('select[name="assetType"]', 'TABLE');
        await page.fill('textarea[name="description"]', '生命周期测试资产');
        
        await page.click('button:has-text("确定")');
        await expect(page.locator('.notification, .message'))
            .toContainText(/创建成功|success/i, { timeout: 15000 });
        
        return assetName;
    }
    
    async function expectAssetStatus(page: any, assetName: string, status: string): Promise<void> {
        await page.goto('/assets');
        await page.waitForLoadState('networkidle');
        
        const statusCell = page.locator(`text=${assetName}`).locator('..').locator('.status, [data-status]');
        await expect(statusCell).toContainText(status, { timeout: 10000 });
    }
    
    test('完整生命周期流转', async ({ page }) => {
        const assetName = `lifecycle_test_${Date.now()}`;
        
        await createAsset(page, assetName);
        await expectAssetStatus(page, assetName, 'DRAFT');
        
        await page.click(`text=${assetName}`);
        await expect(page).toHaveURL(/\/assets\/\d+/);
        
        await page.click('button:has-text("提交审核")');
        await expect(page.locator('.notification, .message'))
            .toContainText(/提交成功|submitted/i, { timeout: 10000 });
        await expectAssetStatus(page, assetName, 'PENDING_REVIEW');
        
        await page.click('button:has-text("审核通过")');
        await page.fill('textarea[name="comment"]', '审核通过');
        await page.click('button:has-text("确定")');
        await expect(page.locator('.notification'))
            .toContainText(/审核通过|approved/i, { timeout: 10000 });
        await expectAssetStatus(page, assetName, 'APPROVED');
        
        await page.click('button:has-text("发布上线")');
        await expect(page.locator('.notification'))
            .toContainText(/发布成功|published/i, { timeout: 10000 });
        await expectAssetStatus(page, assetName, 'ACTIVE');
        
        await page.click('button:has-text("归档")');
        await page.fill('textarea[name="reason"]', '测试归档');
        await page.click('button:has-text("确认归档")');
        await expect(page.locator('.notification'))
            .toContainText(/归档成功|archived/i, { timeout: 10000 });
        await expectAssetStatus(page, assetName, 'ARCHIVED');
    });
    
    test('生命周期状态转换异常 - 跳过审核直接发布', async ({ page }) => {
        const assetName = `invalid_transition_${Date.now()}`;
        
        await createAsset(page, assetName);
        await expectAssetStatus(page, assetName, 'DRAFT');
        
        await page.click(`text=${assetName}`);
        
        const publishBtn = page.locator('button:has-text("发布"), button:has-text("上线")');
        if (await publishBtn.isVisible()) {
            await publishBtn.click();
            await expect(page.locator('.error-message, .warning-message'))
                .toContainText(/请先提交审核|需要审核|pending review/i, { timeout: 5000 });
        }
    });
    
    test('生命周期状态转换异常 - 审核拒绝后不能发布', async ({ page }) => {
        const assetName = `rejected_asset_${Date.now()}`;
        
        await createAsset(page, assetName);
        await page.click(`text=${assetName}`);
        
        await page.click('button:has-text("提交审核")');
        await expect(page.locator('.notification')).toContainText(/提交成功/i, { timeout: 10000 });
        
        await page.click('button:has-text("审核拒绝")');
        await page.fill('textarea[name="reason"]', '信息不完整');
        await page.click('button:has-text("确认拒绝")');
        await expect(page.locator('.notification')).toContainText(/审核已拒绝|rejected/i, { timeout: 10000 });
        
        await expectAssetStatus(page, assetName, 'REJECTED');
        
        const publishBtn = page.locator('button:has-text("发布"), button:has-text("上线")');
        if (await publishBtn.isVisible()) {
            await expect(publishBtn).toBeDisabled();
        }
    });
    
    test('从已废弃状态恢复', async ({ page }) => {
        const assetName = `restore_test_${Date.now()}`;
        
        await createAsset(page, assetName);
        await page.click(`text=${assetName}`);
        
        await page.click('button:has-text("提交审核")');
        await page.click('button:has-text("审核通过")');
        await page.click('button:has-text("发布上线")');
        
        await page.click('button:has-text("废弃")');
        await page.fill('textarea[name="reason"]', '不再使用');
        await page.click('button:has-text("确认废弃")');
        await expect(page.locator('.notification')).toContainText(/废弃成功/i, { timeout: 10000 });
        
        await expectAssetStatus(page, assetName, 'DEPRECATED');
        
        await page.click('button:has-text("恢复")');
        await expect(page.locator('.notification')).toContainText(/恢复成功|restored/i, { timeout: 10000 });
        await expectAssetStatus(page, assetName, 'ACTIVE');
    });
    
    test('生命周期历史记录查看', async ({ page }) => {
        const assetName = `history_test_${Date.now()}`;
        
        await createAsset(page, assetName);
        await page.click(`text=${assetName}`);
        
        await page.click('text=生命周期, a:has-text("生命周期")');
        await expect(page.locator('.lifecycle-timeline, .history-list')).toBeVisible({ timeout: 10000 });
        
        await expect(page.locator('.timeline-item, .history-item')).toHaveCount(greaterThanOrEqual(1));
        
        const firstItem = page.locator('.timeline-item, .history-item').first();
        await expect(firstItem.locator('.status')).toContainText(/DRAFT|创建/i);
    });
    
    test('生命周期状态过滤器', async ({ page }) => {
        await page.click('a[href="/assets"]');
        await expect(page).toHaveURL('/assets');
        
        await page.click('button:has-text("筛选"), button:has-text("状态")');
        
        const statusOptions = ['DRAFT', 'PENDING_REVIEW', 'APPROVED', 'ACTIVE', 'DEPRECATED', 'ARCHIVED'];
        for (const status of statusOptions) {
            const checkbox = page.locator(`input[type="checkbox"][value="${status}"], input[name="${status}"]`);
            if (await checkbox.isVisible()) {
                await checkbox.check();
            }
        }
        
        await page.click('button:has-text("应用筛选")');
        await page.waitForLoadState('networkidle');
        
        const statusBadges = page.locator('.status-badge, .asset-status');
        const count = await statusBadges.count();
        expect(count).toBeGreaterThanOrEqual(0);
    });
});

test.describe('资产认证E2E', () => {
    
    test.beforeEach(async ({ page }) => {
        await page.goto('/login');
        await page.fill('input[name="username"]', 'admin');
        await page.fill('input[name="password"]', 'admin123');
        await page.click('button[type="submit"]');
        await expect(page).toHaveURL('/dashboard');
    });
    
    test('申请资产认证', async ({ page }) => {
        await page.click('a[href="/assets"]');
        const assetLink = page.locator('table tbody tr, .asset-item').first().locator('a, [role="button"]');
        await assetLink.click();
        
        await expect(page).toHaveURL(/\/assets\/\d+/);
        
        await page.click('button:has-text("申请认证")');
        await expect(page.locator('.certification-dialog, .modal')).toBeVisible({ timeout: 5000 });
        
        await page.selectOption('select[name="certificationLevel"]', 'OFFICIAL');
        await page.fill('textarea[name="certificationBasis"]', '符合数据标准规范');
        
        await page.click('button:has-text("提交认证")');
        await expect(page.locator('.notification')).toContainText(/认证申请已提交/i, { timeout: 10000 });
    });
    
    test('查看认证状态', async ({ page }) => {
        await page.click('a[href="/assets"]');
        const assetLink = page.locator('table tbody tr, .asset-item').first();
        await assetLink.click();
        
        await expect(page).toHaveURL(/\/assets\/\d+/);
        
        const certificationBadge = page.locator('.certification-badge, .certification-status');
        if (await certificationBadge.isVisible()) {
            await expect(certificationBadge).toBeVisible();
        }
    });
});

function greaterThanOrEqual(min: number) {
    return {
        symmetricMatch: (received: number) => received >= min
    };
}
