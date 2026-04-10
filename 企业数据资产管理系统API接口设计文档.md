# 企业数据资产管理系统 API接口设计文档

**文档版本**：V1.0
**编制日期**：2026年4月10日
**编制人**：首席架构师
**关联文档**：《企业数据资产管理系统需求文档 V6.2》《企业数据资产管理系统技术架构文档 V1.5》

---

## 文档变更记录

| 版本 | 日期 | 编制人 | 变更说明 |
|------|------|--------|----------|
| V1.0 | 2026-04-10 | 首席架构师 | 初始版本，定义MVP核心接口 |

---

## 1 设计原则

### 1.1 RESTful API设计规范

| 规范 | 说明 |
|------|------|
| 资源命名 | 使用名词复数形式，如 /assets、/users |
| HTTP方法 | GET（查询）、POST（创建）、PUT（更新）、DELETE（删除） |
| 路径层级 | 最大3层，如 /assets/{id}/metadata |
| 版本控制 | URL路径方式，如 /api/v1/assets |
| 认证方式 | Bearer Token (JWT) |

### 1.2 接口响应规范

**成功响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "timestamp": 1704009600000,
  "traceId": "abc123def456"
}
```

**错误响应**：
```json
{
  "code": 10001,
  "message": "资源不存在",
  "error": "NOT_FOUND",
  "timestamp": 1704009600000,
  "traceId": "abc123def456"
}
```

### 1.3 错误码定义

| 错误码范围 | 含义 | 示例 |
|------------|------|------|
| 0 | 成功 | - |
| 10001-10099 | 认证授权错误 | 10001-Token无效 |
| 20001-20099 | 资源操作错误 | 20001-资源不存在 |
| 30001-30099 | 参数校验错误 | 30001-参数格式错误 |
| 40001-40099 | 业务逻辑错误 | 40001-状态不允许操作 |
| 50001-50099 | 系统内部错误 | 50001-服务暂不可用 |

---

## 2 认证模块 API

### 2.1 用户认证

#### POST /api/v1/auth/login
**功能**：用户登录

**请求参数**：
```json
{
  "username": "string",
  "password": "string",
  "captcha": "string",
  "captchaId": "string"
}
```

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "tokenType": "Bearer",
    "user": {
      "id": "u_10001",
      "username": "admin",
      "nickname": "系统管理员",
      "email": "admin@company.com",
      "roles": ["ROLE_ADMIN"]
    }
  }
}
```

---

#### POST /api/v1/auth/logout
**功能**：用户登出

---

#### POST /api/v1/auth/refresh
**功能**：刷新Token

---

#### POST /api/v1/auth/register
**功能**：用户注册

---

## 3 用户管理模块 API

### 3.1 用户CRUD

#### GET /api/v1/users
**功能**：查询用户列表

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认20 |
| keyword | string | 否 | 搜索关键词 |
| departmentId | string | 否 | 部门ID |
| status | int | 否 | 状态：0-禁用，1-启用 |

---

#### POST /api/v1/users
**功能**：创建用户

---

#### GET /api/v1/users/{id}
**功能**：获取用户详情

---

#### PUT /api/v1/users/{id}
**功能**：更新用户信息

---

#### DELETE /api/v1/users/{id}
**功能**：删除用户（软删除）

---

### 3.2 部门管理

#### GET /api/v1/departments
**功能**：获取部门树

---

#### POST /api/v1/departments
**功能**：创建部门

---

## 4 权限管理模块 API

### 4.1 角色管理

#### GET /api/v1/roles
**功能**：获取角色列表

---

#### POST /api/v1/roles
**功能**：创建角色

**请求参数**：
```json
{
  "name": "数据管理员",
  "code": "ROLE_DATA_ADMIN",
  "description": "负责数据资产管理",
  "permissions": [
    "asset:read",
    "asset:write",
    "asset:delete",
    "quality:read",
    "quality:write"
  ]
}
```

---

#### GET /api/v1/roles/{id}/permissions
**功能**：获取角色权限

---

#### PUT /api/v1/roles/{id}/permissions
**功能**：更新角色权限

