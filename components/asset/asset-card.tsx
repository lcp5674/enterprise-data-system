"use client";

import Link from "next/link";
import { Card, CardContent, CardFooter, CardHeader } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { SecurityBadge } from "@/components/ui/security-badge";
import { QualityScore } from "@/components/ui/quality-score";
import {
  Database,
  FileText,
  Api,
  BarChart3,
  LayoutDashboard,
  Star,
  StarOff,
  GitBranch,
  User,
  Clock,
  MoreHorizontal,
} from "lucide-react";
import type { DataAsset } from "@/types";

interface AssetCardProps {
  asset: DataAsset;
  isFavorited?: boolean;
  onToggleFavorite?: (assetId: string) => void;
  showActions?: boolean;
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

export function AssetCard({
  asset,
  isFavorited = false,
  onToggleFavorite,
  showActions = true,
}: AssetCardProps) {
  const Icon = typeIcons[asset.type] || Database;

  const handleFavoriteClick = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    onToggleFavorite?.(asset.id);
  };

  return (
    <Link href={`/asset/${asset.id}`}>
      <Card className="h-full hover:shadow-md transition-shadow cursor-pointer group">
        <CardHeader className="pb-3">
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-3">
              <div className="h-12 w-12 rounded-lg bg-gradient-to-br from-primary/20 to-primary/5 flex items-center justify-center group-hover:from-primary/30 group-hover:to-primary/10 transition-colors">
                <Icon className="h-6 w-6 text-primary" />
              </div>
              <div>
                <h3 className="font-semibold text-base group-hover:text-primary transition-colors line-clamp-1">
                  {asset.name}
                </h3>
                <p className="text-xs text-muted-foreground">
                  {typeLabels[asset.type] || asset.type}
                </p>
              </div>
            </div>
            {showActions && onToggleFavorite && (
              <Button
                variant="ghost"
                size="icon"
                className="h-8 w-8"
                onClick={handleFavoriteClick}
              >
                {isFavorited ? (
                  <Star className="h-4 w-4 text-amber-500 fill-amber-500" />
                ) : (
                  <StarOff className="h-4 w-4 text-muted-foreground" />
                )}
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent className="pb-3">
          <p className="text-sm text-muted-foreground line-clamp-2 mb-3">
            {asset.description || "暂无描述"}
          </p>
          <div className="flex flex-wrap gap-2">
            <SecurityBadge level={asset.security_level} />
            <Badge variant="outline" className="text-xs">
              <QualityScore score={asset.quality_score} size="sm" showLabel={false} />
            </Badge>
            {asset.tags && asset.tags.length > 0 && (
              <Badge variant="secondary" className="text-xs">
                {asset.tags.slice(0, 2).join(", ")}
                {asset.tags.length > 2 && `+${asset.tags.length - 2}`}
              </Badge>
            )}
          </div>
        </CardContent>
        <CardFooter className="pt-0 text-xs text-muted-foreground">
          <div className="flex items-center gap-4">
            {asset.owner && (
              <span className="flex items-center gap-1">
                <User className="h-3 w-3" />
                {asset.owner.name || asset.owner.email}
              </span>
            )}
            <span className="flex items-center gap-1">
              <Clock className="h-3 w-3" />
              {new Date(asset.updated_at).toLocaleDateString("zh-CN")}
            </span>
          </div>
        </CardFooter>
      </Card>
    </Link>
  );
}

interface AssetListItemProps {
  asset: DataAsset;
  isFavorited?: boolean;
  onToggleFavorite?: (assetId: string) => void;
}

export function AssetListItem({
  asset,
  isFavorited = false,
  onToggleFavorite,
}: AssetListItemProps) {
  const Icon = typeIcons[asset.type] || Database;

  return (
    <div className="flex items-center gap-4 p-4 border-b hover:bg-muted/50 transition-colors">
      <div className="h-10 w-10 rounded-lg bg-muted flex items-center justify-center flex-shrink-0">
        <Icon className="h-5 w-5 text-muted-foreground" />
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 mb-1">
          <Link
            href={`/asset/${asset.id}`}
            className="font-medium hover:text-primary truncate"
          >
            {asset.name}
          </Link>
          <SecurityBadge level={asset.security_level} />
        </div>
        <p className="text-sm text-muted-foreground truncate">
          {asset.description || "暂无描述"}
        </p>
      </div>
      <div className="flex items-center gap-4 flex-shrink-0">
        <QualityScore score={asset.quality_score} size="sm" />
        <div className="text-right">
          <div className="text-sm font-medium">{asset.usage_count}</div>
          <div className="text-xs text-muted-foreground">访问次数</div>
        </div>
        {onToggleFavorite && (
          <Button
            variant="ghost"
            size="icon"
            className="h-8 w-8"
            onClick={() => onToggleFavorite(asset.id)}
          >
            {isFavorited ? (
              <Star className="h-4 w-4 text-amber-500 fill-amber-500" />
            ) : (
              <StarOff className="h-4 w-4 text-muted-foreground" />
            )}
          </Button>
        )}
      </div>
    </div>
  );
}
