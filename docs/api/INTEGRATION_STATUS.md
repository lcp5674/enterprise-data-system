# EDAMS 前后端集成状态报告

> 版本: 1.0.0  
> 更新日期: 2026-04-11  
> 状态: 持续更新中

## 1. 集成概览

### 1.1 总体状态

| 服务模块 | 前端Service | 后端Controller | 集成状态 | 备注 |
|----------|-------------|----------------|----------|------|
| 认证服务 (Auth) | ✅ | ⚠️ | 🟡 部分就绪 | 后端需补全Controller |
| 用户服务 (User) | ✅ | ⚠️ | 🟡 部分就绪 | 后端需补全Controller |
| 权限服务 (Permission) | ✅ | ⚠️ | 🟡 部分就绪 | 后端需补全Controller |
| 资产服务 (Asset) | ✅ | ⚠️ | 🟡 部分就绪 | 后端需补全Controller |
| 元数据服务 (Metadata) | ✅ | ✅ | 🟢 已就绪 | 完全匹配 |
| 血缘服务 (Lineage) | ✅ | ✅ | 🟢 已就绪 | 完全匹配 |
| 质量服务 (Quality) | ✅ | ✅ | 🟢 已就绪 | 完全匹配 |
| 数据标准 (Standard) | ✅ | ✅ | 🟢 已就绪 | 完全匹配 |
| 治理服务 (Governance) | ✅ | ✅ | 🟢 已就绪 | 完全匹配 |
| 工作流 (Workflow) | ✅ | ⚠️ | 🟡 部分就绪 | 后端需补全Controller |
| 通知服务 (Notification) | ✅ | ⚠️ | 🟡 部分就绪 | 后端需补全Controller |
| 数据源 (Datasource) | ✅ | ⚠️ | 🟡 部分就绪 | 后端需补全Controller |

**图例说明:**
- 🟢 已就绪 - 前后端完全匹配，可直接联调
- 🟡 部分就绪 - 前端就绪，后端需补全
- 🔴 未就绪 - 前后端均缺失

---

## 2. 详细集成状态

### 2.1 认证服务 (Auth)

**前端文件:** `frontend/edams-web/src/services/auth.ts`

| 接口 | 前端方法 | 后端状态 | 备注 |
|------|----------|----------|------|
| POST /api/v1/auth/login | `login()` | ⚠️ 待实现 | edams-auth需补全 |
| POST /api/v1/auth/logout | `logout()` | ⚠️ 待实现 | edams-auth需补全 |
| POST /api/v1/auth/refresh | `refreshToken()` | ⚠️ 待实现 | edams-auth需补全 |
| GET /api/v1/auth/captcha | `getCaptcha()` | ⚠️ 待实现 | edams-auth需补全 |
| POST /api/v1/auth/mobile/send-code | `sendMobileCode()` | ⚠️ 待实现 | edams-auth需补全 |
| POST /api/v1/auth/mobile/verify | `verifyMobileCode()` | ⚠️ 待实现 | edams-auth需补全 |
| POST /api/v1/auth/register | `register()` | ⚠️ 待实现 | edams-auth需补全 |
| POST /api/v1/auth/password/reset | `resetPassword()` | ⚠️ 待实现 | edams-auth需补全 |
| POST /api/v1/auth/password/change | `changePassword()` | ⚠️ 待实现 | edams-auth需补全 |
| GET /api/v1/auth/mfa/status | `getMFAStatus()` | ⚠️ 待实现 | edams-auth需补全 |
| POST /api/v1/auth/mfa/enable | `enableMFA()` | ⚠️ 待实现 | edams-auth需补全 |
| POST /api/v1/auth/mfa/verify | `verifyMFACode()` | ⚠️ 待实现 | edams-auth需补全 |
| POST /api/v1/auth/mfa/disable | `disableMFA()` | ⚠️ 待实现 | edams-auth需补全 |
| GET /api/v1/auth/session | `getSession()` | ⚠️ 待实现 | edams-auth需补全 |
| DELETE /api/v1/auth/session | `destroySession()` | ⚠️ 待实现 | edams-auth需补全 |
| GET /api/v1/auth/sso/url | `getSSOUrl()` | ⚠️ 待实现 | edams-auth需补全 |
| POST /api/v1/auth/sso/callback | `handleSSOCallback()` | ⚠️ 待实现 | edams-auth需补全 |

**后端现状:** edams-auth服务仅有基础实体类，缺少Controller和Service实现。

