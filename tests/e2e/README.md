# EDAMS E2E 测试

本目录包含EDAMS项目的端到端（E2E）测试，使用Playwright框架编写。

## 目录结构

```
tests/e2e/
├── auth.spec.ts              # 认证模块测试
├── user-management.spec.ts   # 用户管理测试
├── asset-lifecycle.spec.ts   # 资产管理与生命周期测试
├── governance-workflow.spec.ts # 数据治理与审批流测试
├── playwright.config.ts      # Playwright配置文件
└── README.md                 # 本文件
```

## 前置要求

1. Node.js >= 18.x
2. npm >= 9.x
3. 后端服务运行在 http://localhost:8080
4. 前端服务运行在 http://localhost:3000

## 安装依赖

```bash
cd tests/e2e
npm install
npx playwright install
```

## 运行测试

### 运行所有测试
```bash
npx playwright test
```

### 运行特定测试文件
```bash
npx playwright test auth.spec.ts
```

### 运行特定标签的测试
```bash
npx playwright test --grep "登录"
```

### 在UI模式下运行
```bash
npx playwright test --ui
```

### 生成测试报告
```bash
npx playwright test --reporter=html
open playwright-report/index.html
```

## 测试覆盖

### 认证模块 (auth.spec.ts)
- [x] 登录成功
- [x] 登录失败 - 错误密码
- [x] 登录失败 - 用户不存在
- [x] 注册新用户
- [x] 注册表单验证
- [x] MFA验证流程
- [x] Token过期处理
- [x] 登出功能
- [x] 记住登录状态

### 用户管理 (user-management.spec.ts)
- [x] 用户列表加载
- [x] 分页功能
- [x] 搜索用户
- [x] 创建新用户
- [x] 编辑用户
- [x] 删除用户
- [x] 禁用/启用用户
- [x] 重置密码
- [x] 分配角色
- [x] 部门管理

### 资产管理 (asset-lifecycle.spec.ts)
- [x] 资产列表加载
- [x] 资产搜索
- [x] 按类型筛选
- [x] 查看资产详情
- [x] 注册新资产
- [x] 查看生命周期历史
- [x] 创建生命周期记录
- [x] 归档资产
- [x] 版本历史查看
- [x] 版本对比
- [x] 版本回滚
- [x] 执行质量检查
- [x] 查看质量报告

### 数据治理 (governance-workflow.spec.ts)
- [x] 查看治理仪表盘
- [x] 创建治理策略
- [x] 启用/禁用策略
- [x] AI推荐建议
- [x] 质量趋势分析
- [x] 发起审批流程
- [x] 查看待我审批
- [x] 审批通过
- [x] 审批拒绝
- [x] 审批流程查询
- [x] 查看流程图
- [x] 查看数据标准列表
- [x] 创建数据标准
- [x] 执行合规检查
- [x] 查看资产血缘
- [x] 血缘关系可视化
- [x] 上游/下游血缘
- [x] 影响分析

## CI/CD集成

在CI环境中运行:
```bash
CI=true npx playwright test
```

## 调试

### 查看测试录像
1. 运行测试后，打开 `playwright-report/index.html`
2. 点击失败的测试
3. 点击 "Traces" 查看操作录像

### 本地调试
```bash
npx playwright test --debug
```

## 常见问题

### Q: 测试报找不到元素
A: 检查页面是否完全加载，增加适当的等待时间。

### Q: 测试在CI失败但本地通过
A: 确保CI环境的服务端口可用，检查网络策略。

### Q: 如何跳过某些测试?
A: 使用 `test.skip()` 或 `test.fixme()` 标记。
