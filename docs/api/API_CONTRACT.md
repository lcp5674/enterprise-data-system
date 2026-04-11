# EDAMS API契约文档

> 版本: 1.0.0  
> 更新日期: 2026-04-11  
> 文档状态: 前后端联调参考

## 1. 概述

本文档定义了EDAMS（企业数据资产管理系统）前后端API契约，包含所有服务端点的完整规范。

### 1.1 基础信息

| 项目 | 值 |
|------|-----|
| 基础URL | `http://localhost:8888` (开发环境) |
| API版本 | v1 |
| 内容类型 | `application/json` |
| 认证方式 | JWT Bearer Token |

### 1.2 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1712832000000,
  "traceId": "req_123456789"
}
```

### 1.3 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

---

## 2. 认证服务 (Auth)

### 2.1 基础路径
- 前缀: `/api/v1/auth`

### 2.2 接口清单

| 方法 | 路径 | 描述 | 前端Service |
|------|------|------|-------------|
| POST | `/login` | 用户登录 | `auth.login()` |
| POST | `/logout` | 用户登出 | `auth.logout()` |
| POST | `/refresh` | 刷新Token | `auth.refreshToken()` |
| GET | `/captcha` | 获取图形验证码 | `auth.getCaptcha()` |
| POST | `/mobile/send-code` | 发送手机验证码 | `auth.sendMobileCode()` |
| POST | `/mobile/verify` | 手机号验证登录 | `auth.verifyMobileCode()` |
| POST | `/register` | 用户注册 | `auth.register()` |
| POST | `/password/reset` | 重置密码 | `auth.resetPassword()` |
| POST | `/password/change` | 修改密码 | `auth.changePassword()` |
| GET | `/mfa/status` | 获取MFA状态 | `auth.getMFAStatus()` |
| POST | `/mfa/enable` | 启用MFA | `auth.enableMFA()` |
| POST | `/mfa/verify` | 验证MFA码 | `auth.verifyMFACode()` |
| POST | `/mfa/disable` | 禁用MFA | `auth.disableMFA()` |
| GET | `/session` | 获取当前会话 | `auth.getSession()` |
| DELETE | `/session` | 销毁指定会话 | `auth.destroySession()` |
| GET | `/sso/url` | 获取SSO登录URL | `auth.getSSOUrl()` |
| POST | `/sso/callback` | SSO回调处理 | `auth.handleSSOCallback()` |

---

## 3. 用户服务 (User)

### 3.1 基础路径
- 前缀: `/api/v1/users`

### 3.2 接口清单

| 方法 | 路径 | 描述 | 前端Service |
|------|------|------|-------------|
| GET | `/` | 获取用户列表 | `user.getUserList()` |
| GET | `/{id}` | 获取用户详情 | `user.getUserDetail()` |
| GET | `/username/{username}` | 根据用户名获取用户 | `user.getUserByUsername()` |
| POST | `/` | 创建用户 | `user.createUser()` |
| PUT | `/{id}` | 更新用户 | `user.updateUser()` |
| DELETE | `/{id}` | 删除用户 | `user.deleteUser()` |
| POST | `/batch-delete` | 批量删除用户 | `user.batchDeleteUsers()` |
| PUT | `/{id}/enable` | 启用用户 | `user.enableUser()` |
| PUT | `/{id}/disable` | 禁用用户 | `user.disableUser()` |
| PUT | `/{id}/reset-password` | 重置密码 | `user.resetUserPassword()` |
| PUT | `/{id}/roles` | 分配角色 | `user.assignRoles()` |
| GET | `/{id}/roles` | 获取用户角色 | `user.getUserRoles()` |
| GET | `/{id}/menus` | 获取用户菜单 | `user.getUserMenus()` |
| GET | `/{id}/permissions` | 获取用户权限 | `user.getUserPermissions()` |
| POST | `/{id}/unlock` | 解锁用户 | `user.unlockUser()` |
| POST | `/batch` | 批量创建用户 | `user.batchCreateUsers()` |
| GET | `/me/profile` | 获取当前用户资料 | `user.getMyProfile()` |
| PUT | `/me/profile` | 更新当前用户资料 | `user.updateMyProfile()` |
| GET | `/me/preferences` | 获取当前用户偏好 | `user.getMyPreferences()` |
| PUT | `/me/preferences` | 更新当前用户偏好 | `user.updateMyPreferences()` |
| GET | `/me/workbench` | 获取工作台数据 | `user.getMyWorkbench()` |
| GET | `/me/recent` | 获取最近访问资产 | `user.getRecentAssets()` |
| GET | `/me/favorites` | 获取收藏资产 | `user.getFavoriteAssets()` |
| GET | `/check/username` | 检查用户名 | `user.checkUsername()` |
| GET | `/check/email` | 检查邮箱 | `user.checkEmail()` |

---

## 4. 权限服务 (Permission)

### 4.1 基础路径
- 前缀: `/api/v1/permissions`

### 4.2 接口清单

| 方法 | 路径 | 描述 | 前端Service |
|------|------|------|-------------|
| GET | `/` | 获取权限列表 | `permission.getPermissionList()` |
| GET | `/{id}` | 获取权限详情 | `permission.getPermissionDetail()` |
| GET | `/code/{code}` | 根据编码获取权限 | `permission.getPermissionByCode()` |
| POST | `/` | 创建权限 | `permission.createPermission()` |
| PUT | `/{id}` | 更新权限 | `permission.updatePermission()` |
| DELETE | `/{id}` | 删除权限 | `permission.deletePermission()` |
| GET | `/all` | 获取所有权限 | `permission.getAllPermissions()` |
| GET | `/tree` | 获取权限树 | `permission.getPermissionTree()` |
| GET | `/module/{module}` | 根据模块获取权限 | `permission.getPermissionsByModule()` |
| GET | `/type/{type}` | 根据类型获取权限 | `permission.getPermissionsByType()` |
| GET | `/check/code` | 检查权限编码 | `permission.checkPermissionCode()` |
| GET | `/role/{roleId}` | 获取角色的权限 | `permission.getRolePermissions()` |
| GET | `/user/{userId}` | 获取用户的权限 | `permission.getUserPermissions()` |
| GET | `/user/{userId}/codes` | 获取用户的权限编码 | `permission.getUserPermissionCodes()` |
| GET | `/user/{userId}/check/{code}` | 检查用户权限 | `permission.checkUserPermission()` |
| GET | `/audit/logs` | 获取权限审计日志 | `permission.getPermissionAuditLogs()` |
| GET | `/audit/report` | 获取权限审计报告 | `permission.getPermissionAuditReport()` |

---

## 5. 资产服务 (Asset)

### 5.1 基础路径
- 前缀: `/api/v1/assets`

### 5.2 接口清单

| 方法 | 路径 | 描述 | 前端Service |
|------|------|------|-------------|
| GET | `/` | 查询资产列表 | `asset.getAssetList()` |
| GET | `/{id}` | 获取资产详情 | `asset.getAssetDetail()` |
| POST | `/` | 创建资产 | `asset.createAsset()` |
| PUT | `/{id}` | 更新资产 | `asset.updateAsset()` |
| DELETE | `/{id}` | 删除资产 | `asset.deleteAsset()` |
| POST | `/{id}/restore` | 恢复已删除资产 | `asset.restoreAsset()` |
| GET | `/search` | 全文搜索资产 | `asset.searchAssets()` |
| POST | `/advanced-search` | 高级搜索 | `asset.advancedSearch()` |
| GET | `/suggest` | 搜索联想 | `asset.getAssetSuggestions()` |
| GET | `/recommend` | 智能推荐资产 | `asset.getAssetRecommendations()` |
| GET | `/{id}/fields` | 获取资产字段列表 | `asset.getAssetFields()` |
| POST | `/{id}/fields` | 添加资产字段 | `asset.addAssetField()` |
| PUT | `/{id}/fields/{fieldId}` | 更新资产字段 | `asset.updateAssetField()` |
| DELETE | `/{id}/fields/{fieldId}` | 删除资产字段 | `asset.deleteAssetField()` |
| POST | `/{id}/favorite` | 收藏资产 | `asset.favoriteAsset()` |
| DELETE | `/{id}/favorite` | 取消收藏资产 | `asset.unfavoriteAsset()` |
| POST | `/{id}/rating` | 评价资产 | `asset.rateAsset()` |
| GET | `/{id}/ratings` | 获取资产评价列表 | `asset.getAssetRatings()` |
| PUT | `/{id}/sensitivity` | 更新资产敏感等级 | `asset.updateAssetSensitivity()` |
| POST | `/{id}/lifecycle/deprecate` | 废弃资产 | `asset.deprecateAsset()` |
| POST | `/{id}/lifecycle/restore` | 恢复废弃资产 | `asset.restoreDeprecatedAsset()` |
| POST | `/{id}/lifecycle/archive` | 归档资产 | `asset.archiveAsset()` |
| POST | `/{id}/certification` | 提交资产认证 | `asset.submitCertification()` |
| POST | `/{id}/certification/approve` | 审批资产认证 | `asset.approveCertification()` |
| POST | `/{id}/certification/reject` | 驳回资产认证 | `asset.rejectCertification()` |
| GET | `/{id}/sensitive/fields` | 获取资产敏感字段 | `asset.getSensitiveFields()` |
| GET | `/{id}/quality` | 获取资产质量报告 | `asset.getAssetQuality()` |
| GET | `/{id}/quality/trend` | 获取质量趋势 | `asset.getQualityTrend()` |

---

## 6. 元数据服务 (Metadata)

### 6.1 基础路径
- 前缀: `/api/v1/metadata` (网关路由) 或 `/metadata/api/v1` (直接访问)

### 6.2 接口清单

| 方法 | 路径 | 描述 | 前端Service |
|------|------|------|-------------|
| POST | `/register` | 注册元数据 | `metadata.registerMetadata()` |
| PUT | `/{objectId}` | 更新元数据 | `metadata.updateMetadata()` |
| DELETE | `/{objectId}` | 删除元数据 | `metadata.deleteMetadata()` |
| GET | `/{objectId}` | 获取元数据详情 | `metadata.getMetadataDetail()` |
| GET | `/search` | 搜索元数据 | `metadata.searchMetadata()` |
| GET | `/domain/{domainCode}` | 按域查询 | `metadata.getMetadataByDomain()` |
| GET | `/stats` | 获取统计 | `metadata.getMetadataStats()` |
| POST | `/sync/{assetId}` | 同步资产元数据 | `metadata.syncFromAsset()` |
| GET | `/domains` | 获取领域列表 | `metadata.getDomains()` |
| GET | `/types` | 获取对象类型列表 | `metadata.getObjectTypes()` |
| GET | `/{objectId}/history` | 获取变更历史 | `metadata.getMetadataHistory()` |
| GET | `/{objectId}/lineage` | 获取元数据血缘 | `metadata.getMetadataLineage()` |
| POST | `/{objectId}/tags` | 添加标签 | `metadata.addMetadataTags()` |
| DELETE | `/{objectId}/tags/{tag}` | 删除标签 | `metadata.removeMetadataTag()` |
| POST | `/export` | 导出元数据 | `metadata.exportMetadata()` |
| POST | `/import` | 导入元数据 | `metadata.importMetadata()` |
| GET | `/templates/{type}` | 获取模板 | `metadata.getMetadataTemplate()` |
| GET | `/search/popular` | 获取热门搜索 | `metadata.getPopularSearches()` |
| GET | `/search/suggestions` | 获取搜索建议 | `metadata.getSearchSuggestions()` |

---

## 7. 血缘服务 (Lineage)

### 7.1 基础路径
- 前缀: `/api/v1/lineage`

### 7.2 接口清单

| 方法 | 路径 | 描述 | 前端Service |
|------|------|------|-------------|
| GET | `/table/{assetId}` | 获取表级血缘 | `lineage.getTableLineage()` |
| GET | `/field/{assetId}` | 获取字段级血缘 | `lineage.getFieldLineage()` |
| GET | `/path` | 查询血缘路径 | `lineage.getLineagePath()` |
| GET | `/graph` | 获取血缘图数据 | `lineage.getLineageGraph()` |
| GET | `/impact/{assetId}` | 影响分析 | `lineage.getImpactAnalysis()` |
| GET | `/dependency/{assetId}` | 追溯分析 | `lineage.getDependencyAnalysis()` |
| POST | `/` | 创建血缘关系 | `lineage.createLineage()` |
| DELETE | `/{lineageId}` | 删除血缘关系 | `lineage.deleteLineage()` |
| POST | `/verify` | 验证血缘关系 | `lineage.verifyLineage()` |
| GET | `/statistics` | 获取血缘统计 | `lineage.getLineageStatistics()` |
| GET | `/{assetId}/history` | 获取血缘变更历史 | `lineage.getLineageHistory()` |
| GET | `/compare` | 对比血缘差异 | `lineage.compareLineage()` |

---

## 8. 质量服务 (Quality)

### 8.1 基础路径
- 前缀: `/api/v1/quality`

### 8.2 接口清单

| 方法 | 路径 | 描述 | 前端Service |
|------|------|------|-------------|
| GET | `/rules` | 获取质量规则列表 | `quality.getQualityRules()` |
| GET | `/rules/{id}` | 获取质量规则详情 | `quality.getQualityRuleDetail()` |
| POST | `/rules` | 创建质量规则 | `quality.createQualityRule()` |
| PUT | `/rules/{id}` | 更新质量规则 | `quality.updateQualityRule()` |
| DELETE | `/rules/{id}` | 删除质量规则 | `quality.deleteQualityRule()` |
| POST | `/rules/{id}/enable` | 启用质量规则 | `quality.enableQualityRule()` |
| POST | `/rules/{id}/disable` | 禁用质量规则 | `quality.disableQualityRule()` |
| GET | `/rules/templates` | 获取规则模板 | `quality.getRuleTemplates()` |
| POST | `/check` | 触发质量检测 | `quality.triggerQualityCheck()` |
| POST | `/check/batch` | 批量触发检测 | `quality.batchTriggerQualityCheck()` |
| GET | `/check/{checkId}` | 获取检测结果 | `quality.getCheckResult()` |
| GET | `/check/{checkId}/progress` | 获取检测进度 | `quality.getCheckProgress()` |
| GET | `/overview` | 获取质量概览 | `quality.getQualityOverview()` |
| GET | `/issues` | 查询质量问题列表 | `quality.getQualityIssues()` |
| GET | `/issues/{id}` | 获取质量问题详情 | `quality.getQualityIssueDetail()` |
| PUT | `/issues/{id}` | 更新质量问题 | `quality.updateQualityIssue()` |
| POST | `/issues/{id}/resolve` | 解决问题 | `quality.resolveQualityIssue()` |
| POST | `/issues/{id}/close` | 关闭问题 | `quality.closeQualityIssue()` |
| POST | `/issues/{id}/transfer` | 转移问题 | `quality.transferQualityIssue()` |
| POST | `/issues/{id}/ignore` | 忽略问题 | `quality.ignoreQualityIssue()` |
| GET | `/issues/statistics` | 获取问题统计 | `quality.getIssueStatistics()` |

---

## 9. 数据标准服务 (Standard)

### 9.1 基础路径
- 前缀: `/api/v1/standards` (网关路由) 或 `/standard/api/v1/standards` (直接访问)

### 9.2 接口清单

| 方法 | 路径 | 描述 | 前端Service |
|------|------|------|-------------|
| GET | `/` | 获取标准列表 | `standard.getStandards()` |
| POST | `/` | 创建标准 | `standard.createStandard()` |
| PUT | `/{id}` | 更新标准 | `standard.updateStandard()` |
| DELETE | `/{id}` | 删除标准 | `standard.deleteStandard()` |
| GET | `/{id}` | 获取标准详情 | `standard.getStandardDetail()` |
| POST | `/{id}/publish` | 发布标准 | `standard.publishStandard()` |
| GET | `/code/{standardCode}` | 根据编码查询 | `standard.getStandardByCode()` |
| GET | `/active` | 查询激活的标准 | `standard.getActiveStandards()` |
| GET | `/categories` | 获取标准分类树 | `standard.getStandardCategories()` |
| POST | `/import` | 导入标准 | `standard.importStandards()` |
| POST | `/export` | 导出标准 | `standard.exportStandards()` |

---

## 10. 治理服务 (Governance)

### 10.1 基础路径
- 前缀: `/api/v1/governance` 或 `/api/governance` (注意版本差异)

### 10.2 接口清单

| 方法 | 路径 | 描述 | 前端Service |
|------|------|------|-------------|
| GET | `/tasks` | 获取治理任务列表 | `governance.getGovernanceTasks()` |
| POST | `/tasks` | 创建治理任务 | `governance.createTask()` |
| PUT | `/tasks/{id}` | 更新治理任务 | `governance.updateTask()` |
| DELETE | `/tasks/{id}` | 删除治理任务 | `governance.deleteTask()` |
| GET | `/tasks/{id}` | 获取任务详情 | `governance.getTaskDetail()` |
| POST | `/tasks/{id}/execute` | 执行任务 | `governance.executeTask()` |
| POST | `/tasks/{id}/stop` | 停止任务 | `governance.stopTask()` |
| GET | `/reports` | 获取治理报告 | `governance.getGovernanceReport()` |
| GET | `/ai/recommendations` | 获取AI治理建议 | `governance.getAiRecommendations()` |
| POST | `/ai/recommendations/{id}/apply` | 应用AI建议 | `governance.applyAiRecommendation()` |
| POST | `/ai/recommendations/{id}/dismiss` | 忽略AI建议 | `governance.dismissAiRecommendation()` |
| GET | `/rules` | 获取治理规则列表 | `governance.getGovernanceRules()` |
| POST | `/rules` | 创建治理规则 | `governance.createGovernanceRule()` |
| GET | `/metrics` | 获取治理指标 | `governance.getGovernanceMetrics()` |

---

## 11. 工作流服务 (Workflow)

### 11.1 基础路径
- 前缀: `/api/v1/workflows` 或 `/api/v1/process-tasks`

### 11.2 接口清单

| 方法 | 路径 | 描述 | 前端Service |
|------|------|------|-------------|
| GET | `/process-tasks/todo` | 获取待办任务 | `workflow.getTodoTasks()` |
| GET | `/process-tasks/done` | 获取已办任务 | `workflow.getDoneTasks()` |
| GET | `/process-tasks/cc` | 获取抄送任务 | `workflow.getCcTasks()` |
| GET | `/process-tasks/{taskId}` | 获取任务详情 | `workflow.getTaskDetail()` |
| POST | `/process-tasks/{taskId}/approve` | 审批通过任务 | `workflow.approveTask()` |
| POST | `/process-tasks/{taskId}/reject` | 审批拒绝任务 | `workflow.rejectTask()` |
| POST | `/process-tasks/{taskId}/back` | 退回任务 | `workflow.backTask()` |
| POST | `/process-tasks/{taskId}/transfer` | 转办任务 | `workflow.transferTask()` |
| POST | `/process-tasks/{taskId}/delegate` | 委托任务 | `workflow.delegateTask()` |
| POST | `/process-tasks/{taskId}/remind` | 发送任务提醒 | `workflow.sendTaskReminder()` |
| POST | `/process-tasks/batch-approve` | 批量审批 | `workflow.batchApprove()` |
| GET | `/workflows/{instanceId}` | 获取流程实例 | `workflow.getProcessInstance()` |
| POST | `/workflows/{instanceId}/approve` | 审批流程 | `workflow.approveWorkflow()` |
| POST | `/workflows/{instanceId}/reject` | 拒绝流程 | `workflow.rejectWorkflow()` |
| GET | `/workflows/{instanceId}/history` | 获取流程历史 | `workflow.getProcessHistory()` |
| GET | `/workflows/approvals` | 获取我的审批列表 | `workflow.getMyApprovals()` |

---

## 12. 通知服务 (Notification)

### 12.1 基础路径
- 前缀: `/api/v1/notifications`

### 12.2 接口清单

| 方法 | 路径 | 描述 | 前端Service |
|------|------|------|-------------|
| GET | `/` | 获取通知列表 | `notification.getNotificationList()` |
| GET | `/{id}` | 获取通知详情 | `notification.getNotificationDetail()` |
| POST | `/{id}/read` | 标记通知为已读 | `notification.markNotificationAsRead()` |
| POST | `/batch/read` | 批量标记已读 | `notification.batchMarkAsRead()` |
| DELETE | `/{id}` | 删除通知 | `notification.deleteNotification()` |
| GET | `/templates` | 获取通知模板 | `notification.getNotificationTemplates()` |
| GET | `/subscriptions` | 获取订阅列表 | `notification.getSubscriptions()` |
| POST | `/send` | 发送通知 | `notification.sendNotification()` |
| POST | `/send/batch` | 批量发送通知 | `notification.batchSendNotification()` |
| POST | `/send/in-app` | 发送站内消息 | `notification.sendInAppMessage()` |
| POST | `/send/email` | 发送邮件 | `notification.sendEmail()` |
| POST | `/send/sms` | 发送短信 | `notification.sendSms()` |
| POST | `/send/template` | 使用模板发送 | `notification.sendTemplateNotification()` |

---

## 13. 数据源服务 (Datasource)

### 13.1 基础路径
- 前缀: `/api/v1/datasources`

### 13.2 接口清单

| 方法 | 路径 | 描述 | 前端Service |
|------|------|------|-------------|
| GET | `/` | 获取数据源列表 | `datasource.getDatasourceList()` |
| GET | `/{id}` | 获取数据源详情 | `datasource.getDatasourceDetail()` |
| POST | `/` | 创建数据源 | `datasource.createDatasource()` |
| PUT | `/{id}` | 更新数据源 | `datasource.updateDatasource()` |
| DELETE | `/{id}` | 删除数据源 | `datasource.deleteDatasource()` |
| POST | `/{id}/test` | 测试连接 | `datasource.testConnection()` |
| POST | `/{id}/sync` | 同步元数据 | `datasource.syncMetadata()` |
| GET | `/{id}/sync/status` | 获取同步状态 | `datasource.getSyncStatus()` |
| GET | `/{id}/tables` | 获取表列表 | `datasource.getTables()` |
| GET | `/{id}/tables/{database}/{tableName}` | 获取表结构 | `datasource.getTableStructure()` |

---

## 14. 附录

### 14.1 HTTP状态码

| 状态码 | 含义 |
|--------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 204 | 无内容（删除成功） |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效） |
| 403 | 禁止访问（无权限） |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |
| 502 | 网关错误 |
| 503 | 服务暂不可用 |
| 504 | 网关超时 |

### 14.2 错误码

| 错误码 | 含义 |
|--------|------|
| 0 | 成功 |
| 10001 | Token无效 |
| 10002 | Token过期 |
| 10003 | 无权限 |
| 20001 | 资源不存在 |
| 20002 | 资源已存在 |
| 30001 | 参数格式错误 |
| 40001 | 业务逻辑错误 |
| 50001 | 服务不可用 |

### 14.3 文档维护

- 前端服务文件: `frontend/edams-web/src/services/*.ts`
- 后端Controller: `backend/**/controller/*Controller.java`
- API常量: `frontend/edams-web/src/constants/index.ts`
- 类型定义: `frontend/edams-web/src/types/index.ts`
