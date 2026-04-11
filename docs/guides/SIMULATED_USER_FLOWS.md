# 企业数据资产管理系统 - 模拟用户操作指南

> 本文档展示系统部署后可执行的核心用户操作流程。

---

## 一、系统登录

### 1.1 访问系统

```
URL: http://localhost:8000 (前端)
     http://localhost:8888 (API网关)

默认账号:
- 管理员: admin / admin123
- 普通用户: user / user123
```

### 1.2 登录流程

```
┌────────────────────────────────────────────────────────────┐
│                         登录成功                            │
├────────────────────────────────────────────────────────────┤
│ {                                                         │
│   "code": 200,                                            │
│   "data": {                                               │
│     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",   │
│     "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",│
│     "user": {                                             │
│       "id": 1,                                            │
│       "username": "admin",                                │
│       "nickname": "系统管理员",                           │
│       "email": "admin@edams.com",                         │
│       "roles": ["ADMIN"],                                │
│       "permissions": ["*:*"]                             │
│     }                                                     │
│   }                                                       │
│ }                                                         │
└────────────────────────────────────────────────────────────┘
```

---

## 二、数据资产管理

### 2.1 首页仪表盘

```
GET /api/dashboard/stats
Authorization: Bearer {token}

响应:
{
  "assetTotal": 15234,
  "activeAssets": 12456,
  "catalogCount": 256,
  "qualityScore": 87.5,
  "recentAssets": [...],
  "pendingTasks": [...]
}
```

### 2.2 资产搜索

```
POST /api/assets/search
{
  "keyword": "customer",
  "type": "TABLE",
  "tags": ["PII", "核心资产"],
  "page": 1,
  "pageSize": 20
}

支持筛选:
- 资产类型: TABLE, VIEW, FILE, API, STREAM
- 分类分级: 公开, 内部, 敏感, 机密
- 质量状态: 优, 良, 中, 差
- 数据源: MySQL, PostgreSQL, Hive, Kafka
```

### 2.3 资产详情

```
GET /api/assets/{assetId}

响应:
{
  "id": "ast_001",
  "name": "customer_info",
  "displayName": "客户信息表",
  "type": "TABLE",
  "datasource": {
    "name": "MySQL业务库",
    "type": "MYSQL"
  },
  "schema": {
    "columns": [
      {"name": "id", "type": "BIGINT", "nullable": false},
      {"name": "name", "type": "VARCHAR(100)"},
      {"name": "phone", "type": "VARCHAR(20)"},
      {"name": "id_card", "type": "VARCHAR(18)"}
    ]
  },
  "classification": "SENSITIVE",
  "qualityScore": 92.5,
  "tags": ["PII", "客户数据"],
  "owner": {"id": 10, "name": "张三"},
  "lineage": {
    "upstream": [...],
    "downstream": [...]
  }
}
```

---

## 三、血缘关系管理

### 3.1 查看血缘图

```
GET /api/lineage/asset/{assetId}

{
  "nodes": [
    {"id": "ast_001", "name": "customer_info", "type": "TABLE"},
    {"id": "ast_002", "name": "customer_dim", "type": "DIMENSION"},
    {"id": "ast_003", "name": "report_sales", "type": "REPORT"}
  ],
  "edges": [
    {"source": "ast_001", "target": "ast_002", "type": "DERIVED_FROM"},
    {"source": "ast_002", "target": "ast_003", "type": "USES"}
  ]
}
```

### 3.2 影响分析

```
POST /api/lineage/impact
{
  "assetId": "ast_001",
  "direction": "DOWNSTREAM",
  "depth": 3
}

响应: 展示修改该资产会影响的所有下游资产
```

---

## 四、数据质量管理

### 4.1 创建质量规则

```
POST /api/quality/rules
{
  "name": "手机号完整性检查",
  "type": "PATTERN",
  "expression": "phone REGEXP '^1[3-9]\\d{9}$'",
  "severity": "HIGH",
  "dimensions": ["completeness", "validity"]
}
```

### 4.2 执行质量检测

```
POST /api/quality/check
{
  "ruleId": "rule_001",
  "assetIds": ["ast_001", "ast_002"]
}

异步执行，检测完成后会收到Kafka消息通知
```

### 4.3 查看质量报告

```
GET /api/quality/report/{assetId}

{
  "assetId": "ast_001",
  "overallScore": 87.5,
  "dimensions": {
    "completeness": 95.0,
    "uniqueness": 99.5,
    "validity": 85.0,
    "consistency": 88.0
  },
  "issues": [
    {"type": "INVALID_PHONE", "count": 156, "percentage": 2.3}
  ],
  "trend": [...],
  "suggestions": [...]
}
```

---

## 五、数据目录管理

### 5.1 浏览目录树

```
GET /api/catalog/tree

{
  "nodes": [
    {
      "id": "cat_001",
      "name": "客户域",
      "children": [
        {"id": "cat_001_01", "name": "客户信息"},
        {"id": "cat_001_02", "name": "客户行为"}
      ]
    },
    {
      "id": "cat_002",
      "name": "交易域",
      "children": [...]
    }
  ]
}
```

### 5.2 资产归类

```
POST /api/catalog/assets
{
  "assetIds": ["ast_001", "ast_002"],
  "catalogId": "cat_001_01"
}
```

---

## 六、权限管理

### 6.1 创建角色

```
POST /api/roles
{
  "name": "数据分析师",
  "code": "DATA_ANALYST",
  "description": "可以查看和分析数据资产",
  "permissions": [
    "asset:read",
    "catalog:read",
    "lineage:read",
    "quality:read"
  ]
}
```

### 6.2 分配角色

```
POST /api/users/{userId}/roles
{
  "roleIds": ["role_001", "role_002"]
}
```

---

## 七、工作流管理

### 7.1 发起数据申请

```
POST /api/workflow/apply
{
  "type": "DATA_ACCESS",
  "assetIds": ["ast_001"],
  "reason": "用于月度销售报表分析",
  "duration": 30,  // 天
  "approvers": ["user_002"]
}
```

### 7.2 审批流程

```
# 审批通过
POST /api/workflow/approve/{taskId}
{"action": "APPROVE", "comment": "同意"}

# 审批拒绝
POST /api/workflow/approve/{taskId}
{"action": "REJECT", "comment": "需要补充业务说明"}
```

---

## 八、移动端操作

### 8.1 Flutter App 启动

```bash
cd edams-mobile
flutter pub get
flutter run
```

### 8.2 主要功能

| 功能 | 说明 |
|------|------|
| 首页仪表盘 | 快速查看资产统计 |
| 资产搜索 | 扫码或关键字搜索 |
| 目录浏览 | 查看数据目录 |
| 血缘查看 | 查看上下游关系 |
| 消息通知 | 接收质量告警 |
| 个人设置 | 语言切换、主题设置 |

---

## 九、运维监控

### 9.1 查看监控面板

```
Grafana: http://localhost:3000
- 系统概览
- 服务健康状态
- API调用统计
- 数据库连接池
- Kafka消息延迟
```

### 9.2 查看链路追踪

```
Jaeger: http://localhost:16686
- 查看请求链路
- 分析接口耗时
- 定位性能瓶颈
```

---

## 十、系统配置

### 10.1 Nacos 配置中心

```
地址: http://localhost:8848/nacos
账号: nacos / nacos

可配置项:
- 数据库连接池
- Redis缓存策略
- Kafka主题配置
- 限流熔断规则
```

### 10.2 Sentinel 熔断规则

```
地址: http://localhost:8858
账号: sentinel / sentinel_dev_123

可配置:
- 接口限流阈值
- 熔断策略
- 热点参数限流
```
