export type UserRole = 'admin' | 'editor' | 'viewer';

export type SecurityLevel = 'public' | 'internal' | 'sensitive' | 'confidential';

export type AssetType = 'table' | 'file' | 'api' | 'metric' | 'dashboard';

export type NotificationType = 'info' | 'warning' | 'success' | 'error';

export type FeedbackCategory = 'bug' | 'feature' | 'general';

export type FeedbackStatus = 'pending' | 'reviewed' | 'resolved';

export interface User {
  id: string;
  email: string;
  name: string | null;
  avatar_url: string | null;
  role: UserRole;
  department: string | null;
  created_at: string;
  updated_at: string;
}

export interface DataSource {
  id: string;
  name: string;
  type: string;
  connection_config: Record<string, any>;
  owner_id: string;
  owner?: User;
  created_at: string;
}

export interface DataAsset {
  id: string;
  guid: string;
  name: string;
  description: string | null;
  type: AssetType;
  source_id: string | null;
  source?: DataSource;
  schema_definition: SchemaDefinition | null;
  security_level: SecurityLevel;
  quality_score: number;
  usage_count: number;
  owner_id: string;
  owner?: User;
  tags: string[];
  created_at: string;
  updated_at: string;
  is_favorited?: boolean;
}

export interface SchemaField {
  name: string;
  type: string;
  description: string | null;
  is_nullable: boolean;
  is_primary_key: boolean;
  default_value: string | null;
}

export interface SchemaDefinition {
  fields: SchemaField[];
}

export interface LineageEdge {
  id: string;
  source_asset_id: string;
  source_asset?: DataAsset;
  target_asset_id: string;
  target_asset?: DataAsset;
  transformation_type: string;
  transformation_detail: string | null;
  created_at: string;
}

export interface LineageNode extends DataAsset {
  depth?: number;
  x?: number;
  y?: number;
}

export interface LineageGraph {
  nodes: LineageNode[];
  edges: LineageEdge[];
}

export interface AssetAccessLog {
  id: string;
  user_id: string;
  user?: User;
  asset_id: string;
  asset?: DataAsset;
  action: 'view' | 'search' | 'favorite' | 'export';
  created_at: string;
}

export interface Feedback {
  id: string;
  user_id: string;
  user?: User;
  content: string;
  rating: number;
  category: FeedbackCategory;
  status: FeedbackStatus;
  created_at: string;
}

export interface Notification {
  id: string;
  user_id: string;
  title: string;
  content: string | null;
  type: NotificationType;
  is_read: boolean;
  created_at: string;
}

export interface AssetCategory {
  id: string;
  name: string;
  parent_id: string | null;
  path: string;
  level: number;
  asset_count?: number;
  children?: AssetCategory[];
}

export interface DashboardStats {
  totalAssets: number;
  totalSources: number;
  avgQualityScore: number;
  totalAccess: number;
  assetsByType: Record<AssetType, number>;
  assetsBySecurity: Record<SecurityLevel, number>;
  recentAssets: DataAsset[];
  topAssets: DataAsset[];
}

export interface SearchFilters {
  keyword?: string;
  type?: AssetType | 'all';
  security_level?: SecurityLevel | 'all';
  source_id?: string;
  owner_id?: string;
  tags?: string[];
}

export interface ApiResponse<T> {
  data: T | null;
  error: string | null;
}
