/**
 * 质量检测页面对象
 */
import { Page, Locator, expect } from '@playwright/test';

export class QualityPage {
  readonly page: Page;

  // 定位器 - 概览
  readonly overviewTab: Locator;
  readonly rulesTab: Locator;
  readonly issuesTab: Locator;
  readonly reportsTab: Locator;
  readonly overviewCards: Locator;

  // 定位器 - 规则
  readonly rulesList: Locator;
  readonly ruleRows: Locator;
  readonly createRuleButton: Locator;
  readonly ruleNameInput: Locator;
  readonly ruleTypeSelect: Locator;
  readonly ruleSqlInput: Locator;

  // 定位器 - 执行检测
  readonly executeButton: Locator;
  readonly executeAllButton: Locator;
  readonly selectAssetsButton: Locator;
  readonly progressBar: Locator;

  // 定位器 - 报告
  readonly reportButton: Locator;
  readonly reportList: Locator;
  readonly downloadReportButton: Locator;

  // 定位器 - 问题追踪
  readonly issueRows: Locator;
  readonly resolveButton: Locator;
  readonly transferButton: Locator;

  constructor(page: Page) {
    this.page = page;

    // Tab 定位器
    this.overviewTab = page.locator('.ant-tabs-tab:has-text("质量概览")');
    this.rulesTab = page.locator('.ant-tabs-tab:has-text("质量规则")');
    this.issuesTab = page.locator('.ant-tabs-tab:has-text("问题追踪")');
    this.reportsTab = page.locator('.ant-tabs-tab:has-text("质量报告")');

    // 概览定位器
    this.overviewCards = page.locator('.ant-card, [class*="statistic"]');

    // 规则定位器
    this.rulesList = page.locator('.ant-table');
    this.ruleRows = page.locator('.ant-table-row');
    this.createRuleButton = page.locator('button:has-text("创建规则"), button:has-text("新建规则")');
    this.ruleNameInput = page.locator('input[placeholder*="规则名称"], input[placeholder*="名称"]');
    this.ruleTypeSelect = page.locator('.ant-select[placeholder*="类型"]');
    this.ruleSqlInput = page.locator('textarea[placeholder*="SQL"], input[placeholder*="表达式"]');

    // 执行检测定位器
    this.executeButton = page.locator('button:has-text("执行检测"), button:has-text("检测")');
    this.executeAllButton = page.locator('button:has-text("执行全部")');
    this.selectAssetsButton = page.locator('button:has-text("选择资产")');
    this.progressBar = page.locator('.ant-progress, [class*="progress"]');

    // 报告定位器
    this.reportButton = page.locator('button:has-text("生成报告")');
    this.reportList = page.locator('.report-list, .ant-table');
    this.downloadReportButton = page.locator('button:has-text("下载"), a[download]');

    // 问题追踪定位器
    this.issueRows = page.locator('.ant-table-row, .issue-item');
    this.resolveButton = page.locator('button:has-text("解决"), button:has-text("处理")');
    this.transferButton = page.locator('button:has-text("转移")');
  }

  /**
   * 访问质量页面
   */
  async goto(): Promise<void> {
    await this.page.goto('/quality');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 导航到概览
   */
  async goToOverview(): Promise<void> {
    await this.overviewTab.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 导航到规则
   */
  async goToRules(): Promise<void> {
    await this.rulesTab.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 导航到问题追踪
   */
  async goToIssues(): Promise<void> {
    await this.issuesTab.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 导航到报告
   */
  async goToReports(): Promise<void> {
    await this.reportsTab.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 创建质量规则
   */
  async createRule(name: string, type: string, expression: string): Promise<void> {
    await this.goToRules();
    await this.createRuleButton.click();
    await this.page.waitForSelector('.ant-modal, form', { state: 'visible' });

    // 填写表单
    await this.ruleNameInput.fill(name);
    await this.ruleTypeSelect.click();
    await this.page.locator('.ant-select-dropdown >> text=' + type).click();
    await this.ruleSqlInput.fill(expression);

    // 提交
    await this.page.locator('button:has-text("确定")').click();
    await this.page.waitForSelector('.ant-modal', { state: 'hidden' });
  }

  /**
   * 执行质量检测
   */
  async executeCheck(assetNames?: string[]): Promise<void> {
    await this.executeButton.click();
    await this.page.waitForSelector('.ant-modal', { state: 'visible' });

    if (assetNames && assetNames.length > 0) {
      // 选择指定资产
      await this.selectAssetsButton.click();
      for (const name of assetNames) {
        await this.page.locator(`.ant-select-dropdown >> text=${name}`).click();
      }
    }

    // 执行检测
    await this.page.locator('button:has-text("开始检测")').click();

    // 等待检测完成
    await this.page.waitForSelector('.ant-message-success', { timeout: 60000 });
  }

  /**
   * 生成质量报告
   */
  async generateReport(): Promise<void> {
    await this.goToReports();
    await this.reportButton.click();
    await this.page.waitForSelector('.ant-modal', { state: 'visible' });

    // 选择报告类型
    await this.page.locator('.ant-select').first().click();
    await this.page.locator('.ant-select-dropdown >> text=质量报告').click();

    // 生成
    await this.page.locator('button:has-text("生成")').click();
    await this.page.waitForSelector('.ant-message-success', { timeout: 30000 });
  }

  /**
   * 解决问题
   */
  async resolveIssue(issueIndex: number = 0): Promise<void> {
    await this.goToIssues();
    const issue = this.issueRows.nth(issueIndex);
    await issue.locator('button').filter({ hasText: '解决' }).click();
    await this.page.waitForSelector('.ant-modal', { state: 'visible' });
    await this.page.locator('button:has-text("确定")').click();
  }

  /**
   * 获取概览统计
   */
  async getOverviewStats(): Promise<Record<string, string>> {
    await this.goToOverview();
    const stats: Record<string, string> = {};
    const cards = await this.overviewCards.all();
    
    for (const card of cards) {
      const title = await card.locator('[class*="title"], [class*="label"]').textContent();
      const value = await card.locator('[class*="value"], [class*="number"]').textContent();
      if (title && value) {
        stats[title.trim()] = value.trim();
      }
    }
    
    return stats;
  }

  /**
   * 获取规则数量
   */
  async getRuleCount(): Promise<number> {
    await this.goToRules();
    return await this.ruleRows.count();
  }

  /**
   * 获取问题数量
   */
  async getIssueCount(): Promise<number> {
    await this.goToIssues();
    return await this.issueRows.count();
  }
}

export default QualityPage;
