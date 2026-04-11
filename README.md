# EDAMS - 企业数据资产管理系统

> 基于 Spring Cloud Alibaba 的微服务架构企业数据资产管理系统

## 🚀 快速开始

```bash
# 克隆项目
git clone <repository-url>

# 查看完整文档
cat docs/README.md

# 查看部署指南
cat docs/guides/SYSTEM_DEPLOYMENT_GUIDE.md
```

## 📦 项目结构

```
enterprise-data-system/
├── backend/              # 后端微服务
│   ├── core/            # 核心服务（gateway, auth, asset...）
│   ├── domain/          # 域服务（metadata, lineage, quality...）
│   └── discovery/       # 服务发现
├── frontend/            # React前端
│   └── edams-web/
├── mobile/              # Flutter移动端
│   └── edams-mobile/
├── tests/               # 测试
├── docs/                # 📚 完整文档（请查看此处）
│   ├── README.md        # 文档中心首页
│   ├── requirements/    # 需求分析
│   ├── architecture/    # 架构设计
│   ├── database/       # 数据库设计
│   ├── security/       # 安全设计
│   ├── ai/             # AI服务
│   ├── governance/     # 数据治理
│   ├── testing/        # 测试文档
│   ├── operations/     # 运维文档
│   ├── deployment/     # 部署文档
│   ├── integration/     # 集成方案
│   ├── api/            # API设计
│   ├── standards/      # 开发规范
│   ├── reports/         # 项目报告
│   └── guides/         # 用户指南
├── infrastructure/      # 基础设施配置
└── scripts/             # 脚本工具
```

## 🎯 核心功能

| 功能 | 说明 |
|-----|------|
| 数据目录 | 统一的数据资产目录，多维度分类检索 |
| 血缘分析 | 基于图数据库的血缘追踪，可视化数据流转 |
| 质量管理 | 多维度质量规则配置，自动检测与告警 |
| 安全合规 | 数据脱敏、访问控制、合规审计 |
| 生命周期 | 全生命周期状态管理与归档策略 |
| 智能搜索 | Elasticsearch全文检索 + AI智能分析 |

## 🛠️ 技术栈

| 层级 | 技术 |
|-----|------|
| 后端框架 | Spring Boot 3.2.x, Spring Cloud Alibaba 2022.0.0.0 |
| 服务治理 | Nacos, Sentinel, Seata |
| 消息队列 | Apache Kafka |
| 缓存 | Redis Cluster |
| 搜索引擎 | Elasticsearch 8.x |
| 图数据库 | Neo4j 5.x |
| 数据库 | MySQL 8.x, PostgreSQL, MongoDB, TimescaleDB |
| 前端 | React 18 + TypeScript + Ant Design |
| 移动端 | Flutter |

## 📚 文档导航

| 文档类型 | 路径 | 说明 |
|---------|------|------|
| **文档中心** | [docs/README.md](docs/README.md) | 所有文档索引 |
| **需求文档** | [docs/requirements/](docs/requirements/) | 需求分析与商业论证 |
| **架构设计** | [docs/architecture/](docs/architecture/) | 技术/信息架构设计 |
| **API设计** | [docs/api/](docs/api/) | 接口设计文档 |
| **数据库设计** | [docs/database/](docs/database/) | 数据库详细设计 |
| **安全设计** | [docs/security/](docs/security/) | 安全架构与渗透测试 |
| **部署文档** | [docs/deployment/](docs/deployment/) | CI/CD与部署指南 |
| **运维手册** | [docs/operations/](docs/operations/) | 运维SLA与操作手册 |
| **测试文档** | [docs/testing/](docs/testing/) | 测试策略与方案 |
| **开发规范** | [docs/standards/](docs/standards/) | 开发规范文档 |

## 📊 项目状态

| 指标 | 状态 |
|-----|------|
| 代码实现 | 98% |
| 架构版本 | V2.0 |
| 微服务数量 | 31个 |
| P0/P1/P2修复 | 全部完成 ✅ |

## 📞 联系方式

- **项目负责人**: EDAMS Team
- **邮箱**: edams-team@enterprise.com

---
**📖 提示**: 完整文档请访问 [docs/README.md](docs/README.md)
