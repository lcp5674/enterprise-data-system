# 企业数据资产管理系统 E2E 测试套件

## 概述

本目录包含企业数据资产管理系统的端到端（E2E）测试，使用 Playwright 作为主要测试框架。

## 目录结构

```
e2e-tests/
├── pages/                    # 页面对象类
│   ├── LoginPage.ts          # 登录页面
│   ├── HomePage.ts          # 首页
│   ├── AssetPage.ts         # 资产页面
│   ├── CatalogPage.ts       # 目录管理页面
│   ├── QualityPage.ts       # 质量检测页面
│   ├── SystemPage.ts        # 系统管理页面
│   └── LineagePage.ts       # 血缘分析页面
├── specs/                    # 测试用例
│   ├── auth.spec.ts         # 认证流程测试
│   ├── asset.spec.ts        # 资产查询测试
│   ├── lineage.spec.ts      # 血缘生成测试
│   ├── quality.spec.ts      # 质量检测测试
│   ├── permission.spec.ts  # 权限管理测试
│   ├── masking.spec.ts      # 数据脱敏测试
│   └── business-flow.spec.ts # 业务流程测试
├── utils/                    # 工具函数
│   └── helpers.ts           # 辅助函数
├── fixtures/                 # 测试数据
├── reports/                  # 测试报告
├── playwright.config.ts     # Playwright 配置
├── e2e-config.json          # E2E 测试配置
└── README.md                # 本文档
```

## 安装

### 1. 安装 Playwright

```bash
cd /System/Volumes/Data/data/GitCode/enterprise-data-system/edams-web
npm install -D @playwright/test
npx playwright install chromium --with-deps
```

### 2. 配置测试环境

修改 `e2e-tests/e2e-config.json` 中的配置：

```json
{
  "baseUrl": "http://localhost:8000",
  "apiUrl": "http://localhost:8888",
  "testUsers": {
    "admin": {
      "username": "admin",
      "password": "admin123"
    }
  }
}
```

## 运行测试

### 运行所有测试

```bash
cd /System/Volumes/Data/data/GitCode/enterprise-data-system/edams-web
npx playwright test
```

### 运行特定测试

```bash
# 运行认证测试
npx playwright test e2e-tests/specs/auth.spec.ts

# 运行资产测试
npx playwright test e2e-tests/specs/asset.spec.ts
```

### UI 模式运行

```bash
npx playwright test --ui
```

### 运行特定浏览器

```bash
npx playwright test --project=chromium
npx playwright test --project=firefox
npx playwright test --project=webkit
```

## 测试链路

### 链路1: 用户登录到资产查询

```
用户登录 → 首页 → 数据资产搜索 → 资产详情 → 收藏资产
```

测试用例数: 7

### 链路2: 资产注册到血缘生成

```
注册数据源 → 扫描元数据 → 创建目录 → 关联资产 → 生成血缘
```

测试用例数: 14

### 链路3: 质量检测到报告生成

```
创建质量规则 → 执行检测 → 查看结果 → 生成报告
```

测试用例数: 14

### 链路4: 权限管理完整流程

```
创建角色 → 分配权限 → 用户绑定 → 权限验证
```

测试用例数: 14

### 链路5: 数据脱敏完整流程

```
配置脱敏规则 → 测试脱敏 → 导出数据 → 验证效果
```

测试用例数: 15

## 测试用例清单