---

### 4.2 权限分配

#### POST /api/v1/users/{userId}/roles
**功能**：分配用户角色

---

#### POST /api/v1/assets/{assetId}/permissions
**功能**：设置资产权限

---

## 5 资产管理模块 API

### 5.1 资产注册

#### POST /api/v1/assets
**功能**：注册数据资产

**请求参数**：
```json
{
  "name": "客户信息表",
  "alias": "cust_info",
  "type": "TABLE",
  "datasourceId": "ds_001",
  "database": "crm_db",
  "schema": "public",
  "tableName": "customer_info",
  "description": "客户基础信息表",
  "businessDomain": "CRM",
  "sensitivityLevel": "L3",
  "ownerId": "u_10001",
  "tags": ["客户", "核心"],
  "fields": [
    {
      "name": "customer_id",
      "type": "BIGINT",
      "description": "客户ID",
      "nullable": false,
      "primaryKey": true,
      "sensitivityLevel": "L4"
    },
    {
      "name": "customer_name",
      "type": "VARCHAR(100)",
      "description": "客户姓名",
      "nullable": false,
      "sensitivityLevel": "L3"
    }
  ]
}
```

---

#### POST /api/v1/assets/batch
**功能**：批量注册资产

---

### 5.2 资产查询

#### GET /api/v1/assets
**功能**：查询资产列表

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |
| keyword | string | 否 | 关键词搜索 |
| type | string | 否 | 资产类型：TABLE/FILE/API等 |
| sensitivityLevel | string | 否 | 敏感级别：L1-L4 |
| businessDomain | string | 否 | 业务域 |
| ownerId | string | 否 | 负责人 |
| tags | string | 否 | 标签（逗号分隔） |
| sortBy | string | 否 | 排序字段 |
| sortOrder | string | 否 | asc/desc |

---

#### GET /api/v1/assets/{id}
**功能**：获取资产详情

**响应**：
```json
{
  "code": 0,
  "data": {
    "assetId": "ast_100001",
    "name": "客户信息表",
    "alias": "cust_info",
    "type": "TABLE",
    "datasourceId": "ds_001",
    "datasourceName": "CRM数据库",
    "database": "crm_db",
    "schema": "public",
    "tableName": "customer_info",
    "description": "客户基础信息表",
    "businessDomain": "CRM",
    "sensitivityLevel": "L3",
    "owner": {
      "id": "u_10001",
      "name": "张三",
      "email": "zhangsan@company.com"
    },
    "tags": ["客户", "核心"],
    "fields": [...],
    "lineage": {...},
    "qualityReport": {...},
    "relatedAssets": [...],
    "statistics": {
      "rowCount": 1000000,
      "size": "2.5GB",
      "lastUpdateTime": "2026-04-10T00:00:00Z",
      "viewCount": 100,
      "favoriteCount": 10
    },
    "createTime": "2026-01-01T00:00:00Z",
    "updateTime": "2026-04-01T00:00:00Z"
  }
}
```

---

#### GET /api/v1/assets/{id}/metadata
**功能**：获取资产元数据

---

#### GET /api/v1/assets/{id}/fields
**功能**：获取资产字段列表

---

#### PUT /api/v1/assets/{id}
**功能**：更新资产信息

---

#### DELETE /api/v1/assets/{id}
**功能**：删除资产（需权限）

---

### 5.3 资产搜索

#### GET /api/v1/assets/search
**功能**：全文搜索资产

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| q | string | 是 | 搜索关键词 |
| filters | string | 否 | 过滤条件JSON |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

**示例**：GET /api/v1/assets/search?q=客户&filters={"type":"TABLE","sensitivityLevel":"L3"}

---

#### GET /api/v1/assets/suggest
**功能**：搜索联想

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| q | string | 是 | 关键词 |
| limit | int | 否 | 返回数量，默认10 |

---

### 5.4 资产收藏与评价

#### POST /api/v1/assets/{id}/favorite
**功能**：收藏资产

---

#### DELETE /api/v1/assets/{id}/favorite
**功能**：取消收藏

