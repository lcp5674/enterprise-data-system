# 企业数据资产管理系统 API接口设计文档

**文档版本**：V2.0
**编制日期**：2026年4月10日
**编制人**：首席架构师
**关联文档**：《企业数据资产管理系统需求文档 V7.0》《企业数据资产管理系统技术架构文档 V2.0》

---

## 文档变更记录

| 版本 | 日期 | 编制人 | 变更说明 |
|------|------|--------|----------|
| V1.0 | 2026-04-10 | 首席架构师 | 初始版本，定义MVP核心接口 |
| V2.0 | 2026-04-10 | 首席架构师 | 完整版本，覆盖全部20个功能域，400+接口 |

---

## 1 设计原则与规范

### 1.1 RESTful API设计规范

| 规范 | 说明 | 示例 |
|------|------|------|
| 资源命名 | 使用名词复数形式 | `/assets`、`/users` |
| HTTP方法 | GET（查询）、POST（创建）、PUT（更新）、PATCH（部分更新）、DELETE（删除） | - |
| 路径层级 | 最大4层，避免过度嵌套 | `/assets/{id}/fields/{fieldId}` |
| 版本控制 | URL路径方式 | `/api/v1/assets` |
| 认证方式 | Bearer Token (JWT) | `Authorization: Bearer <token>` |
| 路径规范 | kebab-case | `/data-quality`、`/asset-info` |
| 参数规范 | camelCase | `assetId`、`userName` |
| 批量操作 | POST /batch | `/assets/batch` |

### 1.2 接口响应规范

**成功响应（单条）**：
```json
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "timestamp": 1704009600000,
  "traceId": "abc123def456",
  "duration": 25
}
```

**成功响应（分页）**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [ ... ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1000,
      "totalPages": 50
    }
  },
  "timestamp": 1704009600000,
  "traceId": "abc123def456"
}
```

**批量操作响应**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 100,
    "successCount": 98,
    "failCount": 2,
    "results": [
      {"id": "xxx", "status": "SUCCESS"},
      {"id": "yyy", "status": "FAILED", "error": "Duplicate entry"}
    ]
  }
}
```

**错误响应**：
```json
{
  "code": 10001,
  "message": "Token无效或已过期",
  "error": "UNAUTHORIZED",
  "details": "请重新登录获取有效Token",
  "timestamp": 1704009600000,
  "traceId": "abc123def456"
}
```

### 1.3 错误码定义

| 错误码范围 | 含义 | 子错误码示例 |
|------------|------|--------------|
| 0 | 成功 | - |
| 10001-10099 | 认证授权错误 | 10001-Token无效, 10002-Token过期, 10003-无权限 |
| 10101-10199 | 访问控制错误 | 10101-IP受限, 10102-时间受限 |
| 20001-20099 | 资源操作错误 | 20001-资源不存在, 20002-资源已存在, 20003-资源被占用 |
| 20101-20199 | 资源状态错误 | 20101-状态不允许, 20102-资源已锁定 |
| 30001-30099 | 参数校验错误 | 30001-参数格式错误, 30002-必填参数缺失, 30003-参数值超出范围 |
| 40001-40099 | 业务逻辑错误 | 40001-状态不允许操作, 40002-前置条件不满足, 40003-业务规则冲突 |
| 40101-40199 | 流程审批错误 | 40101-审批未通过, 40102-审批超时 |
| 50001-50099 | 系统内部错误 | 50001-服务暂不可用, 50002-系统繁忙, 50003-数据异常 |
| 50101-50199 | 第三方服务错误 | 50101-外部服务超时, 50102-外部服务不可用 |

### 1.4 分页参数规范

| 参数 | 类型 | 必填 | 默认值 | 最大值 | 说明 |
|------|------|------|--------|--------|------|
| page | int | 否 | 1 | - | 页码，从1开始 |
| pageSize | int | 否 | 20 | 100 | 每页数量 |
| sortBy | string | 否 | - | - | 排序字段 |
| sortOrder | string | 否 | desc | - | asc/desc |

### 1.5 HTTP状态码

| 状态码 | 含义 | 使用场景 |
|--------|------|----------|
| 200 | OK | 成功返回 |
| 201 | Created | 资源创建成功 |
| 202 | Accepted | 异步任务已接受 |
| 204 | No Content | 删除成功，无返回内容 |
| 400 | Bad Request | 参数错误、业务逻辑错误 |
| 401 | Unauthorized | 未认证或Token无效 |
| 403 | Forbidden | 无权限访问 |
| 404 | Not Found | 资源不存在 |
| 409 | Conflict | 资源冲突，如重复创建 |
| 422 | Unprocessable Entity | 语义错误，无法处理 |
| 429 | Too Many Requests | 请求过于频繁 |
| 500 | Internal Server Error | 服务器内部错误 |
| 502 | Bad Gateway | 网关错误 |
| 503 | Service Unavailable | 服务暂不可用 |
| 504 | Gateway Timeout | 网关超时 |

### 1.6 请求头规范

| 请求头 | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | Bearer Token |
| Content-Type | 是 | application/json |
| Accept | 否 | application/json |
| X-Request-Id | 否 | 请求唯一ID |
| X-Language | 否 | zh-CN/en-US |
| X-Tenant-Id | 否 | 租户ID（多租户场景） |

---

## 2 认证模块 API (Auth)

### 2.1 用户认证

#### POST /api/v1/auth/login
**功能**：用户登录
**功能说明**：支持用户名密码、手机验证码、企业微信、钉钉、LDAP等多种登录方式

**请求参数**：
```json
{
  "loginType": "PASSWORD",  // PASSWORD/MOBILE_CODE/ENTERPRISE_WECHAT/DINGTALK/LDAP/OAUTH2
  "username": "string",
  "password": "string",     // 密码登录时必填，AES加密传输
  "mobile": "string",        // 手机验证码登录时必填
  "captcha": "string",       // 图形验证码
  "captchaId": "string",     // 图形验证码ID
  "loginSource": "WEB",      // WEB/APP/API
  "deviceId": "string",      // 设备ID
  "deviceInfo": {            // 设备信息
    "os": "iOS 17",
    "appVersion": "1.0.0",
    "ip": "192.168.1.1"
  }
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
    "refreshExpiresIn": 604800,
    "tokenType": "Bearer",
    "user": {
      "id": "u_10001",
      "username": "admin",
      "nickname": "系统管理员",
      "email": "admin@company.com",
      "phone": "138****1234",
      "avatar": "https://xxx.com/avatar.png",
      "department": {
        "id": "dept_001",
        "name": "技术部",
        "path": "/集团/研发中心/技术部"
      },
      "roles": [
        {"id": "role_001", "code": "ROLE_ADMIN", "name": "系统管理员"}
      ],
      "permissions": ["asset:*", "user:*", "system:*"],
      "lastLoginTime": "2026-04-10T08:00:00Z",
      "lastLoginIp": "192.168.1.1"
    },
    "menus": [
      {"id": "menu_001", "name": "首页", "path": "/home", "icon": "home"},
      {"id": "menu_002", "name": "数据资产", "path": "/assets", "children": [...]}
    ],
    "config": {
      "theme": "light",
      "language": "zh-CN",
      "timezone": "Asia/Shanghai"
    }
  }
}
```

---

#### POST /api/v1/auth/logout
**功能**：用户登出
**功能说明**：使当前Token失效，清理会话信息

**请求头**：
```
Authorization: Bearer <token>
```

---

#### POST /api/v1/auth/refresh
**功能**：刷新Token
**功能说明**：使用RefreshToken获取新的AccessToken

**请求参数**：
```json
{
  "refreshToken": "string"
}
```

---

#### POST /api/v1/auth/mobile/send-code
**功能**：发送手机验证码
**功能说明**：发送登录验证码，支持图形验证码校验

**请求参数**：
```json
{
  "mobile": "string",
  "captcha": "string",
  "captchaId": "string",
  "scene": "LOGIN"  // LOGIN/REGISTER/RESET_PASSWORD/BIND_MOBILE
}
```

---

#### POST /api/v1/auth/mobile/verify
**功能**：手机号验证登录
**功能说明**：通过手机验证码直接登录

**请求参数**：
```json
{
  "mobile": "string",
  "code": "string"
}
```

---

#### POST /api/v1/auth/register
**功能**：用户注册
**功能说明**：支持邮箱注册和企业邮箱自动关联

**请求参数**：
```json
{
  "username": "string",
  "password": "string",
  "confirmPassword": "string",
  "email": "string",
  "mobile": "string",
  "nickname": "string",
  "departmentId": "string",
  "inviteCode": "string",
  "agreeTerms": true
}
```

---

#### POST /api/v1/auth/password/reset
**功能**：重置密码
**功能说明**：通过邮箱或手机验证码重置密码

**请求参数**：
```json
{
  "verifyType": "EMAIL",  // EMAIL/MOBILE
  "verifyCode": "string",
  "newPassword": "string",
  "confirmPassword": "string"
}
```

---

#### POST /api/v1/auth/password/change
**功能**：修改密码
**功能说明**：修改当前登录用户的密码

**请求参数**：
```json
{
  "oldPassword": "string",
  "newPassword": "string",
  "confirmPassword": "string"
}
```

---

#### GET /api/v1/auth/captcha
**功能**：获取图形验证码
**响应**：返回验证码图片Base64编码

---

#### GET /api/v1/auth/sso/url
**功能**：获取SSO登录URL
**功能说明**：获取OAuth2/OIDC的单点登录跳转URL

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| provider | string | 是 | 企业微信:wxwork, 钉钉:dingtalk, LDAP:ldap |
| redirectUri | string | 是 | 回调地址 |

---

#### POST /api/v1/auth/sso/callback
**功能**：SSO回调处理
**功能说明**：处理第三方SSO的回调请求

---

#### GET /api/v1/auth/session
**功能**：获取当前会话信息
**功能说明**：获取当前登录用户的会话详情

---

#### DELETE /api/v1/auth/session/{sessionId}
**功能**：销毁指定会话
**功能说明**：强制下线指定设备或会话

---

#### GET /api/v1/auth/mfa/status
**功能**：获取MFA状态
**功能说明**：获取当前用户的双因素认证状态

---

#### POST /api/v1/auth/mfa/enable
**功能**：启用MFA
**功能说明**：启用双因素认证，返回TOTP密钥

**响应**：
```json
{
  "code": 0,
  "data": {
    "secret": "BASE32ENCODEDSECRET",
    "qrCode": "data:image/png;base64,...",
    "backupCodes": ["code1", "code2", "code3", "code4", "code5"]
  }
}
```

---

#### POST /api/v1/auth/mfa/verify
**功能**：验证MFA码
**功能说明**：验证TOTP验证码，绑定MFA设备

**请求参数**：
```json
{
  "code": "string"
}
```

---

#### POST /api/v1/auth/mfa/disable
**功能**：禁用MFA
**功能说明**：禁用双因素认证

