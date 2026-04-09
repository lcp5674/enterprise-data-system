"use client";

import { useState, useEffect } from "react";
import { MainLayout } from "@/components/layout/main-layout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { SecurityBadge } from "@/components/ui/security-badge";
import { QualityScore } from "@/components/ui/quality-score";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Search,
  Filter,
  X,
  Clock,
  TrendingUp,
  Star,
  Database,
  FileText,
  Api,
  BarChart3,
  LayoutDashboard,
  Sparkles,
  ArrowUpDown,
} from "lucide-react";
import Link from "next/link";
import { cn } from "@/lib/utils";
import type { DataAsset, AssetType, SecurityLevel } from "@/types";

// Mock search results
const mockSearchResults: DataAsset[] = [
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
];

const mockSuggestions = [
  "订单相关的数据表",
  "客户维度表",
  "收入报表",
  "API接口",
];

const mockRecentSearches = [
  "ods_orders",
  "客户画像",
  "收入统计",
];

type SortOption = "relevance" | "name" | "quality" | "usage" | "updated";

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

export default function SearchPage() {
  const [keyword, setKeyword] = useState("");
  const [typeFilter, setTypeFilter] = useState<AssetType | "all">("all");
  const [securityFilter, setSecurityFilter] = useState<SecurityLevel | "all">("all");
  const [sortBy, setSortBy] = useState<SortOption>("relevance");
  const [isSearching, setIsSearching] = useState(false);
  const [showSuggestions, setShowSuggestions] = useState(false);

  const filteredResults = mockSearchResults.filter((asset) => {
    const matchesSearch =
      keyword === "" ||
      asset.name.toLowerCase().includes(keyword.toLowerCase()) ||
      asset.description?.toLowerCase().includes(keyword.toLowerCase()) ||
      asset.tags?.some((tag) => tag.toLowerCase().includes(keyword.toLowerCase()));
    const matchesType = typeFilter === "all" || asset.type === typeFilter;
    const matchesSecurity =
      securityFilter === "all" || asset.security_level === securityFilter;
    return matchesSearch && matchesType && matchesSecurity;
  });

  const sortedResults = [...filteredResults].sort((a, b) => {
    switch (sortBy) {
      case "name":
        return a.name.localeCompare(b.name);
      case "quality":
        return b.quality_score - a.quality_score;
      case "usage":
        return b.usage_count - a.usage_count;
      case "updated":
        return new Date(b.updated_at).getTime() - new Date(a.updated_at).getTime();
      default:
        return 0;
    }
  });

  const handleSearch = (term: string) => {
    setKeyword(term);
    setIsSearching(true);
    setTimeout(() => setIsSearching(false), 500);
  };

  return (
    <MainLayout>
      <div className="container mx-auto p-6 space-y-6 max-w-5xl">
        {/* Header */}
        <div>
          <h1 className="text-2xl font-bold">资产搜索</h1>
          <p className="text-muted-foreground">
            通过关键字搜索数据资产，支持智能联想
          </p>
        </div>

        {/* Search Box */}
        <Card className="p-6">
          <div className="relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
            <Input
              placeholder="搜索资产名称、描述或标签..."
              value={keyword}
              onChange={(e) => handleSearch(e.target.value)}
              onFocus={() => setShowSuggestions(true)}
              onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
              className="pl-12 h-12 text-lg"
            />
            {keyword && (
              <Button
                variant="ghost"
                size="icon"
                className="absolute right-2 top-1/2 -translate-y-1/2"
                onClick={() => setKeyword("")}
              >
                <X className="h-4 w-4" />
              </Button>
            )}

            {/* Suggestions Dropdown */}
            {showSuggestions && keyword === "" && (
              <div className="absolute top-full left-0 right-0 mt-2 bg-background border rounded-lg shadow-lg z-10">
                {/* Smart Suggestions */}
                <div className="p-3 border-b">
                  <p className="text-xs text-muted-foreground mb-2 flex items-center gap-1">
                    <Sparkles className="h-3 w-3" />
                    智能推荐
                  </p>
                  <div className="flex flex-wrap gap-2">
                    {mockSuggestions.map((suggestion) => (
                      <button
                        key={suggestion}
                        className="px-3 py-1 text-sm bg-primary/10 text-primary rounded-full hover:bg-primary/20 transition-colors"
                        onClick={() => handleSearch(suggestion)}
                      >
                        {suggestion}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Recent Searches */}
                <div className="p-3">
                  <p className="text-xs text-muted-foreground mb-2 flex items-center gap-1">
                    <Clock className="h-3 w-3" />
                    最近搜索
                  </p>
                  <div className="space-y-1">
                    {mockRecentSearches.map((search) => (
                      <button
                        key={search}
                        className="w-full text-left px-3 py-2 text-sm hover:bg-muted rounded-md transition-colors flex items-center gap-2"
                        onClick={() => handleSearch(search)}
                      >
                        <Search className="h-3 w-3 text-muted-foreground" />
                        {search}
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Quick Filters */}
          <div className="flex flex-wrap gap-2 mt-4">
            <Select value={typeFilter} onValueChange={(v) => setTypeFilter(v as AssetType | "all")}>
              <SelectTrigger className="w-32">
                <SelectValue placeholder="资产类型" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">全部类型</SelectItem>
                <SelectItem value="table">数据表</SelectItem>
                <SelectItem value="api">API接口</SelectItem>
                <SelectItem value="metric">指标</SelectItem>
                <SelectItem value="file">数据文件</SelectItem>
              </SelectContent>
            </Select>

            <Select value={securityFilter} onValueChange={(v) => setSecurityFilter(v as SecurityLevel | "all")}>
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

            <Select value={sortBy} onValueChange={(v) => setSortBy(v as SortOption)}>
              <SelectTrigger className="w-36">
                <SelectValue placeholder="排序方式" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="relevance">
                  <div className="flex items-center gap-2">
                    <ArrowUpDown className="h-4 w-4" />
                    相关度
                  </div>
                </SelectItem>
                <SelectItem value="name">名称</SelectItem>
                <SelectItem value="quality">质量评分</SelectItem>
                <SelectItem value="usage">使用热度</SelectItem>
                <SelectItem value="updated">更新时间</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </Card>

        {/* Results */}
        <div className="flex items-center justify-between">
          <p className="text-sm text-muted-foreground">
            找到 <span className="font-medium">{sortedResults.length}</span> 个相关资产
          </p>
          {(typeFilter !== "all" || securityFilter !== "all" || keyword) && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setKeyword("");
                setTypeFilter("all");
                setSecurityFilter("all");
              }}
            >
              清除筛选
            </Button>
          )}
        </div>

        {/* Results List */}
        <div className="space-y-3">
          {sortedResults.map((asset) => {
            const Icon = typeIcons[asset.type] || Database;
            return (
              <Link key={asset.id} href={`/asset/${asset.id}`}>
                <Card className="hover:shadow-md transition-shadow cursor-pointer group">
                  <CardContent className="p-4">
                    <div className="flex items-start gap-4">
                      <div className="h-12 w-12 rounded-lg bg-gradient-to-br from-primary/20 to-primary/5 flex items-center justify-center flex-shrink-0 group-hover:from-primary/30 group-hover:to-primary/10 transition-colors">
                        <Icon className="h-6 w-6 text-primary" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-1">
                          <h3 className="font-semibold group-hover:text-primary transition-colors">
                            {asset.name}
                          </h3>
                          <Badge variant="secondary" className="text-xs">
                            {typeLabels[asset.type]}
                          </Badge>
                          <SecurityBadge level={asset.security_level} />
                        </div>
                        <p className="text-sm text-muted-foreground line-clamp-2 mb-2">
                          {asset.description}
                        </p>
                        <div className="flex items-center gap-4 text-xs text-muted-foreground">
                          <span className="flex items-center gap-1">
                            <TrendingUp className="h-3 w-3" />
                            {asset.usage_count} 次访问
                          </span>
                          <span className="flex items-center gap-1">
                            <Star className="h-3 w-3" />
                            质量 {asset.quality_score}分
                          </span>
                          {asset.owner && (
                            <span>负责人: {asset.owner.name}</span>
                          )}
                          {asset.tags && asset.tags.length > 0 && (
                            <span className="flex items-center gap-1">
                              标签: {asset.tags.slice(0, 3).join(", ")}
                            </span>
                          )}
                        </div>
                      </div>
                      <QualityScore score={asset.quality_score} size="md" />
                    </div>
                  </CardContent>
                </Card>
              </Link>
            );
          })}

          {sortedResults.length === 0 && (
            <Card className="p-12 text-center">
              <Search className="h-12 w-12 mx-auto mb-4 text-muted-foreground opacity-50" />
              <h3 className="text-lg font-medium mb-2">未找到匹配的资产</h3>
              <p className="text-muted-foreground">
                请尝试调整搜索关键字或筛选条件
              </p>
            </Card>
          )}
        </div>
      </div>
    </MainLayout>
  );
}