| 测试编号 | 测试名称 | 优先级 | 状态 |
|---------|---------|--------|------|
| E2E-AUTH-001 | 用户名密码登录 | P0 | ✅ |
| E2E-AUTH-002 | 手机验证码登录 | P0 | ✅ |
| E2E-AUTH-003 | 第三方登录模拟 | P0 | ✅ |
| E2E-AUTH-004 | 退出登录 | P0 | ✅ |
| E2E-AUTH-005 | Token 验证 | P0 | ✅ |
| E2E-ASSET-001 | 完整链路-用户登录到资产搜索 | P0 | ✅ |
| E2E-ASSET-002 | 按关键字搜索资产 | P0 | ✅ |
| E2E-ASSET-003 | 按资产类型筛选 | P0 | ✅ |
| E2E-ASSET-004 | 刷新资产列表 | P1 | ✅ |
| E2E-ASSET-005 | 查看资产详情 | P0 | ✅ |
| E2E-ASSET-006 | 收藏资产 | P1 | ✅ |
| E2E-ASSET-007 | 导出资产列表 | P1 | ✅ |
| E2E-LINEAGE-001 | 完整链路-资产注册到血缘生成 | P0 | ✅ |
| E2E-LINEAGE-002 | 创建 MySQL 数据源 | P0 | ✅ |
| E2E-LINEAGE-003 | 创建 PostgreSQL 数据源 | P0 | ✅ |
| E2E-LINEAGE-004 | 创建目录 | P0 | ✅ |
| E2E-LINEAGE-005 | 添加子目录 | P1 | ✅ |
| E2E-LINEAGE-006 | 编辑目录 | P1 | ✅ |
| E2E-LINEAGE-007 | 删除目录 | P1 | ✅ |
| E2E-LINEAGE-008 | 查看表级血缘 | P0 | ✅ |
| E2E-LINEAGE-009 | 查看字段血缘 | P0 | ✅ |
| E2E-LINEAGE-010 | 影响分析 | P0 | ✅ |
| E2E-LINEAGE-011 | 数据溯源 | P0 | ✅ |
| E2E-LINEAGE-012 | 血缘图缩放操作 | P1 | ✅ |
| E2E-LINEAGE-013 | 搜索血缘节点 | P1 | ✅ |
| E2E-LINEAGE-014 | 导出血缘关系 | P1 | ✅ |
| E2E-QUALITY-001 | 完整链路-创建规则到报告生成 | P0 | ✅ |
| E2E-QUALITY-002 | 查看质量概览 | P0 | ✅ |
| E2E-QUALITY-003 | 质量评分展示 | P1 | ✅ |
| E2E-QUALITY-004 | 创建完整性规则 | P0 | ✅ |
| E2E-QUALITY-005 | 创建唯一性规则 | P0 | ✅ |
| E2E-QUALITY-006 | 创建有效性规则 | P0 | ✅ |
| E2E-QUALITY-007 | 创建一致性规则 | P0 | ✅ |
| E2E-QUALITY-008 | 获取规则列表 | P1 | ✅ |
| E2E-QUALITY-009 | 执行质量检测 | P0 | ✅ |
| E2E-QUALITY-010 | 执行特定资产检测 | P1 | ✅ |
| E2E-QUALITY-011 | 查看问题列表 | P1 | ✅ |
| E2E-QUALITY-012 | 解决问题 | P1 | ✅ |
| E2E-QUALITY-013 | 生成质量报告 | P0 | ✅ |
| E2E-QUALITY-014 | 导出质量报告 | P1 | ✅ |
| E2E-PERM-001 | 完整链路-创建角色到权限验证 | P0 | ✅ |
| E2E-PERM-002 | 创建自定义角色 | P0 | ✅ |
| E2E-PERM-003 | 创建角色并分配权限 | P0 | ✅ |
| E2E-PERM-004 | 分配功能权限 | P0 | ✅ |
| E2E-PERM-005 | 分配数据权限 | P0 | ✅ |
| E2E-PERM-006 | 获取角色列表 | P1 | ✅ |
| E2E-PERM-007 | 创建新用户 | P0 | ✅ |
| E2E-PERM-008 | 创建用户并分配角色 | P0 | ✅ |
| E2E-PERM-009 | 获取用户列表 | P1 | ✅ |
| E2E-PERM-010 | 编辑用户 | P1 | ✅ |
| E2E-PERM-011 | 管理员权限验证 | P1 | ✅ |
| E2E-PERM-012 | 普通用户权限验证 | P1 | ✅ |
| E2E-PERM-013 | 只读用户权限验证 | P1 | ✅ |
| E2E-PERM-014 | 查看权限变更记录 | P2 | ✅ |
| E2E-MASK-001 | 完整链路-配置脱敏到效果验证 | P0 | ✅ |
| E2E-MASK-002 ~ 015 | 脱敏相关测试 | P1 | ✅ |
| E2E-BUSINESS-001 ~ 004 | 业务流程测试 | P0 | ✅ |

