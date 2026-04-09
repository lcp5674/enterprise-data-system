"use client";

import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { SecurityBadge } from "@/components/ui/security-badge";
import { QualityScore } from "@/components/ui/quality-score";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { formatRelativeTime } from "@/lib/utils";
import {
  Database,
  FileText,
  Api,
  BarChart3,
  LayoutDashboard,
  Star,
  Eye,
  Clock,
  ArrowRight,
} from "lucide-react";
import type { DataAsset } from "@/types";

interface RecentAssetsProps {
  assets: DataAsset[];
  title?: string;
  viewAllHref?: string;
}

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

function AssetCard({ asset }: { asset: DataAsset }) {
  const Icon = typeIcons[asset.type] || Database;
  
  return (
    <div className="flex items-start gap-3 p-3 rounded-lg border hover:bg-muted/50 transition-colors">
      <div className="h-10 w-10 rounded-lg bg-muted flex items-center justify-center flex-shrink-0">
        <Icon className="h-5 w-5 text-muted-foreground" />
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 mb-1">
          <Link
            href={`/asset/${asset.id}`}
            className="font-medium text-sm hover:text-primary truncate"
          >
            {asset.name}
          </Link>
          <Badge variant="secondary" className="text-xs">
            {typeLabels[asset.type] || asset.type}
          </Badge>
        </div>
        <div className="flex items-center gap-2 text-xs text-muted-foreground">
          <SecurityBadge level={asset.security_level} />
          <QualityScore score={asset.quality_score} size="sm" showLabel={false} />
          <span className="flex items-center gap-1">
            <Eye className="h-3 w-3" />
            {asset.usage_count}
          </span>
        </div>
      </div>
    </div>
  );
}

function AssetCardSkeleton() {
  return (
    <div className="flex items-start gap-3 p-3 rounded-lg border">
      <Skeleton className="h-10 w-10 rounded-lg" />
      <div className="flex-1">
        <Skeleton className="h-4 w-32 mb-2" />
        <Skeleton className="h-3 w-48" />
      </div>
    </div>
  );
}

export function RecentAssets({
  assets,
  title = "最近访问",
  viewAllHref = "/recent",
}: RecentAssetsProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle className="text-base font-medium flex items-center gap-2">
          <Clock className="h-4 w-4" />
          {title}
        </CardTitle>
        <Link href={viewAllHref}>
          <Button variant="ghost" size="sm" className="gap-1">
            查看全部
            <ArrowRight className="h-4 w-4" />
          </Button>
        </Link>
      </CardHeader>
      <CardContent>
        <div className="space-y-2">
          {assets.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <Clock className="h-8 w-8 mx-auto mb-2 opacity-50" />
              <p className="text-sm">暂无最近访问记录</p>
            </div>
          ) : (
            assets.slice(0, 5).map((asset) => (
              <AssetCard key={asset.id} asset={asset} />
            ))
          )}
        </div>
      </CardContent>
    </Card>
  );
}

export function FavoriteAssets({
  assets,
  title = "我的收藏",
  viewAllHref = "/favorites",
}: RecentAssetsProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle className="text-base font-medium flex items-center gap-2">
          <Star className="h-4 w-4 text-amber-500" />
          {title}
        </CardTitle>
        <Link href={viewAllHref}>
          <Button variant="ghost" size="sm" className="gap-1">
            查看全部
            <ArrowRight className="h-4 w-4" />
          </Button>
        </Link>
      </CardHeader>
      <CardContent>
        <div className="space-y-2">
          {assets.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <Star className="h-8 w-8 mx-auto mb-2 opacity-50" />
              <p className="text-sm">暂无收藏资产</p>
            </div>
          ) : (
            assets.slice(0, 5).map((asset) => (
              <AssetCard key={asset.id} asset={asset} />
            ))
          )}
        </div>
      </CardContent>
    </Card>
  );
}

export { AssetCard, AssetCardSkeleton };
