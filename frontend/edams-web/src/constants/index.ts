/**
 * API 常量定义
 */

// API 基础配置
export const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8888';
export const API_TIMEOUT = parseInt(process.env.API_TIMEOUT || '30000', 10);

// API 版本
export const API_VERSION = 'v1';

// API 路径
export const API_PATHS = {
  // SSO相关
  SSO: {
    LOGIN: '/api/v1/sso/login',
    CALLBACK: '/api/v1/sso/callback',
    USERINFO: '/api/v1/sso/userinfo',
    LOGOUT: '/api/v1/sso/logout',
    REFRESH: '/api/v1/sso/refresh',
    VALIDATE: '/api/v1/sso/validate',
    REALMS: '/api/v1/sso/realms',
    HEALTH: '/api/v1/sso/health',
    PROVIDERS: '/api/v1/sso/providers',
    BIND: '/api/v1/sso/bind',
    UNBIND: '/api/v1/sso/unbind',
  },

  // 认证相关
  AUTH: {
    LOGIN: '/api/v1/auth/login',
    LOGOUT: '/api/v1/auth/logout',
    REFRESH: '/api/v1/auth/refresh',
    CAPTCHA: '/api/v1/auth/captcha',
    MFA_STATUS: '/api/v1/auth/mfa/status',
    MFA_ENABLE: '/api/v1/auth/mfa/enable',
    MFA_VERIFY: '/api/v1/auth/mfa/verify',
    MFA_DISABLE: '/api/v1/auth/mfa/disable',
    SSO_URL: '/api/v1/sso/login',
    SSO_CALLBACK: '/api/v1/sso/callback',
    SESSION: '/api/v1/auth/session',
    REGISTER: '/api/v1/auth/register',
    RESET_PASSWORD: '/api/v1/auth/password',
    CHANGE_PASSWORD: '/api/v1/auth/password',
    MOBILE_SEND_CODE: '/api/v1/auth/mobile/send-code',
    MOBILE_VERIFY: '/api/v1/auth/mobile/verify',
  },

  // 用户相关
  USER: {
    LIST: '/api/v1/users',
    DETAIL: (id: string) => `/api/v1/users/${id}`,
    CREATE: '/api/v1/users',
    UPDATE: (id: string) => `/api/v1/users/${id}`,
    DELETE: (id: string) => `/api/v1/users/${id}`,
    ENABLE: (id: string) => `/api/v1/users/${id}/enable`,
    DISABLE: (id: string) => `/api/v1/users/${id}/disable`,
    RESET_PASSWORD: (id: string) => `/api/v1/users/${id}/reset-password`,
    ASSIGN_ROLES: (id: string) => `/api/v1/users/${id}/roles`,
    PERMISSIONS: (id: string) => `/api/v1/users/${id}/permissions`,
    UNLOCK: (id: string) => `/api/v1/users/${id}/unlock`,
    BATCH_CREATE: '/api/v1/users/batch',
    ME_PROFILE: '/api/v1/users/me/profile',
    ME_PREFERENCES: '/api/v1/users/me/preferences',
    ME_WORKBENCH: '/api/v1/users/me/workbench',
    ME_RECENT: '/api/v1/users/me/recent',
    ME_FAVORITES: '/api/v1/users/me/favorites',
  },

  // 部门相关
  DEPARTMENT: {
    LIST: '/api/v1/departments',
    TREE: '/api/v1/departments',
    DETAIL: (id: string) => `/api/v1/departments/${id}`,
    CREATE: '/api/v1/departments',
    UPDATE: (id: string) => `/api/v1/departments/${id}`,
    DELETE: (id: string) => `/api/v1/departments/${id}`,
    USERS: (id: string) => `/api/v1/departments/${id}/users`,
    SUBTREE: (id: string) => `/api/v1/departments/${id}/tree`,
    ASSETS: (id: string) => `/api/v1/departments/${id}/assets`,
  },

  // 角色相关
  ROLE: {
    LIST: '/api/v1/roles',
    DETAIL: (id: string) => `/api/v1/roles/${id}`,
    CREATE: '/api/v1/roles',
    UPDATE: (id: string) => `/api/v1/roles/${id}`,
    DELETE: (id: string) => `/api/v1/roles/${id}`,
    PERMISSIONS: (id: string) => `/api/v1/roles/${id}/permissions`,
    UPDATE_PERMISSIONS: (id: string) => `/api/v1/roles/${id}/permissions`,
    USERS: (id: string) => `/api/v1/roles/${id}/users`,
    TREE: '/api/v1/roles/tree',
  },

  // 权限相关
  PERMISSION: {
    LIST: '/api/v1/permissions',
    TREE: '/api/v1/permissions/tree',
    AUDIT_LOGS: '/api/v1/permissions/audit/logs',
    AUDIT_REPORT: '/api/v1/permissions/audit/report',
  },

  // 资产相关
  ASSET: {
    LIST: '/api/v1/assets',
    DETAIL: (id: string) => `/api/v1/assets/${id}`,
    CREATE: '/api/v1/assets',
    UPDATE: (id: string) => `/api/v1/assets/${id}`,
    DELETE: (id: string) => `/api/v1/assets/${id}`,
    RESTORE: (id: string) => `/api/v1/assets/${id}/restore',
    BATCH: '/api/v1/assets/batch',
    SEARCH: '/api/v1/assets/search',
    ADVANCED_SEARCH: '/api/v1/assets/advanced-search',
    SUGGEST: '/api/v1/assets/suggest',
    RECOMMEND: '/api/v1/assets/recommend',
    STATISTICS: {
      OVERVIEW: '/api/v1/assets/statistics/overview',
      TREND: '/api/v1/assets/statistics/trend',
      DISTRIBUTION: '/api/v1/assets/statistics/distribution',
      QUALITY: '/api/v1/assets/statistics/quality',
    },
    IMPORT: {
      TEMPLATE: '/api/v1/assets/import/template',
      UPLOAD: '/api/v1/assets/import',
      STATUS: (taskId: string) => `/api/v1/assets/import/${taskId}`,
      ERRORS: (taskId: string) => `/api/v1/assets/import/${taskId}/errors`,
    },
    METADATA: (id: string) => `/api/v1/assets/${id}/metadata`,
    FIELDS: (id: string) => `/api/v1/assets/${id}/fields`,
    ADD_FIELD: (id: string) => `/api/v1/assets/${id}/fields`,
    UPDATE_FIELD: (id: string, fieldId: string) => `/api/v1/assets/${id}/fields/${fieldId}`,
    DELETE_FIELD: (id: string, fieldId: string) => `/api/v1/assets/${id}/fields/${fieldId}`,
    FAVORITE: (id: string) => `/api/v1/assets/${id}/favorite`,
    RATING: (id: string) => `/api/v1/assets/${id}/rating`,
    RATINGS: (id: string) => `/api/v1/assets/${id}/ratings`,
    SENSITIVITY: (id: string) => `/api/v1/assets/${id}/sensitivity`,
    SENSITIVE_FIELDS: (assetId: string) => `/api/v1/assets/${assetId}/sensitive/fields`,
    QUALITY: (assetId: string) => `/api/v1/assets/${assetId}/quality`,
    QUALITY_TREND: (assetId: string) => `/api/v1/assets/${assetId}/quality/trend`,
    RULES: (assetId: string) => `/api/v1/assets/${assetId}/rules`,
    ADD_RULES: (assetId: string) => `/api/v1/assets/${assetId}/rules`,
    REMOVE_RULES: (assetId: string, ruleConfigId: string) => 
      `/api/v1/assets/${assetId}/rules/${ruleConfigId}`,
    LIFECYCLE: {
      DEPRECATE: (id: string) => `/api/v1/assets/${id}/lifecycle/deprecate`,
      RESTORE: (id: string) => `/api/v1/assets/${id}/lifecycle/restore`,
      ARCHIVE: (id: string) => `/api/v1/assets/${id}/lifecycle/archive`,
    },
    CERTIFICATION: {
      APPLY: (id: string) => `/api/v1/assets/${id}/certification`,
      APPROVE: (id: string) => `/api/v1/assets/${id}/certification/approve`,
      REJECT: (id: string) => `/api/v1/assets/${id}/certification/reject`,
    },
    PERMISSIONS: {
      LIST: (assetId: string) => `/api/v1/assets/${assetId}/permissions`,
      SET: (assetId: string) => `/api/v1/assets/${assetId}/permissions`,
      DELETE: (assetId: string, permissionId: string) => 
        `/api/v1/assets/${assetId}/permissions/${permissionId}`,
      CHECK: (assetId: string) => `/api/v1/assets/${assetId}/permissions/check`,
    },
  },

  // 血缘相关
  LINEAGE: {
    TABLE: (assetId: string) => `/api/v1/lineage/table/${assetId}`,
    FIELD: (assetId: string) => `/api/v1/lineage/field/${assetId}`,
    PATH: '/api/v1/lineage/path',
    GRAPH: '/api/v1/lineage/graph',
    IMPACT: (assetId: string) => `/api/v1/lineage/impact/${assetId}`,
    DEPENDENCY: (assetId: string) => `/api/v1/lineage/dependency/${assetId}`,
    CREATE: '/api/v1/lineage',
    DELETE: (lineageId: string) => `/api/v1/lineage/${lineageId}`,
    VERIFY: '/api/v1/lineage/verify',
    STATISTICS: '/api/v1/lineage/statistics',
    HISTORY: (assetId: string) => `/api/v1/lineage/${assetId}/history`,
    COMPARE: '/api/v1/lineage/compare',
  },

  // 质量相关
  QUALITY: {
    RULES: {
      LIST: '/api/v1/quality/rules',
      DETAIL: (id: string) => `/api/v1/quality/rules/${id}`,
      CREATE: '/api/v1/quality/rules',
      UPDATE: (id: string) => `/api/v1/quality/rules/${id}`,
      DELETE: (id: string) => `/api/v1/quality/rules/${id}`,
      ENABLE: (id: string) => `/api/v1/quality/rules/${id}/enable`,
      DISABLE: (id: string) => `/api/v1/quality/rules/${id}/disable`,
      TEMPLATES: '/api/v1/quality/rules/templates',
    },
    CHECK: {
      TRIGGER: '/api/v1/quality/check',
      BATCH: '/api/v1/quality/check/batch',
      RESULT: (checkId: string) => `/api/v1/quality/check/${checkId}`,
      PROGRESS: (checkId: string) => `/api/v1/quality/check/${checkId}/progress`,
    },
    ISSUES: {
      LIST: '/api/v1/quality/issues',
      DETAIL: (id: string) => `/api/v1/quality/issues/${id}`,
      UPDATE: (id: string) => `/api/v1/quality/issues/${id}`,
      RESOLVE: (id: string) => `/api/v1/quality/issues/${id}/resolve`,
      CLOSE: (id: string) => `/api/v1/quality/issues/${id}/close`,
      TRANSFER: (id: string) => `/api/v1/quality/issues/${id}/transfer`,
      IGNORE: (id: string) => `/api/v1/quality/issues/${id}/ignore`,
      STATISTICS: '/api/v1/quality/issues/statistics',
    },
  },

  // 数据源相关
  DATASOURCE: {
    LIST: '/api/v1/datasources',
    DETAIL: (id: string) => `/api/v1/datasources/${id}`,
    CREATE: '/api/v1/datasources',
    UPDATE: (id: string) => `/api/v1/datasources/${id}`,
    DELETE: (id: string) => `/api/v1/datasources/${id}`,
    TEST: (id: string) => `/api/v1/datasources/${id}/test`,
    SYNC: (id: string) => `/api/v1/datasources/${id}/sync`,
    SYNC_STATUS: (id: string) => `/api/v1/datasources/${id}/sync/status`,
    TABLES: (id: string) => `/api/v1/datasources/${id}/tables`,
    TABLE_STRUCTURE: (id: string, database: string, tableName: string) => 
      `/api/v1/datasources/${id}/tables/${database}/${tableName}`,
  },

  // 通知相关
  NOTIFICATION: {
    LIST: '/api/v1/notifications',
    DETAIL: (id: string) => `/api/v1/notifications/${id}`,
    MARK_READ: (id: string) => `/api/v1/notifications/${id}/read`,
    BATCH_MARK_READ: '/api/v1/notifications/batch/read',
    DELETE: (id: string) => `/api/v1/notifications/${id}/delete',
    TEMPLATES: '/api/v1/notification-templates',
    SUBSCRIPTIONS: '/api/v1/subscriptions',
  },

  // 统计相关
  STATISTICS: {
    OVERVIEW: '/api/v1/statistics/overview',
    TREND: '/api/v1/statistics/trend',
    DISTRIBUTION: '/api/v1/statistics/distribution',
  },

  // 权限申请相关
  PERMISSION_REQUEST: {
    LIST: '/api/v1/permission-requests',
    DETAIL: (id: string) => `/api/v1/permission-requests/${id}`,
    CREATE: '/api/v1/permission-requests',
    APPROVE: (id: string) => `/api/v1/permission-requests/${id}/approve`,
    REJECT: (id: string) => `/api/v1/permission-requests/${id}/reject',
    MY_LIST: '/api/v1/users/me/permission-requests',
  },

  // 知识图谱相关
  KNOWLEDGE: {
    ONTOLOGIES: {
      LIST: '/api/v1/knowledge/ontologies',
      DETAIL: (id: string) => `/api/v1/knowledge/ontologies/${id}`,
      CREATE: '/api/v1/knowledge/ontologies',
    },
    ENTITIES: {
      LIST: '/api/v1/knowledge/entities',
      DETAIL: (id: string) => `/api/v1/knowledge/entities/${id}`,
      CREATE: '/api/v1/knowledge/entities',
    },
    GRAPH: '/api/v1/knowledge/graph',
    PATH: '/api/v1/knowledge/path',
  },

  // 安全相关
  SECURITY: {
    LEVELS: {
      LIST: '/api/v1/security/levels',
      DETAIL: (id: string) => `/api/v1/security/levels/${id}`,
      CREATE: '/api/v1/security/levels',
      UPDATE: (id: string) => `/api/v1/security/levels/${id}`,
      DELETE: (id: string) => `/api/v1/security/levels/${id}`,
    },
    SENSITIVE_PATTERNS: '/api/v1/security/sensitive/patterns',
    SENSITIVE_SCAN: '/api/v1/security/sensitive/scan',
    MASKING_RULES: {
      LIST: '/api/v1/security/masking/rules',
      DETAIL: (id: string) => `/api/v1/security/masking/rules/${id}`,
      CREATE: '/api/v1/security/masking/rules',
      UPDATE: (id: string) => `/api/v1/security/masking/rules/${id}`,
      DELETE: (id: string) => `/api/v1/security/masking/rules/${id}`,
      PREVIEW: '/api/v1/security/masking/preview',
    },
    AUDIT_LOGS: '/api/v1/security/audit/logs',
    COMPLIANCE_REPORT: '/api/v1/security/compliance/report',
  },

  // 工作流相关
  WORKFLOW: {
    APPROVALS: '/api/v1/workflows/approvals',
    DETAIL: (instanceId: string) => `/api/v1/workflows/${instanceId}`,
    APPROVE: (instanceId: string) => `/api/v1/workflows/${instanceId}/approve`,
    REJECT: (instanceId: string) => `/api/v1/workflows/${instanceId}/reject`,
    HISTORY: (instanceId: string) => `/api/v1/workflows/${instanceId}/history`,
  },

  // 治理相关
  GOVERNANCE: {
    TASKS: {
      LIST: '/api/v1/governance/tasks',
      EXECUTE: (id: string) => `/api/v1/governance/tasks/${id}/execute`,
      SUB_TASKS: (id: string) => `/api/v1/governance/tasks/${id}/sub-tasks`,
    },
    MATURITY: {
      CURRENT: '/api/v1/governance/maturity',
      HISTORY: '/api/v1/governance/maturity/history',
    },
    INCENTIVES: {
      POINTS: '/api/v1/governance/incentives/points',
      MY_PROFILE: '/api/v1/users/me/incentives/profile',
    },
  },

  // 价值评估相关
  VALUE: {
    ASSESSMENTS: '/api/v1/value/assessments',
    ASSET_ASSESS: (assetId: string) => `/api/v1/assets/${assetId}/value/assess`,
    ASSET_VALUE: (assetId: string) => `/api/v1/assets/${assetId}/value`,
    PRODUCTS: '/api/v1/value/products',
    ROI_REPORT: '/api/v1/value/roi/report',
  },

  // Webhook相关
  WEBHOOK: {
    LIST: '/api/v1/webhooks',
    DETAIL: (id: string) => `/api/v1/webhooks/${id}`,
    CREATE: '/api/v1/webhooks',
    UPDATE: (id: string) => `/api/v1/webhooks/${id}`,
    DELETE: (id: string) => `/api/v1/webhooks/${id}`,
    TEST: (id: string) => `/api/v1/webhooks/${id}/test`,
    DELIVERIES: (id: string) => `/api/v1/webhooks/${id}/deliveries`,
  },
};

// HTTP 状态码
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  ACCEPTED: 202,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  TOO_MANY_REQUESTS: 429,
  INTERNAL_SERVER_ERROR: 500,
  BAD_GATEWAY: 502,
  SERVICE_UNAVAILABLE: 503,
  GATEWAY_TIMEOUT: 504,
};

// 错误码
export const ERROR_CODES = {
  // 成功
  SUCCESS: 0,

  // 认证授权错误 10001-10099
  AUTH_ERROR: 10001,
  TOKEN_INVALID: 10001,
  TOKEN_EXPIRED: 10002,
  NO_PERMISSION: 10003,
  ACCESS_DENIED: 10004,

  // 访问控制错误 10101-10199
  IP_RESTRICTED: 10101,
  TIME_RESTRICTED: 10102,

  // 资源操作错误 20001-20099
  RESOURCE_NOT_FOUND: 20001,
  RESOURCE_ALREADY_EXISTS: 20002,
  RESOURCE_LOCKED: 20003,

  // 资源状态错误 20101-20199
  STATUS_NOT_ALLOWED: 20101,

  // 参数校验错误 30001-30099
  PARAM_FORMAT_ERROR: 30001,
  PARAM_REQUIRED: 30002,
  PARAM_OUT_OF_RANGE: 30003,

  // 业务逻辑错误 40001-40099
  BUSINESS_ERROR: 40001,
  STATE_NOT_ALLOWED: 40002,
  PRECONDITION_NOT_MET: 40003,
  BUSINESS_RULE_CONFLICT: 40004,

  // 流程审批错误 40101-40199
  APPROVAL_NOT_PASSED: 40101,
  APPROVAL_TIMEOUT: 40102,

  // 系统内部错误 50001-50099
  SERVICE_UNAVAILABLE: 50001,
  SYSTEM_BUSY: 50002,
  DATA_ERROR: 50003,

  // 第三方服务错误 50101-50199
  EXTERNAL_SERVICE_TIMEOUT: 50101,
  EXTERNAL_SERVICE_UNAVAILABLE: 50102,
};

// 存储键名
export const STORAGE_KEYS = {
  TOKEN: process.env.TOKEN_KEY || 'edams_access_token',
  REFRESH_TOKEN: process.env.REFRESH_TOKEN_KEY || 'edams_refresh_token',
  USER_INFO: 'edams_user_info',
  USER_PERMISSIONS: 'edams_user_permissions',
  USER_CONFIG: 'edams_user_config',
  MENUS: 'edams_menus',
  RECENT_ASSETS: 'edams_recent_assets',
  FAVORITE_ASSETS: 'edams_favorite_assets',
  THEME: 'edams_theme',
  LANGUAGE: 'edams_language',
  SIDEBARcollapsed: 'edams_sidebar_collapsed',
};

// 页面标题
export const PAGE_TITLES = {
  HOME: '工作台',
  LOGIN: '登录',
  REGISTER: '注册',
  FORGOT_PASSWORD: '忘记密码',
  MFA: '双因素认证',
  ASSET_LIST: '资产列表',
  ASSET_DETAIL: '资产详情',
  ASSET_CREATE: '注册资产',
  ASSET_FAVORITES: '我的收藏',
  CATALOG_TREE: '目录树',
  CATALOG_DOMAIN: '业务域',
  LINEAGE_GRAPH: '血缘图',
  LINEAGE_IMPACT: '影响分析',
  QUALITY_OVERVIEW: '质量概览',
  QUALITY_RULES: '质量规则',
  QUALITY_REPORTS: '质量报告',
  QUALITY_ISSUES: '问题追踪',
  SYSTEM_USERS: '用户管理',
  SYSTEM_ROLES: '角色权限',
  SYSTEM_DATASOURCES: '数据源配置',
  SYSTEM_NOTIFICATIONS: '通知设置',
  NOT_FOUND: '404 - 页面不存在',
};

// 日期格式
export const DATE_FORMATS = {
  DATE: 'YYYY-MM-DD',
  TIME: 'HH:mm:ss',
  DATETIME: 'YYYY-MM-DD HH:mm:ss',
  DATETIME_COMPACT: 'YYYYMMDDHHmmss',
  MONTH: 'YYYY-MM',
  YEAR: 'YYYY',
};

// 分页默认值
export const PAGE_DEFAULT = {
  PAGE: 1,
  PAGE_SIZE: 20,
  PAGE_SIZE_OPTIONS: ['10', '20', '50', '100'],
  MAX_PAGE_SIZE: 100,
};

// Token 相关
export const TOKEN = {
  ACCESS_TOKEN_EXPIRES_IN: 7200, // 2小时
  REFRESH_TOKEN_EXPIRES_IN: 604800, // 7天
};

// 登录类型
export const LOGIN_TYPES = {
  PASSWORD: 'PASSWORD',
  MOBILE_CODE: 'MOBILE_CODE',
  WECHAT: 'ENTERPRISE_WECHAT',
  DINGTALK: 'DINGTALK',
  LDAP: 'LDAP',
  OAUTH2: 'OAUTH2',
  KEYCLOAK: 'KEYCLOAK',
};

// SSO提供商类型
export const SSO_PROVIDERS = {
  KEYCLOAK: 'keycloak',
  WECHAT_WORK: 'wxwork',
  DINGTALK: 'dingtalk',
  LDAP: 'ldap',
};

// 消息类型
export const NOTIFICATION_TYPES = {
  ASSET_UPDATE: 'ASSET_UPDATE',
  QUALITY_ISSUE: 'QUALITY_ISSUE',
  TASK_ASSIGNED: 'TASK_ASSIGNED',
  APPROVAL: 'APPROVAL',
  SYSTEM: 'SYSTEM',
};

// 导出常量
export default {
  API_PATHS,
  HTTP_STATUS,
  ERROR_CODES,
  STORAGE_KEYS,
  PAGE_TITLES,
  DATE_FORMATS,
  PAGE_DEFAULT,
  TOKEN,
  LOGIN_TYPES,
  NOTIFICATION_TYPES,
};
