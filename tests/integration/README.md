# EDAMS Integration Tests

本模块包含EDAMS项目的集成测试和契约测试。

## 技术栈

- **JUnit 5** - 测试框架
- **Spring Cloud Contract** - 契约测试
- **TestContainers** - 容器化测试环境
- **REST Assured** - HTTP客户端测试

## 目录结构

```
integration/
├── src/test/java/com/enterprise/edams/
│   ├── contract/              # 契约测试
│   │   └── DataCatalogContractTest.java
│   └── integration/            # 集成测试
│       ├── BaseIntegrationTest.java
│       ├── AssetLifecycleIntegrationTest.java
│       ├── DataQualityIntegrationTest.java
│       └── LineageIntegrationTest.java
├── src/test/resources/
│   ├── application-test.yml    # 测试配置
│   └── contracts/              # 契约定义文件
│       ├── catalog/
│       │   ├── getAsset.groovy
│       │   ├── listAssets.groovy
│       │   ├── createAsset.groovy
│       │   └── searchAssets.groovy
│       ├── quality/
│       │   ├── checkQuality.groovy
│       │   └── getCheckResult.groovy
│       └── lineage/
│           └── getLineageGraph.groovy
└── pom.xml
```

## 运行测试

### 本地运行

```bash
cd /workspace/tests/integration
mvn test
```

### 运行契约测试

```bash
mvn test -Dtest="*ContractTest*"
```

### 运行集成测试

```bash
mvn test -Dtest="*IntegrationTest*"
```

### 运行特定测试类

```bash
mvn test -Dtest=DataCatalogContractTest
mvn test -Dtest=DataQualityIntegrationTest
```

## 测试类型

### 契约测试 (Contract Tests)

契约测试确保服务提供者和服务消费者之间的API接口一致性。

- `DataCatalogContractTest` - 资产目录契约测试
  - 获取资产详情
  - 获取资产列表
  - 创建资产
  - 更新资产
  - 删除资产
  - 搜索资产

### 集成测试 (Integration Tests)

集成测试验证多个服务组件之间的交互。

- `AssetLifecycleIntegrationTest` - 资产生命周期集成测试
  - 创建资产 (DRAFT)
  - 提交审核 (PENDING_REVIEW)
  - 审核通过 (APPROVED)
  - 发布上线 (ACTIVE)
  - 废弃 (DEPRECATED)
  - 归档 (ARCHIVED)

- `DataQualityIntegrationTest` - 数据质量集成测试
  - 创建质量规则
  - 触发质量检查
  - 获取检查结果
  - 问题管理

- `LineageIntegrationTest` - 血缘关系集成测试
  - 获取表级血缘
  - 获取字段级血缘
  - 血缘路径查询
  - 影响分析

## 测试环境

测试使用TestContainers启动以下容器：

- **MySQL 8.0** - 测试数据库
- **Redis 7** - 缓存服务

## 配置说明

测试配置通过 `application-test.yml` 管理：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/edams_test
    username: test
    password: test123
  redis:
    host: localhost
    port: 6379
```

## 契约定义

契约文件使用Groovy DSL编写，位于 `src/test/resources/contracts/` 目录。

### 契约文件命名规范

- `<operation>.groovy` - 操作契约
- 例如：`getAsset.groovy`, `createAsset.groovy`

### 契约结构

```groovy
org.springframework.cloud.contract.spec.Contract.make {
    description "描述契约用途"
    request {
        method 'GET'
        url '/api/v1/assets'
    }
    response {
        status 200
        body([...])
    }
}
```

## 持续集成

在CI环境中运行测试：

```bash
mvn verify -Ptest
```

## 注意事项

1. 测试需要Docker环境支持TestContainers
2. 确保MySQL和Redis容器可以正常启动
3. 契约测试会生成 stubs，可用于其他服务的消费者测试