**请求参数**：
```json
{
  "password": "string",
  "code": "string"
}
```

---

## 3 用户管理模块 API (User)

### 3.1 用户CRUD

#### GET /api/v1/users
**功能**：查询用户列表
**功能说明**：支持多条件筛选、分页查询

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认20 |
| keyword | string | 否 | 搜索关键词（用户名/昵称/邮箱） |
| departmentId | string | 否 | 部门ID |
| roleId | string | 否 | 角色ID |
| status | int | 否 | 状态：0-禁用，1-启用 |
| userType | int | 否 | 用户类型：1-内部，2-外部 |
| createdTimeStart | datetime | 否 | 创建时间开始 |
| createdTimeEnd | datetime | 否 | 创建时间结束 |
| sortBy | string | 否 | 排序字段 |
| sortOrder | string | 否 | asc/desc |

**响应**：
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": "u_10001",
        "username": "admin",
        "nickname": "系统管理员",
        "email": "admin@company.com",
        "phone": "138****1234",
        "employeeNo": "EMP001",
        "avatar": "https://xxx.com/avatar.png",
        "department": {
          "id": "dept_001",
          "name": "技术部",
          "path": "/集团/研发中心/技术部"
        },
        "roles": [{"id": "role_001", "name": "系统管理员"}],
        "status": 1,
        "userType": 1,
        "lastLoginTime": "2026-04-10T08:00:00Z",
        "createdTime": "2026-01-01T00:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 1000,
      "totalPages": 50
    }
  }
}
```

---

#### POST /api/v1/users
**功能**：创建用户
**功能说明**：管理员创建新用户

**请求参数**：
```json
{
  "username": "string",
  "nickname": "string",
  "email": "string",
  "phone": "string",
  "employeeNo": "string",
  "departmentId": "string",
  "roleIds": ["role_001", "role_002"],
  "userType": 1,
  "password": "string",
  "sendNotify": true,
  "effectiveTime": "2026-04-10T00:00:00Z",
  "expireTime": "2027-04-10T00:00:00Z",
  "remark": "string"
}
```

---

#### GET /api/v1/users/{id}
**功能**：获取用户详情
**功能说明**：获取指定用户的完整信息

---

#### PUT /api/v1/users/{id}
**功能**：更新用户信息
**功能说明**：更新用户的基本信息

**请求参数**：
```json
{
  "nickname": "string",
  "email": "string",
  "phone": "string",
  "avatar": "string",
  "departmentId": "string",
  "roleIds": ["role_001"],
  "effectiveTime": "datetime",
  "expireTime": "datetime",
  "remark": "string"
}
```

---

#### DELETE /api/v1/users/{id}
**功能**：删除用户（软删除）
**功能说明**：将用户标记为已删除，不物理删除数据

---

#### POST /api/v1/users/batch
**功能**：批量创建用户
**功能说明**：通过Excel或JSON批量导入用户

**请求参数**：
```json
{
  "users": [
    {
      "username": "user001",
      "nickname": "用户001",
      "email": "user001@company.com",
      "departmentId": "dept_001",
      "roleIds": ["role_002"]
    }
  ],
  "notifyByEmail": true,
  "defaultPassword": "string"
}
```

---

#### POST /api/v1/users/{id}/enable
**功能**：启用用户
**功能说明**：恢复已禁用的用户

---

#### POST /api/v1/users/{id}/disable
**功能**：禁用用户
**功能说明**：禁用用户账户，禁止登录

**请求参数**：
```json
{
  "reason": "string",
  "disableType": "TEMP",  // TEMP-临时禁用, PERM-永久禁用
  "expireTime": "datetime"
}
```

---

#### POST /api/v1/users/{id}/reset-password
**功能**：重置用户密码
**功能说明**：管理员强制重置用户密码

**请求参数**：
```json
{
  "newPassword": "string",
  "forceChangeOnLogin": true,
  "notifyUser": true
}
```

---

#### POST /api/v1/users/{id}/roles
**功能**：分配用户角色
**功能说明**：为用户分配角色，替换现有角色

**请求参数**：
```json
{
  "roleIds": ["role_001", "role_002"]
}
```

---

#### GET /api/v1/users/{id}/permissions
**功能**：获取用户权限列表
**功能说明**：获取用户拥有的所有有效权限（含继承）

---

#### GET /api/v1/users/{id}/activities
**功能**：获取用户活动记录
**功能说明**：获取用户的操作日志和登录记录

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| activityType | string | 否 | ACTIVITY-操作, LOGIN-登录 |
| startTime | datetime | 否 | 开始时间 |
| endTime | datetime | 否 | 结束时间 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

#### POST /api/v1/users/{id}/unlock
**功能**：解锁用户
**功能说明**：解锁因连续登录失败被锁定的账户

---

### 3.2 部门管理

#### GET /api/v1/departments
**功能**：获取部门树
**功能说明**：获取完整的组织架构树

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| includeDisabled | boolean | 否 | 是否包含已禁用部门 |
| includeMembers | boolean | 否 | 是否包含成员数量 |

**响应**：
```json
{
  "code": 0,
  "data": [
    {
      "id": "dept_001",
      "name": "集团",
      "parentId": null,
      "level": 1,
      "path": "/集团",
      "sortOrder": 1,
      "leader": {
        "id": "u_001",
        "name": "张三"
      },
      "memberCount": 100,
      "children": [
        {
          "id": "dept_002",
          "name": "研发中心",
          "parentId": "dept_001",
          "level": 2,
          "path": "/集团/研发中心",
          "memberCount": 50,
          "children": [...]
        }
      ]
    }
  ]
}
```

---

#### POST /api/v1/departments
**功能**：创建部门
**功能说明**：在指定父部门下创建子部门

**请求参数**：
```json
{
  "name": "string",
  "parentId": "string",
  "leaderId": "string",
  "sortOrder": 1,
  "contactEmail": "string",
  "contactPhone": "string",
  "description": "string"
}
```

---

#### GET /api/v1/departments/{id}
**功能**：获取部门详情
**功能说明**：获取指定部门的详细信息

---

#### PUT /api/v1/departments/{id}
**功能**：更新部门信息
**功能说明**：更新部门的基本信息和负责人

**请求参数**：
```json
{
  "name": "string",
  "parentId": "string",
  "leaderId": "string",
  "sortOrder": 1,
  "contactEmail": "string",
  "contactPhone": "string",
  "description": "string"
}
```

---

#### DELETE /api/v1/departments/{id}
**功能**：删除部门
**功能说明**：删除部门，需先转移或清空成员

**请求参数**：
```json
{
  "transferMemberTo": "dept_id",  // 成员转移目标部门
  "transferOwnerTo": "user_id"    // 资产负责人转移目标
}
```

---

#### GET /api/v1/departments/{id}/users
**功能**：获取部门用户列表
**功能说明**：获取指定部门下的所有用户

---

#### GET /api/v1/departments/{id}/tree
**功能**：获取部门子树
**功能说明**：获取指定部门及其所有子部门

---

#### GET /api/v1/departments/{id}/assets
**功能**：获取部门资产统计
**功能说明**：获取指定部门负责的资产统计信息

---

### 3.3 用户配置

#### GET /api/v1/users/me/profile
**功能**：获取当前用户资料
**功能说明**：获取登录用户的完整资料

---

#### PUT /api/v1/users/me/profile
**功能**：更新当前用户资料
**功能说明**：更新登录用户的个人资料

**请求参数**：
```json
{
  "nickname": "string",
  "avatar": "string",
  "phone": "string",
  "email": "string"
}
```

---

#### GET /api/v1/users/me/preferences
**功能**：获取用户偏好设置
**功能说明**：获取用户的工作台配置、通知偏好等

---

#### PUT /api/v1/users/me/preferences
**功能**：更新用户偏好设置
**功能说明**：更新用户的个人偏好配置

**请求参数**：
```json
{
  "theme": "light",           // light/dark
  "language": "zh-CN",       // zh-CN/en-US
  "timezone": "Asia/Shanghai",
  "homePage": "/home",
  "pageSize": 20,
  "dateFormat": "YYYY-MM-DD",
  "timeFormat": "HH:mm:ss",
  "notification": {
    "email": true,
    "inApp": true,
    "push": true,
    "types": ["QUALITY_ISSUE", "TASK_ASSIGNED", "APPROVAL"]
  },
  "shortcuts": [
    {"key": "G A", "action": "goto_assets"},
    {"key": "G H", "action": "goto_home"}
  ]
}
```

---

#### GET /api/v1/users/me/workbench
**功能**：获取工作台数据
**功能说明**：获取用户工作台展示的统计数据和快捷入口

---

#### GET /api/v1/users/me/recent
**功能**：获取最近访问记录
**功能说明**：获取用户最近访问的资产列表

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 否 | recent_assets-资产, recent_searches-搜索 |
| limit | int | 否 | 返回数量，默认10 |

---

## 4 权限管理模块 API (Permission)

### 4.1 角色管理

#### GET /api/v1/roles
**功能**：获取角色列表
**功能说明**：获取所有角色，支持筛选

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleType | int | 否 | 角色类型：1-系统角色，2-业务角色 |
| status | int | 否 | 状态：0-禁用，1-启用 |
| keyword | string | 否 | 搜索关键词 |

---

#### POST /api/v1/roles
**功能**：创建角色
**功能说明**：创建新角色并分配权限

**请求参数**：
```json
{
  "name": "string",
  "code": "string",
  "description": "string",
  "roleType": 1,
  "permissions": [
    "asset:read",
    "asset:write",
    "asset:delete",
    "quality:read",
    "quality:write"
  ],
  "isSystem": false
}
```

---

#### GET /api/v1/roles/{id}
**功能**：获取角色详情
**功能说明**：获取角色的完整信息，包括权限列表

---

#### PUT /api/v1/roles/{id}
**功能**：更新角色
**功能说明**：更新角色信息和权限

---

#### DELETE /api/v1/roles/{id}
**功能**：删除角色
**功能说明**：删除角色，需先检查是否有用户使用

---

#### GET /api/v1/roles/{id}/permissions
**功能**：获取角色权限
**功能说明**：获取角色的所有权限列表

---

#### PUT /api/v1/roles/{id}/permissions
**功能**：更新角色权限
**功能说明**：批量更新角色的权限

**请求参数**：
```json
{
  "permissions": [
    "asset:read",
    "asset:write",
    "asset:delete"
  ],
  "append": false  // true-追加, false-替换
}
```

---

#### GET /api/v1/roles/{id}/users
**功能**：获取角色用户列表
**功能说明**：获取拥有该角色的所有用户

---

#### GET /api/v1/roles/tree
**功能**：获取角色树
**功能说明**：获取按角色类型分组的角色树

---

### 4.2 权限配置

#### GET /api/v1/permissions
**功能**：获取权限列表
**功能说明**：获取所有权限定义，按模块分组

**响应**：
```json
{
  "code": 0,
  "data": [
    {
      "module": "资产",
      "moduleCode": "asset",
      "permissions": [
        {"code": "asset:read", "name": "查看资产", "type": "BUTTON"},
        {"code": "asset:write", "name": "编辑资产", "type": "BUTTON"},
        {"code": "asset:delete", "name": "删除资产", "type": "BUTTON"},
        {"code": "asset:export", "name": "导出资产", "type": "BUTTON"}
      ]
    },
    {
      "module": "质量",
      "moduleCode": "quality",
      "permissions": [...]
    }
  ]
}
```

---

#### GET /api/v1/permissions/tree
**功能**：获取权限树
**功能说明**：获取完整的菜单和按钮权限树

---

### 4.3 数据权限

#### GET /api/v1/data-permissions
**功能**：获取数据权限列表
**功能说明**：获取数据级权限配置

---

#### POST /api/v1/assets/{assetId}/permissions
**功能**：设置资产权限
**功能说明**：为资产配置用户或角色的访问权限

**请求参数**：
```json
{
  "permissions": [
    {
      "granteeType": "USER",  // USER/ROLE/DEPARTMENT
      "granteeId": "u_10001",
      "permissionType": "READ",  // READ/WRITE/ADMIN
      "expireTime": "datetime",
      "conditions": [
        {"field": "region", "operator": "IN", "values": ["北京", "上海"]}
      ]
    }
  ]
}
```

---

#### GET /api/v1/assets/{assetId}/permissions
**功能**：获取资产权限列表
**功能说明**：获取资产的完整权限配置

---

#### DELETE /api/v1/assets/{assetId}/permissions/{permissionId}
**功能**：删除资产权限
**功能说明**：移除资产的指定权限配置

---

#### POST /api/v1/assets/{assetId}/permissions/check
**功能**：检查资产访问权限
**功能说明**：检查当前用户对资产的访问权限

---

### 4.4 权限申请

#### GET /api/v1/permission-requests
**功能**：获取权限申请列表
**功能说明**：获取权限申请单列表（管理员视角）

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | PENDING-APPROVED-REJECTED |
| requestType | string | 否 | FUNC-功能权限, DATA-数据权限 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

#### POST /api/v1/permission-requests
**功能**：提交权限申请
**功能说明**：用户申请功能或数据权限

**请求参数**：
```json
{
  "requestType": "DATA",  // FUNC/DATA
  "resourceType": "ASSET",  // ASSET/DATABASE/TABLE
  "resourceId": "ast_001",
  "permissionType": "READ",
  "reason": "业务分析需要访问客户数据",
  "expireTime": "2026-05-10T00:00:00Z",
  "urgency": "NORMAL"  // LOW/NORMAL/HIGH/URGENT
}
```

---

#### GET /api/v1/permission-requests/{id}
**功能**：获取权限申请详情
**功能说明**：获取权限申请的详细信息

---

#### POST /api/v1/permission-requests/{id}/approve
**功能**：审批通过权限申请
**功能说明**：审批通过权限申请单

**请求参数**：
```json
{
  "comment": "同意",
  "validDays": 30,
  "autoRevoke": true
}
```

---

#### POST /api/v1/permission-requests/{id}/reject
**功能**：驳回权限申请
**功能说明**：驳回权限申请单

**请求参数**：
```json
{
  "comment": "需要部门负责人审批"
}
```

---

#### GET /api/v1/users/me/permission-requests
**功能**：获取我的权限申请
**功能说明**：获取当前用户提交的权限申请列表

---

### 4.5 权限审计

#### GET /api/v1/permissions/audit/logs
**功能**：获取权限变更日志
**功能说明**：获取权限分配和变更的历史记录

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | string | 否 | USER/ROLE |
| targetId | string | 否 | 用户或角色ID |
| operatorType | string | 否 | GRANT-授权, REVOKE-回收, MODIFY-修改 |
| startTime | datetime | 否 | 开始时间 |
| endTime | datetime | 否 | 结束时间 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

#### GET /api/v1/permissions/audit/report
**功能**：生成权限审计报告
**功能说明**：生成权限分配和使用情况报告

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reportType | string | 是 | SUMMARY-汇总, DETAIL-明细 |
| startTime | datetime | 是 | 开始时间 |
| endTime | datetime | 是 | 结束时间 |

---

## 5 资产管理模块 API (Asset)

### 5.1 资产注册

#### POST /api/v1/assets
**功能**：注册数据资产
**功能说明**：创建新的数据资产条目

**请求参数**：
```json
{
  "name": "客户信息表",
  "alias": "cust_info",
  "assetType": "TABLE",  // TABLE/VIEW/FILE/API/STREAM/MODEL/REPORT/DASHBOARD
  "datasourceId": "ds_001",
  "database": "crm_db",
  "schema": "public",
  "tableName": "customer_info",
  "description": "客户基础信息表",
  "businessDomain": "CRM",
  "businessDomainPath": "/客户域/客户管理",
  "sensitivityLevel": "L3",
  "ownerId": "u_10001",
  "ownerDepartmentId": "dept_001",
  "tags": ["客户", "核心"],
  "certificationLevel": "GOLD",  // BRONZE/SILVER/GOLD
  "retentionDays": 365,
  "encryptionRequired": true,
  "fields": [
    {
      "name": "customer_id",
      "alias": "客户ID",
      "dataType": "BIGINT",
      "comment": "客户唯一标识",
      "description": "系统生成的客户唯一ID",
      "nullable": false,
      "primaryKey": true,
      "defaultValue": null,
      "maxLength": 20,
      "sensitivityLevel": "L4",
      "businessMeanings": ["客户标识"],
      "enumValues": null,
      "sampleValues": ["10001", "10002"],
      "fieldOrder": 1
    },
    {
      "name": "customer_name",
      "alias": "客户名称",
      "dataType": "VARCHAR(100)",
      "comment": "客户姓名",
      "description": "客户全称",
      "nullable": false,
      "primaryKey": false,
      "defaultValue": null,
      "maxLength": 100,
      "sensitivityLevel": "L3",
      "businessMeanings": ["客户名称"],
      "enumValues": null,
      "sampleValues": ["张三", "李四"],
      "fieldOrder": 2
    }
  ],
  "technicalConfig": {
    "partitioned": true,
    "partitionKeys": ["dt"],
    "indexes": ["idx_customer_name"],
    "compression": "SNAPPY",
    "fileFormat": "ORC"
  },
  "scheduleConfig": {
    "scheduled": true,
    "cronExpression": "0 0 2 * * ?",
    "taskName": "CRM_CUST_DAILY",
    "scheduleType": "BATCH"
  },
  "businessConfig": {
    "dataUpdateFrequency": "每日",
    "dataVolume": "1000万条/天",
    "slA承诺": "T+1"
  }
}
```

---

#### POST /api/v1/assets/batch
**功能**：批量注册资产
**功能说明**：通过Excel或JSON批量导入资产

**请求参数**：
```json
{
  "assets": [...],
  "options": {
    "updateExisting": true,    // 是否更新已存在资产
    "skipValidation": false,   // 是否跳过验证
    "asyncProcess": true       // 是否异步处理
  }
}
```

---

#### GET /api/v1/assets/import/template
**功能**：下载资产导入模板
**功能说明**：下载Excel导入模板

---

#### POST /api/v1/assets/import
**功能**：上传资产导入文件
**功能说明**：上传资产导入文件，返回导入任务ID

**请求参数**：multipart/form-data
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | Excel文件 |
| updateExisting | boolean | 否 | 是否更新已存在 |
| asyncProcess | boolean | 否 | 是否异步 |

---

#### GET /api/v1/assets/import/{taskId}
**功能**：获取导入任务状态
**功能说明**：查询批量导入任务的执行状态

---

#### GET /api/v1/assets/import/{taskId}/errors
**功能**：获取导入错误详情
**功能说明**：获取导入过程中的错误记录

---

### 5.2 资产查询

#### GET /api/v1/assets
**功能**：查询资产列表
**功能说明**：支持多条件筛选、分页查询

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |
| keyword | string | 否 | 关键词搜索（名称/别名/描述） |
| assetType | string | 否 | 资产类型 |
| businessDomain | string | 否 | 业务域 |
| sensitivityLevel | string | 否 | 敏感级别 |
| ownerId | string | 否 | 负责人 |
| datasourceId | string | 否 | 数据源 |
| status | string | 否 | 状态 |
| tags | string | 否 | 标签（逗号分隔） |
| certificationLevel | string | 否 | 认证等级 |
| createdTimeStart | datetime | 否 | 创建时间开始 |
| createdTimeEnd | datetime | 否 | 创建时间结束 |
| updatedTimeStart | datetime | 否 | 更新时间开始 |
| updatedTimeEnd | datetime | 否 | 更新时间结束 |
| sortBy | string | 否 | 排序字段 |
| sortOrder | string | 否 | asc/desc |

**响应**：
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": "ast_100001",
        "name": "客户信息表",
        "alias": "cust_info",
        "assetType": "TABLE",
        "description": "客户基础信息表",
        "businessDomain": "CRM",
        "sensitivityLevel": "L3",
        "sensitivityLevelName": "敏感",
        "owner": {
          "id": "u_10001",
          "name": "张三"
        },
        "tags": ["客户", "核心"],
        "certificationLevel": "GOLD",
        "status": "APPROVED",
        "statistics": {
          "rowCount": 10000000,
          "storageSize": "2.5GB",
          "viewCount": 100,
          "favoriteCount": 10,
          "downloadedCount": 50
        },
        "qualityScore": 95,
        "lineageCount": 20,
        "lastAccessTime": "2026-04-10T00:00:00Z",
        "createdTime": "2026-01-01T00:00:00Z",
        "updatedTime": "2026-04-01T00:00:00Z"
      }
    ],
    "pagination": {...}
  }
}
```

