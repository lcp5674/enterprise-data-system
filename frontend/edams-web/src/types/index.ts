/**
 * 公共类型定义
 */

// 通用响应结构
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp?: number;
  traceId?: string;
}

// 分页响应
export interface PageResponse<T = any> {
  list: T[];
  pagination: Pagination;
}

// 分页参数
export interface Pagination {
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
}

// 分页请求参数
export interface PageParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

// 用户相关类型
export interface User {
  id: string;
  username: string;
  nickname: string;
  email: string;
  phone?: string;
  avatar?: string;
  department?: Department;
  roles: Role[];
  permissions: string[];
  status: number;
  userType: number;
  lastLoginTime?: string;
  lastLoginIp?: string;
  createdTime: string;
  updatedTime?: string;
}

export interface Department {
  id: string;
  name: string;
  path: string;
  parentId?: string;
  level?: number;
  memberCount?: number;
  leader?: UserBasic;
}

export interface Role {
  id: string;
  code: string;
  name: string;
  description?: string;
  roleType?: number;
  status?: number;
  permissions?: string[];
}

export interface UserBasic {
  id: string;
  name: string;
  avatar?: string;
}

// 认证相关类型
export interface LoginParams {
  loginType: 'PASSWORD' | 'MOBILE_CODE' | 'ENTERPRISE_WECHAT' | 'DINGTALK' | 'LDAP' | 'OAUTH2';
  username?: string;
  password?: string;
  mobile?: string;
  code?: string;
  captcha?: string;
  captchaId?: string;
  loginSource?: 'WEB' | 'APP' | 'API';
  deviceId?: string;
  deviceInfo?: DeviceInfo;
}

export interface DeviceInfo {
  os: string;
  appVersion?: string;
  ip?: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  refreshExpiresIn: number;
  tokenType: string;
  user: User;
  menus: MenuItem[];
  config: UserConfig;
}

export interface UserConfig {
  theme: 'light' | 'dark';
  language: string;
  timezone: string;
}

export interface MFAStatus {
  enabled: boolean;
  secret?: string;
  qrCode?: string;
  backupCodes?: string[];
}

// 菜单相关类型
export interface MenuItem {
  id: string;
  name: string;
  path: string;
  icon?: string;
  component?: string;
  routes?: MenuItem[];
  hideInMenu?: boolean;
  hideInBreadcrumb?: boolean;
  authority?: string[];
  exact?: boolean;
  redirect?: string;
}

// 数据资产相关类型
export interface DataAsset {
  id: string;
  guid?: string;
  name: string;
  alias?: string;
  assetType: AssetType;
  datasource?: DataSource;
  database?: string;
  schema?: string;
  tableName?: string;
  description?: string;
  businessDomain?: string;
  businessDomainPath?: string;
  sensitivityLevel: SensitivityLevel;
  owner?: UserBasic;
  certifiedUser?: UserBasic;
  certificationTime?: string;
  certificationLevel?: CertificationLevel;
  status: AssetStatus;
  tags: string[];
  fields?: DataField[];
  lineage?: LineageSummary;
  quality?: QualitySummary;
  statistics?: AssetStatistics;
  relatedAssets?: RelatedAsset[];
  documents?: Document[];
  schedules?: Schedule[];
  createdBy?: string;
  createdTime: string;
  updatedBy?: string;
  updatedTime?: string;
}

export type AssetType = 'TABLE' | 'VIEW' | 'FILE' | 'API' | 'STREAM' | 'MODEL' | 'REPORT' | 'DASHBOARD';

export type SensitivityLevel = 'L1' | 'L2' | 'L3' | 'L4';

export type CertificationLevel = 'BRONZE' | 'SILVER' | 'GOLD';

export type AssetStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'PUBLISHED' | 'DEPRECATED' | 'ARCHIVED';

export interface DataField {
  id: string;
  name: string;
  alias?: string;
  dataType: string;
  comment?: string;
  description?: string;
  nullable: boolean;
  primaryKey: boolean;
  defaultValue?: any;
  maxLength?: number;
  sensitivityLevel?: SensitivityLevel;
  isCertified?: boolean;
  businessMeanings?: string[];
  sampleValues?: string[];
  fieldOrder?: number;
}

export interface DataSource {
  id: string;
  name: string;
  type: string;
  connectionStatus?: string;
}

export interface AssetStatistics {
  rowCount?: number;
  storageSize?: string;
  storageFormat?: string;
  compression?: string;
  partitioned?: boolean;
  partitionKeys?: string[];
  viewCount?: number;
  favoriteCount?: number;
  downloadedCount?: number;
  lastAccessTime?: string;
  lastUpdateTime?: string;
}

export interface LineageSummary {
  upstreamCount: number;
  downstreamCount: number;
  hasLineage: boolean;
}

export interface QualitySummary {
  score: number;
  trend?: string;
  lastCheckTime?: string;
}

