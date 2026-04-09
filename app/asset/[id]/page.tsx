"use client";

import { useState } from "react";
import Link from "next/link";
import { MainLayout } from "@/components/layout/main-layout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { SecurityBadge } from "@/components/ui/security-badge";
import { QualityScore } from "@/components/ui/quality-score";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Database,
  FileText,
  Api,
  BarChart3,
  LayoutDashboard,
  ArrowLeft,
  Star,
  StarOff,
  GitBranch,
  User,
  Clock,
  Eye,
  Download,
  Share2,
  ExternalLink,
  Copy,
  Check,
  MoreHorizontal,
  AlertCircle,
} from "lucide-react";
import { cn } from "@/lib/utils";
import type { DataAsset, SchemaField, LineageNode } from "@/types";

// Mock data
const mockAsset: DataAsset = {
  id: "1",
  guid: "asset-001",
  name: "ods_orders",
  description:
    "订单原始数据表，包含所有渠道（APP、Web、线下门店）订单原始数据。数据从业务系统通过Canal实时同步，每日凌晨2点进行全量快照。字段定义遵循公司ODS层命名规范。",
  type: "table",
  source_id: "1",
  schema_definition: {
    fields: [
      { name: "order_id", type: "string", description: "订单ID，主键", is_nullable: false, is_primary_key: true, default_value: null },
      { name: "order_no", type: "string", description: "订单编号", is_nullable: false, is_primary_key: false, default_value: null },
      { name: "customer_id", type: "string", description: "客户ID", is_nullable: false, is_primary_key: false, default_value: null },
      { name: "order_time", type: "timestamp", description: "下单时间", is_nullable: false, is_primary_key: false, default_value: null },
      { name: "order_amount", type: "decimal(18,2)", description: "订单金额", is_nullable: false, is_primary_key: false, default_value: "0.00" },
      { name: "discount_amount", type: "decimal(18,2)", description: "优惠金额", is_nullable: true, is_primary_key: false, default_value: "0.00" },
      { name: "pay_amount", type: "decimal(18,2)", description: "实付金额", is_nullable: false, is_primary_key: false, default_value: null },
      { name: "order_status", type: "string", description: "订单状态：pendingpaidcompletedcancelled", is_nullable: false, is_primary_key: false, default_value: "pending" },
      { name: "channel", type: "string", description: "订单渠道：appwebstore", is_nullable: false, is_primary_key: false, default_value: null },
      { name: "store_id", type: "string", description: "门店ID", is_nullable: true, is_primary_key: false, default_value: null },
      { name: "create_time", type: "timestamp", description: "创建时间", is_nullable: false, is_primary_key: false, default_value: "CURRENT_TIMESTAMP" },
      { name: "update_time", type: "timestamp", description: "更新时间", is_nullable: false, is_primary_key: false, default_value: "CURRENT_TIMESTAMP" },
    ] as SchemaField[],
  },
  security_level: "internal",
  quality_score: 92,
  usage_count: 156,
  owner_id: "1",
  owner: {
    id: "1",
    email: "zhangsan@company.com",
    name: "张三",
    avatar_url: null,
    role: "editor",
    department: "数据平台部",
    created_at: "",
    updated_at: "",
  },
  tags: ["ods", "订单", "原始数据"],
  created_at: "2024-01-15T08:00:00Z",
  updated_at: "2024-04-01T10:30:00Z",
};

const mockUpstreamLineage: LineageNode[] = [
  { id: "up1", guid: "up-001", name: "erp_orders", description: "ERP订单系统", type: "table", source_id: "1", schema_definition: null, security_level: "internal", quality_score: 88, usage_count: 45, owner_id: "1", tags: ["erp"], created_at: "", updated_at: "" },
  { id: "up2", guid: "up-002", name: "crm_customer", description: "CRM客户系统", type: "table", source_id: "1", schema_definition: null, security_level: "sensitive", quality_score: 85, usage_count: 32, owner_id: "2", tags: ["crm"], created_at: "", updated_at: "" },
];