**总计**: 75+ 测试用例

## 生成报告

### HTML 报告

```bash
npx playwright show-report
```

报告位置: `e2e-tests/reports/html/`

### JSON 报告

报告位置: `e2e-tests/reports/json/results.json`

## 页面对象模式

本测试套件使用 Page Object 模式，提供了以下页面对象类：

- `LoginPage`: 登录页面
- `HomePage`: 首页
- `AssetPage`: 资产页面和详情页
- `CatalogPage`: 目录管理页面
- `QualityPage`: 质量检测页面
- `SystemPage`: 系统管理页面（用户、角色、数据源）
- `LineagePage`: 血缘分析页面

### 使用示例

```typescript
import LoginPage from '../pages/LoginPage';
import AssetPage from '../pages/AssetPage';

test('搜索资产', async ({ page }) => {
  const loginPage = new LoginPage(page);
  const assetPage = new AssetPage(page);
  
  // 登录
  await loginPage.goto();
  await loginPage.loginAsAdmin();
  
  // 搜索资产
  await assetPage.goto();
  await assetPage.searchAssets('customer');
  
  // 验证结果
  const count = await assetPage.getAssetCount();
  expect(count).toBeGreaterThan(0);
});
```

## 辅助工具

`utils/helpers.ts` 提供了丰富的辅助函数：

- `waitForTimeout()`: 等待指定时间
- `waitForElementVisible()`: 等待元素可见
- `mockApiResponse()`: 模拟 API 响应
- `takeScreenshot()`: 截图
- `uploadFile()`: 上传文件
- `generateTestData()`: 生成测试数据

## 配置说明

### playwright.config.ts

主要配置项：

- `testDir`: 测试目录
- `timeout`: 测试超时时间
- `retries`: 失败重试次数
- `reporter`: 报告格式
- `projects`: 浏览器配置

### e2e-config.json

主要配置项：

- `baseUrl`: 前端地址
- `apiUrl`: API 地址
- `testUsers`: 测试用户账号
- `timeouts`: 超时配置
- `datasources`: 测试数据源配置

## 最佳实践

1. **使用页面对象**: 将页面元素和操作封装在 Page 类中
2. **独立测试**: 每个测试用例应独立运行
3. **清理数据**: 在 afterEach 中清理测试数据
4. **使用 fixtures**: 使用 fixtures 管理测试数据
5. **截图**: 在失败时自动截图
6. **日志**: 添加清晰的 console.log 便于调试

## CI/CD 集成

### GitHub Actions

```yaml
name: E2E Tests
on: [push, pull_request]
jobs:
  e2e:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Setup Node
        uses: actions/setup-node@v2
        with:
          node-version: '18'
      - name: Install dependencies
        run: |
          cd edams-web
          npm install
          npx playwright install chromium
      - name: Run tests
        run: npx playwright test
```

## 常见问题

### 1. 测试超时

增加超时配置：

```typescript
test.setTimeout(60000); // 60秒
```

### 2. 元素定位失败

使用更稳定的选择器，或添加等待：

```typescript
await page.waitForSelector('.ant-table', { state: 'visible' });
```

### 3. API Mock

使用 helpers.ts 中的 mockApiResponse 函数。

## 维护

- 定期更新选择器以适应 UI 变化
- 保持测试数据与生产环境一致
- 监控测试稳定性
- 定期清理无效测试用例

## 许可证

MIT
