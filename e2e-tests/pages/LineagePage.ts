/**
 * 血缘分析页面对象
 */
import { Page, Locator, expect } from '@playwright/test';

export class LineagePage {
  readonly page: Page;

  // 定位器
  readonly lineageGraph: Locator;
  readonly searchInput: Locator;
  readonly searchButton: Locator;
  readonly tableTab: Locator;
  readonly fieldTab: Locator;
  readonly impactTab: Locator;
  readonly dependencyTab: Locator;
  readonly nodeList: Locator;
  readonly edgeList: Locator;
  readonly zoomInButton: Locator;
  readonly zoomOutButton: Locator;
  readonly resetViewButton: Locator;
  readonly exportButton: Locator;
  readonly filterDropdown: Locator;

  constructor(page: Page) {
    this.page = page;

    // 初始化定位器
    this.lineageGraph = page.locator('[class*="lineage-graph"], [class*="graph-container"]');
    this.searchInput = page.locator('input[placeholder*="搜索"], input[placeholder*="查询"]');
    this.searchButton = page.locator('button:has-text("搜索"), button:has-text("查询")');
    this.tableTab = page.locator('.ant-tabs-tab:has-text("表级血缘")');
    this.fieldTab = page.locator('.ant-tabs-tab:has-text("字段血缘")');
    this.impactTab = page.locator('.ant-tabs-tab:has-text("影响分析")');
    this.dependencyTab = page.locator('.ant-tabs-tab:has-text("数据溯源")');
    this.nodeList = page.locator('[class*="node"], .ant-tree-node');
    this.edgeList = page.locator('[class*="edge"], [class*="link"]');
    this.zoomInButton = page.locator('button[aria-label*="放大"], button:has-text("放大")');
    this.zoomOutButton = page.locator('button[aria-label*="缩小"], button:has-text("缩小")');
    this.resetViewButton = page.locator('button:has-text("重置视图")');
    this.exportButton = page.locator('button:has-text("导出"), button:has-text("下载")');
    this.filterDropdown = page.locator('.ant-select');
  }

  /**
   * 访问血缘页面
   */
  async goto(): Promise<void> {
    await this.page.goto('/lineage');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 切换到表级血缘
   */
  async goToTableLineage(): Promise<void> {
    await this.tableTab.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 切换到字段血缘
   */
  async goToFieldLineage(): Promise<void> {
    await this.fieldTab.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 切换到影响分析
   */
  async goToImpactAnalysis(): Promise<void> {
    await this.impactTab.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 切换到数据溯源
   */
  async goToDependency(): Promise<void> {
    await this.dependencyTab.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 搜索血缘节点
   */
  async searchNode(nodeName: string): Promise<void> {
    await this.searchInput.fill(nodeName);
    await this.searchButton.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 点击血缘节点
   */
  async clickNode(nodeName: string): Promise<void> {
    await this.page.locator(`[class*="node-title"]:has-text("${nodeName}")`).click();
    await this.page.waitForTimeout(1000);
  }

  /**
   * 获取血缘节点数量
   */
  async getNodeCount(): Promise<number> {
    return await this.nodeList.count();
  }

  /**
   * 获取血缘边数量
   */
  async getEdgeCount(): Promise<number> {
    return await this.edgeList.count();
  }

  /**
   * 放大血缘图
   */
  async zoomIn(): Promise<void> {
    await this.zoomInButton.click();
    await this.page.waitForTimeout(500);
  }

  /**
   * 缩小血缘图
   */
  async zoomOut(): Promise<void> {
    await this.zoomOutButton.click();
    await this.page.waitForTimeout(500);
  }

  /**
   * 重置视图
   */
  async resetView(): Promise<void> {
    await this.resetViewButton.click();
    await this.page.waitForTimeout(1000);
  }

  /**
   * 导出血缘关系
   */
  async exportLineage(format: string = 'json'): Promise<void> {
    await this.exportButton.click();
    await this.page.waitForSelector('.ant-dropdown-menu', { state: 'visible' });
    await this.page.locator(`.ant-dropdown-menu >> text=${format.toUpperCase()}`).click();
    await this.page.waitForTimeout(2000);
  }

  /**
   * 查看节点详情
   */
  async viewNodeDetail(nodeName: string): Promise<{
    name: string;
    type: string;
    upstream: number;
    downstream: number;
  }> {
    await this.clickNode(nodeName);
    
    // 等待详情面板出现
    await this.page.waitForSelector('[class*="detail-panel"], .ant-drawer', { state: 'visible' });
    
    const name = await this.page.locator('[class*="detail-title"]').textContent();
    const type = await this.page.locator('[class*="detail-type"]').textContent();
    const upstream = await this.page.locator('text=上游').locator('..').locator('[class*="count"]').textContent();
    const downstream = await this.page.locator('text=下游').locator('..').locator('[class*="count"]').textContent();
    
    return {
      name: name || '',
      type: type || '',
      upstream: parseInt(upstream || '0', 10),
      downstream: parseInt(downstream || '0', 10),
    };
  }

  /**
   * 按类型过滤节点
   */
  async filterByType(type: string): Promise<void> {
    await this.filterDropdown.first().click();
    await this.page.locator(`.ant-select-dropdown >> text=${type}`).click();
    await this.page.waitForLoadState('networkidle');
  }
}

export default LineagePage;