---

### 2.2 用户服务 (User)

**前端文件:** `frontend/edams-web/src/services/user.ts`

| 接口 | 前端方法 | 后端状态 | 备注 |
|------|----------|----------|------|
| GET /api/v1/users | `getUserList()` | ⚠️ 待实现 | edams-user需补全 |
| GET /api/v1/users/{id} | `getUserDetail()` | ⚠️ 待实现 | edams-user需补全 |
| POST /api/v1/users | `createUser()` | ⚠️ 待实现 | edams-user需补全 |
| PUT /api/v1/users/{id} | `updateUser()` | ⚠️ 待实现 | edams-user需补全 |
| DELETE /api/v1/users/{id} | `deleteUser()` | ⚠️ 待实现 | edams-user需补全 |
| PUT /api/v1/users/{id}/enable | `enableUser()` | ⚠️ 待实现 | edams-user需补全 |
| PUT /api/v1/users/{id}/disable | `disableUser()` | ⚠️ 待实现 | edams-user需补全 |
| PUT /api/v1/users/{id}/roles | `assignRoles()` | ⚠️ 待实现 | edams-user需补全 |
| GET /api/v1/users/me/profile | `getMyProfile()` | ⚠️ 待实现 | edams-user需补全 |

**后端现状:** edams-user服务需要完整实现。

---

### 2.3 权限服务 (Permission)

**前端文件:** `frontend/edams-web/src/services/permission.ts`

| 接口 | 前端方法 | 后端状态 | 备注 |
|------|----------|----------|------|
| GET /api/v1/permissions | `getPermissionList()` | ⚠️ 待实现 | edams-permission需补全 |
| GET /api/v1/permissions/tree | `getPermissionTree()` | ⚠️ 待实现 | edams-permission需补全 |
| POST /api/v1/permissions | `createPermission()` | ⚠️ 待实现 | edams-permission需补全 |
| PUT /api/v1/permissions/{id} | `updatePermission()` | ⚠️ 待实现 | edams-permission需补全 |
| DELETE /api/v1/permissions/{id} | `deletePermission()` | ⚠️ 待实现 | edams-permission需补全 |

**后端现状:** edams-permission服务需要完整实现。

---

### 2.4 资产服务 (Asset)

**前端文件:** `frontend/edams-web/src/services/asset.ts`

| 接口 | 前端方法 | 后端状态 | 备注 |
|------|----------|----------|------|
| GET /api/v1/assets | `getAssetList()` | ⚠️ 待实现 | edams-asset需补全Controller |
| GET /api/v1/assets/{id} | `getAssetDetail()` | ⚠️ 待实现 | edams-asset需补全Controller |
| POST /api/v1/assets | `createAsset()` | ⚠️ 待实现 | edams-asset需补全Controller |
| PUT /api/v1/assets/{id} | `updateAsset()` | ⚠️ 待实现 | edams-asset需补全Controller |
| DELETE /api/v1/assets/{id} | `deleteAsset()` | ⚠️ 待实现 | edams-asset需补全Controller |
| GET /api/v1/assets/search | `searchAssets()` | ⚠️ 待实现 | edams-asset需补全Controller |

**后端现状:** edams-asset服务已有实体类和Kafka配置，缺少Controller和Service实现。

---

### 2.5 元数据服务 (Metadata) ✅

**前端文件:** `frontend/edams-web/src/services/metadata.ts`  
**后端文件:** `backend/domain/services/metadata-service/src/main/java/.../MetadataController.java`

| 接口 | 前端方法 | 后端状态 | 备注 |
|------|----------|----------|------|
| POST /api/v1/metadata/register | `registerMetadata()` | ✅ 已实现 | 完全匹配 |
| PUT /api/v1/metadata/{objectId} | `updateMetadata()` | ✅ 已实现 | 完全匹配 |
| DELETE /api/v1/metadata/{objectId} | `deleteMetadata()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/metadata/{objectId} | `getMetadataDetail()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/metadata/search | `searchMetadata()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/metadata/domain/{domainCode} | `getMetadataByDomain()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/metadata/stats | `getMetadataStats()` | ✅ 已实现 | 完全匹配 |

**状态:** 🟢 已就绪，可直接联调

---

### 2.6 血缘服务 (Lineage) ✅

**前端文件:** `frontend/edams-web/src/services/lineage.ts`  
**后端文件:** `backend/domain/services/lineage-service/src/main/java/.../LineageController.java`