export interface RelatedAsset {
  id: string;
  name: string;
  type: AssetType;
  relation: string;
}

export interface Document {
  id: string;
  name: string;
  type: string;
  url?: string;
}

export interface Schedule {
  taskName: string;
  cronExpression?: string;
  status?: string;
}

// 资产查询参数
export interface AssetQueryParams extends PageParams {
  keyword?: string;
  assetType?: AssetType;
  businessDomain?: string;
  sensitivityLevel?: SensitivityLevel;
  ownerId?: string;
  datasourceId?: string;
  status?: AssetStatus;
  tags?: string;
  certificationLevel?: CertificationLevel;
  createdTimeStart?: string;
  createdTimeEnd?: string;
  updatedTimeStart?: string;
  updatedTimeEnd?: string;
}

// 血缘相关类型
export interface LineageNode {
  id: string;
  name: string;
  alias?: string;
  type: AssetType;
  level: number;
  database?: string;
  businessDomain?: string;
  x?: number;
  y?: number;
}

export interface LineageEdge {
  source: string;
  target: string;
  transform?: string;
  taskName?: string;
  taskId?: string;
  scheduleTime?: string;
}

export interface LineageGraph {
  nodes: LineageNode[];
  edges: LineageEdge[];
  statistics: {
    upstreamCount: number;
    downstreamCount: number;
    maxDepth: number;
    hasCycle: boolean;
  };
}

// 质量相关类型
export interface QualityRule {
  id: string;
  name: string;
  code: string;
  ruleType: RuleType;
  targetType: 'TABLE' | 'FIELD';
  expression?: string;
  description?: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  category: QualityCategory;
  sqlTemplate?: string;
  params?: Record<string, any>;
  status: number;
  createdTime: string;
  updatedTime?: string;
}

export type RuleType = 'NOT_NULL' | 'UNIQUENESS' | 'RANGE' | 'REGEX' | 'FORMULA' | 'CUSTOM';

export type QualityCategory = 'COMPLETENESS' | 'ACCURACY' | 'CONSISTENCY' | 'TIMELINESS' | 'UNIQUENESS';

export interface QualityCheckResult {
  checkId: string;
  assetId: string;
  assetName: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  startTime: string;
  endTime?: string;
  duration?: number;
  summary: {
    totalRules: number;
    passedRules: number;
    failedRules: number;
    errorRules: number;
    passRate: string;
  };
  ruleResults?: QualityRuleResult[];
  executionEngine?: string;
  errorMessage?: string;
}

export interface QualityRuleResult {
  ruleId: string;
  ruleName: string;
  status: 'PASS' | 'FAIL' | 'ERROR';
  passCount?: number;
  totalCount?: number;
  violationCount?: number;
  passRate?: string;
  sampleViolations?: any[];
}

export interface QualityIssue {
  id: string;
  issueNo: string;
  assetId: string;
  assetName: string;
  ruleId: string;
  ruleName: string;
  fieldId?: string;
  fieldName?: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'IGNORED';
  description: string;
  sampleData?: any[];
  checkResultId?: string;
  assignee?: UserBasic;
  assignedTime?: string;
  dueTime?: string;
  resolution?: string;
  resolvedBy?: UserBasic;
  resolvedTime?: string;
  history?: IssueHistory[];
  createdTime: string;
}

export interface IssueHistory {
  action: string;
  operator: string;
  time: string;
  comment?: string;
}

// 数据源相关类型
export interface DataSourceConfig {
  id: string;
  name: string;
  datasourceType: string;
  host?: string;
  port?: number;
  database?: string;
  username?: string;
  connectionUrl?: string;
  properties?: Record<string, any>;
  description?: string;
  tags?: string[];
  maxConnections?: number;
  connectionTimeout?: number;
  idleTimeout?: number;
  status?: string;
  lastSyncTime?: string;
  createdTime: string;
  updatedTime?: string;
}

// 通知相关类型
export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  content: string;
  status: 'UNREAD' | 'READ';
  sourceType?: string;
  sourceId?: string;
  readTime?: string;
  createdTime: string;
}

export type NotificationType = 
  | 'ASSET_UPDATE' 
  | 'QUALITY_ISSUE' 
  | 'TASK_ASSIGNED' 
  | 'APPROVAL' 
  | 'SYSTEM';

// 仪表盘相关类型
export interface DashboardStats {
  totalAssets: number;
  todayAssets: number;
  qualityScore: number;
  qualityTrend: number;
  pendingApprovals: number;
  totalUsers: number;
  activeUsers: number;
}

export interface AssetDistribution {
  type: string;
  count: number;
  percentage: number;
}

export interface QualityTrend {
  date: string;
  score: number;
}

export interface RecentAsset {
  id: string;
  name: string;
  type: AssetType;
  updatedTime: string;
}

