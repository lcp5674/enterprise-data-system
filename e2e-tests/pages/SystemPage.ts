/**
 * 系统管理页面对象
 */
import { Page, Locator, expect } from '@playwright/test';

export class SystemPage {
  readonly page: Page;

  // Tab 定位器
  readonly usersTab: Locator;
  readonly rolesTab: Locator;
  readonly datasourcesTab: Locator;
  readonly permissionsTab: Locator;
  readonly auditTab: Locator;

  // 用户管理
  readonly userList: Locator;
  readonly userRows: Locator;
  readonly createUserButton: Locator;
  readonly usernameInput: Locator;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly roleSelect: Locator;
  readonly departmentSelect: Locator;

  // 角色管理
  readonly roleList: Locator;
  readonly roleRows: Locator;
  readonly createRoleButton: Locator;
  readonly roleNameInput: Locator;
  readonly permissionTree: Locator;
  readonly functionPermissionTab: Locator;
  readonly dataPermissionTab: Locator;

  // 数据源管理
  readonly datasourceList: Locator;
  readonly createDatasourceButton: Locator;
  readonly testConnectionButton: Locator;

  constructor(page: Page) {
    this.page = page;

    // Tab 定位器
    this.usersTab = page.locator('.ant-menu-item:has-text("用户管理")');
    this.rolesTab = page.locator('.ant-menu-item:has-text("角色权限")');
    this.datasourcesTab = page.locator('.ant-menu-item:has-text("数据源")');
    this.permissionsTab = page.locator('.ant-menu-item:has-text("权限管理")');
    this.auditTab = page.locator('.ant-menu-item:has-text("审计日志")');

    // 用户管理定位器
    this.userList = page.locator('.ant-table');
    this.userRows = page.locator('.ant-table-row');
    this.createUserButton = page.locator('button:has-text("新建用户"), button:has-text("创建用户")');
    this.usernameInput = page.locator('input[placeholder*="用户名"], input[placeholder*="账号"]');
    this.emailInput = page.locator('input[type="email"], input[placeholder*="邮箱"]');
    this.passwordInput = page.locator('input[placeholder*="密码"]');
    this.roleSelect = page.locator('.ant-select[placeholder*="角色"]');
    this.departmentSelect = page.locator('.ant-select[placeholder*="部门"]');

    // 角色管理定位器
    this.roleList = page.locator('.ant-table');
    this.roleRows = page.locator('.ant-table-row');
    this.createRoleButton = page.locator('button:has-text("新建角色"), button:has-text("创建角色")');
    this.roleNameInput = page.locator('input[placeholder*="角色名称"], input[placeholder*="名称"]');
    this.permissionTree = page.locator('.ant-tree, [class*="permission"]');
    this.functionPermissionTab = page.locator('.ant-tabs-tab:has-text("功能权限")');
    this.dataPermissionTab = page.locator('.ant-tabs-tab:has-text("数据权限")');

    // 数据源管理定位器
    this.datasourceList = page.locator('.ant-table');
    this.createDatasourceButton = page.locator('button:has-text("创建数据源")');
    this.testConnectionButton = page.locator('button:has-text("测试连接")');
  }

  /**
   * 访问系统管理页面
   */
  async goto(): Promise<void> {
    await this.page.goto('/system');
    await this.page.waitForLoadState('networkidle');
  }

  // ==================== 用户管理 ====================