---

#### GET /api/v1/assets/{id}
**功能**：获取资产详情
**功能说明**：获取资产的完整信息

**响应**：
```json
{
  "code": 0,
  "data": {
    "id": "ast_100001",
    "guid": "guid-xxx-xxx",
    "name": "客户信息表",
    "alias": "cust_info",
    "assetType": "TABLE",
    "datasource": {
      "id": "ds_001",
      "name": "CRM数据库",
      "type": "MYSQL"
    },
    "database": "crm_db",
    "schema": "public",
    "tableName": "customer_info",
    "description": "客户基础信息表",
    "businessDomain": "CRM",
    "businessDomainPath": "/客户域/客户管理",
    "sensitivityLevel": "L3",
    "sensitivityLevelName": "敏感",
    "owner": {
      "id": "u_10001",
      "name": "张三",
      "email": "zhangsan@company.com",
      "department": "技术部"
    },
    "certifiedUser": {
      "id": "u_10002",
      "name": "李四"
    },
    "certificationTime": "2026-03-01T00:00:00Z",
    "certificationLevel": "GOLD",
    "status": "APPROVED",
    "tags": ["客户", "核心"],
    "fields": [
      {
        "id": "fld_001",
        "name": "customer_id",
        "alias": "客户ID",
        "dataType": "BIGINT",
        "comment": "客户唯一标识",
        "description": "系统生成的客户唯一ID",
        "nullable": false,
        "primaryKey": true,
        "sensitivityLevel": "L4",
        "isCertified": true,
        "businessMeanings": ["客户标识"],
        "sampleValues": ["10001", "10002"]
      }
    ],
    "lineage": {
      "upstreamCount": 5,
      "downstreamCount": 15,
      "hasLineage": true
    },
    "quality": {
      "score": 95,
      "trend": "UP",
      "lastCheckTime": "2026-04-10T00:00:00Z"
    },
    "statistics": {
      "rowCount": 10000000,
      "storageSize": "2.5GB",
      "storageFormat": "ORC",
      "compression": "SNAPPY",
      "partitioned": true,
      "partitionKeys": ["dt"],
      "viewCount": 100,
      "favoriteCount": 10,
      "downloadedCount": 50,
      "lastAccessTime": "2026-04-10T00:00:00Z",
      "lastUpdateTime": "2026-04-10T02:00:00Z"
    },
    "relatedAssets": [
      {"id": "ast_001", "name": "ODS客户表", "type": "TABLE", "relation": "上游"},
      {"id": "ast_002", "name": "DW客户宽表", "type": "TABLE", "relation": "下游"}
    ],
    "documents": [
      {"id": "doc_001", "name": "数据字典.docx", "type": "DICTIONARY"}
    ],
    "schedules": [
      {"taskName": "CRM_CUST_DAILY", "cronExpression": "0 0 2 * * ?", "status": "ACTIVE"}
    ],
    "createdBy": "u_10001",
    "createdTime": "2026-01-01T00:00:00Z",
    "updatedBy": "u_10001",
    "updatedTime": "2026-04-01T00:00:00Z"
  }
}
```

