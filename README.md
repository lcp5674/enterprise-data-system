# Enterprise Data Asset Management System

企业数据资产管理系统 - 微服务架构

## 项目简介

本项目是一个基于Spring Cloud Alibaba的微服务架构企业数据资产管理系统，提供数据目录、血缘管理、质量监控、安全合规等核心功能。

## 技术栈

### 后端技术
- **框架**: Spring Boot 3.2.x, Spring Cloud Alibaba 2022.0.0.0
- **服务治理**: Nacos, Sentinel, Seata
- **消息队列**: Apache Kafka
- **缓存**: Redis
- **搜索引擎**: Elasticsearch 8.x
- **图数据库**: Neo4j
- **数据库**: MySQL 8.x, MongoDB, TimescaleDB
- **任务调度**: XXL-Job

### 前端技术
- **框架**: React 18
- **语言**: TypeScript
- **组件库**: Ant Design Pro
- **构建工具**: Vite

## 项目结构

```
enterprise-data-system/
├── edams-parent/                 # 父POM工程
├── edams-common/                 # 公共模块
├── edams-gateway/                # API网关
├── edams-auth/                   # 认证服务
├── edams-admin/                  # 管理服务
├── edams-asset/                  # 资产管理服务
├── edams-lineage/                # 血缘管理服务
├── edams-quality/                # 质量管理服务
├── edams-security/               # 安全合规模块
├── edams-catalog/                # 数据目录模块
├── edams-search/                 # 搜索模块
├── edams-knowledge/              # 知识图谱模块
├── edams-insight/                # 智能洞察模块
├── edams-monitor/                # 监控服务
├── edams-scheduler/              # 调度服务
├── docker/                       # Docker配置
├── scripts/                      # 脚本文件
└── docs/                         # 项目文档
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- Docker & Docker Compose
- Node.js 18+

### 启动步骤

1. 启动基础服务
```bash
cd docker
docker-compose up -d mysql redis nacos sentinel
```

2. 等待Nacos就绪（约30秒）
```bash
sleep 30
```

3. 启动微服务
```bash
cd scripts
./dev-start.sh
```

4. 访问服务
- Nacos控制台: http://localhost:8848/nacos
- API网关: http://localhost:8888
- 前端开发: http://localhost:3000

## 开发指南

### 代码规范

遵循《企业数据资产管理系统开发规范文档》中的规范：

- 命名规范：使用UpperCamelCase和lowerCamelCase
- Git提交：使用Conventional Commits规范
- API设计：遵循RESTful设计原则
- 代码审查：遵循Code Review检查清单

### 模块开发

1. 在edams-parent/pom.xml中添加模块
2. 创建模块目录结构
3. 实现业务代码
4. 添加数据库迁移脚本
5. 配置Nacos服务发现和配置

## 文档

详细技术文档请参考docs目录：

- [技术架构文档](./docs/技术架构文档.md)
- [开发规范文档](./docs/开发规范文档.md)
- [数据库设计文档](./docs/数据库设计文档.md)
- [接口设计文档](./docs/接口设计文档.md)

## License

Proprietary - Enterprise Data Management System
