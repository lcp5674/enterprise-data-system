# Enterprise Data Asset Management System

企业数据资产管理系统 - 微服务架构

## 项目简介

本项目是一个基于 Spring Cloud Alibaba 的微服务架构企业数据资产管理系统，提供数据目录管理、血缘分析追踪、质量监控、安全合规、生命周期管理、智能搜索等核心功能。系统采用多数据库策略，支持关系型数据、图数据、文档型数据、时序数据等多种数据源的统一管理。

### 核心功能特性

- **数据目录管理**：统一的数据资产目录，支持多维度分类检索
- **血缘关系分析**：基于图数据库的血缘追踪，可视化数据流转路径
- **数据质量管理**：多维度质量规则配置，自动质量检测与告警
- **安全合规管控**：数据脱敏、访问控制、合规审计全链路支持
- **生命周期管理**：数据资产的全生命周期状态管理与归档策略
- **智能搜索洞察**：基于 Elasticsearch 的全文检索与 AI 智能分析
- **多端应用支持**：Web 管理平台 + Flutter 移动端应用

## 技术栈

### 后端技术

| 类别 | 技术选型 |
|------|----------|
| 基础框架 | Spring Boot 3.2.x, Spring Cloud Alibaba 2022.0.0.0 |
| 服务治理 | Nacos (注册中心/配置中心), Sentinel (流量控制), Seata (分布式事务) |
| 消息队列 | Apache Kafka |
| 缓存层 | Redis Cluster |
| 搜索引擎 | Elasticsearch 8.x |
| 图数据库 | Neo4j 5.x (血缘关系存储) |
| 关系数据库 | MySQL 8.x, PostgreSQL 15+ |
| 文档数据库 | MongoDB 6.x |
| 时序数据库 | TimescaleDB (指标数据) |
| 任务调度 | XXL-Job |
| 服务网格 | Istio |
| 容器编排 | Kubernetes, Docker |

### 前端技术

| 类别 | 技术选型 |
|------|----------|
| 框架 | React 18 |
| 语言 | TypeScript |
| 组件库 | Ant Design Pro |
| 构建工具 | Vite |
| 状态管理 | Redux Toolkit |
| 路由 | React Router v6 |

### 移动端技术

| 类别 | 技术选型 |
|------|----------|
| 框架 | Flutter 3.x |
| 语言 | Dart |
| 状态管理 | Provider / Riverpod |
| HTTP客户端 | Dio |
| 本地存储 | Hive |

## 项目结构

```
enterprise-data-system/
├── microservices/                      # 微服务主目录
│   └── edams-parent/                   # 父POM工程 (Spring Boot多模块)
│       ├── edams-auth/                 # 认证授权服务
│       │   ├── src/main/java/.../
│       │   │   ├── controller/        # REST控制器
│       │   │   ├── service/           # 业务服务层
│       │   │   ├── repository/        # 数据访问层
│       │   │   └── model/             # 数据模型
│       │   └── src/test/java/         # 单元测试
│       ├── edams-user/                # 用户管理服务
│       ├── edams-permission/          # 权限管理服务
│       ├── edams-workflow/            # 工作流服务
│       ├── edams-notification/        # 通知服务
│       ├── edams-knowledge/           # 知识图谱服务
│       ├── edams-lifecycle/           # 生命周期管理服务
│       ├── edams-aiops/               # AIOps智能运维服务
│       ├── edams-llm/                 # 大语言模型集成服务
│       ├── edams-chatbot/             # 智能问答服务
│       ├── edge-iot-service/          # 边缘IoT服务
│       ├── value-service/             # 资产价值评估服务
│       ├── sandbox-service/           # 沙箱环境服务
│       ├── watermark-service/         # 水印服务
│       ├── sla-service/               # SLA监控服务
│       └── incentive-service/         # 激励服务
├── edams-discovery/                   # 服务发现与配置
│   └── datasource-service/            # 数据源配置服务
├── edams-web/                         # Web前端应用
│   ├── src/pages/                     # 页面组件
│   ├── src/components/                # 公共组件
│   └── public/                        # 静态资源
├── edams-mobile/                      # Flutter移动端应用
│   ├── lib/
│   │   ├── pages/                    # 页面
│   │   ├── widgets/                  # 组件
│   │   └── services/                 # 服务
│   └── pubspec.yaml
├── edams-parent/                      # 遗留父工程 (兼容)
├── services/                          # 核心业务服务
├── e2e-tests/                         # 端到端测试
├── edams-integration-tests/          # 集成测试
├── docker/                           # Docker配置
│   ├── docker-compose.yml            # 容器编排配置
│   ├── Dockerfile                    # 镜像构建文件
│   └── prometheus.yml                # Prometheus监控配置
├── infrastructure/                   # 基础设施配置
│   ├── kubernetes/                   # K8s部署配置
│   └── helm/                        # Helm Chart包
├── scripts/                          # 脚本工具
│   ├── dev-start.sh                 # 开发环境启动脚本
│   ├── build.sh                     # 构建脚本
│   └── deploy.sh                    # 部署脚本
└── *.md                             # 项目文档 (根目录)
```