---

#### GET /api/v1/assets/{id}/metadata
**功能**：获取资产元数据
**功能说明**：获取资产的详细技术元数据

---

#### GET /api/v1/assets/{id}/fields
**功能**：获取资产字段列表
**功能说明**：获取资产的所有字段定义

---

#### POST /api/v1/assets/{id}/fields
**功能**：添加资产字段
**功能说明**：为资产添加新的字段定义

**请求参数**：
```json
{
  "name": "string",
  "alias": "string",
  "dataType": "string",
  "comment": "string",
  "description": "string",
  "nullable": true,
  "primaryKey": false,
  "defaultValue": "string",
  "maxLength": 100,
  "sensitivityLevel": "L2",
  "fieldOrder": 10
}
```

---

#### PUT /api/v1/assets/{id}/fields/{fieldId}
**功能**：更新资产字段
**功能说明**：更新字段定义

---

#### DELETE /api/v1/assets/{id}/fields/{fieldId}
**功能**：删除资产字段
**功能说明**：删除字段定义

---

#### PUT /api/v1/assets/{id}
**功能**：更新资产信息
**功能说明**：更新资产的基本信息和配置

**请求参数**：
```json
{
  "name": "string",
  "alias": "string",
  "description": "string",
  "businessDomain": "string",
  "sensitivityLevel": "L2",
  "ownerId": "string",
  "tags": ["标签1", "标签2"],
  "retentionDays": 365,
  "encryptionRequired": true,
  "technicalConfig": {...},
  "businessConfig": {...}
}
```

---

#### DELETE /api/v1/assets/{id}
**功能**：删除资产（需权限）
**功能说明**：删除资产，支持软删除和硬删除

**请求参数**：
```json
{
  "deleteType": "SOFT",  // SOFT-软删除, HARD-硬删除
  "reason": "string"
}
```

---

#### POST /api/v1/assets/{id}/restore
**功能**：恢复已删除资产
**功能说明**：恢复软删除的资产

---

### 5.3 资产搜索

#### GET /api/v1/assets/search
**功能**：全文搜索资产
**功能说明**：基于Elasticsearch的全文搜索

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| q | string | 是 | 搜索关键词 |
| filters | string | 否 | 过滤条件JSON |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |
| highlight | boolean | 否 | 是否高亮 |
| fuzzy | boolean | 否 | 是否模糊匹配 |

**示例**：`GET /api/v1/assets/search?q=客户&filters={"assetType":"TABLE","sensitivityLevel":"L3"}`

---

#### GET /api/v1/assets/suggest
**功能**：搜索联想
**功能说明**：获取搜索建议

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| q | string | 是 | 关键词 |
| limit | int | 否 | 返回数量，默认10 |
| type | string | 否 | 联想类型：ALL/ASSET/FIELD/TAG |

---

#### GET /api/v1/assets/advanced-search
**功能**：高级搜索
**功能说明**：支持复杂条件组合的高级搜索

**请求参数**：
```json
{
  "keyword": "string",
  "conditions": [
    {
      "field": "assetType",
      "operator": "IN",
      "values": ["TABLE", "VIEW"]
    },
    {
      "field": "sensitivityLevel",
      "operator": "=",
      "value": "L3"
    },
    {
      "field": "businessDomain",
      "operator": "LIKE",
      "value": "CRM"
    },
    {
      "field": "owner.name",
      "operator": "=",
      "value": "张三"
    }
  ],
  "sort": [
    {"field": "updatedTime", "order": "desc"},
    {"field": "qualityScore", "order": "desc"}
  ],
  "page": 1,
  "pageSize": 20
}
```

---

#### GET /api/v1/assets/recommend
**功能**：智能推荐资产
**功能说明**：基于用户画像和行为智能推荐资产

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| scene | string | 否 | 推荐场景：SIMILAR-相似推荐, HISTORY-历史相关, TREND-热门趋势 |
| assetId | string | 否 | 参考资产ID（SIMILAR场景必填） |
| limit | int | 否 | 返回数量，默认10 |

---

### 5.4 资产收藏与评价

#### POST /api/v1/assets/{id}/favorite
**功能**：收藏资产
**功能说明**：将资产添加到用户收藏夹

---

#### DELETE /api/v1/assets/{id}/favorite
**功能**：取消收藏
**功能说明**：从用户收藏夹移除资产

---

#### GET /api/v1/users/me/favorites
**功能**：获取我的收藏列表
**功能说明**：获取当前用户收藏的所有资产

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

#### POST /api/v1/assets/{id}/rating
**功能**：评价资产
**功能说明**：对资产进行评分和评论

**请求参数**：
```json
{
  "rating": 5,        // 1-5分
  "comment": "数据质量很好，使用方便",
  "tags": ["数据质量好", "文档完善"]
}
```

---

#### GET /api/v1/assets/{id}/ratings
**功能**：获取资产评价列表
**功能说明**：获取资产的所有用户评价

---

### 5.5 资产统计

#### GET /api/v1/assets/statistics/overview
**功能**：获取资产总览统计
**功能说明**：获取企业数据资产的总体统计信息

---

#### GET /api/v1/assets/statistics/trend
**功能**：获取资产趋势统计
**功能说明**：获取资产数量和分布的变化趋势

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| period | string | 否 | 统计周期：DAY/WEEK/MONTH |
| startDate | date | 是 | 开始日期 |
| endDate | date | 是 | 结束日期 |
| groupBy | string | 否 | 分组维度：TYPE/DOMAIN/LEVEL/STATUS |

---

#### GET /api/v1/assets/statistics/distribution
**功能**：获取资产分布统计
**功能说明**：获取资产在各维度的分布情况

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dimension | string | 否 | 维度：TYPE/DOMAIN/LEVEL/STATUS/OWNER/DEPARTMENT |

---

#### GET /api/v1/assets/statistics/quality
**功能**：获取质量统计
**功能说明**：获取数据质量相关的统计信息

---

### 5.6 资产生命周期

#### POST /api/v1/assets/{id}/lifecycle/deprecate
**功能**：废弃资产
**功能说明**：标记资产为废弃状态

**请求参数**：
```json
{
  "reason": "string",
  "replacementAssetId": "ast_002",
  "effectiveTime": "datetime",
  "notifyUsers": true
}
```

---

#### POST /api/v1/assets/{id}/lifecycle/restore
**功能**：恢复资产状态
**功能说明**：恢复已废弃的资产

---

#### POST /api/v1/assets/{id}/lifecycle/archive
**功能**：归档资产
**功能说明**：将资产归档到历史库

**请求参数**：
```json
{
  "archiveType": "COLD",  // COLD-冷数据归档, DELETE-到期删除
  "archiveTime": "datetime"
}
```

---

### 5.7 资产认证

#### POST /api/v1/assets/{id}/certification
**功能**：提交资产认证
**功能说明**：申请资产数据认证

**请求参数**：
```json
{
  "certificationLevel": "GOLD",
  "certificationScope": "FULL",  // FULL-全量, PARTIAL-部分
  "certifiedFields": ["field_001", "field_002"],
  "certificationBasis": "string",
  "contactPerson": "string",
  "contactEmail": "string"
}
```

---

#### POST /api/v1/assets/{id}/certification/approve
**功能**：审批资产认证
**功能说明**：审批通过资产认证申请

**请求参数**：
```json
{
  "approvedLevel": "GOLD",
  "comment": "string",
  "validUntil": "datetime"
}
```

---

#### POST /api/v1/assets/{id}/certification/reject
**功能**：驳回资产认证
**功能说明**：驳回资产认证申请

**请求参数**：
```json
{
  "reason": "string",
  "suggestions": "string"
}
```

---

## 6 血缘管理模块 API (Lineage)

### 6.1 血缘查询

#### GET /api/v1/lineage/table/{assetId}
**功能**：获取表级血缘
**功能说明**：获取资产的表级上下游血缘关系

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| direction | string | 否 | UPSTREAM/DOWNSTREAM/BOTH，默认BOTH |
| depth | int | 否 | 追溯深度，默认3，最大10 |
| includeTasks | boolean | 否 | 是否包含任务信息 |

**响应**：
```json
{
  "code": 0,
  "data": {
    "nodes": [
      {
        "id": "ast_001",
        "name": "ods_customer",
        "alias": "ODS客户表",
        "type": "TABLE",
        "level": 1,
        "database": "dw_db",
        "businessDomain": "ODS"
      },
      {
        "id": "ast_002",
        "name": "dwd_customer",
        "alias": "DWD客户表",
        "type": "TABLE",
        "level": 2,
        "database": "dw_db",
        "businessDomain": "DWD"
      },
      {
        "id": "ast_003",
        "name": "dim_customer",
        "alias": "DIM客户维表",
        "type": "TABLE",
        "level": 3,
        "database": "dw_db",
        "businessDomain": "DIM"
      }
    ],
    "edges": [
      {
        "source": "ast_001",
        "target": "ast_002",
        "transform": "INSERT OVERWRITE TABLE dwd_customer SELECT * FROM ods_customer WHERE dt = '${bizdate}'",
        "taskName": "ods_to_dwd_customer",
        "taskId": "task_001",
        "scheduleTime": "0 0 2 * * ?"
      }
    ],
    "statistics": {
      "upstreamCount": 5,
      "downstreamCount": 15,
      "maxDepth": 4,
      "hasCycle": false
    }
  }
}
```

