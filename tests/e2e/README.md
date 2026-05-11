# EDAMS E2E Tests

本目录包含EDAMS项目的前端E2E测试，使用Playwright实现。

## 技术栈

- **Playwright** - E2E测试框架
- **TypeScript** - 测试代码语言
- **@playwright/test** - 测试运行器

## 目录结构

```
e2e/
├── helpers/                    # 测试辅助工具
│   ├── test-fixtures.ts        # 测试夹具定义
│   └── page-helpers.ts         # 页面操作辅助函数
├── *.spec.ts                   # E2E测试用例
├── playwright.config.ts         # Playwright配置
├── package.json                 # 依赖配置
└── README.md
```

## 测试用例

### 资产生命周期测试 (asset-lifecycle.spec.ts)

- 创建数据资产并完成生命周期
- 资产搜索功能
- 资产筛选功能
- 资产收藏功能
- 资产详情页签切换
- 批量选择资产
- 批量删除资产
- 批量导出资产
- 下载导入模板
- 导入资产数据

### 数据生命周期测试 (data-lifecycle.spec.ts)

- 完整生命周期流转 (DRAFT -> PENDING_REVIEW -> APPROVED -> ACTIVE -> ARCHIVED)
- 生命周期状态转换异常处理
- 审核拒绝后状态管理
- 从已废弃状态恢复
- 生命周期历史记录查看
- 生命周期状态过滤器
- 资产认证申请

### 数据分类分级测试 (classification.spec.ts)

- 查看数据分类概览
- 查看分类统计图表
- 查看敏感数据分布
- 查看分类层级结构
- 搜索分类
- 查看分类详情
- 敏感数据类型分布
- 敏感数据扫描结果
- 字段级敏感信息
- 风险评估
- 导出敏感数据报告
- 脱敏规则管理
- 合规性检查

### 质量检查测试 (quality-check.spec.ts)

- 执行质量检查
- 查看质量概览仪表盘
- 查看质量趋势图表
- 按规则类型筛选
- 质量规则管理 (CRUD)
- 规则模板使用
- 检查历史查看
- 检查详情查看
- 质量问题追踪
- 问题认领、解决、转派、忽略

### 认证流程测试 (auth.spec.ts)

- 登录成功
- 登录失败处理
- 注册新用户
- 注册表单验证
- MFA验证流程
- Token过期处理
- 登出功能
- 会话管理

### 治理工作流测试 (governance-workflow.spec.ts)

- 查看治理仪表盘
- 创建治理策略
- 启用/禁用策略
- AI推荐建议
- 质量趋势分析
- 发起审批流程
- 查看待我审批
- 审批通过/拒绝
- 审批流程查询
- 流程图查看
- 数据标准管理
- 血缘关系可视化

## 运行测试

### 安装依赖

```bash
cd tests/e2e
npm install
```

### 安装浏览器

```bash
npm run install:browsers
```

### 运行所有测试

```bash
npm test
```

### 运行带UI的测试

```bash
npm run test:ui
```

### 运行特定浏览器测试

```bash
npm run test:chromium
npm run test:firefox
```

### 运行移动端测试

```bash
npm run test:mobile
```

### 运行特定测试文件

```bash
npx playwright test auth.spec.ts
npx playwright test asset-lifecycle.spec.ts
```

### 运行特定测试用例

```bash
npx playwright test -g "登录成功"
```

### Debug模式

```bash
npm run test:debug
```

## 配置说明

在 `playwright.config.ts` 中配置：

```typescript
export default defineConfig({
  testDir: './',
  baseURL: process.env.BASE_URL || 'http://localhost:3000',
  timeout: 120000,
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
    { name: 'Mobile Chrome', use: { ...devices['Pixel 5'] } },
  ],
});
```

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| BASE_URL | 测试目标URL | http://localhost:3000 |
| CI | 是否在CI环境 | - |
| HEADED | 是否使用有头模式 | false |

## 辅助函数

使用 `helpers/page-helpers.ts` 中的辅助函数：

```typescript
import { login, logout, createTestAsset, navigateToAsset } from './helpers/page-helpers';

// 登录
await login(page);

// 创建测试资产
const assetName = await createTestAsset(page);

// 导航到资产详情
await navigateToAsset(page, assetName);

// 登出
await logout(page);
```

## 测试报告

测试完成后，生成以下报告：

- `playwright-report/` - HTML报告
- `playwright-results.json` - JSON格式结果

查看HTML报告：

```bash
npm run report
```

## 持续集成

在CI环境中运行：

```bash
npm run test:ci
```

GitLab CI配置示例：

```yaml
e2e-tests:
  stage: test
  script:
    - cd tests/e2e
    - npm ci
    - npx playwright install --with-deps
    - npm run test:ci
  artifacts:
    reports:
      junit: playwright-results.xml
    paths:
      - playwright-report/
```

## 注意事项

1. 测试前确保前端服务已启动
2. 测试会创建/修改数据，建议使用测试环境
3. 某些测试依赖特定的初始数据状态
4. 移动端测试需要真机或模拟器支持

## 最佳实践

1. 每个测试用例应独立，不依赖其他测试的执行结果
2. 使用显式等待而非固定延迟
3. 测试失败时自动截图和录制视频
4. 定期清理测试数据
5. 使用page objects模式组织页面元素定位器
