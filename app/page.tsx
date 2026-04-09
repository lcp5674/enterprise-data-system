"use client";

import { MainLayout } from "@/components/layout/main-layout";
import { StatCard } from "@/components/dashboard/stat-card";
import { RecentAssets, FavoriteAssets } from "@/components/dashboard/recent-assets";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Database,
  Table2,
  FileText,
  Api,
  BarChart3,
  Activity,
  CheckCircle,
  Shield,
  TrendingUp,
  ArrowRight,
  Bell,
  MessageSquare,
  Plus,
} from "lucide-react";
import type { DataAsset, DashboardStats, Notification } from "@/types";

// Mock data for demonstration
const mockStats: DashboardStats = {
  totalAssets: 1258,
  totalSources: 24,
  avgQualityScore: 86.5,
  totalAccess: 34567,
  assetsByType: {
    table: 892,
    file: 156,
    api: 124,
    metric: 56,
    dashboard: 30,
  },
  assetsBySecurity: {
    public: 125,
    internal: 856,
    sensitive: 198,
    confidential: 79,
  },
  recentAssets: [],
  topAssets: [],
};

const mockNotifications: Notification[] = [
  {
    id: "1",
    user_id: "1",
    title: "数据质量告警",
    content: "dwd_orders 表的质量评分下降至 72 分",
    type: "warning",
    is_read: false,
    created_at: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
  },
  {
    id: "2",
    user_id: "1",
    title: "血缘变更通知",
    content: "dim_customer 表新增下游依赖",
    type: "info",
    is_read: false,
    created_at: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
  },
  {
    id: "3",
    user_id: "1",
    title: "新功能上线",
    content: "数据血缘追踪功能已升级，支持字段级血缘",
    type: "success",
    is_read: true,
    created_at: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
  },
];

const mockRecentAssets: DataAsset[] = [
  {
    id: "1",
    guid: "asset-001",
    name: "ods_orders",
    description: "订单原始数据表，包含所有渠道订单原始数据",
    type: "table",
    source_id: "1",
    schema_definition: null,
    security_level: "internal",
    quality_score: 92,
    usage_count: 156,
    owner_id: "1",
    owner: { id: "1", email: "zhangsan@company.com", name: "张三", avatar_url: null, role: "editor", department: "数据平台部", created_at: "", updated_at: "" },
    tags: ["ods", "订单", "原始数据"],
    created_at: "",
    updated_at: new Date(Date.now() - 1000 * 60 * 60).toISOString(),
  },
  {
    id: "2",
    guid: "asset-002",
    name: "dim_customer",
    description: "客户维度表，存储客户详细信息",
    type: "table",
    source_id: "1",
    schema_definition: null,
    security_level: "sensitive",
    quality_score: 88,
    usage_count: 234,
    owner_id: "2",
    owner: { id: "2", email: "lisi@company.com", name: "李四", avatar_url: null, role: "editor", department: "数据平台部", created_at: "", updated_at: "" },
    tags: ["dim", "客户", "维度"],
    created_at: "",
    updated_at: new Date(Date.now() - 1000 * 60 * 60 * 3).toISOString(),
  },
  {
    id: "3",
    guid: "asset-003",
    name: "dwd_orders",
    description: "订单明细宽表，ODS层清洗后的数据",
    type: "table",
    source_id: "1",
    schema_definition: null,
    security_level: "internal",
    quality_score: 85,
    usage_count: 189,
    owner_id: "1",
    owner: { id: "1", email: "zhangsan@company.com", name: "张三", avatar_url: null, role: "editor", department: "数据平台部", created_at: "", updated_at: "" },
    tags: ["dwd", "订单", "明细"],
    created_at: "",
    updated_at: new Date(Date.now() - 1000 * 60 * 60 * 5).toISOString(),
  },
  {
    id: "4",
    guid: "asset-004",
    name: "ads_revenue_report",
    description: "收入报表指标，每日、月、年收入汇总",
    type: "metric",
    source_id: "1",
    schema_definition: null,
    security_level: "internal",
    quality_score: 95,
    usage_count: 78,
    owner_id: "3",
    owner: { id: "3", email: "wangwu@company.com", name: "王五", avatar_url: null, role: "editor", department: "财务部", created_at: "", updated_at: "" },
    tags: ["ads", "收入", "报表"],
    created_at: "",
    updated_at: new Date(Date.now() - 1000 * 60 * 60 * 8).toISOString(),
  },
  {
    id: "5",
    guid: "asset-005",
    name: "CustomerAPI",
    description: "客户信息服务接口，提供客户数据查询",
    type: "api",
    source_id: "2",
    schema_definition: null,
    security_level: "sensitive",
    quality_score: 91,
    usage_count: 56,
    owner_id: "2",
    owner: { id: "2", email: "lisi@company.com", name: "李四", avatar_url: null, role: "editor", department: "数据平台部", created_at: "", updated_at: "" },
    tags: ["api", "客户", "服务"],
    created_at: "",
    updated_at: new Date(Date.now() - 1000 * 60 * 60 * 12).toISOString(),
  },
];

const mockFavoriteAssets: DataAsset[] = [
  {
    id: "6",
    guid: "asset-006",
    name: "dim_product",
    description: "商品维度表，存储商品详细信息",
    type: "table",
    source_id: "1",
    schema_definition: null,
    security_level: "internal",
    quality_score: 89,
    usage_count: 145,
    owner_id: "1",
    tags: ["dim", "商品"],
    created_at: "",
    updated_at: "",
  },
  {
    id: "7",
    guid: "asset-007",
    name: "UserBehaviorAPI",
    description: "用户行为数据接口",
    type: "api",
    source_id: "2",
    schema_definition: null,
    security_level: "internal",
    quality_score: 87,
    usage_count: 89,
    owner_id: "2",
    tags: ["api", "用户行为"],
    created_at: "",
    updated_at: "",
  },
];