---

#### GET /api/v1/users/me/favorites
**功能**：获取我的收藏列表

---

## 6 血缘管理模块 API

### 6.1 血缘查询

#### GET /api/v1/lineage/table/{assetId}
**功能**：获取表级血缘

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| direction | string | 否 | UPSTREAM/DOWNSTREAM/BOTH，默认BOTH |
| depth | int | 否 | 追溯深度，默认3 |

**响应**：
```json
{
  "code": 0,
  "data": {
    "nodes": [
      {
        "id": "ast_001",
        "name": "ods_customer",
        "type": "TABLE",
        "level": 1
      },
      {
        "id": "ast_002",
        "name": "dwd_customer",
        "type": "TABLE",
        "level": 2
      },
      {
        "id": "ast_003",
        "name": "dim_customer",
        "type": "TABLE",
        "level": 3
      }
    ],
    "edges": [
      {
        "source": "ast_001",
        "target": "ast_002",
        "transform": "SELECT * FROM ods_customer",
        "taskName": "ods_to_dwd"
      }
    ]
  }
}
```

---

#### GET /api/v1/lineage/field/{assetId}
**功能**：获取字段级血缘

---

#### GET /api/v1/lineage/path
**功能**：查询血缘路径

---

### 6.2 血缘影响分析

#### GET /api/v1/lineage/impact/{assetId}
**功能**：影响分析（下游影响）

---

#### GET /api/v1/lineage/dependency/{assetId}
**功能**：追溯分析（上游来源）

---

## 7 质量管理模块 API

### 7.1 质量规则

#### GET /api/v1/quality/rules
**功能**：获取质量规则列表

**响应**：
```json
{
  "code": 0,
  "data": [
    {
      "ruleId": "qr_001",
      "name": "字段非空检查",
      "type": "NOT_NULL",
      "targetType": "FIELD",
      "expression": "field IS NOT NULL",
      "severity": "HIGH",
      "enabled": true
    },
    {
      "ruleId": "qr_002",
      "name": "数据唯一性检查",
      "type": "UNIQUENESS",
      "targetType": "TABLE",
      "expression": "COUNT(DISTINCT id) = COUNT(*)",
      "severity": "HIGH",
      "enabled": true
    },
    {
      "ruleId": "qr_003",
      "name": "数值范围检查",
      "type": "RANGE",
      "targetType": "FIELD",
      "expression": "field >= 0 AND field <= 100",
      "severity": "MEDIUM",
      "enabled": true
    }
  ]
}
```

---

#### POST /api/v1/quality/rules
**功能**：创建质量规则

---

#### POST /api/v1/assets/{assetId}/rules
**功能**：为资产配置质量规则

---

### 7.2 质量检测

#### POST /api/v1/quality/check
**功能**：触发质量检测

**请求参数**：
```json
{
  "assetId": "ast_100001",
  "ruleIds": ["qr_001", "qr_002"],
  "triggerType": "MANUAL"
}
```

---

#### GET /api/v1/quality/check/{checkId}
**功能**：获取检测结果

---

#### GET /api/v1/assets/{assetId}/quality
**功能**：获取资产质量报告

**响应**：
```json
{
  "code": 0,
  "data": {
    "assetId": "ast_100001",
    "assetName": "客户信息表",
    "overallScore": 95,
    "trend": [
      {"date": "2026-04-01", "score": 92},
      {"date": "2026-04-02", "score": 94},
      {"date": "2026-04-03", "score": 95}
    ],
    "ruleResults": [
      {
        "ruleId": "qr_001",
        "ruleName": "字段非空检查",
        "status": "PASS",
        "passRate": "99.5%",
        "violationCount": 5000
      }
    ],
    "issueSummary": {
      "critical": 0,
      "high": 2,
      "medium": 5,
      "low": 10
    },
    "lastCheckTime": "2026-04-10T00:00:00Z"
  }
}
```

---

### 7.3 问题管理

#### GET /api/v1/quality/issues
**功能**：查询质量问题列表

---

