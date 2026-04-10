/**
 * 首页页面对象
 */
import { Page, Locator, expect } from '@playwright/test';

export class HomePage {
  readonly page: Page;

  // 定位器
  readonly welcomeTitle: Locator;
  readonly statisticsCards: Locator;
  readonly recentAssets: Locator;
  readonly quickActions: Locator;
  readonly notifications: Locator;
  readonly userMenu: Locator;
  readonly sidebar: Locator;

  constructor(page: Page) {
    this.page = page;

    // 初始化定位器
    this.welcomeTitle = page.locator('h1, h2').filter({ hasText: /欢迎|dashboard/i });
    this.statisticsCards = page.locator('.ant-card, [class*="statistic"]');
    this.recentAssets = page.locator('[class*="recent"], [class*="asset"]');
    this.quickActions = page.locator('[class*="quick-action"], [class*="shortcut"]');
    this.notifications = page.locator('.ant-badge, [class*="notification"]');
    this.userMenu = page.locator('.ant-dropdown-menu, [class*="user-menu"]');
    this.sidebar = page.locator('.ant-layout-sider, aside');
  }

  /**
   * 访问首页
   */
  async goto(): Promise<void> {
    await this.page.goto('/home');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 导航到资产页面
   */
  async navigateToAssets(): Promise<void> {
    await this.page.click('text=数据资产, a[href*="asset"]');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 导航到质量页面
   */
  async navigateToQuality(): Promise<void> {
    await this.page.click('text=质量中心, a[href*="quality"]');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 导航到血缘页面
   */
  async navigateToLineage(): Promise<void> {
    await this.page.click('text=血缘分析, a[href*="lineage"]');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 导航到目录页面
   */
  async navigateToCatalog(): Promise<void> {
    await this.page.click('text=目录管理, a[href*="catalog"]');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 导航到系统设置
   */
  async navigateToSystem(): Promise<void> {
    await this.page.click('text=系统管理, a[href*="system"]');
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 获取统计卡片数量
   */
  async getStatisticsCardCount(): Promise<number> {
    return await this.statisticsCards.count();
  }

  /**
   * 获取最近访问的资产数量
   */
  async getRecentAssetsCount(): Promise<number> {
    return await this.recentAssets.count();
  }

  /**
   * 获取通知数量
   */
  async getNotificationCount(): Promise<number> {
    const badge = this.page.locator('.ant-badge sup, .ant-badge-count');
    if (await badge.isVisible().catch(() => false)) {
      const text = await badge.textContent();
      return parseInt(text || '0', 10);
    }
    return 0;
  }

  /**
   * 点击通知图标
   */
  async clickNotifications(): Promise<void> {
    await this.page.locator('[class*="notification"], [aria-label*="通知"]').click();
  }

  /**
   * 登出
   */
  async logout(): Promise<void> {
    await this.page.locator('.ant-avatar, [class*="avatar"]').click();
    await this.page.waitForTimeout(500);
    await this.page.locator('text=退出登录, text=Logout').click();
  }
}

export default HomePage;