## 快速开始

### 环境要求

| 组件 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| Flutter | 3.x |
| Docker | 20.x+ |
| Docker Compose | 2.x+ |

### 前置服务启动

1. 启动基础中间件服务

```bash
cd docker
docker-compose up -d
```

基础服务包含：MySQL、Redis、Nacos、Sentinel、Kafka、Neo4j、Elasticsearch、MongoDB

2. 等待服务就绪

```bash
# 等待Nacos启动（约30秒）
sleep 30

# 验证Nacos控制台
curl http://localhost:8848/nacos
```

### 后端服务启动

1. 编译项目

```bash
# 进入微服务目录
cd microservices/edams-parent

# 编译所有模块
mvn clean install -DskipTests

# 或仅编译指定模块
mvn clean install -pl edams-auth -am -DskipTests
```

2. 启动微服务

```bash
cd scripts
chmod +x *.sh
./dev-start.sh
```

3. 服务启动顺序建议

```
1. edams-auth (认证服务 - 基础依赖)
2. edams-user (用户服务)
3. edams-permission (权限服务)
4. 其他业务服务...
```

### 前端应用启动

1. Web管理平台

```bash
cd edams-web
npm install
npm run dev
# 访问 http://localhost:3000
```

2. 移动端应用

```bash
cd edams-mobile
flutter pub get
flutter run
```

### 访问入口

| 服务 | 地址 | 默认账号 |
|------|------|----------|
| Nacos控制台 | http://localhost:8848/nacos | nacos/nacos |
| Sentinel控制台 | http://localhost:8858 | sentinel/sentinel |
| Neo4j Browser | http://localhost:7474 | neo4j/password |
| Elasticsearch | http://localhost:9200 | - |
| API网关 | http://localhost:8888 | - |
| Web管理平台 | http://localhost:3000 | - |

## 开发指南

### 代码规范

遵循《企业数据资产管理系统开发规范文档》中的规范：

- **命名规范**：类名使用 UpperCamelCase，方法/变量使用 lowerCamelCase，常量使用 UPPER_SNAKE_CASE
- **Git提交**：使用 Conventional Commits 规范，格式：`<type>(<scope>): <subject>`
- **API设计**：遵循 RESTful 设计原则，使用标准 HTTP 方法和状态码
- **代码审查**：遵循 Code Review 检查清单，确保代码质量和安全性

详细规范请参考根目录文档：

- [开发规范文档](./企业数据资产管理系统开发规范文档.md)
- [API接口设计文档](./企业数据资产管理系统API接口设计文档.md)

### 模块开发规范

1. **创建新微服务模块**

