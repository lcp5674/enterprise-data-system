"use client";

import { useState } from "react";
import { MainLayout } from "@/components/layout/main-layout";
import { AssetCard } from "@/components/asset/asset-card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import {
  Search,
  Filter,
  Grid3X3,
  List,
  Map,
  Table2,
  FileText,
  Api,
  BarChart3,
  LayoutDashboard,
  ChevronRight,
  FolderOpen,
  Database,
  X,
} from "lucide-react";
import Link from "next/link";
import type { DataAsset, AssetType, SecurityLevel } from "@/types";

interface AssetCategory {
  id: string;
  name: string;
  icon: typeof Database;
  children?: AssetCategory[];
  assetCount?: number;
}

// Mock categories
const categories: AssetCategory[] = [
  {
    id: "1",
    name: "ODS层",
    icon: Database,
    assetCount: 156,
    children: [
      { id: "1-1", name: "订单数据", icon: Table2, assetCount: 45 },
      { id: "1-2", name: "客户数据", icon: Table2, assetCount: 32 },
      { id: "1-3", name: "商品数据", icon: Table2, assetCount: 28 },
      { id: "1-4", name: "库存数据", icon: Table2, assetCount: 51 },
    ],
  },
  {
    id: "2",
    name: "DWD层",
    icon: Database,
    assetCount: 234,
    children: [
      { id: "2-1", name: "订单明细", icon: Table2, assetCount: 67 },
      { id: "2-2", name: "客户维度", icon: Table2, assetCount: 45 },
      { id: "2-3", name: "商品维度", icon: Table2, assetCount: 52 },
      { id: "2-4", name: "时间维度", icon: Table2, assetCount: 70 },
    ],
  },
  {
    id: "3",
    name: "DWS层",
    icon: Database,
    assetCount: 189,
    children: [
      { id: "3-1", name: "交易汇总", icon: Table2, assetCount: 89 },
      { id: "3-2", name: "客户群组", icon: Table2, assetCount: 100 },
    ],
  },
  {
    id: "4",
    name: "ADS层",
    icon: BarChart3,
    assetCount: 156,
    children: [
      { id: "4-1", name: "业务报表", icon: LayoutDashboard, assetCount: 45 },
      { id: "4-2", name: "指标数据", icon: BarChart3, assetCount: 111 },
    ],
  },
  {
    id: "5",
    name: "数据接口",
    icon: Api,
    assetCount: 124,
    children: [
      { id: "5-1", name: "用户服务", icon: Api, assetCount: 34 },
      { id: "5-2", name: "订单服务", icon: Api, assetCount: 45 },
      { id: "5-3", name: "商品服务", icon: Api, assetCount: 45 },
    ],
  },
  {
    id: "6",
    name: "数据文件",
    icon: FileText,
    assetCount: 156,
    children: [
      { id: "6-1", name: "配置文件", icon: FileText, assetCount: 23 },
      { id: "6-2", name: "日志文件", icon: FileText, assetCount: 78 },
      { id: "6-3", name: "导出报告", icon: FileText, assetCount: 55 },
    ],
  },
];

// Mock assets
const mockAssets: DataAsset[] = [
  {
    id: "1",
    guid: "asset-001",
    name: "ods_orders",
    description: "订单原始数据表，包含所有渠道订单原始数据，数据每日凌晨2点更新",
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
    updated_at: "",
  },
  {
    id: "2",
    guid: "asset-002",
    name: "dim_customer",
    description: "客户维度表，存储客户维度信息，支持SCD Type 2历史变化",
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
    updated_at: "",
  },
  {
    id: "3",
    guid: "asset-003",
    name: "dwd_orders",
    description: "订单明细宽表，ODS层清洗后的订单数据",
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
    updated_at: "",
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
    updated_at: "",
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
    updated_at: "",
  },
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
];

type ViewMode = "grid" | "list" | "map";