function formatTimeAgo(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return "刚刚";
  if (diffMins < 60) return `${diffMins}分钟前`;
  if (diffHours < 24) return `${diffHours}小时前`;
  if (diffDays < 7) return `${diffDays}天前`;
  return date.toLocaleDateString("zh-CN");
}

function getNotificationIcon(type: string) {
  switch (type) {
    case "warning":
      return Bell;
    case "success":
      return CheckCircle;
    default:
      return MessageSquare;
  }
}

export default function HomePage() {
  const stats = mockStats;
  const recentAssets = mockRecentAssets;
  const favoriteAssets = mockFavoriteAssets;
  const notifications = mockNotifications;

  return (
    <MainLayout>
      <div className="container mx-auto p-6 space-y-6">
        {/* Welcome Section */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">欢迎回来</h1>
            <p className="text-muted-foreground">
              查看您的数据资产工作台，了解整体数据状况
            </p>
          </div>
          <div className="flex items-center gap-2">
            <Button variant="outline" className="gap-2">
              <MessageSquare className="h-4 w-4" />
              反馈
            </Button>
            <Button className="gap-2">
              <Plus className="h-4 w-4" />
              注册资产
            </Button>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <StatCard
            title="数据资产总量"
            value={stats.totalAssets.toLocaleString()}
            change={12.5}
            changeLabel="较上月"
            icon={Database}
          />
          <StatCard
            title="数据源连接"
            value={stats.totalSources}
            change={4.2}
            changeLabel="较上月"
            icon={Activity}
          />
          <StatCard
            title="平均质量评分"
            value={stats.avgQualityScore.toFixed(1)}
            change={2.3}
            changeLabel="较上月"
            icon={CheckCircle}
          />
          <StatCard
            title="本月访问量"
            value={(stats.totalAccess / 1000).toFixed(1) + "k"}
            change={18.7}
            changeLabel="较上月"
            icon={TrendingUp}
          />
        </div>

        {/* Asset Type Distribution */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-5">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium flex items-center gap-2">
                <Table2 className="h-4 w-4" />
                数据表
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.assetsByType.table}</div>
              <p className="text-xs text-muted-foreground">占比 70.9%</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium flex items-center gap-2">
                <FileText className="h-4 w-4" />
                数据文件
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.assetsByType.file}</div>
              <p className="text-xs text-muted-foreground">占比 12.4%</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium flex items-center gap-2">
                <Api className="h-4 w-4" />
                API接口
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.assetsByType.api}</div>
              <p className="text-xs text-muted-foreground">占比 9.9%</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium flex items-center gap-2">
                <BarChart3 className="h-4 w-4" />
                指标
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stats.assetsByType.metric}</div>
              <p className="text-xs text-muted-foreground">占比 4.5%</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium flex items-center gap-2">
                <Shield className="h-4 w-4" />
                安全等级
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">4</div>
              <p className="text-xs text-muted-foreground">公开/内部/敏感/机密</p>
            </CardContent>
          </Card>
        </div>

        {/* Main Content Grid */}
        <div className="grid gap-6 lg:grid-cols-3">
          {/* Recent Assets */}
          <div className="lg:col-span-2">
            <RecentAssets assets={recentAssets} />
          </div>

          {/* Notifications */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle className="text-base font-medium flex items-center gap-2">
                <Bell className="h-4 w-4" />
                最新通知
                {notifications.filter((n) => !n.is_read).length > 0 && (
                  <span className="ml-2 px-2 py-0.5 text-xs bg-red-100 text-red-600 rounded-full">
                    {notifications.filter((n) => !n.is_read).length}
                  </span>
                )}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {notifications.map((notification) => {
                  const Icon = getNotificationIcon(notification.type);
                  return (
                    <div
                      key={notification.id}
                      className={`p-3 rounded-lg border ${
                        notification.is_read
                          ? "bg-muted/30"
                          : "bg-primary/5 border-primary/20"
                      }`}
                    >
                      <div className="flex items-start gap-3">
                        <div
                          className={`mt-0.5 p-1.5 rounded-full ${
                            notification.type === "warning"
                              ? "bg-amber-100 text-amber-600"
                              : notification.type === "success"
                              ? "bg-emerald-100 text-emerald-600"
                              : "bg-blue-100 text-blue-600"
                          }`}
                        >
                          <Icon className="h-3 w-3" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium">{notification.title}</p>
                          <p className="text-xs text-muted-foreground mt-1 line-clamp-2">
                            {notification.content}
                          </p>
                          <p className="text-xs text-muted-foreground mt-2">
                            {formatTimeAgo(notification.created_at)}
                          </p>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
              <Button variant="ghost" className="w-full mt-4 gap-2">
                查看全部通知
                <ArrowRight className="h-4 w-4" />
              </Button>
            </CardContent>
          </Card>
        </div>

        {/* Quick Actions */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base font-medium">快捷入口</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-4">
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Database className="h-6 w-6" />
                <span className="text-sm">数据地图</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Table2 className="h-6 w-6" />
                <span className="text-sm">全部资产</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <Api className="h-6 w-6" />
                <span className="text-sm">API服务</span>
              </Button>
              <Button variant="outline" className="h-20 flex-col gap-2">
                <BarChart3 className="h-6 w-6" />
                <span className="text-sm">质量报告</span>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </MainLayout>
  );
}
