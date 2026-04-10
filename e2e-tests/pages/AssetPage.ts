/**
 * 资产页面对象
 */
import { Page, Locator, expect } from '@playwright/test';

export class AssetPage {
  readonly page: Page;

  // 定位器
  readonly searchInput: Locator;
  readonly searchButton: Locator;
  readonly assetList: Locator;
  readonly assetRows: Locator;
  readonly createButton: Locator;
  readonly filterDropdown: Locator;
  readonly refreshButton: Locator;
  readonly exportButton: Locator;

  constructor(page: Page) {
    this.page = page;

    // 初始化定位器
    this.searchInput = page.locator('input[placeholder*="搜索"], input[placeholder*="查询"]');
    this.searchButton = page.locator('button:has-text("搜索"), button:has-text("查询")');
    this.assetList = page.locator('.ant-table-tbody, .asset-list');
    this.assetRows = page.locator('.ant-table-row, .asset-item');
    this.createButton = page.locator('button:has-text("注册资产"), button:has-text("新建"), button:has-text("创建")');
    this.filterDropdown = page.locator('.ant-select, [class*="filter"]');
    this.refreshButton = page.locator('button:has-text("刷新"), button[aria-label*="刷新"]');
    this.exportButton = page.locator('button:has-text("导出")');
  }

  /**
   * 访问资产页面
   */
  async goto(): Promise<void> {
    await this.page.goto('/assets');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 搜索资产
   */
  async searchAssets(keyword: string): Promise<void> {
    await this.searchInput.fill(keyword);
    await this.searchButton.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 获取资产列表行数
   */
  async getAssetCount(): Promise<number> {
    return await this.assetRows.count();
  }

  /**
   * 点击第一个资产查看详情
   */
  async clickFirstAsset(): Promise<void> {
    const firstRow = this.assetRows.first();
    await firstRow.locator('a, button').first().click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 收藏资产
   */
  async favoriteAsset(index: number = 0): Promise<void> {
    const row = this.assetRows.nth(index);
    const favoriteButton = row.locator('[aria-label*="收藏"], button').last();
    await favoriteButton.click();
    await this.page.waitForTimeout(1000);
  }

  /**
   * 点击创建资产按钮
   */
  async clickCreateAsset(): Promise<void> {
    await this.createButton.click();
    await this.page.waitForSelector('.ant-modal, form', { state: 'visible' });
  }

  /**
   * 选择资产类型过滤
   */
  async filterByType(type: string): Promise<void> {
    await this.filterDropdown.first().click();
    await this.page.locator(`.ant-select-dropdown >> text=${type}`).click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 刷新资产列表
   */
  async refreshAssets(): Promise<void> {
    await this.refreshButton.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 导出资产列表
   */
  async exportAssets(): Promise<void> {
    await this.exportButton.click();
    await this.page.waitForTimeout(2000);
  }
}

export class AssetDetailPage {
  readonly page: Page;

  // 定位器
  readonly assetName: Locator;
  readonly assetDescription: Locator;
  readonly metadataSection: Locator;
  readonly fieldsSection: Locator;
  readonly lineageSection: Locator;
  readonly qualitySection: Locator;
  readonly favoriteButton: Locator;
  readonly editButton: Locator;
  readonly permissionButton: Locator;
  readonly backButton: Locator;

  constructor(page: Page) {
    this.page = page;

    // 初始化定位器
    this.assetName = page.locator('h1, h2').first();
    this.assetDescription = page.locator('[class*="description"], [class*="summary"]');
    this.metadataSection = page.locator('text=基本信息, [class*="metadata"]');
    this.fieldsSection = page.locator('text=字段信息, [class*="fields"]');
    this.lineageSection = page.locator('text=血缘关系, [class*="lineage"]');
    this.qualitySection = page.locator('text=质量信息, [class*="quality"]');
    this.favoriteButton = page.locator('button:has-text("收藏"), button[aria-label*="收藏"]');
    this.editButton = page.locator('button:has-text("编辑")');
    this.permissionButton = page.locator('button:has-text("权限")');
    this.backButton = page.locator('button:has-text("返回"), a[href*="assets"]');
  }

  /**
   * 收藏当前资产
   */
  async favorite(): Promise<void> {
    await this.favoriteButton.click();
    await this.page.waitForTimeout(1000);
  }

  /**
   * 点击编辑按钮
   */
  async clickEdit(): Promise<void> {
    await this.editButton.click();
    await this.page.waitForSelector('form', { state: 'visible' });
  }

  /**
   * 点击返回按钮
   */
  async goBack(): Promise<void> {
    await this.backButton.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 获取资产名称
   */
  async getAssetName(): Promise<string> {
    return await this.assetName.textContent() || '';
  }

  /**
   * 验证页面元素
   */
  async verifyPageElements(): Promise<void> {
    await expect(this.assetName).toBeVisible();
    await expect(this.metadataSection).toBeVisible();
  }
}

export default { AssetPage, AssetDetailPage };