export default function AssetPage() {
  const [viewMode, setViewMode] = useState<ViewMode>("grid");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [typeFilter, setTypeFilter] = useState<AssetType | "all">("all");
  const [securityFilter, setSecurityFilter] = useState<SecurityLevel | "all">("all");
  const [expandedCategories, setExpandedCategories] = useState<string[]>(["1", "2"]);
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);

  const toggleCategory = (id: string) => {
    setExpandedCategories((prev) =>
      prev.includes(id) ? prev.filter((c) => c !== id) : [...prev, id]
    );
  };

  const filteredAssets = mockAssets.filter((asset) => {
    const matchesSearch =
      searchKeyword === "" ||
      asset.name.toLowerCase().includes(searchKeyword.toLowerCase()) ||
      asset.description?.toLowerCase().includes(searchKeyword.toLowerCase());
    const matchesType = typeFilter === "all" || asset.type === typeFilter;
    const matchesSecurity =
      securityFilter === "all" || asset.security_level === securityFilter;
    return matchesSearch && matchesType && matchesSecurity;
  });

  const renderCategoryTree = (cats: AssetCategory[], level = 0) => {
    return cats.map((cat) => (
      <div key={cat.id}>
        <button
          onClick={() => {
            if (cat.children && cat.children.length > 0) {
              toggleCategory(cat.id);
            }
            setSelectedCategory(cat.id);
          }}
          className={`w-full flex items-center gap-2 px-3 py-2 hover:bg-muted/50 rounded-lg transition-colors ${
            selectedCategory === cat.id ? "bg-primary/10 text-primary" : ""
          }`}
          style={{ paddingLeft: `${12 + level * 16}px` }}
        >
          {cat.children && cat.children.length > 0 && (
            <ChevronRight
              className={`h-4 w-4 transition-transform ${
                expandedCategories.includes(cat.id) ? "rotate-90" : ""
              }`}
            />
          )}
          <cat.icon className="h-4 w-4" />
          <span className="flex-1 text-left text-sm">{cat.name}</span>
          <Badge variant="secondary" className="text-xs">
            {cat.assetCount}
          </Badge>
        </button>
        {cat.children && expandedCategories.includes(cat.id) && (
          <div>{renderCategoryTree(cat.children, level + 1)}</div>
        )}
      </div>
    ));
  };

  return (
    <MainLayout>
      <div className="flex h-[calc(100vh-4rem)]">
        {/* Left Sidebar - Category Tree */}
        <div className="w-64 border-r bg-background overflow-y-auto">
          <div className="p-4 border-b">
            <h2 className="font-semibold flex items-center gap-2">
              <FolderOpen className="h-4 w-4" />
              数据目录
            </h2>
          </div>
          <div className="p-2">{renderCategoryTree(categories)}</div>
        </div>

        {/* Main Content */}
        <div className="flex-1 overflow-y-auto">
          <div className="container mx-auto p-6 space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-2xl font-bold">数据资产</h1>
                <p className="text-muted-foreground">
                  浏览和发现企业数据资产
                </p>
              </div>
              <div className="flex items-center gap-2">
                <Button variant="outline" size="sm" className="gap-2">
                  <Map className="h-4 w-4" />
                  地图
                </Button>
                <Button size="sm">注册资产</Button>
              </div>
            </div>

            {/* Search and Filters */}
            <Card className="p-4">
              <div className="flex flex-col lg:flex-row gap-4">
                <div className="relative flex-1">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="搜索资产名称或描述..."
                    value={searchKeyword}
                    onChange={(e) => setSearchKeyword(e.target.value)}
                    className="pl-9"
                  />
                </div>
                <div className="flex gap-2 flex-wrap">
                  <Select
                    value={typeFilter}
                    onValueChange={(v) => setTypeFilter(v as AssetType | "all")}
                  >
                    <SelectTrigger className="w-32">
                      <SelectValue placeholder="资产类型" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">全部类型</SelectItem>
                      <SelectItem value="table">
                        <div className="flex items-center gap-2">
                          <Table2 className="h-4 w-4" />
                          数据表
                        </div>
                      </SelectItem>
                      <SelectItem value="api">
                        <div className="flex items-center gap-2">
                          <Api className="h-4 w-4" />
                          API接口
                        </div>
                      </SelectItem>
                      <SelectItem value="metric">
                        <div className="flex items-center gap-2">
                          <BarChart3 className="h-4 w-4" />
                          指标
                        </div>
                      </SelectItem>
                      <SelectItem value="file">
                        <div className="flex items-center gap-2">
                          <FileText className="h-4 w-4" />
                          数据文件
                        </div>
                      </SelectItem>
                    </SelectContent>
                  </Select>
                  <Select
                    value={securityFilter}
                    onValueChange={(v) =>
                      setSecurityFilter(v as SecurityLevel | "all")
                    }
                  >
                    <SelectTrigger className="w-32">
                      <SelectValue placeholder="安全等级" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">全部等级</SelectItem>
                      <SelectItem value="public">公开</SelectItem>
                      <SelectItem value="internal">内部</SelectItem>
                      <SelectItem value="sensitive">敏感</SelectItem>
                      <SelectItem value="confidential">机密</SelectItem>
                    </SelectContent>
                  </Select>
                  <div className="flex border rounded-md">
                    <Button
                      variant={viewMode === "grid" ? "secondary" : "ghost"}
                      size="sm"
                      className="rounded-r-none"
                      onClick={() => setViewMode("grid")}
                    >
                      <Grid3X3 className="h-4 w-4" />
                    </Button>
                    <Button
                      variant={viewMode === "list" ? "secondary" : "ghost"}
                      size="sm"
                      className="rounded-none border-x"
                      onClick={() => setViewMode("list")}
                    >
                      <List className="h-4 w-4" />
                    </Button>
                    <Button
                      variant={viewMode === "map" ? "secondary" : "ghost"}
                      size="sm"
                      className="rounded-l-none"
                      onClick={() => setViewMode("map")}
                    >
                      <Map className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </div>
              {/* Active Filters */}
              {(typeFilter !== "all" || securityFilter !== "all" || searchKeyword) && (
                <div className="flex items-center gap-2 mt-3">
                  <span className="text-sm text-muted-foreground">筛选条件：</span>
                  {searchKeyword && (
                    <Badge variant="secondary" className="gap-1">
                      {searchKeyword}
                      <X
                        className="h-3 w-3 cursor-pointer"
                        onClick={() => setSearchKeyword("")}
                      />
                    </Badge>
                  )}
                  {typeFilter !== "all" && (
                    <Badge variant="secondary" className="gap-1">
                      {typeFilter}
                      <X
                        className="h-3 w-3 cursor-pointer"
                        onClick={() => setTypeFilter("all")}
                      />
                    </Badge>
                  )}
                  {securityFilter !== "all" && (
                    <Badge variant="secondary" className="gap-1">
                      {securityFilter}
                      <X
                        className="h-3 w-3 cursor-pointer"
                        onClick={() => setSecurityFilter("all")}
                      />
                    </Badge>
                  )}
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => {
                      setSearchKeyword("");
                      setTypeFilter("all");
                      setSecurityFilter("all");
                    }}
                  >
                    清除
                  </Button>
                </div>
              )}
            </Card>

            {/* Results */}
            <div className="flex items-center justify-between">
              <p className="text-sm text-muted-foreground">
                共找到 <span className="font-medium">{filteredAssets.length}</span> 个资产
              </p>
            </div>

            {/* Asset Grid/List */}
            {viewMode === "grid" ? (
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {filteredAssets.map((asset) => (
                  <AssetCard key={asset.id} asset={asset} />
                ))}
              </div>
            ) : viewMode === "list" ? (
              <Card>
                <div className="divide-y">
                  {filteredAssets.map((asset) => (
                    <div
                      key={asset.id}
                      className="flex items-center gap-4 p-4 hover:bg-muted/50 transition-colors"
                    >
                      <div className="h-10 w-10 rounded-lg bg-muted flex items-center justify-center">
                        {asset.type === "table" && <Table2 className="h-5 w-5" />}
                        {asset.type === "api" && <Api className="h-5 w-5" />}
                        {asset.type === "metric" && <BarChart3 className="h-5 w-5" />}
                        {asset.type === "file" && <FileText className="h-5 w-5" />}
                        {asset.type === "dashboard" && <LayoutDashboard className="h-5 w-5" />}
                      </div>
                      <div className="flex-1 min-w-0">
                        <Link
                          href={`/asset/${asset.id}`}
                          className="font-medium hover:text-primary"
                        >
                          {asset.name}
                        </Link>
                        <p className="text-sm text-muted-foreground truncate">
                          {asset.description}
                        </p>
                      </div>
                      <div className="flex items-center gap-4">
                        <Badge
                          variant={
                            asset.security_level === "confidential"
                              ? "destructive"
                              : asset.security_level === "sensitive"
                              ? "warning"
                              : "secondary"
                          }
                        >
                          {asset.security_level === "public"
                            ? "公开"
                            : asset.security_level === "internal"
                            ? "内部"
                            : asset.security_level === "sensitive"
                            ? "敏感"
                            : "机密"}
                        </Badge>
                        <span className="text-sm text-muted-foreground w-16 text-right">
                          {asset.quality_score}分
                        </span>
                        <span className="text-sm text-muted-foreground w-16 text-right">
                          {asset.usage_count}次
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </Card>
            ) : (
              <Card className="p-8 text-center">
                <Map className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                <h3 className="font-medium mb-2">地图视图</h3>
                <p className="text-sm text-muted-foreground">
                  地图视图正在开发中，敬请期待
                </p>
              </Card>
            )}
          </div>
        </div>
      </div>
    </MainLayout>
  );
}
