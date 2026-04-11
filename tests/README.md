# EDAMS 测试目录

本目录包含项目的各类测试代码。

## 目录结构

```
tests/
├── integration/         # 集成测试相关
│   ├── test-data.sql     # 测试数据SQL
│   ├── test-config.yml   # 测试配置文件
│   └── docker-compose.test.yml  # 测试环境Docker配置
├── api/                  # API测试脚本
│   ├── auth-api-tests.sh      # 认证服务API测试
│   ├── user-api-tests.sh      # 用户服务API测试
│   ├── asset-api-tests.sh     # 资产服务API测试
│   └── governance-api-tests.sh # 治理服务API测试
└── e2e/                  # 端到端测试(Playwright)
    ├── auth.spec.ts           # 认证模块
    ├── user-management.spec.ts # 用户管理
    ├── asset-lifecycle.spec.ts # 资产管理
    ├── governance-workflow.spec.ts # 数据治理
    ├── playwright.config.ts   # Playwright配置
    └── README.md              # E2E测试说明
```

## 快速开始

### API测试

```bash
# 设置基础URL
export BASE_URL=http://localhost:8080

# 运行认证API测试
bash tests/api/auth-api-tests.sh

# 运行用户API测试
bash tests/api/user-api-tests.sh

# 运行资产API测试
bash tests/api/asset-api-tests.sh

# 运行治理API测试
bash tests/api/governance-api-tests.sh
```

### E2E测试

```bash
cd tests/e2e
npm install
npx playwright install
npx playwright test
```

### 集成测试环境

使用Docker Compose启动测试数据库:

```bash
cd tests/integration
docker-compose -f docker-compose.test.yml up -d
```

## 测试覆盖率

### 后端单元测试
- auth-service: Controller + Service测试
- gateway: 路由 + 过滤器测试
- metadata-service: Controller测试
- lineage-service: Controller测试
- quality-service: Controller测试
- standard-service: Controller测试
- governance-engine: Controller测试

### API测试
- 认证: 登录、注册、Token、MFA
- 用户: CRUD、搜索、分页
- 资产: 注册、查询、版本、生命周期
- 治理: 策略、任务、推荐、合规

### E2E测试
- 认证流程: 登录、注册、登出
- 用户管理: 增删改查、角色分配
- 资产管理: 注册、查看、归档
- 生命周期: 历史记录、版本管理
- 数据治理: 策略、审批流、标准
- 血缘关系: 可视化、影响分析

## 持续集成

测试在以下时机自动执行:
- PR创建/更新时
- 代码合并到主分支时
- 定时任务（每日凌晨）

详见 `.gitlab-ci.yml` 和 `Jenkinsfile`。
