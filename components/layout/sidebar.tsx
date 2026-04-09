"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  Home,
  Map,
  Search,
  GitBranch,
  Shield,
  CheckCircle,
  TrendingUp,
  MessageSquare,
  ChevronDown,
  ChevronRight,
  Database,
  FileText,
  BarChart3,
  Api,
  FolderOpen,
  Star,
  Clock,
  Layers,
} from "lucide-react";
import { useState } from "react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";

interface SidebarProps {
  collapsed?: boolean;
  onToggle?: () => void;
}

const menuGroups = [
  {
    title: "工作台",
    items: [
      { href: "/", label: "首页工作台", icon: Home },
      { href: "/recent", label: "最近访问", icon: Clock },
      { href: "/favorites", label: "我的收藏", icon: Star },
    ],
  },
  {
    title: "资产发现",
    items: [
      { href: "/asset", label: "数据地图", icon: Map },
      { href: "/search", label: "资产搜索", icon: Search },
      { href: "/directory", label: "数据目录", icon: FolderOpen },
    ],
  },
  {
    title: "资产治理",
    items: [
      { href: "/lineage", label: "数据血缘", icon: GitBranch },
      { href: "/model", label: "数据模型", icon: Layers },
      { href: "/standard", label: "数据标准", icon: FileText },
      { href: "/metric", label: "指标管理", icon: BarChart3 },
    ],
  },
  {
    title: "质量与安全",
    items: [
      { href: "/quality", label: "质量检测", icon: CheckCircle },
      { href: "/security", label: "安全中心", icon: Shield },
    ],
  },
  {
    title: "服务与应用",
    items: [
      { href: "/service", label: "资产服务化", icon: Api },
      { href: "/value", label: "价值评估", icon: TrendingUp },
    ],
  },
  {
    title: "反馈与协作",
    items: [
      { href: "/feedback", label: "反馈中心", icon: MessageSquare },
    ],
  },
];

export function Sidebar({ collapsed = false, onToggle }: SidebarProps) {
  const pathname = usePathname();
  const [expandedGroups, setExpandedGroups] = useState<string[]>(
    menuGroups.map((g) => g.title)
  );

  const toggleGroup = (title: string) => {
    setExpandedGroups((prev) =>
      prev.includes(title)
        ? prev.filter((t) => t !== title)
        : [...prev, title]
    );
  };

  const isActive = (href: string) => {
    if (href === "/") return pathname === "/";
    return pathname.startsWith(href);
  };

  if (collapsed) {
    return (
      <aside className="w-16 border-r bg-background flex flex-col items-center py-4 gap-2">
        {menuGroups.map((group) =>
          group.items.map((item) => {
            const Icon = item.icon;
            return (
              <Link key={item.href} href={item.href}>
                <Button
                  variant={isActive(item.href) ? "secondary" : "ghost"}
                  size="icon"
                  className={cn(
                    "h-10 w-10",
                    isActive(item.href) && "bg-primary/10 text-primary"
                  )}
                  title={item.label}
                >
                  <Icon className="h-5 w-5" />
                </Button>
              </Link>
            );
          })
        )}
      </aside>
    );
  }

  return (
    <aside className="w-64 border-r bg-background flex flex-col">
      <div className="flex-1 overflow-y-auto py-4">
        {menuGroups.map((group) => {
          const isExpanded = expandedGroups.includes(group.title);
          return (
            <Collapsible
              key={group.title}
              open={isExpanded}
              onOpenChange={() => toggleGroup(group.title)}
            >
              <div className="px-4 py-2">
                <CollapsibleTrigger asChild>
                  <Button
                    variant="ghost"
                    className="w-full justify-between text-xs font-semibold text-muted-foreground hover:text-foreground"
                    size="sm"
                  >
                    {group.title}
                    {isExpanded ? (
                      <ChevronDown className="h-4 w-4" />
                    ) : (
                      <ChevronRight className="h-4 w-4" />
                    )}
                  </Button>
                </CollapsibleTrigger>
              </div>
              <CollapsibleContent>
                <div className="px-2 pb-2">
                  {group.items.map((item) => {
                    const Icon = item.icon;
                    return (
                      <Link key={item.href} href={item.href}>
                        <Button
                          variant={isActive(item.href) ? "secondary" : "ghost"}
                          className={cn(
                            "w-full justify-start gap-2 text-sm",
                            isActive(item.href) &&
                              "bg-primary/10 text-primary"
                          )}
                          size="sm"
                        >
                          <Icon className="h-4 w-4" />
                          {item.label}
                        </Button>
                      </Link>
                    );
                  })}
                </div>
              </CollapsibleContent>
            </Collapsible>
          );
        })}
      </div>

      {/* Asset Type Quick Access */}
      <div className="border-t p-4">
        <p className="text-xs font-semibold text-muted-foreground mb-2">
          快速入口
        </p>
        <div className="grid grid-cols-2 gap-2">
          <Link href="/asset?type=table">
            <Button variant="outline" size="sm" className="w-full gap-1 text-xs">
              <Database className="h-3 w-3" />
              数据表
            </Button>
          </Link>
          <Link href="/asset?type=api">
            <Button variant="outline" size="sm" className="w-full gap-1 text-xs">
              <Api className="h-3 w-3" />
              API接口
            </Button>
          </Link>
          <Link href="/asset?type=file">
            <Button variant="outline" size="sm" className="w-full gap-1 text-xs">
              <FileText className="h-3 w-3" />
              数据文件
            </Button>
          </Link>
          <Link href="/asset?type=metric">
            <Button variant="outline" size="sm" className="w-full gap-1 text-xs">
              <BarChart3 className="h-3 w-3" />
              指标数据
            </Button>
          </Link>
        </div>
      </div>
    </aside>
  );
}