---

#### GET /api/v1/lineage/field/{assetId}
**功能**：获取字段级血缘
**功能说明**：获取资产的字段级上下游血缘关系

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| direction | string | 否 | UPSTREAM/DOWNSTREAM/BOTH |
| depth | int | 否 | 追溯深度，默认2 |

---

#### GET /api/v1/lineage/path
**功能**：查询血缘路径
**功能说明**：查询两个资产之间的完整血缘路径

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sourceAssetId | string | 是 | 源资产ID |
| targetAssetId | string | 是 | 目标资产ID |
| direction | string | 否 | UPSTREAM/DOWNSTREAM |

---

#### GET /api/v1/lineage/graph
**功能**：获取血缘图数据
**功能说明**：获取用于可视化的血缘图数据

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| assetId | string | 是 | 中心资产ID |
| direction | string | 否 | UPSTREAM/DOWNSTREAM/BOTH |
| depth | int | 否 | 深度，默认3 |
| layout | string | 否 | 布局：LR/TB/CIRCLE |

---

### 6.2 血缘影响分析

#### GET /api/v1/lineage/impact/{assetId}
**功能**：影响分析（下游影响）
**功能说明**：分析资产变更对下游的影响范围

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| depth | int | 否 | 分析深度 |
| includeTasks | boolean | 否 | 是否包含任务 |
| includeReports | boolean | 否 | 是否包含报表 |

**响应**：
```json
{
  "code": 0,
  "data": {
    "assetId": "ast_001",
    "assetName": "客户信息表",
    "impactAnalysis": {
      "directDownstreamCount": 10,
      "totalDownstreamCount": 50,
      "criticalAssetsCount": 5,
      "reportsAffectedCount": 20,
      "estimatedImpactTime": "2026-04-11 02:00:00"
    },
    "affectedAssets": [
      {
        "id": "ast_002",
        "name": "客户宽表",
        "type": "TABLE",
        "dependencyType": "DIRECT",
        "criticalLevel": "HIGH",
        "owner": {...}
      }
    ],
    "affectedTasks": [...],
    "affectedReports": [...],
    "affectedDashboards": [...],
    "mitigationSuggestions": [
      {"suggestion": "通知下游任务负责人", "action": "NOTIFY_OWNERS"},
      {"suggestion": "暂停下游任务", "action": "PAUSE_TASKS"}
    ]
  }
}
```

---

#### GET /api/v1/lineage/dependency/{assetId}
**功能**：追溯分析（上游来源）
**功能说明**：追溯资产的数据来源

---

### 6.3 血缘管理

#### POST /api/v1/lineage
**功能**：手动添加血缘关系
**功能说明**：手动创建血缘关系

**请求参数**：
```json
{
  "sourceAssetId": "ast_001",
  "sourceFieldId": "fld_001",
  "targetAssetId": "ast_002",
  "targetFieldId": "fld_002",
  "lineageType": "MANUAL",  // ETL/SQL/MANUAL
  "transformDesc": "客户ID字段映射",
  "transformSql": "SELECT id AS customer_id FROM source_table"
}
```

---

#### DELETE /api/v1/lineage/{lineageId}
**功能**：删除血缘关系
**功能说明**：删除血缘关系

---

#### POST /api/v1/lineage/verify
**功能**：验证血缘关系
**功能说明**：验证血缘关系的准确性

**请求参数**：
```json
{
  "assetId": "ast_001",
  "verifyType": "FIELD",  // TABLE/FIELD
  "expectedUpstreams": ["ast_100", "ast_200"],
  "expectedDownstreams": ["ast_300"]
}
```

---

#### GET /api/v1/lineage/statistics
**功能**：获取血缘统计
**功能说明**：获取血缘覆盖率和统计信息

---

### 6.4 血缘变更历史

#### GET /api/v1/lineage/{assetId}/history
**功能**：获取血缘变更历史
**功能说明**：获取资产的血缘变更记录

---

#### GET /api/v1/lineage/compare
**功能**：对比血缘差异
**功能说明**：对比两个时间点或版本的血缘差异

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| assetId | string | 是 | 资产ID |
| version1 | string | 是 | 版本1（时间点或快照ID） |
| version2 | string | 是 | 版本2 |

---

## 7 质量管理模块 API (Quality)

### 7.1 质量规则

#### GET /api/v1/quality/rules
**功能**：获取质量规则列表
**功能说明**：获取所有预定义的质量规则

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ruleType | string | 否 | 规则类型 |
| targetType | string | 否 | 目标类型：TABLE/FIELD |
| severity | string | 否 | 严重程度 |
| status | int | 否 | 状态 |
| keyword | string | 否 | 关键词 |

---

#### POST /api/v1/quality/rules
**功能**：创建质量规则
**功能说明**：创建新的质量检测规则

**请求参数**：
```json
{
  "name": "字段非空检查",
  "code": "FIELD_NOT_NULL",
  "ruleType": "NOT_NULL",  // NOT_NULL/UNIQUENESS/RANGE/REGEX/FORMULA/CUSTOM
  "targetType": "FIELD",   // TABLE/FIELD
  "expression": "field IS NOT NULL",
  "description": "检查字段是否为空",
  "severity": "HIGH",       // CRITICAL/HIGH/MEDIUM/LOW
  "category": "COMPLETENESS",  // COMPLETENESS/ACCURACY/CONSISTENCY/TIMELINESS/UNIQUENESS
  "sqlTemplate": "SELECT COUNT(*) FROM ${table} WHERE NOT (${field} IS NOT NULL)",
  "params": {
    "threshold": 0
  }
}
```

---

#### GET /api/v1/quality/rules/{id}
**功能**：获取规则详情
**功能说明**：获取质量规则的详细信息

---

#### PUT /api/v1/quality/rules/{id}
**功能**：更新质量规则
**功能说明**：更新质量规则的定义

---

#### DELETE /api/v1/quality/rules/{id}
**功能**：删除质量规则
**功能说明**：删除质量规则

---

#### POST /api/v1/quality/rules/{id}/enable
**功能**：启用质量规则
**功能说明**：启用规则

---

#### POST /api/v1/quality/rules/{id}/disable
**功能**：禁用质量规则
**功能说明**：禁用规则

---

#### GET /api/v1/quality/rules/templates
**功能**：获取规则模板列表
**功能说明**：获取系统预设的规则模板

---

### 7.2 资产质量规则配置

#### GET /api/v1/assets/{assetId}/rules
**功能**：获取资产质量规则
**功能说明**：获取资产配置的质量检测规则

---

#### POST /api/v1/assets/{assetId}/rules
**功能**：为资产配置质量规则
**功能说明**：为资产分配质量检测规则

**请求参数**：
```json
{
  "rules": [
    {
      "ruleId": "qr_001",
      "fieldId": "fld_001",  // 可选，字段级规则
      "scheduleType": "SCHEDULED",  // IMMEDIATE/SCHEDULED
      "cronExpression": "0 0 3 * * ?",
      "enabled": true,
      "customParams": {
        "threshold": 99.5
      }
    }
  ]
}
```

---

#### DELETE /api/v1/assets/{assetId}/rules/{ruleConfigId}
**功能**：移除资产质量规则
**功能说明**：移除资产配置的规则

---

### 7.3 质量检测

#### POST /api/v1/quality/check
**功能**：触发质量检测
**功能说明**：手动触发质量检测任务

**请求参数**：
```json
{
  "assetId": "ast_100001",
  "ruleIds": ["qr_001", "qr_002"],
  "triggerType": "MANUAL",  // MANUAL/SCHEDULED/API
  "async": true
}
```

---

#### POST /api/v1/quality/check/batch
**功能**：批量触发质量检测
**功能说明**：批量触发多个资产的质量检测

**请求参数**：
```json
{
  "assetIds": ["ast_001", "ast_002"],
  "ruleIds": ["qr_001"],
  "triggerType": "MANUAL"
}
```

---

#### GET /api/v1/quality/check/{checkId}
**功能**：获取检测结果
**功能说明**：获取质量检测的执行结果

**响应**：
```json
{
  "code": 0,
  "data": {
    "checkId": "chk_001",
    "assetId": "ast_100001",
    "assetName": "客户信息表",
    "status": "COMPLETED",  // PENDING/RUNNING/COMPLETED/FAILED
    "startTime": "2026-04-10T03:00:00Z",
    "endTime": "2026-04-10T03:05:00Z",
    "duration": 300000,
    "summary": {
      "totalRules": 10,
      "passedRules": 8,
      "failedRules": 2,
      "errorRules": 0,
      "passRate": "80%"
    },
    "ruleResults": [
      {
        "ruleId": "qr_001",
        "ruleName": "字段非空检查",
        "status": "PASS",
        "passCount": 10000000,
        "totalCount": 10000000,
        "violationCount": 0,
        "passRate": "100%"
      },
      {
        "ruleId": "qr_002",
        "ruleName": "数据唯一性检查",
        "status": "FAIL",
        "passCount": 9995000,
        "totalCount": 10000000,
        "violationCount": 5000,
        "passRate": "99.95%",
        "sampleViolations": [
          {"customer_id": "10001", "violation_type": "DUPLICATE"}
        ]
      }
    ],
    "executionEngine": "SPARK",
    "errorMessage": null
  }
}
```

---

#### GET /api/v1/quality/check/{checkId}/progress
**功能**：获取检测进度
**功能说明**：获取异步检测任务的执行进度

---

### 7.4 资产质量报告

#### GET /api/v1/assets/{assetId}/quality
**功能**：获取资产质量报告
**功能说明**：获取资产的完整质量状况报告

**响应**：
```json
{
  "code": 0,
  "data": {
    "assetId": "ast_100001",
    "assetName": "客户信息表",
    "overallScore": 95,
    "grade": "A",  // A/B/C/D
    "trend": [
      {"date": "2026-04-01", "score": 92},
      {"date": "2026-04-02", "score": 94},
      {"date": "2026-04-03", "score": 95}
    ],
    "categoryScores": {
      "COMPLETENESS": 98,
      "ACCURACY": 92,
      "CONSISTENCY": 95,
      "TIMELINESS": 95,
      "UNIQUENESS": 90
    },
    "ruleResults": [...],
    "issueSummary": {
      "total": 17,
      "critical": 0,
      "high": 2,
      "medium": 5,
      "low": 10
    },
    "recentChecks": [...],
    "lastCheckTime": "2026-04-10T03:00:00Z",
    "nextScheduledCheck": "2026-04-11T03:00:00Z"
  }
}
```

---