| 接口 | 前端方法 | 后端状态 | 备注 |
|------|----------|----------|------|
| GET /api/v1/lineage/table/{assetId} | `getTableLineage()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/lineage/field/{assetId} | `getFieldLineage()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/lineage/graph | `getLineageGraph()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/lineage/impact/{assetId} | `getImpactAnalysis()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/lineage/trace/{assetId} | `getDependencyAnalysis()` | ✅ 已实现 | 完全匹配 |
| POST /api/v1/lineage | `createLineage()` | ✅ 已实现 | 完全匹配 |
| DELETE /api/v1/lineage/{lineageId} | `deleteLineage()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/lineage/statistics | `getLineageStatistics()` | ✅ 已实现 | 完全匹配 |

**状态:** 🟢 已就绪，可直接联调

---

### 2.7 质量服务 (Quality) ✅

**前端文件:** `frontend/edams-web/src/services/quality.ts`  
**后端文件:** `backend/domain/services/quality-service/src/main/java/.../QualityRuleController.java`

| 接口 | 前端方法 | 后端状态 | 备注 |
|------|----------|----------|------|
| GET /api/v1/quality/rules | `getQualityRules()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/quality/rules/{id} | `getQualityRuleDetail()` | ✅ 已实现 | 完全匹配 |
| POST /api/v1/quality/rules | `createQualityRule()` | ✅ 已实现 | 完全匹配 |
| PUT /api/v1/quality/rules/{id} | `updateQualityRule()` | ✅ 已实现 | 完全匹配 |
| DELETE /api/v1/quality/rules/{id} | `deleteQualityRule()` | ✅ 已实现 | 完全匹配 |
| POST /api/v1/quality/rules/{id}/enable | `enableQualityRule()` | ✅ 已实现 | 完全匹配 |
| POST /api/v1/quality/rules/{id}/disable | `disableQualityRule()` | ✅ 已实现 | 完全匹配 |

**状态:** 🟢 已就绪，可直接联调

---

### 2.8 数据标准服务 (Standard) ✅

**前端文件:** `frontend/edams-web/src/services/standard.ts`  
**后端文件:** `backend/domain/services/standard-service/src/main/java/.../StandardController.java`

| 接口 | 前端方法 | 后端状态 | 备注 |
|------|----------|----------|------|
| GET /api/v1/standards | `getStandards()` | ✅ 已实现 | 完全匹配 |
| POST /api/v1/standards | `createStandard()` | ✅ 已实现 | 完全匹配 |
| PUT /api/v1/standards/{id} | `updateStandard()` | ✅ 已实现 | 完全匹配 |
| DELETE /api/v1/standards/{id} | `deleteStandard()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/standards/{id} | `getStandardDetail()` | ✅ 已实现 | 完全匹配 |
| POST /api/v1/standards/{id}/publish | `publishStandard()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/standards/active | `getActiveStandards()` | ✅ 已实现 | 完全匹配 |

**状态:** 🟢 已就绪，可直接联调

---

### 2.9 治理服务 (Governance) ✅

**前端文件:** `frontend/edams-web/src/services/governance.ts`  
**后端文件:** `backend/domain/services/governance-engine/src/main/java/.../GovernanceTaskController.java`

