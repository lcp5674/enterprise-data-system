/**
 * 目录管理页面对象
 */
import { Page, Locator, expect } from '@playwright/test';

export class CatalogPage {
  readonly page: Page;

  // 定位器
  readonly treeContainer: Locator;
  readonly treeNodes: Locator;
  readonly createButton: Locator;
  readonly createRootButton: Locator;
  readonly createChildButton: Locator;
  readonly editButton: Locator;
  readonly deleteButton: Locator;
  readonly importButton: Locator;
  readonly exportButton: Locator;
  readonly searchInput: Locator;
  readonly expandAllButton: Locator;
  readonly collapseAllButton: Locator;

  constructor(page: Page) {
    this.page = page;

    // 初始化定位器
    this.treeContainer = page.locator('.ant-tree, .catalog-tree, [class*="tree"]');
    this.treeNodes = page.locator('.ant-tree-treenode, .tree-node');
    this.createButton = page.locator('button:has-text("新建目录"), button:has-text("创建")');
    this.createRootButton = page.locator('button:has-text("新建根目录")');
    this.createChildButton = page.locator('button:has-text("添加子目录")');
    this.editButton = page.locator('button:has-text("编辑"), [aria-label*="编辑"]');
    this.deleteButton = page.locator('button:has-text("删除"), [aria-label*="删除"]');
    this.importButton = page.locator('button:has-text("导入")');
    this.exportButton = page.locator('button:has-text("导出")');
    this.searchInput = page.locator('input[placeholder*="搜索目录"]');
    this.expandAllButton = page.locator('button:has-text("展开全部")');
    this.collapseAllButton = page.locator('button:has-text("收起全部")');
  }

  /**
   * 访问目录页面
   */
  async goto(): Promise<void> {
    await this.page.goto('/catalog');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 创建根目录
   */
  async createRootCatalog(name: string, code: string): Promise<void> {
    await this.createRootButton.click();
    await this.page.waitForSelector('.ant-modal', { state: 'visible' });
    
    // 填写表单
    await this.page.locator('input[placeholder*="名称"]').fill(name);
    await this.page.locator('input[placeholder*="编码"]').fill(code);
    
    // 提交
    await this.page.locator('button:has-text("确定")').click();
    await this.page.waitForSelector('.ant-modal', { state: 'hidden' });
  }

  /**
   * 选择目录节点
   */
  async selectNode(nodeText: string): Promise<void> {
    await this.page.locator(`.ant-tree-node-content-wrapper:has-text("${nodeText}")`).click();
    await this.page.waitForTimeout(500);
  }

  /**
   * 右键点击目录节点
   */
  async rightClickNode(nodeText: string): Promise<void> {
    const node = this.page.locator(`.ant-tree-node-content-wrapper:has-text("${nodeText}")`);
    await node.click({ button: 'right' });
    await this.page.waitForTimeout(500);
  }

  /**
   * 添加子目录
   */
  async addChildCatalog(parentName: string, childName: string, childCode: string): Promise<void> {
    await this.selectNode(parentName);
    await this.createChildButton.click();
    await this.page.waitForSelector('.ant-modal', { state: 'visible' });
    
    // 填写表单
    await this.page.locator('input[placeholder*="名称"]').fill(childName);
    await this.page.locator('input[placeholder*="编码"]').fill(childCode);
    
    // 提交
    await this.page.locator('button:has-text("确定")').click();
    await this.page.waitForSelector('.ant-modal', { state: 'hidden' });
  }

  /**
   * 编辑目录
   */
  async editCatalog(nodeName: string, newName: string, newCode?: string): Promise<void> {
    await this.rightClickNode(nodeName);
    
    // 点击编辑
    await this.page.locator('text=编辑').click();
    await this.page.waitForSelector('.ant-modal', { state: 'visible' });
    
    // 修改名称
    await this.page.locator('input[placeholder*="名称"]').fill(newName);
    
    if (newCode) {
      await this.page.locator('input[placeholder*="编码"]').fill(newCode);
    }
    
    // 提交
    await this.page.locator('button:has-text("确定")').click();
    await this.page.waitForSelector('.ant-modal', { state: 'hidden' });
  }

  /**
   * 删除目录
   */
  async deleteCatalog(nodeName: string): Promise<void> {
    await this.rightClickNode(nodeName);
    
    // 点击删除
    await this.page.locator('text=删除').click();
    
    // 确认删除
    await this.page.locator('button:has-text("确定")').click();
    await this.page.waitForTimeout(1000);
  }

  /**
   * 展开所有节点
   */
  async expandAll(): Promise<void> {
    await this.expandAllButton.click();
    await this.page.waitForTimeout(1000);
  }

  /**
   * 收起所有节点
   */
  async collapseAll(): Promise<void> {
    await this.collapseAllButton.click();
    await this.page.waitForTimeout(1000);
  }

  /**
   * 获取目录节点数量
   */
  async getNodeCount(): Promise<number> {
    return await this.treeNodes.count();
  }

  /**
   * 搜索目录
   */
  async searchCatalog(keyword: string): Promise<void> {
    await this.searchInput.fill(keyword);
    await this.page.waitForTimeout(500);
  }
}

export default CatalogPage;