#### GET /api/v1/assets/{assetId}/quality/trend
**功能**：获取质量趋势
**功能说明**：获取资产质量分数的变化趋势

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| period | string | 否 | DAY/WEEK/MONTH |
| startDate | date | 是 | 开始日期 |
| endDate | date | 是 | 结束日期 |

---

### 7.5 问题管理

#### GET /api/v1/quality/issues
**功能**：查询质量问题列表
**功能说明**：查询所有质量问题

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| assetId | string | 否 | 资产ID |
| severity | string | 否 | 严重程度 |
| status | string | 否 | 状态 |
| assigneeId | string | 否 | 处理人 |
| startTime | datetime | 否 | 开始时间 |
| endTime | datetime | 否 | 结束时间 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

#### GET /api/v1/quality/issues/{id}
**功能**：获取问题详情
**功能说明**：获取质量问题的详细信息

**响应**：
```json
{
  "code": 0,
  "data": {
    "id": "issue_001",
    "issueNo": "QI-20260410-0001",
    "assetId": "ast_100001",
    "assetName": "客户信息表",
    "ruleId": "qr_002",
    "ruleName": "数据唯一性检查",
    "fieldId": "fld_001",
    "fieldName": "customer_id",
    "severity": "HIGH",
    "status": "OPEN",
    "description": "检测到customer_id存在重复值",
    "sampleData": [
      {"customer_id": "10001", "count": 3},
      {"customer_id": "10002", "count": 2}
    ],
    "checkResultId": "chk_001",
    "assignee": {
      "id": "u_10001",
      "name": "张三"
    },
    "assignedTime": "2026-04-10T04:00:00Z",
    "dueTime": "2026-04-12T00:00:00Z",
    "resolution": null,
    "resolvedBy": null,
    "resolvedTime": null,
    "history": [
      {
        "action": "CREATED",
        "operator": "系统",
        "time": "2026-04-10T03:05:00Z",
        "comment": "质量问题已自动创建"
      },
      {
        "action": "ASSIGNED",
        "operator": "李四",
        "time": "2026-04-10T04:00:00Z",
        "comment": "分配给张三处理"
      }
    ],
    "createdTime": "2026-04-10T03:05:00Z"
  }
}
```

---

#### PUT /api/v1/quality/issues/{id}
**功能**：更新问题状态
**功能说明**：更新质量问题的状态和处理信息

**请求参数**：
```json
{
  "status": "IN_PROGRESS",
  "assigneeId": "u_10001",
  "dueTime": "2026-04-12T00:00:00Z",
  "comment": "已开始排查原因"
}
```

---

#### POST /api/v1/quality/issues/{id}/resolve
**功能**：解决问题
**功能说明**：标记问题为已解决

**请求参数**：
```json
{
  "resolution": "已处理重复数据",
  "rootCause": "数据来源重复写入",
  "fixMethod": "在源系统去重",
  "preventiveMeasure": "增加去重逻辑",
  "verifyCheckId": "chk_002"
}
```

---

#### POST /api/v1/quality/issues/{id}/close
**功能**：关闭问题
**功能说明**：关闭问题（验证通过后）

---

#### POST /api/v1/quality/issues/{id}/transfer
**功能**：转移问题
**功能说明**：将问题转移给其他处理人

**请求参数**：
```json
{
  "assigneeId": "u_10002",
  "reason": "原负责人离职"
}
```

---

#### POST /api/v1/quality/issues/{id}/ignore
**功能**：忽略问题
**功能说明**：标记问题为已知悉忽略

**请求参数**：
```json
{
  "reason": "业务允许少量重复"
}
```

---

#### GET /api/v1/quality/issues/statistics
**功能**：获取问题统计
**功能说明**：获取质量问题的统计信息

---

## 8 安全合规模块 API (Security)

### 8.1 敏感等级管理

#### GET /api/v1/security/levels
**功能**：获取敏感等级定义
**功能说明**：获取系统定义的敏感等级列表

---

#### POST /api/v1/security/levels
**功能**：创建敏感等级
**功能说明**：创建新的敏感等级

**请求参数**：
```json
{
  "levelCode": "L5",
  "levelName": "绝密",
  "color": "#FF0000",
  "description": "最高机密级别",
  "requireAuth": true,
  "requireMFA": true,
  "allowExport": false,
  "allowDownload": false,
  "maskLevel": "FULL",
  "auditRequired": true,
  "approvalRequired": true,
  "approvalWorkflow": "workflow_001",
  "sortOrder": 10
}
```

---

#### PUT /api/v1/security/levels/{id}
**功能**：更新敏感等级
**功能说明**：更新敏感等级定义

---

#### DELETE /api/v1/security/levels/{id}
**功能**：删除敏感等级
**功能说明**：删除敏感等级

---

#### PUT /api/v1/assets/{id}/sensitivity
**功能**：更新资产敏感等级
**功能说明**：修改资产的敏感等级

**请求参数**：
```json
{
  "sensitivityLevel": "L3",
  "changeReason": "业务调整",
  "attachments": ["file_id_1"]
}
```

---

### 8.2 敏感数据识别

#### GET /api/v1/security/sensitive/patterns
**功能**：获取敏感数据识别规则
**功能说明**：获取系统预设的敏感数据识别模式

---

#### POST /api/v1/security/sensitive/scan
**功能**：扫描敏感数据
**功能说明**：扫描资产中的敏感数据

**请求参数**：
```json
{
  "assetId": "ast_001",
  "scanFields": true,
  "scanContent": true,
  "matchPatterns": ["ID_CARD", "MOBILE", "BANK_CARD"]
}
```

---

#### GET /api/v1/assets/{assetId}/sensitive/fields
**功能**：获取资产敏感字段
**功能说明**：获取资产中包含敏感数据的字段

---

### 8.3 脱敏规则

#### GET /api/v1/security/masking/rules
**功能**：获取脱敏规则列表
**功能说明**：获取所有脱敏规则

---

#### POST /api/v1/security/masking/rules
**功能**：创建脱敏规则
**功能说明**：创建新的脱敏规则

**请求参数**：
```json
{
  "name": "手机号脱敏",
  "code": "MOBILE_MASKING",
  "fieldPattern": ".*(phone|mobile|tel).*",
  "dataType": "VARCHAR",
  "maskingType": "PARTIAL",  // HASH/PARTIAL/FULL/RANDOM/REPLACE/CUSTOM
  "pattern": "***",
  "visibleChars": 3,
  "visiblePosition": "LEFT",
  "priority": 100,
  "description": "手机号只显示前3位和后4位",
  "compatibleWith": ["L3", "L4"]
}
```

---

#### PUT /api/v1/security/masking/rules/{id}
**功能**：更新脱敏规则
**功能说明**：更新脱敏规则定义

---

#### DELETE /api/v1/security/masking/rules/{id}
**功能**：删除脱敏规则
**功能说明**：删除脱敏规则

---

#### POST /api/v1/assets/{id}/masking
**功能**：应用脱敏规则
**功能说明**：为资产配置脱敏规则

**请求参数**：
```json
{
  "maskingRules": [
    {
      "ruleId": "mr_001",
      "fieldId": "fld_001"
    }
  ],
  "maskingLevel": "L3",  // 应用到指定敏感等级及以上
  "maskingContext": ["PREVIEW", "EXPORT", "API"]  // 应用场景
}
```

---

#### GET /api/v1/security/masking/preview
**功能**：预览脱敏效果
**功能说明**：预览指定数据的脱敏效果

**请求参数**：
```json
{
  "assetId": "ast_001",
  "fieldId": "fld_001",
  "sampleData": "13812345678",
  "maskingType": "PARTIAL"
}
```

---

### 8.4 合规审计

#### GET /api/v1/security/audit/logs
**功能**：获取安全审计日志
**功能说明**：获取安全相关的审计日志

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| logType | string | 否 | LOGIN/SENSITIVE_ACCESS/DATA_EXPORT/CONFIG_CHANGE |
| userId | string | 否 | 用户ID |
| startTime | datetime | 否 | 开始时间 |
| endTime | datetime | 否 | 结束时间 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

#### GET /api/v1/security/compliance/report
**功能**：生成合规报告
**功能说明**：生成数据安全合规报告

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reportType | string | 是 | DSG-数据安全法, PIPL-个人信息保护, GDPR |
| startDate | date | 是 | 开始日期 |
| endDate | date | 是 | 结束日期 |
| scope | string | 否 | ALL/DEPARTMENT/ASSET |

---

## 9 数据源管理模块 API (Datasource)

### 9.1 数据源CRUD

#### GET /api/v1/datasources
**功能**：获取数据源列表
**功能说明**：获取所有已配置的数据源

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| datasourceType | string | 否 | 数据源类型 |
| status | string | 否 | CONNECTED/DISCONNECTED |
| keyword | string | 否 | 关键词 |

---

#### POST /api/v1/datasources
**功能**：创建数据源
**功能说明**：配置新的数据源连接

**请求参数**：
```json
{
  "name": "CRM数据库",
  "datasourceType": "MYSQL",
  "host": "crm-db.company.com",
  "port": 3306,
  "database": "crm_db",
  "username": "readonly_user",
  "password": "encrypted_password",
  "connectionUrl": "jdbc:mysql://crm-db.company.com:3306/crm_db",
  "properties": {
    "useSSL": true,
    "serverTimezone": "Asia/Shanghai"
  },
  "description": "CRM业务库",
  "tags": ["CRM", "核心"],
  "maxConnections": 10,
  "connectionTimeout": 30000,
  "idleTimeout": 600000
}
```

---

#### GET /api/v1/datasources/{id}
**功能**：获取数据源详情
**功能说明**：获取数据源的详细信息

---

#### PUT /api/v1/datasources/{id}
**功能**：更新数据源
**功能说明**：更新数据源配置

---

#### DELETE /api/v1/datasources/{id}
**功能**：删除数据源
**功能说明**：删除数据源配置

---

#### POST /api/v1/datasources/{id}/test
**功能**：测试数据源连接
**功能说明**：测试数据源连通性

**响应**：
```json
{
  "code": 0,
  "data": {
    "success": true,
    "latency": 50,
    "version": "8.0.32",
    "catalogs": ["crm_db", "analytics_db"],
    "message": "连接成功"
  }
}
```

---

#### POST /api/v1/datasources/{id}/sync
**功能**：同步数据源元数据
**功能说明**：触发数据源元数据同步

**请求参数**：
```json
{
  "syncType": "FULL",  // FULL/INCREMENTAL
  "syncScope": {
    "databases": ["crm_db"],
    "tables": ["customer_*"],
    "excludeTables": ["temp_*"]
  },
  "async": true
}
```

---

#### GET /api/v1/datasources/{id}/sync/status
**功能**：获取同步状态
**功能说明**：获取元数据同步任务状态

---

### 9.2 数据源表管理