// 血缘分析相关
export interface ImpactAnalysis {
  assetId: string;
  assetName: string;
  impactAnalysis: {
    directDownstreamCount: number;
    totalDownstreamCount: number;
    criticalAssetsCount: number;
    reportsAffectedCount: number;
    estimatedImpactTime?: string;
  };
  affectedAssets: AffectedAsset[];
  mitigationSuggestions: MitigationSuggestion[];
}

export interface AffectedAsset {
  id: string;
  name: string;
  type: AssetType;
  dependencyType: 'DIRECT' | 'INDIRECT';
  criticalLevel: 'HIGH' | 'MEDIUM' | 'LOW';
  owner?: UserBasic;
}

export interface MitigationSuggestion {
  suggestion: string;
  action: string;
}

// 权限相关
export interface Permission {
  module: string;
  moduleCode: string;
  permissions: PermissionItem[];
}

export interface PermissionItem {
  code: string;
  name: string;
  type: string;
}

// 表格筛选和排序
export interface TableColumn {
  key: string;
  title: string;
  dataIndex?: string;
  sorter?: boolean;
  sortOrder?: 'ascend' | 'descend';
  filters?: { text: string; value: string }[];
  onFilter?: (value: any, record: any) => boolean;
  render?: (text: any, record: any, index: number) => React.ReactNode;
}

// 导出类型
export type ExportFormat = 'EXCEL' | 'CSV' | 'JSON';

export interface ExportRequest {
  format: ExportFormat;
  columns?: string[];
  filename?: string;
}

// 字典相关
export const ASSET_TYPE_OPTIONS = [
  { label: '数据表', value: 'TABLE' },
  { label: '视图', value: 'VIEW' },
  { label: '文件', value: 'FILE' },
  { label: 'API接口', value: 'API' },
  { label: '流数据', value: 'STREAM' },
  { label: '数据模型', value: 'MODEL' },
  { label: '报表', value: 'REPORT' },
  { label: '仪表盘', value: 'DASHBOARD' },
];

export const SENSITIVITY_LEVEL_OPTIONS = [
  { label: '公开 (L1)', value: 'L1', color: '#52c41a' },
  { label: '内部 (L2)', value: 'L2', color: '#faad14' },
  { label: '敏感 (L3)', value: 'L3', color: '#fa8c16' },
  { label: '机密 (L4)', value: 'L4', color: '#f5222d' },
];

export const CERTIFICATION_LEVEL_OPTIONS = [
  { label: '基础认证', value: 'BRONZE', color: '#cd7f32' },
  { label: '银牌认证', value: 'SILVER', color: '#c0c0c0' },
  { label: '金牌认证', value: 'GOLD', color: '#ffd700' },
];

export const ASSET_STATUS_OPTIONS = [
  { label: '草稿', value: 'DRAFT', color: '#8c8c8c' },
  { label: '待审核', value: 'PENDING', color: '#faad14' },
  { label: '已审核', value: 'APPROVED', color: '#52c41a' },
  { label: '已驳回', value: 'REJECTED', color: '#f5222d' },
  { label: '已发布', value: 'PUBLISHED', color: '#1890ff' },
  { label: '已废弃', value: 'DEPRECATED', color: '#8c8c8c' },
  { label: '已归档', value: 'ARCHIVED', color: '#d9d9d9' },
];

export const QUALITY_CATEGORY_OPTIONS = [
  { label: '完整性', value: 'COMPLETENESS' },
  { label: '准确性', value: 'ACCURACY' },
  { label: '一致性', value: 'CONSISTENCY' },
  { label: '时效性', value: 'TIMELINESS' },
  { label: '唯一性', value: 'UNIQUENESS' },
];

export const SEVERITY_OPTIONS = [
  { label: '致命', value: 'CRITICAL', color: '#f5222d' },
  { label: '高', value: 'HIGH', color: '#fa8c16' },
  { label: '中', value: 'MEDIUM', color: '#faad14' },
  { label: '低', value: 'LOW', color: '#52c41a' },
];

export const ISSUE_STATUS_OPTIONS = [
  { label: '待处理', value: 'OPEN', color: '#f5222d' },
  { label: '处理中', value: 'IN_PROGRESS', color: '#faad14' },
  { label: '已解决', value: 'RESOLVED', color: '#52c41a' },
  { label: '已关闭', value: 'CLOSED', color: '#8c8c8c' },
  { label: '已忽略', value: 'IGNORED', color: '#d9d9d9' },
];

export default {
  ASSET_TYPE_OPTIONS,
  SENSITIVITY_LEVEL_OPTIONS,
  CERTIFICATION_LEVEL_OPTIONS,
  ASSET_STATUS_OPTIONS,
  QUALITY_CATEGORY_OPTIONS,
  SEVERITY_OPTIONS,
  ISSUE_STATUS_OPTIONS,
};