#### PUT /api/v1/quality/issues/{id}
**功能**：更新问题状态

---

## 8 安全合规模块 API

### 8.1 敏感等级管理

#### GET /api/v1/security/levels
**功能**：获取敏感等级定义

**响应**：
```json
{
  "code": 0,
  "data": [
    {
      "level": "L1",
      "name": "公开",
      "color": "#00FF00",
      "description": "可对外公开的数据"
    },
    {
      "level": "L2",
      "name": "内部",
      "color": "#FFFF00",
      "description": "仅企业内部使用"
    },
    {
      "level": "L3",
      "name": "敏感",
      "color": "#FFA500",
      "description": "涉及用户隐私，需保护"
    },
    {
      "level": "L4",
      "name": "机密",
      "color": "#FF0000",
      "description": "核心机密，严格管控"
    }
  ]
}
```

---

#### PUT /api/v1/assets/{id}/sensitivity
**功能**：更新资产敏感等级

---

### 8.2 脱敏规则

#### GET /api/v1/security/masking/rules
**功能**：获取脱敏规则列表

---

#### POST /api/v1/security/masking/rules
**功能**：创建脱敏规则

---

#### POST /api/v1/assets/{id}/masking
**功能**：应用脱敏规则

---

## 9 数据源管理 API

### 9.1 数据源CRUD

#### GET /api/v1/datasources
**功能**：获取数据源列表

**响应**：
```json
{
  "code": 0,
  "data": [
    {
      "id": "ds_001",
      "name": "CRM数据库",
      "type": "MYSQL",
      "host": "crm-db.company.com",
      "port": 3306,
      "database": "crm_db",
      "status": "CONNECTED",
      "assetCount": 150,
      "lastSyncTime": "2026-04-10T00:00:00Z"
    }
  ]
}
```

---

#### POST /api/v1/datasources
**功能**：创建数据源

**请求参数**：
```json
{
  "name": "CRM数据库",
  "type": "MYSQL",
  "host": "crm-db.company.com",
  "port": 3306,
  "database": "crm_db",
  "username": "readonly_user",
  "password": "encrypted_password",
  "description": "CRM业务库"
}
```

---

#### POST /api/v1/datasources/{id}/test
**功能**：测试数据源连接

---

#### POST /api/v1/datasources/{id}/sync
**功能**：同步数据源元数据

---

## 10 统计分析 API

### 10.1 资产统计

#### GET /api/v1/statistics/overview
**功能**：获取资产总览统计

**响应**：
```json
{
  "code": 0,
  "data": {
    "totalAssets": 5000,
    "todayNewAssets": 10,
    "assetByType": {
      "TABLE": 3000,
      "FILE": 1000,
      "API": 500,
      "STREAM": 200
    },
    "assetByLevel": {
      "L1": 2000,
      "L2": 2000,
      "L3": 800,
      "L4": 200
    },
    "assetByDomain": {
      "CRM": 1500,
      "ERP": 1200,
      "SCM": 800
    },
    "qualityOverview": {
      "avgScore": 92,
      "totalIssues": 150
    },
    "lineageOverview": {
      "totalRelations": 10000,
      "avgDepth": 4.5
    }
  }
}
```

---

#### GET /api/v1/statistics/trend
**功能**：获取资产趋势统计

---

## 11 Webhook API

### 11.1 Webhook管理

#### GET /api/v1/webhooks
**功能**：获取Webhook列表

---

#### POST /api/v1/webhooks
**功能**：创建Webhook

**请求参数**：
```json
{
  "name": "资产变更通知",
  "url": "https://callback.company.com/asset",
  "events": ["ASSET_CREATED", "ASSET_UPDATED", "QUALITY_CHECK_FAILED"],
  "secret": "webhook_secret_key",
  "enabled": true
}
```

---

## 12 附录

### 12.1 分页参数默认值

| 参数 | 默认值 | 最大值 |
|------|--------|--------|
| page | 1 | - |
| pageSize | 20 | 100 |

### 12.2 HTTP状态码

| 状态码 | 含义 |
|--------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

**文档结束**