#### GET /api/v1/datasources/{id}/tables
**功能**：获取数据源表列表
**功能说明**：获取数据源下的所有表

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| database | string | 是 | 数据库名 |
| schema | string | 否 | Schema名 |
| keyword | string | 否 | 关键词 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

#### GET /api/v1/datasources/{id}/tables/{database}/{tableName}
**功能**：获取表结构信息
**功能说明**：获取数据源的表结构详情

---

## 10 通知模块 API (Notification)

### 10.1 消息管理

#### GET /api/v1/notifications
**功能**：获取消息列表
**功能说明**：获取当前用户的消息列表

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 否 | 消息类型 |
| status | string | 否 | 状态：UNREAD/READ |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

#### GET /api/v1/notifications/{id}
**功能**：获取消息详情
**功能说明**：获取消息的详细信息

---

#### PUT /api/v1/notifications/{id}/read
**功能**：标记已读
**功能说明**：将消息标记为已读

---

#### PUT /api/v1/notifications/batch/read
**功能**：批量标记已读
**功能说明**：批量将消息标记为已读

**请求参数**：
```json
{
  "notificationIds": ["notif_001", "notif_002"]
}
```

---

#### DELETE /api/v1/notifications/{id}
**功能**：删除消息
**功能说明**：删除消息

---

### 10.2 通知模板

#### GET /api/v1/notification-templates
**功能**：获取通知模板列表
**功能说明**：获取系统通知模板

---

#### POST /api/v1/notification-templates
**功能**：创建通知模板
**功能说明**：创建新的通知模板

**请求参数**：
```json
{
  "name": "质量问题通知",
  "code": "QUALITY_ISSUE",
  "channel": "EMAIL",  // IN_APP/EMAIL/SMS/WECHAT/DINGTALK
  "title": "【质量问题】${assetName}存在${issueCount}个问题",
  "content": "<html>...</html>",
  "variables": ["assetName", "issueCount", "severity"],
  "enabled": true
}
```

---

### 10.3 订阅管理

#### GET /api/v1/subscriptions
**功能**：获取订阅列表
**功能说明**：获取当前用户的订阅配置

---

#### POST /api/v1/subscriptions
**功能**：创建订阅
**功能说明**：订阅资产或主题的变更通知

**请求参数**：
```json
{
  "subscriptionType": "ASSET",  // ASSET/BUSINESS_DOMAIN/TAG
  "targetId": "ast_001",
  "events": ["ASSET_UPDATED", "QUALITY_CHANGED"],
  "channels": ["IN_APP", "EMAIL"],
  "frequency": "INSTANT"  // INSTANT/DAILY/WEEKLY
}
```

---

## 11 工作流模块 API (Workflow)

### 11.1 审批流程

#### GET /api/v1/workflows/approvals
**功能**：获取待我审批列表
**功能说明**：获取需要当前用户审批的任务

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | PENDING/APPROVED/REJECTED |
| workflowType | string | 否 | 流程类型 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

#### GET /api/v1/workflows/{workflowInstanceId}
**功能**：获取流程详情
**功能说明**：获取审批流程的详细信息

---

#### POST /api/v1/workflows/{workflowInstanceId}/approve
**功能**：审批通过
**功能说明**：审批通过流程

**请求参数**：
```json
{
  "comment": "同意",
  "nextApproverId": "u_10002",
  "variables": {
    "approvedLevel": "GOLD"
  }
}
```

---

#### POST /api/v1/workflows/{workflowInstanceId}/reject
**功能**：审批驳回
**功能说明**：驳回流程申请

**请求参数**：
```json
{
  "comment": "需要补充材料",
  "rejectReason": "材料不完整"
}
```

---

#### GET /api/v1/workflows/{workflowInstanceId}/history
**功能**：获取流程历史
**功能说明**：获取流程的审批历史

---

## 12 知识图谱模块 API (Knowledge)

### 12.1 本体管理

#### GET /api/v1/knowledge/ontologies
**功能**：获取本体列表
**功能说明**：获取所有知识图谱本体

---

#### POST /api/v1/knowledge/ontologies
**功能**：创建本体
**功能说明**：创建新的知识本体

**请求参数**：
```json
{
  "name": "客户本体",
  "code": "CUSTOMER_ONTOLOGY",
  "description": "客户领域知识本体",
  "version": "1.0",
  "entities": [
    {
      "name": "Customer",
      "alias": "客户",
      "properties": ["customerId", "name", "type"]
    }
  ],
  "relations": [
    {
      "name": "HAS_ORDER",
      "sourceEntity": "Customer",
      "targetEntity": "Order",
      "description": "客户下订单"
    }
  ]
}
```

---

#### GET /api/v1/knowledge/ontologies/{id}
**功能**：获取本体详情
**功能说明**：获取本体的完整定义

---

### 12.2 实体管理

#### GET /api/v1/knowledge/entities
**功能**：查询实体
**功能说明**：查询图谱中的实体

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ontologyId | string | 否 | 本体ID |
| entityType | string | 否 | 实体类型 |
| keyword | string | 否 | 关键词 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

#### GET /api/v1/knowledge/entities/{id}
**功能**：获取实体详情
**功能说明**：获取实体的详细信息和关系

---

#### POST /api/v1/knowledge/entities
**功能**：创建实体
**功能说明**：在图谱中创建新实体

---

### 12.3 关系管理

#### GET /api/v1/knowledge/relations
**功能**：查询关系
**功能说明**：查询图谱中的关系

---

#### POST /api/v1/knowledge/relations
**功能**：创建关系
**功能说明**：在图谱中创建关系

**请求参数**：
```json
{
  "sourceEntityId": "entity_001",
  "targetEntityId": "entity_002",
  "relationType": "BELONGS_TO",
  "properties": {
    "confidence": 0.95
  }
}
```

---

### 12.4 图谱应用

#### GET /api/v1/knowledge/graph
**功能**：获取图谱子图
**功能说明**：获取指定实体为中心的图谱子图

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| centerEntityId | string | 是 | 中心实体ID |
| depth | int | 否 | 深度，默认2 |
| relationTypes | string | 否 | 关系类型过滤 |

---

#### GET /api/v1/knowledge/path
**功能**：查询关系路径
**功能说明**：查询两个实体之间的最短路径

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sourceEntityId | string | 是 | 源实体ID |
| targetEntityId | string | 是 | 目标实体ID |

---

## 13 数据编织模块 API (Fabric)

### 13.1 跨源关联

#### GET /api/v1/fabric/links
**功能**：获取跨源关联列表
**功能说明**：获取已建立的跨源实体关联

---

#### POST /api/v1/fabric/links
**功能**：创建跨源关联
**功能说明**：建立不同数据源的实体关联

**请求参数**：
```json
{
  "sourceAssetId": "ast_001",
  "targetAssetId": "ast_002",
  "linkType": "SAME_AS",
  "confidence": 0.95,
  "matchFields": [
    {"sourceField": "customer_id", "targetField": "cust_no"}
  ]
}
```

---

### 13.2 虚拟视图

#### GET /api/v1/fabric/views
**功能**：获取虚拟视图列表
**功能说明**：获取已定义的虚拟视图

---

#### POST /api/v1/fabric/views
**功能**：创建虚拟视图
**功能说明**：创建基于多数据源的虚拟视图

**请求参数**：
```json
{
  "name": "客户订单综合视图",
  "code": "CUSTOMER_ORDER_VIEW",
  "description": "整合客户和订单信息的视图",
  "sqlDefinition": "SELECT c.id, c.name, o.order_no, o.amount FROM crm_db.customer c LEFT JOIN oms_db.order o ON c.id = o.customer_id",
  "fields": [
    {"name": "customer_id", "alias": "客户ID", "dataType": "BIGINT"},
    {"name": "customer_name", "alias": "客户名称", "dataType": "VARCHAR"}
  ]
}
```

---

#### GET /api/v1/fabric/views/{id}
**功能**：获取虚拟视图详情
**功能说明**：获取虚拟视图的详细信息

---

#### POST /api/v1/fabric/views/{id}/preview
**功能**：预览虚拟视图
**功能说明**：预览虚拟视图的数据

**请求参数**：
```json
{
  "limit": 100,
  "offset": 0
}
```

---

### 13.3 供需匹配

#### GET /api/v1/fabric/matching/supply
**功能**：获取数据供应列表
**功能说明**：获取已注册的数据供应

---

#### POST /api/v1/fabric/matching/supply
**功能**：注册数据供应
**功能说明**：注册新的数据供应

**请求参数**：
```json
{
  "assetId": "ast_001",
  "supplyName": "客户基础信息服务",
  "description": "提供客户基础信息服务",
  "serviceType": "API",
  "sla": {
    "responseTime": 100,
    "availability": 99.9
  },
  "pricing": {
    "type": "FREE",
    "price": 0
  }
}
```

---

#### GET /api/v1/fabric/matching/demand
**功能**：获取数据需求列表
**功能说明**：获取已提交的数据需求

---

#### POST /api/v1/fabric/matching/demand
**功能**：提交数据需求
**功能说明**：提交新的数据需求

**请求参数**：
```json
{
  "demandName": "需要客户消费数据",
  "description": "用于用户画像分析",
  "requiredFields": ["客户ID", "消费金额", "消费时间"],
  "dataVolume": "1000万条/天",
  "updateFrequency": "实时",
  "contactUserId": "u_10001"
}
```

---

#### GET /api/v1/fabric/matching/recommendations
**功能**：获取匹配推荐
**功能说明**：基于需求获取匹配的供应推荐

---

## 14 治理闭环模块 API (Governance)

### 14.1 治理任务

#### GET /api/v1/governance/tasks
**功能**：获取治理任务列表
**功能说明**：获取系统生成的治理任务

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| taskType | string | 否 | 任务类型 |
| status | string | 否 | 状态 |
| priority | string | 否 | 优先级 |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

#### POST /api/v1/governance/tasks/{id}/execute
**功能**：执行治理任务
**功能说明**：执行指定的治理任务

---

#### GET /api/v1/governance/tasks/{id}/sub-tasks
**功能**：获取子任务列表
**功能说明**：获取治理任务的子任务

---

### 14.2 成熟度评估

#### GET /api/v1/governance/maturity
**功能**：获取成熟度评估
**功能说明**：获取当前数据治理成熟度评估

**响应**：
```json
{
  "code": 0,
  "data": {
    "assessmentId": "ma_001",
    "assessmentDate": "2026-04-01",
    "overallScore": 65,
    "overallLevel": "LEVEL_3",  // LEVEL_1-初始/LEVEL_2-管理/LEVEL_3-定义/LEVEL_4-量化/LEVEL_5-优化
    "dimensionScores": {
      "dataQuality": 70,
      "dataGovernance": 65,
      "dataSecurity": 75,
      "metadataManagement": 60,
      "dataArchitecture": 55
    },
    "comparison": {
      "previousScore": 60,
      "improvement": 5,
      "industryAverage": 55
    },
    "recommendations": [
      {"dimension": "元数据管理", "suggestion": "完善元数据采集覆盖率"},
      {"dimension": "数据架构", "suggestion": "建立数据架构规范"}
    ]
  }
}
```