const mockDownstreamLineage: LineageNode[] = [
  { id: "down1", guid: "down-001", name: "dwd_orders", description: "订单明细宽表", type: "table", source_id: "1", schema_definition: null, security_level: "internal", quality_score: 90, usage_count: 189, owner_id: "1", tags: ["dwd"], created_at: "", updated_at: "" },
  { id: "down2", guid: "down-002", name: "ads_order_stats", description: "订单统计报表", type: "metric", source_id: "1", schema_definition: null, security_level: "internal", quality_score: 95, usage_count: 78, owner_id: "3", tags: ["ads"], created_at: "", updated_at: "" },
  { id: "down3", guid: "down-003", name: "OrderReportAPI", description: "订单报表接口", type: "api", source_id: "2", schema_definition: null, security_level: "internal", quality_score: 92, usage_count: 56, owner_id: "2", tags: ["api"], created_at: "", updated_at: "" },
];

const typeIcons: Record<string, typeof Database> = {
  table: Database,
  file: FileText,
  api: Api,
  metric: BarChart3,
  dashboard: LayoutDashboard,
};

const typeLabels: Record<string, string> = {
  table: "数据表",
  file: "数据文件",
  api: "API接口",
  metric: "指标",
  dashboard: "仪表盘",
};

function LineageCard({ node }: { node: LineageNode }) {
  const Icon = typeIcons[node.type] || Database;
  return (
    <Link href={`/asset/${node.id}`}>
      <div className="p-3 border rounded-lg hover:bg-muted/50 transition-colors cursor-pointer group">
        <div className="flex items-center gap-2 mb-1">
          <Icon className="h-4 w-4 text-muted-foreground" />
          <span className="font-medium text-sm group-hover:text-primary">
            {node.name}
          </span>
        </div>
        <p className="text-xs text-muted-foreground line-clamp-1">
          {node.description}
        </p>
        <div className="flex items-center gap-2 mt-2">
          <SecurityBadge level={node.security_level} />
          <QualityScore score={node.quality_score} size="sm" showLabel={false} />
        </div>
      </div>
    </Link>
  );
}

function FieldTypeBadge({ type }: { type: string }) {
  const getColor = (t: string) => {
    if (t.includes("string") || t.includes("varchar")) return "text-emerald-600 bg-emerald-50";
    if (t.includes("int") || t.includes("bigint")) return "text-blue-600 bg-blue-50";
    if (t.includes("decimal") || t.includes("float")) return "text-purple-600 bg-purple-50";
    if (t.includes("timestamp") || t.includes("date")) return "text-amber-600 bg-amber-50";
    return "text-gray-600 bg-gray-50";
  };
  return (
    <code className={cn("px-1.5 py-0.5 rounded text-xs font-mono", getColor(type))}>
      {type}
    </code>
  );
}

