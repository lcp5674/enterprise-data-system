# EDAMS 测试模块

本项目包含EDAMS企业数据资产管理系统的完整测试套件，包括单元测试、集成测试和E2E测试。

## 目录结构

```
tests/
├── api/                        # API测试脚本
│   ├── asset-api-tests.sh
│   ├── auth-api-tests.sh
│   ├── governance-api-tests.sh
│   └── user-api-tests.sh
├── e2e/                        # E2E测试 (Playwright)
│   ├── helpers/                # 测试辅助工具
│   ├── asset-lifecycle.spec.ts # 资产生命周期测试
│   ├── auth.spec.ts            # 认证流程测试
│   ├── classification.spec.ts   # 数据分类分级测试
│   ├── data-lifecycle.spec.ts  # 数据生命周期测试
│   ├── governance-workflow.spec.ts # 治理工作流测试
│   ├── quality-check.spec.ts    # 质量检查测试
│   ├── playwright.config.ts    # Playwright配置
│   ├── package.json
│   └── README.md
├── integration/                # 集成测试 (Spring Cloud Contract)
│   ├── src/test/java/
│   │   └── com/enterprise/edams/
│   │       ├── contract/       # 契约测试
│   │       └── integration/    # 集成测试
│   ├── src/test/resources/
│   │   ├── application-test.yml
│   │   └── contracts/          # 契约定义文件
│   ├── pom.xml
│   └── README.md
├── integration/                # Docker Compose测试环境
│   ├── docker-compose.test.yml
│   ├── test-config.yml
│   └── test-data.sql
└── README.md                   # 本文件
```

## 测试类型

### 1. API测试 (api/)

基于Shell脚本的API功能测试，用于快速验证后端API接口。

```bash
cd tests/api
./asset-api-tests.sh
./auth-api-tests.sh
```

### 2. 集成测试 (integration/)

使用Spring Cloud Contract和JUnit 5实现的服务间集成测试。

**技术栈：**
- JUnit 5
- Spring Cloud Contract
- TestContainers
- REST Assured

**运行测试：**
```bash
cd tests/integration
mvn test
```

**测试覆盖：**
- 资产生命周期集成测试
- 数据质量集成测试
- 血缘关系集成测试
- 契约测试（API兼容性验证）

### 3. E2E测试 (e2e/)

使用Playwright实现的前端E2E测试，覆盖完整的用户操作流程。

**技术栈：**
- Playwright
- TypeScript
- @playwright/test

**运行测试：**
```bash
cd tests/e2e
npm install
npm test
```

**测试覆盖：**
- 用户认证流程
- 资产生命周期管理
- 数据分类分级
- 质量检查与追踪
- 治理工作流
- 血缘关系可视化

## 快速开始

### 前置条件

- Node.js >= 18.0.0
- Java 17+
- Maven 3.8+
- Docker (用于TestContainers)

### 运行所有测试

```bash
# E2E测试
cd tests/e2e && npm install && npm test

# 集成测试
cd tests/integration && mvn test

# API测试
cd tests/api && bash asset-api-tests.sh
```

## 测试报告

### E2E测试报告

- HTML报告：`playwright-report/index.html`
- JSON结果：`playwright-results.json`

### 集成测试报告

- Surefire报告：`target/surefire-reports/`

## CI/CD集成

### GitLab CI配置示例

```yaml
test:integration:
  stage: test
  script:
    - cd tests/integration
    - mvn test
  artifacts:
    reports:
      junit: tests/integration/target/surefire-reports/*.xml

test:e2e:
  stage: test
  script:
    - cd tests/e2e
    - npm ci
    - npx playwright install --with-deps
    - npm run test:ci
  artifacts:
    reports:
      junit: tests/e2e/playwright-results.xml
    paths:
      - tests/e2e/playwright-report/
```

## 最佳实践

1. **测试隔离**：每个测试用例应独立运行，不依赖其他测试的执行结果
2. **显式等待**：使用Playwright的自动等待机制，避免使用固定延迟
3. **Page Objects**：使用Page Objects模式组织页面元素定位器
4. **测试数据**：使用工厂方法或fixture创建测试数据
5. **失败处理**：配置自动截图和录制功能，便于问题排查

## 测试覆盖率目标

| 测试类型 | 覆盖率目标 | 状态 |
|---------|-----------|------|
| API集成测试 | 核心API 100% | ✅ 已实现 |
| 契约测试 | 主要服务接口 | ✅ 已实现 |
| E2E测试 | 核心业务流程 | ✅ 已实现 |
| 单元测试 | 各服务内部逻辑 | ⚠️ 待补充 |

## 持续改进

- 定期审查测试用例，移除过时的测试
- 根据用户反馈添加新的测试场景
- 优化测试执行时间，提高CI/CD效率
- 补充性能测试和压力测试

## 联系方式

如有问题或建议，请联系测试团队。
