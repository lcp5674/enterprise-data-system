export async function login(page: any, username: string = 'admin', password: string = 'admin123'): Promise<void> {
    await page.goto('/login');
    await page.fill('input[name="username"]', username);
    await page.fill('input[name="password"]', password);
    await page.click('button[type="submit"]');
    await page.waitForURL('/dashboard', { timeout: 30000 });
}

export async function logout(page: any): Promise<void> {
    await page.click('.user-menu, button:has-text("用户")');
    await page.click('button:has-text("退出登录"), button:has-text("登出")');
    await page.waitForURL('/login', { timeout: 10000 });
}

export async function waitForNotification(page: any, text?: string, timeout: number = 10000): Promise<void> {
    const locator = page.locator('.notification, .message, [role="alert"]');
    if (text) {
        await expect(locator).toContainText(text, { timeout });
    } else {
        await expect(locator).toBeVisible({ timeout });
    }
}

export async function fillAssetForm(
    page: any,
    data: {
        name?: string;
        assetType?: string;
        description?: string;
        owner?: string;
        sensitivityLevel?: string;
    }
): Promise<void> {
    if (data.name) {
        await page.fill('input[name="assetName"]', data.name);
    }
    if (data.assetType) {
        await page.selectOption('select[name="assetType"]', data.assetType);
    }
    if (data.description) {
        await page.fill('textarea[name="description"]', data.description);
    }
    if (data.owner) {
        await page.fill('input[name="owner"]', data.owner);
    }
    if (data.sensitivityLevel) {
        await page.selectOption('select[name="sensitivityLevel"]', data.sensitivityLevel);
    }
}

export async function submitAsset(page: any): Promise<void> {
    await page.click('button:has-text("确定")');
    await waitForNotification(page, /创建成功|更新成功|success/i);
}

export async function navigateToAsset(page: any, assetIdOrName: string): Promise<void> {
    await page.click('a[href="/assets"]');
    
    if (assetIdOrName.startsWith('/')) {
        await page.goto(assetIdOrName);
    } else {
        const assetLink = page.locator(`text=${assetIdOrName}`).first();
        await assetLink.click();
    }
    
    await page.waitForURL(/\/assets\/\d+|\/assets/, { timeout: 15000 });
}

export async function createTestAsset(page: any, name?: string): Promise<string> {
    const assetName = name || `test_asset_${Date.now()}`;
    
    await page.click('a[href="/assets"]');
    await page.click('button:has-text("创建资产")');
    await fillAssetForm(page, {
        name: assetName,
        assetType: 'TABLE',
        description: 'E2E测试资产'
    });
    await submitAsset(page);
    
    return assetName;
}

export async function applyFilters(
    page: any,
    filters: {
        assetType?: string;
        status?: string;
        sensitivityLevel?: string;
        owner?: string;
    }
): Promise<void> {
    await page.click('button:has-text("筛选")');
    
    if (filters.assetType) {
        await page.selectOption('select[name="assetType"]', filters.assetType);
    }
    if (filters.status) {
        await page.selectOption('select[name="status"]', filters.status);
    }
    if (filters.sensitivityLevel) {
        await page.selectOption('select[name="sensitivityLevel"]', filters.sensitivityLevel);
    }
    if (filters.owner) {
        await page.fill('input[name="owner"]', filters.owner);
    }
    
    await page.click('button:has-text("应用")');
    await page.waitForLoadState('networkidle');
}

export async function clearFilters(page: any): Promise<void> {
    const clearBtn = page.locator('button:has-text("清除筛选"), button:has-text("清空条件")');
    if (await clearBtn.isVisible()) {
        await clearBtn.click();
    }
}

export async function getAssetStatus(page: any, assetName: string): Promise<string> {
    const row = page.locator(`text=${assetName}`).locator('..');
    const statusCell = row.locator('.status, [data-status]');
    return statusCell.textContent() || '';
}

export async function expectAssetInList(page: any, assetName: string): Promise<void> {
    await expect(page.locator('table tbody')).toContainText(assetName, { timeout: 10000 });
}

export async function expectAssetNotInList(page: any, assetName: string): Promise<void> {
    await expect(page.locator(`text=${assetName}`)).not.toBeVisible({ timeout: 5000 });
}

export async function downloadFile(page: any, triggerSelector: string): Promise<string> {
    const [download] = await Promise.all([
        page.waitForEvent('download'),
        page.click(triggerSelector)
    ]);
    return download.suggestedFilename();
}

export async function uploadFile(page: any, inputSelector: string, filePath: string): Promise<void> {
    const fileInput = page.locator(inputSelector);
    await fileInput.setInputFiles(filePath);
}

export async function switchTab(page: any, tabName: string): Promise<void> {
    const tabBtn = page.locator(`button:has-text("${tabName}"), .tab-item:has-text("${tabName}")`);
    if (await tabBtn.isVisible()) {
        await tabBtn.click();
        await page.waitForTimeout(500);
    }
}

export async function confirmDialog(page: any, confirmText: string = '确定'): Promise<void> {
    await expect(page.locator('.confirm-dialog, [role="alertdialog"]')).toBeVisible();
    await page.click(`button:has-text("${confirmText}")`);
}

export async function cancelDialog(page: any, cancelText: string = '取消'): Promise<void> {
    await page.click(`button:has-text("${cancelText}")`);
}

export async function waitForLoading(page: any, timeout: number = 30000): Promise<void> {
    const loader = page.locator('.loading, .spinner, [data-loading="true"]');
    if (await loader.isVisible()) {
        await expect(loader).toBeHidden({ timeout });
    }
}

export async function waitForNetworkIdle(page: any, timeout: number = 30000): Promise<void> {
    await page.waitForLoadState('networkidle', { timeout });
}

import { expect } from '@playwright/test';