export default function AssetDetailPage() {
  const asset = mockAsset;
  const Icon = typeIcons[asset.type] || Database;
  const [isFavorited, setIsFavorited] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleCopyGuid = () => {
    navigator.clipboard.writeText(asset.guid);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <MainLayout>
      <div className="container mx-auto p-6 space-y-6">
        {/* Breadcrumb */}
        <Breadcrumb>
          <BreadcrumbList>
            <BreadcrumbItem>
              <BreadcrumbLink asChild>
                <Link href="/asset">数据资产</Link>
              </BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator />
            <BreadcrumbItem>
              <BreadcrumbLink asChild>
                <Link href={`/asset?type=${asset.type}`}>{typeLabels[asset.type]}</Link>
              </BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator />
            <BreadcrumbPage>{asset.name}</BreadcrumbPage>
          </BreadcrumbList>
        </Breadcrumb>

        {/* Header */}
        <div className="flex items-start justify-between">
          <div className="flex items-start gap-4">
            <div className="h-14 w-14 rounded-xl bg-gradient-to-br from-primary/20 to-primary/5 flex items-center justify-center">
              <Icon className="h-7 w-7 text-primary" />
            </div>
            <div>
              <div className="flex items-center gap-3 mb-1">
                <h1 className="text-2xl font-bold">{asset.name}</h1>
                <SecurityBadge level={asset.security_level} />
                <Badge variant="secondary">{typeLabels[asset.type]}</Badge>
              </div>
              <div className="flex items-center gap-4 text-sm text-muted-foreground">
                <span className="flex items-center gap-1">
                  <Copy className="h-3.5 w-3.5 cursor-pointer hover:text-foreground" onClick={handleCopyGuid} />
                  {copied ? <Check className="h-3.5 w-3.5 text-emerald-500" /> : <span>GUID: {asset.guid}</span>}
                </span>
              </div>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              className="gap-2"
              onClick={() => setIsFavorited(!isFavorited)}
            >
              {isFavorited ? (
                <Star className="h-4 w-4 text-amber-500 fill-amber-500" />
              ) : (
                <StarOff className="h-4 w-4" />
              )}
              收藏
            </Button>
            <Button variant="outline" size="sm" className="gap-2">
              <GitBranch className="h-4 w-4" />
              血缘
            </Button>
            <Button variant="outline" size="sm" className="gap-2">
              <Share2 className="h-4 w-4" />
              分享
            </Button>
            <Button size="sm" className="gap-2">
              <Download className="h-4 w-4" />
              导出
            </Button>
          </div>
        </div>

        {/* Info Cards */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">质量评分</p>
                  <div className="flex items-center gap-2 mt-1">
                    <span className="text-2xl font-bold">{asset.quality_score}</span>
                    <QualityScore score={asset.quality_score} size="sm" showLabel />
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <p className="text-sm text-muted-foreground">访问次数</p>
              <p className="text-2xl font-bold mt-1">{asset.usage_count}</p>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <p className="text-sm text-muted-foreground">负责人</p>
              <div className="flex items-center gap-2 mt-1">
                <Avatar className="h-6 w-6">
                  <AvatarFallback>{asset.owner?.name?.charAt(0) || "U"}</AvatarFallback>
                </Avatar>
                <span className="font-medium">{asset.owner?.name}</span>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="pt-6">
              <p className="text-sm text-muted-foreground">更新时间</p>
              <p className="font-medium mt-1">{new Date(asset.updated_at).toLocaleDateString("zh-CN")}</p>
            </CardContent>
          </Card>
        </div>

        {/* Tabs */}
        <Tabs defaultValue="schema" className="space-y-4">
          <TabsList>
            <TabsTrigger value="schema">表结构</TabsTrigger>
            <TabsTrigger value="lineage">血缘关系</TabsTrigger>
            <TabsTrigger value="quality">质量报告</TabsTrigger>
            <TabsTrigger value="metadata">元数据</TabsTrigger>
          </TabsList>

          {/* Schema Tab */}
          <TabsContent value="schema" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">基本信息</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm">{asset.description}</p>
                <div className="flex flex-wrap gap-2 mt-4">
                  {asset.tags?.map((tag) => (
                    <Badge key={tag} variant="secondary">
                      {tag}
                    </Badge>
                  ))}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-base">字段列表 ({asset.schema_definition?.fields.length || 0})</CardTitle>
              </CardHeader>
              <CardContent className="p-0">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-12">#</TableHead>
                      <TableHead>字段名</TableHead>
                      <TableHead>类型</TableHead>
                      <TableHead>描述</TableHead>
                      <TableHead className="w-24">键</TableHead>
                      <TableHead className="w-24">非空</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {asset.schema_definition?.fields.map((field, index) => (
                      <TableRow key={field.name}>
                        <TableCell className="text-muted-foreground">{index + 1}</TableCell>
                        <TableCell className="font-mono font-medium">{field.name}</TableCell>
                        <TableCell><FieldTypeBadge type={field.type} /></TableCell>
                        <TableCell className="text-muted-foreground">{field.description || "-"}</TableCell>
                        <TableCell>
                          {field.is_primary_key && (
                            <Badge variant="warning" className="text-xs">PK</Badge>
                          )}
                        </TableCell>
                        <TableCell>
                          {field.is_nullable ? (
                            <span className="text-muted-foreground">是</span>
                          ) : (
                            <span className="text-foreground">否</span>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Lineage Tab */}
          <TabsContent value="lineage" className="space-y-4">
            <div className="grid gap-6 lg:grid-cols-2">
              {/* Upstream */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-base flex items-center gap-2">
                    <ArrowLeft className="h-4 w-4" />
                    上游数据 ({mockUpstreamLineage.length})
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-2">
                  {mockUpstreamLineage.map((node) => (
                    <LineageCard key={node.id} node={node} />
                  ))}
                </CardContent>
              </Card>

              {/* Current */}
              <Card className="border-primary/50 bg-primary/5">
                <CardHeader>
                  <CardTitle className="text-base flex items-center gap-2">
                    <Database className="h-4 w-4 text-primary" />
                    当前资产
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="p-3 border rounded-lg bg-background">
                    <div className="flex items-center gap-2 mb-1">
                      <Icon className="h-4 w-4" />
                      <span className="font-medium">{asset.name}</span>
                    </div>
                    <p className="text-xs text-muted-foreground line-clamp-1">
                      {asset.description}
                    </p>
                  </div>
                </CardContent>
              </Card>

              {/* Downstream */}
              <Card className="lg:col-span-2">
                <CardHeader>
                  <CardTitle className="text-base flex items-center gap-2">
                    下游数据 ({mockDownstreamLineage.length})
                    <ArrowLeft className="h-4 w-4 rotate-180" />
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid gap-3 md:grid-cols-3">
                    {mockDownstreamLineage.map((node) => (
                      <LineageCard key={node.id} node={node} />
                    ))}
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          {/* Quality Tab */}
          <TabsContent value="quality">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">质量报告</CardTitle>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid gap-4 md:grid-cols-3">
                  <div className="p-4 border rounded-lg">
                    <p className="text-sm text-muted-foreground">完整性</p>
                    <p className="text-2xl font-bold text-emerald-600">98.5%</p>
                  </div>
                  <div className="p-4 border rounded-lg">
                    <p className="text-sm text-muted-foreground">准确性</p>
                    <p className="text-2xl font-bold text-emerald-600">96.2%</p>
                  </div>
                  <div className="p-4 border rounded-lg">
                    <p className="text-sm text-muted-foreground">一致性</p>
                    <p className="text-2xl font-bold text-amber-600">89.8%</p>
                  </div>
                </div>
                <div className="p-4 bg-amber-50 border border-amber-200 rounded-lg">
                  <div className="flex items-start gap-2">
                    <AlertCircle className="h-5 w-5 text-amber-600 mt-0.5" />
                    <div>
                      <p className="font-medium text-amber-800">质量提醒</p>
                      <p className="text-sm text-amber-700 mt-1">
                        发现 12 条数据存在 order_status 字段为空的情况，建议检查数据同步任务。
                      </p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Metadata Tab */}
          <TabsContent value="metadata">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">完整元数据</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="grid gap-4 md:grid-cols-2">
                    <div>
                      <p className="text-sm text-muted-foreground">数据源</p>
                      <p className="font-medium">MySQL - 业务数据库集群</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">数据库</p>
                      <p className="font-medium">ods_db</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">表名</p>
                      <p className="font-medium">ods_orders</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">存储引擎</p>
                      <p className="font-medium">InnoDB</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">创建时间</p>
                      <p className="font-medium">{new Date(asset.created_at).toLocaleString("zh-CN")}</p>
                    </div>
                    <div>
                      <p className="text-sm text-muted-foreground">最后更新</p>
                      <p className="font-medium">{new Date(asset.updated_at).toLocaleString("zh-CN")}</p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </MainLayout>
  );
}
