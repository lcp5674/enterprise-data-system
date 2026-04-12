# EDAMS 服务端口分配表 (V2.0)

> 本文档定义了所有微服务的端口分配，确保无端口冲突。
> 所有服务均不使用 context-path，通过 Spring Cloud Gateway 的 Nacos 服务发现进行路由。

## 端口分配总表

### Gateway (1个)
| 服务 | 端口 | 说明 |
|------|------|------|
| edams-gateway | 8888 | API网关，统一入口 |

### 核心服务 edams-parent (14个)
| 服务 | 端口 | 说明 |
|------|------|------|
| edams-auth | 8081 | 认证/JWT/MFA |
| edams-asset | 8082 | 资产管理核心 |
| edams-permission | 8083 | RBAC权限 |
| edams-notification | 8084 | 多渠道通知 |
| edams-workflow | 8085 | 审批流引擎 |
| edams-lifecycle | 8086 | 生命周期管理 |
| edams-analytics | 8087 | 分析报表 |
| edams-collaboration | 8096 | 协作评论 |
| edams-report | 8108 | 报表生成 |
| edams-user | 8089 | 用户/部门管理 |
| edams-knowledge | 8010 | 知识图谱 |
| edams-llm | 8011 | 大模型配额 |
| edams-aiops | 8020 | 智能运维 |
| edams-chatbot | 8021 | AI对话 |

### P3 小型服务 (7个)
| 服务 | 端口 | 说明 |
|------|------|------|
| sandbox-service | 8104 | 沙箱环境 |
| sla-service | 8105 | SLA监控 |
| edams-version | 8095 | 版本管理 |
| value-service | 8113 | 价值评估 |
| watermark-service | 8096 | 数字水印 |
| incentive-service | 8097 | 积分激励 |
| (edge-iot-service) | - | 未实体化 |

### 域服务 domain (10个)
| 服务 | 端口 | 说明 | 状态 |
|------|------|------|------|
| governance-engine | 8014 | 治理引擎 | ✅ |
| **quality-service** | **8098** | 质量检测 | 🔄 已修复（从8085改为8098）|
| lineage-service | 8092 | 数据血缘 | ✅ |
| metadata-service | 8090 | 元数据管理 | ✅ |
| **standard-service** | **8099** | 数据标准 | 🔄 已修复（从8084改为8099）|
| **analytics-service** | **8100** | ClickHouse分析 | 🔄 已修复（从8089改为8100）|
| rule-engine-service | 8018 | Drools规则引擎 | ✅ |
| portal-service | 8093 | 工作台门户 | ✅ |
| admin-service | 8094 | 租户管理 | ✅ |
| **index-service** | **8106** | ES全文搜索 | 🔄 已修复（从8095改为8106）|

### 发现服务 discovery (7个)
| 服务 | 端口 | 说明 |
|------|------|------|
| catalog-service | 8101 | 数据目录 |
| datamap-service | 8102 | 数据地图 |
| search-service | 8103 | 统一搜索 |
| insight-service | 8112 | 智能洞察 |
| metric-service | - | 共用资源 |
| model-service | - | 共用资源 |
| datasource-service | - | 共用资源 |

## 端口冲突修复历史

### V1.0 (修复前) - 8组冲突
- edams-asset(8082) vs edams-user(8082)
- sandbox(8084) vs edams-notification(8084)
- sla(8085) vs edams-workflow(8085)
- edams-analytics(8087) vs edams-collaboration(8087)
- quality-service(8085) vs edams-workflow(8085) ❌ **未修复**
- standard-service(8084) vs edams-notification(8084) ❌ **未修复**
- analytics-service(8089) vs edams-user(8089) ❌ **未修复**
- index-service(8095) vs edams-version(8095) ❌ **未修复**

### V2.0 (2026-04-12) - 全部修复
- quality-service: 8085 → **8098**
- standard-service: 8084 → **8099**
- analytics-service: 8089 → **8100**
- index-service: 8095 → **8106**

## 路由说明

所有服务通过 Spring Cloud Gateway 统一路由：

```
客户端请求: GET /api/v1/assets/1
    ↓
Gateway 路由: lb://edams-asset (Nacos服务发现)
    ↓
StripPrefix=1: 移除 /api/v1 前缀
    ↓
目标服务: localhost:8082/api/v1/assets/1
    ↓
Controller: @RequestMapping("/api/v1/assets")
```

**重要**: 所有服务的 @RequestMapping 必须以 `/api/v1` 开头！

## API路径规范

### 后端Controller规范
- 所有Controller必须使用 `@RequestMapping("/api/v1/...")` 
- 路径版本号必须为 `/v1`

### 前端API路径规范
- 所有API路径统一使用 `/api/v1/...` 格式
- 不允许使用 `/auth/api/v1/...` 等额外前缀

### 常见错误
❌ `/auth/api/v1/auth/login` - 多了 `/auth` 前缀
❌ `/api/governance/tasks` - 缺少 `/v1`
✅ `/api/v1/auth/login`
✅ `/api/v1/governance/tasks`