  /**
   * 导航到用户管理
   */
  async goToUsers(): Promise<void> {
    await this.usersTab.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 创建用户
   */
  async createUser(userData: {
    username: string;
    password: string;
    email: string;
    role?: string;
    department?: string;
  }): Promise<void> {
    await this.goToUsers();
    await this.createUserButton.click();
    await this.page.waitForSelector('.ant-modal, form', { state: 'visible' });

    // 填写表单
    await this.usernameInput.fill(userData.username);
    await this.passwordInput.fill(userData.password);
    await this.emailInput.fill(userData.email);

    if (userData.role) {
      await this.roleSelect.click();
      await this.page.locator(`.ant-select-dropdown >> text=${userData.role}`).click();
    }

    if (userData.department) {
      await this.departmentSelect.click();
      await this.page.locator(`.ant-select-dropdown >> text=${userData.department}`).click();
    }

    // 提交
    await this.page.locator('button:has-text("确定")').click();
    await this.page.waitForSelector('.ant-message-success', { timeout: 10000 });
  }

  /**
   * 获取用户数量
   */
  async getUserCount(): Promise<number> {
    await this.goToUsers();
    return await this.userRows.count();
  }

  /**
   * 编辑用户
   */
  async editUser(username: string): Promise<void> {
    await this.goToUsers();
    const row = this.userRows.filter({ hasText: username }).first();
    await row.locator('button[aria-label*="编辑"]').click();
    await this.page.waitForSelector('.ant-modal', { state: 'visible' });
  }

  /**
   * 删除用户
   */
  async deleteUser(username: string): Promise<void> {
    await this.goToUsers();
    const row = this.userRows.filter({ hasText: username }).first();
    await row.locator('button[aria-label*="删除"]').click();
    await this.page.locator('button:has-text("确定")').click();
    await this.page.waitForTimeout(1000);
  }

  // ==================== 角色管理 ====================

  /**
   * 导航到角色管理
   */
  async goToRoles(): Promise<void> {
    await this.rolesTab.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 创建角色
   */
  async createRole(roleData: {
    name: string;
    code: string;
    description?: string;
    permissions?: string[];
  }): Promise<void> {
    await this.goToRoles();
    await this.createRoleButton.click();
    await this.page.waitForSelector('.ant-modal, form', { state: 'visible' });

    // 填写基本信息
    await this.roleNameInput.fill(roleData.name);
    await this.page.locator('input[placeholder*="编码"]').fill(roleData.code);
    
    if (roleData.description) {
      await this.page.locator('textarea[placeholder*="描述"]').fill(roleData.description);
    }

    // 分配权限
    if (roleData.permissions && roleData.permissions.length > 0) {
      await this.functionPermissionTab.click();
      for (const permission of roleData.permissions) {
        await this.page.locator(`.ant-tree-node-content-wrapper:has-text("${permission}")`).click();
      }
    }

    // 提交
    await this.page.locator('button:has-text("确定")').click();
    await this.page.waitForSelector('.ant-message-success', { timeout: 10000 });
  }

  /**
   * 获取角色数量
   */
  async getRoleCount(): Promise<number> {
    await this.goToRoles();
    return await this.roleRows.count();
  }

  /**
   * 分配功能权限
   */
  async assignFunctionPermissions(roleName: string, permissions: string[]): Promise<void> {
    await this.goToRoles();
    const row = this.roleRows.filter({ hasText: roleName }).first();
    await row.locator('button[aria-label*="编辑"]').click();
    await this.page.waitForSelector('.ant-modal', { state: 'visible' });

    await this.functionPermissionTab.click();
    for (const permission of permissions) {
      const checkbox = this.page.locator(`.ant-tree-node-content-wrapper:has-text("${permission}") >> preceding-sibling::span`);
      await checkbox.click();
    }

    await this.page.locator('button:has-text("确定")').click();
    await this.page.waitForSelector('.ant-message-success', { timeout: 10000 });
  }

  /**
   * 分配数据权限
   */
  async assignDataPermissions(roleName: string, scope: string): Promise<void> {
    await this.goToRoles();
    const row = this.roleRows.filter({ hasText: roleName }).first();
    await row.locator('button[aria-label*="编辑"]').click();
    await this.page.waitForSelector('.ant-modal', { state: 'visible' });

    await this.dataPermissionTab.click();
    await this.page.locator('.ant-select').click();
    await this.page.locator(`.ant-select-dropdown >> text=${scope}`).click();

    await this.page.locator('button:has-text("确定")').click();
    await this.page.waitForSelector('.ant-message-success', { timeout: 10000 });
  }

  // ==================== 数据源管理 ====================

  /**
   * 导航到数据源管理
   */
  async goToDatasources(): Promise<void> {
    await this.datasourcesTab.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 创建数据源
   */
  async createDatasource(datasourceData: {
    name: string;
    type: string;
    host: string;
    port: number;
    database: string;
    username: string;
    password: string;
  }): Promise<void> {
    await this.goToDatasources();
    await this.createDatasourceButton.click();
    await this.page.waitForSelector('.ant-modal, form', { state: 'visible' });

    // 填写表单
    await this.page.locator('input[placeholder*="名称"]').fill(datasourceData.name);
    
    await this.page.locator('.ant-select').first().click();
    await this.page.locator(`.ant-select-dropdown >> text=${datasourceData.type}`).click();

    await this.page.locator('input[placeholder*="主机"]').fill(datasourceData.host);
    await this.page.locator('input[placeholder*="端口"]').fill(datasourceData.port.toString());
    await this.page.locator('input[placeholder*="数据库"]').fill(datasourceData.database);
    await this.page.locator('input[placeholder*="用户名"]').fill(datasourceData.username);
    await this.page.locator('input[placeholder*="密码"]').fill(datasourceData.password);

    // 提交
    await this.page.locator('button:has-text("确定")').click();
    await this.page.waitForSelector('.ant-message-success', { timeout: 10000 });
  }

  /**
   * 测试数据源连接
   */
  async testDatasourceConnection(datasourceName: string): Promise<boolean> {
    await this.goToDatasources();
    const row = this.page.locator('.ant-table-row').filter({ hasText: datasourceName });
    await row.locator('button:has-text("测试连接")').click();
    
    // 等待测试结果
    await this.page.waitForSelector('.ant-message', { timeout: 30000 });
    const message = await this.page.locator('.ant-message').textContent();
    return message?.includes('成功') || false;
  }
}

export default SystemPage;
