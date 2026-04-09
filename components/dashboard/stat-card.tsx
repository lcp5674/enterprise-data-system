"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";
import {
  Database,
  Table2,
  FileText,
  Api,
  BarChart3,
  TrendingUp,
  TrendingDown,
  Activity,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";

interface StatCardProps {
  title: string;
  value: string | number;
  change?: number;
  changeLabel?: string;
  icon: LucideIcon;
  className?: string;
}

function StatCard({
  title,
  value,
  change,
  changeLabel,
  icon: Icon,
  className,
}: StatCardProps) {
  const isPositive = change && change > 0;
  const TrendIcon = isPositive ? TrendingUp : TrendingDown;

  return (
    <Card className={cn("", className)}>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {title}
        </CardTitle>
        <div className="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center">
          <Icon className="h-5 w-5 text-primary" />
        </div>
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        {change !== undefined && (
          <div className="flex items-center gap-1 mt-1">
            <TrendIcon
              className={cn(
                "h-4 w-4",
                isPositive ? "text-emerald-500" : "text-red-500"
              )}
            />
            <span
              className={cn(
                "text-sm",
                isPositive ? "text-emerald-500" : "text-red-500"
              )}
            >
              {isPositive ? "+" : ""}
              {change}%
            </span>
            {changeLabel && (
              <span className="text-sm text-muted-foreground">
                {changeLabel}
              </span>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function StatCardSkeleton() {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <Skeleton className="h-4 w-24" />
        <Skeleton className="h-10 w-10 rounded-lg" />
      </CardHeader>
      <CardContent>
        <Skeleton className="h-8 w-16 mb-2" />
        <Skeleton className="h-4 w-20" />
      </CardContent>
    </Card>
  );
}

export { StatCard, StatCardSkeleton };