```bash
# 在 microservices/edams-parent 下创建新模块
cd microservices/edams-parent
mkdir -p edams-{module-name}/src/main/java/com/enterprise/edams/{module}/
mkdir -p edams-{module-name}/src/main/resources/
mkdir -p edams-{module-name}/src/test/java/com/enterprise/edams/{module}/
```

2. **添加父POM依赖**

在 `microservices/edams-parent/pom.xml` 的 `<modules>` 中添加：

```xml
<modules>
    <!-- existing modules -->
    <module>edams-{module-name}</module>
</modules>
```

3. **配置Nacos服务注册**

在 `src/main/resources/bootstrap.yml` 中配置：

```yaml
spring:
  application:
    name: edams-{module-name}
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_HOST:localhost}:8848
      config:
        server-addr: ${NACOS_HOST:localhost}:8848
        file-extension: yml
```

4. **实现标准目录结构**

```
edams-{module-name}/
├── pom.xml
├── src/main/java/com/enterprise/edams/{module}/
│   ├── {ModuleName}Application.java      # 启动类
│   ├── config/                          # 配置类
│   ├── controller/                      # REST控制器
│   │   └── {EntityName}Controller.java
│   ├── service/                        # 服务层
│   │   ├── {EntityName}Service.java    # 接口
│   │   └── impl/                       # 实现
│   │       └── {EntityName}ServiceImpl.java
│   ├── repository/                     # 数据访问层
│   │   └── {EntityName}Repository.java
│   ├── model/                          # 数据模型
│   │   ├── entity/                     # 实体类
│   │   ├── dto/                        # 数据传输对象
│   │   ├── vo/                         # 视图对象
│   │   └── enums/                      # 枚举类
│   └── common/                         # 模块公共代码
└── src/main/resources/
    ├── bootstrap.yml
    └── mapper/                         # MyBatis Mapper文件
```

5. **添加数据库迁移脚本**

在 `scripts/db/migration/` 目录下添加 Flyway 或 Liquibase 脚本。

### API调用规范

使用 Feign Client 进行服务间调用：

```java
@FeignClient(name = "edams-auth", path = "/api/v1/auth")
public interface AuthFeignClient {

    @PostMapping("/login")
    Result<LoginResponse> login(@RequestBody LoginRequest request);

    @PostMapping("/logout")
    Result<Void> logout(@RequestHeader("Authorization") String token);
}
```

### 消息队列使用

使用 Kafka 进行异步消息处理：

```java
@KafkaListener(topics = "edams-topic", groupId = "edams-group")
public void consumeMessage(ConsumerRecord<String, String> record) {
    // 消息处理逻辑
}
```

## 文档

详细技术文档请参考根目录文档：

| 文档名称 | 说明 |
|----------|------|
| [技术架构文档](./企业数据资产管理系统技术架构文档-最终版.md) | 系统架构、技术选型、部署方案 |
| [开发规范文档](./企业数据资产管理系统开发规范文档.md) | 代码规范、Git流程、API设计标准 |
| [数据库设计文档](./企业数据资产管理系统数据库详细设计.md) | 表结构设计、索引设计 |
| [API接口设计文档](./企业数据资产管理系统API接口设计文档.md) | RESTful接口规范、400+接口定义 |
| [数据同步方案](./企业数据资产管理系统数据同步方案详细设计.md) | 多数据源同步策略 |
| [缓存架构设计](./企业数据资产管理系统缓存架构详细设计.md) | 缓存策略、高可用设计 |
| [服务间通信协议](./企业数据资产管理系统服务间通信协议详细设计.md) | Feign、Kafka通信规范 |
| [集成方案设计](./企业数据资产管理系统集成方案设计.md) | 第三方服务集成 |
| [数据治理引擎](./企业数据资产管理系统数据治理引擎详细设计.md) | 治理策略引擎设计 |

## License

Proprietary - Enterprise Data Management System