| 接口 | 前端方法 | 后端状态 | 备注 |
|------|----------|----------|------|
| GET /api/v1/governance/tasks | `getGovernanceTasks()` | ✅ 已实现 | 完全匹配 |
| POST /api/v1/governance/tasks | `createTask()` | ✅ 已实现 | 完全匹配 |
| PUT /api/v1/governance/tasks/{id} | `updateTask()` | ✅ 已实现 | 完全匹配 |
| DELETE /api/v1/governance/tasks/{id} | `deleteTask()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/governance/tasks/{id} | `getTaskDetail()` | ✅ 已实现 | 完全匹配 |
| POST /api/v1/governance/tasks/{id}/execute | `executeTask()` | ✅ 已实现 | 完全匹配 |
| GET /api/v1/governance/ai/recommendations | `getAiRecommendations()` | ✅ 已实现 | 完全匹配 |

**状态:** 🟢 已就绪，可直接联调

---

### 2.10 工作流服务 (Workflow)

**前端文件:** `frontend/edams-web/src/services/workflow.ts`

| 接口 | 前端方法 | 后端状态 | 备注 |
|------|----------|----------|------|
| GET /api/v1/process-tasks/todo | `getTodoTasks()` | ⚠️ 待实现 | edams-workflow需补全 |
| GET /api/v1/process-tasks/done | `getDoneTasks()` | ⚠️ 待实现 | edams-workflow需补全 |
| POST /api/v1/process-tasks/{taskId}/approve | `approveTask()` | ⚠️ 待实现 | edams-workflow需补全 |
| POST /api/v1/process-tasks/{taskId}/reject | `rejectTask()` | ⚠️ 待实现 | edams-workflow需补全 |

**后端现状:** edams-workflow服务需要完整实现。

---

### 2.11 通知服务 (Notification)

**前端文件:** `frontend/edams-web/src/services/notification.ts`

| 接口 | 前端方法 | 后端状态 | 备注 |
|------|----------|----------|------|
| GET /api/v1/notifications | `getNotificationList()` | ⚠️ 待实现 | edams-notification需补全 |
| POST /api/v1/notifications/{id}/read | `markNotificationAsRead()` | ⚠️ 待实现 | edams-notification需补全 |
| POST /api/v1/notifications/send | `sendNotification()` | ⚠️ 待实现 | edams-notification需补全 |

**后端现状:** edams-notification服务需要完整实现。

---

## 3. 路由配置

### 3.1 API网关路由 (edams-gateway)

```yaml
# 已配置的路由
- id: metadata-service
  uri: lb://metadata-service
  predicates: Path=/api/v1/metadata/**
  
- id: lineage-service
  uri: lb://lineage-service
  predicates: Path=/api/v1/lineage/**
  
- id: quality-service
  uri: lb://quality-service
  predicates: Path=/api/v1/quality/**
  
- id: standard-service
  uri: lb://standard-service
  predicates: Path=/api/v1/standards/**
  
- id: governance-engine
  uri: lb://governance-engine
  predicates: Path=/api/v1/governance/**

# 待配置的路由
- id: edams-auth (待实现)
- id: edams-user (待实现)
- id: edams-permission (待实现)
- id: edams-asset (待实现)
- id: edams-workflow (待实现)
- id: edams-notification (待实现)
```

---

## 4. 待办事项

### 4.1 后端开发任务 (需 @backend-core-lead 处理)

| 优先级 | 服务 | 任务 | 工作量估计 |
|--------|------|------|------------|
| 🔴 P0 | edams-auth | 实现AuthController及JWT认证逻辑 | 2-3天 |
| 🔴 P0 | edams-user | 实现UserController及用户管理 | 2天 |
| 🔴 P0 | edams-permission | 实现PermissionController及RBAC | 2天 |
| 🟡 P1 | edams-asset | 实现AssetController完整CRUD | 2天 |
| 🟡 P1 | edams-workflow | 实现ProcessTaskController | 2天 |
| 🟡 P1 | edams-notification | 实现NotificationController | 1-2天 |

### 4.2 前端优化任务

| 优先级 | 任务 | 描述 |
|--------|------|------|
| 🟢 P2 | 统一metadata.ts风格 | 当前使用默认导出，需改为命名导出与整体风格一致 |
| 🟢 P2 | 统一standard.ts风格 | 同上 |
| 🟢 P2 | 统一governance.ts风格 | 同上 |
| 🟢 P2 | 补充缺失的API调用 | 根据后端实际实现补充 |

---

## 5. 联调建议

### 5.1 第一阶段 (已就绪服务)
可直接开始联调的服务:
1. **元数据服务** - 完整的CRUD + 搜索
2. **血缘服务** - 图查询 + 影响分析
3. **质量服务** - 规则管理 + 检测
4. **数据标准** - 标准管理 + 发布
5. **治理服务** - 任务管理 + AI建议

### 5.2 第二阶段 (待后端完成后)
待后端核心服务完成后联调:
1. 认证服务 (登录/登出/Token刷新)
2. 用户服务 (用户管理)
3. 权限服务 (RBAC)
4. 资产服务 (资产管理)
5. 工作流 (审批流)
6. 通知服务 (消息推送)

---

## 6. 变更日志

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-04-11 | 1.0.0 | 初始版本，完成全量API契约梳理 |

---

## 7. 相关文档

- [API契约文档](./API_CONTRACT.md)
- [前端服务目录](../../frontend/edams-web/src/services/)
- [后端Controller目录](../../backend/)
- [网关路由配置](../../backend/core/edams-parent/edams-gateway/)