---

#### GET /api/v1/governance/maturity/history
**功能**：获取成熟度历史
**功能说明**：获取成熟度评估的历史记录

---

### 14.3 激励机制

#### GET /api/v1/governance/incentives/points
**功能**：获取积分排行榜
**功能说明**：获取用户积分排名

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| period | string | 否 | TODAY/WEEK/MONTH/ALL |
| dimension | string | 否 | 维度：PERSONAL/DEPARTMENT |
| page | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

---

#### GET /api/v1/users/me/incentives/profile
**功能**：获取我的积分档案
**功能说明**：获取当前用户的积分详情

---

## 15 价值评估模块 API (Value)

### 15.1 价值评估

#### GET /api/v1/value/assessments
**功能**：获取价值评估列表
**功能说明**：获取数据资产价值评估结果

---

#### POST /api/v1/assets/{id}/value/assess
**功能**：触发价值评估
**功能说明**：对资产进行价值评估

---

#### GET /api/v1/assets/{id}/value
**功能**：获取资产价值详情
**功能说明**：获取资产的完整价值评估报告

**响应**：
```json
{
  "code": 0,
  "data": {
    "assetId": "ast_001",
    "assessmentDate": "2026-04-10",
    "overallScore": 85,
    "businessValue": {
      "score": 90,
      "usageCount": 500,
      "dependentProcesses": 20,
      "businessCriticality": "HIGH"
    },
    "technicalValue": {
      "score": 80,
      "dataQuality": 95,
      "lineageCompleteness": 85,
      "documentationQuality": 60
    },
    "costValue": {
      "score": 75,
      "storageCost": 1000,
      "maintenanceCost": 500,
      "computeCost": 2000
    },
    "roi": {
      "investment": 50000,
      "estimatedReturn": 150000,
      "roiRatio": 2.0,
      "paybackPeriod": 12
    }
  }
}
```

---

### 15.2 数据产品

#### GET /api/v1/value/products
**功能**：获取数据产品列表
**功能说明**：获取已发布的数据产品

---

#### POST /api/v1/value/products
**功能**：创建数据产品
**功能说明**：将资产封装为数据产品

**请求参数**：
```json
{
  "name": "客户画像API",
  "code": "CUSTOMER_PROFILE_API",
  "description": "提供客户360度画像数据",
  "assetIds": ["ast_001", "ast_002"],
  "apiEndpoint": "/api/v1/products/customer-profile",
  "pricing": {
    "type": "SUBSCRIPTION",
    "price": 10000,
    "priceUnit": "MONTH"
  },
  "sla": {
    "availability": 99.9,
    "responseTime": 200,
    "dataFreshness": "T+1"
  }
}
```

---

### 15.3 ROI追踪

#### GET /api/v1/value/roi/report
**功能**：获取ROI分析报告
**功能说明**：获取数据资产的ROI分析报告

---

## 16 跨组织协作模块 API (Collaboration)

### 16.1 联邦治理

#### GET /api/v1/collaboration/organizations
**功能**：获取组织列表
**功能说明**：获取跨组织架构中的组织列表

---

#### POST /api/v1/collaboration/organizations
**功能**：创建组织
**功能说明**：创建新的子组织

---

### 16.2 数据共享协议

#### GET /api/v1/collaboration/agreements
**功能**：获取共享协议列表
**功能说明**：获取数据共享协议

---

#### POST /api/v1/collaboration/agreements
**功能**：创建共享协议
**功能说明**：创建数据共享协议

**请求参数**：
```json
{
  "name": "集团客户数据共享协议",
  "providerOrgId": "org_001",
  "consumerOrgId": "org_002",
  "scope": {
    "assetIds": ["ast_001", "ast_002"],
    "fields": ["customer_id", "name"]
  },
  "usageTerms": "仅用于营销分析",
  "securityRequirements": {
    "encryptionRequired": true,
    "accessLogging": true,
    "dataRetentionDays": 90
  },
  "effectiveDate": "2026-05-01",
  "expireDate": "2027-05-01"
}
```

---

#### POST /api/v1/collaboration/agreements/{id}/approve
**功能**：审批共享协议
**功能说明**：审批数据共享协议

---

## 17 智能洞察模块 API (Insight)

### 17.1 智能发现

#### GET /api/v1/insight/anomalies
**功能**：获取异常数据列表
**功能说明**：获取检测到的数据异常

---

#### GET /api/v1/insight/patterns
**功能**：获取数据模式
**功能说明**：获取识别的数据模式

---

### 17.2 业务助手

#### POST /api/v1/insight/assistant/chat
**功能**：智能问答
**功能说明**：基于知识图谱的智能问答

**请求参数**：
```json
{
  "question": "客户信息表的负责人是谁？",
  "context": {
    "currentAssetId": "ast_001"
  }
}
```

---

#### GET /api/v1/insight/assistant/history
**功能**：获取问答历史
**功能说明**：获取智能问答的历史记录

---

### 17.3 预测分析

#### GET /api/v1/insight/predictions/quality
**功能**：获取质量预测
**功能说明**：预测数据质量趋势

---

## 18 边缘计算模块 API (Edge)

### 18.1 设备管理

#### GET /api/v1/edge/devices
**功能**：获取边缘设备列表
**功能说明**：获取注册的边缘设备

---

#### POST /api/v1/edge/devices
**功能**：注册边缘设备
**功能说明**：注册新的边缘设备

---

#### GET /api/v1/edge/devices/{id}
**功能**：获取设备详情
**功能说明**：获取边缘设备详细信息

---

#### PUT /api/v1/edge/devices/{id}
**功能**：更新设备配置
**功能说明**：更新边缘设备配置

---

#### POST /api/v1/edge/devices/{id}/commands
**功能**：发送设备指令
**功能说明**：向边缘设备发送控制指令

**请求参数**：
```json
{
  "command": "SYNC_DATA",
  "params": {
    "targetData": ["sensor_001", "sensor_002"]
  }
}
```

---

### 18.2 数据采集

#### GET /api/v1/edge/data/collected
**功能**：获取边缘采集数据
**功能说明**：查询边缘设备采集的数据

---

## 19 伦理合规模块 API (Ethics)

### 19.1 伦理框架

#### GET /api/v1/ethics/policies
**功能**：获取伦理政策列表
**功能说明**：获取企业数据伦理政策

---

#### POST /api/v1/ethics/assessments
**功能**：提交伦理评估申请
**功能说明**：提交数据使用的伦理评估申请

---

#### GET /api/v1/ethics/assessments/{id}
**功能**：获取伦理评估详情
**功能说明**：获取伦理评估的详细结果

---

### 19.2 公平性评估

#### GET /api/v1/ethics/fairness/reports
**功能**：获取公平性报告
**功能说明**：获取数据公平性评估报告

---

## 20 弹性架构模块 API (Elastic)

### 20.1 成本分析

#### GET /api/v1/elastic/costs/overview
**功能**：获取成本概览
**功能说明**：获取系统成本总体情况

---

#### GET /api/v1/elastic/costs/detailed
**功能**：获取成本明细
**功能说明**：获取详细的成本分析

---

### 20.2 资源优化

#### GET /api/v1/elastic/optimizations
**功能**：获取优化建议
**功能说明**：获取系统优化建议

---

## 21 统计分析模块 API (Statistics)

### 21.1 资产统计

#### GET /api/v1/statistics/overview
**功能**：获取资产总览统计
**功能说明**：获取企业数据资产的总体统计

---

#### GET /api/v1/statistics/trend
**功能**：获取资产趋势统计
**功能说明**：获取资产数量和分布的变化趋势

---

#### GET /api/v1/statistics/distribution
**功能**：获取资产分布统计
**功能说明**：获取资产在各维度的分布情况

---

## 22 Webhook模块 API

### 22.1 Webhook管理

#### GET /api/v1/webhooks
**功能**：获取Webhook列表
**功能说明**：获取已配置的Webhook

---

#### POST /api/v1/webhooks
**功能**：创建Webhook
**功能说明**：创建新的Webhook

**请求参数**：
```json
{
  "name": "资产变更通知",
  "url": "https://callback.company.com/asset",
  "events": [
    "ASSET_CREATED",
    "ASSET_UPDATED",
    "ASSET_DELETED",
    "QUALITY_CHECK_FAILED",
    "GOVERNANCE_TASK_ASSIGNED"
  ],
  "secret": "webhook_secret_key",
  "headers": {
    "X-Custom-Header": "value"
  },
  "retryPolicy": {
    "maxRetries": 3,
    "retryDelay": 1000
  },
  "enabled": true
}
```

---

#### GET /api/v1/webhooks/{id}
**功能**：获取Webhook详情
**功能说明**：获取Webhook详细信息

---

#### PUT /api/v1/webhooks/{id}
**功能**：更新Webhook
**功能说明**：更新Webhook配置

---

#### DELETE /api/v1/webhooks/{id}
**功能**：删除Webhook
**功能说明**：删除Webhook

---

#### POST /api/v1/webhooks/{id}/test
**功能**：测试Webhook
**功能说明**：发送测试事件验证Webhook

---

#### GET /api/v1/webhooks/{id}/deliveries
**功能**：获取Webhook投递记录
**功能说明**：获取Webhook事件投递历史

---

## 23 附录

### 23.1 资产类型枚举

| 编码 | 名称 | 说明 |
|------|------|------|
| TABLE | 数据表 | 数据库表 |
| VIEW | 视图 | 数据库视图 |
| FILE | 文件 | 文件/附件 |
| API | API接口 | REST/gRPC接口 |
| STREAM | 流数据 | Kafka/流处理数据 |
| MODEL | 数据模型 | 维度/事实模型 |
| REPORT | 报表 | BI报表 |
| DASHBOARD | 仪表盘 | 数据仪表盘 |

### 23.2 敏感等级枚举

| 编码 | 名称 | 颜色 | 说明 |
|------|------|------|------|
| L1 | 公开 | #00FF00 | 可对外公开 |
| L2 | 内部 | #FFFF00 | 仅企业内部 |
| L3 | 敏感 | #FFA500 | 涉及隐私 |
| L4 | 机密 | #FF0000 | 核心机密 |

### 23.3 质量规则类型

| 编码 | 名称 | 类别 |
|------|------|------|
| NOT_NULL | 非空检查 | 完整性 |
| UNIQUENESS | 唯一性检查 | 唯一性 |
| RANGE | 值域检查 | 准确性 |
| REGEX | 格式检查 | 准确性 |
| FRESHNESS | 时效性检查 | 时效性 |
| CONSISTENCY | 一致性检查 | 一致性 |
| CUSTOM | 自定义规则 | - |

---

**文档结束**

**API接口总数：400+**
**覆盖模块：20个功能域**
**文档版本：V2.0完整版**
